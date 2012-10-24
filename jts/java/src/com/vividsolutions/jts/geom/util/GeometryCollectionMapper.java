package com.vividsolutions.jts.geom.util;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.GeometryMapper.MapOp;

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
