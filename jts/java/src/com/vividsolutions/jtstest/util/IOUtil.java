package com.vividsolutions.jtstest.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTFileReader;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jtstest.testbuilder.io.shapefile.Shapefile;

public class IOUtil 
{
  public static Geometry readMultipleGeometriesFromFile(String filename, GeometryFactory geomFact)
  throws Exception, IOException 
  {
    String ext = FileUtil.extension(filename);
    if (ext.equalsIgnoreCase(".shp"))
      return readMultipleGeometriesFromShapefile(filename, geomFact);
    return readMultipleGeometryFromWKT(filename, geomFact);
  }
    
  private static Geometry readMultipleGeometriesFromShapefile(String filename, GeometryFactory geomFact)
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
  
  private static Geometry readMultipleGeometryFromWKT(String filename, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    return readMultipleGeometryFromWKTString(FileUtil.readText(filename), geomFact);
  }
  
  private static Geometry readMultipleGeometryFromWKTString(String wkt, GeometryFactory geomFact)
  throws ParseException, IOException 
  {
    WKTReader reader = new WKTReader(geomFact);
    WKTFileReader fileReader = new WKTFileReader(new StringReader(wkt), reader);
    List geomList = fileReader.read();
    
    if (geomList.size() == 1)
      return (Geometry) geomList.get(0);
    
    // TODO: turn polygons into a GC   
    return geomFact.buildGeometry(geomList);
  }
  


}
