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
import org.locationtech.jts.operation.relateng.IntersectionMatrixPattern;
import org.locationtech.jts.operation.relateng.RelateNG;

class CleanCoverage {
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
  
  public void merge(List<Polygon> mergables, boolean isOverlap) {
    for (Polygon poly : mergables) {
      merge(poly, isOverlap);
    }
  }
  
  private void merge(Polygon poly, boolean isOverlap) {
    List<CleanArea> adjacent = findAdjacent(poly);
    /**
     * No adjacent means this is likely an artifact
     * of an invalid input polygon. 
     * Discard polygon.
     */
    if (adjacent.size() == 0)
      return;
    
    //TODO merge pick strategies go here
    CleanArea mergeTarget = findBestMergeTargeet(adjacent);
    mergeTarget.add(poly);
  }

  private CleanArea findBestMergeTargeet(List<CleanArea> adjacent) {
    //TODO: other strategies here
    //TODO: max adj len - prob produces best result
    //TODO: max/min id ?
    return findMaxArea(adjacent);
  }

  private static CleanArea findMaxArea(List<CleanArea> items) {
    double maxArea = 0;
    CleanArea found = items.get(0);
    for (CleanArea res : items) {
      double area = res.getArea();
      if (res != found && area > maxArea) {
        maxArea = area;
        found = res;
      }
    }
    return found;
  }

  private List<CleanArea> findAdjacent(Geometry poly) {
    //TODO: use spatial index on cov
    List<CleanArea> adjacent = new ArrayList<CleanArea>();
    RelateNG rel = RelateNG.prepare(poly);
    for (CleanArea res : cov) {
      if (res == null)
        continue;
      if (res.isAdjacent(rel))
        adjacent.add(res);
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
