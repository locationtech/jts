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
 * Represents the contents of an Oracle SDO_GEOMETRY structure.
 * 
 * @author Martin Davis
 *
 */
public class OraGeom
{
  int gType;
  int srid;
  double[] point = null;
  int[] elemInfo = null;
  double[] ordinates = null;
  private int geomType;
  private int ordDim;
  private int lrsDim;

  public OraGeom(int gType, int srid, double[] ptType, int[] elemInfo, double[] ordinates)
  {
    this.gType = gType;
    this.srid = srid;
    this.point = ptType;
    this.elemInfo = elemInfo;
    this.ordinates = ordinates;
    geomType = OraSDO.gTypeGeomType(gType);
    ordDim = OraSDO.gTypeDim(gType);
    lrsDim = OraSDO.gTypeMeasureDim(gType);
  }

  public OraGeom(int gType, int srid, int[] elemInfo, double[] ordinates)
  {
    this(gType, srid, null, elemInfo, ordinates);
  }

  public OraGeom(int gType, int srid, double[] ptType)
  {
    this(gType, srid, ptType, null, null);
  }

  public int geomType()
  {
    return geomType;
  }

  public int ordDim()
  {
    return ordDim;
  }

  public int lrsDim()
  {
    return lrsDim;
  }
  
  public boolean isCompactPoint()
  {
    return lrsDim == 0 && geomType == OraSDO.GEOM_TYPE.POINT && point != null && elemInfo == null;
  }
  
  public boolean isEqual(OraGeom og)
  {
    if (gType != og.gType) return false;
//    if (srid != og.srid) return false;
    if (! isEqual(point, og.point))
        return false;
    // assume is defined by elemInfo and ordinates
    if (! isEqual(elemInfo, og.elemInfo)) 
      return false;
    if (! isEqual(ordinates, og.ordinates)) 
      return false;
    return true;
  }

  private boolean isEqual(double[] a1, double[] a2)
  {
    if (a2 == null || a1 == null) {
      return a2 == a1;
    }
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
    if (a2 == null || a1 == null) {
      return a2 == a1;
    }
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
			  + " ELEM_INFO=" + toStringElemInfo(elemInfo);
  }
  
  public static String toStringElemInfo(int[] elemInfo)
  {
    if (elemInfo == null) return "null";
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < elemInfo.length; i++) {
      if (i > 0) {
        buf.append(",");
        // spacer between triplets
        if (i %3 == 0)
          buf.append("  ");
      }
      buf.append(elemInfo[i]);
    }
    return buf.toString();
  }

}
