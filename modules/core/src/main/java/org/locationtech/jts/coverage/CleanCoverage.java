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
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.locationtech.jts.operation.relateng.IntersectionMatrixPattern;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.locationtech.jts.util.IntArrayList;

class CleanCoverage {
  public static interface MergeStrategy {

    public CleanArea getTarget();

    public void checkMergeTarget(int areaIndex, CleanArea cleanArea, Polygon poly);
    
    public class BorderMergeStrategy implements MergeStrategy {

      private CleanArea target;
      private double targetBorderLen;

      @Override
      public CleanArea getTarget() {
        return target;
      }

      @Override
      public void checkMergeTarget(int areaIndex, CleanArea area, Polygon poly) {
        if (area == null)
          return;

        double borderLen = area.getBorderLength(poly);
        if (target == null || borderLen > targetBorderLen) {
          target = area;
          targetBorderLen = borderLen;
        }
      }
    }
    
    public class AreaMergeStrategy implements MergeStrategy {

      private CleanArea target;
      private double targetArea;
      private boolean isMax;

      AreaMergeStrategy(boolean isMax) {
        this.isMax = isMax;
      }
      
      @Override
      public CleanArea getTarget() {
        return target;
      }

      @Override
      public void checkMergeTarget(int areaIndex, CleanArea area, Polygon poly) {
        if (area == null)
          return;
        double areaVal = area.getArea();
        boolean isBetter = isMax 
            ? areaVal > targetArea 
            : areaVal < targetArea;
        if (target == null || isBetter) {
          target = area;
          targetArea = areaVal;
        }
      }
    }
    
    public class IndexMergeStrategy implements MergeStrategy {

      private CleanArea target;
      private int targetIndex;
      private boolean isMax;

      IndexMergeStrategy(boolean isMax) {
        this.isMax = isMax;
      }
      
      @Override
      public CleanArea getTarget() {
        return target;
      }

      @Override
      public void checkMergeTarget(int areaIndex, CleanArea area, Polygon poly) {
        if (area == null)
          return;
        boolean isBetter = isMax 
            ? areaIndex > targetIndex 
            : areaIndex < targetIndex;
        if (target == null || isBetter) {
          target = area;
          targetIndex = areaIndex;
        }
      }
    }
  }
  
  private CleanArea[] cov;

  public CleanCoverage(int size) {
    cov = new CleanArea[size]; 
  }

  public void add(int i, Polygon poly) {
    if (cov[i] == null) {
      cov[i] = new CleanArea();
    }
    cov[i].add(poly);
  }
  
  public void mergeGaps(List<Polygon> mergables) {
    for (Polygon poly : mergables) {
      mergeGap(poly);
    }
  }
  
  public void mergeOverlap(Polygon overlap, MergeStrategy mergeStrategy, IntArrayList parentIndexes) {
    CleanArea mergeTarget = findMergeTarget(overlap, mergeStrategy, parentIndexes, cov);
    mergeTarget.add(overlap);
  }

  public static CleanArea findMergeTarget(Polygon poly, MergeStrategy strat, IntArrayList parentIndexes, CleanArea[] cov) {
    for (int i = 0; i < parentIndexes.size(); i++) {
      int index = parentIndexes.get(i);
      strat.checkMergeTarget(index, cov[index], poly);
    }
    return strat.getTarget();
  }

  private void mergeGap(Polygon poly) {
    List<CleanArea> adjacent = findAdjacent(poly);
    /**
     * No adjacent means this is likely an artifact
     * of an invalid input polygon. 
     * Discard polygon.
     */
    if (adjacent.size() == 0)
      return;
    
    CleanArea mergeTarget = findMaxBorderLength(poly, adjacent);
    mergeTarget.add(poly);
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

  private List<CleanArea> findAdjacent(Geometry poly) {
    //TODO: use spatial index on cov
    List<CleanArea> adjacent = new ArrayList<CleanArea>();
    RelateNG rel = RelateNG.prepare(poly);
    for (CleanArea area : cov) {
      if (area != null && area.isAdjacent(rel))
        adjacent.add(area);
    }
    return adjacent;
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

}
