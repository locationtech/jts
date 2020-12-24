/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionStrategy;

/**
 * Unions a collection of geometries in an
 * efficient way, using {@link OverlayNG}
 * to ensure robust computation.
 * <p>
 * This class is most useful for performing UnaryUnion using 
 * a fixed-precision model. 
 * For unary union using floating precision,  
 * {@link OverlayNGRobust#union(Geometry)} should be used.
 * 
 * @author Martin Davis
 * @see OverlayNGRobust
 *
 */
public class UnaryUnionNG {
  
  /**
   * Unions a collection of geometries
   * using a given precision model.
   * 
   * @param geom the geometry to union
   * @param pm the precision model to use
   * @return the union of the geometries
   */
  public static Geometry union(Geometry geom, PrecisionModel pm) {
    UnionStrategy unionSRFun = new UnionStrategy() {

      public Geometry union(Geometry g0, Geometry g1) {
        return OverlayNG.overlay(g0, g1, UNION, pm);
      }

      @Override
      public boolean isFloatingPrecision() {
         return OverlayUtil.isFloating(pm);
      }
      
    };
    UnaryUnionOp op = new UnaryUnionOp(geom);
    op.setUnionFunction( unionSRFun );
    return op.union();
  }
  
  private UnaryUnionNG() {
    // no instantiation for now
  }
}
