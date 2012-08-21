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
   * From GEOS #572
   * 
   * The cause is that the longer line nodes the single-segment line.
   * The node then tests as not lying precisely on the original longer line.
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

    // actual matrix is 001F001F2
    assertTrue(a.contains(b));
  }
}
