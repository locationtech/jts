/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.algorithm.PointLocator;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;

public class InputGeometry {
  
  //private static final PointLocator ptLocator = new PointLocator();

  private Geometry[] geom = new Geometry[2];
  private PointOnGeometryLocator ptLocatorA;
  private PointOnGeometryLocator ptLocatorB;
  
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
  
  public boolean isLine(int geomIndex) {
    return geom[geomIndex].getDimension() == 1;
  }
  
  public int locatePointInArea(int geomIndex, Coordinate pt) {
    // Assert: only called if dimension(geomIndex) = 2
    
    //return ptLocator.locate(pt, geom[geomIndex]);
    
    //*
    // this check is important, because IndexedPointInAreaLocator can't handle empty polygons
    if (getGeometry(geomIndex).isEmpty()) 
      return Location.EXTERIOR;
    
    PointOnGeometryLocator ptLocator = getLocator(geomIndex);
    return ptLocator.locate(pt);
    //*/
  }

  private PointOnGeometryLocator getLocator(int geomIndex) {
    if (geomIndex == 0) {
      if (ptLocatorA == null)
        ptLocatorA = new IndexedPointInAreaLocator(getGeometry(geomIndex));
      return ptLocatorA;
    }
    else {
      if (ptLocatorB == null)
        ptLocatorB = new IndexedPointInAreaLocator(getGeometry(geomIndex));
      return ptLocatorB;
    } 
  }

}
