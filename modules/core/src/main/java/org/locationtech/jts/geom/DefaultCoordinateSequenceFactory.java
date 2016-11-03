
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