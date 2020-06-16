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
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionStrategy;

public class OverlayNGPMFloatFunctions {
  
  public static Geometry difference(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, DIFFERENCE );
  }

  public static Geometry intersection(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, INTERSECTION );
  }

  public static Geometry union(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, UNION );
  }

  public static Geometry unaryUnion(Geometry a) {
    UnionStrategy unionSRFun = new UnionStrategy() {

      public Geometry union(Geometry g0, Geometry g1) {
         return OverlayNG.overlay(g0, g1, UNION );
      }

      @Override
      public boolean isFloatingPrecision() {
        return true;
      }
      
    };
    UnaryUnionOp op = new UnaryUnionOp(a);
    op.setUnionFunction(unionSRFun);
    return op.union();
  }
  
  public static Geometry intersectionFloatPMNoOpt(Geometry a, Geometry b) {
    OverlayNG ovr = new OverlayNG(a, b, INTERSECTION);
    ovr.setOptimized(false);
    return ovr.getResult();
  }
  
  public static Geometry intersectionNoValid(Geometry a, Geometry b) {
    Noder noder = createFloatingPrecisionNoder(false);
    return OverlayNG.overlay(a, b, INTERSECTION, new PrecisionModel(), noder);
  }
  
  public static Geometry intersectionIsValid(Geometry a, Geometry b) {
    Noder noder = createFloatingPrecisionNoder(false);
    Geometry geom = OverlayNG.overlay(a, b, INTERSECTION, new PrecisionModel(), noder);
    if (geom.isValid()) return geom;
    return null;
  }
  
  private static Noder createFloatingPrecisionNoder(boolean doValidation) {
    MCIndexNoder mcNoder = new MCIndexNoder();
    LineIntersector li = new RobustLineIntersector();
    mcNoder.setSegmentIntersector(new IntersectionAdder(li));
    
    Noder noder = mcNoder;
    if (doValidation) {
      noder = new ValidatingNoder( mcNoder);
    }
    return noder;
  }
}
