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

import java.util.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.geom.util.PolygonExtracter;

public class ConversionFunctions 
{
  public static Geometry toPoints(Geometry g1, Geometry g2)
  {
    Geometry geoms = FunctionsUtil.buildGeometry(g1, g2);
    return FunctionsUtil.getFactoryOrDefault(g1, g2)
        .createMultiPoint(geoms.getCoordinates());
  }

  public static Geometry toLines(Geometry g1, Geometry g2)
  {
    Geometry geoms = FunctionsUtil.buildGeometry(g1, g2);
    return FunctionsUtil.getFactoryOrDefault(g1, g2)
        .buildGeometry(LinearComponentExtracter.getLines(geoms));
  }

  public static Geometry toMultiPolygon(Geometry g1, Geometry g2)
  {
    Geometry geoms = FunctionsUtil.buildGeometry(g1, g2);
    List polys = PolygonExtracter.getPolygons(g1);
    PolygonExtracter.getPolygons(g2, polys);
    return FunctionsUtil.getFactoryOrDefault(g1, g2)
        .createMultiPolygon( GeometryFactory.toPolygonArray(polys));
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
