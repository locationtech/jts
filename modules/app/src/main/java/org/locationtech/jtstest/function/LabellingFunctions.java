package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jtstest.testbuilder.geom.ConstrainedInteriorPoint;

public class LabellingFunctions {

  public static Geometry labelPoint(Geometry g) {
    Coordinate pt = ConstrainedInteriorPoint.getCoordinate((Polygon) g);
    return g.getFactory().createPoint(pt);
  }
  
  public static Geometry labelPointConstrained(Geometry g, Geometry con) {
    Envelope envCon = con.getEnvelopeInternal();
    Coordinate pt = ConstrainedInteriorPoint.getCoordinate((Polygon) g, envCon);
    return g.getFactory().createPoint(pt);
  }
}
