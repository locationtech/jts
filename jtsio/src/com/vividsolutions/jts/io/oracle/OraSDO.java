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
/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package com.vividsolutions.jts.io.oracle;

import java.sql.SQLException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import oracle.sql.ARRAY;
import oracle.sql.Datum;

/**
 * Set of constants used to interact with MDSYS.GEOMETRY and JTS Geometries. 
 * 
 *
 * @author David Zwiers, Vivid Solutions.
 */
class OraSDO 
{
	
  public static final String TYPE_POINT_TYPE = "MDSYS.SDO_POINT_TYPE";
  public static final String TYPE_ORDINATE_ARRAY = "MDSYS.SDO_ORDINATE_ARRAY";
  public static final String TYPE_ELEM_INFO_ARRAY = "MDSYS.SDO_ELEM_INFO_ARRAY";
  public static final String TYPE_GEOMETRY = "MDSYS.SDO_GEOMETRY";

	/**
	 * Null SRID
	 */
	public static final int SRID_NULL = -1;
	
	/**
	 * 
	 * Extracted from the Oracle Documentation for SDO_ETYPE.
	 * This code indicates the type of element denoted by an SDO_ELEM_INFO triplet.
	 * 
	 * This list may need to be expanded in the future to handle additional Geometry Types.
	 *
	 * @author David Zwiers, Vivid Solutions.
	 * @author Jody Garnett, Refractions Research, Inc.
	 */
	static final class ETYPE{

	    /** <code>ETYPE</code> code representing Point */
	    public static final int POINT = 1;
	
	    /** <code>ETYPE</code> code representing Line */
	    public static final int LINE = 2;
	    
		/** <code>ETYPE</code> code representing Polygon */
		public static final int POLYGON = 3;
	
	    /** <code>ETYPE</code> code representing exterior counterclockwise  polygon ring */
	    public static final int POLYGON_EXTERIOR = 1003;
	
	    /** <code>ETYPE</code> code representing interior clockwise  polygon ring */
	    public static final int POLYGON_INTERIOR = 2003;
	}
    
	/**
	 * Extracted from the Oracle Documentation for SDO_GTYPE.
	 * This represents the last two digits in a GTYPE value,
	 * which specifies the geometry type.
	 * 
	 * This list may need to be expanded in the future to handle additional Geometry Types.
	 *
	 * @author David Zwiers, Vivid Solutions.
	 * @author Brent Owens, The Open Planning Project.
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

	static final class INTERP {
		
		public static final int POINT       	= 1;
		
		public static final int MULTI      		= 2;  // also all higher values
		
		public static final int LINESTRING    	= 1;  // also all higher values
		
		public static final int POLYGON      	= 1;  // also all higher values
		
		public static final int RECTANGLE      	= 3;  // also all higher values
				
	}
	
	/**
	 * Extracts the SDO_ELEM_INFO ETYPE value for a given triplet.
	 * <p>
	 * @see ETYPE for an indication of possible values
	 *
	 * @param elemInfo the SDO_ELEM_INFO array
	 * @param tripletIndex index of the triplet to read
	 * @return ETYPE for indicated triplet, or -1 if an error occurred
	 */
	static int eType(int[] elemInfo, int tripletIndex) {
	    if (((tripletIndex * 3) + 1) >= elemInfo.length) {
	        return -1;
	    }
	    return elemInfo[(tripletIndex * 3) + 1];
	}

	/**
	 * Extracts the SDO_ELEM_INFO interpretation value (SDO_INTERPRETATION) for a given triplet.
	 *
	 * JTS valid interpretation values are: 1 for straight edges, 3 for rectangle
	 *
	 * Other interpretation value include: 2 for arcs, 4 for circles
	 *
	 * @param elemInfo the SDO_ELEM_INFO array
	 * @param tripletIndex index of the triplet to read
	 * @return interpretation value, or -1 if an error occurred
	 */
	static int interpretation(int[] elemInfo, int tripletIndex) {
	    if (((tripletIndex * 3) + 2) >= elemInfo.length) {
	        return -1;
	    }
	    return elemInfo[(tripletIndex * 3) + 2];
	}

	/**
	 * Extracts the SDO_ELEM_INFO start index (SDO_STARTING_OFFSET) in the ordinate array for a given triplet.
	 *
	 * @param elemInfo the SDO_ELEM_INFO array
	 * @param tripletIndex index of the triplet to read
	 * @return Starting Offset, or -1 if an error occurred
	 */
	static int startingOffset(int[] elemInfo, int tripletIndex) {
	    if (((tripletIndex * 3) + 0) >= elemInfo.length) {
	        return -1;
	    }
	    return elemInfo[(tripletIndex * 3) + 0];
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
	 * Extracts the coordinate dimension from an SDO_GTYPE code.
	 * 
	 * @param gType an SDO_GTYPE code
	 * @return the coordinate dimension
	 */
	static int gTypeDim(int gType) {
		return gType / 1000;
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
   * Returns the GTYPE GEOM_TYPE code
   * corresponding to the geometry type.
   * 
   * @see GEOM_TYPE
   *
   * @param geom the geometry to compute the GEOM_TYPE for
   * @return geom type code, if known, or UNKNOWN
   */
  static int geomType(Geometry geom) {
      if (geom == null) {
          return GEOM_TYPE.UNKNOWN_GEOMETRY; 
      } else if (geom instanceof Point) {
          return GEOM_TYPE.POINT;
      } else if (geom instanceof LineString) {
          return GEOM_TYPE.LINE;
      } else if (geom instanceof Polygon) {
          return GEOM_TYPE.POLYGON;
      } else if (geom instanceof MultiPoint) {
          return GEOM_TYPE.MULTIPOINT;
      } else if (geom instanceof MultiLineString) {
          return GEOM_TYPE.MULTILINE;
      } else if (geom instanceof MultiPolygon) {
          return GEOM_TYPE.MULTIPOLYGON;
      } else if (geom instanceof GeometryCollection) {
          return GEOM_TYPE.COLLECTION;
      }
      return GEOM_TYPE.UNKNOWN_GEOMETRY; 
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


}
