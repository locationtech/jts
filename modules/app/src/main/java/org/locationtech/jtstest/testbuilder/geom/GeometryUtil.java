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

package org.locationtech.jtstest.testbuilder.geom;

import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.*;

public class GeometryUtil {

  public static String structureSummary(Geometry g)
  {
    String structure = "";
    if (g instanceof Polygon) {
      structure = ((Polygon) g).getNumInteriorRing() + " holes" ;
    }
    else if (g instanceof GeometryCollection)
      structure = g.getNumGeometries() + " elements";

    return
    g.getGeometryType().toUpperCase() 
    +  " - " + structure
    + (structure.length() > 0 ? ", " : "")
    + g.getNumPoints() + " pts";
  }

  public static String metricsSummary(Geometry g)
  {
    String metrics = "";
    if ( hasLength(g) ) metrics += "Len: " + g.getLength(); 
    if ( hasArea(g) ) metrics += "  Area: " + area(g);
    return metrics;
  }

  public static boolean hasArea(Geometry geom) {
    if (geom.getDimension() >= 2) return true;
    if (geom instanceof LinearRing) return true;
    return false;
  }

  public static boolean hasLength(Geometry geom) {
    if (geom.getDimension() >= 1) return true;
    return false;
  }

  public static double area(Geometry geom) {
    double area = 0;
    if (geom.getDimension() >= 2) {
      area = geom.getArea();
    }
    else if (geom instanceof LinearRing) {
      area = Area.ofRing(geom.getCoordinates());
    }
    return area;
  }

}
