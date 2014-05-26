
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
 * Demonstrates how to implement a CoordinateSequence for a new kind of
 * coordinate (an {@link ExtendedCoordinate} in this example). In this
 * implementation, Coordinates returned by #toArray and #get are live -- parties
 * that change them are actually changing the ExtendedCoordinateSequence's
 * underlying data.
 *
 * @version 1.7
 */
public class ExtendedCoordinateSequence
    implements CoordinateSequence
{
  public static ExtendedCoordinate[] copy(Coordinate[] coordinates)
  {
    ExtendedCoordinate[] copy = new ExtendedCoordinate[coordinates.length];
    for (int i = 0; i < coordinates.length; i++) {
      copy[i] = new ExtendedCoordinate(coordinates[i]);
    }
    return copy;
  }

  public static ExtendedCoordinate[] copy(CoordinateSequence coordSeq)
  {
    ExtendedCoordinate[] copy = new ExtendedCoordinate[coordSeq.size()];
    for (int i = 0; i < coordSeq.size(); i++) {
      copy[i] = new ExtendedCoordinate(coordSeq.getCoordinate(i));
    }
    return copy;
  }

  private ExtendedCoordinate[] coordinates;

  /**
   * Copy constructor -- simply aliases the input array, for better performance.
   */
  public ExtendedCoordinateSequence(ExtendedCoordinate[] coordinates) {
    this.coordinates = coordinates;
  }

  /**
   * Constructor that makes a copy of an array of Coordinates.
   * Always makes a copy of the input array, since the actual class
   * of the Coordinates in the input array may be different from ExtendedCoordinate.
   */
  public ExtendedCoordinateSequence(Coordinate[] copyCoords) {
    coordinates = copy(copyCoords);
  }

  /**
   * Constructor that makes a copy of a CoordinateSequence.
   */
  public ExtendedCoordinateSequence(CoordinateSequence coordSeq) {
    coordinates = copy(coordSeq);
  }

  /**
   * Constructs a sequence of a given size, populated
   * with new {@link ExtendedCoordinate}s.
   *
   * @param size the size of the sequence to create
   */
  public ExtendedCoordinateSequence(int size) {
    coordinates = new ExtendedCoordinate[size];
    for (int i = 0; i < size; i++) {
      coordinates[i] = new ExtendedCoordinate();
    }
  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getDimension()
   */
  public int getDimension() { return 4; }

  public Coordinate getCoordinate(int i) {
    return coordinates[i];
  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getX(int)
   */
  public Coordinate getCoordinateCopy(int index) {
    return new Coordinate(coordinates[index]);
  }
  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getX(int)
   */
  public void getCoordinate(int index, Coordinate coord) {
    coord.x = coordinates[index].x;
    coord.y = coordinates[index].y;
  }


  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getX(int)
   */
  public double getX(int index) {
      return coordinates[index].x;
  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getY(int)
   */
  public double getY(int index) {
      return coordinates[index].y;
  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#getOrdinate(int, int)
   */
  public double getOrdinate(int index, int ordinateIndex)
  {
    switch (ordinateIndex) {
      case CoordinateSequence.X:  return coordinates[index].x;
      case CoordinateSequence.Y:  return coordinates[index].y;
      case CoordinateSequence.Z:  return coordinates[index].z;
      case CoordinateSequence.M:  return coordinates[index].getM();
    }
    return Double.NaN;
  }

  /**
   * @see com.vividsolutions.jts.geom.CoordinateSequence#setOrdinate(int, int, double)
   */
  public void setOrdinate(int index, int ordinateIndex, double value)
  {
    switch (ordinateIndex) {
      case CoordinateSequence.X:  
        coordinates[index].x = value;
        break;
      case CoordinateSequence.Y:  
        coordinates[index].y = value;
        break;
      case CoordinateSequence.Z:  
        coordinates[index].z = value;
        break;
      case CoordinateSequence.M:  
        coordinates[index].setM(value);
        break;
    }
  }

  public Object clone() {
    ExtendedCoordinate[] cloneCoordinates = new ExtendedCoordinate[size()];
    for (int i = 0; i < coordinates.length; i++) {
      cloneCoordinates[i] = (ExtendedCoordinate) coordinates[i].clone();
    }

    return new ExtendedCoordinateSequence(cloneCoordinates);
  }

  public int size() {
    return coordinates.length;
  }

  public Coordinate[] toCoordinateArray() {
    return coordinates;
  }

  public Envelope expandEnvelope(Envelope env)
  {
    for (int i = 0; i < coordinates.length; i++ ) {
      env.expandToInclude(coordinates[i]);
    }
    return env;
  }

  public String toString()
  {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("ExtendedCoordinateSequence [");
    for (int i = 0; i < coordinates.length; i++) {
      if (i > 0) strBuf.append(", ");
      strBuf.append(coordinates[i]);
    }
    strBuf.append("]");
    return strBuf.toString();
  }
}
