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

package org.locationtech.jts.geom.util;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.GeometryMapper.MapOp;

/**
 * Maps the members of a {@link GeometryCollection}
 * into another <tt>GeometryCollection</tt> via a defined
 * mapping function.
 * 
 * @author Martin Davis
 *
 */
public class GeometryCollectionMapper <T,G extends Geometry<T>>
{
  public static <T,G extends Geometry<T>>GeometryCollection<T,G> map(GeometryCollection<T,G> gc, MapOp op)
  {
    GeometryCollectionMapper<T,G> mapper = new GeometryCollectionMapper<>(op);
    return mapper.map(gc);
  }
  
  private MapOp mapOp = null;
  
  public GeometryCollectionMapper(MapOp mapOp) {
    this.mapOp = mapOp;
  }

  public GeometryCollection<T,G> map(GeometryCollection<T,G> gc)
  {
    List<Geometry<T>> mapped = new ArrayList<>();
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Geometry<T> g = mapOp.map(gc.getGeometryN(i));
      if (!g.isEmpty())
        mapped.add(g);
    }
    return gc.getFactory().createGeometryCollection(
        GeometryFactory.toGeometryArray(mapped));
  }
}
