package com.vividsolutions.jtstest.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.GeometryExtracter;

public class SortingFunctions
{
  public static Geometry sortByLength(Geometry g)
  {
    List geoms = components(g);
    Collections.sort(geoms, new GeometryLengthComparator());
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
