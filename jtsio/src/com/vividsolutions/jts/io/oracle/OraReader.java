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
import java.util.*;

import oracle.sql.*;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;

/**
 * Reads a {@link Geometry} from an Oracle <code>MDSYS.GEOMETRY</code> <code>STRUCT</code> object.
 *
 * A {@link GeometryFactory} may be provided, otherwise a default one will be used.
 * The provided GeometryFactory will be used.
 * <p>
 * The Geometry SRID field is populated from the input Geometry.
 * <p>
 * If a {@link PrecisionModel} is supplied it is the callers's responsibility
 * to ensure that it matches the precision of the incoming data.
 * If a lower precision for the data is required, a subsequent
 * process must be run on the data to reduce its precision.
 * <p>
 * To use this class a suitable Oracle JDBC JAR must be present in the classpath.
 * 
 * <h3>LIMITATIONS</h3>
 * <ul>
 * <li>4-dimensional (XYZM) Oracle geometries are not supported
 * <li>Oracle geometries with a GTYPE of <code>43xx</code> (XYMZ) are not supported.
 * <li>The number of dimension read is limited by the number supported
 * by the provided {@link CoordinateSequenceFactory}
 * </ul>
 *
 * @author David Zwiers, Vivid Solutions.
 * @author Martin Davis
 */
public class OraReader {
	
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
	 * Creates a JTS {@link Geometry} representing the MDSYS.GEOMETRY
	 * provided in the STRUCT. The type of geometry created depends on the input data,
	 * since the Geometry type is specified within the STRUCT.
	 * The SRID of the created geometry is set to be the same as the input SRID.
	 *
	 * @param struct The MDSYS.GEOMETRY Object to decode
	 * @return A JTS Geometry if one could be created, null otherwise
	 * 
	 * @throws SQLException if a read error occurs while accessing the struct
     * @throws IllegalArgumentException when an encoding error or unsupported geometry type is found
	 */
	public Geometry read(STRUCT struct) throws SQLException 
	{
		// Return null for null input
		if (struct == null)
			return null;

		Datum data[] = struct.getOracleAttributes();
		int gType = OraUtil.toInteger(data[0], 0);
		int SRID = OraUtil.toInteger(data[1], OraSDO.SRID_NULL);
		double point[] = OraUtil.toDoubleArray((STRUCT) data[2], Double.NaN);
		int elemInfo[] = OraUtil.toIntArray((ARRAY) data[3], 0);
		double ordinates[] = OraUtil.toDoubleArray((ARRAY) data[4], Double.NaN);
		
		/*
		 // MD - creating new GFs is bad practice, so is removed 
		GeometryFactory gf = geometryFactory;
		if(geometryFactory.getSRID() != SRID){
			// clone it and use the geom's srid
			gf = new GeometryFactory(geometryFactory.getPrecisionModel(),SRID,geometryFactory.getCoordinateSequenceFactory());
		}
		 */
		Geometry geom = create(geometryFactory, gType, point, elemInfo, ordinates);
		
		// Set SRID of created Geometry to be the same as input (regardless of geomFactory SRID)
		if (geom != null)
			geom.setSRID(SRID);
		return geom;
	}

