package com.vividsolutions.jtstest.function;

import java.util.*;
import com.vividsolutions.jts.geom.*;

public class ConversionFunctions 
{
  public static Geometry toGeometryCollection(Geometry g)
  {
    if (! (g instanceof GeometryCollection)) {
      return g.getFactory().createGeometryCollection(new Geometry[] { g} );
    }
    
    List atomicGeoms = new ArrayList();
    GeometryCollectionIterator it = new GeometryCollectionIterator(g);
    while (it.hasNext()) {
      Geometry g2 = (Geometry) it.next();
      if (! (g2 instanceof GeometryCollection))
        atomicGeoms.add(g2);
    }
    return g.getFactory().createGeometryCollection(
        GeometryFactory.toGeometryArray(atomicGeoms));
  }

}
