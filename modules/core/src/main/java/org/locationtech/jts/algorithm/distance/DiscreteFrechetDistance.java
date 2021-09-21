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

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Locale;

/**
 * The Fréchet distance is a measure of similarity between curves. Thus it can
 * be used like the Hausdorff distance.
 * <p/>
 * An analogy for the Fréchet distance taken from
 * <a href="http://www.kr.tuwien.ac.at/staff/eiter/et-archive/cdtr9464.pdf">
 *   Computing Discrete Fréchet Distance</a>
 * <pre>
 * A man is walking a dog on a leash: the man can move
 * on one curve, the dog on the other; both may vary their
 * speed, but backtracking is not allowed.
 * </pre>
 * <p/>
 * Its metric is better than Hausdorff's because it takes the flow of the curves
 * into account. It is possible that two curves have a small Hausdorff but a large
 * Fréchet distance.
 * <p/>
 * This implementation attempts to compute only relevant coordinate distances for
 * performance and uses a HashMap as sparse matrix to reduce memory consumption.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Fr%C3%A9chet_distance">Fréchet distance</a>
 * @see <a href="http://www.kr.tuwien.ac.at/staff/eiter/et-archive/cdtr9464.pdf">
 *   Computing Discrete Fréchet Distance</a>
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

    DiscreteFrechetDistance dist = new DiscreteFrechetDistance(g0, g1, false);
    return dist.distance();
  }
  /**
   * Computes the Discrete Fréchet Distance between two {@link Geometry}s
   * using a {@code cartesian} distance computation function.
   *
   * @param g0 the 1st geometry
   * @param g1 the 2nd geometry
   * @return the cartesian distance between {#g0} and {#g1}
   */
  public static double distance(Geometry g0, Geometry g1, boolean keepCoordinatePair) {

    DiscreteFrechetDistance dist = new DiscreteFrechetDistance(g0, g1, keepCoordinatePair);
    return dist.distance();
  }
  private final Geometry g0;
  private final Geometry g1;
  private final boolean keepCoordinatePair;
  private PointPairDistance ptDist;

  /**
   * Creates an instance of this class using the provided geometries.
   *
   * @param g0 a geometry
   * @param g1 a geometry
   * @param keepCoordinatePair
   */
  private DiscreteFrechetDistance(Geometry g0, Geometry g1, boolean keepCoordinatePair) {
    this.g0 = g0;
    this.g1 = g1;
    this.keepCoordinatePair = keepCoordinatePair;
  }

  /**
   * Compute the {@code Discrete Fréchet Distance} between two geometries
   *
   * @return the Discrete Fréchet Distance
   */
  private double distance() {
    Coordinate[] coords0 = g0.getCoordinates();
    Coordinate[] coords1 = g1.getCoordinates();

    MatrixStorage distances = createMatrixStorage(coords0.length, coords1.length);
    int[] diagonal = bresenhamLine(coords0.length, coords1.length);

    HashMap<Double, int[]> distanceToPair = new HashMap<>();
    computeCoordinateDistances(coords0, coords1, diagonal, distances, distanceToPair);
    //System.out.println(distances);
    //System.out.println();
    ptDist = computeFrechet(coords0, coords1, diagonal, distances, distanceToPair);

    return ptDist.getDistance();
  }

  private MatrixStorage createMatrixStorage(int rows, int cols) {
    int max = Math.max(rows, cols);
    if (max < 24)
      return new HashMapMatrix(rows, cols, Double.POSITIVE_INFINITY);
    return new CsrMatrix(rows, cols, Double.POSITIVE_INFINITY);
    //return new RectMatrix(rows, cols, Double.POSITIVE_INFINITY);
  }

  public Coordinate[] getCoordinates() {
    if (ptDist == null)
      distance();

    return ptDist.getCoordinates();
  }

  /**
   * Compute the Fréchet Distance for the given distance matrix.
   *
   * @param coords0 an array of {@code Coordinate}s.
   * @param coords1 an array of {@code Coordinate}s.
   * @param distances a sparse distance matrix
   * @param distanceToPair a lookup for distance and a coordinate pair
   * @param diagonal an array of alternating row/col index values for the diagonal of the distance matrix
   *
   */
  private static PointPairDistance computeFrechet(Coordinate[] coords0, Coordinate[] coords1, int[] diagonal,
                                                  MatrixStorage distances, HashMap<Double, int[]> distanceToPair) {
    for (int d = 0; d < diagonal.length; d += 2) {
      int i0 = diagonal[d];
      int j0 = diagonal[d + 1];

      for (int i = i0; i < coords0.length; i++) {
        if (distances.isValueSet(i, j0)) {
          double dist = getMinDistanceAtCorner(distances, i, j0);
          if (dist > distances.get(i, j0))
            distances.set(i, j0, dist);
        }
        else {
          break;
        }
      }
      for (int j = j0 + 1; j < coords1.length; j++) {
        if (distances.isValueSet(i0, j)) {
          double dist = getMinDistanceAtCorner(distances, i0, j);
          if (dist > distances.get(i0, j))
            distances.set(i0, j, dist);
        }
        else {
          break;
        }
      }
    }

    PointPairDistance result = new PointPairDistance();
    double distance = distances.get(coords0.length-1, coords1.length - 1);
    int[] index = distanceToPair.get(distance);
    if (index != null) {
      Coordinate c0 = coords0[index[0]];
      Coordinate c1 = coords1[index[1]];
      result.initialize(c0, c1, distance);
    }
    return result;
  }

  /**
   * Returns the minimum distance at the corner ({@code i, j}).
   *
   * @param matrix A sparse matrix
   * @param i the column index
   * @param j the row index
   * @return the minimum distance
   */
  private static double getMinDistanceAtCorner(MatrixStorage matrix, int i, int j) {
    if (i > 0 && j > 0) {
      double d0 = getDistance(matrix, i - 1, j - 1);
      double d1 = getDistance(matrix, i - 1, j);
      double d2 = getDistance(matrix, i, j - 1);
      return Math.min(Math.min(d0, d1), d2);
    }
    if (i == 0 && j == 0)
      return matrix.get(0, 0);

    if (i == 0)
      return matrix.get(0, j - 1);

    // j == 0
    return matrix.get(i - 1, 0);
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
  private static double getDistance(MatrixStorage matrix, int i, int j) {
    return matrix.get(i, j);
  }

  /**
   * Computes relevant distances between pairs of {@link Coordinate}s for the
   * computation of the {@code Discrete Fréchet Distance}.
   *
   * @param coords0 an array of {@code Coordinate}s.
   * @param coords1 an array of {@code Coordinate}s.
   * @param diagonal an array of encoded row/col index values for the diagonal of the distance matrix
   * @param distances the sparse distance matrix
   * @param distanceToPair a lookup for coordinate pairs based on a distance.
   */
  private void computeCoordinateDistances(Coordinate[] coords0, Coordinate[] coords1, int[] diagonal,
                                          MatrixStorage distances, HashMap<Double, int[]> distanceToPair) {
    int numDiag = diagonal.length;
    double maxDistOnDiag = 0d;
    int imin = 0, jmin = 0;
    int numCoords0 = coords0.length;
    int numCoords1 = coords1.length;

    // First compute all the distances along the diagonal.
    // Record the maximum distance.

    for (int k = 0; k < numDiag; k += 2) {
      int i0 = diagonal[k];
      int j0 = diagonal[k + 1];
      double diagDist = coords0[i0].distance(coords1[j0]);
      if (diagDist > maxDistOnDiag) maxDistOnDiag = diagDist;
      distances.set(i0, j0, diagDist);
      if (this.keepCoordinatePair) distanceToPair.putIfAbsent(diagDist, new int[] {i0, j0});
    }

    // Check for distances shorter than maxDistOnDiag along the diagonal
    for (int k = 0; k < numDiag - 2; k += 2) {
      // Decode index
      int i0 = diagonal[k];
      int j0 = diagonal[k + 1];

      // Get reference coordinates for col and row
      Coordinate coord0 = coords0[i0];
      Coordinate coord1 = coords1[j0];

      // Check for shorter distances in this row
      int i = i0 + 1;
      for (; i < numCoords0; i++) {
        if (!distances.isValueSet(i, j0)) {
          double dist = coords0[i].distance(coord1);
          if (dist < maxDistOnDiag || i < imin)          {
            distances.set(i, j0, dist);
            if (this.keepCoordinatePair) distanceToPair.putIfAbsent(dist, new int[] {i, j0});
          }
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
        if (!distances.isValueSet(i0, j)) {
          double dist = coord0.distance(coords1[j]);
          if (dist < maxDistOnDiag || j < jmin)
          {
            distances.set(i0, j, dist);
            if (this.keepCoordinatePair) distanceToPair.putIfAbsent(dist, new int[] {i0, j});
          }
          else
            break;
        }
        else
          break;
      }
      jmin = j;
    }

    //System.out.println(distances.toString());
  }

  /**
   * Implementation of the <a href=https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm>
   *   Bresenham's line algorithm</a> for the diagonal of a {@code numCols x numRows} grid.
   *
   * @param numCols the number of columns
   * @param numRows the number of rows
   * @return an array of column and row indices bitwise-or combined.
   */
  private static int[] bresenhamLine(int numCols, int numRows) {
    int dim = Math.max(numCols, numRows);
    int[] pairs = new int[2 * dim];

    int sx = 0 > numCols ? -1 : 1;
    int sy = 0 > numRows ? -1 : 1;
    int x = 0;
    int y = 0;

    int err;
    if (numCols > numRows) {
      err = numCols / 2;
      for (int i = 0, j = 0; i < numCols; i++) {
        pairs[j++] = x;
        pairs[j++] = y;
        err -= numRows;
        if (err < 0) {
          y += sy;
          err += numCols;
        }
        x += sx;
      }
    } else {
      err = numRows / 2;
      for (int i = 0, j = 0; i < numRows; i++) {
        pairs[j++] = x;
        pairs[j++] = y;
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

  /*
  // For debugging purposes only!
  private static String toString(int numRows, int numCols,
                                 Map<Long, Double> sparse) {

    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < numRows; i++)
    {
      sb.append('[');
      for(int j = 0; j < numCols; j++)
      {
        if (j > 0)
          sb.append(", ");
        sb.append(String.format("%8.4f", getDistance(sparse, i, j)));
      }
      sb.append(']');
      if (i < numRows - 1) sb.append(",\n");
    }
    sb.append(']');
    return sb.toString();

  }
   */

  /**
   * Abstract base class for storing 2d matrix data
   */
  private abstract static class MatrixStorage {

    protected final int NumRows;
    protected final int NumCols;

    public MatrixStorage(int numRows, int numCols)
    {
      this.NumRows = numRows;
      this.NumCols = numCols;
    }

    public abstract double get(int i, int j);
    public abstract void set(int i, int j, double value);
    public abstract boolean isValueSet(int i, int j);

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("[");
      for (int i = 0; i < Math.min(10, this.NumRows); i++)
      {
        sb.append('[');
        for(int j = 0; j < Math.min(10, this.NumCols); j++)
        {
          if (j > 0)
            sb.append(", ");
          sb.append(String.format(Locale.ROOT, "%8.4f", get(i, j)));
        }
        sb.append(']');
        if (i < this.NumRows - 1) sb.append(",\n");
      }
      sb.append(']');
      return sb.toString();    }

    //abstract boolean contains(int i, int j);
  }

  private static class RectMatrix extends MatrixStorage {

    private final double[][] matrix;
    private final double defaultValue;
    private final BitSet matrixEntrySet;

    public RectMatrix(int numRows, int numCols, double defaultValue)
    {
      super(numRows, numCols);
      this.matrix = new double[numRows][];
      for (int i = 0; i < numRows; i++) {
        this.matrix[i] = new double[numCols];
        Arrays.fill(this.matrix[i], defaultValue);
      }
      this.defaultValue = defaultValue;
      this.matrixEntrySet = new BitSet(numRows * numCols);
    }

    public double get(int i, int j) { return this.matrix[i][j]; }

    public void set(int i, int j, double value) {
      this.matrix[i][j] = value;
      this.matrixEntrySet.set(i * this.NumCols + j, value != defaultValue);
    }

    public boolean isValueSet(int i, int j) {
      return this.matrixEntrySet.get(i * this.NumCols + j);
    }
  }

  private static class CsrMatrix extends MatrixStorage {

    private final double defaultValue;

    private double[] v;
    private int[] ri, ci;
    private int numValues;

    public CsrMatrix(int numRows, int numCols, double defaultValue) {
      this(numRows, numCols, defaultValue, expectedValuesHeuristic(numRows, numCols));
    }
    public CsrMatrix(int numRows, int numCols, double defaultValue, int expectedValues) {
      super(numRows, numCols);
      this.v = new double[expectedValues];
      this.ci = new int[expectedValues];
      this.ri = new int[numRows + 1];
      this.defaultValue = defaultValue;
      this.numValues = 0;
    }

    private static int expectedValuesHeuristic(int numRows, int numCols) {
      int max = Math.max(numRows, numCols);
      return max * max / 10;
    }

    public double get(int i, int j) {

      // get the index in the vector
      int vi = indexOf(i, j);

      // if the vector index is negative, return default value
      if (vi < 0)
        return defaultValue;

      return this.v[vi];
    }

    private int indexOf(int i, int j) {
      int cLow = this.ri[i];
      int cHigh = this.ri[i+1];
      if (cHigh <= cLow) return ~cLow;

      return Arrays.binarySearch(this.ci, cLow, cHigh, j);
    }

    public void set(int i, int j, double value) {

      // get the index in the vector
      int vi = indexOf(i, j);

      // do we already have a value?
      if (vi < 0)
      {
        // no, we don't, we need to ensure space!
        ensureCapacity(numValues + 1);

        // update row indices
        for (int ii = i + 1; ii <= this.NumRows; ii++)
          ri[ii] += 1;

        // increment number of values
        numValues++;

        // move and update column indices, move values
        vi = ~vi;
        for (int ii = this.numValues; ii > vi; ii--)
        {
          this.ci[ii] = this.ci[ii - 1];
          this.v[ii] = this.v[ii - 1];
        }

        // insert column index
        ci[vi] = j;
      }

      // set the new value
      v[vi] = value;
    }

    public boolean isValueSet(int i, int j) {
      return indexOf(i, j) >= 0;
    }


    private static final int INCREMENT_STEP = 16;

    private void ensureCapacity(int required) {
      if (required < this.v.length)
        return;

      this.v = Arrays.copyOf(this.v, this.v.length + INCREMENT_STEP);
      this.ci = Arrays.copyOf(this.ci, this.v.length + INCREMENT_STEP);
    }

    //public double
  }

  private class HashMapMatrix extends MatrixStorage {

    private final HashMap<Long, Double> matrix;
    private final double defaultValue;

    public HashMapMatrix(int numRows, int numCols, double defaultValue) {
      super(numRows, numCols);
      this.defaultValue = defaultValue;
      this.matrix = new HashMap<>();
    }


    public double get(int i, int j) {
      long key = (long)i << 32 | j;
      return matrix.getOrDefault(key, this.defaultValue);
    }

    public void set(int i, int j, double value) {
      long key = (long)i << 32 | j;
      matrix.put(key, value);
    }

    public boolean isValueSet(int i, int j) {
      long key = (long)i << 32 | j;
      return matrix.containsKey(key);
    }
  }
}
