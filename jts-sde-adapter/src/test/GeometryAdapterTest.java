package test;
/*
 * The Java Topology Suite (JTS) is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

import junit.framework.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.sde.*;
import junit.textui.TestRunner;
import com.vividsolutions.jts.geom.Point;
import com.esri.sde.sdk.client.*;

public class GeometryAdapterTest extends TestCase {

  private static final PrecisionModel precisionModel = new PrecisionModel(1, 0, 0);
  private static final GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  private static final WKTReader reader = new WKTReader(geometryFactory);

  // JTS Point
  private static final Coordinate ptCoordinate = new Coordinate(40.0d, 40.0d);

  // JTS MultiPoint
  private static final Coordinate[] mptCoordinate = {
    new Coordinate(40.0d, 40.0d),
    new Coordinate(80.0d, 80.0d)
  };

  // JTS LineString
  private static final Coordinate[] lsCoordinates = {
    new Coordinate(50, 53),
    new Coordinate(60, 63),
    new Coordinate(70, 73)
  };

  // JTS MultiLineString
  private static final Coordinate[] mtlsCoordinatesLS1 = {
    new Coordinate(50, 53),
    new Coordinate(60, 63),
    new Coordinate(70, 73)
  };
  private static final Coordinate[] mtlsCoordinatesLS2 = {
    new Coordinate(80, 83),
    new Coordinate(90, 93),
    new Coordinate(100, 103)
  };

  // JTS LinerRing
  private static final Coordinate[] lrCoordinates = {
    new Coordinate(10, 13),
    new Coordinate(20, 23),
    new Coordinate(30, 33),
    new Coordinate(10, 13)
  };

  // JTS Polygon
  private static final Coordinate[] polyShellCoords = {
    new Coordinate(0, 0),
    new Coordinate(0, 20),
    new Coordinate(20, 20),
    new Coordinate(20, 0),
    new Coordinate(0, 0)
  };
  private static final Coordinate[] polyHoleCoords = {
    new Coordinate(5, 5),
    new Coordinate(10, 5),
    new Coordinate(10, 10),
    new Coordinate(5, 10),
    new Coordinate(5, 5)
  };

  // JTS MultiPolygon
  private static final Coordinate[] mtpShellP2 = {
    new Coordinate(0.0d, 0.0d),
    new Coordinate(10.0d, 0.0d),
    new Coordinate(10.0d, 10.0d),
    new Coordinate(0.0d, 10.0d),
    new Coordinate(0.0d, 0.0d)
  };
  private static final Coordinate[] mtpHolesP2 = {
    new Coordinate(2.0d, 2.0d),
    new Coordinate(2.0d, 8.0d),
    new Coordinate(4.0d, 8.0d),
    new Coordinate(4.0d, 2.0d),
    new Coordinate(2.0d, 2.0d)
  };
  private static final Coordinate[] mtpShellP1 = {
    new Coordinate(26.0d, 22.0d),
    new Coordinate(28.0d, 22.0d),
    new Coordinate(28.0d, 28.0d),
    new Coordinate(26.0d, 28.0d),
    new Coordinate(26.0d, 22.0d)
  };
  private static final Coordinate[] mtpHolesP1 = {};

  public static void main(String args[]) {
    TestRunner.run(GeometryAdapterTest.class);
  }

  // *****************************************************************
  // ****************** JUnit Test Routines **************************
  // *****************************************************************

  public GeometryAdapterTest(String name) { super(name); }

  public void testPoint() throws Exception
  {
    com.vividsolutions.jts.geom.Point jtsPoint = getPoint();
    System.out.println("JTS Point = "+jtsPoint);
    validatePointShape(new GeometryAdapter(jtsPoint));
  }

  public void testMultiPoint() throws Exception
  {
    com.vividsolutions.jts.geom.MultiPoint jtsMultiPoint = getMultiPoint();
    System.out.println("JTS MultiPoint = "+jtsMultiPoint);
    validateMultiPointShape(new GeometryAdapter(jtsMultiPoint));
  }

  public void testPolygon() throws Exception
  {
    com.vividsolutions.jts.geom.Polygon jtsPolygon = getPolygon();
    System.out.println("JTS Polygon = "+jtsPolygon);
    validatePolygonShape(new GeometryAdapter(jtsPolygon));
  }

  public void testMultiPolygon() throws Exception
  {
    com.vividsolutions.jts.geom.MultiPolygon jtsMultiPolygon = getMultiPolygon();
    System.out.println("JTS MultiPolygon = "+jtsMultiPolygon);
    validateMultiPolygonShape(new GeometryAdapter(jtsMultiPolygon));
  }

  public void testLineString() throws Exception
  {
    com.vividsolutions.jts.geom.LineString jtsLineString = getLineString();
    System.out.println("JTS LineString = "+jtsLineString);
    validateLineStringShape(new GeometryAdapter(jtsLineString));
  }

  public void testMultiLineString() throws Exception
  {
    com.vividsolutions.jts.geom.MultiLineString jtsMultiLineString = getMultiLineString();
    System.out.println("JTS MultiLineString = "+jtsMultiLineString);
    validateMultiLineStringShape(new GeometryAdapter(jtsMultiLineString));
  }

  public void testLinearRing() throws Exception
  {
    com.vividsolutions.jts.geom.LinearRing jtsLinearRing = getLinearRing();
    System.out.println("JTS LinearRing = "+jtsLinearRing);
    validateLinearRingShape(new GeometryAdapter(jtsLinearRing));
  }

  public void testGeometryCollection() throws Exception
  {
    com.vividsolutions.jts.geom.GeometryCollection jtsGeometryCollection = getGeometryColection();
    System.out.println("JTS GeometryCollection = "+jtsGeometryCollection);
    validateGeometryCollectionShape(ShapeFactory.createShapeList(jtsGeometryCollection));
  }

  public void testNullGeometry() throws SeException {
    System.out.println("JTS Null geometry testing...");
    SeShape shape = new GeometryAdapter(null);
    assertTrue(shape.getNumOfPoints() == 0);
    assertTrue(typeName(shape).equals("NIL"));
  }

  public void testEmptyGeometry() throws SeException {
    System.out.println("JTS Empty geometry testing...");
    SeShape shape = new GeometryAdapter(new Point(null, null, 0));
    assertTrue(shape.getNumOfPoints() == 0);
    assertTrue(typeName(shape).equals("NIL"));
  }

  public void testUnknownGeometry() throws SeException {
    System.out.println("JTS Unknown geometry testing...");
    GeometryCollection geometryCollection =
        new GeometryCollection(new Geometry[]{}, geometryFactory.getPrecisionModel(), 0);
    SeShape shape = new GeometryAdapter(geometryCollection);
    assertTrue(shape.getNumOfPoints() == 0);
    assertTrue(typeName(shape).equals("NIL"));
  }

  // *****************************************************************
  // **************** Shape Creation Routines ************************
  // *****************************************************************

  private static com.vividsolutions.jts.geom.Point getPoint() {
    return geometryFactory.createPoint(ptCoordinate);
  }

  private static com.vividsolutions.jts.geom.MultiPoint getMultiPoint() {
    return geometryFactory.createMultiPoint(mptCoordinate);
  }

  private static LineString getLineString() {
    return geometryFactory.createLineString(lsCoordinates);
  }

  private static MultiLineString getMultiLineString() {
    LineString[] lineStrings = {
      geometryFactory.createLineString(mtlsCoordinatesLS1),
      geometryFactory.createLineString(mtlsCoordinatesLS2)
    };
    return geometryFactory.createMultiLineString(lineStrings);
  }

  private static LinearRing getLinearRing() {
    return geometryFactory.createLinearRing(lrCoordinates);
  }

  private static Polygon getPolygon() {
    LinearRing lrShell = geometryFactory.createLinearRing(polyShellCoords);
    LinearRing[] lrHoles = new LinearRing[]{geometryFactory.createLinearRing(polyHoleCoords)};
    return geometryFactory.createPolygon(lrShell, lrHoles);
  }

  private static com.vividsolutions.jts.geom.MultiPolygon getMultiPolygon() {
    LinearRing lrShellP1 = geometryFactory.createLinearRing(mtpShellP1);
    LinearRing[] lrHolesP1 = new LinearRing[]{geometryFactory.createLinearRing(mtpHolesP1)};
    LinearRing lrShellP2 = geometryFactory.createLinearRing(mtpShellP2);
    LinearRing[] lrHolesP2 = new LinearRing[]{geometryFactory.createLinearRing(mtpHolesP2)};
    Polygon[] polygons = {
      geometryFactory.createPolygon(lrShellP1, lrHolesP1),
      geometryFactory.createPolygon(lrShellP2, lrHolesP2)
    };
    return geometryFactory.createMultiPolygon(polygons);
  }

  private com.vividsolutions.jts.geom.GeometryCollection getGeometryColection() {
    Geometry[] geometries = {
      getPolygon(),
      getPoint(),
      getLineString()
    };
    return geometryFactory.createGeometryCollection(geometries);
  }

  // *****************************************************************
  // *************** Shape Validation Routines ***********************
  // *****************************************************************

  public String typeName(SeShape shape82) throws SeException {
          switch (shape82.getType()) {
              case SeShape.TYPE_LINE: return "LINE";
              case SeShape.TYPE_MULTI_LINE: return "MULTI_LINE";
              case SeShape.TYPE_MULTI_POINT: return "MULTI_POINT";
              case SeShape.TYPE_MULTI_POLYGON: return "MULTI_POLYGON";
              case SeShape.TYPE_MULTI_SIMPLE_LINE: return "MULTI_SIMPLE_LINE";
              case SeShape.TYPE_NIL: return "NIL";
              case SeShape.TYPE_POINT: return "POINT";
              case SeShape.TYPE_POLYGON: return "POLYGON";
              case SeShape.TYPE_SIMPLE_LINE: return "SIMPLE_LINE";
          }
          throw new UnsupportedOperationException("Unsupported type: " + shape82.getType());
    }

  public SDEPoint[][][] toPoints(SeShape shape82) throws SeException {
          if (shape82.isNil()) { return new SDEPoint[][][]{}; }
          double[][][] source = shape82.getAllCoords();
          SDEPoint[][][] dest = new SDEPoint[source.length][][];
          for (int i = 0; i < source.length; i++) {
              dest[i] = new SDEPoint[source[i].length][];
              for (int j = 0; j < source[i].length; j++) {
                  dest[i][j] = new SDEPoint[source[i][j].length/2];
                  for (int k = 0; k < source[i][j].length; k += 2) {
                      dest[i][j][k/2] = new SDEPoint(source[i][j][k], source[i][j][k + 1]);
                  }
              }
          }
          return dest;
    }

    private void validatePointShape(SeShape shape82) throws SeException {
        SDEPoint points[][][] = toPoints(shape82);
        assertTrue(new Coordinate(points[0][0][0].getX(), points[0][0][0].getY()).equals(ptCoordinate));
        assertTrue(typeName(shape82).equals("POINT"));
    }

  private void validateMultiPointShape(SeShape shape82) throws SeException {
    SDEPoint points[][][] = toPoints(shape82);
    assertTrue(new Coordinate(points[0][0][0].getX(), points[0][0][0].getY()).equals(mptCoordinate[0]));
    assertTrue(new Coordinate(points[1][0][0].getX(), points[1][0][0].getY()).equals(mptCoordinate[1]));
    assertTrue(typeName(shape82).equals("MULTI_POINT"));
  }

  private void validatePolygonShape(SeShape shape82) throws SeException {
    SDEPoint points[][][] = toPoints(shape82);
    // test polygon shell and hole
    int len = polyShellCoords.length;
    for (int i=0; i<len; i++) { // Note: SDE reverses its array of Points!
      assertTrue(new Coordinate(points[0][0][len-1-i].getX(),points[0][0][len-1-i].getY()).equals(polyShellCoords[i]));
      assertTrue(new Coordinate(points[0][1][len-1-i].getX(),points[0][1][len-1-i].getY()).equals(polyHoleCoords[i]));
    }
    assertTrue(typeName(shape82).equals("POLYGON"));
  }

  private void validateMultiPolygonShape(SeShape shape82) throws SeException {
    SDEPoint points[][][] = toPoints(shape82);
    // test polygon shell and hole
    int len = polyShellCoords.length;
    for (int i=0; i<len; i++) {
      assertTrue(new Coordinate(points[1][0][i].getX(),points[1][0][i].getY()).equals(mtpShellP2[i]));
      assertTrue(new Coordinate(points[1][1][i].getX(),points[1][1][i].getY()).equals(mtpHolesP2[i]));
    }
    assertTrue(new Coordinate(points[0][0][0].getX(),points[0][0][0].getY()).equals(mtpShellP1[0]));
    assertTrue(new Coordinate(points[0][0][1].getX(),points[0][0][1].getY()).equals(mtpShellP1[1]));
    assertTrue(new Coordinate(points[0][0][2].getX(),points[0][0][2].getY()).equals(mtpShellP1[2]));
    assertTrue(new Coordinate(points[0][0][3].getX(),points[0][0][3].getY()).equals(mtpShellP1[3]));
    assertTrue(typeName(shape82).equals("MULTI_POLYGON"));

  }

  private void validateLineStringShape(SeShape shape82) throws SeException {
    SDEPoint points[][][] = toPoints(shape82);
    assertTrue(new Coordinate(points[0][0][0].getX(),points[0][0][0].getY()).equals(lsCoordinates[0]));
    assertTrue(new Coordinate(points[0][0][1].getX(),points[0][0][1].getY()).equals(lsCoordinates[1]));
    assertTrue(new Coordinate(points[0][0][2].getX(),points[0][0][2].getY()).equals(lsCoordinates[2]));
    assertTrue(typeName(shape82).equals("LINE"));
  }

  private void validateMultiLineStringShape(SeShape shape82) throws SeException {
    SDEPoint points[][][] = toPoints(shape82);
    assertTrue(new Coordinate(points[0][0][0].getX(),points[0][0][0].getY()).equals(mtlsCoordinatesLS1[0]));
    assertTrue(new Coordinate(points[0][0][1].getX(),points[0][0][1].getY()).equals(mtlsCoordinatesLS1[1]));
    assertTrue(new Coordinate(points[0][0][2].getX(),points[0][0][2].getY()).equals(mtlsCoordinatesLS1[2]));
    assertTrue(new Coordinate(points[1][0][0].getX(),points[1][0][0].getY()).equals(mtlsCoordinatesLS2[0]));
    assertTrue(new Coordinate(points[1][0][1].getX(),points[1][0][1].getY()).equals(mtlsCoordinatesLS2[1]));
    assertTrue(new Coordinate(points[1][0][2].getX(),points[1][0][2].getY()).equals(mtlsCoordinatesLS2[2]));
    assertTrue(typeName(shape82).equals("MULTI_LINE"));
  }

  private void validateLinearRingShape(SeShape shape82) throws SeException {
    SDEPoint points[][][] = toPoints(shape82);
    assertTrue(new Coordinate(points[0][0][0].getX(),points[0][0][0].getY()).equals(lrCoordinates[0]));
    assertTrue(new Coordinate(points[0][0][1].getX(),points[0][0][1].getY()).equals(lrCoordinates[1]));
    assertTrue(new Coordinate(points[0][0][2].getX(),points[0][0][2].getY()).equals(lrCoordinates[2]));
    assertTrue(new Coordinate(points[0][0][3].getX(),points[0][0][3].getY()).equals(lrCoordinates[3]));
  }

  private void validateGeometryCollectionShape(SeShape[] shapes82) throws SeException {
    validatePolygonShape(shapes82[0]);
    validatePointShape(shapes82[1]);
    validateLineStringShape(shapes82[2]);
  }

}
