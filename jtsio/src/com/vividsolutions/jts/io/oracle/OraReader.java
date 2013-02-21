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
 * Reads a {@link Geometry} from an Oracle <tt>MDSYS.GEOMETRY</tt> STRUCT object.
 *
 * A {@link GeometryFactory} may be provided, otherwise a default one will be used.
 * The provided GeometryFactory will be used, with the exception of the SRID field.
 * This is populated from the input Geometry.
 * <p>
 * If a {@link PrecisionModel} is supplied it is the callers's responsibility
 * to ensure that it matches the precision of the incoming data.
 * If a lower precision for the data is required, a subsequent
 * process must be run on the data to reduce its precision.
 * <p>
 * To use this class a suitable Oracle JDBC driver JAR must be present.
 *
 * @version 9i
 * @author David Zwiers, Vivid Solutions.
 * @author Martin Davis
 */
public class OraReader {
	public static final int NULL_DIMENSION = -1;
	
	private GeometryFactory geometryFactory;

	private int dimension = -1;

	/**
	 * Creates a new reader, with a default GeometryFactory.
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
	 * coordinates are <b>not</b> made precise.
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
	 * This method will attempt to create a JTS Geometry for the MDSYS.GEOMETRY
	 * provided. The Type of geometry created depends on the input data,
	 * since the Geometry type is specified within the STRUCT.
	 *
	 * @param struct The MDSYS.GEOMETRY Object to decode
	 * @return A JTS Geometry if one could be created, null otherwise
	 * @throws SQLException if a read error occurs while accessing the struct
	 */
	public Geometry read(STRUCT struct) throws SQLException {

		// Note Returning null for null Datum
		if (struct == null)
			return null;

		Datum data[] = struct.getOracleAttributes();
		int gType = asInteger(data[0], 0);
		int SRID = asInteger(data[1], OraSDO.SRID_NULL);
		double point[] = asDoubleArray((STRUCT) data[2], Double.NaN);
		int elemInfo[] = asIntArray((ARRAY) data[3], 0);
		double ordinates[] = asDoubleArray((ARRAY) data[4], Double.NaN);
		
		/*
		 // MD - creating new GFs is bad practice, so is removed 
		GeometryFactory gf = geometryFactory;
		if(geometryFactory.getSRID() != SRID){
			// clone it and use the geom's srid
			gf = new GeometryFactory(geometryFactory.getPrecisionModel(),SRID,geometryFactory.getCoordinateSequenceFactory());
		}
		 */
		
		Geometry geom = create(geometryFactory, gType, point, elemInfo, ordinates);
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
   */
	Geometry create(GeometryFactory gf, int gType,
        double[] point, int[] elemInfo, double[] ordinates) {

        int lrsDim = OraSDO.gTypeMeasureDim(gType);

        // find the dimension: represented by the smaller of the two dimensions
        int dim = 0;
        if(dimension != NULL_DIMENSION){
        	dim = dimension;
        }
        else {
          int gTypeDim = OraSDO.gTypeDim(gType);
          dim = gTypeDim;
          // MD - Jan 2013 - this no longer works, and appears wrong anyway
        	//dim = Math.min(gType/1000,gf.getCoordinateSequenceFactory().create(0,0).getDimension()) ;
        }

        if(dim < 2){
        	throw new IllegalArgumentException("Dimension D:" + dim + " is not valid for JTS. " +
        			"Either specify a dimension or use Oracle Locator Version 9i or later");
        }
        int geomType = OraSDO.gTypeGeomType(gType); 

        CoordinateSequence coords = null;

        if (lrsDim == 0 && geomType == OraSDO.GEOM_TYPE.POINT && point != null && elemInfo == null) {
            // Single Point Type Optimization
            coords = coordinates(gf.getCoordinateSequenceFactory(), dim, lrsDim, geomType, point);
            elemInfo = new int[] { 1, OraSDO.ETYPE.POINT, 1 };
        } else {
            coords = coordinates(gf.getCoordinateSequenceFactory(), dim, lrsDim, geomType, ordinates);
        }

        switch (geomType) {
        case OraSDO.GEOM_TYPE.POINT:
            return createPoint(gf, dim, lrsDim, elemInfo, 0, coords);

        case OraSDO.GEOM_TYPE.LINE:
            return createLine(gf, dim, lrsDim, elemInfo, 0, coords);

        case OraSDO.GEOM_TYPE.POLYGON:
            return createPolygon(gf, dim, lrsDim, elemInfo, 0, coords);

        case OraSDO.GEOM_TYPE.MULTIPOINT:
            return createMultiPoint(gf, dim, lrsDim, elemInfo, 0, coords);

        case OraSDO.GEOM_TYPE.MULTILINE:
            return createMultiLine(gf, dim, lrsDim, elemInfo, 0, coords, -1);

        case OraSDO.GEOM_TYPE.MULTIPOLYGON:
            return createMultiPolygon(gf, dim, lrsDim, elemInfo, 0, coords, -1);

        case OraSDO.GEOM_TYPE.COLLECTION:
            return createCollection(gf, dim, lrsDim, elemInfo, 0, coords,-1);

        default:
            return null;
        }
    }

	/**
     * Construct CoordinateList as described by GTYPE.
     *
     * The number of ordinates per coordinate is given by dim.
     * The number of ordinates should be a multiple of this value.

     * In the Special case of GTYPE 2001 and a three ordinates are interpreted
     * as a single Coordinate rather than an error.
     *
     * @param f CoordinateSequenceFactory used to encode ordiantes for JTS
     * @param ordinates
     *
     * @return protected
     *
     * @throws IllegalArgumentException if the ordinate array has an invalid length
     */
    private CoordinateSequence coordinates(CoordinateSequenceFactory f,
        int dim, int lrsDim, int geomType, double[] ordinates) 
    {
    	// handle empty case
        if ((ordinates == null) || (ordinates.length == 0)) {
            return f.create(new Coordinate[0]);
        }

        //      POINT_TYPE Special Case
        //
        if ((dim == 2) && (lrsDim == 0) && (geomType == OraSDO.GEOM_TYPE.POINT) && (ordinates.length == 3)) {
            return f.create(new Coordinate[] {
                    new Coordinate(ordinates[0], ordinates[1], ordinates[2]),
                });
        }

        //int len = dim;

        if ((dim == 0 && ordinates.length != 0 ) || (dim != 0 && ((ordinates.length % dim) != 0))){
            throw new IllegalArgumentException("Dimension D:" + dim + " and L:"
                + lrsDim + " denote Coordinates " + "of " + dim
                + " ordinates. This cannot be resolved with"
                + "an ordinate array of length " + ordinates.length);
        }

        int nCoord = (dim == 0 ? 0 : ordinates.length / dim);

        int csDim = dim;
        CoordinateSequence cs = f.create(nCoord, csDim);

        int actualCSDim = cs.getDimension();
        for(int i=0; i < nCoord; i++){
        	int j=0;
        	// in the future change this condition to include ignored dimensions from mask array
        	for(; j < actualCSDim && j < dim; j++){
        		cs.setOrdinate(i,j,ordinates[i * dim + j]);
        		// may not always want to inc. j when we have a mask array
        	}
        	// in the future change this condition to include ignored dimensions from mask array
        	for(int d = j; j < actualCSDim && (j-d) < lrsDim; j++){
        		cs.setOrdinate(i,j,ordinates[i * dim + j]);
        		// may not always want to inc. j when we have a mask array
        	}
        }
        return cs;
    }

    /**
     * Create MultiGeometry as specified by elemInfo.
     *
     * @param gf Used to construct MultiLineString
     * @param elemInfo Interpretation of coords
     * @param elemIndex Triplet in elemInfo to process as a Polygon
     * @param coords Coordinates to interpret using elemInfo
     * @param numGeom Number of triplets (or -1 for rest)
     *
     * @return GeometryCollection
     *
     * @throws IllegalArgumentException when faced with an encoding error
     */
    private GeometryCollection createCollection(GeometryFactory gf, int dim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords, int numGeom) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);

