/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.impl;

import java.io.Serializable;

import org.locationtech.jts.geom.*;

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
   * Allowable values are 2, 3 or 4.
   */
  private int dimension = 3;
  /**
   * The number of measures of the coordinates in the sequence.
   * Allowable values are 0 or 1.
   */
  private int measures = 0;
  
  private Coordinate[] coordinates;

  /**
   * Constructs a sequence based on the given array
   * of {@link Coordinate}s (the
   * array is not copied).
   * The coordinate dimension defaults to 3.
   *
   * @param coordinates the coordinate array that will be referenced.
   */
  public CoordinateArraySequence(Coordinate[] coordinates)
  {
    this(coordinates, CoordinateArrays.dimension(coordinates), CoordinateArrays.measures(coordinates));
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
    this(coordinates, dimension, CoordinateArrays.measures(coordinates));    
  }
  
  /**
   * Constructs a sequence based on the given array 
   * of {@link Coordinate}s (the array is not copied).
   * <p>
   * It is your responsibility to ensure the array contains Coordinates of the
   * indicated dimension and measures (See 
   * {@link CoordinateArrays#enforceConsistency(Coordinate[])} ).</p>
   *
   * @param coordinates the coordinate array that will be referenced.
   * @param dimension the dimension of the coordinates
   */
  public CoordinateArraySequence(Coordinate[] coordinates, int dimension, int measures)
  {
    this.dimension = dimension;
    this.measures = measures;
    if (coordinates == null) {
      this.coordinates = new Coordinate[0];
    }
    else {
      this.coordinates = coordinates;
    }
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
      coordinates[i] = Coordinates.create(dimension);
    }
  }
  /**
   * Constructs a sequence of a given size, populated
   * with new {@link Coordinate}s.
   *
   * @param size the size of the sequence to create
   * @param dimension the dimension of the coordinates
   */
  public CoordinateArraySequence(int size, int dimension,int measures) {
    coordinates = new Coordinate[size];
    this.dimension = dimension;
    this.measures = measures;
    for (int i = 0; i < size; i++) {
      coordinates[i] = createCoordinate();
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
    measures = coordSeq.getMeasures();    
    coordinates = new Coordinate[coordSeq.size()];

    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] = coordSeq.getCoordinateCopy(i);
    }
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getDimension()
   */
  public int getDimension()
  {
    return dimension;
  }
  
  @Override
  public int getMeasures()
  {
    return measures;
  }

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
    Coordinate copy = createCoordinate();
    copy.setCoordinate(coordinates[i]);
    return copy;
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getX(int)
   */
  public void getCoordinate(int index, Coordinate coord) {
    coord.setCoordinate(coordinates[index]);
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
   * @see org.locationtech.jts.geom.CoordinateSequence#getZ(int)
   */
  public double getZ(int index)
  {
    if (hasZ()) {
      return coordinates[index].getZ();
    } else {
      return Double.NaN;
    }

  }
  
  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getM(int)
   */
  public double getM(int index) {
    if (hasM()) {
      return coordinates[index].getM();
    }
    else {
        return Double.NaN;
    }    
  }
  
  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getOrdinate(int, int)
   */
  public double getOrdinate(int index, int ordinateIndex)
  {
    switch (ordinateIndex) {
      case CoordinateSequence.X:  return coordinates[index].x;
      case CoordinateSequence.Y:  return coordinates[index].y;
      default:
	      return coordinates[index].getOrdinate(ordinateIndex);
    }
  }

  /**
   * Creates a deep copy of the Object
   *
   * @return The deep copy
   * @deprecated
   */
  public Object clone() {
    return copy();
  }
  /**
   * Creates a deep copy of the CoordinateArraySequence
   *
   * @return The deep copy
   */
  public CoordinateArraySequence copy() {
    Coordinate[] cloneCoordinates = new Coordinate[size()];
    for (int i = 0; i < coordinates.length; i++) {
      Coordinate duplicate = createCoordinate();
      duplicate.setCoordinate(coordinates[i]);
      cloneCoordinates[i] = duplicate;
    }
    return new CoordinateArraySequence(cloneCoordinates, dimension, measures);
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
      default:
        coordinates[index].setOrdinate(ordinateIndex, value);
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
      StringBuilder strBuilder = new StringBuilder(17 * coordinates.length);
      strBuilder.append('(');
      strBuilder.append(coordinates[0]);
      for (int i = 1; i < coordinates.length; i++) {
        strBuilder.append(", ");
        strBuilder.append(coordinates[i]);
      }
      strBuilder.append(')');
      return strBuilder.toString();
    } else {
      return "()";
    }
  }
}
