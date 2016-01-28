/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jtstest.function;

import java.lang.reflect.InvocationTargetException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.gml2.GMLWriter;
import org.locationtech.jts.io.kml.KMLWriter;
import org.locationtech.jtstest.util.ClassUtil;


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
