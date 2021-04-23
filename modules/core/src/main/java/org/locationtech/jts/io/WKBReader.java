/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.io;

import java.io.IOException;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Reads a {@link Geometry}from a byte stream in Well-Known Binary format.
 * Supports use of an {@link InStream}, which allows easy use
 * with arbitrary byte stream sources.
 * <p>
 * This class reads the format describe in {@link WKBWriter}.  
 * It partially handles
 * the <b>Extended WKB</b> format used by PostGIS, 
 * by parsing and storing optional SRID values.
 * If a SRID is not specified in an element geometry, it is inherited
 * from the parent's SRID.
 * The default SRID value is 0.
 * <p>
 * Although not defined in the WKB specification, empty points
 * are handled if they are represented as a Point with <code>NaN</code> X and Y ordinates.
 * <p>
 * The reader repairs structurally-invalid input
 * (specifically, LineStrings and LinearRings which contain
 * too few points have vertices added,
 * and non-closed rings are closed).
 * <p>
 * The reader handles most errors caused by malformed or malicious WKB data.
 * It checks for obviously excessive values of the fields 
 * <code>numElems</code>, <code>numRings</code>, and <code>numCoords</code>.
 * It also checks that the reader does not read beyond the end of the data supplied.
 * A {@link ParseException} is thrown if this situation is detected.
 * <p>
 * This class is designed to support reuse of a single instance to read multiple
 * geometries. This class is not thread-safe; each thread should create its own
 * instance.
 * <p>
 * As of version 1.15, the reader can read geometries following the OGC 06-103r4 
 * Simple Features Access 1.2.1 specification,
 * which aligns with the ISO 19125 standard.
 * This format is used by Spatialite and Geopackage.
 * <p>
 * The difference between PostGIS EWKB format and the new ISO/OGC specification is
 * that Z and M coordinates are detected with a bit mask on the higher byte in
 * the former case (0x80 for Z and 0x40 for M) while new OGC specification use
 * specific int ranges for 2D geometries, Z geometries (2D code+1000), M geometries
 * (2D code+2000) and ZM geometries (2D code+3000).
 * <p>
 * Note that the {@link WKBWriter} is not changed and still writes the PostGIS EWKB
 * geometry format.
 * 
 * @see WKBWriter for a formal format specification
 */
public class WKBReader
{
  /**
   * Converts a hexadecimal string to a byte array.
   * The hexadecimal digit symbols are case-insensitive.
   *
   * @param hex a string containing hex digits
   * @return an array of bytes with the value of the hex string
   */
  public static byte[] hexToBytes(String hex)
  {
    int byteLen = hex.length() / 2;
    byte[] bytes = new byte[byteLen];

    for (int i = 0; i < hex.length() / 2; i++) {
      int i2 = 2 * i;
      if (i2 + 1 > hex.length())
        throw new IllegalArgumentException("Hex string has odd length");

      int nib1 = hexToInt(hex.charAt(i2));
      int nib0 = hexToInt(hex.charAt(i2 + 1));
      byte b = (byte) ((nib1 << 4) + (byte) nib0);
      bytes[i] = b;
    }
    return bytes;
  }

  private static int hexToInt(char hex)
  {
    int nib = Character.digit(hex, 16);
    if (nib < 0)
      throw new IllegalArgumentException("Invalid hex digit: '" + hex + "'");
    return nib;
  }

  private static final String INVALID_GEOM_TYPE_MSG
  = "Invalid geometry type encountered in ";

  private static final String FIELD_NUMCOORDS = "numCoords";

  private static final String FIELD_NUMRINGS = null;

  private static final String FIELD_NUMELEMS = null;

  private GeometryFactory factory;
  private CoordinateSequenceFactory csFactory;
  private PrecisionModel precisionModel;
  // default dimension - will be set on read
  private int inputDimension = 2;
  /**
   * true if structurally invalid input should be reported rather than repaired.
   * At some point this could be made client-controllable.
   */
  private boolean isStrict = false;
  private ByteOrderDataInStream dis = new ByteOrderDataInStream();
  private double[] ordValues;

  private int maxNumFieldValue;

  public WKBReader() {
    this(new GeometryFactory());
  }

  public WKBReader(GeometryFactory geometryFactory) {
    this.factory = geometryFactory;
    precisionModel = factory.getPrecisionModel();
    csFactory = factory.getCoordinateSequenceFactory();
  }

  /**
   * Reads a single {@link Geometry} in WKB format from a byte array.
   *
   * @param bytes the byte array to read from
   * @return the geometry read
   * @throws ParseException if the WKB is ill-formed
   */
  public Geometry read(byte[] bytes) throws ParseException
  {  
    // possibly reuse the ByteArrayInStream?
    // don't throw IOExceptions, since we are not doing any I/O
    try {
      return read(new ByteArrayInStream(bytes), bytes.length / 16);
    }
    catch (IOException ex) {
      throw new RuntimeException("Unexpected IOException caught: " + ex.getMessage());
    }
  }

