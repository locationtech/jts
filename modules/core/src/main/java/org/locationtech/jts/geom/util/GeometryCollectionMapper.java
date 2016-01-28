/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jts.geom.util;

import java.util.*;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryMapper.MapOp;

/**
 * Maps the members of a {@link GeometryCollection}
 * into another <tt>GeometryCollection</tt> via a defined
 * mapping function.
 * 
 * @author Martin Davis
 *
 */
public class GeometryCollectionMapper 
{
  public static GeometryCollection map(GeometryCollection gc, MapOp op)
  {
    GeometryCollectionMapper mapper = new GeometryCollectionMapper(op);
    return mapper.map(gc);
  }
  
  private MapOp mapOp = null;
  
  public GeometryCollectionMapper(MapOp mapOp) {
    this.mapOp = mapOp;
  }

  public GeometryCollection map(GeometryCollection gc)
  {
    List mapped = new ArrayList();
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Geometry g = mapOp.map(gc.getGeometryN(i));
      if (!g.isEmpty())
        mapped.add(g);
    }
    return gc.getFactory().createGeometryCollection(
        GeometryFactory.toGeometryArray(mapped));
  }
}
