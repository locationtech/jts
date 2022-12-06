package org.locationtech.jts.operation.relate;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

public class ZeroLengthLineStringTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(ZeroLengthLineStringTest.class);
  }

  private GeometryFactory factory = new GeometryFactory();
  private WKTReader reader = new WKTReader(factory);

  public ZeroLengthLineStringTest(String name)
  {
    super(name);
  }

  /**
   * From JTS #345
   *
   * 0-length LineString is invalid (not clear from the spec, refers
   * to the ticket about this question)
   *
   * @throws Exception
   */
  public void testZeroLengthLineStringInvalid()
          throws Exception
  {
    String a = "LINESTRING (0 0, 0 0)";
    Geometry geom1 = reader.read(a);
    assertTrue(!geom1.isValid());
  }


  /**
   * From JTS #345
   *
   * Intersects a geom with itself should return true, even if geom
   * is a 0-length (degenerated) LineString
   *
   * @throws Exception
   */
  public void testIntersectsZeroLengthLineStringWithItself()
          throws Exception
  {
    String a = "LINESTRING (0 0, 0 0)";
    Geometry geom = reader.read(a);
    assertTrue(geom.intersects(geom));
  }

  /**
   * From JTS #345
   *
   * Intersects geom with a buffer around it should return true,
   * even if geom is a 0-length (degenerated) LineString
   *
   * @throws Exception
   */
  public void testIntersectsZeroLengthLineStringWithBuffer()
          throws Exception
  {
    String a = "LINESTRING (0 0, 0 0)";
    Geometry geom = reader.read(a);
    assertTrue(geom.intersects(geom.buffer(1.0)));
  }

  /**
   * From JTS #345
   *
   * Boundary of a zero-length LineString is empty
   *
   * @throws Exception
   */
  public void testZeroLengthLineStringBoundary()
          throws Exception
  {
    String a = "LINESTRING (0 0, 0 0)";
    Geometry geom = reader.read(a);
    assertTrue(geom.getBoundary().isEmpty());
  }

  /**
   * From JTS #345
   *
   * Intersects a valid LineString with a 0-dimensional LineString
   * located on one of its boundary should return true
   *
   * @throws Exception
   */
  public void testIntersectsBetweenLineStringAndItsBoundary()
          throws Exception
  {
    String a = "LINESTRING (0 0, 1 0)";
    String b = "LINESTRING (0 0, 0 0)";
    Geometry geom1 = reader.read(a);
    Geometry geom2 = reader.read(b);
    assertTrue(geom1.intersects(geom2));
  }

  /**
   * From JTS #345
   *
   * WARNING touches between a LineString and a 0-length LineString lying
   * on its boundary returns false but it should return true as for the
   * punctal case.
   * The test with 0-length linestring is deactivated.
   * One way to return true as for the Point is to say that 0-length LineString
   * has dimension 1. IMHO, it LineString#getDimension() should return one, but
   * it breaks other tests which check that empty LineString#getDimension return
   * 1 (which can probably be discussed).
   *
   * @throws Exception
   */

  public void testTouchesBetweenLineStringAndItsBoundary()
          throws Exception
  {
    String a = "LINESTRING (0 0, 1 0)";
    String b = "LINESTRING (0 0, 0 0)";
    String c = "POINT (0 0)";
    Geometry geom1 = reader.read(a);
    Geometry geom2 = reader.read(b);
    Geometry geom3 = reader.read(c);
    assertTrue(geom1.touches(geom2));
    assertTrue(geom2.touches(geom1));
    assertTrue(geom1.touches(geom3));
    assertTrue(geom3.touches(geom1));
  }

}
