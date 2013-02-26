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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Tests OraWriter without requiring an Oracle connection.
 * 
 * @author mbdavis
 *
 */
public class OraWriterCreateTest extends BaseOraTestCase
{

  public static void main(String[] args) {
    junit.textui.TestRunner.run(OraWriterCreateTest.class);
  }

  WKTReader wktRdr = new WKTReader();
  
  public OraWriterCreateTest(String arg){
    super(arg);
  }

  public void XXtestPoint() throws Exception {
    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2001,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),MDSYS.SDO_ORDINATE_ARRAY(50,50));
    checkValue(oraGeom, "POINT (50 50)");
  }

  public void XXtestXYZM_Point() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(4001,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),MDSYS.SDO_ORDINATE_ARRAY(50,50,100,200));
	    checkValue(oraGeom, 3, "POINT (50 50)");
  }

  public void testXYZ_PointType() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3001,NULL,MDSYS.SDO_POINT_TYPE(50,50,100),NULL,NULL);
	    checkValue(oraGeom, 3, "POINT (50 50 100)");
  }

  public void testXYZ_MultiPoint() throws Exception {
    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3005,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,2),MDSYS.SDO_ORDINATE_ARRAY(50,50,5, 100,200,300));
    checkValue(oraGeom, 3, "MULTIPOINT ((50 50 5), (100 200 300))");
  }

  public void XXtestXY_LineString() throws Exception {
	    OraGeom oraGeom = MDSYS.
	    		SDO_GEOMETRY(2002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50));
	    checkValue(oraGeom, "LINESTRING (0 0, 50 50)");
  }

  public void testXYZ_LineString() throws Exception {
	  checkValue(
			  MDSYS.SDO_GEOMETRY(3002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,0,50,50,100)),
	    3, "LINESTRING (0 0 0, 50 50 100)");
  }

  public void XXtestXYM_LineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3302,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1),MDSYS.SDO_ORDINATE_ARRAY(1, 1, 20, 2, 2, 30));
	    checkValue(oraGeom, "LINESTRING (1 1, 2 2)");
}

  public void testXY_Polygon() throws Exception {
	  OraGeom oraGeom = MDSYS.
			  SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,0,50,50,0,50,0,0));
	    checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testXY_MultiPolygon() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2007,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3,5,2003,3,9,1003,3),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50,40,40,20,20,60,0,70,10));
	    checkValue(oraGeom, "MULTIPOLYGON (((0 0, 50 0, 50 50, 0 50, 0 0), (40 40, 20 40, 20 20, 40 20, 40 40)), ((60 0, 70 0, 70 10, 60 10, 60 0)))");
  }

  public void testXY_PolygonRect() throws Exception {
	    OraGeom oraGeom = MDSYS.
	    		SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50));
	    checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testXY_PolygonHoleRect() throws Exception {
	    OraGeom oraGeom = MDSYS.
	    		SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3,5,2003,3),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50,40,40,20,20));
	    checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0), (40 40, 20 40, 20 20, 40 20, 40 40))");
}

  //====================================================================================
  // Cases from Oracle documentation
  
  public void testXY_Point_Doc() throws Exception {
    checkValue( MDSYS.SDO_GEOMETRY(2001, NULL, MDSYS.SDO_POINT_TYPE(-79, 37, NULL), NULL, NULL),
            "POINT (-79 37)");
  }
  public void testXY_Rectangle_Doc() throws Exception {
    checkValue( MDSYS.SDO_GEOMETRY(2003, NULL, NULL,
              MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),
              MDSYS.SDO_ORDINATE_ARRAY(1,1, 5,7) ),
            "POLYGON ((1 1, 5 1, 5 7, 1 7, 1 1))");
  }
  public void testXY_PolygonWithHole_Doc() throws Exception {
    checkValue( MDSYS.SDO_GEOMETRY(2003,  NULL,  NULL,
            MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1, 19,2003,1), 
            MDSYS.SDO_ORDINATE_ARRAY(2,4, 4,3, 10,3, 13,5, 13,9, 11,13, 5,13, 2,11, 2,4,
                7,5, 7,10, 10,10, 10,5, 7,5) ),
                "POLYGON ((2 4, 4 3, 10 3, 13 5, 13 9, 11 13, 5 13, 2 11, 2 4), (7 5, 7 10, 10 10, 10 5, 7 5))");
  }
  public void testXY_GeometryCollection_Doc() throws Exception {
    checkValue(  MDSYS.SDO_GEOMETRY(2004, NULL, NULL,
              MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1, 3,2,1, 7,1003,1, 17,1003,1, 25,2003,1),
              MDSYS.SDO_ORDINATE_ARRAY(
                    1,1,
                    1,2, 2,1,
                    2,2, 3,2, 3,3, 2,3, 2,2,
                    5,1, 5,5, 9,5, 5,1,
                    5,3, 6,4, 6,3, 5,3 ) ), 
          "GEOMETRYCOLLECTION (POINT (1 1), LINESTRING (1 2, 2 1), POLYGON ((2 2, 3 2, 3 3, 2 3, 2 2)), POLYGON ((5 1, 5 5, 9 5, 5 1), (5 3, 6 4, 6 3, 5 3)))");
  }
  

  //====================================================================================
  // Cases from GeoTools

  public void testXY_LineString_GT() throws Exception {
    checkValue(  MDSYS.SDO_GEOMETRY(2002, NULL, NULL,
              MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),
              MDSYS.SDO_ORDINATE_ARRAY(1,2, 2,1, 3,1, 4,2, 4,7)),
          "LINESTRING (1 2, 2 1, 3 1, 4 2, 4 7)");
  }

  public void testXY_MultiLineString_GT() throws Exception {
    checkValue(  MDSYS.SDO_GEOMETRY(2006, NULL, NULL,
              MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1,11,2,1),
              MDSYS.SDO_ORDINATE_ARRAY(1,2, 2,1, 3,1, 4,2, 4,7, 2,7, 4,7, 5,7)),
          "MULTILINESTRING ((1 2, 2 1, 3 1, 4 2, 4 7), (2 7, 4 7, 5 7))");
  }

  public void testXY_Triangle_GT() throws Exception {
    checkValue(MDSYS.SDO_GEOMETRY(2003, NULL,  NULL,
             MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1),
             MDSYS.SDO_ORDINATE_ARRAY(9,5, 13,5, 11,8, 9,5)),
          "POLYGON ((9 5, 13 5, 11 8, 9 5))");
  }

  public void testXY_MultiPolygon_GT() throws Exception {
    checkValue(  MDSYS.SDO_GEOMETRY(2007, NULL,  NULL,
             MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1, 11,1003,1),
             MDSYS.SDO_ORDINATE_ARRAY(2,3, 7,3, 7,9, 2,9, 2,3,
                   9,5, 13,5, 11,5, 9,5)            ), 
          "MULTIPOLYGON (((2 3, 7 3, 7 9, 2 9, 2 3)), ((9 5, 13 5, 11 5, 9 5)))");
  }

  public void testXY_MultiPolygonHole_GT() throws Exception {
    checkValue(  MDSYS.SDO_GEOMETRY(2007, NULL, NULL,
              MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1,11,2003,1,19,1003,1),
              MDSYS.SDO_ORDINATE_ARRAY(2,3, 7,3, 7,9, 2,9, 2,3,
                    3,4, 3,8, 6,8, 3,4, 
                    9,5, 13,5, 11,8, 9,5)           ), 
          "MULTIPOLYGON (((2 3, 7 3, 7 9, 2 9, 2 3), (3 4, 3 8, 6 8, 3 4)), ((9 5, 13 5, 11 8, 9 5)))");
  }

  //====================================================================================


  void checkValue(OraGeom oraGeom, String wkt)
  {
	  checkValue(oraGeom, -1, wkt);
  }
  
  void checkValue(OraGeom expectedOraGeom, int targetDim, String wkt)
  {
    Geometry geom = null;
    try {
      geom = wktRdr.read(wkt);
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
    
    final OraWriter oraWriter = new OraWriter(null);
    if (targetDim > -1) oraWriter.setDimension(targetDim);

    final OraGeom actual = oraWriter.createOraGeom(geom);
    
    boolean isEqual = actual.isEqual(expectedOraGeom);
    if (! isEqual) {
      System.out.println("Error writing  " + wkt);
    }
    assertTrue(isEqual);
  }
}
