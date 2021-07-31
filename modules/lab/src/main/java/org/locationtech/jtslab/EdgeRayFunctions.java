/*
 * Copyright (c) 2018 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtslab;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtslab.edgeray.EdgeRayArea;
import org.locationtech.jtslab.edgeray.EdgeRayIntersectionArea;

public class EdgeRayFunctions {
  public static double area(Geometry g) {
    return EdgeRayArea.area(g);
  }
  
  public static double intersectionArea(Geometry geom0, Geometry geom1) {
    EdgeRayIntersectionArea area = new EdgeRayIntersectionArea(geom0, geom1);
    return area.getArea();
  }
  
  public static double checkIntersectionArea(Geometry geom0, Geometry geom1) {
    double intArea = intersectionArea(geom0, geom1);
    
    double intAreaStd = geom0.intersection(geom1).getArea();
    
    double diff = Math.abs(intArea - intAreaStd)/Math.max(intArea, intAreaStd);
    
    return diff;
  }
}
