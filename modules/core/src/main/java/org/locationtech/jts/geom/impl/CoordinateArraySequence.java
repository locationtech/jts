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
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 * A {@link CoordinateSequence} backed by an array of {@link Coordinate}s.
 * This is the implementation that {@link Geometry}s use by default.
 * Coordinates returned by #toArray and #getCoordinate are live --
 * modifications to them are actually changing the
 * CoordinateSequence's underlying data.
 * A dimension may be specified for the coordinates in the sequence,
 * which may be 2 or 3.
 * The actual coordinates will always have 3 ordinates,
 * but the dimension is useful as metadata in some situations. 
 *
 * @version 1.7
 */
public class CoordinateArraySequence
    implements CoordinateSequence, Serializable
{
  //With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
  private static final long serialVersionUID = -915438501601840650L;

  /**
   * The actual dimension of the coordinates in the sequence.
   * Allowable values are 2 or 3.
   */
  private int dimension = 3;
  
  private Coordinate[] coordinates;

  /**
   * Constructs a sequence based on the given array
   * of {@link Coordinate}s (the
   * array is not copied).
   * The coordinate dimension defaults to 3.
   *
   * @param coordinates the coordinate array that will be referenced.
   */
  public CoordinateArraySequence(Coordinate[] coordinates) {
    this(coordinates, 3);
  }

  /**
   * Constructs a sequence based on the given array 
   * of {@link Coordinate}s (the
   * array is not copied).
   *
   * @param coordinates the coordinate array that will be referenced.
   * @param dimension the dimension of the coordinates
   */
  public CoordinateArraySequence(Coordinate[] coordinates, int dimension) {
    this.coordinates = coordinates;
    this.dimension = dimension;
    if (coordinates == null)
      this.coordinates = new Coordinate[0];
  }

  /**
   * Constructs a sequence of a given size, populated
   * with new {@link Coordinate}s.
   *
   * @param size the size of the sequence to create
   */
  public CoordinateArraySequence(int size) {
    coordinates = new Coordinate[size];
    for (int i = 0; i < size; i++) {
      coordinates[i] = new Coordinate();
    }
  }

  /**
   * Constructs a sequence of a given size, populated
   * with new {@link Coordinate}s.
   *
   * @param size the size of the sequence to create
   * @param dimension the dimension of the coordinates
   */
  public CoordinateArraySequence(int size, int dimension) {
    coordinates = new Coordinate[size];
    this.dimension = dimension;
    for (int i = 0; i < size; i++) {
      coordinates[i] = new Coordinate();
    }
  }

  /**
   * Creates a new sequence based on a deep copy of the given {@link CoordinateSequence}.
   * The coordinate dimension is set to equal the dimension of the input.
   *
   * @param coordSeq the coordinate sequence that will be copied.
   */
  public CoordinateArraySequence(CoordinateSequence coordSeq)
  {
    // NOTE: this will make a sequence of the default dimension
    if (coordSeq == null) {
      coordinates = new Coordinate[0];
      return;
    }
    dimension = coordSeq.getDimension();
    coordinates = new Coordinate[coordSeq.size()];

    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] = coordSeq.getCoordinateCopy(i);
    }
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getDimension()
   */
  public int getDimension() { return dimension; }

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
    coord.z = coordinates[index].z;
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
   * Creates a deep copy of the Object
   *
   * @return The deep copy
   */
  public Object clone() {
    Coordinate[] cloneCoordinates = new Coordinate[size()];
    for (int i = 0; i < coordinates.length; i++) {
      cloneCoordinates[i] = (Coordinate) coordinates[i].clone();
    }
    return new CoordinateArraySequence(cloneCoordinates, dimension);
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
   * @see org.locationtech.jts.geom.CoordinateSequence#setOrdinate(int, int, double)
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
      default:
          throw new IllegalArgumentException("invalid ordinateIndex");
    }
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