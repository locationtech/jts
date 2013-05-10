package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.gml2.GMLWriter;
import com.vividsolutions.jts.io.kml.KMLWriter;
import com.vividsolutions.jts.io.oracle.OraWriter;

public class WriterFunctions 
{
  public static String writeKML(Geometry geom)
  {
    if (geom == null) return "";
    KMLWriter writer = new KMLWriter();
    return writer.write(geom);
  }
  
  public static String writeGML(Geometry geom)
  {
    if (geom == null) return "";
    return (new GMLWriter()).write(geom);
  }

  public static String writeOra(Geometry g)
  {
    if (g == null) return "";
    return (new OraWriter(null)).writeSQL(g);
  }
  
  public static String writeWKB(Geometry g)
  {
    if (g == null) return "";
    return WKBWriter.toHex((new WKBWriter().write(g)));
  }
}
