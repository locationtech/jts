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
package org.locationtech.jts.geom.impl;

import java.io.Serializable;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;

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