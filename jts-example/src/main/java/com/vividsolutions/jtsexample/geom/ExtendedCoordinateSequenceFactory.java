
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
package com.vividsolutions.jtsexample.geom;

import com.vividsolutions.jts.geom.*;

/**
 * Creates ExtendedCoordinateSequenceFactory internally represented
 * as an array of {@link ExtendedCoordinate}s.
 *
 * @version 1.7
 */
public class ExtendedCoordinateSequenceFactory
    implements CoordinateSequenceFactory
{
    private static ExtendedCoordinateSequenceFactory instance = new ExtendedCoordinateSequenceFactory();

    private ExtendedCoordinateSequenceFactory() {
    }

    /**
     * Returns the singleton instance of ExtendedCoordinateSequenceFactory
     */
    public static ExtendedCoordinateSequenceFactory instance() {
        return instance;
    }

    /**
     * Returns an ExtendedCoordinateSequence based on the given array -- the array is used
     * directly if it is an instance of ExtendedCoordinate[]; otherwise it is
     * copied.
     */
    public CoordinateSequence create(Coordinate[] coordinates) {
      return coordinates instanceof ExtendedCoordinate[]
          ? new ExtendedCoordinateSequence((ExtendedCoordinate[]) coordinates)
          : new ExtendedCoordinateSequence(coordinates);
    }

    public CoordinateSequence create(CoordinateSequence coordSeq) {
      return coordSeq instanceof ExtendedCoordinateSequence
          ? new ExtendedCoordinateSequence((ExtendedCoordinateSequence) coordSeq)
          : new ExtendedCoordinateSequence(coordSeq);
    }

    /**
     * @see com.vividsolutions.jts.geom.CoordinateSequenceFactory#create(int, int)
     */
    public CoordinateSequence create(int size, int dimension) {
      return new ExtendedCoordinateSequence(size);
    }

}
