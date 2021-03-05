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
package org.locationtech.jtstest.util.io;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ByteArrayInStream;
import org.locationtech.jts.io.ByteOrderDataInStream;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.InStream;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBConstants;
import org.locationtech.jts.io.WKBWriter;

/**
 * Dumps out WKB in a structured formatted text display.
 */
public class WKBDumper
{
  public static void dump(byte[] bytes, Writer writer) {
    WKBDumper dumper = new WKBDumper();
    dumper.read(bytes, writer);
  }
  
  public static String dump(byte[] bytes) {
    WKBDumper dumper = new WKBDumper();
    return dumper.readString(bytes);
  }

  private ByteOrderDataInStream dis = new ByteOrderDataInStream();
  private Writer writer;
  private int inputDimension;

  public WKBDumper() {
    
  }

  private String readString(byte[] bytes) {
    writer = new StringWriter();
    read(bytes);
    return writer.toString();
  }
  
  private void read(byte[] bytes, Writer writer) {
    this.writer = writer;
    read(bytes);
  }
  
  /**
   * Reads a single {@link Geometry} in WKB format from a byte array.
   *
   * @param bytes the byte array to read from
   * @return the geometry read
   * @throws IOException 
   * @throws ParseException if the WKB is ill-formed
   */
  private void read(byte[] bytes)
  {
    // possibly reuse the ByteArrayInStream?
    // don't throw IOExceptions, since we are not doing any I/O
    try {
      read(new ByteArrayInStream(bytes));
    }
    catch (Exception ex) {
      // TODO Auto-generated catch block
      try {
        writer.write("ParseException: " + ex.getMessage() + "\n");
      } catch (IOException e) {
        // Nothing we can do
      }
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
  private void read(InStream is)
  throws IOException, ParseException
  {
    dis.setInStream(is);
    readGeometry(0);
  }

  private void readGeometry(int SRID)
  throws IOException, ParseException
  {
    // determine byte order
    byte byteOrderWKB = readEndian();

    // always set byte order, since it may change from geometry to geometry
    if ( byteOrderWKB == WKBConstants.wkbNDR ) {
      dis.setOrder(ByteOrderValues.LITTLE_ENDIAN);
    } else if ( byteOrderWKB == WKBConstants.wkbXDR ) {
      dis.setOrder(ByteOrderValues.BIG_ENDIAN);
    }

    int typeInt = readInt();
    
    // Adds %1000 to make it compatible with OGC 06-103r4
    int geometryType = (typeInt & 0xffff)%1000;

    // handle 3D and 4D WKB geometries
    // geometries with Z coordinates have the 0x80 flag (postgis EWKB)
    // or are in the 1000 range (Z) or in the 3000 range (ZM) of geometry type (OGC 06-103r4)
    boolean hasZ = ((typeInt & 0x80000000) != 0 || (typeInt & 0xffff)/1000 == 1 || (typeInt & 0xffff)/1000 == 3);
    // geometries with M coordinates have the 0x40 flag (postgis EWKB)
    // or are in the 1000 range (M) or in the 3000 range (ZM) of geometry type (OGC 06-103r4)
    boolean hasM = ((typeInt & 0x40000000) != 0 || (typeInt & 0xffff)/1000 == 2 || (typeInt & 0xffff)/1000 == 3);
    //System.out.println(typeInt + " - " + geometryType + " - hasZ:" + hasZ);
    inputDimension = 2 + (hasZ?1:0) + (hasM?1:0);

    // determine if SRIDs are present
    boolean hasSRID = (typeInt & 0x20000000) != 0;
    
    writer.write(geometryTypeName(geometryType) + " ( " + geometryType + " ) "); 
    if (hasZ) writer.write(" Z" );
    if (hasM) writer.write(" M" );
    if (hasSRID) writer.write(" SRID" );
    
    writer.write("\n");

    
    if (hasSRID) {
      SRID = readTaggedInt("SRID");
    }

    Geometry geom = null;
    switch (geometryType) {
      case WKBConstants.wkbPoint :
        readPoint();
        break;
      case WKBConstants.wkbLineString :
        readLineString();
        break;
     case WKBConstants.wkbPolygon :
       readPolygon();
        break;
      case WKBConstants.wkbMultiPoint :
      case WKBConstants.wkbMultiLineString :
      case WKBConstants.wkbMultiPolygon :
      case WKBConstants.wkbGeometryCollection :
        readGeometryCollection(SRID);
        break;
      default: 
        //throw new ParseException("Unknown WKB type " + geometryType);
    }
  }

  private static String geometryTypeName(int geometryType) {
    switch (geometryType) {
    case WKBConstants.wkbPoint : return "POINT";
    case WKBConstants.wkbLineString : return "LINESTRING";
    case WKBConstants.wkbPolygon : return "POLYGON";
    case WKBConstants.wkbMultiPoint : return "MULTIPOINT";
    case WKBConstants.wkbMultiLineString : return "MULTILINESTRING";
    case WKBConstants.wkbMultiPolygon : return "MULTIPOLYGON";
    case WKBConstants.wkbGeometryCollection : return "GEOMETRYCOLLECTION";
    default: 
      return "Unknown";
    }
  }

  private void readPoint() throws IOException, ParseException
  {
    readCoordinateSequence(1);
    // If X and Y are NaN create a empty point
    /*
    if (Double.isNaN(pts.getX(0)) || Double.isNaN(pts.getY(0))) {
      //return factory.createPoint();
    }
    */
  }

  private void readLineString() throws IOException, ParseException
  {
    int size = readTaggedInt("Num Points");
    readCoordinateSequence(size);
  }

  private void readLinearRing() throws IOException, ParseException
  {
    int size = readTaggedInt("Num Points");
    readCoordinateSequence(size);
  }

  private void readPolygon() throws IOException, ParseException
  {
    int numRings = readTaggedInt("Num Rings");
    readLinearRing();
    for (int i = 0; i < numRings - 1; i++) {
      readLinearRing();
    }
  }

  private void readGeometryCollection(int SRID) throws IOException, ParseException
  {
    int numGeom = readTaggedInt("Num Elements");
    for (int i = 0; i < numGeom; i++) {
      writer.write(" ------- [ " + i + " ] ---------------\n");
      readGeometry(SRID);
    }
  }

  private void readCoordinateSequence(int size) throws IOException, ParseException
  {
    for (int i = 0; i < size; i++) {
      readCoordinate(i);
    }
  }

  /**
   * Reads a coordinate value with the specified dimensionality.
   * Makes the X and Y ordinates precise according to the precision model
   * in use.
   * @throws ParseException 
   */
  private void readCoordinate(int index) throws IOException, ParseException
  {
    writer.write(dis.getCount() + ": ");
    String hex = "";
    String nums = "";
    for (int i = 0; i < inputDimension; i++) {
        double d = dis.readDouble();
        hex += WKBWriter.toHex(dis.getData()) + " ";
        nums += (i > 0 ? ", " : "") + d;
    }
    writer.write(hex + " [" + index + "] " + nums + "\n");
  }
  
  private int readTaggedInt(String tag) throws IOException, ParseException {
    int size = readInt();
    writer.write(tag + " = " + size + "\n");
    return size;
  }
  
  private int readInt() throws IOException, ParseException {
    writer.write(dis.getCount() + ": ");
    int i = dis.readInt();
    
    // TODO: write in hex
    writer.write(WKBWriter.toHex(dis.getData()) + " - ");
    return i;
  }

  private byte readEndian() throws IOException, ParseException {
    writer.write(dis.getCount() + ": ");
    byte i = dis.readByte();
    String endian = i == WKBConstants.wkbNDR ? "NDR (Little endian)" :"XDR (Big endian)";
    // TODO: write in hex
    writer.write(WKBWriter.toHex(dis.getData()) + " - " + endian + "\n");
    return i;
  }

}
