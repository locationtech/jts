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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.math.MathUtil;


/**
 * Useful utility functions for handling Coordinate arrays
 *
 * @version 1.7
 */
public class CoordinateArrays {
  private final static Coordinate[] coordArrayType = new Coordinate[0];

  private CoordinateArrays() {
  }

  /**
   * Determine dimension based on subclass of {@link Coordinate}.
   *
   * @param pts supplied coordinates
   * @return number of ordinates recorded
   */
  public static int dimension(Coordinate[] pts) {
    if (pts == null || pts.length == 0) {
      return 3; // unknown, assume default
    }
    int dimension = 0;
    for (Coordinate coordinate : pts) {
      dimension = Math.max(dimension, Coordinates.dimension(coordinate));
    }
    return dimension;
  }

  /**
   * Determine number of measures based on subclass of {@link Coordinate}.
   *
   * @param pts supplied coordinates
   * @return number of measures recorded
   */
  public static int measures(Coordinate[] pts) {
    if (pts == null || pts.length == 0) {
      return 0; // unknown, assume default
    }
    int measures = 0;
    for (Coordinate coordinate : pts) {
      measures = Math.max(measures, Coordinates.measures(coordinate));
    }
    return measures;
  }


  /**
   * Utility method ensuring array contents are of consistent dimension and measures.
   * <p>
   * Array is modified in place if required, coordinates are replaced in the array as required
   * to ensure all coordinates have the same dimension and measures. The final dimension and
   * measures used are the maximum found when checking the array.
   * </p>
   *
   * @param array Modified in place to coordinates of consistent dimension and measures.
   */
  public static void enforceConsistency(Coordinate[] array)
  {
    if (array == null) {
      return;
    }
    // step one check
    int maxDimension = -1;
    int maxMeasures = -1;
    boolean isConsistent = true;
    for (int i = 0; i < array.length; i++) {
      Coordinate coordinate = array[i];
      if (coordinate != null) {
        int d = Coordinates.dimension(coordinate);
        int m = Coordinates.measures(coordinate);
        if( maxDimension == -1){
           maxDimension = d;
           maxMeasures = m;
           continue;
        }
        if( d != maxDimension || m != maxMeasures ){
          isConsistent = false;
          maxDimension = Math.max(maxDimension, d);
          maxMeasures = Math.max(maxMeasures, m);
        }
      }
    }
    if (!isConsistent) {
      // step two fix
      Coordinate sample = Coordinates.create(maxDimension, maxMeasures);
      Class<?> type = sample.getClass();

      for (int i = 0; i < array.length; i++) {
        Coordinate coordinate = array[i];
        if (coordinate != null && !coordinate.getClass().equals(type)) {
          Coordinate duplicate = Coordinates.create(maxDimension, maxMeasures);
          duplicate.setCoordinate(coordinate);
          array[i] = duplicate;
        }
      }
    }
  }

  /**
   * Utility method ensuring array contents are of the specified dimension and measures.
   * <p>
   * Array is returned unmodified if consistent, or a copy of the array is made with
   * each inconsistent coordinate duplicated into an instance of the correct dimension and measures.
   * </p></>
   *
   * @param array coordinate array
   * @param dimension
   * @param measures
   * @return array returned, or copy created if required to enforce consistency.
   */
  public static Coordinate[] enforceConsistency(Coordinate[] array,int dimension, int measures)
  {
    Coordinate sample = Coordinates.create(dimension,measures);
    Class<?> type = sample.getClass();
    boolean isConsistent = true;
    for (int i = 0; i < array.length; i++) {
      Coordinate coordinate = array[i];
      if (coordinate != null && !coordinate.getClass().equals(type)) {
        isConsistent = false;
        break;
      }
    }
    if (isConsistent) {
      return array;
    }
    else {
      Class<? extends Coordinate> coordinateType = sample.getClass();
      Coordinate copy[] = (Coordinate[]) Array.newInstance(coordinateType, array.length);
      for (int i = 0; i < copy.length; i++) {
        Coordinate coordinate = array[i];
        if (coordinate != null && !coordinate.getClass().equals(type)) {
          Coordinate duplicate = Coordinates.create(dimension,measures);
          duplicate.setCoordinate(coordinate);
          copy[i] = duplicate;
        }
        else {
          copy[i] = coordinate;
        }
      }
      return copy;
    }
  }

