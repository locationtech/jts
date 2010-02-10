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

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.*;

import oracle.jdbc.OracleConnection;
import oracle.sql.*;

/**
 * 
 * Translates a JTS Geometry into an Oracle STRUCT representing an MDSYS.GEOMETRY object. 
 * 
 * A connection to an oracle instance with access to the definition of the MDSYS.GEOMETRY 
 * object is required by the oracle driver.
 * 
 * @version 9i
 * @author David Zwiers, Vivid Solutions.
 */
public class OraWriter {
	private OracleConnection connection;
	private int dimension = 2;
	private int srid = Constants.SRID_NULL;
	
	private String DATATYPE = "MDSYS.SDO_GEOMETRY";
	
	/**
	 * Initialize the Oracle MDSYS.GEOMETRY Encoder with a valid oracle connection. 
	 * 
	 * The connection should have sufficient priveledges to view the description of the MDSYS.GEOMETRY type.
	 * 
	 * The dimension is set to 2
	 * 
	 * @param con
	 */
	public OraWriter(OracleConnection con){
		this.connection = con;
	}
	
	/**
	 * Initialize the Oracle MDSYS.GEOMETRY Encoder with a valid oracle connection. 
	 * 
	 * The connection should have sufficient priveledges to view the description of the MDSYS.GEOMETRY type.
	 * 
	 * @param con
	 * @param dimension 
	 */
	public OraWriter(OracleConnection con, int dimension){
		this.connection = con;
		this.dimension = dimension;
	}
	
	/**
	 * Provides the oppotunity to force all geometries written using this writter to be written using the 
	 * specified srid. This is useful in two cases: 1) when you do not want the geometry's srid to be 
	 * over-written or 2) when you want to ensure an entire layer is always written using a constant srid.
	 * 
	 * @param srid
	 */
	public void setSRID(int srid){
		this.srid = srid;
	}

	/**
	 * This routine will translate the JTS Geometry into an Oracle MDSYS.GEOMETRY STRUCT.
	 * 
	 * Although invalid geometries may be encoded, and inserted into an Oracle DB, this is 
	 * not recomended. It is the responsibility of the user to ensure the geometry is valid 
	 * prior to calling this method. The user should also ensure the the geometry's SRID 
	 * field contains the correct value, if an SRID is desired. An incorrect SRID value may 
	 * cause index exceptions during an insert or update. 
	 * 
	 * When a null Geometry is passed in, a non-null, empty STRUCT is returned. Therefore, 
	 * inserting the the result of calling this method directly into a table will never result 
	 * in null insertions. 
	 * (March 2006)
	 * 
	 * To pass a NULL Geometry into an oracle geometry parameter using jdbc, use 
	 * java.sql.CallableStatement.setNull(index,java.sql.Types.STRUCT,"MDSYS.SDO_GEOMETRY")
	 * (April 2006)
	 * 
	 * @param geom JTS Geometry to encode
	 * @return Oracle MDSYS.GEOMETRY STRUCT
	 * @throws SQLException 
	 */
	public STRUCT write(Geometry geom) throws SQLException{
		
		// this line may be problematic ... for v9i and later 
		// need to revisit.
		
		// was this ... does not work for 9i
//		if( geom == null) return toSTRUCT( null, DATATYPE );
		
		//works fro 9i
		if( geom == null) return toSTRUCT( new Datum[5], DATATYPE );
		
		// does not work for 9i
//		if( geom == null) return null;
		
		//empty geom
		if( geom.isEmpty() || geom.getCoordinate() == null) 
			return toSTRUCT( new Datum[5], DATATYPE );
        
        int gtype = gType( geom);
        NUMBER SDO_GTYPE = new NUMBER( gtype );

        //int srid = geom.getFactory().getSRID();
        int srid = this.srid == Constants.SRID_NULL? geom.getSRID() : this.srid;
        NUMBER SDO_SRID = srid == Constants.SRID_NULL ? null : new NUMBER( srid );
        
        double[] point = point( geom );
        
        STRUCT SDO_POINT;
        
        ARRAY SDO_ELEM_INFO;
        ARRAY SDO_ORDINATES;
        
        if( point == null ){
            int elemInfo[] = elemInfo( geom , gtype);
            
            List list = new ArrayList();
            coordinates(list, geom);
                        
            int dim = gtype / 1000;
            int lrs = (gtype - dim*1000)/100;
            int len = dim+lrs; // size per coordinate
            double[] ordinates = new double[list.size()*len];
            
            int k=0;
            for(int i=0;i<list.size() && k<ordinates.length;i++){
            	int j=0;
            	double[] ords = (double[]) list.get(i);
            	for(;j<len && j<ords.length;j++){
            		ordinates[k++] = ords[j];
            	}
            	for(;j<len;j++){ // mostly safety
            		ordinates[k++] = Double.NaN;
            	}
            }
            
            list = null;
            
            SDO_POINT = null;
            SDO_ELEM_INFO = toARRAY( elemInfo, "MDSYS.SDO_ELEM_INFO_ARRAY" );
            SDO_ORDINATES = toARRAY( ordinates, "MDSYS.SDO_ORDINATE_ARRAY" );                        
        }
        else { // Point Optimization
            Datum data[] = new Datum[]{
                toNUMBER( point[0] ),
                toNUMBER( point[1] ),
                toNUMBER( point[2] ),
            };
            SDO_POINT = toSTRUCT( data, "MDSYS.SDO_POINT_TYPE"  );
            SDO_ELEM_INFO = null;
            SDO_ORDINATES = null;
        }                
        Datum attributes[] = new Datum[]{
            SDO_GTYPE,
            SDO_SRID,
            SDO_POINT,
            SDO_ELEM_INFO,
            SDO_ORDINATES
        };
        return toSTRUCT( attributes, DATATYPE );      
	}

