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
 * Translates a JTS Geometry into an Oracle STRUCT representing an <code>MDSYS.SDO_GEOMETRY</code> object. 
 * <p>
 * A connection to an Oracle instance with access to the definition of the <code>MDSYS.SDO_GEOMETRY</code>
 * type is required.
 * 
 * @version 9i
 * @author Martin Davis
 * @author David Zwiers, Vivid Solutions.
 */
public class OraWriter 
{
	/**
	 * A connection providing access to the required type definitions
	 */
	private OracleConnection connection;
	/**
	 * The maximum output dimension to write
	 */
	private int maxOutputDimension = 2;
	/**
	 * The default SRID to write 
	 */
	private int srid = OraGeom.SRID_NULL;

	/**
	 * Creates a writer using a valid Oracle connection. 
	 * 
	 * The connection should have sufficient privileges to view the description of the MDSYS.SDO_GEOMETRY type.
	 * 
	 * The maximum output dimension is set to 2
	 * 
	 * @param con a valid Oracle connection
	 */
	public OraWriter(OracleConnection con){
		this.connection = con;
	}
	
	/**
	 * Creates a writer using a valid Oracle connection,
	 * and specifying the maximum output dimension. 
	 * 
	 * The connection should have sufficient privileges to view the description of the MDSYS.GEOMETRY type.
	 * 
	 * @param con a valid Oracle connection
	 * @param dimension the maximum output dimension
	 */
	public OraWriter(OracleConnection con, int maxOutputDimension){
		this.connection = con;
		this.maxOutputDimension = maxOutputDimension;
	}
	
  /**
   * Sets the maximum output dimension for the created Oracle geometries.
   * 
   * 
   * @param dimension The dimension to set.
   */
  public void setDimension(int maxOutputDimension) {
    this.maxOutputDimension = maxOutputDimension;
  }

	/**
	 * Forces geometries to be written using the specified SRID. 
	 * This is useful in two cases: 
	 * <ul>
	 * <li>to avoid using the native geometry's SRID
	 * <li>to ensure an entire table is written using a fixed SRID.
	 * </ul>
	 * 
	 * @param srid the srid to use
	 */
	public void setSRID(int srid){
		this.srid = srid;
	}

  /**
   * Converts a {@link Geometry} into an Oracle MDSYS.SDO_GEOMETRY STRUCT.
   * <p>
   * Although invalid geometries may be encoded, and inserted into an Oracle DB,
   * this is not recommended. It is the responsibility of the user to ensure the
   * geometry is valid prior to calling this method. 
   * <p>
   * The SRID of the created SDO_GEOMETRY is the SRID defined explicitly for the writer, if any; 
   * otherwise it is the SRID contained in the input geometry. 
   * The caller should ensure the the SRID is valid for the intended use, 
   * since an incorrect SRID may cause indexing exceptions during an
   * INSERT or UPDATE.
   * <p>
   * When a null Geometry is passed in, a non-null, empty SDO_GEOMETRY STRUCT is returned.
   * Therefore, inserting the output of the writer into a
   * table will never result in NULL insertions.
   * To pass a NULL Geometry into an Oracle SDO_GEOMETRY-valued parameter using JDBC, use
   * <pre>
   * java.sql.CallableStatement.setNull(index, java.sql.Types.STRUCT, "MDSYS.SDO_GEOMETRY"). 
   * </pre>
   * 
   * @param geom the geometry to encode
   * @return a Oracle MDSYS.SDO_GEOMETRY STRUCT representing the geometry
   * @throws SQLException if an encoding error was encountered
   */
  public STRUCT write(Geometry geom) throws SQLException
  {
    // this line may be problematic ... for v9i and later
    // need to revisit.

    // was this ... does not work for 9i
    // if( geom == null) return toSTRUCT( null, DATATYPE );

    if (geom == null || geom.isEmpty() || geom.getCoordinate() == null)
      return createEmptySDOGeometry();

    OraGeom oraGeom = createOraGeom(geom);
    
    STRUCT SDO_POINT = null;
    ARRAY SDO_ELEM_INFO = null;
    ARRAY SDO_ORDINATES = null;
    if (oraGeom.point == null) {
      SDO_ELEM_INFO = OraUtil.toARRAY(oraGeom.elemInfo, OraGeom.TYPE_ELEM_INFO_ARRAY,
          connection);
      SDO_ORDINATES = OraUtil.toARRAY(oraGeom.ordinates, OraGeom.TYPE_ORDINATE_ARRAY,
          connection);
    }
    else { // Point Optimization
      Datum data[] = new Datum[] { 
          OraUtil.toNUMBER(oraGeom.point[0]),
          OraUtil.toNUMBER(oraGeom.point[1]), 
          OraUtil.toNUMBER(oraGeom.point[2]), };
      SDO_POINT = OraUtil.toSTRUCT(data, OraGeom.TYPE_POINT_TYPE, connection);
    }
    
    NUMBER SDO_GTYPE = new NUMBER(oraGeom.gType);
    NUMBER SDO_SRID = oraGeom.srid == OraGeom.SRID_NULL ? null : new NUMBER(oraGeom.srid);
    
    Datum sdoGeometryComponents[] = new Datum[] { 
        SDO_GTYPE, 
        SDO_SRID, 
        SDO_POINT,
        SDO_ELEM_INFO, 
        SDO_ORDINATES };
    return OraUtil.toSTRUCT(sdoGeometryComponents, OraGeom.TYPE_GEOMETRY, connection);
  }