  /**
   * Reads a {@link Geometry} in binary WKB format from an {@link InStream}.
   *
   * @param is the stream to read from
   * @return the Geometry read
   * @throws IOException if the underlying stream creates an error
   * @throws ParseException if the WKB is ill-formed
   */
  public Geometry read(InStream is)
  throws IOException, ParseException
  {
    // can't tell size of InStream, but MAX_VALUE should be safe
    return read(is, Integer.MAX_VALUE);
  }

  private Geometry read(InStream is, int maxCoordNum)
  throws IOException, ParseException
  {
    /**
     * This puts an upper bound on the allowed value
     * in coordNum fields.
     * It avoids OOM exceptions due to malformed input.
     */
    this.maxNumFieldValue = maxCoordNum;
    dis.setInStream(is);
    return readGeometry(0);
  }
  
  private int readNumField(String fieldName) throws IOException, ParseException {
    // num field is unsigned int, but Java has only signed int
    int num = dis.readInt();
    if (num < 0 || num > maxNumFieldValue) {
      throw new ParseException(fieldName + " value is too large");
    }
    return num;
  }
  
  private Geometry readGeometry(int SRID)
  throws IOException, ParseException
  {

      // determine byte order
      byte byteOrderWKB = dis.readByte();

      // always set byte order, since it may change from geometry to geometry
     if(byteOrderWKB == WKBConstants.wkbNDR)
     {
        dis.setOrder(ByteOrderValues.LITTLE_ENDIAN);
     }
     else if(byteOrderWKB == WKBConstants.wkbXDR)
     {
        dis.setOrder(ByteOrderValues.BIG_ENDIAN);
     }
     else if(isStrict)
     {
        throw new ParseException("Unknown geometry byte order (not NDR or XDR): " + byteOrderWKB);
     }
     //if not strict and not XDR or NDR, then we just use the dis default set at the
     //start of the geometry (if a multi-geometry).  This  allows WBKReader to work
     //with Spatialite native BLOB WKB, as well as other WKB variants that might just
     //specify endian-ness at the start of the multigeometry.


    int typeInt = dis.readInt();
    
    /**
     * To get geometry type mask out EWKB flag bits, 
     * and use only low 3 digits of type word.
     * This supports both EWKB and ISO/OGC.
     */
    int geometryType = (typeInt & 0xffff) % 1000;

    // handle 3D and 4D WKB geometries
    // geometries with Z coordinates have the 0x80 flag (postgis EWKB)
    // or are in the 1000 range (Z) or in the 3000 range (ZM) of geometry type (ISO/OGC 06-103r4)
    boolean hasZ = ((typeInt & 0x80000000) != 0 || (typeInt & 0xffff)/1000 == 1 || (typeInt & 0xffff)/1000 == 3);
    // geometries with M coordinates have the 0x40 flag (postgis EWKB)
    // or are in the 1000 range (M) or in the 3000 range (ZM) of geometry type (ISO/OGC 06-103r4)
    boolean hasM = ((typeInt & 0x40000000) != 0 || (typeInt & 0xffff)/1000 == 2 || (typeInt & 0xffff)/1000 == 3);
    //System.out.println(typeInt + " - " + geometryType + " - hasZ:" + hasZ);
    inputDimension = 2 + (hasZ ? 1 : 0) + (hasM ? 1 : 0);

    // determine if SRIDs are present (EWKB only)
    boolean hasSRID = (typeInt & 0x20000000) != 0;
    if (hasSRID) {
      SRID = dis.readInt();
    }

    // only allocate ordValues buffer if necessary
    if (ordValues == null || ordValues.length < inputDimension)
      ordValues = new double[inputDimension];

    Geometry geom = null;
    switch (geometryType) {
      case WKBConstants.wkbPoint :
        geom = readPoint();
        break;
      case WKBConstants.wkbLineString :
        geom = readLineString();
        break;
     case WKBConstants.wkbPolygon :
       geom = readPolygon();
        break;
      case WKBConstants.wkbMultiPoint :
        geom = readMultiPoint(SRID);
        break;
      case WKBConstants.wkbMultiLineString :
        geom = readMultiLineString(SRID);
        break;
     case WKBConstants.wkbMultiPolygon :
        geom = readMultiPolygon(SRID);
        break;
      case WKBConstants.wkbGeometryCollection :
        geom = readGeometryCollection(SRID);
        break;
      default: 
        throw new ParseException("Unknown WKB type " + geometryType);
    }
    setSRID(geom, SRID);
    return geom;
  }

  /**
   * Sets the SRID, if it was specified in the WKB
   *
   * @param g the geometry to update
   * @return the geometry with an updated SRID value, if required
   */
  private Geometry setSRID(Geometry g, int SRID)
  {
    if (SRID != 0)
      g.setSRID(SRID);
    return g;
  }

