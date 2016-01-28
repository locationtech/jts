/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package com.vividsolutions.jtstest.util.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBHexFileReader;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.gml2.GMLReader;
import org.xml.sax.SAXException;

import com.vividsolutions.jtstest.testbuilder.io.shapefile.Shapefile;
import com.vividsolutions.jtstest.util.FileUtil;

public class IOUtil 
{
  public static Geometry readFile(String filename, GeometryFactory geomFact)
  throws Exception, IOException 
  {
    String ext = FileUtil.extension(filename);
    if (ext.equalsIgnoreCase(".shp"))
      return readShapefile(filename, geomFact);
    if (ext.equalsIgnoreCase(".wkb"))
      return readWKBHexFile(filename, geomFact);
    if (ext.equalsIgnoreCase(".gml"))
      return readGMLFile(filename, geomFact);
    if (ext.equalsIgnoreCase(".geojson"))
      return readGeoJSONFile(filename, geomFact);
    return readWKTFile(filename, geomFact);
  }
    
  private static Geometry readShapefile(String filename, GeometryFactory geomFact)
  throws Exception 
  {
    Shapefile shpfile = new Shapefile(new FileInputStream(filename));
    shpfile.readStream(geomFact);
    List geomList = new ArrayList();
    do {
      Geometry geom = shpfile.next();
      if (geom == null)
        break;
      geomList.add(geom);
    } while (true);
    
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }
  
  private static Geometry readGMLFile(String filename, GeometryFactory geomFact)
  throws ParseException, IOException, SAXException, ParserConfigurationException 
  {
    return readGMLString(FileUtil.readText(filename), geomFact);
  }
  
  private static Geometry readWKBHexFile(String filename, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    return readWKBHexString(FileUtil.readText(filename), geomFact);
  }
  
  /*
  private static Geometry readWKBHexString(String wkbHexFile, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKBReader reader = new WKBReader(geomFact);
    String wkbHex = cleanHex(wkbHexFile);
    return reader.read(WKBReader.hexToBytes(wkbHex));
  }
  */

  private static String cleanHex(String hexStuff)
  {
    return hexStuff.replaceAll("[^0123456789ABCDEFabcdef]", "");
  }
  
  private static Geometry readWKTFile(String filename, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    return readWKTString(FileUtil.readText(filename), geomFact);
  }
  
  /**
   * Reads one or more WKT geometries from a string.
   * 
   * @param wkt
   * @param geomFact
   * @return the geometry read
   * @throws ParseException
   * @throws IOException
   */
  public static Geometry readWKTString(String wkt, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKTReader reader = new WKTReader(geomFact);
    WKTFileReader fileReader = new WKTFileReader(new StringReader(wkt), reader);
    List geomList = fileReader.read();
    
    if (geomList.size() == 1)
      return (Geometry) geomList.get(0);
    
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }
  
  public static Geometry readWKBHexString(String wkb, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKBReader reader = new WKBReader(geomFact);
    WKBHexFileReader fileReader = new WKBHexFileReader(new StringReader(wkb), reader);
    List geomList = fileReader.read();
    
    if (geomList.size() == 1)
      return (Geometry) geomList.get(0);
    
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }
  
  public static Geometry readGMLString(String gml, GeometryFactory geomFact)
  throws ParseException, IOException, SAXException, ParserConfigurationException 
  {
    GMLReader reader = new GMLReader();
    Geometry geom = reader.read(gml, geomFact);
    return geom;
  }
  
  private static Geometry readGeoJSONFile(String filename, GeometryFactory geomFact)
  throws ParseException, IOException, SAXException, ParserConfigurationException 
  {
    return readGeoJSONString(FileUtil.readText(filename), geomFact);
  }
  
  public static Geometry readGeoJSONString(String s, GeometryFactory geomFact)
  throws ParseException 
  {
    GeoJsonMultiReader reader = new GeoJsonMultiReader(geomFact);
    Geometry geom = reader.read(s);
    return geom;
  }
  


}
