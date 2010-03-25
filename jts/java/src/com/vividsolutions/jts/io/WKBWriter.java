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
package com.vividsolutions.jts.io;

import java.io.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;

/**
 * Writes a {@link Geometry} into Well-Known Binary format.
 * Supports use of an {@link OutStream}, which allows easy use
 * with arbitary byte stream sinks.
 * <p>
 * The WKB format is specified in the 
 * OGC <A HREF="http://www.opengis.org/techno/specs.htm"><i>Simple Features for SQL</i></a>
 * specification.
 * This implementation also partially supports the <b>Extended WKB</b> 
 * standard for representing
 * 3-dimensional coordinates.  The presence of 3D coordinates is signified
 * by setting the high bit of the <tt>wkbType</tt> word.
 * <p>
 * Empty Points cannot be represented in WKB; an
 * {@link IllegalArgumentException} will be thrown if one is
 * written. The WKB specification does not support representing {@link LinearRing}s;
 * they will be written as {@link LineString}s.
 * <p>
 * This class is designed to support reuse of a single instance to read multiple
 * geometries. This class is not thread-safe; each thread should create its own
 * instance.
 *
 * @see WKBReader
 */
public class WKBWriter
{
  /**
   * Converts a byte array to a hexadecimal string.
   * 
   * @param bytes
   * @return
   * 
   * @deprecated
   */
  public static String bytesToHex(byte[] bytes)
  {
    return toHex(bytes);
  }

