/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import java.util.List;

import static org.locationtech.jts.operation.overlayng.OverlayNG.SYMDIFFERENCE;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.ValidatingNoder;
import org.locationtech.jts.operation.overlayng.RingClipper;
import org.locationtech.jts.operation.overlayng.LineLimiter;
import org.locationtech.jts.operation.overlayng.OverlayNG;

public class OverlayNGTestFunctions {
  
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
  
  public static Geometry edgesNoded(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = sameOrEmpty(a, b);
    b = sameOrEmpty(b, a);
    // op should not matter, since edges are captured pre-result
    OverlayNG ovr = new OverlayNG(a, b, pm, UNION);
    ovr.setOutputNodedEdges(true);
    return ovr.getResult();
  }

  public static Geometry edgesNodedIntersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = sameOrEmpty(a, b);
    b = sameOrEmpty(b, a);
    // op should not matter, since edges are captured pre-result
    OverlayNG ovr = new OverlayNG(a, b, pm, INTERSECTION);
    ovr.setOutputNodedEdges(true);
    return ovr.getResult();
  }

  public static Geometry edgesNodedIntNoOpt(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = sameOrEmpty(a, b);
    b = sameOrEmpty(b, a);
    // op should not matter, since edges are captured pre-result
    OverlayNG ovr = new OverlayNG(a, b, pm, INTERSECTION);
    ovr.setOutputNodedEdges(true);
    ovr.setOptimized(false);
    return ovr.getResult();
  }

  private static Geometry extractPoly(Geometry g) {
    if (g instanceof Polygon) return g;
    if (g instanceof MultiPolygon) return g;
    return ConversionFunctions.toMultiPolygon(g, null);
  }
  
  public static Geometry edgesIntersectionResult(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = sameOrEmpty(a, b);
    b = sameOrEmpty(b, a);
   OverlayNG ovr = new OverlayNG(a, b, pm, INTERSECTION);
    ovr.setOutputResultEdges(true);
    return ovr.getResult();
  }

  public static Geometry edgesIntersectionAll(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = sameOrEmpty(a, b);
    b = sameOrEmpty(b, a);
    OverlayNG ovr = new OverlayNG(a, b, pm, INTERSECTION);
    ovr.setOutputEdges(true);
    return ovr.getResult();
  }
  
  public static Geometry edgesUnionResult(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = sameOrEmpty(a, b);
    b = sameOrEmpty(b, a);
    OverlayNG ovr = new OverlayNG(a, b, pm, UNION);
    ovr.setOutputResultEdges(true);
    return ovr.getResult();
  }
  
  public static Geometry edgesUnionAll(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = sameOrEmpty(a, b);
    b = sameOrEmpty(b, a);
    OverlayNG ovr = new OverlayNG(a, b, pm, UNION);
    ovr.setOutputEdges(true);
    return ovr.getResult();
  }
  
  public static Geometry intersectionSRNoOpt(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    OverlayNG ovr = new OverlayNG(a, b, pm, INTERSECTION);
    ovr.setOptimized(false);
    return ovr.getResult();
  }

  public static Geometry intersectionNoOpt(Geometry a, Geometry b) {
    OverlayNG ovr = new OverlayNG(a, b, INTERSECTION);
    ovr.setOptimized(false);
    return ovr.getResult();
  }
  
  public static Geometry intersectionNoValid(Geometry a, Geometry b) {
    Noder noder = createFloatingPrecisionNoder(false);
    return OverlayNG.overlay(a, b, INTERSECTION, new PrecisionModel(), noder);
  }
  
  public static Geometry intersectionIsValid(Geometry a, Geometry b) {
    Noder noder = createFloatingPrecisionNoder(false);
    Geometry geom = OverlayNG.overlay(a, b, INTERSECTION, new PrecisionModel(), noder);
    if (geom.isValid()) return geom;
    return null;
  }
  
  private static Noder createFloatingPrecisionNoder(boolean doValidation) {
    MCIndexNoder mcNoder = new MCIndexNoder();
    LineIntersector li = new RobustLineIntersector();
    mcNoder.setSegmentIntersector(new IntersectionAdder(li));
    
    Noder noder = mcNoder;
    if (doValidation) {
      noder = new ValidatingNoder( mcNoder);
    }
    return noder;
  }
  
  public static Geometry unionIntSymDiff(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    // force non-null inputs
    a = sameOrEmpty(a, b);
    b = sameOrEmpty(b, a);
    // op should not matter, since edges are captured pre-result
    Geometry inter = extractPoly( OverlayNG.overlay(a, b, INTERSECTION, pm) );
    Geometry symDiff = extractPoly( OverlayNG.overlay(a, b, SYMDIFFERENCE, pm) );
    Geometry union = extractPoly( OverlayNG.overlay(inter, symDiff, UNION, pm) );
    return union;
  }

  public static Geometry unionIntSymDiffOriginal(Geometry a, Geometry b) {
    // force non-null inputs
    a = sameOrEmpty(a, b);
    b = sameOrEmpty(b, a);
    // op should not matter, since edges are captured pre-result
    Geometry inter = extractPoly( a.intersection(b) );
    Geometry symDiff = extractPoly( a.symDifference(b) );
    Geometry union = extractPoly( inter.union(symDiff) );
    return union;
  }

  public static Geometry unionClassicNoderNoValid(Geometry a, Geometry b) {
    Noder noder = createClassicNoder(false);
    return OverlayNG.overlay(a, b, UNION, null, noder );
  }

  
  static Noder createClassicNoder(boolean doValidation) {
    MCIndexNoder mcNoder = new MCIndexNoder();
    LineIntersector li = new RobustLineIntersector();
    mcNoder.setSegmentIntersector(new IntersectionAdder(li));
    
    Noder noder = mcNoder;
    if (doValidation) {
      noder = new ValidatingNoder( mcNoder);
    }
    return noder;
  }
  
  public static Geometry clipRing(Geometry line, Geometry box) {
    RingClipper clipper = new RingClipper(box.getEnvelopeInternal());
    Coordinate[] pts = clipper.clip(line.getCoordinates());
    return line.getFactory().createLineString(pts);
  }
  
  public static Geometry limitLine(Geometry line, Geometry box) {
    LineLimiter limiter = new LineLimiter(box.getEnvelopeInternal());
    List<Coordinate[]> sections = limiter.limit(line.getCoordinates());
   
    return toLines(sections, line.getFactory());
  }

  private static Geometry toLines(List<Coordinate[]> sections, GeometryFactory factory) {
    LineString[] lines = new LineString[sections.size()];
    int i = 0;
    for (Coordinate[] pts : sections) {
      lines[i++] = factory.createLineString(pts);
    }
    if (lines.length == 1) return lines[0];
    return factory.createMultiLineString(lines);
  }
  
  public static Geometry edgesOverlayResult(Geometry a) {
    OverlayNG ovr = new OverlayNG(a, null, UNION);
    ovr.setOutputResultEdges(true);
    return ovr.getResult();
  }
}
