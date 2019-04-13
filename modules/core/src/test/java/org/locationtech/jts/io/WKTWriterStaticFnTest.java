package org.locationtech.jts.io;

import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.LineString;
import test.jts.GeometryTestCase;

import java.util.Random;

public class WKTWriterStaticFnTest extends GeometryTestCase {

  private Random _rnd;
  private WKTReader _reader;

  public static void main(String[] args) {
    TestRunner.run(new TestSuite(WKTWriterStaticFnTest.class));
  }

  public WKTWriterStaticFnTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _rnd = new Random(13);
    _reader = new WKTReader();
    _reader.setIsOldJtsCoordinateSyntaxAllowed(false);
  }

  public void testStaticToPoint() throws ParseException {
    for (int i = 0; i < 1000; i++) {
      Coordinate cs = new Coordinate(100 * _rnd.nextDouble(), 100 * _rnd.nextDouble());
      String toPointText = WKTWriter.toPoint(cs);
      Coordinate cd = _reader.read(toPointText).getCoordinate();
      assertEquals(cs, cd);
    }
  }

  public void testStaticToLineStringFromSequence() throws ParseException {
    for (int i = 0; i < 1000; i++) {
      int size = 2 + _rnd.nextInt(10);
      CoordinateSequence cs = getCSFactory(Ordinate.createXY()).create(size, 2, 0);
      for (int j = 0; j < cs.size(); j++) {
        cs.setOrdinate(j, CoordinateSequence.X, 100 * _rnd.nextDouble());
        cs.setOrdinate(j, CoordinateSequence.Y, 100 * _rnd.nextDouble());
      }
      String toLineStringText = WKTWriter.toLineString(cs);
      CoordinateSequence cd = ((LineString)_reader.read(toLineStringText)).getCoordinateSequence();
      assertEquals(cs.size(), cd.size());
      for (int j = 0; j < cs.size(); j++) {
        assertEquals(cs.getCoordinate(j), cd.getCoordinate(j));
      }
      //assertEquals(cs, cd);
    }
  }

  public void testStaticToLineStringFromCoordinateArray() throws ParseException {
    for (int i = 0; i < 1000; i++) {
      int size = 2 + _rnd.nextInt(10);
      Coordinate[] cs = new Coordinate[size];
      for (int j = 0; j < cs.length; j++) {
        cs[j] = new CoordinateXY(100 * _rnd.nextDouble(), 100 * _rnd.nextDouble());
      }
      String toLineStringText = WKTWriter.toLineString(cs);
      Coordinate[] cd = _reader.read(toLineStringText).getCoordinates();

      for (int j = 0; j < cs.length; j++) {
        assertEquals(cs[j], cd[j]);
      }
    }
  }

  public void testStaticToLineStringFromTwoCoords() throws ParseException {
    for (int i = 0; i < 1000; i++) {
      Coordinate[] cs = new Coordinate[] {new CoordinateXY(100 * _rnd.nextDouble(), 100 * _rnd.nextDouble()),
              new CoordinateXY(100 * _rnd.nextDouble(), 100 * _rnd.nextDouble())};
      String toLineStringText = WKTWriter.toLineString(cs[0], cs[1]);
      Coordinate[] cd = _reader.read(toLineStringText).getCoordinates();
      assertEquals(2, cd.length);
      assertEquals(cs[0], cd[0]);
      assertEquals(cs[1], cd[1]);
    }
  }
  
  public void testPointNoSciNot() {
    Coordinate coord = new Coordinate(123456789, 987654321);
    String wkt = WKTWriter.toPoint(coord);
    assertEquals("POINT ( 123456789 987654321 )", wkt);
  }
  
  public void testLineStringNoSciNot() {
    Coordinate coord = new Coordinate(123456789, 987654321);
    Coordinate coord2 = new Coordinate(100000000, 900000000);
    String wkt = WKTWriter.toLineString(coord, coord2);
    assertEquals("LINESTRING ( 123456789 987654321, 100000000 900000000 )", wkt);
  }
}
