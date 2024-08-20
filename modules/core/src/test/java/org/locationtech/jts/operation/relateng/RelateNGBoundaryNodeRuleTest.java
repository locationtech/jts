/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.operation.relateng;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.IntersectionMatrix;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;


/**
 * Tests {@link RelateNG} with {@link BoundaryNodeRule}s.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class RelateNGBoundaryNodeRuleTest
    extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(RelateNGBoundaryNodeRuleTest.class);
  }

  public RelateNGBoundaryNodeRuleTest(String name)
  {
    super(name);
  }

  public void testMultiLineStringSelfIntTouchAtEndpoint()
  {
    String a = "MULTILINESTRING ((20 20, 100 100, 100 20, 20 100), (60 60, 60 140))";
    String b = "LINESTRING (60 60, 20 60)";

    // under EndPoint, A has a boundary node - A.bdy / B.bdy = 0
    runRelate(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "FF1F00102"    );
  }

  public void testLineStringSelfIntTouchAtEndpoint()
  {
    String a = "LINESTRING (20 20, 100 100, 100 20, 20 100)";
    String b = "LINESTRING (60 60, 20 60)";

    // results for both rules are the same
    runRelate(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "F01FF0102"    );
    runRelate(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "F01FF0102"    );
  }

  public void testMultiLineStringTouchAtEndpoint()
  {
    String a = "MULTILINESTRING ((0 0, 10 10), (10 10, 20 20))";
    String b = "LINESTRING (10 10, 20 0)";

    // under Mod2, A touch point is not boundary - A.int / B.bdy = 0
    runRelate(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "F01FF0102"    );
    // under EndPoint, A has a boundary node - A.bdy / B.bdy = 0
    runRelate(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "FF1F00102"    );
    // under MultiValent, A has a boundary node but B does not - A.bdy / B.bdy = F and A.bdy / B.int = 0
    runRelate(a, b,  BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,  "FF10FF1F2"    );
  }

  public void testLineRingTouchAtEndpoints()
  {
    String a = "LINESTRING (20 100, 20 220, 120 100, 20 100)";
    String b = "LINESTRING (20 20, 20 100)";

    // under Mod2, A has no boundary - A.int / B.bdy = 0
    runRelate(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "F01FFF102"    );
    // under EndPoint, A has a boundary node - A.bdy / B.bdy = 0
    runRelate(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "FF1F0F102"    );
    // under MultiValent, A has a boundary node but B does not - A.bdy / B.bdy = F and A.bdy / B.int = 0
    runRelate(a, b,  BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,  "FF10FF1F2"    );
  }

  public void testLineRingTouchAtEndpointAndInterior()
  {
    String a = "LINESTRING (20 100, 20 220, 120 100, 20 100)";
    String b = "LINESTRING (20 20, 40 100)";

    // this is the same result as for the above test
    runRelate(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "F01FFF102"    );
    // this result is different - the A node is now on the boundary, so A.bdy/B.ext = 0
    runRelate(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "F01FF0102"    );
  }

  public void testPolygonEmptyRing()
  {
    String a = "POLYGON EMPTY";
    String b = "LINESTRING (20 100, 20 220, 120 100, 20 100)";

    // closed line has no boundary under SFS rule
    runRelate(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "FFFFFF1F2"    );
    
    // closed line has boundary under ENDPOINT rule
    runRelate(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "FFFFFF102"    );
  }

  public void testPolygonEmptyMultiLineStringClosed()
  {
    String a = "POLYGON EMPTY";
    String b = "MULTILINESTRING ((0 0, 0 1), (0 1, 1 1, 1 0, 0 0))";

    // closed line has no boundary under SFS rule
    runRelate(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "FFFFFF1F2"    );
    
    // closed line has boundary under ENDPOINT rule
    runRelate(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "FFFFFF102"    );
  }

  public void testPolygonEqualRotated()
  {
    String a = "POLYGON ((0 0, 140 0, 140 140, 0 140, 0 0))";
    String b = "POLYGON ((140 0, 0 0, 0 140, 140 140, 140 0))";

    // BNR only considers linear endpoints, so results are equal for all rules
    runRelate(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "2FFF1FFF2"    );
    runRelate(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "2FFF1FFF2"    );
    runRelate(a, b,  BoundaryNodeRule.MONOVALENT_ENDPOINT_BOUNDARY_RULE,  "2FFF1FFF2"    );
    runRelate(a, b,  BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,  "2FFF1FFF2"    );  
  }
  
  public void testLineStringInteriorTouchMultivalent()
  {
    String a = "POLYGON EMPTY";
    String b = "MULTILINESTRING ((0 0, 0 1), (0 1, 1 1, 1 0, 0 0))";

    // closed line has no boundary under SFS rule
    runRelate(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "FFFFFF1F2"    );
    
    // closed line has boundary under ENDPOINT rule
    runRelate(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "FFFFFF102"    );
  }

  void runRelate(String wkt1, String wkt2, BoundaryNodeRule bnRule, String expectedIM)
  {
    Geometry g1 = read(wkt1);
    Geometry g2 = read(wkt2);
    IntersectionMatrix im = RelateNG.relate(g1, g2, bnRule);
    String imStr = im.toString();
    //System.out.println(imStr);
    assertTrue("Expected " + expectedIM + ", found " + im, im.matches(expectedIM));
    }

}
