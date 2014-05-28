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
 * Removes holes which are invalid due to not being wholly covered by the parent shell.
 * <p>
 * Notes:
 * <ul>
 * <li>Does not remove holes which are invalid due to touching other rings at more than one point
 * </ul>
 * 
 * @author Martin Davis
 *
 */
public class InvalidHoleRemover {

  /**
   * Removes invalid holes from the polygons in a geometry.
   * 
   * @param geom the geometry to clean
   * @return the geometry with invalid holes removed
   */
  public static Geometry clean(Geometry geom) {
    InvalidHoleRemover pihr = new InvalidHoleRemover(geom);
    return pihr.getResult();
  }
  
  private Geometry geom;

  /**
   * Creates a new invalid hole remover instance.
   * 
   * @param geom the geometry to process
   */
  public InvalidHoleRemover(Geometry geom) {
    this.geom = geom;
  }
  
  /**
   * Gets the cleaned geometry.
   * 
   * @return the geometry with invalid holes removed.
   */
  public Geometry getResult()
  {
    return GeometryMapper.map(geom, new InvalidHoleRemoverMapOp());
  }
  
  private static class InvalidHoleRemoverMapOp implements MapOp {

    public Geometry map(Geometry geom) {
      if (geom instanceof Polygon)
        return  PolygonInvalidHoleRemover.clean((Polygon) geom);
      return geom;
    }
    
  }
  
  private static class PolygonInvalidHoleRemover {
    
    public static Polygon clean(Polygon poly) {
      PolygonInvalidHoleRemover pihr = new PolygonInvalidHoleRemover(poly);
      return pihr.getResult();
    }
    
    private Polygon poly;

    public PolygonInvalidHoleRemover(Polygon poly) {
      this.poly = poly;
    }
    
    public Polygon getResult()
    {
      GeometryFactory gf = poly.getFactory();
      Polygon shell = gf.createPolygon((LinearRing) poly.getExteriorRing());
      PreparedGeometry shellPrep = PreparedGeometryFactory.prepare(shell);
      
      List holes = new ArrayList();
      for (int i = 0; i < poly.getNumInteriorRing(); i++) {
        LinearRing hole = (LinearRing) poly.getInteriorRingN(i);
        if (shellPrep.covers(hole)) {
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
