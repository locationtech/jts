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

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.ValidatingNoder;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionFunction;

public class OverlayNGTestFunctions {
  
  public static Geometry edgesNoded(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = OverlayNGFunctions.sameOrEmpty(a, b);
    b = OverlayNGFunctions.sameOrEmpty(b, a);
    // op should not matter, since edges are captured pre-result
    OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.UNION);
    ovr.setOutputNodedEdges(true);
    return ovr.getResultGeometry();
  }

  public static Geometry edgesNodedIntersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = OverlayNGFunctions.sameOrEmpty(a, b);
    b = OverlayNGFunctions.sameOrEmpty(b, a);
    // op should not matter, since edges are captured pre-result
    OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.INTERSECTION);
    ovr.setOutputNodedEdges(true);
    return ovr.getResultGeometry();
  }

  public static Geometry edgesNodedIntNoOpt(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = OverlayNGFunctions.sameOrEmpty(a, b);
    b = OverlayNGFunctions.sameOrEmpty(b, a);
    // op should not matter, since edges are captured pre-result
    OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.INTERSECTION);
    ovr.setOutputNodedEdges(true);
    ovr.setOptimized(false);
    return ovr.getResultGeometry();
  }

  private static Geometry extractPoly(Geometry g) {
    if (g instanceof Polygon) return g;
    if (g instanceof MultiPolygon) return g;
    return ConversionFunctions.toMultiPolygon(g, null);
  }
  
  public static Geometry edgesIntersectionResult(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = OverlayNGFunctions.sameOrEmpty(a, b);
    b = OverlayNGFunctions.sameOrEmpty(b, a);
   OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.INTERSECTION);
    ovr.setOutputResultEdges(true);
    return ovr.getResultGeometry();
  }

  public static Geometry edgesIntersectionAll(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = OverlayNGFunctions.sameOrEmpty(a, b);
    b = OverlayNGFunctions.sameOrEmpty(b, a);
    OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.INTERSECTION);
    ovr.setOutputEdges(true);
    return ovr.getResultGeometry();
  }
  
  public static Geometry edgesUnionResult(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = OverlayNGFunctions.sameOrEmpty(a, b);
    b = OverlayNGFunctions.sameOrEmpty(b, a);
    OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.UNION);
    ovr.setOutputResultEdges(true);
    return ovr.getResultGeometry();
  }
  
  public static Geometry unionIntSymDiff(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = OverlayNGFunctions.sameOrEmpty(a, b);
    b = OverlayNGFunctions.sameOrEmpty(b, a);
    // op should not matter, since edges are captured pre-result
    Geometry inter = extractPoly( OverlayNG.overlay(a, b, pm, OverlayOp.INTERSECTION) );
    Geometry symDiff = extractPoly( OverlayNG.overlay(a, b, pm, OverlayOp.SYMDIFFERENCE) );
    Geometry union = extractPoly( OverlayNG.overlay(inter, symDiff, pm, OverlayOp.UNION) );
    return union;
  }

  public static Geometry intersectionNoOpt(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    OverlayNG ovr = new OverlayNG(a, b, pm, OverlayOp.INTERSECTION);
    ovr.setOptimized(false);
    return ovr.getResultGeometry();
  }

  public static Geometry unionIntSymDiffOriginal(Geometry a, Geometry b) {
    // force non-null inputs
    a = OverlayNGFunctions.sameOrEmpty(a, b);
    b = OverlayNGFunctions.sameOrEmpty(b, a);
    // op should not matter, since edges are captured pre-result
    Geometry inter = extractPoly( a.intersection(b) );
    Geometry symDiff = extractPoly( a.symDifference(b) );
    Geometry union = extractPoly( inter.union(symDiff) );
    return union;
  }

  public static Geometry unionClassicNoding(Geometry a, Geometry b, double scaleFactor) {
    Noder noder = getSimpleNoder(false);
    return OverlayNG.overlay(a, b, null, noder, OverlayOp.UNION );
  }

  public static Geometry intersectionClassicNoding(Geometry a, Geometry b, double scaleFactor) {
    Noder noder = getSimpleNoder(false);
    return OverlayNG.overlay(a, b, null, noder, OverlayOp.INTERSECTION );
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
