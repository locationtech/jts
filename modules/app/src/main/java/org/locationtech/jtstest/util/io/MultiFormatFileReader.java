/*
 * Copyright (c) 2020 Martin Davis
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBHexFileReader;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jtstest.testbuilder.io.shapefile.Shapefile;
import org.locationtech.jtstest.util.FileUtil;


/**
 * Reads a {@link Geometry} collection from a file which is in 
 * WKT, WKBHex, GML, GeoJSON, or SHP format.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class MultiFormatFileReader
{
  public static Geometry readGeometry(String filename, int limit, int offset, GeometryFactory geomFactory) throws Exception {
    MultiFormatFileReader rdr = new MultiFormatFileReader(geomFactory);
    rdr.setLimit(limit);
    rdr.setOffset(offset);
    return rdr.read(filename);
  }
  
  public static List<Geometry> read(String filename, int limit, int offset, GeometryFactory geomFactory) throws Exception {
    MultiFormatFileReader rdr = new MultiFormatFileReader(geomFactory);
    rdr.setLimit(limit);
    rdr.setOffset(offset);
    return rdr.readList(filename);
  }
  
  private GeometryFactory geomFact;
  private int limit = -1;
  private int offset = 0;

  public MultiFormatFileReader()
  {
    this(new GeometryFactory());
  }

  public MultiFormatFileReader(GeometryFactory geomFactory)
  {
    this.geomFact = geomFactory;
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
  
  public Geometry read(String filename)
      throws Exception
  {
    String ext = FileUtil.extension(filename);
    if (ext.equalsIgnoreCase(".wkb"))
      return toGeometry(readWKBHexFile(filename));
    if (ext.equalsIgnoreCase(".shp"))
      return toGeometry(readShapefile(filename));
    
    if (ext.equalsIgnoreCase(".gml"))
      return IOUtil.readFile(filename, geomFact);
    if (ext.equalsIgnoreCase(".geojson"))
      return IOUtil.readFile(filename, geomFact);
    
    return toGeometry(readWKTFile(filename));
  }
  
  public List<Geometry> readList(String filename)
      throws Exception
  {
    String ext = FileUtil.extension(filename);
    if (ext.equalsIgnoreCase(".wkb"))
      return readWKBHexFile(filename);
    if (ext.equalsIgnoreCase(".shp"))
      return readShapefile(filename);
    
    /*
    if (ext.equalsIgnoreCase(".gml"))
      return IOUtil.readFile(filename, geomFact);
      */
    if (ext.equalsIgnoreCase(".geojson"))
      return readGeoJSONFile(filename);
    
    return readWKTFile(filename);
  }
  
  private List<Geometry> readGeoJSONFile(String filename) throws ParseException, IOException {
    GeoJsonMultiReader reader = new GeoJsonMultiReader(geomFact);
    List<Geometry> geoms = reader.readList(FileUtil.readText(filename));
    return geoms;
  }

  private List<Geometry> readWKBHexFile(String filename)
  throws ParseException, IOException 
  {
    WKBReader reader = new WKBReader(geomFact);
    WKBHexFileReader fileReader = new WKBHexFileReader(filename, reader);
    if (limit >= 0) fileReader.setLimit(limit);
    if (offset > 0) fileReader.setOffset(offset);
    return fileReader.read();
  }

  private List<Geometry> readWKTFile(String filename)
  throws ParseException, IOException 
  {
    WKTReader reader = new WKTReader(geomFact);
    WKTFileReader fileReader = new WKTFileReader(filename, reader);
    if (limit >= 0) fileReader.setLimit(limit);
    if (offset > 0) fileReader.setOffset(offset);
    return fileReader.read();
  }
  
  private List<Geometry> readShapefile(String filename)
  throws Exception 
  {
    Shapefile shpfile = new Shapefile(new FileInputStream(filename));
    shpfile.readStream(geomFact);
    int count = 0;
    List<Geometry> geomList = new ArrayList<Geometry>();
    do {
      Geometry geom = shpfile.next();
      boolean isOverLimit = limit >= 0 && geomList.size() > limit;
      if (geom == null || isOverLimit)
        break;
      if (count >= offset) {
        geomList.add(geom);
      }
      count++;
    } while (true);
    return geomList;
  }
  
  private Geometry toGeometry(List<Geometry> geomList) {
    if (geomList.size() == 1)
      return (Geometry) geomList.get(0);
    
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }

}
