/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.io.geojson;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonWriter;


import test.jts.GeometryTestCase;


public class GeoJsonWriterTest extends GeometryTestCase {

  public GeoJsonWriter geoJsonWriter;

  public GeoJsonWriterTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
    this.geoJsonWriter = new GeoJsonWriter();
  }

  public void testCRS() throws ParseException {
    runTest("POINT (1 2)", 1234,
        "{'type':'Point','coordinates':[1,2],'crs':{'type':'name','properties':{'name':'EPSG:1234'}}}"
        );
  }

  public void testPoint() throws ParseException {
    runTest("POINT (1 2)",
        "{'type':'Point','coordinates':[1,2]}"
        );
  }

  public void testLineString() throws ParseException {
    runTest("LINESTRING (1 2, 10 20, 100 200)",
        "{'type':'LineString','coordinates':[[1,2],[10,20],[100,200]]}");
  }

  public void testPolygon() throws ParseException {
    runTest("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))",
        "{'type':'Polygon','coordinates':[[[0.0,0.0],[100,0.0],[100,100],[0.0,100],[0.0,0.0]]]}");
  }

  public void testPolygonWithHole() throws ParseException {
    runTest("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0), (1 1, 1 10, 10 10, 10 1, 1 1) )",
        "{'type':'Polygon','coordinates':[[[0.0,0.0],[100,0.0],[100,100],[0.0,100],[0.0,0.0]],[[1,1],[1,10],[10,10],[10,1],[1,1]]]}");
  }

  public void testMultiPoint() throws ParseException {
    runTest("MULTIPOINT ((0 0), (1 4), (100 200))",
        "{'type':'MultiPoint','coordinates':[[0.0,0.0],[1,4],[100,200]]}");
  }

  public void testMultiLineString() throws ParseException {
    runTest("MULTILINESTRING ((0 0, 1 10), (10 10, 20 30), (123 123, 456 789))",
        "{'type':'MultiLineString','coordinates':[[[0.0,0.0],[1,10]],[[10,10],[20,30]],[[123,123],[456,789]]]}");
  }

  public void testMultiPolygon() throws ParseException {
    runTest("MULTIPOLYGON ( ((0 0, 100 0, 100 100, 0 100, 0 0), (1 1, 1 10, 10 10, 10 1, 1 1) ), ((200 200, 200 250, 250 250, 250 200, 200 200)) )",
        "{'type':'MultiPolygon','coordinates':[[[[0.0,0.0],[100,0.0],[100,100],[0.0,100],[0.0,0.0]],[[1,1],[1,10],[10,10],[10,1],[1,1]]],[[[200,200],[200,250],[250,250],[250,200],[200,200]]]]}"
        );
  }

  public void testGeometryCollection() throws ParseException {
    runTest("GEOMETRYCOLLECTION ( POINT ( 1 1), LINESTRING (0 0, 10 10), POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0)) )",
        "{'type':'GeometryCollection','geometries':[{'type':'Point','coordinates':[1,1]},{'type':'LineString','coordinates':[[0.0,0.0],[10,10]]},{'type':'Polygon','coordinates':[[[0.0,0.0],[100,0.0],[100,100],[0.0,100],[0.0,0.0]]]}]}"
        );
  }

  // empty atomic geometries are not supported in GeoJSON
  
  public void testMultiPointEmpty() throws ParseException {
    runTest("MULTIPOINT EMPTY", "{'type':'MultiPoint','coordinates':[]}");
  }

  public void testMultiLineStringEmpty() throws ParseException {
    runTest("MULTILINESTRING EMPTY","{'type':'MultiLineString','coordinates':[]}");
  }

  public void testMultiPolygonEmpty() throws ParseException {
    runTest("MULTIPOLYGON EMPTY","{'type':'MultiPolygon','coordinates':[]}");
  }

  public void testGeometryCollectionEmpty() throws ParseException {
    runTest("GEOMETRYCOLLECTION EMPTY","{'type':'GeometryCollection','geometries':[]}");
  }

  private void runTest(String wkt) throws ParseException {
    Geometry expected = read(wkt);
    geoJsonWriter.setEncodeCRS(false);
    String json = this.geoJsonWriter.write(expected);
    System.out.println('"' + json.replace('"', '\'') + '"');
    //checkEqual(result, expected);
  }
 
  private void runTest(String wkt, String expectedGeojson) throws ParseException {
    runTest(wkt, 0, false, expectedGeojson);
  }

  private void runTest(String wkt, int srid, String expectedGeojson) throws ParseException {
    runTest(wkt, srid, true, expectedGeojson);
  }

  private void runTest(String wkt, int srid, boolean encodeCRS, String expectedGeojson) throws ParseException {
    Geometry geom = read(wkt);
    geom.setSRID(srid);
    geoJsonWriter.setEncodeCRS(encodeCRS);
    String json = this.geoJsonWriter.write(geom);
    json = json.replace('"', '\'');
    assertEquals(json, expectedGeojson);
  }

}
