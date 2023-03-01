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
package org.locationtech.jtslab;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.overlayarea.GeometryArea;
import org.locationtech.jts.operation.overlayarea.OverlayArea;

public class OverlayAreaFunctions {

  public static double areaSingle(Geometry g) {
    return GeometryArea.area(g);
  }
  
  public static double intersectionArea(Geometry geom0, Geometry geom1) {
    return OverlayArea.intersectionArea(geom0, geom1);
  }
  
  private static Geometry overlayAreaKey;
  private static OverlayArea overlayAreaCache;

  public static double intersectionAreaPrep(Geometry geom0, Geometry geom1) {
    if (geom0 != overlayAreaKey) {
      overlayAreaKey = geom0;
      overlayAreaCache = new OverlayArea(geom0);
    }
    return overlayAreaCache.intersectionArea(geom1);
  }
  
  public static Geometry intersectionAreaPrepData(Geometry geom0, Geometry geom1) {
    double area = intersectionAreaPrep(geom0, geom1);
    if (area == 0.0) return null;
    Geometry result = geom1.copy();
    result.setUserData(area);
    return result;
  }
  
  public static Geometry intersectionAreaData(Geometry geom0, Geometry geom1) {
    double area = intersectionArea(geom0, geom1);
    if (area == 0.0) return null;
    Geometry result = geom1.copy();
    result.setUserData(area);
    return result;
  }
  
  public static double intAreaOrig(Geometry geom0, Geometry geom1) {
    double intArea = geom0.intersection(geom1).getArea();
    return intArea;
  }
  
  static PreparedGeometry geomPrepCache = null;
  static Geometry geomPrepKey = null;
  
  public static double intAreaOrigPrep(Geometry geom0, Geometry geom1) {
    if (geom0 != geomPrepKey) {
      geomPrepKey = geom0;
      geomPrepCache = PreparedGeometryFactory.prepare(geom0);
    }
    return intAreaFullPrep(geom0, geomPrepCache, geom1);
  }

  public static Geometry intAreaOrigData(Geometry geom0, Geometry geom1) {
    double area = intAreaOrig(geom0, geom1);
    if (area == 0.0) return null;
    
    Geometry result = geom1.copy();
    result.setUserData(area);
    return result;
  }
  
  public static Geometry intAreaOrigPrepData(Geometry geom0, Geometry geom1) {
    double area = intAreaOrigPrep(geom0, geom1);
    if (area == 0.0) return null;
    
    Geometry result = geom1.copy();
    result.setUserData(area);
    return result;
  }
  
  private static double intAreaFullPrep(Geometry geom, PreparedGeometry geomPrep, Geometry geom1) {
    if (! geomPrep.intersects(geom1)) return 0.0;
    if (geomPrep.contains(geom1)) return geom1.getArea();
    double intArea = geom.intersection(geom1).getArea();
    return intArea;
  }
  
  public static double checkIntArea(Geometry geom0, Geometry geom1) {
    double intArea = intersectionArea(geom0, geom1);
    
    double intAreaStd = geom0.intersection(geom1).getArea();
    
    double diff = Math.abs(intArea - intAreaStd)/Math.max(intArea, intAreaStd);
    
    return diff;
  }
}
