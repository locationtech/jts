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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.oracle.OraGeom;
import org.locationtech.jts.io.oracle.OraWriter;

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

  public void testTest() throws Exception
  {
    //testXY_RectangleMultiPolygon();
  }
  
  public void testPoint() throws Exception {
    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2001,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),MDSYS.SDO_ORDINATE_ARRAY(50,50));
    checkValuePointOrdinates(oraGeom, "POINT (50 50)");
  }

  // Writing measures is not yet supported
  public void TODO_testXYZM_Point() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(4001,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),MDSYS.SDO_ORDINATE_ARRAY(50,50,100,200));
	    checkValue(oraGeom, 3, "POINT (50 50)");
}

  public void testXYZM_Point() throws Exception {
      OraGeom oraGeom = MDSYS.SDO_GEOMETRY(4001,0,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),MDSYS.SDO_ORDINATE_ARRAY(50,50,DNULL,DNULL));
      checkValue(oraGeom, 4, "POINT (50 50)");
}

  public void testXYZM_Point_SetDim() throws Exception {
      OraGeom oraGeom = MDSYS.SDO_GEOMETRY(4001,0,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),MDSYS.SDO_ORDINATE_ARRAY(50,50,DNULL,DNULL));
      checkValue(oraGeom, false, false, 4, "POINT (50 50)");
}

  public void testXYZ_PointType() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3001,NULL,MDSYS.SDO_POINT_TYPE(50,50,100),NULL,NULL);
	    checkValue(oraGeom, 3, "POINT (50 50 100)");
  }

  public void testXYZ_MultiPoint() throws Exception {
    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3005,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1,2),MDSYS.SDO_ORDINATE_ARRAY(50,50,5, 100,200,300));
    checkValue(oraGeom, 3, "MULTIPOINT ((50 50 5), (100 200 300))");
  }

  public void testXY_LineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50));
	    checkValue(oraGeom, "LINESTRING (0 0, 50 50)");
  }

  public void testXYZ_LineString() throws Exception {
	  checkValue(
			  MDSYS.SDO_GEOMETRY(3002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,0,50,50,100)),
			  3, "LINESTRING (0 0 0, 50 50 100)");
  }

  /**
   * Tests limiting output dimension.
   * 
   * @throws Exception
   */
  public void testXY_LineString_from_XYZ() throws Exception {
	  checkValue(
			  MDSYS.SDO_GEOMETRY(2002,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50)),
			  2, "LINESTRING (0 0 0, 50 50 100)");
  }

  // Writing measures are not yet supported
  public void TODO_testXYM_LineString() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(3302,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1),MDSYS.SDO_ORDINATE_ARRAY(1, 1, 20, 2, 2, 30));
	    checkValue(oraGeom, "LINESTRING (1 1, 2 2)");
  }

  public void testXY_Polygon() throws Exception {
    OraGeom oraGeom = MDSYS.
        SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,0,50,50,0,50,0,0));
      checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  public void testXY_Polygon_ShellNotCCW() throws Exception {
    OraGeom oraGeom = MDSYS.
        SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,0,50,50,0,50,0,0));
      checkValue(oraGeom, "POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))");
  }

  public void testXY_Polygon_HoleNotCW() throws Exception {
    OraGeom oraGeom = MDSYS.
    SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,1,11,2003,1),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,0,50,50,0,50,0,0, 10,10, 10,20, 20,10, 10,10));
      checkValue(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0), (10 10, 20 10, 10 20, 10 10))");
  }

  public void testXY_RectanglePolygon() throws Exception {
	    OraGeom oraGeom = MDSYS.
	    		SDO_GEOMETRY(2003,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50));
	    checkValueRectangle(oraGeom, "POLYGON ((0 0, 50 0, 50 50, 0 50, 0 0))");
  }

  /**
   * MultiPoints can be written directly as COLLECTION elements.
   * 
   * @throws Exception
   */
  public void testXY_GeometryCollection_MultiPoint() throws Exception {
    checkValue(  MDSYS.SDO_GEOMETRY(2004, NULL, NULL,
              MDSYS.SDO_ELEM_INFO_ARRAY(1,1,3,  7,2,1),
              MDSYS.SDO_ORDINATE_ARRAY(1,1,  2,2,  3,3,  1,2,  2,1 ) ), 
          "GEOMETRYCOLLECTION (MULTIPOINT (1 1, 2 2, 3 3), LINESTRING (1 2, 2 1) )");
  }

  /**
   * OraWriter does not support writing polygons with more than one ring as rectangles,
   * so these tests are disabled.
   * 
   * @throws Exception
   */
  public void INVALID_testXY_RectangleMultiPolygon() throws Exception {
	    OraGeom oraGeom = MDSYS.SDO_GEOMETRY(2007,NULL,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3,5,2003,3,9,1003,3),MDSYS.SDO_ORDINATE_ARRAY(0,0,50,50,40,40,20,20,60,0,70,10));
	    checkValueRectangle(oraGeom, "MULTIPOLYGON (((0 0, 50 0, 50 50, 0 50, 0 0), (40 40, 20 40, 20 20, 40 20, 40 40)), ((60 0, 70 0, 70 10, 60 10, 60 0)))");
  }

  public void INVALID_testXY_RectanglePolygonHole() throws Exception {
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
    checkValueRectangle( MDSYS.SDO_GEOMETRY(2003, NULL, NULL,
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
  public void testXY_Rectangle_GeometryCollection_Doc() throws Exception {
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
  
  /**
   * MultiPolygons in GeometryCollections are written as a sequence of Polygons.
   * 
   * @throws Exception
   */
  public void testXY_GeometryCollection_MultiPolygon_Doc() throws Exception {
    checkValue(  MDSYS.SDO_GEOMETRY(2004, NULL, NULL,
              MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1, 3,2,1, 7,1003,1, 17,1003,1, 25,2003,1),
              MDSYS.SDO_ORDINATE_ARRAY(
                    1,1,
                    1,2, 2,1,
                    2,2, 3,2, 3,3, 2,3, 2,2,
                    5,1, 9,5, 5,5, 5,1,
                    5,3, 6,4, 6,3, 5,3 ) ), 
          "GEOMETRYCOLLECTION (POINT (1 1), LINESTRING (1 2, 2 1), MULTIPOLYGON (((2 2, 3 2, 3 3, 2 3, 2 2)), ((5 1, 9 5, 5 5, 5 1), (5 3, 6 4, 6 3, 5 3))) )");
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
                   9,5, 11,5, 13,5, 9,5)            ), 
          "MULTIPOLYGON (((2 3, 7 3, 7 9, 2 9, 2 3)), ((9 5, 11 5, 13 5, 9 5)))");
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


  void checkValue(OraGeom expectedOraGeom, String wkt)
  {
    checkValue(expectedOraGeom, -1, wkt);
  }
  
  void checkValueRectangle(OraGeom oraGeom, String wkt)
  {
    checkValue(oraGeom, true, true, -1, wkt);
  }
  
  void checkValuePointOrdinates(OraGeom oraGeom, String wkt)
  {
    checkValue(oraGeom, false, false, -1, wkt);
  }
  
  void checkValue(OraGeom expectedOraGeom, int targetDim, String wkt)
  {
    // default values
    checkValue(expectedOraGeom, true, false, targetDim, wkt);
  }
  
  void checkValue(OraGeom expectedOraGeom, boolean isOptimizePoint, boolean isOptimizeRectangle, int targetDim, String wkt)
  {
    Geometry geom = null;
    try {
      geom = wktRdr.read(wkt);
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
    
    final OraWriter oraWriter = new OraWriter();
    if (targetDim > -1) 
    	oraWriter.setDimension(targetDim);
    oraWriter.setOptimizePoint(isOptimizePoint);
    oraWriter.setOptimizeRectangle(isOptimizeRectangle);

    final OraGeom actual = oraWriter.createOraGeom(geom);
    
    boolean isEqual = actual.isEqual(expectedOraGeom);
    if (! isEqual) {
    	//actual.isEqual(expectedOraGeom);
      System.out.println("Error writing  " + wkt);
      System.out.println("Expected:   " + expectedOraGeom + "  Actual: " + actual);
    }
    assertTrue(isEqual);
  }
}
