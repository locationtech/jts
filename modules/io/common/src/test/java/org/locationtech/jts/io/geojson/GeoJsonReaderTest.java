/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.io.geojson;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import test.jts.GeometryTestCase;

public class GeoJsonReaderTest extends GeometryTestCase {

  private GeoJsonReader geoJsonRdr;

  public GeoJsonReaderTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
    this.geoJsonRdr = new GeoJsonReader();
  }

  public void testEmptyArray() throws ParseException {
    runParseEx("[]");
  }
 
  public void testEmptyObject() throws ParseException {
    runParseEx("{}");
  }

  public void testEmptyCoordinatesPoint() throws ParseException {
    runTest("{\"type\":\"Point\",\"coordinates\":[]}", "POINT EMPTY");
  }

  public void testNullCoordinatesPoint() throws ParseException {
    runTest("{\"type\":\"Point\",\"coordinates\":null}", "POINT EMPTY");
  }

  public void testEmptyCoordinatesLineString() throws ParseException {
    runTest("{\"type\":\"LineString\",\"coordinates\":[]}", "LINESTRING EMPTY");
  }

  public void testNullCoordinatesLineString() throws ParseException {
    runTest("{\"type\":\"LineString\",\"coordinates\":null}", "LINESTRING EMPTY");
  }

  public void testEmptyCoordinatesPolygon() throws ParseException {
    runTest("{\"type\":\"Polygon\",\"coordinates\":[]}", "POLYGON EMPTY");
  }

  public void testNullCoordinatesPolygon() throws ParseException {
    runTest("{\"type\":\"Polygon\",\"coordinates\":null}", "POLYGON EMPTY");
  }

  private void runParseEx(String json) {
    try {
      Geometry geom = geoJsonRdr.read(json);
      fail();
    }
    catch (ParseException ex) {
    }
  }

  private void runTest(String geojson, String expectedWkt) throws ParseException {
    runTest(geojson, expectedWkt, 0, false);
  }

  private void runTest(String geojson, String expectedWkt, int srid, boolean encodeCRS) throws ParseException {
    Geometry expectedGeom = read(expectedWkt);
    expectedGeom.setSRID(srid);
    Geometry geom = geoJsonRdr.read(geojson);
    assertEquals(expectedGeom, geom);
  }

}
