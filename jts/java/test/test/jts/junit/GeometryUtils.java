package test.jts.junit;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;

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
