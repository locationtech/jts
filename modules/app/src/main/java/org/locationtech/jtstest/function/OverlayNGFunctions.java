/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import static org.locationtech.jts.operation.overlayng.OverlayNG.DIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.SYMDIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.operation.overlayng.CoverageUnion;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.PrecisionReducer;
import org.locationtech.jts.operation.overlayng.PrecisionUtil;
import org.locationtech.jts.operation.overlayng.UnaryUnionNG;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionFunction;
import org.locationtech.jtstest.geomfunction.Metadata;

public class OverlayNGFunctions {
  
 
  static Geometry sameOrEmpty(Geometry a, Geometry b) {
    if (a != null) return a;
    // return empty geom of same type
    if (b.getDimension() == 2) {
      return b.getFactory().createPolygon();
    }
    if (b.getDimension() == 1) {
      return b.getFactory().createLineString();
    }
    return b.getFactory().createPoint();
  }

  public static Geometry intersection(Geometry a, Geometry b, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    return OverlayNG.overlay(a, b, INTERSECTION, new PrecisionModel(scaleFactor));
  }
  
  public static Geometry union(Geometry a, Geometry b, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    return OverlayNG.overlay(a, b, UNION, new PrecisionModel(scaleFactor));
  }
  
  public static Geometry difference(Geometry a, Geometry b, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    return OverlayNG.overlay(a, b, DIFFERENCE, new PrecisionModel(scaleFactor));
  }

  public static Geometry differenceBA(Geometry a, Geometry b, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    return OverlayNG.overlay(b, a, DIFFERENCE, new PrecisionModel(scaleFactor));
  }

  public static Geometry symDifference(Geometry a, Geometry b, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    return OverlayNG.overlay(a, b, SYMDIFFERENCE, new PrecisionModel(scaleFactor));
  }
  
  @Metadata(description="Unary union a collection of geometries")
  public static Geometry unaryUnion(Geometry a, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    return UnaryUnionNG.union(a, new PrecisionModel(scaleFactor));
  }

  
  
  @Metadata(description="Intersection with automatically-determined maximum precision")
  public static Geometry intersectionAutoPM(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, INTERSECTION);
  }
  
  @Metadata(description="Union with automatically-determined maximum precision")
  public static Geometry unionAutoPM(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, UNION);
  }
  
  @Metadata(description="Difference with automatically-determined maximum precision")
  public static Geometry differenceAutoPM(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, DIFFERENCE);
  }  
  
  @Metadata(description="Unary union with automatically-determined maximum precision")
  public static Geometry unaryUnionAutoPM(Geometry a) {
    return UnaryUnionNG.union(a);
  }
  
  
  
  @Metadata(description="Union a fully-noded coverage (polygons or lines)")
  public static Geometry unionCoverage(Geometry geom) {
    Geometry cov = OverlayNGFunctions.extractHomo(geom);
    return CoverageUnion.union(cov);
  }

  public static Geometry reducePrecision(Geometry a, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    return PrecisionReducer.reducePrecision(a, new PrecisionModel(scaleFactor));
  }
  
  @Metadata(description="Reduce precision of the max dimension components in a GeometryCollection")
  public static Geometry reducePrecisionGC(Geometry a, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    
    /**
     * This ONLY works if the input GeometryCollection 
     * is a non-overlapping polygonal coverage!
     */
    Geometry homoGeom = extractHomo(a);
    Geometry union = PrecisionReducer.reducePrecision(a, new PrecisionModel(scaleFactor));
    
    List components = null;
    switch (a.getDimension()) {
      case 2: 
        components = PolygonExtracter.getPolygons(union);
        break;
      case 1:
        components = LineStringExtracter.getLines(union);
        break;
    }
    Geometry result = a.getFactory().buildGeometry(components);
    return result;
  }
  
  /**
   * Extracts homogeneous components with largest dimension.
   * 
   * @param geom
   * @return a homogeneous collection
   */
  static Geometry extractHomo(Geometry geom) {
    int resultDimension = geom.getDimension();
    List components = null;
    switch (resultDimension) {
    case 2: 
      components = PolygonExtracter.getPolygons(geom);
      break;
    case 1:
      components = LineStringExtracter.getLines(geom);
      break;
    }
    Geometry result = geom.getFactory().buildGeometry(components);
    return result;
  }
  
}