  /**
   * Tests whether an array of {@link Coordinate}s forms a ring,
   * by checking length and closure.
   * Self-intersection is not checked.
   *
   * @param pts an array of Coordinates
   * @return true if the coordinate form a ring.
   */
  public static boolean isRing(Coordinate[] pts) {
    if (pts.length < 4) return false;
    if (!pts[0].equals2D(pts[pts.length - 1])) return false;
    return true;
  }

  /**
   * Finds a point in a list of points which is not contained in another list of points
   *
   * @param testPts the {@link Coordinate}s to test
   * @param pts     an array of {@link Coordinate}s to test the input points against
   * @return a {@link Coordinate} from <code>testPts</code> which is not in <code>pts</code>, '
   * or <code>null</code>
   */
  public static Coordinate ptNotInList(Coordinate[] testPts, Coordinate[] pts) {
    for (int i = 0; i < testPts.length; i++) {
      Coordinate testPt = testPts[i];
      if (CoordinateArrays.indexOf(testPt, pts) < 0)
        return testPt;
    }
    return null;
  }

  /**
   * Orients an array of points to have a specified orientation.
   * The array is not copied if it already has the required orientation.
   * The points are not copied.
   * 
   * @param pts the array to orient
   * @param orientCW true if the points should be oriented CVW
   * @return the oriented array
   */
  public static Coordinate[] orient(Coordinate[] pts, boolean orientCW) {
    boolean isFlipped = orientCW == Orientation.isCCW(pts);
    if (isFlipped) {
      pts = pts.clone();
      CoordinateArrays.reverse(pts);
    }
    return pts;
  }
  
  /**
   * Compares two {@link Coordinate} arrays
   * in the forward direction of their coordinates,
   * using lexicographic ordering.
   *
   * @param pts1
   * @param pts2
   * @return an integer indicating the order
   */
  public static int compare(Coordinate[] pts1, Coordinate[] pts2) {
    int i = 0;
    while (i < pts1.length && i < pts2.length) {
      int compare = pts1[i].compareTo(pts2[i]);
      if (compare != 0)
        return compare;
      i++;
    }
    // handle situation when arrays are of different length
    if (i < pts2.length) return -1;
    if (i < pts1.length) return 1;

    return 0;
  }

  /**
   * A {@link Comparator} for {@link Coordinate} arrays
   * in the forward direction of their coordinates,
   * using lexicographic ordering.
   */
  public static class ForwardComparator
    implements Comparator {
    public int compare(Object o1, Object o2) {
      Coordinate[] pts1 = (Coordinate[]) o1;
      Coordinate[] pts2 = (Coordinate[]) o2;

      return CoordinateArrays.compare(pts1, pts2);
    }
  }


  /**
   * Determines which orientation of the {@link Coordinate} array
   * is (overall) increasing.
   * In other words, determines which end of the array is "smaller"
   * (using the standard ordering on {@link Coordinate}).
   * Returns an integer indicating the increasing direction.
   * If the sequence is a palindrome, it is defined to be
   * oriented in a positive direction.
   *
   * @param pts the array of Coordinates to test
   * @return <code>1</code> if the array is smaller at the start
   * or is a palindrome,
   * <code>-1</code> if smaller at the end
   */
  public static int increasingDirection(Coordinate[] pts) {
    for (int i = 0; i < pts.length / 2; i++) {
      int j = pts.length - 1 - i;
      // skip equal points on both ends
      int comp = pts[i].compareTo(pts[j]);
      if (comp != 0)
        return comp;
    }
    // array must be a palindrome - defined to be in positive direction
    return 1;
  }

  /**
   * Determines whether two {@link Coordinate} arrays of equal length
   * are equal in opposite directions.
   *
   * @param pts1
   * @param pts2
   * @return <code>true</code> if the two arrays are equal in opposite directions.
   */
  private static boolean isEqualReversed(Coordinate[] pts1, Coordinate[] pts2) {
    for (int i = 0; i < pts1.length; i++) {
      Coordinate p1 = pts1[i];
      Coordinate p2 = pts2[pts1.length - i - 1];
      if (p1.compareTo(p2) != 0)
        return false;
    }
    return true;
  }

