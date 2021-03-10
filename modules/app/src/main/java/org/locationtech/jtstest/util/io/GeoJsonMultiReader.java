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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;


public class GeoJsonMultiReader {
  private static final String GEOJSON_FEATURECOLLECTION = "FeatureCollection";
  private static final String GEOJSON_COORDINATES = "coordinates";
  private GeometryFactory geomFact;
  private GeoJsonReader rdr;

  public GeoJsonMultiReader(GeometryFactory geomFact) {
    this.geomFact = geomFact;
    rdr = new GeoJsonReader(geomFact);
  }
  
  public List<Geometry> readList(String s) throws ParseException {
    if (isFeatureCollection(s)) {
      return readFeatureCollectionList(s);
    }
    return readGeometryList(s);
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

  private List<Geometry> readGeometryList(String s) throws ParseException {
    // TODO: trim string to include only Geometry object
    Geometry geom = rdr.read(s);
    List<Geometry> geomList = new ArrayList<Geometry>();
    geomList.add(geom);
    return geomList;
  }

  private List<Geometry> readFeatureCollectionList(String s) throws ParseException {
    Pattern p = Pattern.compile("\\{[^\\{\\}]+?\\}");
    Matcher m = p.matcher(s);
    List<Geometry> geoms = new ArrayList<Geometry>();
    while (true) {
      boolean isFound = m.find();
      if (! isFound) break;
      String substr = m.group();
      if (isGeometry(substr)) {
        geoms.add(readGeometry(substr));
      }
      //System.out.println(sgeom);
    }
    return geoms;
  }
  
  /**
   * Extracts all Geometry object substrings and reads them
   * @param s
   * @throws ParseException 
   */
  private Geometry readFeatureCollection(String s) throws ParseException {
    List<Geometry> geoms = readFeatureCollectionList(s);
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
  }

  private boolean isGeometry(String s) {
    return s.indexOf(GEOJSON_COORDINATES) >= 0;
  }

  private static boolean isFeatureCollection(String s) {
    return s.indexOf(GEOJSON_FEATURECOLLECTION) >= 0;
  }
}
