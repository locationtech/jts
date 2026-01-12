/*
 * Copyright (c) 2026 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.triangulate;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

public class VoronoiChecker {
  
  public static boolean isValid(Geometry voronoiDiagram) {
    boolean isValid = voronoiDiagram.isValid();
    if (! isValid) return false;
    
    boolean isConvex = isConvex(voronoiDiagram);
    if (! isConvex) return false;
    
    boolean isNonoverlapping = isNonOverlapping(voronoiDiagram);
    if (! isNonoverlapping) return false;
    
    return true;
  }
  
  private static boolean isConvex(Geometry voronoiDiagram) {
    Geometry union = voronoiDiagram.union();
    if (! (union instanceof Polygon)) {
      return false;
    }
    return isConvex((Polygon) union);
  }
  
  private static boolean isConvex(Polygon poly) {
    Coordinate[] pts = poly.getCoordinates();
    for (int i = 0; i < pts.length - 1; i++) {
      int iprev = i - 1;
      if (iprev < 0) iprev = pts.length - 2;
      int inext = i + 1;
      //-- orientation must be CLOCKWISE or COLLINEAR
      boolean isConvex = Orientation.COUNTERCLOCKWISE != Orientation.index(pts[iprev], pts[i], pts[inext]);
      if (! isConvex)
        return false;
    }
    return true;
  }
  
  private static final String INTERIOR_INTERSECTS = "T********";
  
  private static boolean isNonOverlapping(Geometry result) {
    int n = result.getNumGeometries();
    for (int i1 = 0; i1 < n; i1++) {
      Polygon poly1 = (Polygon) result.getGeometryN(i1);
      for (int i2 = i1 + 1; i2 < n; i2++) {
        Polygon poly2 = (Polygon) result.getGeometryN(i2);
        boolean isOverlapping = poly1.relate(poly2, INTERIOR_INTERSECTS);
        if (isOverlapping)
          return false;
      }
    }
    return true;
  }
}
