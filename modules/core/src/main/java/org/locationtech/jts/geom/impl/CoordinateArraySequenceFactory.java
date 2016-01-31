/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package org.locationtech.jts.geom.impl;

import java.io.Serializable;

import org.locationtech.jts.geom.*;

/**
 * Creates {@link CoordinateSequence}s represented as an array of {@link Coordinate}s.
 *
 * @version 1.7
 */
public final class CoordinateArraySequenceFactory
    implements CoordinateSequenceFactory, Serializable
{
  private static final long serialVersionUID = -4099577099607551657L;
  private static final CoordinateArraySequenceFactory instanceObject = new CoordinateArraySequenceFactory();

  private CoordinateArraySequenceFactory() {
  }

  private Object readResolve() {
  	// http://www.javaworld.com/javaworld/javatips/jw-javatip122.html
    return CoordinateArraySequenceFactory.instance();
  }

  /**
   * Returns the singleton instance of {@link CoordinateArraySequenceFactory}
   */
  public static CoordinateArraySequenceFactory instance() {
    return instanceObject;
  }

  /**
   * Returns a {@link CoordinateArraySequence} based on the given array (the array is
   * not copied).
   *
   * @param coordinates
   *            the coordinates, which may not be null nor contain null
   *            elements
   */
  public CoordinateSequence create(Coordinate[] coordinates) {
    return new CoordinateArraySequence(coordinates);
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(org.locationtech.jts.geom.CoordinateSequence)
   */
  public CoordinateSequence create(CoordinateSequence coordSeq) {
    return new CoordinateArraySequence(coordSeq);
  }

  /**
   * The created sequence dimension is clamped to be &lt;= 3.
   * 
   * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(int, int)
   *
   */
  public CoordinateSequence create(int size, int dimension) {
    if (dimension > 3)
      dimension = 3;
      //throw new IllegalArgumentException("dimension must be <= 3");
    // handle bogus dimension
    if (dimension < 2)
    	// TODO: change to dimension = 2  ???
      return new CoordinateArraySequence(size);
    return new CoordinateArraySequence(size, dimension);
  }
}