        int length = coords.size()*dim;

		if (!(sOffset <= length))
		    throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET "+sOffset+" inconsistent with ORDINATES length "+coords.size());

        int endTriplet = (numGeom != -1) ? elemIndex + numGeom : elemInfo.length / 3 + 1;

        List list = new LinkedList();
        int etype;
        int interpretation;
        Geometry geom;

        boolean cont = true;
        for (int i = elemIndex; cont && i < endTriplet; i++) {
            etype = OraSDO.eType(elemInfo, i);
            interpretation = OraSDO.interpretation(elemInfo, i);

            switch (etype) {
            case -1:
                cont = false; // We are at the end of the list - get out of here
                continue;
                
            case OraSDO.ETYPE.POINT:

                if (interpretation == OraSDO.INTERP.POINT) {
                    geom = createPoint(gf, dim, lrs, elemInfo, i, coords);
                } else if (interpretation > 1) {
                    geom = createMultiPoint(gf, dim, lrs, elemInfo, i, coords);
                } else {
                    throw new IllegalArgumentException(
                        "ETYPE.POINT requires INTERPRETATION >= 1");
                }
                break;

            case OraSDO.ETYPE.LINE:
                geom = createLine(gf, dim, lrs, elemInfo, i, coords);
                break;

            case OraSDO.ETYPE.POLYGON:
            case OraSDO.ETYPE.POLYGON_EXTERIOR:
                geom = createPolygon(gf, dim, lrs, elemInfo, i, coords);
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

            list.add(geom);
        }

        GeometryCollection geoms = gf.createGeometryCollection((Geometry[]) list.toArray(new Geometry[list.size()]));

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
    private MultiPolygon createMultiPolygon(GeometryFactory gf, int dim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords, int numGeom){

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);

        int length = coords.size()*dim;

		if (!(sOffset >= 1) || !(sOffset <= length))
		    throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET "+sOffset+" inconsistent with ORDINATES length "+coords.size());
		if(!(etype == OraSDO.ETYPE.POLYGON) && !(etype == OraSDO.ETYPE.POLYGON_EXTERIOR))
		    throw new IllegalArgumentException("ETYPE "+etype+" inconsistent with expected POLYGON or POLYGON_EXTERIOR");
		if (! (interpretation == OraSDO.INTERP.POLYGON || interpretation == OraSDO.INTERP.RECTANGLE)) {
			return null;
		}

        int endTriplet = (numGeom != -1) ? elemIndex + numGeom : (elemInfo.length / 3) + 1;

        List list = new LinkedList();
        boolean cont = true;

        for (int i = elemIndex; cont && i < endTriplet && (etype = OraSDO.eType(elemInfo, i)) != -1; i++) {
            if ((etype == OraSDO.ETYPE.POLYGON) || (etype == OraSDO.ETYPE.POLYGON_EXTERIOR)) {
                Polygon poly = createPolygon(gf, dim, lrs, elemInfo, i, coords);
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
    private MultiLineString createMultiLine(GeometryFactory gf, int dim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords, int numGeom) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);

        int length = coords.size()*dim;

		if (!(sOffset >= 1) || !(sOffset <= length))
		    throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET "+sOffset+" inconsistent with ORDINATES length "+coords.size());
		if(!(etype == OraSDO.ETYPE.LINE))
		    throw new IllegalArgumentException("ETYPE "+etype+" inconsistent with expected LINE");
		if (!(interpretation == OraSDO.INTERP.LINESTRING)){
            // we cannot represent INTERPRETATION > 1
			return null;
		}

        int endTriplet = (numGeom != -1) ? (elemIndex + numGeom) : (elemInfo.length / 3);

        List list = new LinkedList();

        boolean cont = true;
        for (int i = elemIndex; cont && i < endTriplet && (etype = OraSDO.eType(elemInfo, i)) != -1 ;i++) {
            if (etype == OraSDO.ETYPE.LINE) {
                list.add(createLine(gf, dim, lrs, elemInfo, i, coords));
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
    private MultiPoint createMultiPoint(GeometryFactory gf, int dim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);

		if (!(sOffset >= 1) || !(sOffset <= coords.size()))
		    throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET "+sOffset+" inconsistent with ORDINATES length "+coords.size());
		if(!(etype == OraSDO.ETYPE.POINT))
		    throw new IllegalArgumentException("ETYPE "+etype+" inconsistent with expected POINT");
		if (!(interpretation > OraSDO.INTERP.POINT)){
			return null;
		}

        int len = dim;

        int start = (sOffset - 1) / len;
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
    private Polygon createPolygon(GeometryFactory gf, int dim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);

        if( !(1 <= sOffset && sOffset <= (coords.size() * dim))){
            throw new IllegalArgumentException(
                    "ELEM_INFO STARTING_OFFSET "+sOffset+
                    "inconsistent with COORDINATES length "+(coords.size() * dim) );
        }

		if(!(etype == OraSDO.ETYPE.POLYGON) && !(etype == OraSDO.ETYPE.POLYGON_EXTERIOR)){
			throw new IllegalArgumentException("ETYPE "+etype+" inconsistent with expected POLYGON or POLYGON_EXTERIOR");
		}
		if (! (interpretation == OraSDO.INTERP.POLYGON || interpretation == OraSDO.INTERP.RECTANGLE)) {
			return null;
		}

        LinearRing exteriorRing = createLinearRing(gf, dim, lrs, elemInfo, elemIndex, coords);

        List rings = new LinkedList();

        boolean cont = true;
        for (int i = elemIndex + 1; cont && (etype = OraSDO.eType(elemInfo, i)) != -1; i++) {
            if (etype == OraSDO.ETYPE.POLYGON_INTERIOR) {
                rings.add(createLinearRing(gf, dim, lrs, elemInfo, i, coords));
            } else if (etype == OraSDO.ETYPE.POLYGON) { // need to test Clockwiseness of Ring to see if it is
                                                 // interior or not - (use POLYGON_INTERIOR to avoid pain)

                LinearRing ring = createLinearRing(gf, dim, lrs, elemInfo, i, coords);

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
    private LinearRing createLinearRing(GeometryFactory gf, int dim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);
        int length = coords.size()*dim;

		if (!(sOffset <= length))
		    throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET "+sOffset+" inconsistent with ORDINATES length "+coords.size());
		if(!(etype == OraSDO.ETYPE.POLYGON) && !(etype == OraSDO.ETYPE.POLYGON_EXTERIOR) && !(etype == OraSDO.ETYPE.POLYGON_INTERIOR)){
		    throw new IllegalArgumentException("ETYPE "+etype+" inconsistent with expected POLYGON, POLYGON_EXTERIOR or POLYGON_INTERIOR");
		}
		if (! (interpretation == OraSDO.INTERP.POLYGON || interpretation == OraSDO.INTERP.RECTANGLE)) {
			return null;
		}
        LinearRing ring;


		int len = dim;
		int start = (sOffset - 1) / len;
		int eOffset = OraSDO.startingOffset(elemInfo, elemIndex+1); // -1 for end
        int end = (eOffset != -1) ? ((eOffset - 1) / len) : coords.size();

        if (interpretation == OraSDO.INTERP.POLYGON) {
            ring = gf.createLinearRing(subList(gf.getCoordinateSequenceFactory(),coords, start,end));
        } else { // interpretation == OraSDO.INTERP.RECTANGLE
            // rectangle does not maintain measures
            //
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
     * @throws IllegalArgumentException If asked to create a curve
     */
    private LineString createLine(GeometryFactory gf, int dim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords) {

    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);

		if (etype != OraSDO.ETYPE.LINE)
			return null;

        if (interpretation != OraSDO.INTERP.LINESTRING) {
            throw new IllegalArgumentException("ELEM_INFO INTERPRETATION "
                + interpretation + " not supported"
                + "by JTS LineString.  Straight edges"
                + "( ELEM_INFO INTERPRETATION 1) is supported");
        }

		int len = dim;
		int start = (sOffset - 1) / len;
		int eOffset = OraSDO.startingOffset(elemInfo, elemIndex+1); // -1 for end
        int end = (eOffset != -1) ? ((eOffset - 1) / len) : coords.size();


        LineString line = gf.createLineString(subList(gf.getCoordinateSequenceFactory(),coords, start,end));

        return line;
    }

    /**
     * Create Point as encoded.
     *
     * @param gf
     * @param dim The number of Dimensions
     * @param elemInfo
     * @param elemIndex
     * @param coords
     *
     * @return Point
     */
    private Point createPoint(GeometryFactory gf, int dim, int lrs, int[] elemInfo, int elemIndex, CoordinateSequence coords) {
    	int sOffset = OraSDO.startingOffset(elemInfo, elemIndex);
        int etype = OraSDO.eType(elemInfo, elemIndex);
        int interpretation = OraSDO.interpretation(elemInfo, elemIndex);

		if (!(sOffset >= 1) || !(sOffset <= coords.size() * dim))
		    throw new IllegalArgumentException("ELEM_INFO STARTING_OFFSET "+sOffset+" inconsistent with ORDINATES length "+coords.size());
		if (etype != OraSDO.ETYPE.POINT)
		    throw new IllegalArgumentException("ETYPE "+etype+" inconsistent with expected POINT");
		if (interpretation != OraSDO.INTERP.POINT){
			return null;
		}

		int len = dim;
		int start = (sOffset - 1) / len;
		int eOffset = OraSDO.startingOffset(elemInfo, elemIndex+1); // -1 for end

		Point point = null;
        if ((sOffset == 1) && (eOffset == -1)) {
            // Use all Coordinates
        	point = gf.createPoint( coords);
        }else{
	        int end = (eOffset != -1) ? ((eOffset - 1) / len) : coords.size();
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

    /** Presents datum as an int */
	static int asInteger(Datum datum, final int DEFAULT)
			throws SQLException {
		if (datum == null)
			return DEFAULT;
		return ((NUMBER) datum).intValue();
	}

	/** Presents datum as a double */
	static double asDouble(Datum datum, final double DEFAULT) {
		if (datum == null)
			return DEFAULT;
		return ((NUMBER) datum).doubleValue();
	}

	/** Presents struct as a double[] */
	private static double[] asDoubleArray(STRUCT struct, final double DEFAULT)
			throws SQLException {
		if (struct == null)
			return null;
		return asDoubleArray(struct.getOracleAttributes(), DEFAULT);
	}

	/** Presents array as a double[] */
	private static  double[] asDoubleArray(ARRAY array, final double DEFAULT)
			throws SQLException {
		if (array == null)
			return null;
		if (DEFAULT == 0)
			return array.getDoubleArray();

		return asDoubleArray(array.getOracleArray(), DEFAULT);
	}

	/** Presents Datum[] as a int[] */
	static int[] asIntArray(Datum data[], final int DEFAULT)
			throws SQLException {
		if (data == null)
			return null;
		int array[] = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			array[i] = OraReader.asInteger(data[i], DEFAULT);
		}
		return array;
	}

	static int[] asIntArray(ARRAY array, int DEFAULT)
			throws SQLException {
		if (array == null)
			return null;
		if (DEFAULT == 0)
			return array.getIntArray();
	
		return asIntArray(array.getOracleArray(), DEFAULT);
	}

	/** Presents Datum[] as a double[] */
	static double[] asDoubleArray(Datum data[], final double DEFAULT) {
		if (data == null)
			return null;
		double array[] = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			array[i] = OraReader.asDouble(data[i], DEFAULT);
		}
		return array;
	}

}