	private STRUCT createEmptySDOGeometry() throws SQLException {
		return OraUtil.toSTRUCT(new Datum[5], OraGeom.TYPE_GEOMETRY, connection);
	}

  /**
   * Creates an {@link OraGeom} structure corresponding to the Oracle SDO_GEOMETRY
   * attributes representing the given Geometry.
   * This allows disconnected testing, since no Oracle types are accessed.
   * 
   * @param geom the non-null, non-empty Geometry to write
   * @return an OraGeom structure
   */
  OraGeom createOraGeom(Geometry geom)
  {
    int gtype = gType(geom);
    int srid = this.srid == OraGeom.SRID_NULL ? geom.getSRID() : this.srid;
    double[] point = pointOrdinates(geom);
    int elemInfo[] = null;
    double[] ordinates = null;
    
    if (point == null) {
      elemInfo = elemInfo(geom, gtype);

      List list = new ArrayList();
      coordinates(list, geom);

      int dim = OraGeom.gTypeDim(gtype);
      int lrs = OraGeom.gTypeMeasureDim(gtype);
      // MD - BUG????  Should be simply dim ????
      int ordDim = dim; // size per coordinate
      
      ordinates = buildOrdinates(list, ordDim);
      // free the list of coordinates
      list = null;
    }
    OraGeom oraGeom = new OraGeom(gtype, srid, point, elemInfo, ordinates);
    return oraGeom;
  }

