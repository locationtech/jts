/*
 * Copyright (c) 2021 Felix Obermaier.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import java.util.HashMap;

/**
 * The Fréchet distance between two curves in a metric space is a measure of the
 * similarity between the curves.
 * <p/>
 * An analogy for the Fréchet distance taken from
 * <a href="http://www.kr.tuwien.ac.at/staff/eiter/et-archive/cdtr9464.pdf">Computing Discrete Fréchet Distance</a>
 * <pre>
 * A man is walking a dog on a leash: the man can move
 * on one curve, the dog on the other; both may vary their
 * speed, but backtracking is not allowed.
 * </pre>
 * @see <a href="http://www.kr.tuwien.ac.at/staff/eiter/et-archive/cdtr9464.pdf">Computing Discrete Fréchet Distance</a>
 */
public class DiscreteFrechetDistance {

  /**
   * Computes the Discrete Fréchet Distance between two {@link Geometry}s
   * using a {@code cartesian} distance computation function.
   *
   * @param g0 the 1st geometry
   * @param g1 the 2nd geometry
   * @return the cartesian distance between {#g0} and {#g1}
   */
  public static double distance(Geometry g0, Geometry g1) {
    return distance(g0, g1, CartesianDistance.getInstance());
  }

  /**
   * Computes the discrete Fréchet distance between two {@link Geometry}s
   * using the provided distance computation function.
   *
   * @param g0 the 1st geometry
   * @param g1 the 2nd geometry
   * @return the Discrete Fréchet Distance between the two geometries
   */
  public static double distance(Geometry g0, Geometry g1, DistanceFunction distanceFunction)
  {
    if (distanceFunction == null)
      throw new NullPointerException("distanceFunction");

    DiscreteFrechetDistance dist = new DiscreteFrechetDistance(g0, g1, distanceFunction);
    return dist.distance();
  }

  private final Geometry g0;
  private final Geometry g1;
  private final DistanceFunction distanceFunction;

  /**
   * Creates an instance of this class using the provided geometries and
   * the provided {@link DistanceFunction} to compute distances between
   * {@link Coordinate}s.
   *
   * @param g0 a geometry
   * @param g1 a geometry
   * @param distanceFunction an object to perform distance calculations.
   */
  private DiscreteFrechetDistance(Geometry g0, Geometry g1, DistanceFunction distanceFunction) {
    this.g0 = g0;
    this.g1 = g1;
    this.distanceFunction = distanceFunction;
  }

  /**
   * Compute the {@code Discrete Fréchet Distance} between two geometries
   *
   * @return the Discrete Fréchet Distance
   */
  private double distance() {
    Coordinate[] coords0 = g0.getCoordinates();
    Coordinate[] coords1 = g1.getCoordinates();

    long[] diagonal = bresenhamLine(coords0.length, coords1.length);
    HashMap<Long, Double> distances = computeCoordinateDistances(coords0, coords1, diagonal);
    computeFrechet(distances, diagonal, coords0.length, coords1.length);
    long key = (long)(coords0.length - 1) << 32 | (coords1.length - 1);

    return distances.get(key);
  }

  /**
   * Compute the Fréchet Distance for the given distance matrix.
   *
   * @param distances a sparse matrix
   * @param diagonal an array of encoded row/col index values for the diagonal of the distance matrix
   * @param numPoints0 the number of {@code Coordinate}s in the 1st {@code Geometry}
   * @param numPoints1 the number of {@code Coordinate}s in the 2nd {@code Geometry}
   *
   */
  private static void computeFrechet(HashMap<Long, Double> distances,
                                     long[] diagonal, int numPoints0, int numPoints1) {
    for (long key : diagonal) {
      int i0 = (int)(key >> 32);
      int j0 = (int)key & 0x7FFFFFFF;

      for (int i = i0; i < numPoints0; i++) {
        key = (long)i << 32 | j0;
        if (distances.containsKey(key)) {
          double dist = getMinDistanceAtCorner(distances, i, j0);
          if (dist > distances.get(key))
            distances.put(key, dist);
          else
            break;
        }
      }
      for (int j = j0 + 1; j < numPoints1; j++) {
        key = (long)i0 << 32 | j;
        if (distances.containsKey(key)) {
          double dist = getMinDistanceAtCorner(distances, j, j0);
          if (dist > distances.get(key))
            distances.put(key, dist);
          else
            break;
        }
      }
    }
  }

