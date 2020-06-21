package org.locationtech.jtstest.testbuilder.ui;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

public class IOUtil {
  
  public static String toWKBHex(Geometry g)
  {
    if (g == null) return "";
    return WKBWriter.toHex((new WKBWriter().write(g)));
  }
}
