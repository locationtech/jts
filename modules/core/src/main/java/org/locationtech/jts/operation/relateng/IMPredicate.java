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

import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.Location;

/**
 * A base class for predicates which are
 * determined using entries in a {@link IntersectionMatrix}.
 * 
 * @author Martin Davis
 *
 */
abstract class IMPredicate extends BasicPredicate {

  public static boolean isDimsCompatibleWithCovers(int dim0, int dim1) {
    //- allow Points coveredBy zero-length Lines
    if (dim0 == Dimension.P && dim1 == Dimension.L)
      return true;
    return dim0 >= dim1;
  }
  
  static final int DIM_UNKNOWN = Dimension.DONTCARE;

  protected int dimA;
  protected int dimB;
  protected IntersectionMatrix intMatrix;
  
  public IMPredicate() {
    intMatrix = new IntersectionMatrix();
    //-- E/E is always dim = 2
    intMatrix.set(Location.EXTERIOR, Location.EXTERIOR, Dimension.A);
  }
  
  @Override
  public void init(int dimA, int dimB) {
    this.dimA = dimA;
    this.dimB = dimB;
  }
  
  @Override
  public void updateDimension(int locA, int locB, int dimension) {
    //-- only record an increased dimension value
    if (isDimChanged(locA, locB, dimension)) {
      intMatrix.set(locA, locB, dimension);
      //-- set value if predicate value can be known
      if (isDetermined()) {
        setValue( valueIM());
      }
    }
  }

  public boolean isDimChanged(int locA, int locB, int dimension) {
    return dimension > intMatrix.get(locA, locB);
  }
  
  /**
   * Tests whether predicate evaluation can be short-circuited
   * due to the current state of the matrix providing
   * enough information to determine the predicate value.
   * <p>
   * If this value is true then {@link valueIM()}
   * must provide the correct result of the predicate.   
   * 
   * @return true if the predicate value is determined
   */
  protected abstract boolean isDetermined();
  
  /**
   * Tests whether the exterior of the specified input geometry
   * is intersected by any part of the other input.
   * 
   * @param isA the input geometry
   * @return true if the input geometry exterior is intersected
   */
  protected boolean intersectsExteriorOf(boolean isA) {
    if (isA) {
      return isIntersects(Location.EXTERIOR, Location.INTERIOR)
          || isIntersects(Location.EXTERIOR, Location.BOUNDARY);
    }
    else {
      return isIntersects(Location.INTERIOR, Location.EXTERIOR)
          || isIntersects(Location.BOUNDARY, Location.EXTERIOR);      
    }
  }
  
  protected boolean isIntersects(int locA, int locB) {
    return intMatrix.get(locA, locB) >= Dimension.P;
  }
  
  public boolean isKnown(int locA, int locB) {
    return intMatrix.get(locA, locB) != DIM_UNKNOWN;
  }
  
  public boolean isDimension(int locA, int locB, int dimension) {
    return intMatrix.get(locA, locB) == dimension;
  }
  
  public int getDimension(int locA, int locB) {
    return intMatrix.get(locA, locB);
  }
  
  /**
   * Sets the final value based on the state of the IM.
   */
  @Override
  public void finish() {
    setValue(valueIM());
  }
  
  /**
   * Gets the value of the predicate according to the current
   * intersection matrix state.
   * 
   * @return the current predicate value
   */
  protected abstract boolean valueIM();

  public String toString() {
    return name() + ": " + intMatrix;
  }

}
