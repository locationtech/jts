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
package org.locationtech.jts.geom;

import org.locationtech.jts.io.OrdinateFormat;


/**
 * Utility functions for manipulating {@link CoordinateSequence}s
 *
 * @version 1.7
 */
public class CoordinateSequences {

  /**
   * Reverses the coordinates in a sequence in-place.
   * 
   * @param seq the coordinate sequence to reverse
   */
  public static void reverse(CoordinateSequence seq)
  {
    if (seq.size() <= 1) return;
    
    int last = seq.size() - 1;
    int mid = last / 2;
    for (int i = 0; i <= mid; i++) {
      swap(seq, i, last - i);
    }
  }

  /**
   * Swaps two coordinates in a sequence.
   *
   * @param seq the sequence to modify
   * @param i the index of a coordinate to swap
   * @param j the index of a coordinate to swap
   */
  public static void swap(CoordinateSequence seq, int i, int j)
  {
    if (i == j) return;
    for (int dim = 0; dim < seq.getDimension(); dim++) {
      double tmp = seq.getOrdinate(i, dim);
      seq.setOrdinate(i, dim, seq.getOrdinate(j, dim));
      seq.setOrdinate(j, dim, tmp);
    }
  }
  
  /**
   * Copies a section of a {@link CoordinateSequence} to another {@link CoordinateSequence}.
   * The sequences may have different dimensions;
   * in this case only the common dimensions are copied.
   *
   * @param src the sequence to copy from
   * @param srcPos the position in the source sequence to start copying at
   * @param dest the sequence to copy to
   * @param destPos the position in the destination sequence to copy to
   * @param length the number of coordinates to copy
   */
  public static void copy(CoordinateSequence src, int srcPos, CoordinateSequence dest, int destPos, int length)
  {
  	for (int i = 0; i < length; i++) {
  		copyCoord(src, srcPos + i, dest, destPos + i);
  	}
  }

  /**
   * Copies a coordinate of a {@link CoordinateSequence} to another {@link CoordinateSequence}.
   * The sequences may have different dimensions;
   * in this case only the common dimensions are copied.
   * 
   * @param src the sequence to copy from
   * @param srcPos the source coordinate to copy
   * @param dest the sequence to copy to
   * @param destPos the destination coordinate to copy to
   */
  public static void copyCoord(CoordinateSequence src, int srcPos, CoordinateSequence dest, int destPos)
  {
    int minDim = Math.min(src.getDimension(), dest.getDimension());
		for (int dim = 0; dim < minDim; dim++) {
			dest.setOrdinate(destPos, dim, src.getOrdinate(srcPos, dim));
		}
  }
  
  /**
   * Tests whether a {@link CoordinateSequence} forms a valid {@link LinearRing},
   * by checking the sequence length and closure
   * (whether the first and last points are identical in 2D). 
   * Self-intersection is not checked.
   * 
   * @param seq the sequence to test
   * @return true if the sequence is a ring
   * @see LinearRing
   */
  public static boolean isRing(CoordinateSequence seq) 
  {
  	int n = seq.size();
  	if (n == 0) return true;
  	// too few points
  	if (n <= 3) 
  		return false;
  	// test if closed
  	return seq.getOrdinate(0, CoordinateSequence.X) == seq.getOrdinate(n-1, CoordinateSequence.X)
  		&& seq.getOrdinate(0, CoordinateSequence.Y) == seq.getOrdinate(n-1, CoordinateSequence.Y);
  }
  
  /**
   * Ensures that a CoordinateSequence forms a valid ring, 
   * returning a new closed sequence of the correct length if required.
   * If the input sequence is already a valid ring, it is returned 
   * without modification.
   * If the input sequence is too short or is not closed, 
   * it is extended with one or more copies of the start point.
   * 
   * @param fact the CoordinateSequenceFactory to use to create the new sequence
   * @param seq the sequence to test
   * @return the original sequence, if it was a valid ring, or a new sequence which is valid.
   */
  public static CoordinateSequence ensureValidRing(CoordinateSequenceFactory fact, CoordinateSequence seq)
  {
  	int n = seq.size();
  	// empty sequence is valid
  	if (n == 0) return seq; 
  	// too short - make a new one
  	if (n <= 3) 
  		return createClosedRing(fact, seq, 4);
  	
  	boolean isClosed = seq.getOrdinate(0, CoordinateSequence.X) == seq.getOrdinate(n-1, CoordinateSequence.X)
		&& seq.getOrdinate(0, CoordinateSequence.Y) == seq.getOrdinate(n-1, CoordinateSequence.Y);
  	if (isClosed) return seq;
  	// make a new closed ring
  	return createClosedRing(fact, seq, n+1);
  }
  