  /**
   * Converts a byte array to a hexadecimal string.
   * 
   * @param bytes a byte array
   * @return a string of hexadecimal digits
   */
  public static String toHex(byte[] bytes)
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      buf.append(toHexDigit((b >> 4) & 0x0F));
      buf.append(toHexDigit(b & 0x0F));
    }
    return buf.toString();
  }

  private static char toHexDigit(int n)
  {
    if (n < 0 || n > 15)
      throw new IllegalArgumentException("Nibble value out of range: " + n);
    if (n <= 9)
      return (char) ('0' + n);
    return (char) ('A' + (n - 10));
  }

  private int outputDimension = 2;
  private int byteOrder;
  private ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
  private OutStream byteArrayOutStream = new OutputStreamOutStream(byteArrayOS);
  // holds output data values
  private byte[] buf = new byte[8];

  /**
   * Creates a writer that writes {@link Geometry}s with
   * output dimension = 2 and BIG_ENDIAN byte order
   */
  public WKBWriter() {
    this(2, ByteOrderValues.BIG_ENDIAN);
  }

  /**
   * Creates a writer that writes {@link Geometry}s with
   * the given dimension (2 or 3) for output coordinates
   * and {@link BIG_ENDIAN} byte order.
   * If the input geometry has a small coordinate dimension,
   * coordinates will be padded with {@link NULL_ORDINATE}.
   *
   * @param outputDimension the coordinate dimension to output (2 or 3)
   */
  public WKBWriter(int outputDimension) {
    this(outputDimension, ByteOrderValues.BIG_ENDIAN);
  }

  /**
   * Creates a writer that writes {@link Geometry}s with
   * the given dimension (2 or 3) for output coordinates
   * and byte order
   * If the input geometry has a small coordinate dimension,
   * coordinates will be padded with {@link NULL_ORDINATE}.
   *
   * @param outputDimension the coordinate dimension to output (2 or 3)
   * @param byteOrder the byte ordering to use
   */
  public WKBWriter(int outputDimension, int byteOrder) {
    this.outputDimension = outputDimension;
    this.byteOrder = byteOrder;

    if (outputDimension < 2 || outputDimension > 3)
      throw new IllegalArgumentException("Output dimension must be 2 or 3");
  }

  /**
   * Writes a {@link Geometry} into a byte array.
   *
   * @param geom the geometry to write
   * @return the byte array containing the WKB
   */
  public byte[] write(Geometry geom)
  {
    try {
      byteArrayOS.reset();
      write(geom, byteArrayOutStream);
    }
    catch (IOException ex) {
      throw new RuntimeException("Unexpected IO exception: " + ex.getMessage());
    }
    return byteArrayOS.toByteArray();
  }

  /**
   * Writes a {@link Geometry} to an {@link OutStream}.
   *
   * @param geom the geometry to write
   * @param os the out stream to write to
   * @throws IOException if an I/O error occurs
   */
  public void write(Geometry geom, OutStream os) throws IOException
  {
    if (geom instanceof Point)
      writePoint((Point) geom, os);
    // LinearRings will be written as LineStrings
    else if (geom instanceof LineString)
      writeLineString((LineString) geom, os);
    else if (geom instanceof Polygon)
      writePolygon((Polygon) geom, os);
    else if (geom instanceof MultiPoint)
      writeGeometryCollection(WKBConstants.wkbMultiPoint, (MultiPoint) geom, os);
    else if (geom instanceof MultiLineString)
      writeGeometryCollection(WKBConstants.wkbMultiLineString,
          (MultiLineString) geom, os);
    else if (geom instanceof MultiPolygon)
      writeGeometryCollection(WKBConstants.wkbMultiPolygon,
          (MultiPolygon) geom, os);
    else if (geom instanceof GeometryCollection)
      writeGeometryCollection(WKBConstants.wkbGeometryCollection,
          (GeometryCollection) geom, os);
    else {
      Assert.shouldNeverReachHere("Unknown Geometry type");
    }
  }

  private void writePoint(Point pt, OutStream os) throws IOException
  {
    if (pt.getCoordinateSequence().size() == 0)
      throw new IllegalArgumentException("Empty Points cannot be represented in WKB");
    writeByteOrder(os);
    writeGeometryType(WKBConstants.wkbPoint, os);
    writeCoordinateSequence(pt.getCoordinateSequence(), false, os);
  }

  private void writeLineString(LineString line, OutStream os)
      throws IOException
  {
    writeByteOrder(os);
    writeGeometryType(WKBConstants.wkbLineString, os);
    writeCoordinateSequence(line.getCoordinateSequence(), true, os);
  }

  private void writePolygon(Polygon poly, OutStream os) throws IOException
  {
    writeByteOrder(os);
    writeGeometryType(WKBConstants.wkbPolygon, os);
    writeInt(poly.getNumInteriorRing() + 1, os);
    writeCoordinateSequence(poly.getExteriorRing().getCoordinateSequence(), true, os);
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      writeCoordinateSequence(poly.getInteriorRingN(i).getCoordinateSequence(), true,
          os);
    }
  }

  private void writeGeometryCollection(int geometryType, GeometryCollection gc,
      OutStream os) throws IOException
  {
    writeByteOrder(os);
    writeGeometryType(geometryType, os);
    writeInt(gc.getNumGeometries(), os);
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      write(gc.getGeometryN(i), os);
    }
  }

  private void writeByteOrder(OutStream os) throws IOException
  {
    if (byteOrder == ByteOrderValues.LITTLE_ENDIAN)
      buf[0] = WKBConstants.wkbNDR;
    else
      buf[0] = WKBConstants.wkbXDR;
    os.write(buf, 1);
  }

  private void writeGeometryType(int geometryType, OutStream os)
      throws IOException
  {
    int flag3D = (outputDimension == 3) ? 0x80000000 : 0;
    int typeInt = geometryType | flag3D;
    writeInt(typeInt, os);
  }

  private void writeInt(int intValue, OutStream os) throws IOException
  {
    ByteOrderValues.putInt(intValue, buf, byteOrder);
    os.write(buf, 4);
  }

  private void writeCoordinateSequence(CoordinateSequence seq, boolean writeSize, OutStream os)
      throws IOException
  {
    if (writeSize)
      writeInt(seq.size(), os);

    for (int i = 0; i < seq.size(); i++) {
      writeCoordinate(seq, i, os);
    }
  }

  private void writeCoordinate(CoordinateSequence seq, int index, OutStream os)
  throws IOException
  {
    ByteOrderValues.putDouble(seq.getX(index), buf, byteOrder);
    os.write(buf, 8);
    ByteOrderValues.putDouble(seq.getY(index), buf, byteOrder);
    os.write(buf, 8);
    
    // only write 3rd dim if caller has requested it for this writer
    if (outputDimension >= 3) {
      // if 3rd dim is requested, only access and write it if the CS provides is
    	double ordVal = Coordinate.NULL_ORDINATE;
    	if (seq.getDimension() >= 3)
    		ordVal = seq.getOrdinate(index, 2);
      ByteOrderValues.putDouble(ordVal, buf, byteOrder);
      os.write(buf, 8);
    }
  }
}