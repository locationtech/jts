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

import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.ValidatingNoder;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionFunction;

public class OverlayNGFunctions {
  
  public static Geometry edgesNoded(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // op should not matter, since edges are captured pre-result
    OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.UNION);
    ovr.setOutputNodedEdges(true);
    return ovr.getResultGeometry();
  }

  public static Geometry intersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, pm, OverlayOp.INTERSECTION);
  }

  public static Geometry edgesIntersectionResult(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.INTERSECTION);
    ovr.setOutputResultEdges(true);
    return ovr.getResultGeometry();
  }

  public static Geometry edgesIntersectionAll(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.INTERSECTION);
    ovr.setOutputEdges(true);
    return ovr.getResultGeometry();
  }
  
  public static Geometry union(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, pm, OverlayOp.UNION);
  }
  
  public static Geometry edgesUnionResult(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.UNION);
    ovr.setOutputResultEdges(true);
    return ovr.getResultGeometry();
  }

  public static Geometry difference(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, pm, OverlayOp.DIFFERENCE);
  }

  public static Geometry differenceBA(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(b, a, pm, OverlayOp.DIFFERENCE);
  }

  public static Geometry symDifference(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlayNG.overlay(a, b, pm, OverlayOp.SYMDIFFERENCE);
  }
  
  public static Geometry unaryUnion(Geometry a, double scaleFactor) {
    final PrecisionModel pm = new PrecisionModel(scaleFactor);
    UnionFunction unionSRFun = new UnionFunction() {

      public Geometry union(Geometry g0, Geometry g1) {
        return OverlayNG.overlay(g0, g1, pm, OverlayOp.UNION);
      }
      
    };
    UnaryUnionOp op = new UnaryUnionOp(a);
    op.setUnionFunction(unionSRFun);
    return op.union();
  }

  public static Geometry reducePrecision(Geometry a, double scaleFactor) {
    Point emptyPoint = a.getFactory().createPoint();
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    
    /**
     * This ONLY works if the input GeometryCollection 
     * is a non-overlapping polygonal coverage!
     */
    Geometry homoGeom = forceHomo(a);
    Geometry union = OverlayNG.overlay(homoGeom, emptyPoint, pm, OverlayOp.UNION);
    
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
  
  public static Geometry unaryUnionClassicNoding(Geometry a) {
    UnionFunction unionSRFun = new UnionFunction() {

      public Geometry union(Geometry g0, Geometry g1) {
        Noder noder = getSimpleNoder(false);
        return OverlayNG.overlay(g0, g1, null, noder, OverlayOp.UNION );
      }
      
    };
    UnaryUnionOp op = new UnaryUnionOp(a);
    op.setUnionFunction(unionSRFun);
    return op.union();
  }
  
  private static Noder getSimpleNoder(boolean doValidation) {
    MCIndexNoder mcNoder = new MCIndexNoder();
    LineIntersector li = new RobustLineIntersector();
    mcNoder.setSegmentIntersector(new IntersectionAdder(li));
    
    Noder noder = mcNoder;
    if (doValidation) {
      noder = new ValidatingNoder( mcNoder);
    }
    return noder;
  }
}
