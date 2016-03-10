/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io.oracle;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.oracle.OraGeom;
import org.locationtech.jts.io.oracle.OraReader;


/**
 * Tests {@link OraReader} without requiring an Oracle connection.
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

  //TODO: tests for ETYPE=POLYGON  (mixture of holes and shells
  //TODO: tests with mixed ETYPE=POLYGON & POLYGON_EXTERIOR
  
  public void testXY_Point() throws Exception {
    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2001,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),MDSYS.SDO_ORDINATE_ARRAY(50,50));
    checkValue(oraGeom, "POINT (50 50)");
  }

  public void testXYM_Point() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3301,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(50,50,100));
	    checkValue(oraGeom, 3, "POINT (50 50 100)");
  }

  public void testXYZM_Point() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(4001,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(50,50,100,200));
	    checkValue(oraGeom, 3, "POINT (50 50 100)");
  }

  public void testXYZ_PointType() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3001,NULL,
	    		MDSYS.SDO_POINT_TYPE(50,50,100),NULL,NULL);
	    checkValue(oraGeom, "POINT (50 50 100)");
  }

  public void testXY_OrientedPoint() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2001,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1, 3,1,0),MDSYS.SDO_ORDINATE_ARRAY(12,14, 0.3,0.2));
	    checkValue(oraGeom, "POINT (12 14)");
  }

  public void testXYZ_OrientedPoint() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2001,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1, 3,1,0),MDSYS.SDO_ORDINATE_ARRAY(12,14, 0.3,0.2));
	    checkValue(oraGeom, "POINT (12 14)");
  }
  
  public void testXY_MultiPoint_MultiElem() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2005,32639,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1,3,1,1),MDSYS.SDO_ORDINATE_ARRAY(548810.5,3956383.4, 548766.8,3956415.9));
	    checkValue(oraGeom, "MULTIPOINT ((548810.5 3956383.4), (548766.8 3956415.9))");
  }
	  
  public void testXYM_MultiPoint_MultiElem() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3005,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1,4,1,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(1,1,99, 2,2,99));
	    checkValue(oraGeom, "MULTIPOINT ((1 1), (2 2))");
  }
	  
  public void testXYZ_MultiPoint() throws Exception {
    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3005,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,2),
    		MDSYS.SDO_ORDINATE_ARRAY(50,50,5, 100,200,300));
    checkValue(oraGeom, "MULTIPOINT ((50 50 5), (100 200 300))");
  }

  public void testXY_LineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50));
	    checkValue(oraGeom, "LINESTRING (0 0, 50 50)");
  }

  public void testXYZ_LineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(0,0,0,50,50,100));
	    checkValue(oraGeom, "LINESTRING (0 0 0, 50 50 100)");
  }

  public void testXYM_LineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3302,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1),
	    		MDSYS.SDO_ORDINATE_ARRAY(1, 1, 20, 2, 2, 30));
	    checkValue(oraGeom, "LINESTRING (1 1, 2 2)");
  }

  public void testXYZM_LineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(4402,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(0,0,2,3,50,50,100,200));
	    checkValue(oraGeom, "LINESTRING (0 0, 50 50)");
  }

  public void testXYMZ_LineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(4302,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(0,0,2,3,50,50,100,200));
	    checkValue(oraGeom, "LINESTRING (0 0, 50 50)");
  }

  public void testXYZ_MultiLineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3006,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1,7,2,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(0,0,2,50,50,100,10,10,12,150,150,110));
	    checkValue(oraGeom, "MULTILINESTRING ((0 0, 50 50), (10 10, 150 150))");
}

  public void testXYM_MultiLineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3306,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1,7,2,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(0,0,2,50,50,100,10,10,12,150,150,110));
	    checkValue(oraGeom, "MULTILINESTRING ((0 0, 50 50), (10 10, 150 150))");
}

  public void testXYZM_MultiLineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(4406,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1,9,2,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(0,0,2,3,50,50,100,200,10,10,12,13,150,150,110,210));
	    checkValue(oraGeom, "MULTILINESTRING ((0 0, 50 50), (10 10, 150 150))");
  	}

  public void testXYMZ_MultiLineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(4306,8307,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1,9,2,1),
	    		MDSYS.SDO_ORDINATE_ARRAY(0,0,2,3,50,50,100,200,10,10,12,13,150,150,110,210));
	    checkValue(oraGeom, "MULTILINESTRING ((0 0, 50 50), (10 10, 150 150))");
  }

  public void testXY_Polygon() throws Exception {
	  OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1),
			  MDSYS.SDO_ORDINATE_ARRAY(0,0,50,0,50,50,0,50,0,0));
	    checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testXYZ_Polygon() throws Exception {
	  OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1),
			  MDSYS.SDO_ORDINATE_ARRAY(0,0,99,50,0,99,50,50,99,0,50,99,0,0,99));
	    checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testXYM_Polygon() throws Exception {
	  OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3303,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1),
			  MDSYS.SDO_ORDINATE_ARRAY(0,0,99,50,0,99,50,50,99,0,50,99,0,0,99));
	    checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testXYZ_PolygonWithHole() throws Exception {
	  checkValue( MDSYS.SDO_GEOMETRY(3003,  NULL,  NULL,
				    MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1, 28,2003,1), 
				    MDSYS.SDO_ORDINATE_ARRAY(2,4,99, 4,3,99, 10,3,99, 13,5,99, 13,9,99, 11,13,99, 5,13,99, 2,11,99, 2,4,99,
				        7,5,99, 7,10,99, 10,10,99, 10,5,99, 7,5,99 ) ),
				        "POLYGON ((2 4, 4 3, 10 3, 13 5, 13 9, 11 13, 5 13, 2 11, 2 4), (7 5, 7 10, 10 10, 10 5, 7 5))");
  }

  public void testXYM_PolygonWithHole() throws Exception {
	  checkValue( MDSYS.SDO_GEOMETRY(3303,  NULL,  NULL,
				    MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1, 28,2003,1), 
				    MDSYS.SDO_ORDINATE_ARRAY(2,4,99, 4,3,99, 10,3,99, 13,5,99, 13,9,99, 11,13,99, 5,13,99, 2,11,99, 2,4,99,
				        7,5,99, 7,10,99, 10,10,99, 10,5,99, 7,5,99 ) ),
				        "POLYGON ((2 4, 4 3, 10 3, 13 5, 13 9, 11 13, 5 13, 2 11, 2 4), (7 5, 7 10, 10 10, 10 5, 7 5))");
  }

  public void testXY_RectangleMultiPolygon() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2007,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3,5,2003,3,9,1003,3),
	    		MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50,40,40,20,20,60,0,70,10));
	    checkValue(oraGeom, "MULTIPOLYGON (((0 0, 50 0, 50 50, 0 50, 0 0), (40 40, 20 40, 20 20, 40 20, 40 40)), ((60 0, 70 0, 70 10, 60 10, 60 0)))");
  }

  public void testXY_Rectangle() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),
	    		MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50));
	    checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testXY_RectangleHole() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3,  5,2003,3),
	    				MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50,  40,40,20,20));
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
			              5,1, 9,5, 5,5, 5,1,
			              5,3, 6,4, 6,3, 5,3 ) ), 
	    		"GEOMETRYCOLLECTION (POINT (1 1), LINESTRING (1 2, 2 1), POLYGON ((2 2, 3 2, 3 3, 2 3, 2 2)), POLYGON ((5 1, 9 5, 5 5, 5 1), (5 3, 6 4, 6 3, 5 3)))");
  }
  public void testXYM_GeometryCollection_Doc() throws Exception {
	  checkValue(  MDSYS.SDO_GEOMETRY(3304, NULL, NULL,
			        MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1, 4,2,1, 10,1003,1, 25,1003,1, 37,2003,1),
			        MDSYS.SDO_ORDINATE_ARRAY(
			              1,1,99,
			              1,2,99, 2,1,99,
			              2,2,99, 3,2,99, 3,3,99, 2,3,99, 2,2,99,
			              5,1,99, 9,5,99, 5,5,99, 5,1,99,
			              5,3,99, 6,4,99, 6,3,99, 5,3,99 ) ), 
	    		"GEOMETRYCOLLECTION (POINT (1 1), LINESTRING (1 2, 2 1), POLYGON ((2 2, 3 2, 3 3, 2 3, 2 2)), POLYGON ((5 1, 9 5, 5 5, 5 1), (5 3, 6 4, 6 3, 5 3)))");
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
			             9,5, 13,5, 11,5, 9,5)			      ), 
	    		"MULTIPOLYGON (((2 3, 7 3, 7 9, 2 9, 2 3)), ((9 5, 13 5, 11 5, 9 5)))");
  }

  public void testXYZ_MultiPolygon() throws Exception {
	  checkValue(  MDSYS.SDO_GEOMETRY(3007, NULL,  NULL,
			       MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1, 16,1003,1),
			       MDSYS.SDO_ORDINATE_ARRAY(2,3,99, 7,3,99, 7,9,99, 2,9,99, 2,3,99,
			             9,5,99, 13,5,99, 11,5,99, 9,5,99 )			      ), 
	    		"MULTIPOLYGON (((2 3, 7 3, 7 9, 2 9, 2 3)), ((9 5, 13 5, 11 5, 9 5)))");
  }

  public void testXYM_MultiPolygon() throws Exception {
	  checkValue(  MDSYS.SDO_GEOMETRY(3307, NULL,  NULL,
			       MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1, 16,1003,1),
			       MDSYS.SDO_ORDINATE_ARRAY(2,3,99, 7,3,99, 7,9,99, 2,9,99, 2,3,99,
			             9,5,99, 13,5,99, 11,5,99, 9,5,99 )			      ), 
	    		"MULTIPOLYGON (((2 3, 7 3, 7 9, 2 9, 2 3)), ((9 5, 13 5, 11 5, 9 5)))");
  }

  public void testXYZM_MultiPolygon() throws Exception {
	  checkValue(  MDSYS.SDO_GEOMETRY(4307, NULL,  NULL,
			       MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1, 21,1003,1),
			       MDSYS.SDO_ORDINATE_ARRAY(2,3,99,88, 7,3,99,88, 7,9,99,88, 2,9,99,88, 2,3,99,88,
			             9,5,99,88, 13,5,99,88, 11,5,99,88, 9,5,99,88 )			      ), 
	    		"MULTIPOLYGON (((2 3, 7 3, 7 9, 2 9, 2 3)), ((9 5, 13 5, 11 5, 9 5)))");
  }

  public void testXY_MultiPolygonHole_GT() throws Exception {
	  checkValue(  MDSYS.SDO_GEOMETRY(2007, NULL, NULL,
			        MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1,11,2003,1,19,1003,1),
			        MDSYS.SDO_ORDINATE_ARRAY(2,3, 7,3, 7,9, 2,9, 2,3,
			              3,4, 3,8, 6,8, 3,4, 
			              9,5, 13,5, 11,8, 9,5)			      ), 
	    		"MULTIPOLYGON (((2 3, 7 3, 7 9, 2 9, 2 3), (3 4, 3 8, 6 8, 3 4)), ((9 5, 13 5, 11 8, 9 5)))");
  }

  //====================================================================================
  // Unsupported Geometry types
  
  public void testFAIL_CompoundPolygon() throws Exception {
      OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1005,2, 1,2,1, 5,2,2),MDSYS.SDO_ORDINATE_ARRAY(6,10, 10,1, 14,10, 10,14, 6,10));
      checkFailure(oraGeom, "CURVEPOLYGON ((-10.355339059327378 25.0, 25.0 -10.355339059327378, 60.35533905932738 25.0, 25.0 60.35533905932738, -10.355339059327378 25.0))");
  }

  public void testFAIL_Circle() throws Exception {
      OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,4),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,0,50,50));
      checkFailure(oraGeom, "CURVEPOLYGON ((-10.355339059327378 25.0, 25.0 -10.355339059327378, 60.35533905932738 25.0, 25.0 60.35533905932738, -10.355339059327378 25.0))");
  }
  
  public void testFAIL_SolidPolygonXY() throws Exception {
      OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3008,NULL,NULL,/*SDO_ELEM_INFO_ARRAY*/ new int[] {1,1007,1,1,1006,6,1,1003,3,7,1003,3,13,1003,3,19,1003,3,25,1003,3,31,1003,3}, 
          /*SDO_ORDINATE_ARRAY*/ new double[]{1.0,0.0,-1.0,1.0,1.0,1.0,1.0,0.0,1.0,0.0,0.0,-1.0,0.0,1.0,1.0,0.0,0.0,-1.0,0.0,1.0,-1.0,1.0,1.0,1.0,0.0,0.0,1.0,1.0,1.0,1.0,1.0,1.0,-1.0,0.0,0.0,-1.0});
      checkFailure(oraGeom, "ERROR");
  }
	
	public void testFAIL_MultiSolidPolygonXY() throws Exception {
	      OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3009,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1007,3,7,1007,3),MDSYS.SDO_ORDINATE_ARRAY(-2.0,1.0,3.0,-3.0,-1.0,0.0,0.0,0.0,0.0,1.0,1.0,1.0));
	      checkFailure(oraGeom, "ERROR");
	}
	
	public void testFAIL_CompoundPolygonXY() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1005,2, 1,2,1, 5,2,2),MDSYS.SDO_ORDINATE_ARRAY(6,10, 10,1, 14,10, 10,14, 6,10));
	    checkFailure(oraGeom, "ERROR CURVEPOLYGON ((-10.355339059327378 25.0, 25.0 -10.355339059327378, 60.35533905932738 25.0, 25.0 60.35533905932738, -10.355339059327378 25.0))");
	}
	
	public void testFAIL_CircularLineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,2),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,0,50,50));
	    checkFailure(oraGeom, "ERROR LINESTRING (0 0, 50 0, 50 50)");
	}
	
	public void testFAIL_CompoundLineStringXY() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,4,2, 1,2,1, 3,2,2),MDSYS.SDO_ORDINATE_ARRAY(10,10, 10,14, 6,10, 14,10));
	    checkFailure(oraGeom, "ERROR LINESTRING (0 0, 50 0, 50 50)");
	}
	
  //====================================================================================
  
  public void testRawGeometryCollectionWithPoint() throws Exception {
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
    final Geometry actual = oraReader.read(new OraGeom(gType, 0, elemInfo, ordinates));

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

  public void testRawLineStringM2() throws Exception {
    final GeometryFactory geometryFactory = new GeometryFactory();
    final OraReader oraReader = new OraReader(geometryFactory);

    // Geometry type is a 3-dimensional measured line.
    final int gType = 3302;
    // The 'line' is starting at ordinate offset 1.
    final int[] elemInfo = new int[] {1, 2, 1};
    final double[] ordinates = new double[] {1, 1, 20, 2, 2, 30};
    // Made 'create' method package private to enable test.
    final Geometry actual = oraReader.read(new OraGeom(gType, 0, elemInfo, ordinates));

    // Preparing expected result.
    final LineString expected =
        geometryFactory.createLineString(new Coordinate[] {new Coordinate(1, 1), new Coordinate(2, 2)});

    assertEquals(expected, actual);
  }

  void checkFailure(OraGeom oraGeom, String wkt)
  {
	  try {
		  checkValue(oraGeom, wkt);
	  }
	  catch (IllegalArgumentException e) {
		  // correct expected result
		  return;
	  }
	  fail("Expected IllegalArgumentException");
  }
  
  void checkValue(OraGeom oraGeom, String wkt)
  {
	  checkValue(oraGeom, -1, wkt);
  }
  
  void checkValue(OraGeom oraGeom, int targetDim, String wkt)
  {
    final GeometryFactory geometryFactory = new GeometryFactory();
    final OraReader oraReader = new OraReader(geometryFactory);
    if (targetDim > -1) oraReader.setDimension(targetDim);

    final Geometry actual = oraReader.read(oraGeom);

    Geometry expected = null;
    try {
      expected = wktRdr.read(wkt);
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
    
    boolean isEqual = actual.equalsNorm(expected);
    if (! isEqual) {
      System.out.println("Expected " + expected + ", actual " + actual);
    }
    assertTrue(isEqual);
  }
}
