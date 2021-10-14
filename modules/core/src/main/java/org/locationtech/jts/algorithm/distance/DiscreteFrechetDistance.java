/*
 * Copyright (c) 2021 Felix Obermaier.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.distance;

import java.util.Arrays;
import java.util.HashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * The Fréchet distance is a measure of similarity between curves. Thus, it can
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
 * Its metric is better than the Hausdorff distance 
 * because it takes the directions of the curves into account. 
 * It is possible that two curves have a small Hausdorff but a large
 * Fréchet distance.
 * <p/>
 * This implementation is base on the following optimized Fréchet distance algorithm:
 * <pre>Thomas Devogele, Maxence Esnault, Laurent Etienne. Distance discrète de Fréchet optimisée. Spatial
 * Analysis and Geomatics (SAGEO), Nov 2016, Nice, France. hal-02110055</pre>
 * <p/>
 * Several matrix storage implementations are provided
 *
 * @see <a href="https://en.wikipedia.org/wiki/Fr%C3%A9chet_distance">Fréchet distance</a>
 * @see <a href="http://www.kr.tuwien.ac.at/staff/eiter/et-archive/cdtr9464.pdf">
 *   Computing Discrete Fréchet Distance</a>
 * @see <a href="https://hal.archives-ouvertes.fr/hal-02110055/document">Distance discrète de Fréchet optimisée</a>
 * @see <a href="https://towardsdatascience.com/fast-discrete-fr%C3%A9chet-distance-d6b422a8fb77">
 *   Fast Discrete Fréchet Distance</a>
 */
public class DiscreteFrechetDistance {

  /**
   * Computes the Discrete Fréchet Distance between two {@link Geometry}s
   * using a {@code Cartesian} distance computation function.
   *
   * @param g0 the 1st geometry
   * @param g1 the 2nd geometry
   * @return the cartesian distance between {#g0} and {#g1}
   */
  public static double distance(Geometry g0, Geometry g1) {

    DiscreteFrechetDistance dist = new DiscreteFrechetDistance(g0, g1);
    return dist.distance();
  }

  private final Geometry g0;
  private final Geometry g1;
  private PointPairDistance ptDist;

  /**
   * Creates an instance of this class using the provided geometries.
   *
   * @param g0 a geometry
   * @param g1 a geometry
   */
  public DiscreteFrechetDistance(Geometry g0, Geometry g1) {
    this.g0 = g0;
    this.g1 = g1;
  }

  /**
   * Computes the {@code Discrete Fréchet Distance} between the input geometries
   *
   * @return the Discrete Fréchet Distance
   */
  private double distance() {
    Coordinate[] coords0 = g0.getCoordinates();
    Coordinate[] coords1 = g1.getCoordinates();

    MatrixStorage distances = createMatrixStorage(coords0.length, coords1.length);
    int[] diagonal = bresenhamDiagonal(coords0.length, coords1.length);

    HashMap<Double, int[]> distanceToPair = new HashMap<>();
    computeCoordinateDistances(coords0, coords1, diagonal, distances, distanceToPair);
    ptDist = computeFrechet(coords0, coords1, diagonal, distances, distanceToPair);

    return ptDist.getDistance();
  }

  /**
   * Creates a matrix to store the computed distances.
   * 
   * @param rows the number of rows
   * @param cols the number of columns
   * @return a matrix storage
   */
  private static MatrixStorage createMatrixStorage(int rows, int cols) {

    int max = Math.max(rows, cols);
    // NOTE: these constraints need to be verified
    if (max < 1024)
      return new RectMatrix(rows, cols, Double.POSITIVE_INFINITY);

    return new CsrMatrix(rows, cols, Double.POSITIVE_INFINITY);
  }

  /**
   * Gets the pair of {@link Coordinate}s at which the distance is obtained.
   *
   * @return the pair of Coordinates at which the distance is obtained
   */
  public Coordinate[] getCoordinates() {
    if (ptDist == null)
      distance();

    return ptDist.getCoordinates();
  }

