package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.algorithm.PointLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class InputGeometry {
  
  private static final PointLocator ptLocator = new PointLocator();

  public Geometry[] geom;
  
  public InputGeometry(Geometry geomA, Geometry geomB) {
    geom = new Geometry[] { geomA, geomB };
  }
  
  public int getDimension(int index) {
    return geom[index].getDimension();
  }

  public Geometry getGeometry(int geomIndex) {
    return geom[geomIndex];
  }

  public boolean isArea(int geomIndex) {
    return geom[geomIndex].getDimension() == 2;
  }
  
  public int locatePoint(int geomIndex, Coordinate pt) {
    return ptLocator.locate(pt, geom[geomIndex]);
  }
}
