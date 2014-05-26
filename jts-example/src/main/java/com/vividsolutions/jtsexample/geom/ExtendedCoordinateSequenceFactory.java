
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
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
