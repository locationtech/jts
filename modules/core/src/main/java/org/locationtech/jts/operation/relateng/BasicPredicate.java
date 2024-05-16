/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Location;

/**
 * The base class for relate topological predicates
 * with a boolean value.
 * Implements tri-state logic for the predicate value,
 * to detect when the final value has been determined.
 * 
 * @author Martin Davis
 *
 */
abstract class BasicPredicate implements TopologyPredicate {

  private static final int UNKNOWN = -1;
  private static final int FALSE = 0;
  private static final int TRUE = 1;
  
  private static boolean isKnown(int value) {
     return value > UNKNOWN;
  }
  
  private static boolean toBoolean(int value) {
    return value == TRUE;
  }
  
  private static int toValue(boolean val) {
    return val ? TRUE : FALSE;
  }
  
  /**
   * Tests if two geometries intersect 
   * based on an interaction at given locations.
   * 
   * @param locA the location on geometry A
   * @param locB the location on geometry B
   * @return true if the geometries intersect
   */
  public static boolean isIntersection(int locA, int locB) {
    //-- i.e. some location on both geometries intersects
    return locA != Location.EXTERIOR && locB != Location.EXTERIOR;
  }
  
  private int value = UNKNOWN;

  /*
  public boolean isSelfNodingRequired() {
    return false;
  }
  */
  
  @Override
  public boolean isKnown() {
    return isKnown(value);
  }
  
  @Override
  public boolean value() {
    return toBoolean(value);
  }
  
  /**
   * Updates the predicate value to the given state
   * if it is currently unknown.
   * 
   * @param val the predicate value to update
   */
  protected void setValue(boolean val) {
    //-- don't change already-known value
    if (isKnown())
      return;
    value = toValue(val);
  }
  
  protected void setValue(int val) {
    //-- don't change already-known value
    if (isKnown())
      return;
    value = val;
  }

  protected void setValueIf(boolean value, boolean cond) {
    if (cond)
      setValue(value);
  }
  
  protected void require(boolean cond) {
    if (! cond)
      setValue(false);
  }
  
  protected void requireCovers(Envelope a, Envelope b) {
    require(a.covers(b));
  }
}
