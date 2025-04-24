/*
 * Copyright (c) 2025 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.locationtech.jts.operation.relateng.IntersectionMatrixPattern;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.locationtech.jts.util.IntArrayList;

class CleanCoverage {

  /**
   * The areas in the clean coverage.
   * Entries may be null, if no resultant corresponded to the input area.
   */
  private CleanArea[] cov;
  //-- used for finding areas to merge gaps
  private Quadtree covIndex;

  public CleanCoverage(int size) {
    cov = new CleanArea[size]; 
  }

  public void add(int i, Polygon poly) {
    if (cov[i] == null) {
      cov[i] = new CleanArea();
    }
    cov[i].add(poly);
  }
  
  public void mergeOverlap(Polygon overlap, MergeStrategy mergeStrategy, IntArrayList parentIndexes) {
    int mergeTarget = findMergeTarget(overlap, mergeStrategy, parentIndexes, cov);
    add(mergeTarget, overlap);
  }

  public static int findMergeTarget(Polygon poly, MergeStrategy strat, IntArrayList parentIndexes, CleanArea[] cov) {
    //-- sort parent indexes ascending, so that overlaps merge to first parent by default
    int[] indexesAsc = parentIndexes.toArray();
    Arrays.sort(indexesAsc);
    for (int i = 0; i < indexesAsc.length; i++) {
      int index = indexesAsc[i];
      strat.checkMergeTarget(index, cov[index], poly);
    }
    return strat.getTarget();
  }

  public void mergeGaps(List<Polygon> gaps) {
    createIndex();
    for (Polygon gap : gaps) {
      mergeGap(gap);
    }
  }
  
  private void mergeGap(Polygon gap) {
    List<CleanArea> adjacents = findAdjacentAreas(gap);
    /**
     * No adjacent means this is likely an artifact
     * of an invalid input polygon. 
     * Discard polygon.
     */
    if (adjacents.size() == 0)
      return;
    
    CleanArea mergeTarget = findMaxBorderLength(gap, adjacents);
    covIndex.remove(mergeTarget.getEnvelope(), mergeTarget);
    mergeTarget.add(gap);
    covIndex.insert(mergeTarget.getEnvelope(), mergeTarget);
  }
  
  private CleanArea findMaxBorderLength(Polygon poly, List<CleanArea> areas) {
    double maxLen = 0;
    CleanArea maxLenArea = null;
    for (CleanArea a : areas) {
      double len = a.getBorderLength(poly);
      if (maxLenArea == null || len > maxLen) {
        maxLen = len;
        maxLenArea = a;
      }
    }
    return maxLenArea;
    
  }

  private List<CleanArea> findAdjacentAreas(Geometry poly) {
    List<CleanArea> adjacents = new ArrayList<CleanArea>();
    RelateNG rel = RelateNG.prepare(poly);
    Envelope queryEnv = poly.getEnvelopeInternal();
    @SuppressWarnings("unchecked")
    List<CleanArea> candidateAdjIndex = covIndex.query(queryEnv);
    for (CleanArea area : candidateAdjIndex) {
      if (area != null && area.isAdjacent(rel)) {
        adjacents.add(area);
      }
    }
    return adjacents;
  }

  private void createIndex() {
    covIndex = new Quadtree();
    for (int i = 0; i < cov.length; i++) {
      //-- null areas are never merged to
      if (cov[i] != null) {
        covIndex.insert(cov[i].getEnvelope(), cov[i]);
      }
    }
  }
  
  public Geometry[] toCoverage(GeometryFactory geomFactory) {
    Geometry[] cleanCov = new Geometry[cov.length];
    for (int i = 0; i < cov.length; i++) {
      Geometry merged = null;
      if (cov[i] == null) {
        merged = geomFactory.createEmpty(2);
      } 
      else {
        merged = cov[i].union();
      }
      cleanCov[i] = merged;
    }
    return cleanCov;
  }
  
  private static class CleanArea {
    //TODO: is it any faster to store single polygons explicitly and only create array if needed?
    List<Polygon> polys = new ArrayList<Polygon>(); 
    
    public void add(Polygon poly) {
      polys.add(poly);
    }
    
    public Envelope getEnvelope() {
      Envelope env = new Envelope();
      for (Polygon poly : polys) {
        env.expandToInclude(poly.getEnvelopeInternal());
      }
      return env;
    }

    public double getBorderLength(Polygon adjPoly) {
      //TODO: find optimal way of computing border len given a coverage
      double len = 0;
      for (Polygon poly : polys) {
        //TODO: find longest connected border len
        Geometry border = OverlayNGRobust.overlay(poly, adjPoly, OverlayNG.INTERSECTION);
        double borderLen = border.getLength();
        len += borderLen;
      }
      return len;
    }

    public double getArea() {
      //TODO: cache area?
      double area = 0;
      for (Polygon poly : polys) {
        area += poly.getArea();
      }
      return area;
    }

    public boolean isAdjacent(RelateNG rel) {
      for (Polygon geom : polys) {
        //TODO: is there a faster way to check adjacency in coverage?
        boolean isAdjacent = rel.evaluate(geom, IntersectionMatrixPattern.ADJACENT);
        if (isAdjacent)
          return true;
      }
      return false;
    }

    public Geometry union() {
      Geometry[] geoms = GeometryFactory.toGeometryArray(polys);
      return CoverageUnion.union(geoms);
    }
  }

  public static interface MergeStrategy {

    public int getTarget();

    public void checkMergeTarget(int areaIndex, CleanArea cleanArea, Polygon poly);
    
    public class BorderMergeStrategy implements MergeStrategy {

      private int targetIndex = -1;
      private double targetBorderLen;

      @Override
      public int getTarget() {
        return targetIndex;
      }

      @Override
      public void checkMergeTarget(int areaIndex, CleanArea area, Polygon poly) {
        double borderLen = area == null ? 0 : area.getBorderLength(poly);
        if (targetIndex < 0 || borderLen > targetBorderLen) {
          targetIndex = areaIndex;
          targetBorderLen = borderLen;
        }
      }
    }
    
    public class AreaMergeStrategy implements MergeStrategy {

      private int targetIndex = -1;
      private double targetArea;
      private boolean isMax;

      AreaMergeStrategy(boolean isMax) {
        this.isMax = isMax;
      }
      
      @Override
      public int getTarget() {
        return targetIndex;
      }

      @Override
      public void checkMergeTarget(int areaIndex, CleanArea area, Polygon poly) {
        double areaVal = area == null ? 0.0 : area.getArea();
        boolean isBetter = isMax 
            ? areaVal > targetArea 
            : areaVal < targetArea;
        if (targetIndex < 0 || isBetter) {
          targetIndex = areaIndex;
          targetArea = areaVal;
        }
      }
    }
    
    public class IndexMergeStrategy implements MergeStrategy {

      private int targetIndex = -1;
      private boolean isMax;

      IndexMergeStrategy(boolean isMax) {
        this.isMax = isMax;
      }
      
      @Override
      public int getTarget() {
        return targetIndex;
      }

      @Override
      public void checkMergeTarget(int areaIndex, CleanArea area, Polygon poly) {
        boolean isBetter = isMax 
            ? areaIndex > targetIndex 
            : areaIndex < targetIndex;
        if (targetIndex < 0 || isBetter) {
          targetIndex = areaIndex;
        }
      }
    }
  }
}
