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

import com.vividsolutions.jts.io.oracle.OraSDO.ETYPE;

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

  public int startingOffset(int elemIndex)
  {
    // if beyond actual elements, return "virtual" startingOffset
    if (((elemIndex * 3)) >= elemInfo.length) {
      return ordinates.length + 1;
    }
    return elemInfo[elemIndex * 3];
  }

  /**
   * Extracts the SDO_ELEM_INFO ETYPE value for a given triplet.
   * <p>
   * @see ETYPE for an indication of possible values
   *
   * @param elemIndex index of the triplet to read
   * @return ETYPE for indicated triplet, or -1 if the triplet index is out of range
   */
  public int eType(int elemIndex)
  {
    if (((elemIndex * 3) + 1) >= elemInfo.length) {
      return -1;
    }
    return elemInfo[(elemIndex * 3) + 1];
  }

  /**
   * Extracts the SDO_ELEM_INFO interpretation value (SDO_INTERPRETATION) for a given triplet.
   * <p>
   * JTS valid interpretation values are: 1 for straight edges, 3 for rectangle
   * Other interpretation value include: 2 for arcs, 4 for circles
   *
   * @param elemIndex index of the triplet to read
   * @return interpretation value, or -1 if the triplet index is out of range
   */
  public int interpretation(int elemIndex)
  {
    if (((elemIndex * 3) + 2) >= elemInfo.length) {
      return -1;
    }
    return elemInfo[(elemIndex * 3) + 2];
  }

  public int ordinateLen()
  {
    if (ordinates != null)
      return ordinates.length;
    return 0;
  }

  public int numElements()
  {
    if (elemInfo == null) return 0;
    return elemInfo.length / 3;
  }
  /**
   * Extracts the SDO_ELEM_INFO start index (SDO_STARTING_OFFSET) in the ordinate array for a given triplet.
   *
   * @param elemInfo the SDO_ELEM_INFO array
   * @param tripletIndex index of the triplet to read
   * @return Starting Offset, or -1 if the triplet index is too large
   */
  static int startingOffset(int[] elemInfo, int tripletIndex) {
      if (((tripletIndex * 3) + 0) >= elemInfo.length) {
          return -1;
      }
      return elemInfo[(tripletIndex * 3) + 0];
  }

  /**
   * Extracts the SDO_ELEM_INFO interpretation value (SDO_INTERPRETATION) for a given triplet.
   * <p>
   * JTS valid interpretation values are: 1 for straight edges, 3 for rectangle
   * Other interpretation value include: 2 for arcs, 4 for circles
   *
   * @param elemInfo the SDO_ELEM_INFO array
   * @param tripletIndex index of the triplet to read
   * @return interpretation value, or -1 if the triplet index is too large
   */
  static int interpretation(int[] elemInfo, int tripletIndex) {
      if (((tripletIndex * 3) + 2) >= elemInfo.length) {
          return -1;
      }
      return elemInfo[(tripletIndex * 3) + 2];
  }

  /**
   * Extracts the SDO_ELEM_INFO ETYPE value for a given triplet.
   * <p>
   * @see ETYPE for an indication of possible values
   *
   * @param elemInfo the SDO_ELEM_INFO array
   * @param tripletIndex index of the triplet to read
   * @return ETYPE for indicated triplet, or -1 if the triplet index is too large
   */
  static int eType(int[] elemInfo, int tripletIndex) {
      if (((tripletIndex * 3) + 1) >= elemInfo.length) {
          return -1;
      }
      return elemInfo[(tripletIndex * 3) + 1];
  }


}
