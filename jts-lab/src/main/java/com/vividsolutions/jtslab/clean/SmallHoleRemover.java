/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtslab.clean;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Removes holes which are smaller than a given area.
 * 
 * @author Martin Davis
 *
 */
public class SmallHoleRemover {

  private static class IsSmall implements HoleRemover.Predicate {
    private double area;

    public IsSmall(double area) {
      this.area = area;
    }

    @Override
    public boolean value(Geometry geom) {
      double holeArea = Math.abs(CGAlgorithms.signedArea(geom.getCoordinates()));
      return holeArea <= area;
    }
    
  }
  
  /**
   * Removes small holes from the polygons in a geometry.
   * 
   * @param geom the geometry to clean
   * @return the geometry with invalid holes removed
   */
  public static Geometry clean(Geometry geom, double areaTolerance) {
    HoleRemover remover = new HoleRemover(geom, new IsSmall(areaTolerance));
    return remover.getResult();
  }
  
}
