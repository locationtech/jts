package com.vividsolutions.jtstest.function;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;

public class ConversionFunctions 
{
  public static Geometry toPoints(Geometry g)
  {
    return g.getFactory().createMultiPoint(g.getCoordinates());
  }

  public static Geometry toLines(Geometry g)
  {
    return g.getFactory().buildGeometry(LinearComponentExtracter.getLines(g));
  }

  public static Geometry toGeometryCollection(Geometry g, Geometry g2)
  {
    
    List atomicGeoms = new ArrayList();
    if (g != null) addComponents(g, atomicGeoms);
    if (g2 != null) addComponents(g2, atomicGeoms);
    return g.getFactory().createGeometryCollection(
        GeometryFactory.toGeometryArray(atomicGeoms));
  }

  private static void addComponents(Geometry g, List atomicGeoms)
  {
    if (! (g instanceof GeometryCollection)) {
      atomicGeoms.add(g);
      return;
    }

    GeometryCollectionIterator it = new GeometryCollectionIterator(g);
    while (it.hasNext()) {
      Geometry gi = (Geometry) it.next();
      if (! (gi instanceof GeometryCollection))
        atomicGeoms.add(gi);
    }
  }

}
