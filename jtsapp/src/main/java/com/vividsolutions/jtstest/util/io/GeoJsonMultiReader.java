package com.vividsolutions.jtstest.util.io;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;

public class GeoJsonMultiReader {
  private static final String GEOJSON_FEATURECOLLECTION = "FeatureCollection";
  private static final String GEOJSON_COORDINATES = "coordinates";
  private GeometryFactory geomFact;
  private GeoJsonReader rdr;

  public GeoJsonMultiReader(GeometryFactory geomFact) {
    this.geomFact = geomFact;
    rdr = new GeoJsonReader(geomFact);
  }
  
  public Geometry read(String s) throws ParseException {
    if (isFeatureCollection(s)) {
      return readFeatureCollection(s);
    }
    return readGeometry(s);
  }

  private Geometry readGeometry(String s) throws ParseException {
    // TODO: trim string to include only Geometry object
    return rdr.read(s);
  }

  /**
   * Extracts all Geometry object substrings and reads them
   * @param s
   * @throws ParseException 
   */
  private Geometry readFeatureCollection(String s) throws ParseException {
    Pattern p = Pattern.compile("\\{[^\\{\\}]+?\\}");
    Matcher m = p.matcher(s);
    List geoms = new ArrayList();
    while (true) {
      boolean isFound = m.find();
      if (! isFound) break;
      String substr = m.group();
      if (isGeometry(substr)) {
        geoms.add(readGeometry(substr));
      }
      //System.out.println(sgeom);
    }
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
  }

  private boolean isGeometry(String s) {
    return s.indexOf(GEOJSON_COORDINATES) >= 0;
  }

  private static boolean isFeatureCollection(String s) {
    return s.indexOf(GEOJSON_FEATURECOLLECTION) >= 0;
  }
}
