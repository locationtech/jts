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
package org.locationtech.jts.io.oracle;

import java.sql.SQLException;
import java.util.*;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.Assert;


import oracle.jdbc.OracleConnection;
import oracle.sql.*;

/**
 * Translates a JTS Geometry into an Oracle STRUCT representing an <code>MDSYS.SDO_GEOMETRY</code> object. 
 * Supports writing all JTS geometry types into an equivalent Oracle representation.
 * <p>
 * To write an Oracle <code>STRUCT</code> a connection to an Oracle instance with access to the definition of the <code>MDSYS.SDO_GEOMETRY</code>
 * type is required.  Oracle <code>SDO_GEOMETRY</code> SQL strings may be written without a connection, however.
 * <p>
 * By default, a single {@link Point} is written using the optimized <code>SDO_POINT_TYPE</code> attribute.
 * This can be overridden to use the (less compact) <code>SDO_ELEM_INFO/SDOORDINATES</code> representation
 * by using {@link #setOptimizePoint(boolean)}.
 * <p>
 * By default, rectangular polygons are written as regular 5-point polygons.
 * This can be changed to use the optimized RECTANGLE points 
 * by using {@link #setOptimizeRectangle(boolean)}.
 * Note that RECTANGLEs do not support LRS Measure ordinate values.
 * Also, this class only writes RECTANGLEs for polygons containing a single ring (i.e. the shell). 
 * <p>
 * Oracle cannot represent {@link MultiPolygon}s or {@link MultiLineString}s directly as elements
 * of a {@link GeometryCollection}. Instead, their components are written individually.
 * {@link MultiPoint}s are represented directly, however.
 * 
 * The dimension of the output <code>SDO_GEOMETRY</code> is determined as follows:
 * <ul>
 * <li>by default, the dimension matches that of the input
 * <li>currently the coordinate dimension of the input is determined by inspecting a sample coordinate.
 * If the Z value is <code>NaN</code>, the coordinate dimension is assumed to be 2.
 * (In the future this will be determined from the underlying {@link CoordinateSequence}s.
 * <li>the dimension can be set explicitly by the {@link #setDimension(int)} method.
 * This allows forcing Z output even if the Z values are <code>NaN</code>.
 * Conversely, if Z values are present this allows forcing 2D output.
 * </ul>
 * 
 * <h3>LIMITATIONS</h3>
 * <ul>
 * <li>Since JTS does not support Measures, they cannot be written.
 * (A future release could allow forcing interpreting Z as M, or else providing a fixed M value).
 * </ul>
 * 
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
	private int outputDimension = OraGeom.NULL_DIMENSION;
	/**
	 * The default SRID to write 
	 */
	private int srid = OraGeom.SRID_NULL;
	private boolean isOptimizeRectangle = false;
	private boolean isOptimizePoint = true;

  /**
   * Creates a writer for Oracle geometry formats. 
   * 
   * The output dimension will be whatever the dimension of the input is.
   */
  public OraWriter()
  {
  }
  
  /**
   * Creates a writer for Oracle geometry formats,
   * specifying the maximum output dimension. 
   * 
   * @param outputDimension the coordinate dimension to use for the output
   */
  public OraWriter(int outputDimension)
  {
    this.outputDimension = outputDimension;
  }

  /**
   * Creates a writer using a valid Oracle connection. 
   * <p>
   * To simplify connection resource handling, the connection should be
   * provided in the {@link #write(Geometry, OracleConnection)} method.
   * Accordingly, this constructor has been deprecated.
   * <p>
   * The connection should have sufficient privileges to view the description of the MDSYS.SDO_GEOMETRY type.
   * <p>
   * The output dimension will be whatever the dimension of the input is.
   * 
   * @param con a valid Oracle connection
   * @deprecated use {@link #OraWriter()} instead
   */
  public OraWriter(OracleConnection con)
  {
    this.connection = con;
  }
  
	/**
	 * Creates a writer using a valid Oracle connection,
	 * and specifying the maximum output dimension. 
   * <p>
   * To simplify connection resource handling, the connection should be
   * provided in the {@link #write(Geometry, OracleConnection)} method.
   * Accordingly, this constructor has been deprecated.
   * <p>
   * The connection should have sufficient privileges to view the description of the MDSYS.SDO_GEOMETRY type.
	 * 
	 * @param con a valid Oracle connection
	 * @param outputDimension the coordinate dimension to use for the output
	 * @deprecated use {@link #OraWriter(int)} instead
	 */
	public OraWriter(OracleConnection con, int outputDimension)
	{
		this.connection = con;
		this.outputDimension = outputDimension;
	}
	
    /**
     * Sets the coordinate dimension for the created Oracle geometries.
     * 
     * @param outputDimension
     *            the coordinate dimension to use for the output
     */
    public void setDimension(int outputDimension) 
    {
        if (outputDimension < 2)
            throw new IllegalArgumentException("Output dimension must be >= 2");
        this.outputDimension = outputDimension;
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
	public void setSRID(int srid)
	{
		this.srid = srid;
	}

  /**
   * Sets whether rectangle polygons should be written using the 
   * optimized 4-coordinate RECTANGLE format
   * (ETYPE=1003, INTERPRETATION=3).
   * If this option is false, rectangles are written as 5-coordinate polygons.
   * The default setting is <code>false</code>.
   * 
   * @param isOptimizeRectangle whether to optimize rectangle writing
   */
  public void setOptimizeRectangle(boolean isOptimizeRectangle)
  {
    this.isOptimizeRectangle  = isOptimizeRectangle;
  }
  
  /**
   * Sets whether points should be written using the 
   * optimized SDO_POINT_TYPE format.
   * If this option is <code>false</code>, points are written using the SDO_ORDINATES attribute.
   * The default setting is <code>true</code>.
   * 
   * @param isOptimizePoint whether to optimize point writing
   */
  public void setOptimizePoint(boolean isOptimizePoint)
  {
    this.isOptimizePoint   = isOptimizePoint;
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
   * @deprecated
   */
  public STRUCT write(Geometry geom) throws SQLException
  {
    return write(geom, connection);
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
  public STRUCT write(Geometry geom, OracleConnection connection) throws SQLException
  {
    // this line may be problematic ... for v9i and later need to revisit.

    // was this ... does not work for 9i
    // if( geom == null) return toSTRUCT( null, DATATYPE );

    if (geom == null || geom.isEmpty() || geom.getCoordinate() == null)
      return createEmptySDOGeometry(connection);

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

  /**
   * Writes a Geometry in Oracle SDO_GEOMETRY SQL literal format.
   * <p>
   * Examples of output are:
   * <pre>
   * SDO_GEOMETRY(2001,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1,1),SDO_ORDINATE_ARRAY(50,50))
   * SDO_GEOMETRY(3001,NULL,SDO_POINT_TYPE(50,50,100,),NULL,NULL)
   * SDO_GEOMETRY(3006,8307,NULL,SDO_ELEM_INFO_ARRAY(1,2,1,  7,2,1),SDO_ORDINATE_ARRAY(0,0,2,  50,50,100,  10,10,12,  150,150,110))
   * </pre>
   * 
   * @param geom the Geometry to write
   * @return a string representing the geometry as an SDO_GEOMETRY literal
   */
  public String writeSQL(Geometry geom)
  {
	  if (geom == null) return OraGeom.SQL_NULL;
	  OraGeom oraGeom = createOraGeom(geom);
	  return oraGeom.toString();
  }
  
	private STRUCT createEmptySDOGeometry(OracleConnection connection) throws SQLException {
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
    double[] point = null;
    int elemInfo[] = null;
    double[] ordinates = null;
    
    // if geometry ordinate data should be represented by SDO_ORDINATES array
    if (isEncodeAsPointType(geom)) {
        point = pointOrdinates(geom);
    }
    else {
      int dim = dimension(geom);
      List elemTriplets = new ArrayList();
      List ordGeoms = new ArrayList();
      int lastOrdOffset = writeElement(geom, dim, 1, elemTriplets, ordGeoms);
      elemInfo = flattenTriplets(elemTriplets);
      ordinates = writeGeometryOrdinates(elemTriplets, ordGeoms, lastOrdOffset - 1, dim);
    }
    
    OraGeom oraGeom = new OraGeom(gtype, srid, point, elemInfo, ordinates);
    return oraGeom;
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
    Point point = (Point) geom;
    Coordinate coord = point.getCoordinate();
    return new double[] { coord.x, coord.y, coord.z };
  }

  /**
   * Writes each geometry element which will appear in the output,
   * by recursing through the input geometry, 
   * and identifying each element and how it will 
   * appear in the output elemInfo array.
   * For each element the relevant geometry component
   * is recorded as well, to allow the ordinates
   * array to be written from them subsequently.
   * The total length of the ordinate array is summed
   * during this process as well (which also allows determining startingOffsets).
   * 
   * @param geom
   * @param dim
   * @param offset
   * @param elemTriplets
   * @param ordGeoms
   * @return the final startingOffset
   */
  private int writeElement(Geometry geom, int dim, int offset, List elemTriplets, List ordGeoms)
  {
    int interp;
    int geomType = OraGeom.geomType(geom);
    switch (geomType) {
    case OraGeom.GEOM_TYPE.POINT:
      // full point encoding - optimized one has been done earlier if possible
      Point point = (Point) geom;
      elemTriplets.add(triplet(offset, OraGeom.ETYPE.POINT, OraGeom.INTERP.POINT));
      ordGeoms.add(point);
      return offset + dim;
      
    case OraGeom.GEOM_TYPE.MULTIPOINT:
      MultiPoint points = (MultiPoint) geom;
      int nPts = points.getNumGeometries();
      // this works for nPts >= 1 (0 has already been handled)
      elemTriplets.add(triplet(offset, OraGeom.ETYPE.POINT, nPts));
      ordGeoms.add(points);
      return offset + dim * nPts;
      
    case OraGeom.GEOM_TYPE.LINE:
      LineString line = (LineString) geom;
      elemTriplets.add(triplet(offset, OraGeom.ETYPE.LINE, OraGeom.INTERP.LINESTRING));
      ordGeoms.add(line);
      return offset + dim * line.getNumPoints();
      
    case OraGeom.GEOM_TYPE.MULTILINE:
      MultiLineString lines = (MultiLineString) geom;
      for (int i = 0; i < lines.getNumGeometries(); i++) {
        LineString lineElem = (LineString) lines.getGeometryN(i);
        offset = writeElement(lineElem, dim, offset, elemTriplets, ordGeoms);
      }
      return offset;
      
    case OraGeom.GEOM_TYPE.POLYGON:
      Polygon polygon = (Polygon) geom;
      // shell
      LineString ring = polygon.getExteriorRing();
      interp = isWriteAsRectangle(polygon) ? OraGeom.INTERP.RECTANGLE : OraGeom.INTERP.POLYGON;
      elemTriplets.add(triplet(offset, OraGeom.ETYPE.POLYGON_EXTERIOR, interp));
      ordGeoms.add(ring);
      if (interp == OraGeom.INTERP.RECTANGLE) {
        offset += 4;
      }
      else {
        offset += dim * ring.getNumPoints();
      }
      
      // holes
      int holes = polygon.getNumInteriorRing();
      for (int i = 0; i < holes; i++) {
        ring = polygon.getInteriorRingN(i);
        elemTriplets.add(triplet(offset, OraGeom.ETYPE.POLYGON_INTERIOR, OraGeom.INTERP.POLYGON));
        ordGeoms.add(ring);
        offset += dim * ring.getNumPoints();
      }
      return offset;
      
    case OraGeom.GEOM_TYPE.MULTIPOLYGON:
      MultiPolygon polys = (MultiPolygon) geom;
      Polygon poly;
      for (int i = 0; i < polys.getNumGeometries(); i++) {
        poly = (Polygon) polys.getGeometryN(i);
        offset = writeElement(poly, dim, offset, elemTriplets, ordGeoms);
      }
      return offset;
      
    case OraGeom.GEOM_TYPE.COLLECTION:
      GeometryCollection geoms = (GeometryCollection) geom;
      for (int i = 0; i < geoms.getNumGeometries(); i++) {
        geom = geoms.getGeometryN(i);
        offset = writeElement(geom, dim, offset, elemTriplets, ordGeoms);
      }
      return offset;
    }

    throw new IllegalArgumentException("Cannot encode JTS "
        + geom.getGeometryType() + " as SDO_ELEM_INFO "
        + "(Limited to Point, Line, Polygon, GeometryCollection, MultiPoint,"
        + " MultiLineString and MultiPolygon)");
  }
  
  private static int[] triplet(int sOffset, int etype, int interp)
  {
    return new int[] { sOffset, etype, interp };
  }
  
  private int[] flattenTriplets(List elemTriplets)
  {
    int[] elemInfo = new int[3 * elemTriplets.size()];
    int eiIndex = 0;
    for (int i = 0; i < elemTriplets.size(); i++) {
      int[] triplet = (int[]) elemTriplets.get(i);
      for (int ii = 0; ii < 3; ii++) {
        elemInfo[eiIndex++] = triplet[ii];
      }
    }
    return elemInfo;
  }

  /**
   * Writes the ordinate values extracted from each element Geometry
   * into a double array.
   * This optimizes memory usage by only allocating the single
   * double array required to pass the ordinates to the Oracle STRUCT.
   * 
   * @param elemTriplets
   * @param ordGeoms
   * @param ordSize
   * @param dim
   * @return the final ordinate array
   */
  private double[] writeGeometryOrdinates(List elemTriplets, List ordGeoms, int ordSize, int dim)
  {
    double[] ords = new double[ordSize];
    int ordIndex = 0;
    for (int ielem = 0; ielem < elemTriplets.size(); ielem++) {
      int[] triplet = (int[]) elemTriplets.get(ielem);
      
      int startOffset = triplet[0]; 
      //verify startOffset is same as ordIndex
      Assert.isTrue(startOffset == ordIndex + 1,
          "ElemInfo computed startingOffset does not match actual ordinates position");
      
      int elemType = triplet[1];
      int interp = triplet[2];
      Geometry geom = (Geometry) ordGeoms.get(ielem);
      switch (elemType) {
      case OraGeom.ETYPE.POINT:
        if (interp == 1) {
          ordIndex = writeOrds(((Point) geom).getCoordinateSequence(), dim, ords, ordIndex);
        }
        else {
          // must be > 1 - write MultiPoint
          ordIndex = writeOrds((MultiPoint) geom, dim, ords, ordIndex);          
        }
        break;
        
      case OraGeom.ETYPE.LINE:
        ordIndex = writeOrds(((LineString) geom).getCoordinateSequence(), dim, ords, ordIndex);
        break;
        
      case OraGeom.ETYPE.POLYGON_EXTERIOR:
        if (interp == OraGeom.INTERP.RECTANGLE) {
          ordIndex = writeRectangleOrds(geom, dim, ords, ordIndex);
        }
        else {
          ordIndex = writeOrdsOriented(((LineString) geom).getCoordinateSequence(), dim, ords, ordIndex, true);
        }
        break;
        
      case OraGeom.ETYPE.POLYGON_INTERIOR:
        ordIndex = writeOrdsOriented(((LineString) geom).getCoordinateSequence(), dim, ords, ordIndex, false);
        break;
      }
    }
    return ords; 
  }

  /**
   * Writes ordinates in the orientation
   * specified by the isWriteCCW CCW flag.
   * Coordinates are reversed if necessary.
   * 
   * @param seq the coordinates to write
   * @param dim the output dimension required
   * @param ordData the ordinates array
   * @param ordIndex the starting index in the ordinates array
   * @param isWriteCCW true if the ordinates should be written in CCW orientation, false if CW 
   * @return the next index to write in the ordinates array
   */
  private int writeOrdsOriented(CoordinateSequence seq, int dim,
      double[] ordData, int ordIndex, boolean isWriteCCW)
  {
    Coordinate[] coords = seq.toCoordinateArray();
    //TODO: add method to CGAlgorithms to compute isCCW for CoordinateSequences
    boolean isCCW = CGAlgorithms.isCCW(coords);
    if (isCCW != isWriteCCW) {
      return writeOrdsReverse(seq, dim, ordData, ordIndex);
    }
    return writeOrds(seq, dim, ordData, ordIndex);
  }

  private int writeOrdsReverse(CoordinateSequence seq, int dim, double[] ordData, int ordIndex)
  {
    int nCoord = seq.size();
    for (int i = nCoord-1; i >= 0; i--) {
      for (int id = 0; id < dim; id++) {
        ordData[ordIndex++] = seq.getOrdinate(i, id);
      }
    }
    return ordIndex;
  }

  private int writeOrds(CoordinateSequence seq, int dim, double[] ordData, int ordIndex)
  {
    int nCoord = seq.size();
    for (int i = 0; i < nCoord; i++) {
      for (int id = 0; id < dim; id++) {
        ordData[ordIndex++] = seq.getOrdinate(i, id);
      }
    }
    return ordIndex;
  }

  private int writeOrds(MultiPoint geom, int dim, double[] ordData, int ordIndex)
  {
    int nGeom = geom.getNumGeometries();
    for (int i = 0; i < nGeom; i++) {
      CoordinateSequence seq = ((Point) geom.getGeometryN(i)).getCoordinateSequence();
      for (int id = 0; id < dim; id++) {
        ordData[ordIndex++] = seq.getOrdinate(0, id);
      }
    }
    return ordIndex;
  }

  private int writeRectangleOrds(Geometry ring, int dim, double[] ordData, int ordIndex)
  {
    Envelope e = ring.getEnvelopeInternal();
    ordData[ordIndex++] = e.getMinX();
    ordData[ordIndex++] = e.getMinY();
    ordData[ordIndex++] = e.getMaxX();
    ordData[ordIndex++] = e.getMaxY();
    return ordIndex;
  }

  /**
   * Tests if a <code>polygon</code> can be written aa a RECTANGLE.
   * Rectangles are only supported without a SRID!
   *
   * @param polygon
   * @return <code>true</code> if polygon is SRID==NULL and a rectangle
   */
  private boolean isWriteAsRectangle(Polygon polygon) {
    if (! isOptimizeRectangle) return false;

    if (lrsDim(polygon) != 0) {
        // cannot support LRS on a rectangle
        return false;
    }
    return polygon.isRectangle();
  }
      	
  private boolean isEncodeAsPointType(Geometry geom)
  {
    if (! isOptimizePoint) return false;
    if (geom instanceof Point && (lrsDim(geom) == 0) && outputDimension <= 3) 
      return true;
    // Geometry type is not appropriate for SDO_POINT_TYPE
    return false;
  }

  /**
   * Produce SDO_GTYPE code for input Geometry.
   * 
   * @param geom
   * @return SDO_GTYPE code
   */
  private int gType(Geometry geom)
  {
    return OraGeom.gType(dimension(geom), lrsDim(geom), OraGeom.geomType(geom));
  }

  /**
   * Return dimension of output coordinates (either 2,3 or 4), 
   * respecting the explicit output dimension (if any).
   * 
   * @param geom
   * @return coordinate dimension number
   */
  private int dimension(Geometry geom) {
	  if (outputDimension != OraGeom.NULL_DIMENSION)
		  return outputDimension;
	  
	  //TODO: check dimension of a geometry CoordinateSequence to determine dimension
  	int d = Double.isNaN(geom.getCoordinate().z) ? 2 : 3;
  	return d;
  }

  /**
   * Return LRS dimension as defined by SDO_GTYPE (either 3,4 or 0).
   * 
   * @param geom
   * @return LRS dimension
   */
  private int lrsDim(Geometry geom) {
    //TODO: implement measure support when available 
  	return 0;
  }

}
