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

package org.locationtech.jts.io.kml;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


public class KMLWriterTest extends TestCase 
{
  PrecisionModel precisionModel = new PrecisionModel(1);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader rdr = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(KMLWriterTest.class);
  }

  public KMLWriterTest(String name) { super(name); }

  public void testPoint()
  {
    checkEqual("POINT (1 1)", 
        "<Point><coordinates>1.0,1.0</coordinates></Point>");
  }

  public void testLine()
  {
    checkEqual("LINESTRING (1 1, 2 2)", 
        "<LineString><coordinates>1.0,1.0 2.0,2.0</coordinates></LineString>");
  }

  public void testPolygon()
  {
    checkEqual("POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))", 
        "<Polygon><outerBoundaryIs><LinearRing><coordinates>1.0,1.0 2.0,1.0 2.0,2.0 1.0,2.0 1.0,1.0</coordinates></LinearRing></outerBoundaryIs></Polygon>");
  }

  public void testPolygonWithHole()
  {
    checkEqual("POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (2 8, 8 8, 8 2, 2 2, 2 8))", 
        "<Polygon><outerBoundaryIs><LinearRing><coordinates>1.0,9.0 9.0,9.0 9.0,1.0 1.0,1.0 1.0,9.0</coordinates></LinearRing></outerBoundaryIs><innerBoundaryIs><LinearRing><coordinates>2.0,8.0 8.0,8.0 8.0,2.0 2.0,2.0 2.0,8.0</coordinates></LinearRing></innerBoundaryIs></Polygon>");
  }

  public void testMultiPoint()
  {
    checkEqual("MULTIPOINT ((1 1), (2 2))", 
        "<MultiGeometry><Point><coordinates>1.0,1.0</coordinates></Point><Point><coordinates>2.0,2.0</coordinates></Point></MultiGeometry>");
  }

  public void testMultiLineString()
  {
    checkEqual("MULTILINESTRING ((2 9, 2 2), (5 5, 8 5))", 
        "<MultiGeometry><LineString><coordinates>2.0,9.0 2.0,2.0</coordinates></LineString><LineString><coordinates>5.0,5.0 8.0,5.0</coordinates></LineString></MultiGeometry>");
  }

  public void testMultiPolygon()
  {
    checkEqual("MULTIPOLYGON (((2 9, 5 9, 5 5, 2 5, 2 9)), ((6 4, 8 4, 8 2, 6 2, 6 4)))", 
        "<MultiGeometry><Polygon><outerBoundaryIs><LinearRing><coordinates>2.0,9.0 5.0,9.0 5.0,5.0 2.0,5.0 2.0,9.0</coordinates></LinearRing></outerBoundaryIs></Polygon><Polygon><outerBoundaryIs><LinearRing><coordinates>6.0,4.0 8.0,4.0 8.0,2.0 6.0,2.0 6.0,4.0</coordinates></LinearRing></outerBoundaryIs></Polygon></MultiGeometry>");
  }

  public void testGeometryCollection()
  {
    checkEqual("GEOMETRYCOLLECTION (LINESTRING (1 9, 1 2, 3 2), POLYGON ((3 9, 5 9, 5 7, 3 7, 3 9)), POINT (5 5))", 
        "<MultiGeometry><LineString><coordinates>1.0,9.0 1.0,2.0 3.0,2.0</coordinates></LineString><Polygon><outerBoundaryIs><LinearRing><coordinates>3.0,9.0 5.0,9.0 5.0,7.0 3.0,7.0 3.0,9.0</coordinates></LinearRing></outerBoundaryIs></Polygon><Point><coordinates>5.0,5.0</coordinates></Point></MultiGeometry>");
  }

  public void testExtrudeAltitudeLineString()
  {
    KMLWriter kmlWriter = new KMLWriter();
    kmlWriter.setExtrude(true);
    kmlWriter.setAltitudeMode(KMLWriter.ALTITUDE_MODE_ABSOLUTE);
    checkEqual(kmlWriter, "LINESTRING (1 1, 2 2)", 
        "<LineString><extrude>1</extrude><altitudeMode>absolute</altitudeMode><coordinates>1.0,1.0 2.0,2.0</coordinates></LineString>");
  }

  public void testExtrudeTesselateLineString()
  {
    KMLWriter kmlWriter = new KMLWriter();
    kmlWriter.setExtrude(true);
    kmlWriter.setTesselate(true);
    //kmlWriter.setAltitudeMode(KMLWriter.ALTITUDE_MODE_ABSOLUTE);
    checkEqual(kmlWriter, "LINESTRING (1 1, 2 2)", 
        "<LineString><extrude>1</extrude><tesselate>1</tesselate><coordinates>1.0,1.0 2.0,2.0</coordinates></LineString>");
  }

  public void testExtrudeAltitudePolygon()
  {
    KMLWriter kmlWriter = new KMLWriter();
    kmlWriter.setExtrude(true);
    kmlWriter.setAltitudeMode(KMLWriter.ALTITUDE_MODE_ABSOLUTE);
    checkEqual(kmlWriter, "POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))", 
        "<Polygon><extrude>1</extrude><altitudeMode>absolute</altitudeMode><outerBoundaryIs><LinearRing><coordinates>1.0,1.0 2.0,1.0 2.0,2.0 1.0,2.0 1.0,1.0</coordinates></LinearRing></outerBoundaryIs></Polygon>");
  }

  public void testExtrudeGeometryCollection()
  {
    KMLWriter kmlWriter = new KMLWriter();
    kmlWriter.setExtrude(true);
    checkEqual(kmlWriter, "GEOMETRYCOLLECTION (LINESTRING (1 9, 1 2, 3 2), POLYGON ((3 9, 5 9, 5 7, 3 7, 3 9)), POINT (5 5))", 
        "<MultiGeometry><LineString><extrude>1</extrude><coordinates>1.0,9.0 1.0,2.0 3.0,2.0</coordinates></LineString><Polygon><extrude>1</extrude><outerBoundaryIs><LinearRing><coordinates>3.0,9.0 5.0,9.0 5.0,7.0 3.0,7.0 3.0,9.0</coordinates></LinearRing></outerBoundaryIs></Polygon><Point><extrude>1</extrude><coordinates>5.0,5.0</coordinates></Point></MultiGeometry>");
  }

  public void testPrecision()
  {
    KMLWriter kmlWriter = new KMLWriter();
    kmlWriter.setPrecision(1);
    checkEqual(kmlWriter, "LINESTRING (1.0001 1.1234, 2.5555 2.99999)", 
        " <LineString><coordinates>1,1.1 2.6,3</coordinates></LineString>");
  }


  private void checkEqual(String wkt, String expectedKML) {
    KMLWriter kmlWriter = new KMLWriter();
    checkEqual(kmlWriter, wkt, expectedKML);
  }

  private void checkEqual(KMLWriter kmlWriter, String wkt, String expectedKML) {
    try {
      Geometry geom = rdr.read(wkt);
      checkEqual(kmlWriter, geom, expectedKML);
    } catch (ParseException e) {
      throw new RuntimeException("ParseException: " + e.getMessage());
    }
  }

  private void checkEqual(KMLWriter kmlWriter, Geometry geom, String expectedKML) {
    String kml = kmlWriter.write(geom);
    String kmlNorm = normalizeKML(kml);
    String expectedKMLNorm = normalizeKML(expectedKML);
    boolean isEqual = kmlNorm.equalsIgnoreCase(expectedKMLNorm);
    if (! isEqual) {
      System.out.println("\nGenerated KML:  " + kmlNorm + "\n  Expected KML: " + expectedKMLNorm);
    }
    assertTrue(isEqual);
  }

  /**
   * Normalizes an XML string by converting all whitespace to a single blank char.
   * 
   * @param expectedKML
   * @return
   */
  private String normalizeKML(String kml) {
    String condenseSpace = kml.replaceAll("\\s+", " ").trim();
    String removeRedundantSpace = condenseSpace.replaceAll("> <", "><");
    return removeRedundantSpace;
  }
}
