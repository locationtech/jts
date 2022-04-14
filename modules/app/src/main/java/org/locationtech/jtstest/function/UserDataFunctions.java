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
package org.locationtech.jtstest.function;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

public class UserDataFunctions {

  public static Geometry area(Geometry g) {
    Geometry result = g.copy();
    List<Geometry> geoms = FunctionsUtil.elements(result);
    // annotate geometries with area
    for (Geometry geom : geoms) {
      geom.setUserData(geom.getArea());
    }
    return result;
  }
 
  public static Geometry length(Geometry g) {
    Geometry result = g.copy();
    List<Geometry> geoms = FunctionsUtil.elements(result);
    // annotate geometries with area
    for (Geometry geom : geoms) {
      geom.setUserData(geom.getLength());
    }
    return result;
  }
 
  public static Geometry numPoints(Geometry g) {
    Geometry result = g.copy();
    List<Geometry> geoms = FunctionsUtil.elements(result);
    // annotate geometries with area
    for (Geometry geom : geoms) {
      geom.setUserData(geom.getNumPoints());
    }
    return result;
  }
 
  public static Geometry index(Geometry g) {
    Geometry result = g.copy();
    List<Geometry> geoms = FunctionsUtil.elements(result);
    // annotate geometries with area
    for (int i = 0; i < geoms.size(); i++) {
      geoms.get(i).setUserData(i);
    }
    return result;
  }
 
 

}
