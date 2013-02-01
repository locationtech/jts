package com.vividsolutions.jts.io.oracle;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class OraReaderTest extends TestCase
{
  public static void main(String[] args) {
    junit.textui.TestRunner.run(OraReaderTest.class);
  }

  public OraReaderTest(String arg){
    super(arg);
  }

  public void testCreateGeometryCollectionWithPoint() throws Exception {
    final GeometryFactory geometryFactory = new GeometryFactory();
    final OraReader oraReader = new OraReader(geometryFactory);

    // Geometry type is a 'collection'.
    final int gType = 2004;
    // A collection of a 'line' and a 'point'/
    // The 'line' is starting at ordinate offset 1.
    // The 'point' is starting at ordinate offset 5.
    final int[] elemInfo = new int[] {1, 2, 1, 5, 1, 1};
    // 6 ordinates.
    // 'line' (1, 1, 2, 2).
    // 'point' (3, 3).
    final double[] ordinates = new double[] {1, 1, 2, 2, 3, 3};
    // Made 'create' method package private to enable test.
    final Geometry actual = oraReader.create(geometryFactory, gType, null, elemInfo, ordinates);

    // Preparing expected result.
    final LineString lineString =
        geometryFactory.createLineString(new Coordinate[] {new Coordinate(1, 1), new Coordinate(2, 2)});
    final Point point = geometryFactory.createPoint(new Coordinate(3, 3));

    final List<Geometry> geometries = new ArrayList<Geometry>();
    geometries.add(lineString);
    geometries.add(point);

    final GeometryCollection expected =
        geometryFactory.createGeometryCollection(GeometryFactory.toGeometryArray(geometries));

    assertEquals(expected, actual);
}

}
