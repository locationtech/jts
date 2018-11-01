package org.locationtech.jtstest.testbuilder.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jtstest.TestUtil;

import junit.framework.TestCase;

public class ConstrainedInteriorPointTest extends TestCase {

  public ConstrainedInteriorPointTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    String[] testCaseName = {ConstrainedInteriorPointTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }
  
  public void testUshape() {
    checkPoint("POLYGON ((100 100, 200 100, 200 200, 300 200, 300 100, 320 100, 320 240, 100 240, 100 100))", new Coordinate(150, 170));
  }
  
  public void testSimple() {
    checkPoint("POLYGON ((100 100, 100 200, 200 200, 200 100, 100 100))", new Coordinate(150, 150));
  }

  public void testSimpleHole() {
    checkPoint("POLYGON ((100 100, 100 200, 200 200, 200 100, 100 100), (150 190, 190 190, 190 110, 150 110, 150 190))",
        new Coordinate(125, 150));
  }
  
  public void testSimpleConstrained() {
    checkPoint("POLYGON ((100 300, 300 300, 300 100, 100 100, 100 300))",
        "POLYGON ((350 50, 200 50, 200 200, 350 200, 350 50))",
        new Coordinate(250, 150));
  }

  private void checkPoint(String wkt, Coordinate ptExpected) {
    checkPoint(wkt, null, ptExpected);
  }
  
  private void checkPoint(String wkt, String wktCon, Coordinate ptExpected) {
    Geometry poly = TestUtil.readWKT(wkt);
    Coordinate ptActual = null;
    if (wktCon != null) {
      Envelope envCon = TestUtil.readWKT(wktCon).getEnvelopeInternal();
      ptActual = ConstrainedInteriorPoint.getCoordinate((Polygon) poly, envCon);
    }
    else {
      ptActual = ConstrainedInteriorPoint.getCoordinate((Polygon) poly);
    }
    //System.out.println(ptActual);
    assertTrue(ptExpected.equals2D(ptActual));
  }
  
}