  /**
   * A {@link Comparator} for {@link Coordinate} arrays
   * modulo their directionality.
   * E.g. if two coordinate arrays are identical but reversed
   * they will compare as equal under this ordering.
   * If the arrays are not equal, the ordering returned
   * is the ordering in the forward direction.
   */
  public static class BidirectionalComparator
    implements Comparator {
    public int compare(Object o1, Object o2) {
      Coordinate[] pts1 = (Coordinate[]) o1;
      Coordinate[] pts2 = (Coordinate[]) o2;

      if (pts1.length < pts2.length) return -1;
      if (pts1.length > pts2.length) return 1;

      if (pts1.length == 0) return 0;

      int forwardComp = CoordinateArrays.compare(pts1, pts2);
      boolean isEqualRev = isEqualReversed(pts1, pts2);
      if (isEqualRev)
        return 0;
      return forwardComp;
    }

    public int OLDcompare(Object o1, Object o2) {
      Coordinate[] pts1 = (Coordinate[]) o1;
      Coordinate[] pts2 = (Coordinate[]) o2;

      if (pts1.length < pts2.length) return -1;
      if (pts1.length > pts2.length) return 1;

      if (pts1.length == 0) return 0;

      int dir1 = increasingDirection(pts1);
      int dir2 = increasingDirection(pts2);

      int i1 = dir1 > 0 ? 0 : pts1.length - 1;
      int i2 = dir2 > 0 ? 0 : pts1.length - 1;

      for (int i = 0; i < pts1.length; i++) {
        int comparePt = pts1[i1].compareTo(pts2[i2]);
        if (comparePt != 0)
          return comparePt;
        i1 += dir1;
        i2 += dir2;
      }
      return 0;
    }

  }

  /**
   * Creates a deep copy of the argument {@link Coordinate} array.
   *
   * @param coordinates an array of Coordinates
   * @return a deep copy of the input
   */
  public static Coordinate[] copyDeep(Coordinate[] coordinates) {
    Coordinate[] copy = new Coordinate[coordinates.length];
    for (int i = 0; i < coordinates.length; i++) {
      copy[i] = coordinates[i].copy();
    }
    return copy;
  }

  /**
   * Creates a deep copy of a given section of a source {@link Coordinate} array
   * into a destination Coordinate array.
   * The destination array must be an appropriate size to receive
   * the copied coordinates.
   *
   * @param src       an array of Coordinates
   * @param srcStart  the index to start copying from
   * @param dest      the
   * @param destStart the destination index to start copying to
   * @param length    the number of items to copy
   */
  public static void copyDeep(Coordinate[] src, int srcStart, Coordinate[] dest, int destStart, int length) {
    for (int i = 0; i < length; i++) {
      dest[destStart + i] = src[srcStart + i].copy();
    }
  }

  /**
   * Converts the given Collection of Coordinates into a Coordinate array.
   */
  public static Coordinate[] toCoordinateArray(Collection coordList) {
    return (Coordinate[]) coordList.toArray(coordArrayType);
  }

