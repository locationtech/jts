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

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Locale;

public class MatrixStorageImplTests extends TestCase {

  @Test
  public void testCsrMatrix()
  {
    MatrixStorage mat = new CsrMatrix(4, 6, 0d, 8);
    runOrderedTest(mat);
    mat = new CsrMatrix(4, 6, 0d, 8);
    runUnorderedTest(mat);
    System.out.println(mat);
  }
  @Test
  public void testHashMapMatrix()
  {
    MatrixStorage mat = new HashMapMatrix(4, 6, 0d);
    runOrderedTest(mat);
    mat = new HashMapMatrix(4, 6, 0d);
    runUnorderedTest(mat);
    System.out.println(mat);
  }
  @Test
  public void testRectMatrix()
  {
    MatrixStorage mat = new RectMatrix(4, 6, 0d);
    runOrderedTest(mat);
    mat = new RectMatrix(4, 6, 0d);
    runUnorderedTest(mat);
    System.out.println(mat);
  }

  private static void runOrderedTest(MatrixStorage mat) {
    mat.set(0, 0, 10);
    mat.set(0, 1, 20);
    mat.set(1, 1, 30);
    mat.set(1, 3, 40);
    mat.set(2, 2, 50);
    mat.set(2, 3, 60);
    mat.set(2, 4, 70);
    mat.set(3, 5, 80);

    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 0, 0, 10d, mat.get(0, 0)), 10d, mat.get(0, 0));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 0, 1, 20d, mat.get(0, 1)), 20d, mat.get(0, 1));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 1, 1, 30d, mat.get(1, 1)), 30d, mat.get(1, 1));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 1, 3, 40d, mat.get(1, 3)), 40d, mat.get(1, 3));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 2, 2, 50d, mat.get(2, 2)), 50d, mat.get(2, 2));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 2, 3, 60d, mat.get(2, 3)), 60d, mat.get(2, 3));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 2, 4, 70d, mat.get(2, 4)), 70d, mat.get(2, 4));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 3, 5, 80d, mat.get(3, 5)), 80d, mat.get(3, 5));

  }

  private static void runUnorderedTest(MatrixStorage mat) {
    mat.set(0, 0, 10);
    mat.set(3, 5, 80);
    mat.set(0, 1, 20);
    mat.set(2, 4, 70);
    mat.set(1, 1, 30);
    mat.set(2, 3, 60);
    mat.set(2, 2, 50);
    mat.set(1, 3, 40);

    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 0, 0, 10d, mat.get(0, 0)), 10d, mat.get(0, 0));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 0, 1, 20d, mat.get(0, 1)), 20d, mat.get(0, 1));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 1, 1, 30d, mat.get(1, 1)), 30d, mat.get(1, 1));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 1, 3, 40d, mat.get(1, 3)), 40d, mat.get(1, 3));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 2, 2, 50d, mat.get(2, 2)), 50d, mat.get(2, 2));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 2, 3, 60d, mat.get(2, 3)), 60d, mat.get(2, 3));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 2, 4, 70d, mat.get(2, 4)), 70d, mat.get(2, 4));
    assertEquals(String.format("%1$d -> %2$d = %4$f /= %3$f", 3, 5, 80d, mat.get(3, 5)), 80d, mat.get(3, 5));

  }

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
      for (int i = 0; i < this.NumRows; i++)
      {
        sb.append('[');
        for(int j = 0; j < this.NumCols; j++)
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
      return max * Math.max(max / 100, 5);
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
      int cHigh = this.ri[i + 1];
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
        for (int ii = this.numValues - 1; ii > vi; ii--)
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
