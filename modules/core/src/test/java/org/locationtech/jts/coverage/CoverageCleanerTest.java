/*
 * Copyright (c) 2025 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import org.locationtech.jts.coverage.CoverageCleaner;
import org.locationtech.jts.coverage.CoverageValidator;
import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class CoverageCleanerTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(CoverageCleanerTest.class);
  }
  
  public CoverageCleanerTest(String name) {
    super(name);
  }
  
  public void testSingleNearMatch() {
    checkCleaner(readArray(
        "POLYGON ((1 9, 9 9, 9 4.99, 1 5, 1 9))",
        "POLYGON ((1 1, 1 5, 9 5, 9 1, 1 1))"),
        0.1);
  }

  public void testManyNearMatches() {
    checkCleaner(readArray(
        "POLYGON ((1 9, 9 9, 9 5, 8 5, 7 5, 4 5.5, 3 5, 2 5, 1 5, 1 9))",
        "POLYGON ((1 1, 1 4.99, 2 5.01, 3.01 4.989, 5 3, 6.99 4.99, 7.98 4.98, 9 5, 9 1, 1 1))"),
        0.1);
  }

  // Tests that if interior point lies in a spike that is snapped away, polygon is still in result
  public void testPolygonSnappedPreserved() {
    checkCleaner(readArray(
        "POLYGON ((90 0, 10 0, 89.99 30, 90 100, 90 0))"),
        0.1,
        readArray(
            "POLYGON ((90 0, 10 0, 89.99 30, 90 0))"));
  }
  
  // Tests that if interior point lies in a spike that is snapped away, polygon is still in result
  public void testPolygonsSnappedPreserved() {
    checkCleaner(readArray(
        "POLYGON ((0 0, 0 2, 5 2, 5 8, 5.01 0, 0 0))",
        "POLYGON ((0 8, 5 8, 5 2, 0 2, 0 8))"
        ),
        0.02,
        readArray(
            "POLYGON ((0 0, 0 2, 5 2, 5.01 0, 0 0))",
            "POLYGON ((0 8, 5 8, 5 2, 0 2, 0 8))"
            ));
  }
  
  //TODO: add test with MultiPolygon that snaps together (so needs merging)
  
  private void checkCleaner(Geometry[] cov, double tolerance) {
    Geometry[] covClean = CoverageCleaner.clean(cov, tolerance);
    checkValidCoverage(covClean, tolerance);
  }

  private void checkCleaner(Geometry[] cov, double tolerance, Geometry[] expected) {
    Geometry[] actual = CoverageCleaner.clean(cov, tolerance);
    checkValidCoverage(actual, tolerance);
    checkEqual(expected, actual);
  }

  public void checkCleaner(String wkt, double tolerance) {
    Geometry[] cov = readArray(wkt);
    checkCleaner(cov, tolerance);
  }

  private void checkValidCoverage(Geometry[] coverage, double tolerance) {
    for (Geometry geom : coverage) {
      assertTrue(geom.isValid());
    }
    boolean isValid = CoverageValidator.isValid(coverage, tolerance);
    assertTrue(isValid);  
  }
}
