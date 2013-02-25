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
public class OraReaderCreateTest extends BaseOraTestCase
{

  public static void main(String[] args) {
    junit.textui.TestRunner.run(OraReaderCreateTest.class);
  }

  WKTReader wktRdr = new WKTReader();
  
  public OraReaderCreateTest(String arg){
    super(arg);
  }

  public void testPoint() throws Exception {
    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2001,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),MDSYS.SDO_ORDINATE_ARRAY(50,50));
    checkValue(oraGeom, "POINT (50 50)");
  }

  public void testPointXYZM() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(4001,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),MDSYS.SDO_ORDINATE_ARRAY(50,50,100,200));
	    checkValue(oraGeom, 3, "POINT (50 50)");
  }

  public void testPointType() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3001,NULL,MDSYS.SDO_POINT_TYPE(50,50,100),NULL,NULL);
	    checkValue(oraGeom, "POINT (50 50)");
  }

  public void testMultiPoint() throws Exception {
    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3005,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,2),MDSYS.SDO_ORDINATE_ARRAY(50,50,5, 100,200,300));
    checkValue(oraGeom, "MULTIPOINT ((50 50), (100 200))");
  }

  public void testLineStringXY() throws Exception {
	    OraGeom oraGeom = MDSYS.
	    		SDO_GEOMETRY(2002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50));
	    checkValue(oraGeom, "LINESTRING (0 0, 50 50)");
  }

  public void testLineStringXYZ() throws Exception {
	    OraGeom oraGeom = MDSYS.
	    		SDO_GEOMETRY(3002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,0,50,50,100));
	    checkValue(oraGeom, "LINESTRING (0 0, 50 50)");
  }

  public void testLineStringXYM() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3302,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1),MDSYS.SDO_ORDINATE_ARRAY(1, 1, 20, 2, 2, 30));
	    checkValue(oraGeom, "LINESTRING (1 1, 2 2)");
  }

  public void testLineStringXYMZ() throws Exception {
	    OraGeom oraGeom = MDSYS.
	    		SDO_GEOMETRY(4302,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,2,3,50,50,100,200));
	    checkValue(oraGeom, "LINESTRING (0 0, 50 50)");
  }

  public void testMultiLineStringXYMZ() throws Exception {
	    OraGeom oraGeom = MDSYS.
	    		SDO_GEOMETRY(4306,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1,9,2,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,2,3,50,50,100,200,10,10,12,13,150,150,110,210));
	    checkValue(oraGeom, "MULTILINESTRING ((0 0, 50 50), (10 10, 150 150))");
  }

  public void testPolygonXY() throws Exception {
	  OraGeom oraGeom = MDSYS.
			  SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,0,50,50,0,50,0,0));
	    checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testMultiPolygonXY() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2007,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3,5,2003,3,9,1003,3),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50,40,40,20,20,60,0,70,10));
	    checkValue(oraGeom, "MULTIPOLYGON (((0 0, 50 0, 50 50, 0 50, 0 0), (40 40, 20 40, 20 20, 40 20, 40 40)), ((60 0, 70 0, 70 10, 60 10, 60 0)))");
  }

  public void testPolygonRectXY() throws Exception {
	    OraGeom oraGeom = MDSYS.
	    		SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50));
	    checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testPolygonHoleRectXY() throws Exception {
	    OraGeom oraGeom = MDSYS.
	    		SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3,5,2003,3),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50,40,40,20,20));
	    checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0), (40 40, 20 40, 20 20, 40 20, 40 40))");
  }

  //====================================================================================
  // Tests from Oracle documentation
  
  public void testDocPoint() throws Exception {
	  checkValue(
			  MDSYS.SDO_GEOMETRY(2001, NULL, MDSYS.SDO_POINT_TYPE(-79, 37, NULL), NULL, NULL),
					  "POINT (-79 37)");
  }
  public void testDocRectangle() throws Exception {
	  checkValue(
			  MDSYS.SDO_GEOMETRY(
					    2003, 
					    NULL,
					    NULL,
					    MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),
					    MDSYS.SDO_ORDINATE_ARRAY(1,1, 5,7) 
					  ),
					  "POLYGON ((1 1, 5 1, 5 7, 1 7, 1 1))");
  }
  public void testDocPolygonWithHole() throws Exception {
	  checkValue(
			  MDSYS.SDO_GEOMETRY(
					   2003,  
					    NULL,
					    NULL,
					    MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1, 19,2003,1), 
					    MDSYS.SDO_ORDINATE_ARRAY(2,4, 4,3, 10,3, 13,5, 13,9, 11,13, 5,13, 2,11, 2,4,
					        7,5, 7,10, 10,10, 10,5, 7,5)
					  ),
					  "POLYGON ((2 4, 4 3, 10 3, 13 5, 13 9, 11 13, 5 13, 2 11, 2 4), (7 5, 7 10, 10 10, 10 5, 7 5))");
  }
  public void testDocGeometryCollection() throws Exception {
	  checkValue(
			  MDSYS.SDO_GEOMETRY(
			       2004, NULL, NULL,
			        MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1, 3,2,1, 7,1003,1, 17,1003,1, 25,2003,1),
			        MDSYS.SDO_ORDINATE_ARRAY(
			              1,1,
			              1,2, 2,1,
			              2,2, 3,2, 3,3, 2,3, 2,2,
			              5,1, 5,5, 9,5, 5,1,
			              5,3, 6,4, 6,3, 5,3
			        )
			      ), 
	    		"GEOMETRYCOLLECTION (POINT (1 1), LINESTRING (1 2, 2 1), POLYGON ((2 2, 3 2, 3 3, 2 3, 2 2)), POLYGON ((5 1, 5 5, 9 5, 5 1), (5 3, 6 4, 6 3, 5 3)))");
  }
  
  //====================================================================================
  // Tests from GeoTools

  public void testDocTriangle() throws Exception {
	  checkValue(
			  MDSYS.SDO_GEOMETRY(
					       2003,
					       NULL,
					       NULL,
					       MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1),
					       MDSYS.SDO_ORDINATE_ARRAY(9,5, 13,5, 11,8, 9,5)),
	    		"POLYGON ((9 5, 13 5, 11 8, 9 5))");
  }

  public void testDocMultiPolygon() throws Exception {
	  checkValue(
			  MDSYS.SDO_GEOMETRY(
					2007,
					       NULL,
					       NULL,
					       MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1, 11,1003,1),
					       MDSYS.SDO_ORDINATE_ARRAY(2,3, 7,3, 7,9, 2,9, 2,3,
					             9,5, 13,5, 11,5, 9,5)			      ), 
	    		"MULTIPOLYGON (((2 3, 7 3, 7 9, 2 9, 2 3)), ((9 5, 13 5, 11 5, 9 5)))");
  }

  public void testDocMultiPolygonHole() throws Exception {
	  checkValue(
			  MDSYS.SDO_GEOMETRY(
					2007,
			        NULL,
			        NULL,
			        MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1,11,2003,1,19,1003,1),
			        MDSYS.SDO_ORDINATE_ARRAY(2,3, 7,3, 7,9, 2,9, 2,3,
			              3,4, 3,8, 6,8, 3,4, 
			              9,5, 13,5, 11,8, 9,5)			      ), 
	    		"MULTIPOLYGON (((2 3, 7 3, 7 9, 2 9, 2 3), (3 4, 3 8, 6 8, 3 4)), ((9 5, 13 5, 11 8, 9 5)))");
  }

  
  //====================================================================================
  
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

  void checkValue(OraGeom oraGeom, String wkt)
  {
	  checkValue(oraGeom, -1, wkt);
  }
  
  void checkValue(OraGeom oraGeom, int targetDim, String wkt)
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
