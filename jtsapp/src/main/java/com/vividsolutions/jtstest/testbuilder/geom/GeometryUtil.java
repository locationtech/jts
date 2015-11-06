package com.vividsolutions.jtstest.testbuilder.geom;

import com.vividsolutions.jts.geom.*;

public class GeometryUtil {

  public static String structureSummary(Geometry g)
  {
    String structure = "";
    if (g instanceof Polygon) {
      structure = ((Polygon) g).getNumInteriorRing() + " holes" ;
    }
    else if (g instanceof GeometryCollection)
      structure = g.getNumGeometries() + " elements";

    return
    g.getGeometryType().toUpperCase() 
    +  " - " + structure
    + (structure.length() > 0 ? ", " : "")
    + g.getNumPoints() + " pts";
  }

  public static String metricsSummary(Geometry g)
  {
    String metrics = "Length = " + g.getLength() + "    Area = " + g.getArea();
    return metrics;
  }

}
