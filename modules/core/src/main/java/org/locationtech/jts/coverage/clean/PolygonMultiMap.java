/*
 * Copyright (c) 2025 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage.clean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

class PolygonMultiMap {

  private Map<Integer, List<Polygon>> indexPolygonsMap = new HashMap<Integer, List<Polygon>>();
  
  public boolean containsKey(int index) {
    return indexPolygonsMap.containsKey(index);
  }

  public void add(int index, Polygon polygon) {
    List<Polygon> polyList = indexPolygonsMap.get(index);
    if (polyList != null) {
      polyList.add(polygon);
      return;
    }
    //-- create new entry
    polyList = new ArrayList<Polygon>();
    polyList.add(polygon);
    indexPolygonsMap.put(index, polyList);
  }

  public Set<Integer> keys() {
    return indexPolygonsMap.keySet();
  }

  public Geometry getGeometry(int index, GeometryFactory geomFactory) {
    List<Polygon> polyList = indexPolygonsMap.get(index);
    Polygon[] polygons = GeometryFactory.toPolygonArray(polyList);
    
    //TODO: merge polygons to absorb slivers?
    
    if (polygons.length == 1) {
      return polygons[0];
    }
    return geomFactory.createMultiPolygon(polygons);
  }

}