  /**
   * Computes the Fréchet Distance for the given distance matrix.
   *
   * @param coords0 an array of {@code Coordinate}s.
   * @param coords1 an array of {@code Coordinate}s.
   * @param diagonal an array of alternating col/row index values for the diagonal of the distance matrix
   * @param distances the distance matrix
   * @param distanceToPair a lookup for coordinate pairs based on a distance
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
    if (index == null) {
      throw new IllegalStateException("Pair of points not recorded for computed distance");
    }
    result.initialize(coords0[index[0]], coords1[index[1]], distance);
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
      double d0 = matrix.get(i - 1, j - 1);
      double d1 = matrix.get(i - 1, j);
      double d2 = matrix.get(i, j - 1);
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
   * Computes relevant distances between pairs of {@link Coordinate}s for the
   * computation of the {@code Discrete Fréchet Distance}.
   *
   * @param coords0 an array of {@code Coordinate}s.
   * @param coords1 an array of {@code Coordinate}s.
   * @param diagonal an array of alternating col/row index values for the diagonal of the distance matrix
   * @param distances the distance matrix
   * @param distanceToPair a lookup for coordinate pairs based on a distance
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
      distanceToPair.putIfAbsent(diagDist, new int[] {i0, j0});
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
            distanceToPair.putIfAbsent(dist, new int[] {i, j0});
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
            distanceToPair.putIfAbsent(dist, new int[] {i0, j});
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
   * Computes the indices for the diagonal of a {@code numCols x numRows} grid
   * using the <a href=https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm>
   * Bresenham line algorithm</a>.
   *
   * @param numCols the number of columns
   * @param numRows the number of rows
   * @return a packed array of column and row indices
   */
  static int[] bresenhamDiagonal(int numCols, int numRows) {
    int dim = Math.max(numCols, numRows);
    int[] diagXY = new int[2 * dim];

    int dx = numCols - 1;
    int dy = numRows - 1;
    int err;
    int i = 0;
    if (numCols > numRows) {
      int y = 0;
      err = 2 * dy - dx;
      for (int x = 0; x < numCols; x++) {
        diagXY[i++] = x;
        diagXY[i++] = y;
        if (err > 0) {
          y += 1;
          err -= 2 * dx;
        }
        err += 2 * dy;
      }
    } else {
      int x = 0;
      err = 2 * dx - dy;
      for (int y = 0; y < numRows; y++) {
        diagXY[i++] = x;
        diagXY[i++] = y;
        if (err > 0) {
          x += 1;
          err -= 2 * dy;
        }
        err += 2 * dx;
      }
    }
    return diagXY;
  }

  /**
   * Abstract base class for storing 2d matrix data
   */
  abstract static class MatrixStorage {

    protected final int numRows;
    protected final int numCols;
    protected final double defaultValue;

    /**
     * Creates an instance of this class
     * @param numRows the number of rows
     * @param numCols the number of columns
     * @param defaultValue A default value
     */
    public MatrixStorage(int numRows, int numCols, double defaultValue)
    {
      this.numRows = numRows;
      this.numCols = numCols;
      this.defaultValue = defaultValue;
    }

    /**
     * Gets the matrix value at i, j
     * @param i the row index
     * @param j the column index
     * @return The matrix value at i, j
     */
    public abstract double get(int i, int j);

    /**
     * Sets the matrix value at i, j
     * @param i the row index
     * @param j the column index
     * @param value The matrix value to set at i, j
     */
    public abstract void set(int i, int j, double value);

    /**
     * Gets a flag indicating if the matrix has a set value, e.g. one that is different
     * than {@link MatrixStorage#defaultValue}.
     * @param i the row index
     * @param j the column index
     * @return a flag indicating if the matrix has a set value
     */
    public abstract boolean isValueSet(int i, int j);

    /* For debugging purposes only
    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("[");
      for (int i = 0; i < this.numRows; i++)
      {
        sb.append('[');
        for(int j = 0; j < this.numCols; j++)
        {
          if (j > 0)
            sb.append(", ");
          sb.append(String.format(java.util.Locale.ROOT, "%8.4f", get(i, j)));
        }
        sb.append(']');
        if (i < this.numRows - 1) sb.append(",\n");
      }
      sb.append(']');
      return sb.toString();
    }
     */
  }

