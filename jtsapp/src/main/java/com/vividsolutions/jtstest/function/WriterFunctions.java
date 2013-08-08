package com.vividsolutions.jtstest.function;

import java.lang.reflect.InvocationTargetException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.gml2.GMLWriter;
import com.vividsolutions.jts.io.kml.KMLWriter;
import com.vividsolutions.jtstest.util.ClassUtil;

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

  public static String writeOra(Geometry g) throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    if (g == null) return "";
    // call dynamically to avoid dependency on OraWriter
    String sql = (String) ClassUtil.dynamicCall("com.vividsolutions.jts.io.oracle.OraWriter", 
        "writeSQL", 
        new Class[] { Geometry.class }, 
        new Object[] { g });
    return sql;
    //return (new OraWriter(null)).writeSQL(g);
  }
  
  public static String writeWKB(Geometry g)
  {
    if (g == null) return "";
    return WKBWriter.toHex((new WKBWriter().write(g)));
  }
  

}
