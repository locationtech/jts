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

import static org.locationtech.jts.operation.overlayng.OverlayNG.DIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.SYMDIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.PrecisionReducer;
import org.locationtech.jts.operation.overlayng.UnaryUnionNG;
import org.locationtech.jtstest.geomfunction.Metadata;

public class OverlayNGSRFunctions {

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
  
  @Metadata(description="Reduce precision of a geometry")
  public static Geometry reducePrecision(Geometry a, 
      @Metadata(title="Grid Scale") double scaleFactor) {
    
    /**
     * This ONLY works if the input GeometryCollection 
     * is a non-overlapping polygonal coverage!
     */
    Geometry homoGeom = extractHomo(a);
    Geometry reduced = PrecisionReducer.reducePrecision(homoGeom, new PrecisionModel(scaleFactor));
    return reduced;
    /*
    // Not sure why this is needed? 
    // Should be part of precision reducer, or Overlay (strict mode)
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
    */
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
