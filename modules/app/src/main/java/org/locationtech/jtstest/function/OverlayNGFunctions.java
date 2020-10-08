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
package org.locationtech.jtstest.function;

import static org.locationtech.jts.operation.overlayng.OverlayNG.DIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;
import static org.locationtech.jts.operation.overlayng.OverlayNG.SYMDIFFERENCE;
import static org.locationtech.jts.operation.overlayng.OverlayNG.UNION;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlayng.CoverageUnion;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.locationtech.jts.operation.union.UnionStrategy;
import org.locationtech.jtstest.geomfunction.Metadata;

public class OverlayNGFunctions {
  
  public static Geometry difference(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, DIFFERENCE );
  }

  public static Geometry differenceBA(Geometry a, Geometry b) {
      return OverlayNG.overlay(b, a, DIFFERENCE );
  }

  public static Geometry intersection(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, INTERSECTION );
  }

  public static Geometry symDifference(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, SYMDIFFERENCE );
  }

  public static Geometry union(Geometry a,
      @Metadata(isRequired=false)
      Geometry b) {
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
  
  @Metadata(description="Fast Union of a fully-noded coverage (polygons or lines)")
  public static Geometry unionCoverage(Geometry geom) {
    Geometry cov = OverlayNGSRFunctions.extractHomo(geom);
    return CoverageUnion.union(cov);
  }
}
