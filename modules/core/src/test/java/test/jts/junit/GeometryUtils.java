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

package test.jts.junit;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;


public class GeometryUtils 
{
	//TODO: allow specifying GeometryFactory
	
	public static WKTReader reader = new WKTReader();
	
  public static List readWKT(String[] inputWKT)
  throws ParseException
  {
    ArrayList geometries = new ArrayList();
    for (int i = 0; i < inputWKT.length; i++) {
        geometries.add(reader.read(inputWKT[i]));
    }
    return geometries;
  }
  
  public static Geometry readWKT(String inputWKT)
  throws ParseException
  {
  	return reader.read(inputWKT);
  }
  
  public static Collection readWKTFile(String filename) 
  throws IOException, ParseException
  {
    WKTFileReader fileRdr = new WKTFileReader(filename, reader);
    List geoms = fileRdr.read();
    return geoms;
  }
  
  public static Collection readWKTFile(Reader rdr) 
  throws IOException, ParseException
  {
    WKTFileReader fileRdr = new WKTFileReader(rdr, reader);
    List geoms = fileRdr.read();
    return geoms;
  }
  

  public static boolean isEqual(Geometry a, Geometry b)
  {
  	Geometry a2 = normalize(a);
  	Geometry b2 = normalize(b);
  	return a2.equalsExact(b2);
  }
  
  public static Geometry normalize(Geometry g)
  {
  	Geometry g2 = (Geometry) g.clone();
  	g2.normalize();
  	return g2;
  }
}
