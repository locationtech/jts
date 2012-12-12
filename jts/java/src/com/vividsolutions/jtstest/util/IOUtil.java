package com.vividsolutions.jtstest.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBHexFileReader;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTFileReader;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jtstest.testbuilder.io.shapefile.Shapefile;

public class IOUtil 
{
  public static Geometry readGeometriesFromFile(String filename, GeometryFactory geomFact)
  throws Exception, IOException 
  {
    String ext = FileUtil.extension(filename);
    if (ext.equalsIgnoreCase(".shp"))
      return readGeometriesFromShapefile(filename, geomFact);
    if (ext.equalsIgnoreCase(".wkb"))
      return readGeometryFromWKBHexFile(filename, geomFact);
    return readGeometriesFromWKTFile(filename, geomFact);
  }
    
  private static Geometry readGeometriesFromShapefile(String filename, GeometryFactory geomFact)
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
  
  private static Geometry readGeometryFromWKBHexFile(String filename, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    return readGeometryFromWKBHexString(FileUtil.readText(filename), geomFact);
  }
  
  private static Geometry readGeometryFromWKBHexString(String wkbHexFile, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKBReader reader = new WKBReader(geomFact);
    String wkbHex = cleanHex(wkbHexFile);
    return reader.read(WKBReader.hexToBytes(wkbHex));
  }

  private static String cleanHex(String hexStuff)
  {
    return hexStuff.replaceAll("[^0123456789ABCDEFabcdef]", "");
  }
  
  private static Geometry readGeometriesFromWKTFile(String filename, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    return readGeometriesFromWKTString(FileUtil.readText(filename), geomFact);
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
  public static Geometry readGeometriesFromWKTString(String wkt, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKTReader reader = new WKTReader(geomFact);
    WKTFileReader fileReader = new WKTFileReader(new StringReader(wkt), reader);
    List geomList = fileReader.read();
    
    if (geomList.size() == 1)
      return (Geometry) geomList.get(0);
    
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }
  
  public static Geometry readGeometriesFromWKBHexString(String wkb, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKBReader reader = new WKBReader(geomFact);
    WKBHexFileReader fileReader = new WKBHexFileReader(new StringReader(wkb), reader);
    List geomList = fileReader.read();
    
    if (geomList.size() == 1)
      return (Geometry) geomList.get(0);
    
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }
  


}
