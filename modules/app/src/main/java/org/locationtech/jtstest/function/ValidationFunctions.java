/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;


public class ValidationFunctions
{
  /**
   * Validates all geometries in a collection independently.
   * Errors are returned as points at the invalid location
   * 
   * @param g
   * @return the invalid locations, if any
   */
  public static Geometry invalidLocations(Geometry g)
  {
    List invalidLoc = new ArrayList();
    for (int i = 0; i < g.getNumGeometries(); i++) {
      Geometry geom = g.getGeometryN(i);
      IsValidOp ivop = new IsValidOp(geom);
      TopologyValidationError err = ivop.getValidationError();
      if (err != null) {
        invalidLoc.add(g.getFactory().createPoint(err.getCoordinate()));
      }
    }
    return g.getFactory().buildGeometry(invalidLoc);
  }
  
  public static Geometry invalidGeoms(Geometry g)
  {
    List invalidGeoms = new ArrayList();
    for (int i = 0; i < g.getNumGeometries(); i++) {
      Geometry geom = g.getGeometryN(i);
      IsValidOp ivop = new IsValidOp(geom);
      TopologyValidationError err = ivop.getValidationError();
      if (err != null) {
        invalidGeoms.add(geom);
      }
    }
    return g.getFactory().buildGeometry(invalidGeoms);
  }
  
  public static boolean isValidAllowSelfTouchingRingFormingHole(Geometry g) {
    IsValidOp validOp = new IsValidOp(g);
    validOp.setSelfTouchingRingFormingHoleValid(true);
    return validOp.isValid();     
  }

}
