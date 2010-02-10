package test.jts.junit.operation.valid;

import java.util.*;

import junit.framework.TestCase;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.operation.valid.IsValidOp;

/**
 * Tests validating geometries with
 * non-closed rings.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class ValidClosedRingTest
    extends TestCase
{
  private static WKTReader rdr = new WKTReader();

  public ValidClosedRingTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(ValidClosedRingTest.class);
  }

  public void testBadLinearRing()
  {
    LinearRing ring = (LinearRing) fromWKT("LINEARRING (0 0, 0 10, 10 10, 10 0, 0 0)");
    updateNonClosedRing(ring);
    checkIsValid(ring, false);
  }

  public void testGoodLinearRing()
  {
    LinearRing ring = (LinearRing) fromWKT("LINEARRING (0 0, 0 10, 10 10, 10 0, 0 0)");
    checkIsValid(ring, true);
  }

  public void testBadPolygonShell()
  {
    Polygon poly = (Polygon) fromWKT("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))");
    updateNonClosedRing((LinearRing) poly.getExteriorRing());
    checkIsValid(poly, false);
  }

  public void testBadPolygonHole()
  {
    Polygon poly = (Polygon) fromWKT("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0), (1 1, 2 1, 2 2, 1 2, 1 1) ))");
    updateNonClosedRing((LinearRing) poly.getInteriorRingN(0));
    checkIsValid(poly, false);
  }

  public void testGoodPolygon()
  {
    Polygon poly = (Polygon) fromWKT("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))");
    checkIsValid(poly, true);
  }

  public void testBadGeometryCollection()
  {
    GeometryCollection gc = (GeometryCollection) fromWKT("GEOMETRYCOLLECTION ( POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0), (1 1, 2 1, 2 2, 1 2, 1 1) )), POINT(0 0) )");
    Polygon poly = (Polygon) gc.getGeometryN(0);
    updateNonClosedRing((LinearRing) poly.getInteriorRingN(0));
    checkIsValid(poly, false);
  }


  private void checkIsValid(Geometry geom, boolean expected)
  {
    IsValidOp validator = new IsValidOp(geom);
    boolean isValid = validator.isValid();
    assertTrue(isValid == expected);
  }

  Geometry fromWKT(String wkt)
  {
    Geometry geom = null;
    try {
      geom = rdr.read(wkt);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return geom;
  }

  private void updateNonClosedRing(LinearRing ring)
  {
    Coordinate[] pts = ring.getCoordinates();
    pts[0].x += 0.0001;
  }
}