  /**
   * Returns the minimum distance at the corner ({@code i, j}).
   *
   * @param matrix A sparse matrix
   * @param i the column index
   * @param j the row index
   * @return the minimum distance
   */
  private static double getMinDistanceAtCorner(HashMap<Long, Double> matrix, int i, int j) {
    if (i > 0 && j > 0) {
      double d0 = getDistance(matrix, i-1, j-1);
      double d1 = getDistance(matrix, i-1, j);
      double d2 = getDistance(matrix, i, j-1);
      return Math.min(Math.min(d0, d1), d2);
    }
    if (i == 0 && j == 0) {
      return matrix.get(0L);
    }
    if (i == 0) {
      return matrix.getOrDefault((long)j, Double.POSITIVE_INFINITY);
    }
    // j == 0
    return matrix.getOrDefault((long) i<< 32, Double.POSITIVE_INFINITY);
  }

  /**
   * Gets the computed distance between the ith-{@code Coordinate} of the 1st {@code Geometry}
   * and the jth-{@code Geometry} of the 2nd {@code Geometry}.
   *
   * @param matrix A sparse matrix
   * @param i the column index
   * @param j the row index
   * @return the distance computed for the given matrix index ({@code i, j}). If not computed,
   *  the result is {@link Double#POSITIVE_INFINITY}.
   */
  private static double getDistance(HashMap<Long, Double> matrix, int i, int j) {
    long key = (long)i << 32 | j;
    if (matrix.containsKey(key))
      return matrix.get(key);
    return Double.POSITIVE_INFINITY;
  }

  /**
   * Computes relevant distances between pairs of {@link Coordinate}s for the
   * computation of the {@code Discrete Fréchet Distance}.
   *
   * @param coords0 an array of {@code Coordinate}s.
   * @param coords1 an array of {@code Coordinate}s.
   * @param diagonal an array of encoded row/col index values for the diagonal of the distance matrix
   * @return A sparse matrix of the relevant {@code Coordinate} pair distances.
   */
  private HashMap<Long, Double> computeCoordinateDistances(Coordinate[] coords0, Coordinate[] coords1, long[] diagonal) {
    int numDiag = diagonal.length;
    double maxDistOnDiag = 0d;
    int imin = 0, jmin = 0;
    int numCoords0 = coords0.length;
    int numCoords1 = coords1.length;

    // First compute all the distances along the diagonal.
    // Record the maximum distance.
    HashMap<Long, Double> distances = new HashMap<>(numDiag);
    for (long l : diagonal) {
      int i0 = (int) (l >> 32);
      int j0 = (int) l & 0x7FFFFFFF;
      double diagDist = this.distanceFunction.distance(coords0[i0], coords1[j0]);
      if (diagDist > maxDistOnDiag) maxDistOnDiag = diagDist;
      distances.put(l, diagDist);
    }

    // Check for shorter distances along the diagonal
    for (int k = 0; k < numDiag - 1; k++) {
      // Decode index
      int i0 = (int)(diagonal[k] >> 32);
      int j0 = (int)diagonal[k] & 0x7FFFFFFF;

      // Get reference coordinates for col and row
      Coordinate coord0 = coords0[i0];
      Coordinate coord1 = coords1[j0];

      // Check for shorter distances in this row
      int i = i0 + 1;
      for (; i < numCoords0; i++) {
        long key = ((long)i << 32) | j0;
        if (!distances.containsKey(key)) {
          double dist = this.distanceFunction.distance(coords0[i], coord1);
          if (dist < maxDistOnDiag || i < imin)
            distances.put(key, dist);
          else
            break;
        }
        else
          break;
      }
      imin = i;

      // Check for shorter distances in this column
      int j = j0 + 1;
      for (; j < numCoords1; j++) {
        long key = ((long)i0 << 32) | j;
        if (!distances.containsKey(key)) {
          double dist = this.distanceFunction.distance(coord0, coords1[j]);
          if (dist < maxDistOnDiag || j < jmin)
            distances.put(key, dist);
          else
            break;
        }
        else
          break;
      }
      jmin = j;
    }

    return distances;
  }

  /**
   * Implementation of the <a href=https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm>
   *   Bresenham's line algorithm</a> for the diagonal of a {@code numCols x numRows} grid.
   *
   * @param numCols the number of columns
   * @param numRows the number of rows
   * @return an array of column and row indices bitwise-or combined.
   */
  private static long[] bresenhamLine(int numCols, int numRows) {
    int dim = Math.max(numCols, numRows);
    long[] pairs = new long[dim];

    int sx = 0 > numCols ? -1 : 1;
    int sy = 0 > numRows ? -1 : 1;
    int x = 0;
    int y = 0;

    int err;
    if (numCols > numRows) {
      err = numCols / 2;
      for (int i = 0; i < numCols; i++) {
        pairs[i] = ((long)x << 32) | y;
        err -= numRows;
        if (err < 0) {
          y += sy;
          err += numCols;
        }
        x += sx;
      }
    } else {
      err = numRows / 2;
      for (int i = 0; i < numRows; i++) {
        pairs[i] = ((long)x << 32) | y;
        err -= numCols;
        if (err < 0) {
          x += sx;
          err += numRows;
        }
        y += sy;
      }
    }
    return pairs;
  }

}
