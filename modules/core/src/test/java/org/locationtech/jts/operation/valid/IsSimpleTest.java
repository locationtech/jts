/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.valid;

import java.util.List;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests {@link IsSimpleOp} with different {@link BoundaryNodeRule}s.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class IsSimpleTest
    extends GeometryTestCase
{
  private static final double TOLERANCE = 0.00005;

  public static void main(String args[]) {
    TestRunner.run(IsSimpleTest.class);
  }

  public IsSimpleTest(String name)
  {
    super(name);
  }

  /**
   * 2 LineStrings touching at an endpoint
   * @throws Exception
   */
  public void test2TouchAtEndpoint() throws Exception {
		String a = "MULTILINESTRING((0 1, 1 1, 2 1), (0 0, 1 0, 2 1))";
		checkIsSimple(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE, true,
				new Coordinate(2, 1));
		checkIsSimple(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE, true,
				new Coordinate(2, 1));
	}

  /**
   * 3 LineStrings touching at an endpoint.
   *
   * @throws Exception
   */
  public void test3TouchAtEndpoint() throws Exception {
		String a = "MULTILINESTRING ((0 1, 1 1, 2 1),   (0 0, 1 0, 2 1),  (0 2, 1 2, 2 1))";

		// rings are simple under all rules
		checkIsSimple(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE, true,
				new Coordinate(2, 1));
		checkIsSimple(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE, true,
				new Coordinate(2, 1));
	}

	public void testCross() throws Exception {
		String a = "MULTILINESTRING ((20 120, 120 20), (20 20, 120 120))";
		checkIsSimple(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE, false,
				new Coordinate(70, 70));
		checkIsSimple(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE, false,
				new Coordinate(70, 70));
	}


  public void testMultiLineStringWithRingTouchAtEndpoint()
      throws Exception
  {
    String a = "MULTILINESTRING ((100 100, 20 20, 200 20, 100 100), (100 200, 100 100))";

    // under Mod-2, the ring has no boundary, so the line intersects the interior ==> not simple
    checkIsSimple(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE,      false,  new Coordinate(100, 100)  );
    // under Endpoint, the ring has a boundary point, so the line does NOT intersect the interior ==> simple
    checkIsSimple(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  true  );
  }

  public void testRing()
      throws Exception
  {
    String a = "LINESTRING (100 100, 20 20, 200 20, 100 100)";

    // rings are simple under all rules
    checkIsSimple(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE,      true  );
    checkIsSimple(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  true  );
  }

  public void testLinesAll() {
    checkIsSimpleAll("MULTILINESTRING ((10 20, 90 20), (10 30, 90 30), (50 40, 50 10))",
        BoundaryNodeRule.MOD2_BOUNDARY_RULE,
        "MULTIPOINT((50 20), (50 30))");
  }

  public void testPolygonAll() {
    checkIsSimpleAll("POLYGON ((0 0, 7 0, 6 -1, 6 -0.1, 6 0.1, 3 5.9, 3 6.1, 3.1 6, 2.9 6, 0 0))",
      BoundaryNodeRule.MOD2_BOUNDARY_RULE,
      "MULTIPOINT((6 0), (3 6))");
  }

  public void testMultiPointAll() {
    checkIsSimpleAll("MULTIPOINT((1 1), (1 2), (1 2), (1 3), (1 4), (1 4), (1 5), (1 5))",
      BoundaryNodeRule.MOD2_BOUNDARY_RULE,
      "MULTIPOINT((1 2), (1 4), (1 5))");
  }
  public void testGeometryCollectionAll() {
    checkIsSimpleAll("GEOMETRYCOLLECTION(MULTILINESTRING ((10 20, 90 20), (10 30, 90 30), (50 40, 50 10)), " +
      "MULTIPOINT((1 1), (1 2), (1 2), (1 3), (1 4), (1 4), (1 5), (1 5)))",
      BoundaryNodeRule.MOD2_BOUNDARY_RULE,
      "MULTIPOINT((50 20), (50 30), (1 2), (1 4), (1 5))");
  }

  private void checkIsSimple(String wkt, BoundaryNodeRule bnRule, boolean expectedResult)
  {
    checkIsSimple(wkt, bnRule, expectedResult, null);
  }

  private void checkIsSimple(String wkt, BoundaryNodeRule bnRule, boolean expectedResult, Coordinate expectedLocation)
  {
    Geometry g = read(wkt);
    IsSimpleOp op = new IsSimpleOp(g, bnRule);
    boolean isSimple = op.isSimple();
    Coordinate nonSimpleLoc = op.getNonSimpleLocation();

// if geom is not simple, should have a valid location
    assertTrue(isSimple || nonSimpleLoc != null);

    assertTrue(expectedResult == isSimple);

    if ( !isSimple && expectedLocation != null ) {
      assertTrue(expectedLocation.distance(nonSimpleLoc) < TOLERANCE);
    }
  }

  private void checkIsSimpleAll(String wkt, BoundaryNodeRule bnRule,
      String wktExpectedPts)
  {
    Geometry g = read(wkt);
    IsSimpleOp op = new IsSimpleOp(g, bnRule);
    op.setFindAllLocations(true);
    op.isSimple();
    List<Coordinate> nonSimpleCoords = op.getNonSimpleLocations();
    Geometry nsPts = g.getFactory().createMultiPointFromCoords(CoordinateArrays.toCoordinateArray(nonSimpleCoords));

    Geometry expectedPts = read(wktExpectedPts);
    checkEqual(expectedPts, nsPts);
  }

}
