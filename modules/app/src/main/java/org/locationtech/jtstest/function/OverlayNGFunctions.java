/*
 * Copyright (c) 2016 Vivid Solutions.
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

import static org.locationtech.jts.operation.overlayng.OverlayNGOp.DIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNGOp.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNGOp.SYMDIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNGOp.UNION;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.operation.overlayng.OverlayNGOp;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionFunction;

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

  public static Geometry intersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNGOp.overlay(a, b, pm, INTERSECTION);
  }
  
  public static Geometry union(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNGOp.overlay(a, b, pm, UNION);
  }
  
  public static Geometry difference(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNGOp.overlay(a, b, pm, DIFFERENCE);
  }

  public static Geometry differenceBA(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNGOp.overlay(b, a, pm, DIFFERENCE);
  }

  public static Geometry symDifference(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNGOp.overlay(a, b, pm, SYMDIFFERENCE);
  }
  
  public static Geometry unaryUnion(Geometry a, double scaleFactor) {
    final PrecisionModel pm = new PrecisionModel(scaleFactor);
    UnionFunction unionSRFun = new UnionFunction() {

      public Geometry union(Geometry g0, Geometry g1) {
        return OverlayNGOp.overlay(g0, g1, pm, UNION);
      }
      
    };
    
    //UnionFunction overlapSRFun = OverlapUnion.wrap(unionSRFun);
    //op.setUnionFunction( overlapSRFun );
    
    UnaryUnionOp op = new UnaryUnionOp(a);
    op.setUnionFunction( unionSRFun );
    return op.union();
  }

  public static Geometry reducePrecision(Geometry a, double scaleFactor) {
    return OverlayNGOp.reducePrecision(a, new PrecisionModel(scaleFactor));
  }
  
  public static Geometry reducePrecisionGC(Geometry a, double scaleFactor) {
    Point emptyPoint = a.getFactory().createPoint();
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    
    /**
     * This ONLY works if the input GeometryCollection 
     * is a non-overlapping polygonal coverage!
     */
    Geometry homoGeom = forceHomo(a);
    Geometry union = OverlayNGOp.overlay(homoGeom, emptyPoint, pm, UNION);
    
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

  private static Geometry forceHomo(Geometry geom) {
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
