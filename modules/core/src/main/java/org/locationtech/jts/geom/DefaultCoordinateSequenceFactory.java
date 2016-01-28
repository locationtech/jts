
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
package org.locationtech.jts.geom;

import java.io.Serializable;

/**
 * Creates CoordinateSequences represented as an array of {@link Coordinate}s.
 *
 * @version 1.7
 *
 * @deprecated no longer used
 */
public class DefaultCoordinateSequenceFactory
    implements CoordinateSequenceFactory, Serializable
{
  private static final long serialVersionUID = -4099577099607551657L;
  private static final DefaultCoordinateSequenceFactory instanceObject = new DefaultCoordinateSequenceFactory();

  public DefaultCoordinateSequenceFactory() {
  }

  private Object readResolve() {
  	// see http://www.javaworld.com/javaworld/javatips/jw-javatip122.html
    return DefaultCoordinateSequenceFactory.instance();
  }

  /**
   * Returns the singleton instance of DefaultCoordinateSequenceFactory
   */
  public static DefaultCoordinateSequenceFactory instance() {
    return instanceObject;
  }



  /**
   * Returns a DefaultCoordinateSequence based on the given array (the array is
   * not copied).
   *
   * @param coordinates
   *            the coordinates, which may not be null nor contain null
   *            elements
   */
  public CoordinateSequence create(Coordinate[] coordinates) {
    return new DefaultCoordinateSequence(coordinates);
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(org.locationtech.jts.geom.CoordinateSequence)
   */
  public CoordinateSequence create(CoordinateSequence coordSeq) {
    return new DefaultCoordinateSequence(coordSeq);
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(int, int)
   */
  public CoordinateSequence create(int size, int dimension) {
    return new DefaultCoordinateSequence(size);
  }
}