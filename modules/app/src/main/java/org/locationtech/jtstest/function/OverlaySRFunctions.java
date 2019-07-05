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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlaysr.OverlaySR;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionFunction;

public class OverlaySRFunctions {
  public static Geometry intersection(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlaySR.overlay(a, b, pm, OverlayOp.INTERSECTION);
  }

  public static Geometry intersectionLines(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    OverlaySR ovr = new OverlaySR(a, b, pm, OverlayOp.INTERSECTION);
    ovr.setOutputResultEdges(true);
    return ovr.getResultGeometry();
  }

  public static Geometry intersectionAllLines(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    OverlaySR ovr = new OverlaySR(a, b, pm, OverlayOp.INTERSECTION);
    ovr.setOutputEdges(true);
    return ovr.getResultGeometry();
  }

  public static Geometry union(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlaySR.overlay(a, b, pm, OverlayOp.UNION);
  }
  
  public static Geometry unionLines(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    OverlaySR ovr = new OverlaySR(a, b, pm, OverlayOp.UNION);
    ovr.setOutputResultEdges(true);
    return ovr.getResultGeometry();
  }

  public static Geometry difference(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlaySR.overlay(a, b, pm, OverlayOp.DIFFERENCE);
  }

  public static Geometry differenceBA(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlaySR.overlay(b, a, pm, OverlayOp.DIFFERENCE);
  }

  public static Geometry symDifference(Geometry a, Geometry b, double scaleFactor) {
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    return OverlaySR.overlay(a, b, pm, OverlayOp.SYMDIFFERENCE);
  }
  
  public static Geometry unaryUnion(Geometry a, double scaleFactor) {
    final PrecisionModel pm = new PrecisionModel(scaleFactor);
    UnionFunction unionSRFun = new UnionFunction() {

      public Geometry union(Geometry g0, Geometry g1) {
        return OverlaySR.overlay(g0, g1, pm, OverlayOp.UNION);
      }
      
    };
    UnaryUnionOp op = new UnaryUnionOp(a);
    op.setUnionFunction(unionSRFun);
    return op.union();
  }

}