  private Point readPoint() throws IOException, ParseException
  {
    CoordinateSequence pts = readCoordinateSequence(1);
    // If X and Y are NaN create a empty point
    if (Double.isNaN(pts.getX(0)) || Double.isNaN(pts.getY(0))) {
      return factory.createPoint();
    }
    return factory.createPoint(pts);
  }

  private LineString readLineString() throws IOException, ParseException
  {
    int size = readNumField(FIELD_NUMCOORDS);
    CoordinateSequence pts = readCoordinateSequenceLineString(size);
    return factory.createLineString(pts);
  }

  private LinearRing readLinearRing() throws IOException, ParseException
  {
    int size = readNumField(FIELD_NUMCOORDS);
    CoordinateSequence pts = readCoordinateSequenceRing(size);
    return factory.createLinearRing(pts);
  }

  private Polygon readPolygon() throws IOException, ParseException
  {
    int numRings = readNumField(FIELD_NUMRINGS);
    LinearRing[] holes = null;
    if (numRings > 1)
      holes = new LinearRing[numRings - 1];

    // empty polygon
    if (numRings <= 0)
      return factory.createPolygon();
    
    LinearRing shell = readLinearRing();
    for (int i = 0; i < numRings - 1; i++) {
      holes[i] = readLinearRing();
    }
    return factory.createPolygon(shell, holes);
  }

  private MultiPoint readMultiPoint(int SRID) throws IOException, ParseException
  {
    int numGeom = readNumField(FIELD_NUMELEMS);
    Point[] geoms = new Point[numGeom];
    for (int i = 0; i < numGeom; i++) {
      Geometry g = readGeometry(SRID);
      if (! (g instanceof Point))
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiPoint");
      geoms[i] = (Point) g;
    }
    return factory.createMultiPoint(geoms);
  }

  private MultiLineString readMultiLineString(int SRID) throws IOException, ParseException
  {
    int numGeom = readNumField(FIELD_NUMELEMS);
    LineString[] geoms = new LineString[numGeom];
    for (int i = 0; i < numGeom; i++) {
      Geometry g = readGeometry(SRID);
      if (! (g instanceof LineString))
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiLineString");
      geoms[i] = (LineString) g;
    }
    return factory.createMultiLineString(geoms);
  }

  private MultiPolygon readMultiPolygon(int SRID) throws IOException, ParseException
  {
    int numGeom = readNumField(FIELD_NUMELEMS);
    Polygon[] geoms = new Polygon[numGeom];

    for (int i = 0; i < numGeom; i++) {
      Geometry g = readGeometry(SRID);
      if (! (g instanceof Polygon))
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiPolygon");
      geoms[i] = (Polygon) g;
    }
    return factory.createMultiPolygon(geoms);
  }

  private GeometryCollection readGeometryCollection(int SRID) throws IOException, ParseException
  {
    int numGeom = readNumField(FIELD_NUMELEMS);
    Geometry[] geoms = new Geometry[numGeom];
    for (int i = 0; i < numGeom; i++) {
      geoms[i] = readGeometry(SRID);
    }
    return factory.createGeometryCollection(geoms);
  }

  private CoordinateSequence readCoordinateSequence(int size) throws IOException, ParseException
  {
    CoordinateSequence seq = csFactory.create(size, inputDimension);
    int targetDim = seq.getDimension();
    if (targetDim > inputDimension)
      targetDim = inputDimension;
    for (int i = 0; i < size; i++) {
      readCoordinate();
      for (int j = 0; j < targetDim; j++) {
        seq.setOrdinate(i, j, ordValues[j]);
      }
    }
    return seq;
  }

  private CoordinateSequence readCoordinateSequenceLineString(int size) throws IOException, ParseException
  {
    CoordinateSequence seq = readCoordinateSequence(size);
    if (isStrict) return seq;
    if (seq.size() == 0 || seq.size() >= 2) return seq;
    return CoordinateSequences.extend(csFactory, seq, 2);
  }
  
  private CoordinateSequence readCoordinateSequenceRing(int size) throws IOException, ParseException
  {
    CoordinateSequence seq = readCoordinateSequence(size);
    if (isStrict) return seq;
    if (CoordinateSequences.isRing(seq)) return seq;
    return CoordinateSequences.ensureValidRing(csFactory, seq);
  }

  /**
   * Reads a coordinate value with the specified dimensionality.
   * Makes the X and Y ordinates precise according to the precision model
   * in use.
   * @throws ParseException 
   */
  private void readCoordinate() throws IOException, ParseException
  {
    for (int i = 0; i < inputDimension; i++) {
      if (i <= 1) {
        ordValues[i] = precisionModel.makePrecise(dis.readDouble());
      }
      else {
        ordValues[i] = dis.readDouble();
      }

    }
  }

}
