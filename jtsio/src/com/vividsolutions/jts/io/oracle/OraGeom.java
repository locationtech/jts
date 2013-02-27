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

import java.util.Arrays;

/**
 * A structure to mimic the contents of an Oracle SDO_GEOMETRY structure.
 * 
 * @author mbdavis
 *
 */
public class OraGeom
{
  public int gType;
  public int srid;
  public double[] ptType = null;
  public int[] elemInfo = null;
  public double[] ordinates = null;

  public OraGeom(int gType, int srid, double[] ptType, int[] elemInfo, double[] ordinates)
  {
    this.gType = gType;
    this.srid = srid;
    this.ptType = ptType;
    this.elemInfo = elemInfo;
    this.ordinates = ordinates;
  }

  public OraGeom(int gType, int srid, int[] elemInfo, double[] ordinates)
  {
    this(gType, srid, null, elemInfo, ordinates);
  }

  public OraGeom(int gType, int srid, double[] ptType)
  {
    this(gType, srid, ptType, null, null);
  }

  /*
  public static OraGeom sdo_geometry(int gType, int srid, double[] ptType,
	      int elemInfo, int ordinates)
	  {
	    return new OraGeom(gType, srid, ptType);
	  }
*/
  
  public boolean isEqual(OraGeom og)
  {
    if (gType != og.gType) return false;
//    if (srid != og.srid) return false;
    if (ptType != null) {
      return isEqual(ptType, og.ptType);
    }
    // assume is defined by elemInfo and ordinates
    if (! isEqual(elemInfo, og.elemInfo)) 
      return false;
    if (! isEqual(ordinates, og.ordinates)) 
      return false;
    return true;
  }

  private boolean isEqual(double[] a1, double[] a2)
  {
    if (a2 == null) return false;
    if (a1.length != a2.length) return false;
    for (int i = 0; i < a1.length; i++) {
      // check NaN == NaN
      if (Double.isNaN(a1[i]) && Double.isNaN(a2[i])) 
    	  continue;
      if (a1[i] != a2[i]) 
    	  return false;
    }
    return true;
  }
  private boolean isEqual(int[] a1, int[] a2)
  {
    if (a2 == null) return false;
    if (a1.length != a2.length) return false;
    for (int i = 0; i < a1.length; i++) {
      if (a1[i] != a2[i]) return false;
    }
    return true;
  }
  
  public String toString()
  {
	  return "GTYPE=" + gType 
			  + " SRID=" + srid
			  + " ELEM_INFO=" + Arrays.toString(elemInfo);
  }
}
