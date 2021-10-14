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

import junit.framework.TestCase;

import org.locationtech.jts.algorithm.distance.DiscreteFrechetDistance.MatrixStorage;
import org.locationtech.jts.algorithm.distance.DiscreteFrechetDistance.HashMapMatrix;
import org.locationtech.jts.algorithm.distance.DiscreteFrechetDistance.CsrMatrix;
import org.locationtech.jts.algorithm.distance.DiscreteFrechetDistance.RectMatrix;

import org.junit.Test;

public class MatrixStorageTest extends TestCase {

  @Test
  public void testCsrMatrix()
  {
    MatrixStorage mat = new CsrMatrix(4, 6, 0d, 8);
    runOrderedTest(mat);
    mat = new CsrMatrix(4, 6, 0d, 8);
    runUnorderedTest(mat);
  }
  @Test
  public void testHashMapMatrix()
  {
    MatrixStorage mat = new HashMapMatrix(4, 6, 0d);
    runOrderedTest(mat);
    mat = new HashMapMatrix(4, 6, 0d);
    runUnorderedTest(mat);
  }
  @Test
  public void testRectMatrix()
  {
    MatrixStorage mat = new RectMatrix(4, 6, 0d);
    runOrderedTest(mat);
    mat = new RectMatrix(4, 6, 0d);
    runUnorderedTest(mat);
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
}
