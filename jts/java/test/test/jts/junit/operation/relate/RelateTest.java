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
public class RelateTest
    extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(RelateTest.class);
  }

  private GeometryFactory fact = new GeometryFactory();
  private WKTReader rdr = new WKTReader(fact);

  public RelateTest(String name)
  {
    super(name);
  }

  /**
   * From GEOS #572
   * 
   * The cause is that the longer line nodes the single-segment line.
   * The node then tests as not lying precisely on the original longer line.
   * 
   * @throws Exception
   */
  public void testContainsIncorrectIMMatrix()
      throws Exception
  {
    String a = "LINESTRING (1 0, 0 2, 0 0, 2 2)";
    String b = "LINESTRING (0 0, 2 2)";

    // actual matrix is 001F001F2
    // true matrix should be 101F00FF2
    runRelateTest(a, b,  "001F001F2"    );
  }

  void runRelateTest(String wkt1, String wkt2, String expectedIM)
      throws ParseException
  {
    Geometry g1 = rdr.read(wkt1);
    Geometry g2 = rdr.read(wkt2);
    IntersectionMatrix im = RelateOp.relate(g1, g2);
    String imStr = im.toString();
    System.out.println(imStr);
    assertTrue(im.matches(expectedIM));
  }
}