/*
 * Copyright (c) 2020 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayarea;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFilter;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/** 
 * Computes the area of a geometry using the {@link EdgeVector} summing
 * approach. 
 * This provides a validation of the correctness of the {@link OverlayArea} approach.
 * It is not intended to replace the standard polygon area computation.
 * 
 * @author Martin Davis
 *
 */
public class GeometryArea {
  
  public static double area(Geometry geom) {
    GeometryArea area = new GeometryArea(geom);
    return area.getArea();
  }
  
  private Geometry geom;

  public GeometryArea(Geometry geom) {
    this.geom = geom;
  }

  private double getArea() {   
    PolygonAreaFilter filter = new PolygonAreaFilter();
    geom.apply(filter);
    return filter.area;
  }
  
  private class PolygonAreaFilter implements GeometryFilter {
    double area = 0;
    @Override
    public void filter(Geometry geom) {
      if (geom instanceof Polygon) {
        area += areaPolygon((Polygon) geom);
      }
    }
  }
  
  private double areaPolygon(Polygon geom) {
    double area = areaRing(geom.getExteriorRing());
    for (int i = 0; i < geom.getNumInteriorRing(); i++) {
      LinearRing hole = geom.getInteriorRingN(i);
      area -= areaRing(hole);
    }
    return area;
  }
  
  public double areaRing(LinearRing ring) {
    // TODO: handle hole rings, multiPolygons
    CoordinateSequence seq = ring.getCoordinateSequence();
    boolean isCW = ! Orientation.isCCW(seq);
    
    // scan every segment
    double area = 0;
    for (int i = 1; i < seq.size(); i++) {
      int i0 = i - 1;
      int i1 = i;
      /**
       * Sum the partial areas for the two
       * opposing SegmentVectors representing the edge.
       * If the ring is oriented CW then the interior is to the right of the vector,
       * and the opposing vector is opposite.
       */
      area += EdgeVector.area2Term(
          seq.getX(i0), seq.getY(i0),
          seq.getX(i1), seq.getY(i1),
          isCW)
          + EdgeVector.area2Term(
              seq.getX(i1), seq.getY(i1),
              seq.getX(i0), seq.getY(i0),
              ! isCW);
    }
    return area / 2;
  }
}
