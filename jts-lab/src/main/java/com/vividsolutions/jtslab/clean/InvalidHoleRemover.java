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