	/**
     * Encode Geometry as described by GTYPE and ELEM_INFO
     * 
     * @param list Flat list of Double
     * @param geom Geometry 
     *
     * @throws IllegalArgumentException If geometry cannot be encoded
     */
    private void coordinates(List list, Geometry geom) {
        switch (template(geom)) {

        case Constants.SDO_GTEMPLATE.POINT:
            addCoordinates(list, ((Point)geom).getCoordinateSequence());
            return;
        case Constants.SDO_GTEMPLATE.LINE:
            addCoordinates(list, ((LineString)geom).getCoordinateSequence());
            return;
        case Constants.SDO_GTEMPLATE.POLYGON:
            switch (elemInfoInterpretation(geom,Constants.SDO_ETYPE.POLYGON_EXTERIOR)) {
            case 3:
                Envelope e = geom.getEnvelopeInternal();
                list.add(new double[] { e.getMinX(), e.getMinY() });
                list.add(new double[] { e.getMaxX(), e.getMaxY() });
                return;
            case 1:
            	Polygon polygon = (Polygon) geom;
                int holes = polygon.getNumInteriorRing();
                
                // check outer ring's direction
                CoordinateSequence ring = polygon.getExteriorRing().getCoordinateSequence();
                if (!CGAlgorithms.isCCW(ring.toCoordinateArray())) {
                    ring = reverse(polygon.getFactory().getCoordinateSequenceFactory(), ring); 
                }
                addCoordinates(list,ring);

                for (int i = 0; i < holes; i++) {
                	// check inner ring's direction
                	ring = polygon.getInteriorRingN(i).getCoordinateSequence();
                	if (CGAlgorithms.isCCW(ring.toCoordinateArray())) {
                        ring = reverse(polygon.getFactory().getCoordinateSequenceFactory(), ring); 
                    }
                    
                    addCoordinates(list,ring);
                }
                return;
            }
            break; // interpretations 2,4 not supported
        case Constants.SDO_GTEMPLATE.MULTIPOINT:
        case Constants.SDO_GTEMPLATE.MULTILINE:
        case Constants.SDO_GTEMPLATE.MULTIPOLYGON:
        case Constants.SDO_GTEMPLATE.COLLECTION:
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                coordinates(list,geom.getGeometryN(i));
            }
            return;
        }

