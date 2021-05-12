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
      int nHoles = ((Polygon) g).getNumInteriorRing();
      if (nHoles > 0) structure = nHoles + " holes, " ;
    }
    String size = "";
    if (g instanceof GeometryCollection)
      size = " [ " + g.getNumGeometries() + " ]";

    return
    g.getGeometryType().toUpperCase() 
    +  size + " - " + structure
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

  /**
   * Gets the envelope including all holes which might lie outside a polygon.
   * 
   * @param geom
   * @return
   */
  public static Envelope totalEnvelope(Geometry geom) {
    Envelope env = geom.getEnvelopeInternal();
    geom.apply(new GeometryComponentFilter() {
  
      @Override
      public void filter(Geometry comp) {
        env.expandToInclude(comp.getEnvelopeInternal());
      }
      
    });
    return env;
  }

}
