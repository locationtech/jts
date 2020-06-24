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
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;


import test.jts.GeometryTestCase;

import java.util.HashMap;


public class GeoJsonTest extends GeometryTestCase {

  public GeoJsonWriter geoJsonWriter;

  public GeoJsonReader geoJsonReader;

  public GeoJsonTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {

    this.geoJsonWriter = new GeoJsonWriter();
    this.geoJsonReader = new GeoJsonReader();
  }

  public void testPoint() throws ParseException {
    runTest("POINT (1 2)");
  }

  public void testLineString() throws ParseException {
    runTest("LINESTRING (1 2, 10 20, 100 200)");
  }

  public void testPolygon() throws ParseException {
    runTest("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))");
  }

  public void testPolygonWithHole() throws ParseException {
    runTest("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0), (1 1, 1 10, 10 10, 10 1, 1 1) )");
  }

  public void testMultiPoint() throws ParseException {
    runTest("MULTIPOINT ((0 0), (1 4), (100 200))");
  }

  public void testMultiLineString() throws ParseException {
    runTest("MULTILINESTRING ((0 0, 1 10), (10 10, 20 30), (123 123, 456 789))");
  }

  public void testMultiPolygon() throws ParseException {
    runTest("MULTIPOLYGON ( ((0 0, 100 0, 100 100, 0 100, 0 0), (1 1, 1 10, 10 10, 10 1, 1 1) ), ((200 200, 200 250, 250 250, 250 200, 200 200)) )");
  }

  public void testGeometryCollection() throws ParseException {
    runTest("GEOMETRYCOLLECTION ( POINT ( 1 1), LINESTRING (0 0, 10 10), POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0)) )");
  }

  public void testNestedGeometryCollection() throws ParseException {
    runTest("GEOMETRYCOLLECTION ( POINT (20 20), GEOMETRYCOLLECTION ( POINT ( 1 1), LINESTRING (0 0, 10 10), POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0)) ) )");
  }

  // empty atomic geometries are not supported in GeoJSON
  
  public void testMultiPointEmpty() throws ParseException {
    runTest("MULTIPOINT EMPTY");
  }

  public void testMultiLineStringEmpty() throws ParseException {
    runTest("MULTILINESTRING EMPTY");
  }

  public void testMultiPolygonEmpty() throws ParseException {
    runTest("MULTIPOLYGON EMPTY");
  }

  public void testGeometryCollectionEmpty() throws ParseException {
    runTest("GEOMETRYCOLLECTION EMPTY");
  }

  public void testFeature() throws ParseException {
    runTest(new Feature("123", read("POINT (1 1)"), new HashMap<String, Object>()),
            "{'type':'Feature','id':'123','properties':{},'geometry':{'type':'Point','coordinates':[1,1]}}");
  }

  public void testFeatureNullId() throws ParseException {
    runTest(new Feature(null, read("POINT (1 1)"), new HashMap<String, Object>()),
            "{'type':'Feature','properties':{},'geometry':{'type':'Point','coordinates':[1,1]}}");
  }

  public void testFeatureNullProperties() throws ParseException {
    runTest(new Feature("test", read("POINT (1 1)"), null),
            "{'type':'Feature','id':'test','properties':null,'geometry':{'type':'Point','coordinates':[1,1]}}");
  }

  public void testFeatureCollection() throws ParseException {
    FeatureCollection collection = new FeatureCollection(
            new Feature(null, read("POINT (1 1)"), null));
    runTest(collection,
            "{'type':'FeatureCollection','features':[{'type':'Feature','properties':null,'geometry':{'type':'Point','coordinates':[1,1]}}]}");
  }

  public void testFeatureCollectionEmpty() throws ParseException {
    FeatureCollection collection = new FeatureCollection();
    runTest(collection, "{'type':'FeatureCollection','features':[]}");
  }

  private void runTest(String wkt) throws ParseException {
    Geometry expected = read(wkt);
    String json = this.geoJsonWriter.write(expected);
    Geometry result = this.geoJsonReader.read(json);
    checkEqual(result, expected);
  }

  private void runTest(Feature feature, String expectedGeojson) throws ParseException {
    geoJsonWriter.setEncodeCRS(false);
    String json = geoJsonWriter.write(feature);
    String escaped = json.replace('"', '\'');
    assertEquals(escaped, expectedGeojson);
    Feature result = geoJsonReader.readFeature(json);
    assertEquals(feature, result);
  }

  private void runTest(FeatureCollection collection, String expectedGeojson) throws ParseException {
    geoJsonWriter.setEncodeCRS(false);
    String json = geoJsonWriter.write(collection);
    String escaped = json.replace('"', '\'');
    assertEquals(escaped, expectedGeojson);
    FeatureCollection result = geoJsonReader.readFeatureCollection(json);
    assertEquals(collection, result);
  }
}
