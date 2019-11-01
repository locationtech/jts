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

import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionFunction;

/**
 * Unions a collection of geometries in an 
 * efficient way, using {@link OverlayNG}
 * to ensure robust computation.
 * 
 * @author Martin Davis
 *
 */
public class UnaryUnionNG {
  
  /**
   * Unions a collection of geometries,
   * using a given precision model.
   * 
   * @param geom the geometry to union
   * @param pm the precision model to use
   * @return the union of the geometries
   */
  public static Geometry union(Geometry geom, PrecisionModel pm) {
    UnionFunction unionSRFun = new UnionFunction() {

      public Geometry union(Geometry g0, Geometry g1) {
        return OverlayNG.overlay(g0, g1, UNION, pm);
      }
      
    };
    UnaryUnionOp op = new UnaryUnionOp(geom);
    op.setUnionFunction( unionSRFun );
    return op.union();
  }
  
  /**
   * Unions a collection of geometries,
   * using a precision model optimized to provide maximum
   * precision while ensuring robust computation.
   * 
   * @param geom the geometry to union
   * @return the union of the geometries
   */
  public static Geometry union(Geometry geom) {
    PrecisionModel pm = PrecisionUtil.robustPM(geom);
    return UnaryUnionNG.union(geom, pm);
  }
}
