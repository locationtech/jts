/*
 * Copyright (c) 2018 Vivid Solutions
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom;

import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

/**
 * The internal representation of a list of coordinates inside a Geometry.
 * <p>
 * This allows Geometries to store their
 * points using something other than the JTS {@link Coordinate} class. 
 * For example, a storage-efficient implementation
 * might store coordinate sequences as an array of x's
 * and an array of y's. 
 * Or a custom coordinate class might support extra attributes like M-values.
 * <p>
 * Implementing a custom coordinate storage structure
 * requires implementing the {@link CoordinateSequence} and
 * {@link CoordinateSequenceFactory} interfaces. 
 * To use the custom CoordinateSequence, create a
 * new {@link GeometryFactory} parameterized by the CoordinateSequenceFactory
 * The {@link GeometryFactory} can then be used to create new {@link Geometry}s.
 * The new Geometries
 * will use the custom CoordinateSequence implementation.
 * <p>
 * For an example, see the code for ExtendedCoordinateExample.
 *
 * @see CoordinateArraySequenceFactory
 * @see PackedCoordinateSequenceFactory
 *
 * @version 1.7
 */
public interface CoordinateSequence
    extends Cloneable
{
  /** Standard ordinate index value for, where X is 0 */
  int X = 0;

  /** Standard ordinate index value for, where Y is 1 */
  int Y = 1;
  
  /**
   * Standard ordinate index value for, where Z is 2.
   *
   * <p>This constant assumes XYZM coordinate sequence definition, please check this assumption
   * using {@link #getDimension()} and {@link #getMeasures()} before use.
   */
  /** Standard z-ordinate index */
  int Z = 2;

  /**
   * Standard ordinate index value for, where M is 3.
   *
   * <p>This constant assumes XYZM coordinate sequence definition, please check this assumption
   * using {@link #getDimension()} and {@link #getMeasures()} before use.
   */
  int M = 3;

  /**
   * Returns the dimension (number of ordinates in each coordinate) for this sequence.
   *
   * <p>This total includes any measures, indicated by non-zero {@link #getMeasures()}.
   *
   * @return the dimension of the sequence.
   */
  int getDimension();

  /**
   * Returns the number of measures included in {@link #getDimension()} for each coordinate for this
   * sequence.
   * 
   * For a measured coordinate sequence a non-zero value is returned.
   * <ul>
   * <li>For XY sequence measures is zero</li>
   * <li>For XYM sequence measure is one<li>
   * <li>For XYZ sequence measure is zero</li>
   * <li>For XYZM sequence measure is one</li>
   * <li>Values greater than one are supported</li>
   * </ul>
   *
   * @return the number of measures included in dimension
   */
  default int getMeasures() {
    return 0;
  }
  
  /**
   * Checks {@link #getDimension()} and {@link #getMeasures()} to determine if {@link #getZ(int)}
   * is supported.
   * 
   * @return true if {@link #getZ(int)} is supported.
   */
  default boolean hasZ() {
      return (getDimension()-getMeasures()) > 2;
  }

  /**
   * Tests whether the coordinates in the sequence have measures associated with them. Returns true
   * if {@link #getMeasures()} {@code > 0}. See {@link #getMeasures()} to determine the number of measures
   * present.
   *
   * @return true if {@link #getM(int)} is supported.
   *
   * @see #getMeasures()
   * @see #getM(int)
   */
  default boolean hasM() {
      return getMeasures() > 0;
  }

  /**
   * Creates a coordinate for use in this sequence.
   * <p>
   * The coordinate is created supporting the same number of {@link #getDimension()} and {@link #getMeasures()}
   * as this sequence and is suitable for use with {@link #getCoordinate(int, Coordinate)}.
   * </p>
   * @return coordinate for use with this sequence
   */
  default Coordinate createCoordinate() {
    return Coordinates.create(getDimension(), getMeasures());
  }
  
  /**
   * Returns (possibly a copy of) the i'th coordinate in this sequence.
   * Whether or not the Coordinate returned is the actual underlying
   * Coordinate or merely a copy depends on the implementation.
   * <p>
   * Note that in the future the semantics of this method may change
   * to guarantee that the Coordinate returned is always a copy.
   * Callers should not to assume that they can modify a CoordinateSequence by
   * modifying the object returned by this method.
   *
   * @param i the index of the coordinate to retrieve
   * @return the i'th coordinate in the sequence
   */
  Coordinate getCoordinate(int i);

  /**
   * Returns a copy of the i'th coordinate in this sequence.
   * This method optimizes the situation where the caller is
   * going to make a copy anyway - if the implementation
   * has already created a new Coordinate object, no further copy is needed.
   *
   * @param i the index of the coordinate to retrieve
   * @return a copy of the i'th coordinate in the sequence
   */
  Coordinate getCoordinateCopy(int i);

  /**
   * Copies the i'th coordinate in the sequence to the supplied
   * {@link Coordinate}.  Only the first two dimensions are copied.
   *
   * @param index the index of the coordinate to copy
   * @param coord a {@link Coordinate} to receive the value
   */
  void getCoordinate(int index, Coordinate coord);

  /**
   * Returns ordinate X (0) of the specified coordinate.
   *
   * @param index  the coordinate index in the sequence
   * @return the value of the X ordinate in the index'th coordinate
   */
  double getX(int index);

  /**
   * Returns ordinate Y (1) of the specified coordinate.
   *
   * @param index  the coordinate index in the sequence
   * @return the value of the Y ordinate in the index'th coordinate
   */
  double getY(int index);

  /**
   * Returns ordinate Z of the specified coordinate if available.
   *
   @param index  the coordinate index in the sequence
   * @return the value of the Z ordinate in the index'th coordinate, or Double.NaN if not defined.
   */
  default double getZ(int index)
  {
    if (hasZ()) {
        return getOrdinate(index, 2);
    } else {
        return Double.NaN;
    }
  }

  /**
   * Returns ordinate M of the specified coordinate if available.
   * 
   * @param index  the coordinate index in the sequence
   * @return the value of the M ordinate in the index'th coordinate, or Double.NaN if not defined.
   */
  default double getM(int index)
  {
    if (hasM()) {
      final int mIndex = getDimension()-getMeasures();
      return getOrdinate( index, mIndex );
    }
    else {
        return Double.NaN;
    }
  }
  
  /**
   * Returns the ordinate of a coordinate in this sequence.
   * Ordinate indices 0 and 1 are assumed to be X and Y.
   * <p>
   * Ordinates indices greater than 1 have user-defined semantics
   * (for instance, they may contain other dimensions or measure
   * values as described by {@link #getDimension()} and {@link #getMeasures()}).
   *
   * @param index  the coordinate index in the sequence
   * @param ordinateIndex the ordinate index in the coordinate (in range [0, dimension-1])
   * @return ordinate value
   */
  double getOrdinate(int index, int ordinateIndex);

  /**
   * Returns the number of coordinates in this sequence.
   * @return the size of the sequence
   */
  int size();

  /**
   * Sets the value for a given ordinate of a coordinate in this sequence.
   *
   * @param index  the coordinate index in the sequence
   * @param ordinateIndex the ordinate index in the coordinate (in range [0, dimension-1])
   * @param value  the new ordinate value
   */
  void setOrdinate(int index, int ordinateIndex, double value);

  /**
   * Returns (possibly copies of) the Coordinates in this collection.
   * Whether or not the Coordinates returned are the actual underlying
   * Coordinates or merely copies depends on the implementation. Note that
   * if this implementation does not store its data as an array of Coordinates,
   * this method will incur a performance penalty because the array needs to
   * be built from scratch.
   *
   * @return a array of coordinates containing the point values in this sequence
   */
  Coordinate[] toCoordinateArray();

  /**
   * Expands the given {@link Envelope} to include the coordinates in the sequence.
   * Allows implementing classes to optimize access to coordinate values.
   *
   * @param env the envelope to expand
   * @return a ref to the expanded envelope
   */
  Envelope expandEnvelope(Envelope env);

  /**
   * Returns a deep copy of this collection.
   * Called by Geometry#clone.
   *
   * @return a copy of the coordinate sequence containing copies of all points
   * @deprecated Recommend {@link #copy()} 
   */
  Object clone();
  
  /**
   * Returns a deep copy of this collection.
   *
   * @return a copy of the coordinate sequence containing copies of all points
   */
  CoordinateSequence copy();
}
