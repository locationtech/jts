package com.vividsolutions.jts.io.oracle;

/**
 * Mimics Oracle MDSYS functions for building geometries.
 * Useful for creating test objects.
 * 
 * @author mbdavis
 *
 */
public class MDSYS {

	public static OraGeom SDO_GEOMETRY(int gType, int srid, int ptType,
			int[] elemInfo, double[] ordinates) {
		return new OraGeom(gType, srid, elemInfo, ordinates);
	}

	public static OraGeom SDO_GEOMETRY(int gType, int srid, double[] ptType,
			int null1, int null2) {
		return new OraGeom(gType, srid, ptType);
	}

	public static double[] SDO_POINT_TYPE(int x, int y, int z) {
		return new double[] { x, y, z };
	}

	public static int[] SDO_ELEM_INFO_ARRAY(int... i) {
		return i;
	}
	
	public static double[] SDO_ORDINATE_ARRAY(double... d) {
		return d;
	}


}
