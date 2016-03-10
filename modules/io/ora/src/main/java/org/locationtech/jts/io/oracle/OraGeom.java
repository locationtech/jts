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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;


/**
 * Represents the contents of an Oracle SDO_GEOMETRY structure.
 * Also provides code values and convenience methods for working
 * with SDO_GEOMETRY values.
 * 
 * @author Martin Davis
 *
 */
class OraGeom
{
	public static final int NULL_DIMENSION = -1;	

	private static NumberFormat fmt = new DecimalFormat("0.################");
	  
	public static final String SQL_NULL = "NULL";

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
    geomType = gTypeGeomType(gType);
    ordDim = gTypeDim(gType);
    lrsDim = gTypeMeasureDim(gType);
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
    return lrsDim == 0 && geomType == OraGeom.GEOM_TYPE.POINT && point != null && elemInfo == null;
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
	  return toSQLString();
	  /*
	  return "GTYPE=" + gType 
			  + " SRID=" + srid
			  + " ELEM_INFO=" + toStringElemInfo(elemInfo)
			  + " ORDS=" + toString(ordinates);
			  */
  }
  
  public String toSQLString()
  {
  	StringBuffer buf = new StringBuffer();
  	buf.append("SDO_GEOMETRY(");
  	
  	buf.append(gType);
  	buf.append(",");
  	
  	buf.append(srid >= 0 ? String.valueOf(srid) : SQL_NULL);
  	buf.append(",");
  	
  	buf.append(toStringPointType());
  	buf.append(",");
  	
  	buf.append(toStringElemInfo());
  	buf.append(",");
  	
  	buf.append(toStringOrdinates());
  	buf.append(")");
  	
  	return buf.toString();
  }
  
  private String toString(double[] ordinates)
  {
    if (ordinates == null) return SQL_NULL;
    
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < ordinates.length; i++) {
      if (i > 0) {
        buf.append(",");
        // spacer between triplets
        if (i % ordDim == 0)
          buf.append("  ");
      }
      buf.append(number(ordinates[i]));
    }
    return buf.toString();
  }

  private static String number(double d)
  {
	 if (Double.isNaN(d)) return SQL_NULL;
	 return fmt.format(d);
  }
  
  public static String toStringElemInfo(int[] elemInfo)
  {
    if (elemInfo == null) return "null";
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < elemInfo.length; i++) {
      if (i > 0) {
        buf.append(",");
        // spacer between triplets
        if (i % 3 == 0)
          buf.append("  ");
      }
      buf.append(elemInfo[i]);
    }
    return buf.toString();
  }
  
  private Object toStringOrdinates() {
	  if (ordinates == null) {
		 return SQL_NULL;
	  }
	  return "SDO_ORDINATE_ARRAY(" + toString(ordinates) + ")"; 
  }

  private Object toStringElemInfo() {
	  if (elemInfo == null) {
		 return SQL_NULL;
	  }
	  return "SDO_ELEM_INFO_ARRAY(" + toStringElemInfo(elemInfo) + ")"; 
  }

  private Object toStringPointType() {
	  if (point == null) {
		 return SQL_NULL;
	  }
	  return "SDO_POINT_TYPE(" 
	  	+ number(point[0]) + ","
	  	+ number(point[1]) + ","
	  	+ number(point[2])
	  	+ ")"; 
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
   *
   * @param elemIndex index of the triplet to read
   * @return ETYPE for indicated triplet, or -1 if the triplet index is out of range
   * 
   * @see ETYPE
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
   * Computes the SDO_GTYPE code for the given D, L, and TT components.
   * 
   * @param dim the coordinate dimension
   * @param lrsDim the measure dimension
   * @param geomType the geometry type code
   * @return the SDO_GTYPE code
   */
  public static int gType(int dim, int lrsDim, int geomType)
  {
    return dim * 1000 + lrsDim * 100 + geomType;
  }

  /**
   * Returns the GTYPE GEOM_TYPE code
   * corresponding to the geometry type.
   * 
   * @see OraGeom.GEOM_TYPE
   *
   * @param geom the geometry to compute the GEOM_TYPE for
   * @return geom type code, if known, or UNKNOWN
   */
  static int geomType(Geometry geom) {
    if (geom == null) {
        return OraGeom.GEOM_TYPE.UNKNOWN_GEOMETRY; 
    } else if (geom instanceof Point) {
        return OraGeom.GEOM_TYPE.POINT;
    } else if (geom instanceof LineString) {
        return OraGeom.GEOM_TYPE.LINE;
    } else if (geom instanceof Polygon) {
        return OraGeom.GEOM_TYPE.POLYGON;
    } else if (geom instanceof MultiPoint) {
        return OraGeom.GEOM_TYPE.MULTIPOINT;
    } else if (geom instanceof MultiLineString) {
        return OraGeom.GEOM_TYPE.MULTILINE;
    } else if (geom instanceof MultiPolygon) {
        return OraGeom.GEOM_TYPE.MULTIPOLYGON;
    } else if (geom instanceof GeometryCollection) {
        return OraGeom.GEOM_TYPE.COLLECTION;
    }
    return OraGeom.GEOM_TYPE.UNKNOWN_GEOMETRY; 
  }

  /**
   * Extracts the coordinate dimension containing the Measure value from 
   * an SDO_GTYPE code.
   * For a measured geometry this is 0, 3 or 4.  0 indicates that the last dimension is the measure dimension
   * For an non-measured geometry this is 0.
   * 
   * @param gType an SDO_GTYPE code
   * @return the Measure dimension
   */
  static int gTypeMeasureDim(int gType) {
  	return (gType % 1000) / 100;
  }

  /**
   * Extracts the coordinate dimension from an SDO_GTYPE code.
   * 
   * @param gType an SDO_GTYPE code
   * @return the coordinate dimension
   */
  static int gTypeDim(int gType) {
  	return gType / 1000;
  }

  /**
   * Extracts the GEOM_TYPE code from an SDO_GTYPE code.
   * 
   * @param gType an SDO_GTYPE code
   * @return the GEOM_TYPE code
   */
  static int gTypeGeomType(int gType) {
  	return gType % 100;
  }

  /**
   * Extracts the SDO_ELEM_INFO start index (SDO_STARTING_OFFSET) in the ordinate array for a given triplet.
   * Starting offsets are 1-based indexes.
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
   * @see OraGeom.ETYPE for an indication of possible values
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

  /**
   * Codes used in SDO_INTERPRETATION attribute.
   * 
   * @author Martin Davis
   *
   */
  static final class INTERP {
    
    public static final int POINT         = 1;
    
    public static final int LINESTRING    = 1;  
    
    public static final int POLYGON       = 1;  
    
    public static final int RECTANGLE     = 3;  
        
  }

  /**
   * Codes used to specify geometry type
   * These are used in the last two digits in a GTYPE value.
   */
  static final class GEOM_TYPE {
  
    /** <code>TT</code> code representing Unknown type */
    public static final int UNKNOWN_GEOMETRY       = 00;
  
    /** <code>TT</code> code representing Point */
    public static final int POINT         = 01;
  
    /** <code>TT</code> code representing Line (or Curve) */
    public static final int LINE          = 02;  
      
    /** <code>TT</code> code representing Polygon */
    public static final int POLYGON       = 03;
  
    /** <code>TT</code> code representing Collection */
    public static final int COLLECTION    = 04;   
  
    /** <code>TT</code> code representing MultiPoint */
    public static final int MULTIPOINT    = 05;       
  
    /** <code>TT</code> code representing MultiLine (or MultiCurve) */
    public static final int MULTILINE     = 06;
  
    /** <code>TT</code> code representing MULTIPOLYGON */
    public static final int MULTIPOLYGON  = 07;
  }

  /**
   * Codes used in the SDO_ETYPE attribute.
   * The code indicates the type of element denoted by an SDO_ELEM_INFO triplet.
   */
  static final class ETYPE
  {
    /** <code>ETYPE</code> code representing Point */
    public static final int POINT = 1;
  
    /** <code>ETYPE</code> code representing Line */
    public static final int LINE = 2;
  
    /** <code>ETYPE</code> code representing Polygon ring 
     *  Shell or hole is determined by orientation (CCW or CW).
     *  Now deprecated. 
     */
    public static final int POLYGON = 3;
  
    /**
     * <code>ETYPE</code> code representing exterior counterclockwise polygon ring
     */
    public static final int POLYGON_EXTERIOR = 1003;
  
    /** <code>ETYPE</code> code representing interior clockwise polygon ring */
    public static final int POLYGON_INTERIOR = 2003;
  }
  
  /**
   * Oracle types used by SDO_GEOMETRY
   */
  public static final String TYPE_GEOMETRY = "MDSYS.SDO_GEOMETRY";
  public static final String TYPE_ELEM_INFO_ARRAY = "MDSYS.SDO_ELEM_INFO_ARRAY";
  public static final String TYPE_ORDINATE_ARRAY = "MDSYS.SDO_ORDINATE_ARRAY";
  public static final String TYPE_POINT_TYPE = "MDSYS.SDO_POINT_TYPE";
  
  /**
   * Value indicating a Null SRID.
   */
  public static final int SRID_NULL = -1;


}
