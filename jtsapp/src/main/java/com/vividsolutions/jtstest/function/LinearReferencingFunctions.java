package com.vividsolutions.jtstest.function;


import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

public class LinearReferencingFunctions
{
  public static Geometry extractPoint(Geometry g, double index)
  {
    LengthIndexedLine ll = new LengthIndexedLine(g);
    Coordinate p = ll.extractPoint(index);
    return g.getFactory().createPoint(p);
  }
  public static Geometry extractLine(Geometry g, double start, double end)
  {
    LengthIndexedLine ll = new LengthIndexedLine(g);
    return ll.extractLine(start, end);
  }
  public static Geometry project(Geometry g, Geometry g2)
  {
    LengthIndexedLine ll = new LengthIndexedLine(g);
    double index = ll.project(g2.getCoordinate());
    Coordinate p = ll.extractPoint(index);
    return g.getFactory().createPoint(p);
  }
  public static double projectIndex(Geometry g, Geometry g2)
  {
    LengthIndexedLine ll = new LengthIndexedLine(g);
    return ll.project(g2.getCoordinate());
  }

}