	/**
   * Decode geometry from provided SDO encoded information.
   *
   * <p></p>
   *
   * @param gf Used to construct returned Geometry
   * @param gType SDO_GTEMPLATE represents dimension, LRS, and geometry type
   * @param point
   * @param elemInfo
   * @param ordinates
   *
   * @return Geometry as encoded
   * @throws IllegalArgumentException when an encoding error or unsupported geometry type is found
   */
	Geometry create(GeometryFactory gf, int gType,
        double[] point, int[] elemInfo, double[] ordinates) {

        int ordDim = OraSDO.gTypeDim(gType);
        if (ordDim < 2) {
        	throw new IllegalArgumentException("Dimension D = " + ordDim + " is not supported by JTS. " +
        			"Either specify a valid dimension or use Oracle Locator Version 9i or later");
        }
        int lrsDim = OraSDO.gTypeMeasureDim(gType);

        // The actual dimension to read is the smaller of the explicit dimension and the input dimension
        int outputDim = ordDim;
        if(dimension != NULL_DIMENSION){
        	outputDim = dimension;
        }
        
        int geomType = OraSDO.gTypeGeomType(gType); 

        CoordinateSequence coords = null;

        if (lrsDim == 0 && geomType == OraSDO.GEOM_TYPE.POINT && point != null && elemInfo == null) {
            // Single Point Type Optimization
            coords = coordinates(gf.getCoordinateSequenceFactory(), outputDim, ordDim, lrsDim, geomType, point);
            elemInfo = new int[] { 1, OraSDO.ETYPE.POINT, 1 };
        } else {
            coords = coordinates(gf.getCoordinateSequenceFactory(), outputDim,  ordDim, lrsDim, geomType, ordinates);
        }

        switch (geomType) {
        case OraSDO.GEOM_TYPE.POINT:
            return createPoint(gf, ordDim, lrsDim, elemInfo, 0, coords);

        case OraSDO.GEOM_TYPE.LINE:
            return createLine(gf, ordDim, lrsDim, elemInfo, 0, coords);

        case OraSDO.GEOM_TYPE.POLYGON:
            return createPolygon(gf, ordDim, lrsDim, elemInfo, 0, coords);

        case OraSDO.GEOM_TYPE.MULTIPOINT:
            return createMultiPoint(gf, ordDim, lrsDim, elemInfo, 0, coords);

        case OraSDO.GEOM_TYPE.MULTILINE:
            return createMultiLine(gf, ordDim, lrsDim, elemInfo, 0, coords, -1);

        case OraSDO.GEOM_TYPE.MULTIPOLYGON:
            return createMultiPolygon(gf, ordDim, lrsDim, elemInfo, 0, coords, -1);

        case OraSDO.GEOM_TYPE.COLLECTION:
            return createCollection(gf, ordDim, lrsDim, elemInfo, 0, coords,-1);

        default:
        	throw new IllegalArgumentException("GTYPE " + gType + " is not supported");
        }
    }

	/**
     * Construct coordinate sequence as described by the GTYPE.
     *
     * The number of ordinates read per coordinate is given by outputDim.
     * The number of ordinates for each input point is given by ordDim.
     * The ordinate array length must be a multiple of this value.

     * In the special case of GTYPE=2001 and an ordinate array of length 3,
     * a single Coordinate is created, rather than throwing an error.
     *
     * @param csFactory CoordinateSequenceFactory used to encode ordinates for JTS
     * @param outputDim dimension of target coordinate sequence
     * @param ordDim dimension of input coordinates
     * @param lrsDim the index of the dimension containing the measure value (if any)
     * @param geomType the geometry type of the input
     * @param ordinates
     *
     * @return a coordinate sequence representing all the read ordinates
     *
     * @throws IllegalArgumentException if the ordinate array has an invalid length
     */
    private CoordinateSequence coordinates(CoordinateSequenceFactory csFactory,
        int outputDim, int ordDim, int lrsDim, int geomType, double[] ordinates) 
    {
    	// handle empty case
        if ((ordinates == null) || (ordinates.length == 0)) {
            return csFactory.create(new Coordinate[0]);
        }

        //      POINT_TYPE Special Case
        //
        if ((ordDim == 2) && (lrsDim == 0) && (geomType == OraSDO.GEOM_TYPE.POINT) && (ordinates.length == 3)) {
            return csFactory.create(new Coordinate[] {
                    new Coordinate(ordinates[0], ordinates[1], ordinates[2]),
                });
        }

        if ((ordDim == 0 && ordinates.length != 0 ) || (ordDim != 0 && ((ordinates.length % ordDim) != 0))){
            throw new IllegalArgumentException("SDO_GTYPE Dimension " + ordDim
                + "is inconsistent with SDO_ORDINATES length " + ordinates.length);
        }

        int nCoord = (ordDim == 0 ? 0 : ordinates.length / ordDim);

        int csDim = outputDim;
        CoordinateSequence cs = csFactory.create(nCoord, csDim);

        int actualCSDim = cs.getDimension();
        for(int iCoord = 0; iCoord < nCoord; iCoord++){
        	int iOrd = 0;
        	// in the future change this condition to include ignored dimensions from mask array
        	for(; iOrd < actualCSDim && iOrd < ordDim; iOrd++){
        		cs.setOrdinate(iCoord, iOrd, ordinates[iCoord * ordDim + iOrd]);
        		// may not always want to inc. j when we have a mask array
        	}
        	// in the future change this condition to include ignored dimensions from mask array
        	for(int d = iOrd; iOrd < actualCSDim && (iOrd-d) < lrsDim; iOrd++){
        		cs.setOrdinate(iCoord, iOrd, ordinates[iCoord * ordDim + iOrd]);
        		// may not always want to inc. j when we have a mask array
        	}
        }
        return cs;
    }

