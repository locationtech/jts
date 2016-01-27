/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package com.vividsolutions.jts.io.oracle;

/**
 * Mimics Oracle MDSYS functions for building geometries.
 * Useful for creating test objects.
 * 
 * @author mbdavis
 *
 */
public class MDSYS {

  protected static final int NULL = -1;

	public static OraGeom SDO_GEOMETRY(int gType, int srid, int ptType,
			int[] elemInfo, double[] ordinates) {
		return new OraGeom(gType, srid, elemInfo, ordinates);
	}

	public static OraGeom SDO_GEOMETRY(int gType, int srid, double[] ptType,
			int null1, int null2) {
		return new OraGeom(gType, srid, ptType);
	}

	public static double[] SDO_POINT_TYPE(double x, double y, double z) {
	  if (z == NULL) z = Double.NaN;
		return new double[] { x, y, z };
	}

	public static int[] SDO_ELEM_INFO_ARRAY(int... i) {
		return i;
	}
	
	public static double[] SDO_ORDINATE_ARRAY(double... d) {
		return d;
	}


}
