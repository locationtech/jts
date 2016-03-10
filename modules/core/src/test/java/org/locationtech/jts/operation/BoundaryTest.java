/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Tests {@link BoundaryOp} with different {@link BoundaryNodeRule}s.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class BoundaryTest
    extends TestCase
{
  private static final double TOLERANCE = 0.00005;

  public static void main(String args[]) {
    TestRunner.run(BoundaryTest.class);
  }

  private GeometryFactory fact = new GeometryFactory();
  private WKTReader rdr = new WKTReader(fact);

  public BoundaryTest(String name)
  {
    super(name);
  }

  /**
   * For testing only.
   *
   * @throws Exception
   */
  public void test1()
      throws Exception
  {
    String a = "MULTILINESTRING ((0 0, 10 10), (10 10, 20 20))";
    // under MultiValent, the common point is the only point on the boundary
    runBoundaryTest(a, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,
                    "POINT (10 10)"  );
  }

  public void test2LinesTouchAtEndpoint2()
      throws Exception
  {
    String a = "MULTILINESTRING ((0 0, 10 10), (10 10, 20 20))";

    // under Mod-2, the common point is not on the boundary
    runBoundaryTest(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE,
                    "MULTIPOINT ((0 0), (20 20))" );
    // under Endpoint, the common point is on the boundary
    runBoundaryTest(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,
                    "MULTIPOINT ((0 0), (10 10), (20 20))"  );
    // under MonoValent, the common point is not on the boundary
    runBoundaryTest(a, BoundaryNodeRule.MONOVALENT_ENDPOINT_BOUNDARY_RULE,
                    "MULTIPOINT ((0 0), (20 20))"  );
    // under MultiValent, the common point is the only point on the boundary
    runBoundaryTest(a, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,
                    "POINT (10 10)"  );
  }

  public void test3LinesTouchAtEndpoint2()
      throws Exception
  {
    String a = "MULTILINESTRING ((0 0, 10 10), (10 10, 20 20), (10 10, 10 20))";

    // under Mod-2, the common point is on the boundary (3 mod 2 = 1)
    runBoundaryTest(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE,
                    "MULTIPOINT ((0 0), (10 10), (10 20), (20 20))" );
    // under Endpoint, the common point is on the boundary (it is an endpoint)
    runBoundaryTest(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,
                    "MULTIPOINT ((0 0), (10 10), (10 20), (20 20))"  );
    // under MonoValent, the common point is not on the boundary (it has valence > 1)
    runBoundaryTest(a, BoundaryNodeRule.MONOVALENT_ENDPOINT_BOUNDARY_RULE,
                    "MULTIPOINT ((0 0), (10 20), (20 20))"  );
    // under MultiValent, the common point is the only point on the boundary
    runBoundaryTest(a, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,
                    "POINT (10 10)"  );
  }

  public void testMultiLineStringWithRingTouchAtEndpoint()
      throws Exception
  {
    String a = "MULTILINESTRING ((100 100, 20 20, 200 20, 100 100), (100 200, 100 100))";

    // under Mod-2, the ring has no boundary, so the line intersects the interior ==> not simple
    runBoundaryTest(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE,
                    "MULTIPOINT ((100 100), (100 200))" );
    // under Endpoint, the ring has a boundary point, so the line does NOT intersect the interior ==> simple
    runBoundaryTest(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,
                    "MULTIPOINT ((100 100), (100 200))"  );
  }

  public void testRing()
      throws Exception
  {
    String a = "LINESTRING (100 100, 20 20, 200 20, 100 100)";

    // rings are simple under all rules
    runBoundaryTest(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE,
                    "MULTIPOINT EMPTY");
    runBoundaryTest(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,
                    "POINT (100 100)"  );
  }



  private void runBoundaryTest(String wkt, BoundaryNodeRule bnRule, String wktExpected)
      throws ParseException
  {
    Geometry g = rdr.read(wkt);
    Geometry expected = rdr.read(wktExpected);

    BoundaryOp op = new BoundaryOp(g, bnRule);
    Geometry boundary = op.getBoundary();
    boundary.normalize();
//    System.out.println("Computed Boundary = " + boundary);
    assertTrue(boundary.equalsExact(expected));
  }

}
