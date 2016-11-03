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
 * The CoordinateSequence implementation that Geometries use by default. In
 * this implementation, Coordinates returned by #toArray and #get are live --
 * parties that change them are actually changing the
 * DefaultCoordinateSequence's underlying data.
 *
 * @version 1.7
 *
 * @deprecated no longer used
 */
class DefaultCoordinateSequence
    implements CoordinateSequence, Serializable
{
  //With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
  private static final long serialVersionUID = -915438501601840650L;
  private Coordinate[] coordinates;

  /**
   * Constructs a DefaultCoordinateSequence based on the given array (the
   * array is not copied).
   *
   * @param coordinates the coordinate array that will be referenced.
   */
  public DefaultCoordinateSequence(Coordinate[] coordinates) {
    if (Geometry.hasNullElements(coordinates)) {
      throw new IllegalArgumentException("Null coordinate");
    }
    this.coordinates = coordinates;
  }

  /**
   * Creates a new sequence based on a deep copy of the given {@link CoordinateSequence}.
   *
   * @param coordSeq the coordinate sequence that will be copied.
   */
  public DefaultCoordinateSequence(CoordinateSequence coordSeq) {
    coordinates = new Coordinate[coordSeq.size()];
    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] = coordSeq.getCoordinateCopy(i);
    }
  }

  /**
   * Constructs a sequence of a given size, populated
   * with new {@link Coordinate}s.
   *
   * @param size the size of the sequence to create
   */
  public DefaultCoordinateSequence(int size) {
    coordinates = new Coordinate[size];
    for (int i = 0; i < size; i++) {
      coordinates[i] = new Coordinate();
    }
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getDimension()
   */
  public int getDimension() { return 3; }

  /**
   * Get the Coordinate with index i.
   *
   * @param i
   *                  the index of the coordinate
   * @return the requested Coordinate instance
   */
  public Coordinate getCoordinate(int i) {
    return coordinates[i];
  }
  /**
   * Get a copy of the Coordinate with index i.
   *
   * @param i  the index of the coordinate
   * @return a copy of the requested Coordinate
   */
  public Coordinate getCoordinateCopy(int i) {
    return new Coordinate(coordinates[i]);
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getX(int)
   */
  public void getCoordinate(int index, Coordinate coord) {
    coord.x = coordinates[index].x;
    coord.y = coordinates[index].y;
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getX(int)
   */
  public double getX(int index) {
    return coordinates[index].x;
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getY(int)
   */
  public double getY(int index) {
    return coordinates[index].y;
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getOrdinate(int, int)
   */
  public double getOrdinate(int index, int ordinateIndex)
  {
    switch (ordinateIndex) {
      case CoordinateSequence.X:  return coordinates[index].x;
      case CoordinateSequence.Y:  return coordinates[index].y;
      case CoordinateSequence.Z:  return coordinates[index].z;
    }
    return Double.NaN;
  }
  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#setOrdinate(int, int, double)
   */
  public void setOrdinate(int index, int ordinateIndex, double value)
  {
    switch (ordinateIndex) {
      case CoordinateSequence.X:  coordinates[index].x = value; break;
      case CoordinateSequence.Y:  coordinates[index].y = value; break;
      case CoordinateSequence.Z:  coordinates[index].z = value; break;
    }
  }
  /**
   * Creates a deep copy of the Object
   *
   * @return The deep copy
   */
  public Object clone() {
    Coordinate[] cloneCoordinates = new Coordinate[size()];
    for (int i = 0; i < coordinates.length; i++) {
      cloneCoordinates[i] = (Coordinate) coordinates[i].clone();
    }
    return new DefaultCoordinateSequence(cloneCoordinates);
  }
  /**
   * Returns the size of the coordinate sequence
   *
   * @return the number of coordinates
   */
  public int size() {
    return coordinates.length;
  }
  /**
   * This method exposes the internal Array of Coordinate Objects
   *
   * @return the Coordinate[] array.
   */
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

  /**
   * Returns the string Representation of the coordinate array
   *
   * @return The string
   */
  public String toString() {
    if (coordinates.length > 0) {
      StringBuffer strBuf = new StringBuffer(17 * coordinates.length);
      strBuf.append('(');
      strBuf.append(coordinates[0]);
      for (int i = 1; i < coordinates.length; i++) {
        strBuf.append(", ");
        strBuf.append(coordinates[i]);
      }
      strBuf.append(')');
      return strBuf.toString();
    } else {
      return "()";
    }
  }
}