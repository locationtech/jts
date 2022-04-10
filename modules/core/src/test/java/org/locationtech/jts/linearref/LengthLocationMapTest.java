package org.locationtech.jts.linearref;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import test.jts.GeometryTestCase;

public class LengthLocationMapTest extends GeometryTestCase {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(LengthLocationMapTest.class);
  }

  public LengthLocationMapTest(String name) {
    super(name);
  }

  public void testLengthAtPosition30()
  {
    checkLlm("LINESTRING (0 0, 0 100)", "POINT (0 30)", 30);
  }

  public void testLengthAtPosition50()
  {
    checkLlm("LINESTRING (0 0, 0 100)", "POINT (0 50)", 50);
  }

  public void testLengthAtPosition60()
  {
    checkLlm("LINESTRING (0 0, 0 100)", "POINT (0 60)", 60);
  }

  public void testLengthAtPosition100()
  {
    checkLlm("LINESTRING (0 0, 0 100)", "POINT (0 100)", 100);
  }

  public void testLengthAtPosition101()
  {
    checkLlm("LINESTRING (0 0, 0 100)", "POINT (0 101)", 100);
  }

  public void testLengthAtPosition0()
  {
    checkLlm("LINESTRING (0 0, 0 100)", "POINT (0 0)", 0);
  }

  public void testLengthAtPositionMinus1()
  {
    checkLlm("LINESTRING (0 0, 0 100)", "POINT (0 -1)", 0);
  }

  public void testMultiLineLengthPosition30()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 50, 0 100))", "POINT (0 30)", 30);
  }

  public void testMultiLineLengthPosition50()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 50, 0 100))", "POINT (0 50)", 50);
  }

  public void testMultiLineLengthPosition60()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 50, 0 100))", "POINT (0 60)", 60);
  }

  public void testMultiLineLengthAtPosition100()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 50, 0 100))", "POINT (0 100)", 100);
  }

  public void testMultiLineLengthAtPosition101()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 50, 0 100))", "POINT (0 101)", 100);
  }

  public void testMultiLineLengthAtPosition0()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 50, 0 100))", "POINT (0 0)", 0);
  }

  public void testMultiLineLengthAtPositionMinus1()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 50, 0 100))", "POINT (0 -1)", 0);
  }

  public void testMultiLineHoleLengthPosition30()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 51, 0 100))", "POINT (0 30)", 30);
  }

  public void testMultiLineHoleLengthPosition50()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 51, 0 100))", "POINT (0 50)", 50);
  }

  public void testMultiLineHoleLengthPosition60()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 51, 0 100))", "POINT (0 60)", 59);
  }

  public void testMultiLineHoleLengthAtPosition100()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 51, 0 100))", "POINT (0 100)", 99);
  }

  public void testMultiLineHoleLengthAtPosition101()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 51, 0 100))", "POINT (0 101)", 99);
  }

  public void testMultiLineHoleLengthAtPosition0()
  {
    checkLlm("MULTILINESTRING((0 0, 0 50), (0 51, 0 100))", "POINT (0 0)", 0);
  }
  public void testMultiLineHoleLengthAtPosition60()
  {
    checkLlm("MULTILINESTRING((0 0, 0 30), (0 31, 0 60), (0 61, 0 100))", "POINT (0 60)", 59);
  }
  public void testMultiLineHoleLengthAtPositionMinus1()
  {
   checkLlm("MULTILINESTRING((0 0, 0 50), (0 51, 0 100))", "POINT (0 -1)", 0);
  }

  private void checkLlm(String wkt0, String wkt1, double expectedDistance) {
    Lineal line = (Lineal) read(wkt0);
    Point point = (Point)read(wkt1);
    if (line instanceof LineString)
      checkLlm((LineString) line, point, expectedDistance);
    else
      checkLlm((MultiLineString) line, point, expectedDistance);
  }

  private void checkLlm(LineString geom0, Point geom1, double expectedDistance) {
     LinearLocation loc = LocationIndexOfPoint.indexOf(geom0, geom1.getCoordinate());
     assertEquals(expectedDistance, LengthLocationMap.getLength(geom0, loc));
  }
  private void checkLlm(MultiLineString geom0, Point geom1, double expectedDistance) {
    LinearLocation loc = LocationIndexOfPoint.indexOf(geom0, geom1.getCoordinate());
    assertEquals(expectedDistance, LengthLocationMap.getLength(geom0, loc));
  }
}