  /**
   * Tests whether {@link Coordinate#equals(Object)} returns true for any two consecutive Coordinates
   * in the given array.
   * 
   * @param coord an array of coordinates
   * @return true if the array has repeated points
   */
  public static boolean hasRepeatedPoints(Coordinate[] coord) {
    for (int i = 1; i < coord.length; i++) {
      if (coord[i - 1].equals(coord[i])) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns either the given coordinate array if its length is greater than the
   * given amount, or an empty coordinate array.
   */
  public static Coordinate[] atLeastNCoordinatesOrNothing(int n, Coordinate[] c) {
    return c.length >= n ? c : new Coordinate[]{};
  }

  /**
   * If the coordinate array argument has repeated points,
   * constructs a new array containing no repeated points.
   * Otherwise, returns the argument.
   *
   * @param coord an array of coordinates
   * @return the array with repeated coordinates removed
   * @see #hasRepeatedPoints(Coordinate[])
   */
  public static Coordinate[] removeRepeatedPoints(Coordinate[] coord) {
    if (!hasRepeatedPoints(coord)) return coord;
    CoordinateList coordList = new CoordinateList(coord, false);
    return coordList.toCoordinateArray();
  }

  /**
   * Tests whether an array has any repeated or invalid coordinates.
   * 
   * @param coord an array of coordinates
   * @return true if the array contains repeated or invalid coordinates
   * @see Coordinate#isValid()
   */
  public static boolean hasRepeatedOrInvalidPoints(Coordinate[] coord) {
    for (int i = 0; i < coord.length; i++) {
      if (! coord[i].isValid())
        return true;
      if (i > 0 && coord[i - 1].equals(coord[i])) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * If the coordinate array argument has repeated or invalid points,
   * constructs a new array containing no repeated points.
   * Otherwise, returns the argument.
   * 
   * @param coord an array of coordinates
   * @return the array with repeated and invalid coordinates removed
   * @see #hasRepeatedOrInvalidPoints(Coordinate[])
   * @see Coordinate#isValid() 
   */
  public static Coordinate[] removeRepeatedOrInvalidPoints(Coordinate[] coord) {
    if (!hasRepeatedOrInvalidPoints(coord)) return coord;
    CoordinateList coordList = new CoordinateList();
    for (int i = 0; i < coord.length; i++) {
      if (! coord[i].isValid()) continue;
      coordList.add(coord[i], false);
    }
    return coordList.toCoordinateArray();
  }
  
  /**
   * Collapses a coordinate array to remove all null elements.
   *
   * @param coord the coordinate array to collapse
   * @return an array containing only non-null elements
   */
  public static Coordinate[] removeNull(Coordinate[] coord) {
    int nonNull = 0;
    for (int i = 0; i < coord.length; i++) {
      if (coord[i] != null) nonNull++;
    }
    Coordinate[] newCoord = new Coordinate[nonNull];
    // empty case
    if (nonNull == 0) return newCoord;

    int j = 0;
    for (int i = 0; i < coord.length; i++) {
      if (coord[i] != null) newCoord[j++] = coord[i];
    }
    return newCoord;
  }

  /**
   * Reverses the coordinates in an array in-place.
   */
  public static void reverse(Coordinate[] coord) {
    if (coord.length <= 1)
      return;
    
    int last = coord.length - 1;
    int mid = last / 2;
    for (int i = 0; i <= mid; i++) {
      Coordinate tmp = coord[i];
      coord[i] = coord[last - i];
      coord[last - i] = tmp;
    }
  }

  /**
   * Returns true if the two arrays are identical, both null, or pointwise
   * equal (as compared using Coordinate#equals)
   *
   * @see Coordinate#equals(Object)
   */
  public static boolean equals(
    Coordinate[] coord1,
    Coordinate[] coord2) {
    if (coord1 == coord2) return true;
    if (coord1 == null || coord2 == null) return false;
    if (coord1.length != coord2.length) return false;
    for (int i = 0; i < coord1.length; i++) {
      if (!coord1[i].equals(coord2[i])) return false;
    }
    return true;
  }

  /**
   * Returns true if the two arrays are identical, both null, or pointwise
   * equal, using a user-defined {@link Comparator} for {@link Coordinate} s
   *
   * @param coord1               an array of Coordinates
   * @param coord2               an array of Coordinates
   * @param coordinateComparator a Comparator for Coordinates
   */
  public static boolean equals(
    Coordinate[] coord1,
    Coordinate[] coord2,
    Comparator coordinateComparator) {
    if (coord1 == coord2) return true;
    if (coord1 == null || coord2 == null) return false;
    if (coord1.length != coord2.length) return false;
    for (int i = 0; i < coord1.length; i++) {
      if (coordinateComparator.compare(coord1[i], coord2[i]) != 0)
        return false;
    }
    return true;
  }

  /**
   * Returns the minimum coordinate, using the usual lexicographic comparison.
   *
   * @param coordinates the array to search
   * @return the minimum coordinate in the array, found using <code>compareTo</code>
   * @see Coordinate#compareTo(Coordinate)
   */
  public static Coordinate minCoordinate(Coordinate[] coordinates) {
    Coordinate minCoord = null;
    for (int i = 0; i < coordinates.length; i++) {
      if (minCoord == null || minCoord.compareTo(coordinates[i]) > 0) {
        minCoord = coordinates[i];
      }
    }
    return minCoord;
  }

  /**
   * Shifts the positions of the coordinates until <code>firstCoordinate</code>
   * is first.
   *
   * @param coordinates     the array to rearrange
   * @param firstCoordinate the coordinate to make first
   */
  public static void scroll(Coordinate[] coordinates, Coordinate firstCoordinate) {
    int i = indexOf(firstCoordinate, coordinates);
    scroll(coordinates, i);
  }

  /**
   * Shifts the positions of the coordinates until the coordinate
   * at <code>firstCoordinate</code> is first.
   *
   * @param coordinates            the array to rearrange
   * @param indexOfFirstCoordinate the index of the coordinate to make first
   */
  public static void scroll(Coordinate[] coordinates, int indexOfFirstCoordinate) {
    scroll(coordinates, indexOfFirstCoordinate, CoordinateArrays.isRing(coordinates));
  }

  /**
   * Shifts the positions of the coordinates until the coordinate
   * at <code>indexOfFirstCoordinate</code> is first.
   * <p/>
   * If {@code ensureRing} is {@code true}, first and last
   * coordinate of the returned array are equal.
   *
   * @param coordinates            the array to rearrange
   * @param indexOfFirstCoordinate the index of the coordinate to make first
   * @param ensureRing             flag indicating if returned array should form a ring.
   */
  public static void scroll(Coordinate[] coordinates, int indexOfFirstCoordinate, boolean ensureRing) {
    int i = indexOfFirstCoordinate;
    if (i <= 0) return;

    Coordinate[] newCoordinates = new Coordinate[coordinates.length];
    if (!ensureRing) {
      System.arraycopy(coordinates, i, newCoordinates, 0, coordinates.length - i);
      System.arraycopy(coordinates, 0, newCoordinates, coordinates.length - i, i);
    } else {
      int last = coordinates.length - 1;

      // fill in values
      int j;
      for (j = 0; j < last; j++)
        newCoordinates[j] = coordinates[(i + j) % last];

      // Fix the ring (first == last)
      newCoordinates[j] = newCoordinates[0].copy();
    }
    System.arraycopy(newCoordinates, 0, coordinates, 0, coordinates.length);
  }

  /**
   * Returns the index of <code>coordinate</code> in <code>coordinates</code>.
   * The first position is 0; the second, 1; etc.
   *
   * @param coordinate  the <code>Coordinate</code> to search for
   * @param coordinates the array to search
   * @return the position of <code>coordinate</code>, or -1 if it is
   * not found
   */
  public static int indexOf(Coordinate coordinate, Coordinate[] coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      if (coordinate.equals(coordinates[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Extracts a subsequence of the input {@link Coordinate} array
   * from indices <code>start</code> to
   * <code>end</code> (inclusive).
   * The input indices are clamped to the array size;
   * If the end index is less than the start index,
   * the extracted array will be empty.
   *
   * @param pts   the input array
   * @param start the index of the start of the subsequence to extract
   * @param end   the index of the end of the subsequence to extract
   * @return a subsequence of the input array
   */
  public static Coordinate[] extract(Coordinate[] pts, int start, int end) {
    start = MathUtil.clamp(start, 0, pts.length);
    end = MathUtil.clamp(end, -1, pts.length);

    int npts = end - start + 1;
    if (end < 0) npts = 0;
    if (start >= pts.length) npts = 0;
    if (end < start) npts = 0;

    Coordinate[] extractPts = new Coordinate[npts];
    if (npts == 0) return extractPts;

    int iPts = 0;
    for (int i = start; i <= end; i++) {
      extractPts[iPts++] = pts[i];
    }
    return extractPts;
  }

  /**
   * Computes the envelope of the coordinates.
   *
   * @param coordinates the coordinates to scan
   * @return the envelope of the coordinates
   */
  public static Envelope envelope(Coordinate[] coordinates) {
    Envelope env = new Envelope();
    for (int i = 0; i < coordinates.length; i++) {
      env.expandToInclude(coordinates[i]);
    }
    return env;
  }

  /**
   * Extracts the coordinates which intersect an {@link Envelope}.
   *
   * @param coordinates the coordinates to scan
   * @param env         the envelope to intersect with
   * @return an array of the coordinates which intersect the envelope
   */
  public static Coordinate[] intersection(Coordinate[] coordinates, Envelope env) {
    CoordinateList coordList = new CoordinateList();
    for (int i = 0; i < coordinates.length; i++) {
      if (env.intersects(coordinates[i]))
        coordList.add(coordinates[i], true);
    }
    return coordList.toCoordinateArray();
  }
}
