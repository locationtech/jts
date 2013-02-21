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
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class OraReaderCreateTest extends TestCase
{
  private static final int NULL = -1;

  public static void main(String[] args) {
    junit.textui.TestRunner.run(OraReaderCreateTest.class);
  }

  WKTReader wktRdr = new WKTReader();
  
  public OraReaderCreateTest(String arg){
    super(arg);
  }

  public void testPoint() throws Exception {
    OraGeom oraGeom = OraGeom.sdo_geometry(2001,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(50,50));
    checkOracleValue(oraGeom, "POINT (50 50)");
  }

  public void XXXtestPoint2() throws Exception {
    OraGeom oraGeom = OraGeom.sdo_geometry(4001,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(50,50,100,200));
    checkOracleValue(oraGeom, "POINT (50 50)");
  }

  public void testMultiPoint() throws Exception {
    OraGeom oraGeom = OraGeom.sdo_geometry(3005,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,2),SDO_ORDINATE_ARRAY(50,50,5, 100,200,300));
    checkOracleValue(oraGeom, "MULTIPOINT ((50 50), (100 200))");
  }

  public void testLineStringM() throws Exception {
    OraGeom oraGeom = OraGeom.sdo_geometry(3302,NULL,NULL,SDO_ELEM_INFO_ARRAY(1, 2, 1),SDO_ORDINATE_ARRAY(1, 1, 20, 2, 2, 30));
    checkOracleValue(oraGeom, "LINESTRING (1 1, 2 2)");
  }

  private static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3, double i4, double i5)
  {
    // TODO Auto-generated method stub
    return new double[] { i0, i1, i2, i3, i4, i5 };
  }

  private static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3)
  {
    // TODO Auto-generated method stub
    return new double[] { i0, i1, i2, i3 };
  }

  private static double[] SDO_ORDINATE_ARRAY(double i, double j)
  {
    return new double[] { i, j };
  }

  private static int[] SDO_ELEM_INFO_ARRAY(int i, int j, int k)
  {
    return new int[] { i, j, k };
  }

  public void testGeometryCollectionWithPoint() throws Exception {
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

  public void testLineStringM2() throws Exception {
    final GeometryFactory geometryFactory = new GeometryFactory();
    final OraReader oraReader = new OraReader(geometryFactory);

    // Geometry type is a 3-dimensional measured line.
    final int gType = 3302;
    // The 'line' is starting at ordinate offset 1.
    final int[] elemInfo = new int[] {1, 2, 1};
    final double[] ordinates = new double[] {1, 1, 20, 2, 2, 30};
    // Made 'create' method package private to enable test.
    final Geometry actual = oraReader.create(geometryFactory, gType, null, elemInfo, ordinates);

    // Preparing expected result.
    final LineString expected =
        geometryFactory.createLineString(new Coordinate[] {new Coordinate(1, 1), new Coordinate(2, 2)});

    assertEquals(expected, actual);
  }

  void checkOracleValue(OraGeom oraGeom, String wkt)
  {
    Geometry expected = null;
    try {
      expected = wktRdr.read(wkt);
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
    
    final GeometryFactory geometryFactory = new GeometryFactory();
    final OraReader oraReader = new OraReader(geometryFactory);

    final Geometry actual = oraReader.create(geometryFactory, oraGeom.gType, null, oraGeom.elemInfo, oraGeom.ordinates);
    
    boolean isEqual = actual.equalsNorm(expected);
    if (! isEqual) {
      System.out.println("Expected " + expected + ", actual " + actual);
    }
    assertTrue(isEqual);
  }
}
