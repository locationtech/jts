package com.vividsolutions.jts.io.oracle;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class OraWriterSQLTest extends TestCase {
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(OraWriterSQLTest.class);
	}

	WKTReader wktRdr = new WKTReader();

	public OraWriterSQLTest(String arg) {
		super(arg);
	}

	public void testPoint() throws Exception {
		checkValue("POINT (50 50)",
				"SDO_GEOMETRY(2001,0,SDO_POINT_TYPE(50,50,NULL),NULL,NULL)");
	}

	public void testPointType() throws Exception {
		checkValue("POINT (50 50)", false, -1,
				"SDO_GEOMETRY(2001,0,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(50,50))");
	}

	public void testXYZ_LineString() throws Exception {
	  checkValue("LINESTRING (0 0 0, 50 50 100)",
	    "SDO_GEOMETRY(2002,0,NULL,SDO_ELEM_INFO_ARRAY(1,2,1),SDO_ORDINATE_ARRAY(0,0,  50,50))");
	}

	//============================================================
	
	private void checkValue(String wkt, String sqlExpected) {
		checkValue(wkt, true, -1, sqlExpected);
	}

	private void checkValue(String wkt, boolean isOptimizePoint, int targetDim, String sqlExpected) {
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
	    
	    String sql = oraWriter.writeSQL(geom);
	    boolean isEqual = sql.equals(sqlExpected);
	    if (! isEqual) {
	      System.out.println("Error writing  " + wkt);
	      System.out.println("Expected:   " + sqlExpected + "  Actual: " + sql);
	    }
	    assertTrue(isEqual);
	}

}
