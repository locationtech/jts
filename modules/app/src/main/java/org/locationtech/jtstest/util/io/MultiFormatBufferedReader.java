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
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBHexFileReader;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;


/**
 * Reads a {@link Geometry} from a string which is in either WKT, WKBHex
 * or GML format
 *
 * @author Martin Davis
 * @version 1.7
 */
public class MultiFormatBufferedReader
{
  public static List<Geometry> read(Reader reader, int limit, int offset, GeometryFactory geomFactory) throws Exception {
    MultiFormatBufferedReader rdr = new MultiFormatBufferedReader(geomFactory);
    rdr.setLimit(limit);
    rdr.setOffset(offset);
    return rdr.read(reader);
  }
  
  private GeometryFactory geomFactory;
  private int limit = -1;
  private int offset = 0;

  public MultiFormatBufferedReader()
  {
    this(new GeometryFactory());
  }

  public MultiFormatBufferedReader(GeometryFactory geomFactory)
  {
    this.geomFactory = geomFactory;
  }

  /**
   * Sets the maximum number of geometries to read.
   * 
   * @param limit the maximum number of geometries to read
   */
  public void setLimit(int limit)
  {
    this.limit = limit;
  }
  
  /**
   * Sets the number of geometries to skip before storing.
   * 
   * @param offset the number of geometries to skip
   */
  public void setOffset(int offset)
  {
    this.offset = offset;
  }
  
  public List<Geometry> read(Reader reader)
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
  
  private List<Geometry> readWKBHex(Reader rdr, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKBReader reader = new WKBReader(geomFact);
    WKBHexFileReader fileReader = new WKBHexFileReader(rdr, reader);
    if (limit >= 0) fileReader.setLimit(limit);
    if (offset > 0) fileReader.setOffset(offset);
    return fileReader.read();
  }

  public List<Geometry> readWKT(Reader rdr, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKTReader reader = new WKTReader(geomFact);
    WKTFileReader fileReader = new WKTFileReader(rdr, reader);
    if (limit >= 0) fileReader.setLimit(limit);
    if (offset > 0) fileReader.setOffset(offset);
    return fileReader.read();
  }

}
