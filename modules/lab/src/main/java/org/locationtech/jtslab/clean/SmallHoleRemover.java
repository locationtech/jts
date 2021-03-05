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
package org.locationtech.jtslab.clean;

import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.Geometry;

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

    public boolean value(Geometry geom) {
      double holeArea = Area.ofRing(geom.getCoordinates());
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
