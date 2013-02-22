/*
 * The JTS Topology Suite is a collection of Java classes that
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

/**
 * Tests OraReader without requiring an Oracle connection.
 * 
 * @author mbdavis
 *
 */
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

  public void testPointXYZM() throws Exception {
	    OraGeom oraGeom = OraGeom.sdo_geometry(4001,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(50,50,100,200));
	    checkOracleValue(oraGeom, 3, "POINT (50 50)");
  }

  public void testPoint2() throws Exception {
	    OraGeom oraGeom = OraGeom.sdo_geometry(3001,NULL,SDO_POINT_TYPE(50,50,100),NULL,NULL);
	    checkOracleValue(oraGeom, "POINT (50 50)");
  }

  public void testMultiPoint() throws Exception {
    OraGeom oraGeom = OraGeom.sdo_geometry(3005,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,2),SDO_ORDINATE_ARRAY(50,50,5, 100,200,300));
    checkOracleValue(oraGeom, "MULTIPOINT ((50 50), (100 200))");
  }

  public void testLineStringXYM() throws Exception {
	    OraGeom oraGeom = OraGeom.sdo_geometry(3302,NULL,NULL,SDO_ELEM_INFO_ARRAY(1, 2, 1),SDO_ORDINATE_ARRAY(1, 1, 20, 2, 2, 30));
	    checkOracleValue(oraGeom, "LINESTRING (1 1, 2 2)");
  }

  public void testPolygonXY() throws Exception {
	  OraGeom oraGeom = OraGeom.
			  sdo_geometry(2003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1),SDO_ORDINATE_ARRAY(0,0,50,0,50,50,0,50,0,0));
	    checkOracleValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testMultiPolygonXY() throws Exception {
	    OraGeom oraGeom = OraGeom.sdo_geometry(2007,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,3,5,2003,3,9,1003,3),SDO_ORDINATE_ARRAY(0,0,50,50,40,40,20,20,60,0,70,10));
	    checkOracleValue(oraGeom, "MULTIPOLYGON (((0 0, 50 0, 50 50, 0 50, 0 0), (40 40, 20 40, 20 20, 40 20, 40 40)), ((60 0, 70 0, 70 10, 60 10, 60 0)))");
  }

  public void testPolygonRectXY() throws Exception {
	    OraGeom oraGeom = OraGeom.
	    		sdo_geometry(2003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,3),SDO_ORDINATE_ARRAY(0,0,50,50));
	    checkOracleValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testPolygonHoleRectXY() throws Exception {
	    OraGeom oraGeom = OraGeom.
	    		sdo_geometry(2003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,3,5,2003,3),SDO_ORDINATE_ARRAY(0,0,50,50,40,40,20,20));
	    checkOracleValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0), (40 40, 20 40, 20 20, 40 20, 40 40))");
  }

  
  
  
  
  
  
  
  private static double[] SDO_POINT_TYPE(int x, int y, int z) {
	// TODO Auto-generated method stub
	return new double[] { x, y, z};
  }

  private static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3)
  {
    // TODO Auto-generated method stub
    return new double[] { i0, i1, i2, i3 };
  }

  private static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3, double i4, double i5)
  {
    // TODO Auto-generated method stub
    return new double[] { i0, i1, i2, i3, i4, i5 };
  }

  private static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3, double i4, double i5, 
		  double i6, double i7, double i8, double i9, double i10, double i11)
  {
    // TODO Auto-generated method stub
    return new double[] { i0, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11 };
  }

  private static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3, double i4, double i5, 
		  double i6, double i7, double i8, double i9)
  {
    // TODO Auto-generated method stub
    return new double[] { i0, i1, i2, i3, i4, i5, i6, i7, i8, i9 };
  }

  private static double[] SDO_ORDINATE_ARRAY(double i0, double i1, double i2, double i3, double i4, double i5, 
		  double i6, double i7)
  {
    // TODO Auto-generated method stub
    return new double[] { i0, i1, i2, i3, i4, i5, i6, i7 };
  }

  private static double[] SDO_ORDINATE_ARRAY(double i, double j)
  {
    return new double[] { i, j };
  }

  private static int[] SDO_ELEM_INFO_ARRAY(int t11, int t12, int t13)
  {
    return new int[] { t11, t12, t13 };
  }

  private static int[] SDO_ELEM_INFO_ARRAY(int t11, int t12, int t13, int t21, int t22, int t23)
  {
    return new int[] { t11, t12, t13, t21, t22, t23 };
  }

  private static int[] SDO_ELEM_INFO_ARRAY(int t11, int t12, int t13, int t21, int t22, int t23, int t31, int t32, int t33)
  {
    return new int[] { t11, t12, t13, t21, t22, t23, t31, t32, t33 };
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
	  checkOracleValue(oraGeom, -1, wkt);
  }
  
  void checkOracleValue(OraGeom oraGeom, int targetDim, String wkt)
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
    if (targetDim > -1) oraReader.setDimension(targetDim);

    final Geometry actual = oraReader.create(geometryFactory, oraGeom.gType, oraGeom.ptType, oraGeom.elemInfo, oraGeom.ordinates);
    
    boolean isEqual = actual.equalsNorm(expected);
    if (! isEqual) {
      System.out.println("Expected " + expected + ", actual " + actual);
    }
    assertTrue(isEqual);
  }
}