        throw new IllegalArgumentException("Cannot encode JTS "
            + geom.getGeometryType() + " as "
            + "SDO_ORDINATRES (Limitied to Point, Line, Polygon, "
            + "GeometryCollection, MultiPoint, MultiLineString and MultiPolygon)");
    }

    /**
     * Adds a double array to list.
     * 
     * <p>
     * The double array will contain all the ordinates in the Coordiante
     * sequence.
     * </p>
     *
     * @param list
     * @param sequence
     */
    private static void addCoordinates(List list, CoordinateSequence sequence) {
    	Coordinate coord = null;
        for (int i = 0; i < sequence.size(); i++) {
        	coord = sequence.getCoordinate(i);
        	if(coord.z == Double.NaN)
        		list.add( new double[] { coord.x, coord.y});
        	else
        		list.add( new double[] { coord.x, coord.y, coord.z });
        }
    }

    /**
     * Return SDO_ELEM_INFO array for geometry
     * 
     * <pre><code><b>
     * # Name                Meaning</b>
     * 0 SDO_STARTING_OFFSET Offsets start at one
     * 1 SDO_ETYPE           Describes how ordinates are ordered
     * 2 SDO_INTERPRETATION  SDO_ETYPE: 4, 1005, or 2005
     *                       Number of triplets involved in compound geometry
     *                       
     *                       SDO_ETYPE: 1, 2, 1003, or 2003
     *                       Describes ordering of ordinates in geometry  
     * </code></pre>
     * 
     * <p>
     * For compound elements (SDO_ETYPE values 4 and 5) the last element of one
     * is the first element of the next.
     * </p>
     *
     * @param geom Geometry being represented
     *
     * @return Descriptionof Ordinates representation
     */
	private int[] elemInfo(Geometry geom, int gtype) {
		List list = new LinkedList();

        elemInfo(list, geom, 1, gtype);
        
        int[] array = new int[list.size()];
        int offset = 0;

        for (Iterator i = list.iterator(); i.hasNext(); offset++) {
            array[offset] = ((Number) i.next()).intValue();
        }

        return array;
    }
	
    /**
     * Add to SDO_ELEM_INFO list for geometry and GTYPE.
     *
     * @param elemInfoList List used to gather SDO_ELEM_INFO
     * @param geom Geometry to encode
     * @param sOffSet Starting offset in SDO_ORDINATES
     *
     * @throws IllegalArgumentException If geom cannot be encoded by ElemInfo
     */
    private void elemInfo(List elemInfoList, Geometry geom, int sOffSet, int gtype) {

        switch (gtype - (gtype/100) * 100) { // removes right two digits
        case Constants.SDO_GTEMPLATE.POINT:
            addInt(elemInfoList, sOffSet);
            addInt(elemInfoList, Constants.SDO_ETYPE.POINT);
            addInt(elemInfoList, 1); // INTERPRETATION single point

            return;

        case Constants.SDO_GTEMPLATE.MULTIPOINT:
            MultiPoint points = (MultiPoint) geom;

            addInt(elemInfoList, sOffSet);
            addInt(elemInfoList, Constants.SDO_ETYPE.POINT);
            addInt(elemInfoList, elemInfoInterpretation(points, Constants.SDO_ETYPE.POINT));

            return;

        case Constants.SDO_GTEMPLATE.LINE:
            addInt(elemInfoList, sOffSet);
            addInt(elemInfoList, Constants.SDO_ETYPE.LINE);
            addInt(elemInfoList, 1); // INTERPRETATION straight edges    

            return;

        case Constants.SDO_GTEMPLATE.MULTILINE:
        	MultiLineString lines = (MultiLineString) geom;
            LineString line;
            int offset = sOffSet;
            int dim = gtype/1000;
            int len = dim + (gtype-dim*1000)/100;

            for (int i = 0; i < lines.getNumGeometries(); i++) {
                line = (LineString) lines.getGeometryN(i);
                addInt(elemInfoList, offset);
                addInt(elemInfoList, Constants.SDO_ETYPE.LINE);
                addInt(elemInfoList, 1); // INTERPRETATION straight edges  
                offset += (line.getNumPoints() * len);
            }

            return;

        case Constants.SDO_GTEMPLATE.POLYGON:
        	Polygon polygon = (Polygon)geom;
            int holes = polygon.getNumInteriorRing();

            if (holes == 0) {
                addInt(elemInfoList, sOffSet);
                addInt(elemInfoList, elemInfoEType(polygon));
                addInt(elemInfoList, elemInfoInterpretation(polygon, Constants.SDO_ETYPE.POLYGON_EXTERIOR));
                return;
            }

            dim = gtype/1000;
            len = dim + (gtype-dim*1000)/100;
            offset = sOffSet;
            LineString ring;

            ring = polygon.getExteriorRing();
            addInt(elemInfoList, offset);
            addInt(elemInfoList, elemInfoEType(polygon));
            addInt(elemInfoList, elemInfoInterpretation(polygon, Constants.SDO_ETYPE.POLYGON_EXTERIOR));
            offset += (ring.getNumPoints() * len);

            for (int i = 1; i <= holes; i++) {
                ring = polygon.getInteriorRingN(i - 1);
                addInt(elemInfoList, offset);
                addInt(elemInfoList, Constants.SDO_ETYPE.POLYGON_INTERIOR);
                addInt(elemInfoList, elemInfoInterpretation(ring, Constants.SDO_ETYPE.POLYGON_INTERIOR));
                offset += (ring.getNumPoints() * len);
            }

            return;

        case Constants.SDO_GTEMPLATE.MULTIPOLYGON:
        	MultiPolygon polys = (MultiPolygon) geom;
            Polygon poly;
            offset = sOffSet;

            dim = gtype/1000;
            len = dim + (gtype-dim*1000)/100;

            for (int i = 0; i < polys.getNumGeometries(); i++) {
                poly = (Polygon) polys.getGeometryN(i);
                elemInfo(elemInfoList, poly, offset, gType(poly));
                if( isRectangle( poly )){
                    offset += (2 * len);                
                }
                else {
                    offset += (poly.getNumPoints() * len);                
                }            
            }

            return;

        case Constants.SDO_GTEMPLATE.COLLECTION:
        	GeometryCollection geoms = (GeometryCollection) geom;
            offset = sOffSet;
            dim = gtype/1000;
            len = dim + (gtype-dim*1000)/100;

            for (int i = 0; i < geoms.getNumGeometries(); i++) {
                geom = geoms.getGeometryN(i);
                // MD  20/3/07 modified to provide gType of component geometry
                elemInfo(elemInfoList, geom, offset, gType(geom));
                if( geom instanceof Polygon && isRectangle( (Polygon) geom )){
                    offset += (2 * len);                
                }
                else {
                    offset += (geom.getNumPoints() * len);                
                }                        
            }

            return;
        }

        throw new IllegalArgumentException("Cannot encode JTS "
            + geom.getGeometryType() + " as SDO_ELEM_INFO "
            + "(Limitied to Point, Line, Polygon, GeometryCollection, MultiPoint,"
            + " MultiLineString and MultiPolygon)");
    }

    private void addInt(List list, int i) {
        list.add(new Integer(i));
    }

    /**
     * We need to check if a <code>polygon</code> a rectangle so we can produce
     * the correct encoding.
     * 
     * Rectangles are only supported without a SRID!
     *
     * @param polygon
     *
     * @return <code>true</code> if polygon is SRID==0 and a rectangle
     */
    private boolean isRectangle(Polygon polygon) {
        if (polygon.getFactory().getSRID() != Constants.SRID_NULL) {
            // Rectangles only valid in CAD applications
            // that do not have an SRID system
            //
            return false;
        }

        if (lrs(polygon) != 0) {
            // cannot support LRS on a rectangle
            return false;
        }

        Coordinate[] coords = polygon.getCoordinates();

        if (coords.length != 5) {
            return false;
        }

        if ((coords[0] == null) || (coords[1] == null) || (coords[2] == null)
                || (coords[3] == null)) {
            return false;
        }

        if (!coords[0].equals2D(coords[4])) {
            return false;
        }

        double x1 = coords[0].x;
        double y1 = coords[0].y;
        double x2 = coords[1].x;
        double y2 = coords[1].y;
        double x3 = coords[2].x;
        double y3 = coords[2].y;
        double x4 = coords[3].x;
        double y4 = coords[3].y;

        if ((x1 == x4) && (y1 == y2) && (x3 == x2) && (y3 == y4)) {
            // 1+-----+2
            //  |     |
            // 4+-----+3
            return true;
        }

        if ((x1 == x2) && (y1 == y4) && (x3 == x4) && (y3 == y2)) {
            // 2+-----+3
            //  |     |
            // 1+-----+4
            return true;
        }

        return false;
    }
    /**
     * Produce <code>SDO_ETYPE</code> for geometry description as stored in the
     * <code>SDO_ELEM_INFO</code>.
     * 
     * <p>
     * Describes how Ordinates are ordered:
     * </p>
     * <pre><code><b>
     * Value Elements Meaning</b>
     *    0           Custom Geometry (like spline) 
     *    1  simple   Point (or Points)
     *    2  simple   Line (or Lines)
     *    3           polygon ring of unknown order (discouraged update to 1003 or 2003)
     * 1003  simple   polygon ring (1 exterior counterclockwise order)
     * 2003  simple   polygon ring (2 interior clockwise order)
     *    4  compound series defines a linestring
     *    5  compound series defines a polygon ring of unknown order (discouraged)
     * 1005  compound series defines exterior polygon ring (counterclockwise order)
     * 2005  compound series defines interior polygon ring (clockwise order)
     * </code></pre>
     * 
     * @param geom Geometry being represented
     *
     * @return Descriptionof Ordinates representation
     *
     * @throws IllegalArgumentException
     */
    private int elemInfoEType(Geometry geom) {
        switch (template(geom)) {

        case Constants.SDO_GTEMPLATE.POINT:
            return Constants.SDO_ETYPE.POINT;

        case Constants.SDO_GTEMPLATE.LINE:
            return Constants.SDO_ETYPE.LINE;

        case Constants.SDO_GTEMPLATE.POLYGON:
        	// jts convention
            return Constants.SDO_ETYPE.POLYGON_EXTERIOR; // cc order

        default:

            // should never happen!
            throw new IllegalArgumentException("Unknown encoding of SDO_GTEMPLATE");
        }
    }
    
    /**
     * Allows specification of <code>INTERPRETATION</code> used to interpret
     * <code>geom</code>.
     * 
     * @param geom Geometry to encode
     * @param etype ETYPE value requiring an INTERPREATION
     *
     * @return INTERPRETATION ELEM_INFO entry for geom given etype
     *
     * @throws IllegalArgumentException If asked to encode a curve
     */
    private int elemInfoInterpretation(Geometry geom, int etype) {
        switch (etype) {

        case Constants.SDO_ETYPE.POINT:

            if (geom instanceof Point) {
                return 1;
            }

            if (geom instanceof MultiPoint) {
                return ((MultiPoint) geom).getNumGeometries();
            }

            break;

        case Constants.SDO_ETYPE.LINE:
        	// always straight for jts
            return 1;

        case Constants.SDO_ETYPE.POLYGON:
        case Constants.SDO_ETYPE.POLYGON_EXTERIOR:
        case Constants.SDO_ETYPE.POLYGON_INTERIOR:

            if (geom instanceof Polygon) {
                Polygon polygon = (Polygon) geom;
            	// always straight for jts
                if (isRectangle(polygon)) {
                    return 3;
                }
            }

            return 1;
        }

        throw new IllegalArgumentException("Cannot encode JTS "
            + geom.getGeometryType() + " as "
            + "SDO_INTERPRETATION (Limitied to Point, Line, Polygon, "
            + "GeometryCollection, MultiPoint, MultiLineString and MultiPolygon)");
    }
	
    /**
     * Return SDO_POINT_TYPE for geometry
     * 
     * Will return non null for Point objects. <code>null</code> is returned
     * for all non point objects.

     * You cannot use this with LRS Coordiantes
     * Subclasses may wish to repress this method and force Points to be
     * represented using SDO_ORDINATES.
     *
     * @param geom
     *
     * @return double[]
     */
	private double[] point(Geometry geom) {
        if (geom instanceof Point && (lrs(geom) == 0)) {
            Point point = (Point) geom;
            Coordinate coord = point.getCoordinate();

            return new double[] { coord.x, coord.y, coord.z };
        }

        // SDO_POINT_TYPE only used for non LRS Points
        return null;
    }

    /**
     * Produce SDO_GTEMPLATE representing provided Geometry.
     * 
     * <p>
     * Encoding of Geometry type and dimension.
     * </p>
     * 
     * <p>
     * SDO_GTEMPLATE defined as for digits <code>[d][l][tt]</code>:
     * </p>
     * 
     * @param geom
     *
     * @return SDO_GTEMPLATE
     */
	private int gType(Geometry geom) {
        int d = dimension(geom) * 1000;
        int l = lrs(geom) * 100;
        int tt = template(geom);

        return d + l + tt;
    }

    /**
     * Return dimensions as defined by SDO_GTEMPLATE (either 2,3 or 4).
     * 
     *
     * @param geom
     *
     * @return num dimensions
     */
    private int dimension(Geometry geom) {
    	int d = Double.isNaN(geom.getCoordinate().z)?2:3;
		return d<dimension?d:dimension;
    }

    /**
     * Return LRS as defined by SDO_GTEMPLATE (either 3,4 or 0).
     * 
     * @param geom
     *
     * @return <code>0</code>
     */
    private int lrs(Geometry geom) {
        // when measures are supported this may change
    	// until then ... 
    	return 0;
    }
    
    /**
     * Return TT as defined by SDO_GTEMPLATE (represents geometry type).
     * 
     * @see Constants.SDO_GTEMPLATE
     *
     * @param geom
     *
     * @return template code
     */
    private int template(Geometry geom) {
        if (geom == null) {
            return -1; // UNKNOWN
        } else if (geom instanceof Point) {
            return Constants.SDO_GTEMPLATE.POINT;
        } else if (geom instanceof LineString) {
            return Constants.SDO_GTEMPLATE.LINE;
        } else if (geom instanceof Polygon) {
            return Constants.SDO_GTEMPLATE.POLYGON;
        } else if (geom instanceof MultiPoint) {
            return Constants.SDO_GTEMPLATE.MULTIPOINT;
        } else if (geom instanceof MultiLineString) {
            return Constants.SDO_GTEMPLATE.MULTILINE;
        } else if (geom instanceof MultiPolygon) {
            return Constants.SDO_GTEMPLATE.MULTIPOLYGON;
        } else if (geom instanceof GeometryCollection) {
            return Constants.SDO_GTEMPLATE.COLLECTION;
        }

        throw new IllegalArgumentException("Cannot encode JTS "
            + geom.getGeometryType() + " as SDO_GTEMPLATE "
            + "(Limitied to Point, Line, Polygon, GeometryCollection, MultiPoint,"
            + " MultiLineString and MultiPolygon)");
    }
	
    /** Convience method for STRUCT construction. */
    private STRUCT toSTRUCT( Datum attributes[], String dataType )
            throws SQLException
    {
    	if( dataType.startsWith("*.")){
    		dataType = "DRA."+dataType.substring(2);//TODO here
    	}
        StructDescriptor descriptor =
            StructDescriptor.createDescriptor( dataType, connection );
    
         return new STRUCT( descriptor, connection, attributes );
    }
    
    /** 
     * Convience method for ARRAY construction.
     * <p>
     * Compare and contrast with toORDINATE - which treats <code>Double.NaN</code>
     * as<code>NULL</code></p>
     */
    private ARRAY toARRAY( double doubles[], String dataType )
            throws SQLException
    {
        ArrayDescriptor descriptor =
            ArrayDescriptor.createDescriptor( dataType, connection );
        
         return new ARRAY( descriptor, connection, doubles );
    }
    
    /** 
     * Convience method for ARRAY construction.
     */
    private ARRAY toARRAY( int ints[], String dataType )
        throws SQLException
    {
        ArrayDescriptor descriptor =
            ArrayDescriptor.createDescriptor( dataType, connection );
            
         return new ARRAY( descriptor, connection, ints );
    }

    /** 
     * Convience method for NUMBER construction.
     * <p>
     * Double.NaN is represented as <code>NULL</code> to agree
     * with JTS use.</p>
     */
    private NUMBER toNUMBER( double number ) throws SQLException{
        if( Double.isNaN( number )){
            return null;
        }
        return new NUMBER( number );
    }

    /**
     * reverses the coordinate order
     *
     * @param factory
     * @param sequence
     *
     * @return CoordinateSequence reversed sequence
     */
    private CoordinateSequence reverse(CoordinateSequenceFactory factory, CoordinateSequence sequence) {
    	CoordinateList list = new CoordinateList(sequence.toCoordinateArray());
        Collections.reverse(list);
        return factory.create(list.toCoordinateArray());
    }

	/**
	 * @param dimension The dimension to set.
	 */
	public void setDimension(int dimension) {
		this.dimension = dimension;
	}
}
