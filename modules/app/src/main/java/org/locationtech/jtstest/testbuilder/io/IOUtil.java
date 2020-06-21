package org.locationtech.jtstest.testbuilder.io;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.gml2.GMLWriter;

public class IOUtil {
  
  public static String toWKBHex(Geometry g)
  {
    if (g == null) return "";
    return WKBWriter.toHex((new WKBWriter().write(g)));
  }
  
  public static String toGML(Geometry g)
  {
    if (g == null) return "";
    return (new GMLWriter()).write(g);
  }
  
  public static String toWKT(Geometry g, boolean isFormatted)
  {
    if (g == null) return "";
    if (! isFormatted)
      return g.toString();
    WKTWriter writer = new WKTWriter();
    writer.setFormatted(isFormatted);
    writer.setMaxCoordinatesPerLine(5);
    return writer.write(g);
  }
}
