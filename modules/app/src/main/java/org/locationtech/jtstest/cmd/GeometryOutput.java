/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.cmd;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.locationtech.jts.io.gml2.GMLWriter;
import org.locationtech.jtstest.testbuilder.io.SVGTestWriter;

/**
 * Outputs geometry in a specified format.
 * 
 * @author Admin
 *
 */
public class GeometryOutput {
  private CommandOutput out;

  public GeometryOutput(CommandOutput out) {
    this.out = out;
  }
  
  public void printGeometry(Geometry geom, int srid, String outputFormat) {
    String txt = null;
    if (outputFormat.equalsIgnoreCase(CommandOptions.FORMAT_WKT)
        || outputFormat.equalsIgnoreCase(CommandOptions.FORMAT_TXT)) {
      txt = geom.toString();
    }
    else if (outputFormat.equalsIgnoreCase(CommandOptions.FORMAT_WKB)) {
      txt = writeWKB(geom, srid); //
    }
    else if (outputFormat.equalsIgnoreCase(CommandOptions.FORMAT_GML)) {
      txt = (new GMLWriter()).write(geom);
    }
    else if (outputFormat.equalsIgnoreCase(CommandOptions.FORMAT_GEOJSON)) {
      txt = writeGeoJSON(geom);
    }
    else if (outputFormat.equalsIgnoreCase(CommandOptions.FORMAT_SVG)) {
      txt = SVGTestWriter.writeSVG(geom, null);
    }
    
    if (txt == null) return;
    out.println(txt);
  }

  private String writeWKB(Geometry geom, int srid) {
    WKBWriter writer;
    if (JTSOpRunner.isCustomSRID(srid)) {
      writer = new WKBWriter(2, true);
    }
    else {
      writer = new WKBWriter();
    }
    return WKBWriter.toHex(writer.write(geom));
  }

  private static String writeGeoJSON(Geometry geom) {
    GeoJsonWriter writer = new GeoJsonWriter();
    writer.setEncodeCRS(false);
    return writer.write(geom);
  }
  
  public static String writeGeometrySummary(String label,
      Geometry g)
  {
    if (g == null) return "";
    return String.format("%s: %s (%d)", label, g.getGeometryType().toUpperCase(), g.getNumPoints());
  }

  public static String writeGeometrySummary(String label,
      List<Geometry> geoms)
  {
    if (geoms == null) return "";
    int nVert = getNumPoints(geoms);
    String geomTypes = getTypesSummary(geoms);
    return writeGeometrySummary(label, geoms.size(), geomTypes, nVert);
  }

  public static String writeGeometrySummary(String label,
      int numGeoms, String geomTypes, int numVert)
  {
    return String.format("%s : %d %s, %d vertices", label, numGeoms, geomTypes, numVert);
  }

  private static int getNumPoints(List<Geometry> geoms) {
    int n = 0;
    for (Geometry g : geoms ) {
      n += g.getNumPoints();
    }
    return n;
  }

  private static String getTypesSummary(List<Geometry> geoms) {
    
    int numPoint = 0;
    int numMultiPoint = 0;
    int numLineString = 0;
    int numMultiLineString = 0;
    int numPolygon = 0;
    int numMultiPolygon = 0;
    int numGeometryCollection = 0;
    
    for (Geometry g : geoms ) {
      if (g instanceof Point) numPoint++;
      else if (g instanceof MultiPoint) numMultiPoint++;
      else if (g instanceof LineString) numLineString++;
      else if (g instanceof MultiLineString) numMultiLineString++;
      else if (g instanceof Polygon) numPolygon++;
      else if (g instanceof MultiPolygon) numMultiPolygon++;
      else if (g instanceof GeometryCollection) numGeometryCollection++;
    }
    StringBuilder sb = new StringBuilder();
    addName("Point", numPoint, sb);
    addName("MultiPoint", numMultiPoint, sb);
    addName("LineString", numLineString, sb);
    addName("MultiLineString", numMultiLineString, sb);
    addName("Polygon", numPolygon, sb);
    addName("MultiPolygon", numMultiPolygon, sb);
    addName("GeometryCollection", numGeometryCollection, sb);
    return sb.toString();
  }
  
  private static void addName(String name, int num, StringBuilder sb) {
    if (num <= 0) return;
    if (sb.length() > 0) sb.append("/");
    sb.append(name);
    if (num > 1) sb.append("s");
  }
}
