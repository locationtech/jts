package org.locationtech.jtslab.edgeray;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

public class EdgeRayArea {
  public static double area(Geometry geom) {
    EdgeRayArea area = new EdgeRayArea(geom);
    return area.getArea();
  }
  
  private Geometry geom;

  public EdgeRayArea(Geometry geom) {
    this.geom = geom;
  }
  
  public double getArea() {
    Polygon poly = (Polygon) geom;
    CoordinateSequence seq = poly.getExteriorRing().getCoordinateSequence();
    boolean isCW = ! Orientation.isCCW(seq);
    // TODO: for now assume poly is CW
    
    // scan every segment
    double area = 0;
    for (int i = 1; i < seq.size(); i++) {
      int i0 = i - 1;
      int i1 = i;
      /*
      area += EdgeRay.areaTermBoth(seq.getX(i0), seq.getY(i0),
          seq.getX(i1), seq.getY(i1));
          */
      area += EdgeRay.areaTerm(seq.getX(i0), seq.getY(i0),
          seq.getX(i1), seq.getY(i1), isCW);
      area += EdgeRay.areaTerm(seq.getX(i1), seq.getY(i1),
          seq.getX(i0), seq.getY(i0), ! isCW);
    }
    return area;
  }
}
