package com.vividsolutions.jtstest.function;


import com.vividsolutions.jts.dissolve.LineDissolver;
import com.vividsolutions.jts.geom.Geometry;

public class DissolveFunctions {
  
  public static Geometry dissolve(Geometry geom)
  {
    return LineDissolver.dissolve(geom);
  }
}
