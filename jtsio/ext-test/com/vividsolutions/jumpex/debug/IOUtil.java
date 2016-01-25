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

package com.vividsolutions.jumpex.debug;


import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.*;

/**
 * 
 * Jump IO Utilities
 *
 * @author Vivid Solutions. 
 */
public class IOUtil {

  /**
 * @param filename
 * @return FeatureCollection
 * @throws Exception
 */
public static FeatureCollection loadShapefile(String filename)
      throws Exception
  {
    ShapefileReader rdr = new ShapefileReader();
    DriverProperties dp = new DriverProperties();
    dp.set("File", filename);
    return rdr.read(dp);
  }

  /**
 * @param fc
 * @param filename
 * @throws Exception
 */
public static void saveShapefile(FeatureCollection fc, String filename)
      throws Exception
  {
    ShapefileWriter writer = new ShapefileWriter();
    DriverProperties dp = new DriverProperties();
    dp.set("File", filename);
    writer.write(fc, dp);
  }
  /**
 * @param fc
 */
public static void print(FeatureCollection fc)
  {
    List featList = fc.getFeatures();
    for (Iterator i = featList.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      System.out.println(f.getGeometry());
    }
  }

}