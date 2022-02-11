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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;

import test.jts.GeometryTestCase;

import java.util.Arrays;

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

  public void testEmptyFeatureCollection() throws ParseException {
    runTest("{ \"type\": \"FeatureCollection\", \"features\": [] }", "GEOMETRYCOLLECTION EMPTY");
  }

  public void testFeatureCollection() throws ParseException {
    final String featureCollectionTemplate = "{ \"type\": \"FeatureCollection\", \"features\": [ %s, %s ] }";
    String polygonFeature = "{ \"type\": \"Feature\", \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [ [ [ 10, 20, 0 ], [ 11, 21, 0 ], [ 10, 20, 0 ] ] ] }, \"properties\": { \"name\": \"Some polygonGeometry property\" } }";
    String pointFeature = "{ \"type\": \"Feature\", \"geometry\": { \"type\": \"Point\", \"coordinates\": [ 12, 13, 1 ] }, \"properties\": { \"name\": \"Some point property\" } }";
    final String featureCollection = String.format(featureCollectionTemplate, polygonFeature, pointFeature);

    final Geometry geometryCollection = geoJsonRdr.read(featureCollection);
    assertEquals(GeometryCollection.TYPENAME_GEOMETRYCOLLECTION, geometryCollection.getGeometryType());
    assertEquals(2, geometryCollection.getNumGeometries());

    final Geometry polygon = geometryCollection.getGeometryN(0);
    assertEquals(GeometryCollection.TYPENAME_POLYGON, polygon.getGeometryType());
    assertEquals(Polygon.class, polygon.getClass());
    assertEquals(1, polygon.getNumGeometries());
    final Coordinate[] polygonCoordinates = polygon.getCoordinates();
    assertEquals(3, polygonCoordinates.length);
    final Coordinate[] expectedPolygonCoordinates = {
            new Coordinate(10, 20, 0),
            new Coordinate(11, 21, 0),
            new Coordinate(10, 20, 0)
    };
    assertTrue(Arrays.equals(expectedPolygonCoordinates, polygonCoordinates));

    final Geometry point = geometryCollection.getGeometryN(1);
    assertEquals(GeometryCollection.TYPENAME_POINT, point.getGeometryType());
    assertEquals(Point.class, point.getClass());
    assertEquals(1, point.getNumGeometries());
    final Coordinate[] pointCoordinates = point.getCoordinates();
    assertEquals(1, pointCoordinates.length);
    final Coordinate[] expectedPointCoordinates = {new Coordinate(12, 13, 1)};
    assertTrue(Arrays.equals(expectedPointCoordinates, pointCoordinates));
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
