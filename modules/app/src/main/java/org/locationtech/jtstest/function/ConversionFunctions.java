/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.geom.util.PolygonExtracter;

public class ConversionFunctions 
{
  public static Geometry pointsToLine(Geometry g)
  {
    Coordinate[] pts = g.getCoordinates();
    LineString line = g.getFactory().createLineString(pts);
    return line;
  }
  
  public static Geometry lineToPolygon(Geometry g)
  {
    if (g instanceof Polygonal) return g;
    // TODO: ensure ring is valid
    CoordinateList ringList = new CoordinateList();
    Coordinate[] pts = g.getCoordinates();
    for (Coordinate pt : pts) {
      ringList.add(pt, true);
    }
    ringList.closeRing();
    LinearRing ring = g.getFactory().createLinearRing(ringList.toCoordinateArray());
    return g.getFactory().createPolygon(ring, null);
  }
  
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

  public static Geometry toGeometryCollection(Geometry g1, Geometry g2)
  {
    List atomicGeoms = new ArrayList();
    if (g1 != null) addComponents(g1, atomicGeoms);
    if (g2 != null) addComponents(g2, atomicGeoms);
    return FunctionsUtil.getFactoryOrDefault(g1, g2).createGeometryCollection(
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