  private static CoordinateSequence createClosedRing(CoordinateSequenceFactory fact, CoordinateSequence seq, int size)
  {
    CoordinateSequence newseq = fact.create(size, seq.getDimension());
    int n = seq.size();
    copy(seq, 0, newseq, 0, n);
    // fill remaining coordinates with start point
    for (int i = n; i < size; i++)
      copy(seq, 0, newseq, i, 1);
    return newseq;
  }
  
  public static CoordinateSequence extend(CoordinateSequenceFactory fact, CoordinateSequence seq, int size)
  {
    CoordinateSequence newseq = fact.create(size, seq.getDimension());
    int n = seq.size();
    copy(seq, 0, newseq, 0, n);
    // fill remaining coordinates with end point, if it exists
    if (n > 0) {
      for (int i = n; i < size; i++)
        copy(seq, n-1, newseq, i, 1);
    }
    return newseq;
  }

  /**
   * Tests whether two {@link CoordinateSequence}s are equal.
   * To be equal, the sequences must be the same length.
   * They do not need to be of the same dimension, 
   * but the ordinate values for the smallest dimension of the two
   * must be equal.
   * Two <code>NaN</code> ordinates values are considered to be equal. 
   * 
   * @param cs1 a CoordinateSequence
   * @param cs2 a CoordinateSequence
   * @return true if the sequences are equal in the common dimensions
   */
  public static boolean isEqual(CoordinateSequence cs1, CoordinateSequence cs2) {
    int cs1Size = cs1.size();
    int cs2Size = cs2.size();
    if (cs1Size != cs2Size) return false;
    int dim = Math.min(cs1.getDimension(), cs2.getDimension());
    for (int i = 0; i < cs1Size; i++) {
      for (int d = 0; d < dim; d++) {
        double v1 = cs1.getOrdinate(i, d);
        double v2 = cs2.getOrdinate(i, d);
        if (cs1.getOrdinate(i, d) == cs2.getOrdinate(i, d)) {
          continue;
        }
        else if (Double.isNaN(v1) && Double.isNaN(v2)) {
          // special check for NaNs
          continue;
        }
        else {
          return false;
        }
      }
    }
    return true;
  }
  
  /**
   * Creates a string representation of a {@link CoordinateSequence}.
   * The format is:
   * <pre>
   *   ( ord0,ord1.. ord0,ord1,...  ... )
   * </pre>
   * 
   * @param cs the sequence to output
   * @return the string representation of the sequence
   */
  public static String toString(CoordinateSequence cs)
  {
    int size = cs.size();
    if (size == 0) 
      return "()";
    int dim = cs.getDimension();
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    for (int i = 0; i < size; i++) {
      if (i > 0) builder.append(" ");
      for (int d = 0; d < dim; d++) {
        if (d > 0) builder.append(",");
        builder.append( OrdinateFormat.DEFAULT.format(cs.getOrdinate(i, d)) );
      }
    }
    builder.append(')');
    return builder.toString();
  }

  /**
   *  Returns the minimum coordinate, using the usual lexicographic comparison.
   *
   *@param  seq  the coordinate sequence to search
   *@return  the minimum coordinate in the sequence, found using <code>compareTo</code>
   *@see Coordinate#compareTo(Object)
   */
  public static Coordinate minCoordinate(CoordinateSequence seq)
  {
    Coordinate minCoord = null;
    for (int i = 0; i < seq.size(); i++) {
      Coordinate testCoord = seq.getCoordinate(i);
      if (minCoord == null || minCoord.compareTo(testCoord) > 0) {
        minCoord = testCoord;
      }
    }
    return minCoord;
  }
  /**
   *  Returns the index of the minimum coordinate of the whole
   *  coordinate sequence, using the usual lexicographic comparison.
   *
   *@param  seq  the coordinate sequence to search
   *@return  the index of the minimum coordinate in the sequence, found using <code>compareTo</code>
   *@see Coordinate#compareTo(Object)
   */
  public static int minCoordinateIndex(CoordinateSequence seq) {
    return minCoordinateIndex(seq, 0, seq.size() - 1);
  }

