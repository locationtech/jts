package test.jts.junit.operation.relate;

import java.util.*;
import java.io.IOException;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.util.*;
import com.vividsolutions.jts.operation.relate.RelateOp;

/**
 * Tests {@link Geometry#relate} with different {@link BoundaryNodeRule}s.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class RelateBoundaryNodeRuleTest
    extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(RelateBoundaryNodeRuleTest.class);
  }

  private GeometryFactory fact = new GeometryFactory();
  private WKTReader rdr = new WKTReader(fact);

  public RelateBoundaryNodeRuleTest(String name)
  {
    super(name);
  }

  public void testMultiLineStringSelfIntTouchAtEndpoint()
      throws Exception
  {
    String a = "MULTILINESTRING ((20 20, 100 100, 100 20, 20 100), (60 60, 60 140))";
    String b = "LINESTRING (60 60, 20 60)";

    // under EndPoint, A has a boundary node - A.bdy / B.bdy = 0
    runRelateTest(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "FF1F00102"    );
  }

  public void testLineStringSelfIntTouchAtEndpoint()
      throws Exception
  {
    String a = "LINESTRING (20 20, 100 100, 100 20, 20 100)";
    String b = "LINESTRING (60 60, 20 60)";

    // results for both rules are the same
    runRelateTest(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "F01FF0102"    );
    runRelateTest(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "F01FF0102"    );
  }

  public void testMultiLineStringTouchAtEndpoint()
      throws Exception
  {
    String a = "MULTILINESTRING ((0 0, 10 10), (10 10, 20 20))";
    String b = "LINESTRING (10 10, 20 0)";

    // under Mod2, A has no boundary - A.int / B.bdy = 0
//    runRelateTest(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "F01FFF102"    );
    // under EndPoint, A has a boundary node - A.bdy / B.bdy = 0
    runRelateTest(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "FF1F00102"    );
    // under MultiValent, A has a boundary node but B does not - A.bdy / B.bdy = F and A.int
//    runRelateTest(a, b,  BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,  "0F1FFF1F2"    );
  }

  public void testLineRingTouchAtEndpoints()
      throws Exception
  {
    String a = "LINESTRING (20 100, 20 220, 120 100, 20 100)";
    String b = "LINESTRING (20 20, 20 100)";

    // under Mod2, A has no boundary - A.int / B.bdy = 0
//    runRelateTest(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "F01FFF102"    );
    // under EndPoint, A has a boundary node - A.bdy / B.bdy = 0
//    runRelateTest(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "FF1F0F102"    );
    // under MultiValent, A has a boundary node but B does not - A.bdy / B.bdy = F and A.int
    runRelateTest(a, b,  BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,  "0F1FFF1F2"    );
  }

  public void testLineRingTouchAtEndpointAndInterior()
      throws Exception
  {
    String a = "LINESTRING (20 100, 20 220, 120 100, 20 100)";
    String b = "LINESTRING (20 20, 40 100)";

    // this is the same result as for the above test
    runRelateTest(a, b,  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE,   "F01FFF102"    );
    // this result is different - the A node is now on the boundary, so A.bdy/B.ext = 0
    runRelateTest(a, b,  BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,  "F01FF0102"    );
  }

  void runRelateTest(String wkt1, String wkt2, BoundaryNodeRule bnRule, String expectedIM)
      throws ParseException
  {
    Geometry g1 = rdr.read(wkt1);
    Geometry g2 = rdr.read(wkt2);
    IntersectionMatrix im = RelateOp.relate(g1, g2, bnRule);
    String imStr = im.toString();
    System.out.println(imStr);
    assertTrue(im.matches(expectedIM));
  }
}