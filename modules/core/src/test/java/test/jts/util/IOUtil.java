/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.util;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;

public class IOUtil {
	  public static Geometry read(String wkt)
	  {
		  WKTReader rdr = new WKTReader();
	    try {
	      return rdr.read(wkt);
	    }
	    catch (ParseException ex) {
	      throw new RuntimeException(ex);
	    }
	  }

    public static List readWKT(String[] inputWKT)
    throws ParseException
    {
      ArrayList geometries = new ArrayList();
      for (int i = 0; i < inputWKT.length; i++) {
          geometries.add(IOUtil.reader.read(inputWKT[i]));
      }
      return geometries;
    }

    public static Geometry readWKT(String inputWKT)
    throws ParseException
    {
    	return IOUtil.reader.read(inputWKT);
    }

    public static Collection readWKTFile(String filename) 
    throws IOException, ParseException
    {
      WKTFileReader fileRdr = new WKTFileReader(filename, IOUtil.reader);
      List geoms = fileRdr.read();
      return geoms;
    }

    public static Collection readWKTFile(Reader rdr) 
    throws IOException, ParseException
    {
      WKTFileReader fileRdr = new WKTFileReader(rdr, IOUtil.reader);
      List geoms = fileRdr.read();
      return geoms;
    }

    public static WKTReader reader = new WKTReader();

}
