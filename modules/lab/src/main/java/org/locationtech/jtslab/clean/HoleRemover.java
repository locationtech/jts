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
package org.locationtech.jtslab.clean;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryMapper;
import org.locationtech.jts.geom.util.GeometryMapper.MapOp;

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
      Polygon shell = gf.createPolygon(poly.getExteriorRing());
      
      List holes = new ArrayList();
      for (int i = 0; i < poly.getNumInteriorRing(); i++) {
        LinearRing hole = poly.getInteriorRingN(i);
        if (! isRemoved.value(hole)) {
          holes.add(hole);
        }
      }
      // all holes valid, so return original
      if (holes.size() == poly.getNumInteriorRing())
        return poly;
      
      // return new polygon with covered holes only
      Polygon result = gf.createPolygon(poly.getExteriorRing(),
          GeometryFactory.toLinearRingArray(holes));
      return result;
    }

  }
}
