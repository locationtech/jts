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

package com.vividsolutions.jtstest.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryExtracter;


public class SortingFunctions
{
  public static Geometry sortByLength(Geometry g)
  {
    List geoms = components(g);
    Collections.sort(geoms, new GeometryLengthComparator());
    
    // annotate geometries with area
    for (Object o : geoms) {
      Geometry geom = (Geometry) o;
      geom.setUserData(geom.getLength());
    }
    
    return g.getFactory().buildGeometry(geoms);
  }
  
  private static class GeometryLengthComparator implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      Geometry g1 = (Geometry) o1;
      Geometry g2 = (Geometry) o2;
      return Double.compare(g1.getLength(), g2.getLength());
    }
  }
  
  public static Geometry sortByArea(Geometry g)
  {
    List geoms = components(g);
    Collections.sort(geoms, new GeometryAreaComparator());
    
    // annotate geometries with area
    for (Object o : geoms) {
      Geometry geom = (Geometry) o;
      geom.setUserData(geom.getArea());
    }
    
    return g.getFactory().buildGeometry(geoms);
  }
  
  private static class GeometryAreaComparator implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      Geometry g1 = (Geometry) o1;
      Geometry g2 = (Geometry) o2;
      return Double.compare(g1.getArea(), g2.getArea());
    }
  }
  
  private static List components(Geometry g)
  {
    List comp = new ArrayList();
    for (int i = 0; i < g.getNumGeometries(); i++) {
      comp.add(g.getGeometryN(i));
    }
    return comp;
  }
}
