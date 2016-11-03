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

import org.locationtech.jts.io.oracle.OraGeom;

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
