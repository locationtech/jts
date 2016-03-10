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

package org.locationtech.jtstest.testbuilder.geom;

import java.util.*;

import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.*;

public class GeometryCombiner 
{
  private GeometryFactory geomFactory;
  
  public GeometryCombiner(GeometryFactory geomFactory) {
    this.geomFactory = geomFactory;
  }

  public Geometry addPolygonRing(Geometry orig, Coordinate[] pts)
  {
    LinearRing ring = geomFactory.createLinearRing(pts);
    
    if (orig == null) {
      return geomFactory.createPolygon(ring, null);
    }
    if (! (orig instanceof Polygonal)) {
      return combine(orig, 
          geomFactory.createPolygon(ring, null));
    }
    // add the ring as either a hole or a shell
    Polygon polyContaining = findPolygonContaining(orig, pts[0]);
    if (polyContaining == null) {
      return combine(orig, geomFactory.createPolygon(ring, null));
    }
    
    // add ring as hole
    Polygon polyWithHole = addHole(polyContaining, ring);
    return replace(orig, polyContaining, polyWithHole);
  }
  
  public Geometry addLineString(Geometry orig, Coordinate[] pts)
  {
    LineString line = geomFactory.createLineString(pts);
    return combine(orig, line);
  }
  
  public Geometry addPoint(Geometry orig, Coordinate pt)
  {
    Point point = geomFactory.createPoint(pt);
    return combine(orig, point);
  }
  
  private static Polygon findPolygonContaining(Geometry geom, Coordinate pt)
  {
    PointLocator locator = new PointLocator();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Polygon poly = (Polygon) geom.getGeometryN(i);
      int loc = locator.locate(pt, poly);
      if (loc == Location.INTERIOR)
        return poly;
    }
    return null;
  }
  
  public Polygon addHole(Polygon poly, LinearRing hole)
  {
    int nOrigHoles = poly.getNumInteriorRing();
    LinearRing[] newHoles = new LinearRing[nOrigHoles + 1];
    for (int i = 0; i < nOrigHoles; i++) {
      newHoles[i] = (LinearRing) poly.getInteriorRingN(i);
    }
    newHoles[nOrigHoles] = hole;
    return geomFactory.createPolygon((LinearRing) poly.getExteriorRing(), newHoles);
  }
  
  public Geometry combine(Geometry orig, Geometry geom)
  {
    List origList = extractElements(orig, true);
    List geomList = extractElements(geom, true);
    origList.addAll(geomList);
    
    if (origList.size() == 0) {
      // return a clone of the orig geometry
      return (Geometry) orig.clone();
    }
    // return the "simplest possible" geometry
    return geomFactory.buildGeometry(origList);
  }
  
  public static List extractElements(Geometry geom, boolean skipEmpty)
  {
    List elem = new ArrayList();
    if (geom == null)
      return elem;
    
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Geometry elemGeom = geom.getGeometryN(i);
      if (skipEmpty && elemGeom.isEmpty())
        continue;
      elem.add(elemGeom);
    }
    return elem;
  }
  
  public static Geometry replace(Geometry parent, Geometry original, Geometry replacement)
  {
    List elem = extractElements(parent, false);
    Collections.replaceAll(elem, original, replacement);
    return parent.getFactory().buildGeometry(elem);
  }
}
