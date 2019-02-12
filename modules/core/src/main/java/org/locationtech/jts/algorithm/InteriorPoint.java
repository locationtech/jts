package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Computes an interior point of a <code>{@link Geometry}</code>.
 * An interior point is guaranteed to lie in the interior of the Geometry,
 * if it possible to calculate such a point exactly. 
 * Otherwise, the point may lie on the boundary of the geometry.
 * <p>
 * The interior point of an empty geometry is <code>POINT EMPTY</code>.
 */
public class InteriorPoint {
  
  public static Point getInteriorPoint(Geometry geom) {
    GeometryFactory factory = geom.getFactory();
    
    if (geom.isEmpty()) 
      return createPointEmpty(factory);
    
    Coordinate interiorPt = null;
    int dim = geom.getDimension();
    if (dim == 0) {
      interiorPt = InteriorPointPoint.getInteriorPoint(geom);
    }
    else if (dim == 1) {
      interiorPt = InteriorPointLine.getInteriorPoint(geom);
    }
    else {
      interiorPt = InteriorPointArea.getInteriorPoint(geom);
    }
    return createPointPrecise(factory, interiorPt);
  }

  private static Point createPointEmpty(GeometryFactory factory) {
    return factory.createPoint();
  }

  private static Point createPointPrecise(GeometryFactory factory, Coordinate coord) {
    factory.getPrecisionModel().makePrecise(coord);
    return factory.createPoint(coord);
  }
}
