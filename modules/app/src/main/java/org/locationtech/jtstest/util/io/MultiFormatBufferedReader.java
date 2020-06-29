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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.List;

import javax.xml.parsers.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.gml2.*;


/**
 * Reads a {@link Geometry} from a string which is in either WKT, WKBHex
 * or GML format
 *
 * @author Martin Davis
 * @version 1.7
 */
public class MultiFormatBufferedReader
{
  private GeometryFactory geomFactory;

  public MultiFormatBufferedReader()
  {
    this(new GeometryFactory());
  }

  public MultiFormatBufferedReader(GeometryFactory geomFactory)
  {
    this.geomFactory = geomFactory;
  }

  public Geometry read(Reader reader)
      throws ParseException, IOException
  {
    BufferedReader bufRdr = new BufferedReader(reader);
    
    bufRdr.mark(20);
    char[] lookahead = new char[10];
    bufRdr.read(lookahead, 0, 10);
    bufRdr.reset();
    
    String laStr = new String(lookahead);
    if (MultiFormatReader.isWKB(laStr)) {
      return readWKBHex(bufRdr, geomFactory);
    } 
    else if (MultiFormatReader.isWKT(laStr)) {
      return readWKT(bufRdr, geomFactory);
    }
    throw new ParseException("Unknown format of data: " + laStr);
  }
  
  private static Geometry readWKBHex(Reader rdr, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKBReader reader = new WKBReader(geomFact);
    WKBHexFileReader fileReader = new WKBHexFileReader(rdr, reader);
    List geomList = fileReader.read();
    
    if (geomList.size() == 1)
      return (Geometry) geomList.get(0);
    
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }

  public static Geometry readWKT(Reader rdr, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKTReader reader = new WKTReader(geomFact);
    WKTFileReader fileReader = new WKTFileReader(rdr, reader);
    List geomList = fileReader.read();
    
    if (geomList.size() == 1)
      return (Geometry) geomList.get(0);
    
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }

}
