/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom;

import org.locationtech.jts.operation.relate.RelateOp;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.locationtech.jts.operation.relateng.RelatePredicate;

/**
 * Internal class which encapsulates the runtime switch to use RelateNG.
 * <p>
 * This class allows the {@link Geometry} predicate methods to be 
 * switched between the original {@link RelateOp} algorithm 
 * and the modern {@link RelateNG} codebase
 * via a system property <code>jts.relate</code>.
 * <ul>
 * <li><code>jts.relate=old</code> - (default) use original RelateOp algorithm
 * <li><code>jts.relate=ng</code> - use RelateNG
 * </ul>
 * 
 * @author mdavis
 *
 */
class GeometryRelate 
{
  public static String RELATE_PROPERTY_NAME = "jts.relate";
  
  public static String RELATE_PROPERTY_VALUE_NG = "ng";
  public static String RELATE_PROPERTY_VALUE_OLD = "old";
  
  /**
   * Currently the old relate implementation is the default
   */
  public static boolean RELATE_NG_DEFAULT = false;

  private static boolean isRelateNG = RELATE_NG_DEFAULT;

  static {
    setRelateImpl(System.getProperty(RELATE_PROPERTY_NAME));
  }
  
  /**
   * This function is provided primarily for unit testing.
   * It is not recommended to use it dynamically, since 
   * that may result in inconsistent overlay behaviour.
   * 
   * @param relateImplCode the code for the overlay method (may be null)
   */
  static void setRelateImpl(String relateImplCode) {
    if (relateImplCode == null) 
      return;
    // set flag explicitly since current value may not be default
    isRelateNG = RELATE_NG_DEFAULT;
    
    if (RELATE_PROPERTY_VALUE_NG.equalsIgnoreCase(relateImplCode) )
      isRelateNG = true;
  }
  
  static boolean intersects(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, RelatePredicate.intersects());
    }
    if (a.isGeometryCollection() || b.isGeometryCollection()) {
      for (int i = 0 ; i < a.getNumGeometries() ; i++) {
        for (int j = 0 ; j < b.getNumGeometries() ; j++) {
          if (a.getGeometryN(i).intersects(b.getGeometryN(j))) {
            return true;
          }
        }
      }
      return false;
    }
    return RelateOp.relate(a, b).isIntersects();
  }

  static boolean contains(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, RelatePredicate.contains());
    }
    // optimization - lower dimension cannot contain areas
    if (b.getDimension() == 2 && a.getDimension() < 2) {
      return false;
    }
    // optimization - P cannot contain a non-zero-length L
    // Note that a point can contain a zero-length lineal geometry,
    // since the line has no boundary due to Mod-2 Boundary Rule
    if (b.getDimension() == 1 && a.getDimension() < 1 && b.getLength() > 0.0) {
      return false;
    }
    // optimization - envelope test
    if (! a.getEnvelopeInternal().contains(b.getEnvelopeInternal()))
      return false;
    return RelateOp.relate(a, b).isContains();
  }

  static boolean covers(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, RelatePredicate.covers());
    }
    // optimization - lower dimension cannot cover areas
    if (b.getDimension() == 2 && a.getDimension() < 2) {
      return false;
    }
    // optimization - P cannot cover a non-zero-length L
    // Note that a point can cover a zero-length lineal geometry
    if (b.getDimension() == 1 && a.getDimension() < 1 && b.getLength() > 0.0) {
      return false;
    }
    // optimization - envelope test
    if (! a.getEnvelopeInternal().covers(b.getEnvelopeInternal()))
      return false;
    // optimization for rectangle arguments
    if (a.isRectangle()) {
      // since we have already tested that the test envelope is covered
      return true;
    }
    return RelateOp.relate(a, b).isCovers();  
  }
  
  static boolean coveredBy(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, RelatePredicate.coveredBy());
    }
    return covers(b, a);  
  }
  
  static boolean crosses(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, RelatePredicate.crosses());
    }
    // short-circuit test
    if (! a.getEnvelopeInternal().intersects(b.getEnvelopeInternal()))
      return false;
    return RelateOp.relate(a, b).isCrosses(a.getDimension(), b.getDimension()); 
  }
  
  static boolean disjoint(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, RelatePredicate.disjoint());
    }
    return ! intersects(a, b);
  }
  
  static boolean equalsTopo(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, RelatePredicate.equalsTopo());
    }
    if (! a.getEnvelopeInternal().equals(b.getEnvelopeInternal()))
      return false;
    return RelateOp.relate(a, b).isEquals(a.getDimension(), b.getDimension());  
  }
  
  static boolean overlaps(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, RelatePredicate.overlaps());
    }
    if (! a.getEnvelopeInternal().intersects(b.getEnvelopeInternal()))
      return false;
    return RelateOp.relate(a, b).isOverlaps(a.getDimension(), b.getDimension());
  }
  
  static boolean touches(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, RelatePredicate.touches());
    }
    if (! a.getEnvelopeInternal().intersects(b.getEnvelopeInternal()))
      return false;
    return RelateOp.relate(a, b).isTouches(a.getDimension(), b.getDimension());
  }

  static boolean within(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, RelatePredicate.within());
    }
    return contains(b, a);
  }

  static IntersectionMatrix relate(Geometry a, Geometry b)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b);
    }
    Geometry.checkNotGeometryCollection(a);
    Geometry.checkNotGeometryCollection(b);
    return RelateOp.relate(a, b);
  }
  
  static boolean relate(Geometry a, Geometry b, String intersectionPattern)
  {
    if (isRelateNG) {
      return RelateNG.relate(a, b, intersectionPattern);
    }
    Geometry.checkNotGeometryCollection(a);
    Geometry.checkNotGeometryCollection(b);
    return RelateOp.relate(a, b).matches(intersectionPattern);
  }
  
}
