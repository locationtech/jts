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
package org.locationtech.jtstest.function;

import static org.locationtech.jts.operation.overlayng.OverlayNG.DIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.ValidatingNoder;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGSnapIfNeeded;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionFunction;

public class OverlayNGSnapIfNeededFunctions {
  
  public static Geometry difference(Geometry a, Geometry b) {
    return OverlayNGSnapIfNeeded.overlay(a, b, DIFFERENCE );
  }

  public static Geometry intersection(Geometry a, Geometry b) {
    return OverlayNGSnapIfNeeded.overlay(a, b, INTERSECTION );
  }

  public static Geometry union(Geometry a, Geometry b) {
    return OverlayNGSnapIfNeeded.overlay(a, b, UNION );
  }

  public static Geometry unaryUnion(Geometry a) {
    UnionFunction unionSRFun = new UnionFunction() {

      public Geometry union(Geometry g0, Geometry g1) {
         return OverlayNGSnapIfNeeded.overlay(g0, g1, UNION );
      }
      
    };
    UnaryUnionOp op = new UnaryUnionOp(a);
    op.setUnionFunction(unionSRFun);
    return op.union();
  }
  
}