    /**
     * Create a {@link GeometryCollection} as specified by elemInfo.
     *
     * @param gf Used to construct MultiLineString
     * @param ordDim dimension of input coordinates
     * @param elemIndex Triplet in elemInfo to process as a Polygon
     * @param coords Coordinates to interpret using elemInfo
     * @param numGeom Number of triplets (or -1 for rest)
     *
     * @return GeometryCollection
     *
     * @throws IllegalArgumentException when an encoding error or unsupported geometry type is found
     */
    private GeometryCollection createCollection(GeometryFactory gf, int ordDim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords, int numGeom) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int ordLength = coords.size()*ordDim;

	   	checkOrdinates(elemInfo, elemIndex, sOffset, ordLength, "GeometryCollection");

        int endTriplet = (numGeom != -1) ? elemIndex + numGeom : elemInfo.length / 3 + 1;

        List geomList = new ArrayList();
        boolean cont = true;
        for (int i = elemIndex; cont && i < endTriplet; i++) {
            int etype = OraSDO.eType(elemInfo, i);
            int interpretation = OraSDO.interpretation(elemInfo, i);
            Geometry geom;

            switch (etype) {
            case -1:
                cont = false; // We are at the end of the list - get out of here
                continue;
                
            case OraSDO.ETYPE.POINT:

                if (interpretation == OraSDO.INTERP.POINT) {
                    geom = createPoint(gf, ordDim, lrs, elemInfo, i, coords);
                } else if (interpretation > 1) {
                    geom = createMultiPoint(gf, ordDim, lrs, elemInfo, i, coords);
                } else {
                    throw new IllegalArgumentException(
                        "ETYPE.POINT requires INTERPRETATION >= 1");
                }
                break;

            case OraSDO.ETYPE.LINE:
                geom = createLine(gf, ordDim, lrs, elemInfo, i, coords);
                break;

            case OraSDO.ETYPE.POLYGON:
            case OraSDO.ETYPE.POLYGON_EXTERIOR:
                geom = createPolygon(gf, ordDim, lrs, elemInfo, i, coords);
                i += ((Polygon) geom).getNumInteriorRing();
                break;

            case OraSDO.ETYPE.POLYGON_INTERIOR:
                throw new IllegalArgumentException(
                    "ETYPE 2003 (Polygon Interior) no expected in a GeometryCollection"
                    + "(2003 is used to represent polygon holes, in a 1003 polygon exterior)");

            default:
                throw new IllegalArgumentException("ETYPE " + etype
                    + " not representable as a JTS Geometry."
                    + "(Custom and Compound Straight and Curved Geometries not supported)");
            }

            geomList.add(geom);
        }

