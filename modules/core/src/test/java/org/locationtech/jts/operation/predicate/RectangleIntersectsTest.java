package org.locationtech.jts.operation.predicate;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class RectangleIntersectsTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(RectangleIntersectsTest.class);
  }

  public RectangleIntersectsTest(String name) { super(name); }
  
  public void testXYZM() throws ParseException {
    GeometryFactory geomFact = new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
    WKTReader rdr = new WKTReader(geomFact);
    Polygon rect = (Polygon) rdr.read("POLYGON ZM ((1 9 2 3, 9 9 2 3, 9 1 2 3, 1 1 2 3, 1 9 2 3))");
    Geometry line = rdr.read("LINESTRING ZM (5 15 5 5, 15 5 5 5)");
    boolean rectIntersects = RectangleIntersects.intersects(rect, line);
    assertEquals(false, rectIntersects);
  }
}
