/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.function;

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
