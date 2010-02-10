/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package test.jts.junit.operation;

import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.operation.*;
import junit.framework.*;
import junit.textui.*;

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
