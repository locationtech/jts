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
 * Tests {@link Geometry#relate}.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class ContainsTest
    extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(ContainsTest.class);
  }

  private GeometryFactory fact = new GeometryFactory();
  private WKTReader rdr = new WKTReader(fact);

  public ContainsTest(String name)
  {
    super(name);
  }

  /**
   * From GEOS #572.
   * A case where B is contained in A, but 
   * the JTS relate algorithm fails to compute this correctly.
   * 
   * The cause is that the long segment in A nodes the single-segment line in B.
   * The node location cannot be computed precisely.
   * The node then tests as not lying precisely on the original long segment in A.
   * 
   * The solution is to change the relate algorithm so that it never computes
   * new intersection points, only ones which occur at existing vertices.
   * (The topology of the implicit intersections can still be computed
   * to contribute to the intersection matrix result).
   * This will require a complete reworking of the relate algorithm. 
   * 
   * @throws Exception
   */
  public void testContainsIncorrect()
      throws Exception
  {
    String a = "LINESTRING (1 0, 0 2, 0 0, 2 2)";
    String b = "LINESTRING (0 0, 2 2)";

    // for now assert this as false, although it should be true
    assertTrue(! a.contains(b));
  }
}
