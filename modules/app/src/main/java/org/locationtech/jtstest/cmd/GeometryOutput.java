package org.locationtech.jtstest.cmd;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
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
      List<Geometry> g)
  {
    if (g == null) return "";
    int nVert = getNumPoints(g);
    return writeGeometrySummary(label, g.size(), nVert);
  }

  public static String writeGeometrySummary(String label,
      int numGeoms, int numVert)
  {
    return String.format("%s : %d geometries, %d vertices", label, numGeoms, numVert);
  }

  private static int getNumPoints(List<Geometry> geoms) {
    int n = 0;
    for (Geometry g : geoms ) {
      n += g.getNumPoints();
    }
    return n;
  }

}
