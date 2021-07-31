/*
 * Copyright (c) 2019 Martin Davis, and others.
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

/**
 * Functions to test using spatial predicates 
 * as a filter in front of overlay operations
 * to optimize performance.
 * 
 * @author Martin Davis
 *
 */
public class OverlayOptFunctions {
  
  /**
   * Use spatial predicates as a filter
   * in front of intersection.
   * 
   * @param a a geometry
   * @param b a geometry
   * @return the intersection of the geometries
   */
  public static Geometry intersectionOpt(Geometry a, Geometry b) {
    if (! a.intersects(b)) return null;
    if (a.covers(b)) return b.copy();
    if (b.covers(a)) return a.copy();
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
  public static Geometry intersectionOptPrep(Geometry a, Geometry b) {
    PreparedGeometry pg = cacheFetch(a);
    if (! pg.intersects(b)) return null;
    if (pg.covers(b)) return b.copy();
    return a.intersection(b);
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
