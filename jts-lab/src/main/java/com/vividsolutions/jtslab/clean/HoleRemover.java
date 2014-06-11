/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtslab.clean;

import java.util.List;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.geom.util.GeometryMapper;
import com.vividsolutions.jts.geom.util.GeometryMapper.MapOp;

/**
 * Removes holes which match a given predicate.
 * 
 * @author Martin Davis
 *
 */
public class HoleRemover {
  
  public interface Predicate {
    boolean value(Geometry geom);
  }

  private Geometry geom;
  private Predicate isRemoved;

  /**
   * Creates a new hole remover instance.
   * 
   * @param geom the geometry to process
   */
  public HoleRemover(Geometry geom, Predicate isRemoved) {
    this.geom = geom;
    this.isRemoved = isRemoved;
  }
  
  /**
   * Gets the cleaned geometry.
   * 
   * @return the geometry with matched holes removed.
   */
  public Geometry getResult()
  {
    return GeometryMapper.map(geom, new HoleRemoverMapOp());
  }
  
  private class HoleRemoverMapOp implements MapOp {
    public Geometry map(Geometry geom) {
      if (geom instanceof Polygon)
        return  PolygonHoleRemover.clean((Polygon) geom, isRemoved);
      return geom;
    }
  }
  
  private static class PolygonHoleRemover {
    
    public static Polygon clean(Polygon poly, Predicate isRemoved) {
      PolygonHoleRemover pihr = new PolygonHoleRemover(poly, isRemoved);
      return pihr.getResult();
    }
    
    private Polygon poly;
    private Predicate isRemoved;

    public PolygonHoleRemover(Polygon poly, Predicate isRemoved) {
      this.poly = poly;
      this.isRemoved = isRemoved;
    }
    
    public Polygon getResult()
    {
      GeometryFactory gf = poly.getFactory();
      Polygon shell = gf.createPolygon((LinearRing) poly.getExteriorRing());
      
      List holes = new ArrayList();
      for (int i = 0; i < poly.getNumInteriorRing(); i++) {
        LinearRing hole = (LinearRing) poly.getInteriorRingN(i);
        if (! isRemoved.value(hole)) {
          holes.add(hole);
        }
      }
      // all holes valid, so return original
      if (holes.size() == poly.getNumInteriorRing())
        return poly;
      
      // return new polygon with covered holes only
      Polygon result = gf.createPolygon((LinearRing) poly.getExteriorRing(), 
          GeometryFactory.toLinearRingArray(holes));
      return result;
    }

  }
}
