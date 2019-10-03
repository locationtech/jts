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
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.SegmentExtractingNoder;
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
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, INTERSECTION, pm);
  }
  
  @Metadata(description="Intersection using automatic precision")
  public static Geometry intersectionAuto(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, INTERSECTION);
  }
  
  public static Geometry union(Geometry a, Geometry b, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, UNION, pm);
  }
  
  @Metadata(description="Union using automatic precision")
  public static Geometry unionAuto(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, UNION);
  }
  
  public static Geometry difference(Geometry a, Geometry b, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, DIFFERENCE, pm);
  }

  public static Geometry differenceBA(Geometry a, Geometry b, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(b, a, DIFFERENCE, pm);
  }

  @Metadata(description="Difference using automatic precision")
  public static Geometry differenceAuto(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, DIFFERENCE);
  }

  public static Geometry symDifference(Geometry a, Geometry b, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, SYMDIFFERENCE, pm);
  }
  
  public static Geometry unaryUnion(Geometry a, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    final PrecisionModel pm = new PrecisionModel(scaleFactor);
    UnionFunction unionSRFun = new UnionFunction() {

      public Geometry union(Geometry g0, Geometry g1) {
        return OverlayNG.overlay(g0, g1, UNION, pm);
      }
      
    };
    
    //UnionFunction overlapSRFun = OverlapUnion.wrap(unionSRFun);
    //op.setUnionFunction( overlapSRFun );
    
    UnaryUnionOp op = new UnaryUnionOp(a);
    op.setUnionFunction( unionSRFun );
    return op.union();
  }

  @Metadata(description="Unary union using automatic precision")
  public static Geometry unaryUnionAuto(Geometry a) {
    PrecisionModel pm = OverlayNG.precisionModel(a, null);
    UnionFunction unionSRFun = new UnionFunction() {

      public Geometry union(Geometry g0, Geometry g1) {
        return OverlayNG.overlay(g0, g1, UNION, pm);
      }
      
    };
    
    //UnionFunction overlapSRFun = OverlapUnion.wrap(unionSRFun);
    //op.setUnionFunction( overlapSRFun );
    
    UnaryUnionOp op = new UnaryUnionOp(a);
    op.setUnionFunction( unionSRFun );
    return op.union();
  }

  @Metadata(description="Union a noded coverage (polygons or lines)")
  public static Geometry unionCoverage(Geometry geom) {
    Geometry cov = OverlayNGFunctions.extractHomo(geom);
    Noder noder = new SegmentExtractingNoder();
    Point emptyPoint = cov.getFactory().createPoint();
    return OverlayNG.overlay(cov, emptyPoint, UNION, null, noder );
  }
  
  public static Geometry reducePrecision(Geometry a, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    return OverlayNG.reducePrecision(a, new PrecisionModel(scaleFactor));
  }
  
  @Metadata(description="Reduce precision of max dimension in a GC")
  public static Geometry reducePrecisionGC(Geometry a, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    Point emptyPoint = a.getFactory().createPoint();
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    
    /**
     * This ONLY works if the input GeometryCollection 
     * is a non-overlapping polygonal coverage!
     */
    Geometry homoGeom = extractHomo(a);
    Geometry union = OverlayNG.overlay(homoGeom, emptyPoint, UNION, pm);
    
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