  private double[] buildOrdinates(List list, int ordDim)
  {
    double[] ordinates = new double[list.size() * ordDim];

    int k = 0;
    for (int i = 0; i < list.size() && k < ordinates.length; i++) {
      int j = 0;
      double[] ords = (double[]) list.get(i);
      for (; j < ordDim && j < ords.length; j++) {
        ordinates[k++] = ords[j];
      }
      for (; j < ordDim; j++) { // mostly safety
        ordinates[k++] = Double.NaN;
      }
    }
    return ordinates;
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
        switch (OraGeom.geomType(geom)) {

        case OraGeom.GEOM_TYPE.POINT:
            addCoordinates(list, ((Point)geom).getCoordinateSequence());
            return;
        case OraGeom.GEOM_TYPE.LINE:
            addCoordinates(list, ((LineString)geom).getCoordinateSequence());
            return;
        case OraGeom.GEOM_TYPE.POLYGON:
            switch (elemInfoInterpretation(geom,OraGeom.ETYPE.POLYGON_EXTERIOR)) {
            case OraGeom.INTERP.RECTANGLE:
                Envelope e = geom.getEnvelopeInternal();
                list.add(new double[] { e.getMinX(), e.getMinY() });
                list.add(new double[] { e.getMaxX(), e.getMaxY() });
                return;
            case OraGeom.INTERP.POLYGON:
            	Polygon polygon = (Polygon) geom;
                int holes = polygon.getNumInteriorRing();
                
                // check outer ring's direction
                CoordinateSequence ring = polygon.getExteriorRing().getCoordinateSequence();
                if (! CGAlgorithms.isCCW(ring.toCoordinateArray())) {
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
        case OraGeom.GEOM_TYPE.MULTIPOINT:
        case OraGeom.GEOM_TYPE.MULTILINE:
        case OraGeom.GEOM_TYPE.MULTIPOLYGON:
        case OraGeom.GEOM_TYPE.COLLECTION:
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                coordinates(list, geom.getGeometryN(i));
            }
            return;
        }

        throw new IllegalArgumentException("Cannot encode JTS "
            + geom.getGeometryType() + " as "
            + "SDO_ORDINATES (Limited to Point, Line, Polygon, "
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
  private int[] elemInfo(Geometry geom, int gtype)
  {
    List list = new ArrayList();
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
  private void elemInfo(List elemInfoList, Geometry geom, int sOffSet, int gtype)
  {
    int dim = OraGeom.gTypeDim(gtype);
    int len = dim;
    switch (OraGeom.gTypeGeomType(gtype)) {
    case OraGeom.GEOM_TYPE.POINT:
      addInt(elemInfoList, sOffSet);
      addInt(elemInfoList, OraGeom.ETYPE.POINT);
      addInt(elemInfoList, OraGeom.INTERP.POINT);
      return;
    case OraGeom.GEOM_TYPE.MULTIPOINT:
      MultiPoint points = (MultiPoint) geom;
      addInt(elemInfoList, sOffSet);
      addInt(elemInfoList, OraGeom.ETYPE.POINT);
      addInt(elemInfoList, elemInfoInterpretation(points, OraGeom.ETYPE.POINT));
      return;
    case OraGeom.GEOM_TYPE.LINE:
      addInt(elemInfoList, sOffSet);
      addInt(elemInfoList, OraGeom.ETYPE.LINE);
      addInt(elemInfoList, OraGeom.INTERP.LINESTRING);
      return;
    case OraGeom.GEOM_TYPE.MULTILINE:
      MultiLineString lines = (MultiLineString) geom;
      LineString line;
      int offset = sOffSet;
      for (int i = 0; i < lines.getNumGeometries(); i++) {
        line = (LineString) lines.getGeometryN(i);
        addInt(elemInfoList, offset);
        addInt(elemInfoList, OraGeom.ETYPE.LINE);
        addInt(elemInfoList, OraGeom.INTERP.LINESTRING);
        offset += (line.getNumPoints() * len);
      }
      return;
    case OraGeom.GEOM_TYPE.POLYGON:
      Polygon polygon = (Polygon) geom;
      int holes = polygon.getNumInteriorRing();
      if (holes == 0) {
        addInt(elemInfoList, sOffSet);
        addInt(elemInfoList, elemInfoEType(polygon));
        addInt(elemInfoList,
            elemInfoInterpretation(polygon, OraGeom.ETYPE.POLYGON_EXTERIOR));
        return;
      }
      // shell
      offset = sOffSet;
      LineString ring = polygon.getExteriorRing();
      addInt(elemInfoList, offset);
      addInt(elemInfoList, elemInfoEType(polygon));
      addInt(elemInfoList,
          elemInfoInterpretation(polygon, OraGeom.ETYPE.POLYGON_EXTERIOR));
      // holes
      offset += (ring.getNumPoints() * len);
      for (int i = 1; i <= holes; i++) {
        ring = polygon.getInteriorRingN(i - 1);
        addInt(elemInfoList, offset);
        addInt(elemInfoList, OraGeom.ETYPE.POLYGON_INTERIOR);
        addInt(elemInfoList,
            elemInfoInterpretation(ring, OraGeom.ETYPE.POLYGON_INTERIOR));
        offset += (ring.getNumPoints() * len);
      }
      return;
    case OraGeom.GEOM_TYPE.MULTIPOLYGON:
      MultiPolygon polys = (MultiPolygon) geom;
      Polygon poly;
      offset = sOffSet;
      for (int i = 0; i < polys.getNumGeometries(); i++) {
        poly = (Polygon) polys.getGeometryN(i);
        elemInfo(elemInfoList, poly, offset, gType(poly));
        if (isRectangle(poly)) {
          offset += (2 * len);
        }
        else {
          offset += (poly.getNumPoints() * len);
        }
      }
      return;
    case OraGeom.GEOM_TYPE.COLLECTION:
      GeometryCollection geoms = (GeometryCollection) geom;
      offset = sOffSet;
      for (int i = 0; i < geoms.getNumGeometries(); i++) {
        geom = geoms.getGeometryN(i);
        // MD 20/3/07 modified to provide gType of component geometry
        elemInfo(elemInfoList, geom, offset, gType(geom));
        if (geom instanceof Polygon && isRectangle((Polygon) geom)) {
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
        + "(Limited to Point, Line, Polygon, GeometryCollection, MultiPoint,"
        + " MultiLineString and MultiPolygon)");
  }

    private static void addInt(List list, int i) {
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
     * @return <code>true</code> if polygon is SRID==NULL and a rectangle
     */
    private boolean isRectangle(Polygon polygon) {
        if (polygon.getFactory().getSRID() > 0) {
            // Rectangles only valid in CAD applications
            // that do not have an SRID system
            //
            return false;
        }

        if (lrsDim(polygon) != 0) {
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
        switch (OraGeom.geomType(geom)) {
        case OraGeom.GEOM_TYPE.POINT:
            return OraGeom.ETYPE.POINT;
        case OraGeom.GEOM_TYPE.LINE:
            return OraGeom.ETYPE.LINE;
        case OraGeom.GEOM_TYPE.POLYGON:
        	// jts convention
            return OraGeom.ETYPE.POLYGON_EXTERIOR; // cc order
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
        case OraGeom.ETYPE.POINT:
            if (geom instanceof Point) {
                return OraGeom.INTERP.POINT;
            }
            if (geom instanceof MultiPoint) {
                return ((MultiPoint) geom).getNumGeometries();
            }
            break;
            
        case OraGeom.ETYPE.LINE:
        	// always straight for jts
            return OraGeom.INTERP.LINESTRING;
            
        case OraGeom.ETYPE.POLYGON:
        case OraGeom.ETYPE.POLYGON_EXTERIOR:
        case OraGeom.ETYPE.POLYGON_INTERIOR:
            if (geom instanceof Polygon) {
                Polygon polygon = (Polygon) geom;
            	// always straight for jts
                if (isRectangle(polygon)) {
                    return OraGeom.INTERP.RECTANGLE;
                }
            }
            return OraGeom.INTERP.POLYGON;
        }
        throw new IllegalArgumentException("Cannot encode JTS "
            + geom.getGeometryType() + " as "
            + "SDO_INTERPRETATION (Limitied to Point, Line, Polygon, "
            + "GeometryCollection, MultiPoint, MultiLineString and MultiPolygon)");
    }
	
    /**
     * Extracts ordinate data for SDO_POINT_TYPE for Point geometries.
     * <code>null</code> is returned
     * for all non-Point geometries, or for LRS points.

     * This cannot be used for LRS coordinates.
     * Subclasses may wish to repress this method and force Points to be
     * represented using SDO_ORDINATES.
     *
     * @param geom the geometry providing the ordinates
     * @return double[] the point ordinates
     */
  private double[] pointOrdinates(Geometry geom)
  {
    if (geom instanceof Point && (lrsDim(geom) == 0)) {
      Point point = (Point) geom;
      Coordinate coord = point.getCoordinate();
      return new double[] { coord.x, coord.y, coord.z };
    }
    // Geometry type is not appropriate for SDO_POINT_TYPE
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
    private int gType(Geometry geom)
    {
      return OraGeom.gType(dimension(geom), lrsDim(geom), OraGeom.geomType(geom));
    }

    /**
     * Return dimensions as defined by SDO_GTEMPLATE (either 2,3 or 4), 
     * and clamped to the maximum output dimension (if any).
     * 
     *
     * @param geom
     *
     * @return num dimensions
     */
    private int dimension(Geometry geom) {
    	int d = Double.isNaN(geom.getCoordinate().z) ? 2 : 3;
		return d < maxOutputDimension ? d : maxOutputDimension;
    }

    /**
     * Return LRS dimension as defined by SDO_GTYPE (either 3,4 or 0).
     * 
     * @param geom
     *
     * @return <code>0</code>
     */
    private int lrsDim(Geometry geom) {
        // when measures are supported this may change
    	// until then ... 
    	return 0;
    }
    
  /**
   * reverses the coordinate order
   * 
   * @param factory
   * @param sequence
   * 
   * @return CoordinateSequence reversed sequence
   */
  private CoordinateSequence reverse(CoordinateSequenceFactory factory,
      CoordinateSequence sequence)
  {
    CoordinateList list = new CoordinateList(sequence.toCoordinateArray());
    Collections.reverse(list);
    return factory.create(list.toCoordinateArray());
  }

}
