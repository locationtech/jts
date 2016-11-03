
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
package org.locationtech.jtsexample.geom;

import org.locationtech.jts.geom.*;

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
     * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(int, int)
     */
    public CoordinateSequence create(int size, int dimension) {
      return new ExtendedCoordinateSequence(size);
    }

}
