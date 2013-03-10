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
import java.util.ArrayList;
import java.util.List;

import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.STRUCT;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Reads a {@link Geometry} from an Oracle <code>MDSYS.GEOMETRY</code> <code>STRUCT</code> object.
 * <p>
 * The number of dimensions read is limited by the number supported
 * by the {@link CoordinateSequenceFactory} used.
 * <p>
 * A {@link GeometryFactory} may be provided, otherwise a default one will be used.
 * If a {@link PrecisionModel} other than {@link PrecisionModel#FLOATING} 
 * is supplied it is the callers's responsibility
 * to ensure that it matches the precision of the incoming data.
 * If a lower precision for the data is required, a subsequent
 * process must be run on the data to reduce its precision.
 * <p>
 * The following Oracle geometry types are supported:
 * <ul>
 * <li>POINT, MULTIPOINT
 * <li>LINE, MULTILINE
 * <li>POLYGON, MULTIPOLYGON
 * </ul>
 * The optimized representations of <code>SDO_POINT</code> 
 * and <code>RECTANGLE</code> are supported:
 * <ul>
 * <li>If the <code>SDO_POINT</code> attribute is present 
 * and <code>SDO_ELEM_INFO</code> and <code>SDO_ORDINATES</code> are not,
 * a {@link Point} geometry is read.
 * Otherwise, the geometry specified by the latter two attributes is read.
 * <li><code>RECTANGLE</code>s are converted to equivalent {@link Polygon}s
 * </ul> 
 * The Geometry SRID field is populated from the input Geometry.
 * <p>
 * To use this class a suitable Oracle JDBC JAR must be present in the classpath.
 * 
 * <h3>LIMITATIONS</h3>
 * <ul>
 * <li>Geometries with Measures (XYM or XYZM) can be read, but the Measure values are not preserved
 * <li>Oracle geometries with a GTYPE of <code>43xx</code> (XYMZ) are not supported.
 * <li>Geometries containing arcs are not supported
 * <li>Surface and solid geometries are not supported
 * <li>There is currently no way to read ancillary SDO_POINT information, if present
 * </ul>
 *
 * @author Martin Davis
 */
public class OraReader 
{
	
	private static final int NULL_DIMENSION = -1;
	
	private GeometryFactory geometryFactory;

	private int dimension = NULL_DIMENSION;

	/**
	 * Creates a new reader, with a default {@link GeometryFactory}.
	 *
	 * @see #OraReader(GeometryFactory)
	 */
	public OraReader() {
		this(new GeometryFactory());
	}

	/**
	 * Creates a new reader, with the supplied {@link GeometryFactory}.
	 * It is assumed that the supplied {@link PrecisionModel}
	 * matches the precision of the incoming data -
	 * coordinates are <b>not</b> made precise when read.
	 *
	 * @param gf A non-null geometry factory for later use.
	 *
	 * @throws NullPointerException when the geometry factory is null.
	 */
	public OraReader(GeometryFactory gf) {
		if (gf == null)
			throw new NullPointerException("Geometry Factory may not be Null");
		this.geometryFactory = gf;
	}
	
	/**
	 * Gets the number of coordinate dimensions which will be read.
	 *
	 * @return the dimension which will be read
	 */
	public int getDimension() {
		return dimension;
	}

	/**
	 * Sets the number of coordinate dimensions to read.
	 *
	 * @param dimension the dimension to read
	 */
	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	/**
	 * Reads a {@link Geometry} representing the MDSYS.GEOMETRY
	 * provided in the STRUCT. The type of geometry created 
	 * depends on the Geometry type specified within the STRUCT.
	 * The SRID of the created geometry is set to be the same as the input SRID.
	 *
	 * @param struct The MDSYS.GEOMETRY Object to decode
	 * @return the Geometry if one could be created, null otherwise
	 * 
	 * @throws SQLException if a read error occurs while accessing the struct
   * @throws IllegalArgumentException if an unsupported geometry type or encoding error is found
	 */
	public Geometry read(STRUCT struct) throws SQLException 
	{
		// Return null for null input
		if (struct == null)
			return null;

		Datum data[] = struct.getOracleAttributes();
		
		int gType = OraUtil.toInteger(data[0], 0);
		int SRID = OraUtil.toInteger(data[1], OraGeom.SRID_NULL);
		double point[] = OraUtil.toDoubleArray((STRUCT) data[2], Double.NaN);
		int elemInfo[] = OraUtil.toIntArray((ARRAY) data[3], 0);
		double ordinates[] = OraUtil.toDoubleArray((ARRAY) data[4], Double.NaN);
		OraGeom oraGeom = new OraGeom(gType, SRID, point, elemInfo, ordinates);
		Geometry geom = read(oraGeom);
		
		// Set SRID of created Geometry to be the same as input (regardless of geomFactory SRID)
		if (geom != null)
			geom.setSRID(SRID);
		return geom;
	}

	/**
   * Reads a {@link Geometry} from SDO_GEOMETRY attributes.
   *
   * @param oraGeom the Oracle geometry to read
   * @return the Geometry read
   * @throws IllegalArgumentException when an encoding error or unsupported geometry type is found
   */
	Geometry read(OraGeom oraGeom) {
    int ordDim = oraGeom.ordDim();
    if (ordDim < 2) {
    	throw new IllegalArgumentException("Dimension D = " + ordDim + " is not supported by JTS. " +
    			"Either specify a valid dimension or use Oracle Locator Version 9i or later");
    }
    // The actual dimension read is the smaller of the explicit dimension and the input dimension
    int outputDim = ordDim;
    if(dimension != NULL_DIMENSION){
    	outputDim = dimension;
    }    
    // read from SDO_POINT_TYPE, if that carries the primary geometry data
    if (oraGeom.isCompactPoint()) {
      CoordinateSequence ptCoord = extractCoords(oraGeom, oraGeom.point);
      return createPoint(ptCoord);
    } 
    
    CoordinateSequence coords = null;

    switch (oraGeom.geomType()) {
    case OraGeom.GEOM_TYPE.POINT:
        return readPoint(oraGeom, 0);
    case OraGeom.GEOM_TYPE.LINE:
        return readLine(oraGeom, 0);
    case OraGeom.GEOM_TYPE.POLYGON:
        return readPolygon(oraGeom, 0);
    case OraGeom.GEOM_TYPE.MULTIPOINT:
        return readMultiPoint(oraGeom, 0);
    case OraGeom.GEOM_TYPE.MULTILINE:
        return readMultiLine(oraGeom);
    case OraGeom.GEOM_TYPE.MULTIPOLYGON:
        return readMultiPolygon(oraGeom);
    case OraGeom.GEOM_TYPE.COLLECTION:
        return readCollection(oraGeom);
    default:
    	throw new IllegalArgumentException("GTYPE " + oraGeom.gType + " is not supported");
    }
  }

  /**
   * Create a {@link GeometryCollection} as specified by elemInfo. Note that in
   * Oracle, unlike the SFS and JTS, collections contain only atomic types or
   * (possibly) MultiPoints. This makes them simpler to parse.
   * 
   * @param oraGeom
   *          SDO_GEOMETRY attributes being read
   * @param coords
   *          the coordinates of the entire geometry
   * @return GeometryCollection
   * 
   * @throws IllegalArgumentException
   *           when an encoding error or unsupported geometry type is found
   */
    private GeometryCollection readCollection(OraGeom oraGeom) 
    {
      checkOrdinates(oraGeom, 0, "GeometryCollection");
      
      int nElem = oraGeom.numElements();
      List geomList = new ArrayList();
      boolean cont = true;
      for (int i = 0; cont && i < nElem; i++) {
        int etype = oraGeom.eType(i);
        int interpretation = oraGeom.interpretation(i);
        Geometry geom;

        switch (etype) {
        case -1:
          cont = false; // We are at the end of the list - get out of here
          continue;
            
        case OraGeom.ETYPE.POINT:
            if (interpretation == OraGeom.INTERP.POINT) {
                geom = readPoint(oraGeom, i);
            } else if (interpretation > 1) {
                geom = readMultiPoint(oraGeom, i);
            } else {
                throw new IllegalArgumentException("ETYPE.POINT requires INTERPRETATION >= 1");
            }
            break;

        case OraGeom.ETYPE.LINE:
            geom = readLine(oraGeom, i);
            break;

        case OraGeom.ETYPE.POLYGON:
        case OraGeom.ETYPE.POLYGON_EXTERIOR:
            geom = readPolygon(oraGeom, i);
            i += ((Polygon) geom).getNumInteriorRing();
            break;

        case OraGeom.ETYPE.POLYGON_INTERIOR:
            throw new IllegalArgumentException(
                "ETYPE 2003 (Polygon Interior) not expected in a Collection");

        default:
            throw new IllegalArgumentException("ETYPE " + etype
                + " not representable as a JTS Geometry."
                + "(Custom and Compound Straight and Curved Geometries not supported)");
        }
        geomList.add(geom);
      }
      GeometryCollection geoms = geometryFactory.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
      return geoms;
    }

    /**
     * Create MultiPolygon as encoded by elemInfo.
     *
     * @param oraGeom SDO_GEOMETRY attributes being read
     * @param coords the coordinates of the entire geometry
     * @return MultiPolygon
     */
    private MultiPolygon readMultiPolygon(OraGeom oraGeom)
    {
      int nElem = oraGeom.numElements();
      List geoms = new ArrayList();
      for (int i = 0; i < nElem; i++) {
        int etype = oraGeom.eType(i);
        if ((etype == OraGeom.ETYPE.POLYGON) || (etype == OraGeom.ETYPE.POLYGON_EXTERIOR)) {
          Polygon poly = readPolygon(oraGeom, i);
          i += poly.getNumInteriorRing(); // skip interior rings
          geoms.add(poly);
        } 
        else { // not a Polygon - stop reading
        	break;
        }
      }
      MultiPolygon polys = geometryFactory.createMultiPolygon(GeometryFactory.toPolygonArray(geoms));
      return polys;
    }

    /**
     * Create MultiLineString as encoded by elemInfo.
     *
     * @param oraGeom SDO_GEOMETRY attributes being read
     * @param coords the coordinates of the entire geometry
     * @return MultiLineString
     */
    private MultiLineString readMultiLine(OraGeom oraGeom) 
    {
      int nElem = oraGeom.numElements();
      List geoms = new ArrayList();
      for (int i = 0; i < nElem; i++) {
        int etype = oraGeom.eType(i);
        // stop reading if not a line
        if (etype != OraGeom.ETYPE.LINE)
          break;
        geoms.add(readLine(oraGeom, i));
      }
      MultiLineString lines = geometryFactory.createMultiLineString(GeometryFactory.toLineStringArray(geoms));
      return lines;
    }

    /**
     * Create MultiPoint as encoded by elemInfo.
     *
     * @param oraGeom SDO_GEOMETRY attributes being read
     * @param elemIndex the element being read
     * @param coords the coordinates of the entire geometry
     * @return MultiPoint
     */
    private MultiPoint readMultiPoint(OraGeom oraGeom, int elemIndex) 
    {
      CoordinateSequence seq;
      /**
       * Special handling when GTYPE is MULTIPOINT.
       * In this case all ordinates are read as a single MultiPoint, regardless of elemInfo contents.
       * This is because MultiPoints can be encoded as either a single MULTI elemInfo,
       * or as multiple POINT elemInfos
       */
      if (oraGeom.geomType() == OraGeom.GEOM_TYPE.MULTIPOINT) {
        seq = extractCoords(oraGeom, oraGeom.ordinates);
      }
      else {
        int etype = oraGeom.eType(elemIndex);
        int interpretation = oraGeom.interpretation(elemIndex);
  
        checkOrdinates(oraGeom, elemIndex, "MultiPoint");
        checkETYPE(etype, OraGeom.ETYPE.POINT, "MultiPoint");
        // MultiPoints have a unique interpretation code
        if (! (interpretation >= 1)){
          errorInterpretation(interpretation, "MultiPoint");
        }
        seq = extractCoords(oraGeom, elemIndex);
      }
      MultiPoint points = geometryFactory.createMultiPoint(seq);
      return points;
    }
    
    /**
     * Read {@link Polygon) from encoded geometry.
     *
     * @param oraGeom SDO_GEOMETRY attributes being read
     * @param elemIndex the element being read
     * @param coords the coordinates of the entire geometry
     * @return Polygon as encoded by elemInfo, or null when faced with and
     *         encoding that can not be captured by JTS
     * @throws IllegalArgumentException When faced with an invalid SDO encoding
     */
    private Polygon readPolygon(OraGeom oraGeom, int elemIndex) 
    {
      int etype = oraGeom.eType(elemIndex);
      int interpretation = oraGeom.interpretation(elemIndex);

    	checkOrdinates(oraGeom, elemIndex, "Polygon");
    	checkETYPE(etype,OraGeom.ETYPE.POLYGON, OraGeom.ETYPE.POLYGON_EXTERIOR, "Polygon");
    	checkInterpretation(interpretation, OraGeom.INTERP.POLYGON, OraGeom.INTERP.RECTANGLE, "Polygon");

      int nElem = oraGeom.numElements();
    	// ETYPE is either POLYGON or POLYGON_EXTERIOR
      LinearRing exteriorRing = readLinearRing(oraGeom, elemIndex);

      /**
       * Holes are read as long as ETYPE = POLYGON_INTERIOR
       * or ETYPE = POLYGON && orient = CW (Hole)
       */
      List holeRings = new ArrayList();
      for (int i = elemIndex + 1; i < nElem; i++) {
        etype = oraGeom.eType(i);
        if (etype == OraGeom.ETYPE.POLYGON_INTERIOR) {
          holeRings.add(readLinearRing(oraGeom, i));
        } 
        else if (etype == OraGeom.ETYPE.POLYGON) { 
          // test orientation of Ring to see if it is
          // an interior (hole) ring
          LinearRing ring = readLinearRing(oraGeom, i);
          // TODO: use the coordSeq directly (requires new CGAlgorithms method)
          boolean isHole = ! CGAlgorithms.isCCW(ring.getCoordinates());
          // if not a hole, exit
          if (! isHole)
            break;
          // it is an Interior Hole
          holeRings.add(ring);
        } 
        else { // not a LinearRing - get out of here
            break;
        }
      }
      Polygon poly = geometryFactory.createPolygon(exteriorRing, 
          GeometryFactory.toLinearRingArray(holeRings));
      return poly;
    }

    /**
     * Create LinearRing for exterior/interior polygon ELEM_INFO triplets.
     *
     * @param oraGeom SDO_GEOMETRY attributes being read
     * @param elemIndex the element being read
     * @param coords the coordinates of the entire geometry
     * @return LinearRing
     *
     * @throws IllegalArgumentException If circle, or curve is requested
     */
    private LinearRing readLinearRing(OraGeom oraGeom, int elemIndex) 
    {
      int etype = oraGeom.eType(elemIndex);
      int interpretation = oraGeom.interpretation(elemIndex);

    	checkOrdinates(oraGeom, elemIndex, "Polygon");
    	checkETYPE(etype,OraGeom.ETYPE.POLYGON, OraGeom.ETYPE.POLYGON_EXTERIOR,  OraGeom.ETYPE.POLYGON_INTERIOR, "Polygon");
    	checkInterpretation(interpretation, OraGeom.INTERP.POLYGON, OraGeom.INTERP.RECTANGLE, "Polygon");

      CoordinateSequence seq = extractCoords(oraGeom, elemIndex);
    	LinearRing ring;
      if (interpretation == OraGeom.INTERP.POLYGON) {
        ring = geometryFactory.createLinearRing(seq);
      } 
      else { 
      	// interpretation == OraSDO.INTERP.RECTANGLE
        // rectangle does not maintain measures
        Coordinate min = seq.getCoordinate(0);
        Coordinate max = seq.getCoordinate(1);
        ring = geometryFactory.createLinearRing(new Coordinate[] {
                    min, new Coordinate(max.x, min.y), 
                    max, new Coordinate(min.x, max.y), 
                    min
                });
      }
      return ring;
    }

  /**
   * Create LineString as encoded.
   * 
   * @param oraGeom SDO_GEOMETRY attributes being read
   * @param elemIndex the element being read
   * @param coords the coordinates of the entire geometry
   * @return LineString
   * 
   * @throws IllegalArgumentException If asked to create a curve
   */
  private LineString readLine(OraGeom oraGeom, int elemIndex)
  {
    int etype = oraGeom.eType(elemIndex);
    int interpretation = oraGeom.interpretation(elemIndex);

  	checkOrdinates(oraGeom, elemIndex, "LineString");
  	checkETYPE(etype, OraGeom.ETYPE.LINE, "LineString");
  	checkInterpretation(interpretation, OraGeom.INTERP.LINESTRING, "LineString");
	
    LineString line = geometryFactory.createLineString(
        extractCoords(oraGeom, elemIndex));
    return line;
  }

    /**
     * Create Point as encoded.
     *
   * @param oraGeom SDO_GEOMETRY attributes being read
   * @param elemIndex the element being read
   * @param coords the coordinates of the entire geometry
     * @return Point
     */
    private Point readPoint(OraGeom oraGeom, int elemIndex) {
      int etype = oraGeom.eType(elemIndex);
      int interpretation = oraGeom.interpretation(elemIndex);

  		checkOrdinates(oraGeom, elemIndex, "Point");
  		checkETYPE(etype,OraGeom.ETYPE.POINT, "Point");
  		checkInterpretation(interpretation, OraGeom.INTERP.POINT, "Point");
  
      CoordinateSequence seq = extractCoords(oraGeom, elemIndex);
      return createPoint(seq);
    }

    private Point createPoint(CoordinateSequence coords)
    {
      return geometryFactory.createPoint(coords);
    }

    /**
     * Constructs a coordinate sequence from the ordinates
     * for a given element
     * taking into account the input and output dimensions.
     *
     * The number of ordinates read per coordinate is given by outputDim.
     * The number of ordinates for each input point is given by ordDim.
     * The ordinate array length must be a multiple of this value.
     *
     * @param oraGeom the input geometry
     * @param elemIndex the element to read
     * @return a coordinate sequence representing the ordinates for the element
     *
     * @throws IllegalArgumentException if the ordinate array has an invalid length
     */
    private CoordinateSequence extractCoords(OraGeom oraGeom, int elemIndex)
    {
      int start = oraGeom.startingOffset(elemIndex);
      int end = oraGeom.startingOffset(elemIndex + 1);
      return extractCoords(oraGeom, oraGeom.ordinates, start, end);
    }
    
    private CoordinateSequence extractCoords(OraGeom oraGeom, double[] ordinates)
    {
      return extractCoords(oraGeom, ordinates, 1, ordinates.length + 1);
    }
    
    private CoordinateSequence extractCoords(OraGeom oraGeom, double[] ordinates, int start, int end)
    {
      int ordDim = oraGeom.ordDim();
      // The actual dimension read is the smaller of the explicit dimension and the input dimension
      int outputDim = oraGeom.ordDim();
      if(dimension != NULL_DIMENSION){
        outputDim = dimension;
      }    
      CoordinateSequenceFactory csFactory = geometryFactory.getCoordinateSequenceFactory();
      // handle empty case
      if ((ordinates == null) || (ordinates.length == 0)) {
        return csFactory.create(new Coordinate[0]);
      }
      /*
      if (checkValidOrdLength) {
        if ((ordDim == 0 && ordinates.length != 0)
            || (ordDim != 0 && ((ordinates.length % ordDim) != 0))) {
          throw new IllegalArgumentException("SDO_GTYPE Dimension " + ordDim
              + " is inconsistent with SDO_ORDINATES length " + ordinates.length);
        }
      }
      */
      int nCoord = (ordDim == 0 ? 0 : (end - start) / ordDim);

      int csDim = outputDim;
      CoordinateSequence cs = csFactory.create(nCoord, csDim);
      int actualCSDim = cs.getDimension();
      int readDim = Math.min(actualCSDim, ordDim);
      
      for (int iCoord = 0; iCoord < nCoord; iCoord++) {
        for (int iDim = 0; iDim < readDim; iDim++) {
          int ordIndex = start + iCoord * ordDim + iDim - 1;
          cs.setOrdinate(iCoord, iDim, ordinates[ordIndex]);
        }
      }
      return cs;
    }

    private static void checkETYPE(int eType, int val1, String geomType)
    {
    	checkETYPE(eType,val1, -1, -1, geomType);
    }
    
    private static void checkETYPE(int eType, int val1, int val2, String geomType)
    {
    	checkETYPE(eType,val1, val2, -1, geomType);
    }
    
    private static void checkETYPE(int eType, int val1, int val2, int val3, String geomType)
    {
    	if (eType == val1) return;
    	if (val2 >= 0 && eType == val2) return;
    	if (val3 >= 0 && eType == val3) return;
    	throw new IllegalArgumentException("SDO_ETYPE "+ eType +" is not supported when reading a " + geomType);
    }
    
    private static void checkInterpretation(int interpretation, int val1, String geomType) {
    	checkInterpretation(interpretation, val1, -1, geomType);
    }
    
    private static void checkInterpretation(int interpretation, int val1, int val2, String geomType) {
		if (interpretation == val1) return;
	   	if (val2 >= 0 && interpretation == val2) return;
	   	errorInterpretation(interpretation, geomType);
    }

  	private static void errorInterpretation(int interpretation, String geomType) {
  		throw new IllegalArgumentException("SDO_INTERPRETATION "+ interpretation +" is not supported when reading a " + geomType);
  	}

    private static void checkOrdinates(OraGeom oraGeom, int elemIndex, String geomType)
    {
      int startOffset = oraGeom.startingOffset(elemIndex);
      int ordLen = oraGeom.ordinateLen();
      checkOrdinates(oraGeom.elemInfo, elemIndex, startOffset, ordLen, geomType);
    }

    private static void checkOrdinates(int[] elemInfo, int elemIndex, int startOffset, int ordLen, String geomType)
    {
      if (startOffset > ordLen)
		    throw new IllegalArgumentException("STARTING_OFFSET " + startOffset + " inconsistent with ORDINATES length " + ordLen
		    		+ " (Element " + elemIndex + " in SDO_ELEM_INFO " + OraGeom.toStringElemInfo(elemInfo) + ")");
    }
    

}
