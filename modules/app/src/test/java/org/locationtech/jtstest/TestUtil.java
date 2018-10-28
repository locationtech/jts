package org.locationtech.jtstest;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class TestUtil {
  
  public static Geometry readWKT(String wkt) {
    WKTReader reader = new WKTReader();
    try {
      return reader.read(wkt);
    } catch (ParseException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