  /**
   *  Returns the index of the minimum coordinate of a part of
   *  the coordinate sequence (defined by {@code from} and {@code to},
   *  using the usual lexicographic comparison.
   *
   *@param  seq   the coordinate sequence to search
   *@param  from  the lower search index
   *@param  to    the upper search index
   *@return  the index of the minimum coordinate in the sequence, found using <code>compareTo</code>
   *@see Coordinate#compareTo(Object)
   */
  public static int minCoordinateIndex(CoordinateSequence seq, int from, int to)
  {
    int minCoordIndex = -1;
    Coordinate minCoord = null;
    for (int i = from; i <= to; i++) {
      Coordinate testCoord = seq.getCoordinate(i);
      if (minCoord == null || minCoord.compareTo(testCoord) > 0) {
          minCoord = testCoord;
          minCoordIndex = i;
      }
    }
    return minCoordIndex;
  }

  /**
   *  Shifts the positions of the coordinates until <code>firstCoordinate</code>
   *  is first.
   *
   *@param  seq      the coordinate sequence to rearrange
   *@param  firstCoordinate  the coordinate to make first
   */
  public static void scroll(CoordinateSequence seq, Coordinate firstCoordinate) {
    int i = indexOf(firstCoordinate, seq);
    if (i <= 0) return;
    scroll(seq, i);
  }

  /**
   *  Shifts the positions of the coordinates until the coordinate at  <code>firstCoordinateIndex</code>
   *  is first.
   *
   *@param  seq      the coordinate sequence to rearrange
   *@param  indexOfFirstCoordinate  the index of the coordinate to make first
   */
  public static void scroll(CoordinateSequence seq, int indexOfFirstCoordinate)
  {
    scroll(seq, indexOfFirstCoordinate, CoordinateSequences.isRing(seq));
  }

  /**
   *  Shifts the positions of the coordinates until the coordinate at  <code>firstCoordinateIndex</code>
   *  is first.
   *
   *@param  seq      the coordinate sequence to rearrange
   *@param  indexOfFirstCoordinate
   *                 the index of the coordinate to make first
   *@param  ensureRing
   *                 makes sure that {@code} will be a closed ring upon exit
   */
    public static void scroll(CoordinateSequence seq, int indexOfFirstCoordinate, boolean ensureRing) {
    int i = indexOfFirstCoordinate;
    if (i <= 0) return;

    // make a copy of the sequence
    CoordinateSequence copy = seq.copy();

    // test if ring, determine last index
    int last = ensureRing ? seq.size() - 1: seq.size();

    // fill in values
    for (int j = 0; j < last; j++)
    {
      for (int k = 0; k < seq.getDimension(); k++)
        seq.setOrdinate(j, k, copy.getOrdinate((indexOfFirstCoordinate+j)%last, k));
    }

    // Fix the ring (first == last)
    if (ensureRing) {
      for (int k = 0; k < seq.getDimension(); k++)
        seq.setOrdinate(last, k, seq.getOrdinate(0, k));
    }
  }

  /**
   *  Returns the index of <code>coordinate</code> in a {@link CoordinateSequence}
   *  The first position is 0; the second, 1; etc.
   *
   *@param  coordinate   the <code>Coordinate</code> to search for
   *@param  seq  the coordinate sequence to search
   *@return              the position of <code>coordinate</code>, or -1 if it is
   *      not found
   */
  public static int indexOf(Coordinate coordinate, CoordinateSequence seq) {
    for (int i = 0; i < seq.size(); i++) {
      if (coordinate.x == seq.getOrdinate(i, CoordinateSequence.X) &&
          coordinate.y == seq.getOrdinate(i, CoordinateSequence.Y)) {
        return i;
      }
    }
    return -1;
  }}
