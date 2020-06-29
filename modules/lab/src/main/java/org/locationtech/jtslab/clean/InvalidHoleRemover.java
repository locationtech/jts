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
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.GeometryMapper;
import org.locationtech.jts.geom.util.GeometryMapper.MapOp;

/**
 * Removes holes which are invalid due to not being wholly covered by the parent shell.
 * <p>
 * Notes:
 * <ul>
 * <li>Does not remove holes which are invalid due to touching other rings at more than one point
 * <li>Does not remove holes which are nested inside another hole
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
      Polygon shell = gf.createPolygon(poly.getExteriorRing());
      PreparedGeometry shellPrep = PreparedGeometryFactory.prepare(shell);
      
      List holes = new ArrayList();
      for (int i = 0; i < poly.getNumInteriorRing(); i++) {
        LinearRing hole = poly.getInteriorRingN(i);
        if (shellPrep.covers(hole)) {
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