  /**
   * Straight forward implementation of a rectangular matrix
   */
  final static class RectMatrix extends MatrixStorage {

    private final double[] matrix;

    /**
     * Creates an instance of this matrix using the given number of rows and columns.
     * A default value can be specified
     *
     * @param numRows the number of rows
     * @param numCols the number of columns
     * @param defaultValue A default value
     */
    public RectMatrix(int numRows, int numCols, double defaultValue)
    {
      super(numRows, numCols, defaultValue);
      this.matrix = new double[numRows * numCols];
      Arrays.fill(this.matrix, defaultValue);
    }

    public double get(int i, int j) { return this.matrix[i * numCols + j]; }

    public void set(int i, int j, double value) {
      this.matrix[i * numCols + j] = value;
    }

    public boolean isValueSet(int i, int j) {
      return Double.doubleToLongBits(get(i, j)) != Double.doubleToLongBits(this.defaultValue);
    }
  }

  /**
   * A matrix implementation that adheres to the
   * <a href="https://en.wikipedia.org/wiki/Sparse_matrix#Compressed_sparse_row_(CSR,_CRS_or_Yale_format)">
   *   Compressed sparse row format</a>.<br/>
   * Note: Unfortunately not as fast as expected.
   */
  final static class CsrMatrix extends MatrixStorage {

    private double[] v;
    private final int[] ri;
    private int[] ci;

    public CsrMatrix(int numRows, int numCols, double defaultValue) {
      this(numRows, numCols, defaultValue, expectedValuesHeuristic(numRows, numCols));
    }
    public CsrMatrix(int numRows, int numCols, double defaultValue, int expectedValues) {
      super(numRows, numCols, defaultValue);
      this.v = new double[expectedValues];
      this.ci = new int[expectedValues];
      this.ri = new int[numRows + 1];
    }

    /**
     * Computes an initial value for the number of expected values
     * @param numRows the number of rows
     * @param numCols the number of columns
     * @return the expected number of values in the sparse matrix
     */
    private static int expectedValuesHeuristic(int numRows, int numCols) {
      int max = Math.max(numRows, numCols);
      return max * max / 10;
    }

    private int indexOf(int i, int j) {
      int cLow = this.ri[i];
      int cHigh = this.ri[i+1];
      if (cHigh <= cLow) return ~cLow;

      return Arrays.binarySearch(this.ci, cLow, cHigh, j);
    }

    @Override
    public double get(int i, int j) {

      // get the index in the vector
      int vi = indexOf(i, j);

      // if the vector index is negative, return default value
      if (vi < 0)
        return defaultValue;

      return this.v[vi];
    }

    @Override
    public void set(int i, int j, double value) {

      // get the index in the vector
      int vi = indexOf(i, j);

      // do we already have a value?
      if (vi < 0)
      {
        // no, we don't, we need to ensure space!
        ensureCapacity(this.ri[this.numRows] + 1);

        // update row indices
        for (int ii = i + 1; ii <= this.numRows; ii++)
          ri[ii] += 1;

        // move and update column indices, move values
        vi = ~vi;
        for (int ii = this.ri[this.numRows]; ii > vi; ii--)
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

    @Override
    public boolean isValueSet(int i, int j) {
      return indexOf(i, j) >= 0;
    }


    /**
     * Ensures that the column index vector (ci) and value vector (v) are sufficiently large.
     * @param required the number of items to store in the matrix
     */
    private void ensureCapacity(int required) {
      if (required < this.v.length)
        return;

      int increment = Math.max(this.numRows, this.numCols);
      this.v = Arrays.copyOf(this.v, this.v.length + increment);
      this.ci = Arrays.copyOf(this.ci, this.v.length + increment);
    }
  }

  /**
   * A sparse matrix based on java's {@link HashMap}.
   */
  final static class HashMapMatrix extends MatrixStorage {

    private final HashMap<Long, Double> matrix;

    /**
     * Creates an instance of this class
     * @param numRows the number of rows
     * @param numCols the number of columns
     * @param defaultValue a default value
     */
    public HashMapMatrix(int numRows, int numCols, double defaultValue) {
      super(numRows, numCols, defaultValue);
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
