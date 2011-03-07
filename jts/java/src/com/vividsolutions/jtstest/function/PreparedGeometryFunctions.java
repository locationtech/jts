package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class PreparedGeometryFunctions 
{
  private static PreparedGeometry createPG(Geometry g)
  {
    return (new PreparedGeometryFactory()).create(g);
  }
  
  public static boolean preparedIntersects(Geometry g1, Geometry g2)
  {
    return createPG(g1).intersects(g2);
  }
  
  public static boolean intersects(Geometry g1, Geometry g2)
  {
    return g1.intersects(g2);
  }
  

}
