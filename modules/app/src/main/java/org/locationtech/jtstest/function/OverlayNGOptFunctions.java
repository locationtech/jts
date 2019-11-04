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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.overlayng.OverlayNG;

/**
 * Functions to test using spatial predicates 
 * as a filter in front of overlay operations
 * to optimize performance.
 * 
 * @author Martin Davis
 *
 */
public class OverlayNGOptFunctions {
  
  private static Geometry fastIntersect(Geometry a, Geometry b) {
    IntersectionMatrix im = a.relate(b);
    if (! im.isIntersects()) return a.getFactory().createEmpty(a.getDimension());
    if (im.isCovers()) return b.copy();
    if (im.isCoveredBy()) return a.copy();
    // null indicates full overlay required
    return null;
  }
  
  /**
   * Use spatial predicates as a filter
   * in front of intersection.
   * 
   * @param a a geometry
   * @param b a geometry
   * @return the intersection of the geometries
   */
  public static Geometry intersectionOrigClassic(Geometry a, Geometry b) {
    Geometry intFast = fastIntersect(a, b);
    if (intFast != null) return intFast;
    return a.intersection(b);
  }
  
  /**
   * Use prepared geometry spatial predicates as a filter
   * in front of intersection,
   * with the first operand prepared.
   * 
   * @param a a geometry to prepare
   * @param b a geometry
   * @return the intersection of the geometries
   */
  public static Geometry intersectionOrigPrep(Geometry a, Geometry b) {
    PreparedGeometry pg = cacheFetch(a);
    if (! pg.intersects(b)) return null;
    if (pg.covers(b)) return b.copy();
    return a.intersection(b);
  }
  
  public static Geometry intersectionOrigPrepNoCache(Geometry a, Geometry b) {
    PreparedGeometry pg = (new PreparedGeometryFactory()).create(a);
    if (! pg.intersects(b)) return null;
    if (pg.covers(b)) return b.copy();
    return a.intersection(b);
  }
  
  public static Geometry intersection(Geometry a, Geometry b) {
    Geometry intFast = fastIntersect(a, b);
    if (intFast != null) return intFast;
    return OverlayNG.overlayFixedPrecision(a, b, OverlayNG.INTERSECTION);
  }
  
  public static Geometry intersectionPrep(Geometry a, Geometry b) {
    PreparedGeometry pg = cacheFetch(a);
    if (! pg.intersects(b)) return null;
    if (pg.covers(b)) return b.copy();
    return OverlayNG.overlayFixedPrecision(a, b, OverlayNG.INTERSECTION);
  }
  
  public static Geometry intersectionPrepNoCache(Geometry a, Geometry b) {
    PreparedGeometry pg = (new PreparedGeometryFactory()).create(a);
    if (! pg.intersects(b)) return null;
    if (pg.covers(b)) return b.copy();
    return OverlayNG.overlayFixedPrecision(a, b, OverlayNG.INTERSECTION);
  }
  
  private static Geometry cacheKey = null;
  private static PreparedGeometry cache = null;
  

  private static PreparedGeometry cacheFetch(Geometry g) {
    if (g != cacheKey) {
      cacheKey = g;
      cache = (new PreparedGeometryFactory()).create(g);
    }
    return cache;
  }
  
}
