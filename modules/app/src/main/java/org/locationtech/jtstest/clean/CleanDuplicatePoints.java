
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
package org.locationtech.jtstest.clean;

import java.util.*;

import org.locationtech.jts.geom.*;


/**
 * @version 1.7
 */
public class CleanDuplicatePoints {

  public static Coordinate[] removeDuplicatePoints(Coordinate[] coord)
  {
    List uniqueCoords = new ArrayList();
    Coordinate lastPt = null;
    for (int i = 0; i < coord.length; i++) {
      if (lastPt == null || ! lastPt.equals(coord[i])) {
        lastPt = coord[i];
        uniqueCoords.add(new Coordinate(lastPt));
      }
    }
    return (Coordinate[]) uniqueCoords.toArray(new Coordinate[0]);
  }

  private GeometryFactory fact;

  public CleanDuplicatePoints() {
  }

  public Geometry clean(Geometry g)
  {
    fact = g.getFactory();
    if (g.isEmpty()) return g;
    if (g instanceof Point)                   return g;
    else if (g instanceof MultiPoint)         return g;
                        // LineString also handles LinearRings
    else if (g instanceof LinearRing)         return clean((LinearRing) g);
    else if (g instanceof LineString)         return clean((LineString) g);
    else if (g instanceof Polygon)            return clean((Polygon) g);
    else if (g instanceof MultiLineString)    return clean((MultiLineString) g);
    else if (g instanceof MultiPolygon)       return clean((MultiPolygon) g);
    else if (g instanceof GeometryCollection) return clean((GeometryCollection) g);
    else  throw new UnsupportedOperationException(g.getClass().getName());
  }

  private LinearRing clean(LinearRing g)
  {
    Coordinate[] coords = removeDuplicatePoints(g.getCoordinates());
    return fact.createLinearRing(coords);
  }

  private LineString clean(LineString g)
  {
    Coordinate[] coords = removeDuplicatePoints(g.getCoordinates());
    return fact.createLineString(coords);
  }

  private Polygon clean(Polygon poly)
  {
    Coordinate[] shellCoords = removeDuplicatePoints(poly.getExteriorRing().getCoordinates());
    LinearRing shell = fact.createLinearRing(shellCoords);
    List holes = new ArrayList();
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      Coordinate[] holeCoords = removeDuplicatePoints(poly.getInteriorRingN(i).getCoordinates());
      holes.add(fact.createLinearRing(holeCoords));
    }
    return fact.createPolygon(shell, GeometryFactory.toLinearRingArray(holes));
  }

  private MultiPolygon clean(MultiPolygon g)
  {
    List polys = new ArrayList();
    for (int i = 0; i < g.getNumGeometries(); i++) {
      Polygon poly = (Polygon) g.getGeometryN(i);
      polys.add(clean(poly));
    }
    return fact.createMultiPolygon(GeometryFactory.toPolygonArray(polys));
  }
  private MultiLineString clean(MultiLineString g)
  {
    List lines = new ArrayList();
    for (int i = 0; i < g.getNumGeometries(); i++) {
      LineString line = (LineString) g.getGeometryN(i);
      lines.add(clean(line));
    }
    return fact.createMultiLineString(GeometryFactory.toLineStringArray(lines));
  }
  private GeometryCollection clean(GeometryCollection g)
  {
    List geoms = new ArrayList();
    for (int i = 0; i < g.getNumGeometries(); i++) {
      Geometry geom = g.getGeometryN(i);
      geoms.add(clean(geom));
    }
    return fact.createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
  }

}
