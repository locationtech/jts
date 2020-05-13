package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.algorithm.PointLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class IndexedPointOnLineLocator implements PointOnGeometryLocator {

  private Geometry inputGeom;

  public IndexedPointOnLineLocator(Geometry geomLinear) {
    this.inputGeom = geomLinear;
  }

  @Override
  public int locate(Coordinate p) {
    // TODO: optimize this with a segment index
    PointLocator locator = new PointLocator();
    return locator.locate(p, inputGeom);
  }

}