        GeometryCollection geoms = gf.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
        return geoms;
    }

    /**
     * Create MultiPolygon as encoded by elemInfo.
     *
     *
     * @param gf Used to construct MultiLineString
     * @param elemInfo Interpretation of coords
     * @param elemIndex Triplet in elemInfo to process as a Polygon
     * @param coords Coordinates to interpret using elemInfo
     * @param numGeom Number of triplets (or -1 for rest)
     *
     * @return MultiPolygon
     */
    private MultiPolygon createMultiPolygon(GeometryFactory gf, int ordDim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords, int numGeom){

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);
        int ordLength = coords.size()*ordDim;

	   	checkOrdinates(elemInfo, elemIndex, sOffset, ordLength, "Polygon");
    	checkETYPE(etype,OraSDO.ETYPE.POLYGON, OraSDO.ETYPE.POLYGON_EXTERIOR, "Polygon");
    	checkIntepretation(interpretation, OraSDO.INTERP.POLYGON, OraSDO.INTERP.RECTANGLE, "Polygon");

        int endTriplet = (numGeom != -1) ? elemIndex + numGeom : (elemInfo.length / 3) + 1;

        List list = new LinkedList();
        boolean cont = true;

        for (int i = elemIndex; cont && i < endTriplet && (etype = OraSDO.eType(elemInfo, i)) != -1; i++) {
            if ((etype == OraSDO.ETYPE.POLYGON) || (etype == OraSDO.ETYPE.POLYGON_EXTERIOR)) {
                Polygon poly = createPolygon(gf, ordDim, lrs, elemInfo, i, coords);
                i += poly.getNumInteriorRing(); // skip interior rings
                list.add(poly);
            } else { // not a Polygon - get out here
            	cont = false;
            }
        }

        MultiPolygon polys = gf.createMultiPolygon((Polygon[]) list.toArray(new Polygon[list.size()]));

        return polys;
    }

    /**
     * Create MultiLineString as encoded by elemInfo.
     *
     *
     * @param gf Used to construct MultiLineString
     * @param elemInfo Interpretation of coords
     * @param elemIndex Triplet in elemInfo to process as a Polygon
     * @param coords Coordinates to interpret using elemInfo
     * @param numGeom Number of triplets (or -1 for rest)
     *
     * @return MultiLineString
     */
    private MultiLineString createMultiLine(GeometryFactory gf, int ordDim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords, int numGeom) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);
        int ordLength = coords.size()*ordDim;

	   	checkOrdinates(elemInfo, elemIndex, sOffset, ordLength, "MultiLineString");
		checkETYPE(etype,OraSDO.ETYPE.LINE, "MultiLineString");
		checkIntepretation(interpretation, OraSDO.INTERP.LINESTRING, "MultiLineString");

        int endTriplet = (numGeom != -1) ? (elemIndex + numGeom) : (elemInfo.length / 3);

        List list = new LinkedList();

        boolean cont = true;
        for (int i = elemIndex; cont && i < endTriplet && (etype = OraSDO.eType(elemInfo, i)) != -1 ;i++) {
            if (etype == OraSDO.ETYPE.LINE) {
                list.add(createLine(gf, ordDim, lrs, elemInfo, i, coords));
            } else { // not a LineString - get out of here
                cont = false;
            }
        }

        MultiLineString lines = gf.createMultiLineString((LineString[]) list.toArray(new LineString[list.size()]));

        return lines;
    }

    /**
     * Create MultiPoint as encoded by elemInfo.
     *
     *
     * @param gf Used to construct polygon
     * @param elemInfo Interpretation of coords
     * @param elemIndex Triplet in elemInfo to process as a Polygon
     * @param coords Coordinates to interpret using elemInfo
     *
     * @return MultiPoint
     */
    private MultiPoint createMultiPoint(GeometryFactory gf, int ordDim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);
        int ordLength = coords.size()*ordDim;

    	checkOrdinates(elemInfo, elemIndex, sOffset, ordLength, "MultiPoint");
		checkETYPE(etype,OraSDO.ETYPE.POINT, "MultiPoint");
		// MultiPoints have a unique interpretation code
		if (! (interpretation > 1)){
			errorInterpretation(interpretation, "MultiPoint");
		}
		
        int start = (sOffset - 1) / ordDim;
        int end = start + interpretation;

        MultiPoint points = gf.createMultiPoint(subList(gf.getCoordinateSequenceFactory(), coords, start, end));

        return points;
    }

    /**
     * Create Polygon as encoded.
     *
     * @see OraSDO#interpretation(int[], int)
     *
     * @param gf Used to construct polygon
     * @param elemInfo Interpretation of coords
     * @param elemIndex Triplet in elemInfo to process as a Polygon
     * @param coords Coordinates to interpret using elemInfo
     *
     * @return Polygon as encoded by elemInfo, or null when faced with and
     *         encoding that can not be captured by JTS
     * @throws IllegalArgumentException When faced with an invalid SDO encoding
     */
    private Polygon createPolygon(GeometryFactory gf, int ordDim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);
        int ordLength = coords.size()*ordDim;

    	checkOrdinates(elemInfo, elemIndex, sOffset, ordLength, "Polygon");
    	checkETYPE(etype,OraSDO.ETYPE.POLYGON, OraSDO.ETYPE.POLYGON_EXTERIOR, "Polygon");
    	checkIntepretation(interpretation, OraSDO.INTERP.POLYGON, OraSDO.INTERP.RECTANGLE, "Polygon");

        LinearRing exteriorRing = createLinearRing(gf, ordDim, lrs, elemInfo, elemIndex, coords);

        List rings = new LinkedList();

        boolean cont = true;
        for (int i = elemIndex + 1; cont && (etype = OraSDO.eType(elemInfo, i)) != -1; i++) {
            if (etype == OraSDO.ETYPE.POLYGON_INTERIOR) {
                rings.add(createLinearRing(gf, ordDim, lrs, elemInfo, i, coords));
            } else if (etype == OraSDO.ETYPE.POLYGON) { // need to test Clockwiseness of Ring to see if it is
                                                 // interior or not - (use POLYGON_INTERIOR to avoid pain)

                LinearRing ring = createLinearRing(gf, ordDim, lrs, elemInfo, i, coords);

                if (CGAlgorithms.isCCW(ring.getCoordinates())) { // it is an Interior Hole
                    rings.add(ring);
                } else { // it is the next Polygon! - get out of here
                    cont = false;
                }
            } else { // not a LinearRing - get out of here
                cont = false;
            }
        }

        Polygon poly = gf.createPolygon(exteriorRing, (LinearRing[]) rings.toArray(new LinearRing[rings.size()]));

        return poly;
    }

    /**
     * Create LinearRing for exterior/interior polygon ELEM_INFO triplets.
     *
     * @param gf
     * @param elemInfo
     * @param elemIndex
     * @param coords
     *
     * @return LinearRing
     *
     * @throws IllegalArgumentException If circle, or curve is requested
     */
    private LinearRing createLinearRing(GeometryFactory gf, int ordDim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);
        int ordLength = coords.size()*ordDim;

    	checkOrdinates(elemInfo, elemIndex, sOffset, ordLength, "Polygon");
    	checkETYPE(etype,OraSDO.ETYPE.POLYGON, OraSDO.ETYPE.POLYGON_EXTERIOR,  OraSDO.ETYPE.POLYGON_INTERIOR, "Polygon");
    	checkIntepretation(interpretation, OraSDO.INTERP.POLYGON, OraSDO.INTERP.RECTANGLE, "Polygon");

		int start = (sOffset - 1) / ordDim;
		int eOffset = OraSDO.startingOffset(elemInfo, elemIndex+1); // -1 for end
        int end = (eOffset != -1) ? ((eOffset - 1) / ordDim) : coords.size();

    	LinearRing ring;
        if (interpretation == OraSDO.INTERP.POLYGON) {
            ring = gf.createLinearRing(subList(gf.getCoordinateSequenceFactory(),coords, start,end));
        } 
        else { 
        	// interpretation == OraSDO.INTERP.RECTANGLE
            // rectangle does not maintain measures
            CoordinateSequence ext = subList(gf.getCoordinateSequenceFactory(),coords, start,end);
            Coordinate min = ext.getCoordinate(0);
            Coordinate max = ext.getCoordinate(1);
            ring = gf.createLinearRing(new Coordinate[] {
                        min, new Coordinate(max.x, min.y), max,
                        new Coordinate(min.x, max.y), min
                    });
        }

        return ring;
    }

  /**
   * Create LineString as encoded.
   * 
   * @param gf
   * @param elemInfo
   * @param coords
   * 
   * @return LineString
   * 
   * @throws IllegalArgumentException
   *           If asked to create a curve
   */
  private LineString createLine(GeometryFactory gf, int ordDim, int lrs,
      int[] elemInfo, int elemIndex, CoordinateSequence coords)
  {

    int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
    int etype = OraSDO.eType(elemInfo, elemIndex);
    int interpretation = OraSDO.interpretation(elemInfo, elemIndex);
    int ordLength = coords.size() * ordDim;

	checkOrdinates(elemInfo, elemIndex, sOffset, ordLength, "LineString");
	checkETYPE(etype,OraSDO.ETYPE.LINE, "LineString");
	checkIntepretation(interpretation, OraSDO.INTERP.LINESTRING, "LineString");
	
	/*
    if (etype != OraSDO.ETYPE.LINE)
		throw new IllegalArgumentException("SDO_ETYPE "+ etype +" is not supported when reading a LineString");

    if (interpretation != OraSDO.INTERP.LINESTRING) {
      throw new IllegalArgumentException(
          "SDO_INTERPRETATION "
              + interpretation
              + " not supported when reading LineStrings. "
              + "Only straight line edges (ELEM_INFO INTERPRETATION=1) are supported");
    }
*/
	
    int start = (sOffset - 1) / ordDim;
    int eOffset = OraSDO.startingOffset(elemInfo, elemIndex + 1); // -1 for end
    int end = (eOffset != -1) ? ((eOffset - 1) / ordDim) : coords.size();

    LineString line = gf.createLineString(subList(
        gf.getCoordinateSequenceFactory(), coords, start, end));

    return line;
  }

    /**
     * Create Point as encoded.
     *
     * @param gf
     * @param ordDim The number of Dimensions
     * @param elemInfo
     * @param elemIndex
     * @param coords
     *
     * @return Point
     */
    private Point createPoint(GeometryFactory gf, int ordDim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords) {
    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);
        int ordLength = coords.size() * ordDim;

		checkOrdinates(elemInfo, elemIndex, sOffset, ordLength, "Point");
		checkETYPE(etype,OraSDO.ETYPE.POINT, "Point");
		checkIntepretation(interpretation, OraSDO.INTERP.POINT, "Point");

		int start = (sOffset - 1) / ordDim;
		int eOffset = OraSDO.startingOffset(elemInfo, elemIndex+1); // -1 for end

		Point point = null;
        if ((sOffset == 1) && (eOffset == -1)) {
            // Use all Coordinates
        	point = gf.createPoint( coords);
        }else{
	        int end = (eOffset != -1) ? ((eOffset - 1) / ordDim) : coords.size();
	        point = gf.createPoint(subList(gf.getCoordinateSequenceFactory(),coords,start,end));
        }
        return point;
    }



	/**
     * Version of List.subList() that returns a CoordinateSequence.
     *
     * <p>
     * Returns from start (inclusive) to end (exlusive):
     * </p>
     *
     * @param factory Manages CoordinateSequences for JTS
     * @param coords coords to sublist
     * @param start starting offset
     * @param end upper bound of sublist
     *
     * @return a CoordinateSequence
     */
    private CoordinateSequence subList(CoordinateSequenceFactory factory, CoordinateSequence coords, int start, int end) {
        if ((start == 0) && (end == coords.size())) {
            return coords;
        }
        if (coords instanceof List) {
            List sublist = ((List) coords).subList(start, end);
            if (sublist instanceof CoordinateSequence) {
                return (CoordinateSequence) sublist;
            }
        }
        CoordinateList list = new CoordinateList(coords.toCoordinateArray());
        Coordinate[] array = new Coordinate[end - start];
        int index = 0;
        for (Iterator i = list.subList(start, end).iterator(); i.hasNext(); index++) {
            array[index] = (Coordinate) i.next();
        }
        return factory.create(array);
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
    
    private static void checkIntepretation(int interpretation, int val1, String geomType) {
    	checkIntepretation(interpretation, val1, -1, geomType);
    }
    
    private static void checkIntepretation(int interpretation, int val1, int val2, String geomType) {
		if (interpretation == val1) return;
	   	if (val2 >= 0 && interpretation == val2) return;
	   	errorInterpretation(interpretation, geomType);
	}

	private static void errorInterpretation(int interpretation, String geomType) {
		throw new IllegalArgumentException("SDO_INTERPRETATION "+ interpretation +" is not supported when reading a " + geomType);
	}

    private static void checkOrdinates(int[] elemInfo, int index, int startOffset, int ordLen, String geomType)
    {
		if (startOffset > ordLen)
		    throw new IllegalArgumentException("STARTING_OFFSET " + startOffset + " inconsistent with ORDINATES length " + ordLen
		    		+ " (Element " + index + " in SDO_ELEM_INFO " + OraGeom.toStringElemInfo(elemInfo) + ")");
    }
    

}
