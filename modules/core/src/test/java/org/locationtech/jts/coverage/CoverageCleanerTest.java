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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.PolygonalExtracter;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class CoverageCleanerTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(CoverageCleanerTest.class);
  }
  
  public CoverageCleanerTest(String name) {
    super(name);
  }
  
  public void testCoverageWithEmpty() {
    checkClean(
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 4, 1 4, 1 9)), POLYGON EMPTY, POLYGON ((2 1, 2 5, 8 5, 8 1, 2 1)))",
        "GEOMETRYCOLLECTION (POLYGON ((1 4, 1 9, 9 9, 9 4, 8 4, 2 4, 1 4)), POLYGON EMPTY, POLYGON ((8 1, 2 1, 2 4, 8 4, 8 1)))");
  }

  public void testSingleNearMatch() {
    checkCleanSnap(readArray(
        "POLYGON ((1 9, 9 9, 9 4.99, 1 5, 1 9))",
        "POLYGON ((1 1, 1 5, 9 5, 9 1, 1 1))"),
        0.1);
  }

  public void testManyNearMatches() {
    checkCleanSnap(readArray(
        "POLYGON ((1 9, 9 9, 9 5, 8 5, 7 5, 4 5.5, 3 5, 2 5, 1 5, 1 9))",
        "POLYGON ((1 1, 1 4.99, 2 5.01, 3.01 4.989, 5 3, 6.99 4.99, 7.98 4.98, 9 5, 9 1, 1 1))"),
        0.1);
  }

  // Tests that if interior point lies in a spike that is snapped away, polygon is still in result
  public void testPolygonSnappedPreserved() {
    checkCleanSnap(readArray(
        "POLYGON ((90 0, 10 0, 89.99 30, 90 100, 90 0))"),
        0.1,
        readArray(
            "POLYGON ((90 0, 10 0, 89.99 30, 90 0))"));
  }
  
  // Tests that if interior point lies in a spike that is snapped away, polygon is still in result
  public void testPolygonsSnappedPreserved() {
    checkCleanSnap(readArray(
        "POLYGON ((0 0, 0 2, 5 2, 5 8, 5.01 0, 0 0))",
        "POLYGON ((0 8, 5 8, 5 2, 0 2, 0 8))"
        ),
        0.02,
        readArray(
            "POLYGON ((0 0, 0 2, 5 2, 5.01 0, 0 0))",
            "POLYGON ((0 8, 5 8, 5 2, 0 2, 0 8))"
            ));
  }  
  
  // Tests that a collapsed polygon due to snapping is returned as EMPTY
  public void testPolygonsSnappedCollapse() {
    checkCleanSnap(readArray(
        "POLYGON ((1 1, 1 9, 6 5, 9 1, 1 1))",
        "POLYGON ((9 1, 6 5.1, 1 9, 9 9, 9 1))",
        "POLYGON ((9 1, 6 5, 1 9, 6 5.1, 9 1))"
        ),
        1,
        readArray(
            "POLYGON ((6 5, 9 1, 1 1, 1 9, 6 5))",
            "POLYGON ((9 9, 9 1, 6 5, 1 9, 9 9))",
            "POLYGON EMPTY"
            ));
  }
  
  public void testMergeGapToLongestBorder() {
    checkCleanGapWidth("GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 5, 1 5, 1 9)), POLYGON ((5 1, 5 5, 1 5, 5 1)), POLYGON ((5 1, 5.1 5, 9 5, 5 1)))",
        1,
        "GEOMETRYCOLLECTION (POLYGON ((5.1 5, 5 5, 1 5, 1 9, 9 9, 9 5, 5.1 5)), POLYGON ((5 1, 1 5, 5 5, 5 1)), POLYGON ((5 1, 5 5, 5.1 5, 9 5, 5 1)))"
        );
  }

  String covWithGaps = "GEOMETRYCOLLECTION (POLYGON ((1 3, 9 3, 9 1, 1 1, 1 3)), POLYGON ((1 3, 1 9, 4 9, 4 3, 3 4, 1 3)), POLYGON ((4 9, 7 9, 7 3, 6 5, 5 5, 4 3, 4 9)), POLYGON ((7 9, 9 9, 9 3, 8 3.1, 7 3, 7 9)))";

  public void testMergeGapWidth_0() {
    checkCleanGapWidth(covWithGaps,
        0,
        "GEOMETRYCOLLECTION (POLYGON ((9 3, 9 1, 1 1, 1 3, 4 3, 7 3, 9 3)), POLYGON ((1 9, 4 9, 4 3, 3 4, 1 3, 1 9)), POLYGON ((6 5, 5 5, 4 3, 4 9, 7 9, 7 3, 6 5)), POLYGON ((7 9, 9 9, 9 3, 8 3.1, 7 3, 7 9)))"
        );
  }

  public void testMergeGapWidth_1() {
    checkCleanGapWidth(covWithGaps,
        1,
        "GEOMETRYCOLLECTION (POLYGON ((7 3, 9 3, 9 1, 1 1, 1 3, 4 3, 7 3)), POLYGON ((1 9, 4 9, 4 3, 1 3, 1 9)), POLYGON ((7 3, 6 5, 5 5, 4 3, 4 9, 7 9, 7 3)), POLYGON ((7 9, 9 9, 9 3, 7 3, 7 9)))"
        );
  }

  public void testMergeGapWidth_2() {
    checkCleanGapWidth(covWithGaps,
        2,
        "GEOMETRYCOLLECTION (POLYGON ((9 3, 9 1, 1 1, 1 3, 4 3, 7 3, 9 3)), POLYGON ((1 9, 4 9, 4 3, 1 3, 1 9)), POLYGON ((7 3, 4 3, 4 9, 7 9, 7 3)), POLYGON ((9 9, 9 3, 7 3, 7 9, 9 9)))"
        );
  }

  String covWithOverlap = "GEOMETRYCOLLECTION (POLYGON ((1 3, 5 3, 4 1, 1 1, 1 3)), POLYGON ((1 3, 1 9, 4 9, 4 3, 3 1.9, 1 3)))";
  
  public void testMergeOverlapMinArea() {
    checkCleanOverlapMerge(covWithOverlap,
        CoverageCleaner.MERGE_MIN_AREA,
        "GEOMETRYCOLLECTION (POLYGON ((5 3, 4 1, 1 1, 1 3, 4 3, 5 3)), POLYGON ((1 9, 4 9, 4 3, 1 3, 1 9)))"
        );
  }

  public void testMergeOverlapMaxArea() {
    checkCleanOverlapMerge(covWithOverlap,
        CoverageCleaner.MERGE_MAX_AREA,
        "GEOMETRYCOLLECTION (POLYGON ((1 1, 1 3, 3 1.9, 4 3, 5 3, 4 1, 1 1)), POLYGON ((1 3, 1 9, 4 9, 4 3, 3 1.9, 1 3)))"
        );
  }

  public void testMergeOverlapMinId() {
    checkCleanOverlapMerge(covWithOverlap,
        CoverageCleaner.MERGE_MIN_INDEX,
        "GEOMETRYCOLLECTION (POLYGON ((5 3, 4 1, 1 1, 1 3, 4 3, 5 3)), POLYGON ((1 9, 4 9, 4 3, 1 3, 1 9)))"
        );
  }

  public void testMergeOverlap2() {
    checkCleanSnap(readArray(
        "POLYGON ((5 9, 9 9, 9 1, 5 1, 5 9))",
        "POLYGON ((1 5, 5 5, 5 2, 1 2, 1 5))",
        "POLYGON ((2 7, 5 7, 5 4, 2 4, 2 7))"
        ),
        0.1,
        readArray(
            "POLYGON ((5 1, 5 2, 5 4, 5 5, 5 7, 5 9, 9 9, 9 1, 5 1))",
            "POLYGON ((5 2, 1 2, 1 5, 2 5, 5 5, 5 4, 5 2))",
            "POLYGON ((5 5, 2 5, 2 7, 5 7, 5 5))"
            ));
  }
  
  public void testMergeOverlap() {
    checkCleanOverlapMerge("GEOMETRYCOLLECTION (POLYGON ((5 9, 9 9, 9 1, 5 1, 5 9)), POLYGON ((1 5, 5 5, 5 2, 1 2, 1 5)), POLYGON ((2 7, 5 7, 5 4, 2 4, 2 7)))",
        CoverageCleaner.MERGE_LONGEST_BORDER,
        "GEOMETRYCOLLECTION (POLYGON ((5 7, 5 9, 9 9, 9 1, 5 1, 5 2, 5 4, 5 5, 5 7)), POLYGON ((5 2, 1 2, 1 5, 2 5, 5 5, 5 4, 5 2)), POLYGON ((2 5, 2 7, 5 7, 5 5, 2 5)))"
        );
  }
  
  //-------------------------------------------
  
  //-- a duplicate coverage element is assigned to the lowest result index 
  public void testDuplicateItems() {
    checkClean("GEOMETRYCOLLECTION (POLYGON ((1 9, 9 1, 1 1, 1 9)), POLYGON ((1 9, 9 1, 1 1, 1 9)))",
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 9 1, 1 1, 1 9)), POLYGON EMPTY)"
        );
  }
  
  public void testCoveredItem() {
    checkClean("GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 4, 1 4, 1 9)), POLYGON ((2 5, 2 8, 8 8, 8 5, 2 5)))",
        "GEOMETRYCOLLECTION (POLYGON ((9 9, 9 4, 1 4, 1 9, 9 9)), POLYGON EMPTY)"
        );
  }
  
  public void testCoveredItemMultiPolygon() {
    checkClean("GEOMETRYCOLLECTION (MULTIPOLYGON (((1 1, 1 5, 5 5, 5 1, 1 1)), ((6 5, 6 1, 9 1, 6 5))), POLYGON ((6 1, 6 5, 9 1, 6 1)))",
        "GEOMETRYCOLLECTION (MULTIPOLYGON (((1 5, 5 5, 5 1, 1 1, 1 5)), ((6 5, 9 1, 6 1, 6 5))), POLYGON EMPTY)"
        );
  }

  
  //TODO: add test with MultiPolygon that snaps together (so needs merging)
  
  //=========================================================
 
  private void checkClean(String wkt, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry[] cov = toArray(geom);
    Geometry[] actual = CoverageCleaner.cleanGapWidth(cov, 0);
    Geometry[] covExpected = toArray(read(wktExpected));
    checkEqual(covExpected, actual);   
  }
  
  private void checkCleanGapWidth(String wkt, double gapWidth, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry[] cov = toArray(geom);
    Geometry[] actual = CoverageCleaner.cleanGapWidth(cov, gapWidth);
    Geometry[] covExpected = toArray(read(wktExpected));
    checkEqual(covExpected, actual);   
  }
  
  private void checkCleanOverlapMerge(String wkt, int mergeStrategy, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry[] cov = toArray(geom);
    Geometry[] actual = CoverageCleaner.cleanOverlapGap(cov, mergeStrategy, 0);
    Geometry[] covExpected = toArray(read(wktExpected));
    try {
      checkEqual(covExpected, actual);  
    }
    catch (Throwable ex) {
      Geometry actualGeom = toGeometryCollection(actual);
      System.out.println(actualGeom);
      throw ex;
    }
  }
  
  private Geometry toGeometryCollection(Geometry[] actual) {
    return ((new GeometryFactory().createGeometryCollection(actual)));
  }

  private Geometry[] toArray(Geometry geom) {
    return GeometryFactory.toGeometryArray(PolygonalExtracter.getPolygonals(geom));
  }

  private void checkCleanSnap(Geometry[] cov, double snapDist) {
    Geometry[] covClean = CoverageCleaner.clean(cov, snapDist, 0);
    checkValidCoverage(covClean, snapDist);
  }

  private void checkCleanSnap(Geometry[] cov, double snapDist, Geometry[] expected) {
    Geometry[] actual = CoverageCleaner.clean(cov, snapDist, 0);
    checkValidCoverage(actual, snapDist);
    checkEqual(expected, actual);
  }

  public void checkCleanSnap(String wkt, double snapDist) {
    Geometry[] cov = readArray(wkt);
    checkCleanSnap(cov, snapDist);
  }

  private void checkValidCoverage(Geometry[] coverage, double tolerance) {
    for (Geometry geom : coverage) {
      assertTrue(geom.isValid());
    }
    boolean isValid = CoverageValidator.isValid(coverage, tolerance);
    assertTrue(isValid);  
  }
}
