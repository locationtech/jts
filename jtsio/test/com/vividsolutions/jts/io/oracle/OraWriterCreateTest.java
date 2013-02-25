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
  private static final int NULL = -1;

  public static void main(String[] args) {
    junit.textui.TestRunner.run(OraWriterCreateTest.class);
  }

  WKTReader wktRdr = new WKTReader();
  
  public OraWriterCreateTest(String arg){
    super(arg);
  }

  public void XXtestPoint() throws Exception {
    OraGeom oraGeom = OraGeom.sdo_geometry(2001,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(50,50));
    checkOracleValue(oraGeom, "POINT (50 50)");
  }

  public void XXtestPointXYZM() throws Exception {
	    OraGeom oraGeom = OraGeom.sdo_geometry(4001,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(50,50,100,200));
	    checkOracleValue(oraGeom, 3, "POINT (50 50)");
  }

  public void testPointType() throws Exception {
	    OraGeom oraGeom = OraGeom.sdo_geometry(3001,NULL,SDO_POINT_TYPE(50,50,100),NULL,NULL);
	    checkOracleValue(oraGeom, "POINT (50 50)");
  }

  public void testMultiPoint() throws Exception {
    OraGeom oraGeom = OraGeom.sdo_geometry(3005,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,2),SDO_ORDINATE_ARRAY(50,50,5, 100,200,300));
    checkOracleValue(oraGeom, "MULTIPOINT ((50 50), (100 200))");
  }

  public void XXtestLineStringXY() throws Exception {
	    OraGeom oraGeom = OraGeom.
	    		sdo_geometry(2002,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,2,1),SDO_ORDINATE_ARRAY(0,0,50,50));
	    checkOracleValue(oraGeom, "LINESTRING (0 0, 50 50)");
  }

  public void testLineStringXYZ() throws Exception {
	    OraGeom oraGeom = OraGeom.
	    		sdo_geometry(3002,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,2,1),SDO_ORDINATE_ARRAY(0,0,0,50,50,100));
	    checkOracleValue(oraGeom, "LINESTRING (0 0, 50 50)");
  }

  public void XXtestLineStringXYM() throws Exception {
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

  void checkOracleValue(OraGeom oraGeom, String wkt)
  {
	  checkOracleValue(oraGeom, -1, wkt);
  }
  
  void checkOracleValue(OraGeom expectedOraGeom, int targetDim, String wkt)
  {
    Geometry geom = null;
    try {
      geom = wktRdr.read(wkt);
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
    
    final OraWriter oraWriter = new OraWriter(null);
//    if (targetDim > -1) oraReader.setDimension(targetDim);

    final OraGeom actual = oraWriter.createOraGeom(geom);
    
    boolean isEqual = actual.isEqual(expectedOraGeom);
    if (! isEqual) {
      System.out.println("Error writing  " + wkt);
    }
    assertTrue(isEqual);
  }
}
