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
import org.locationtech.jts.operation.overlayng.OverlayNG;

public class OverlayNGStrictFunctions {
  
  public static Geometry difference(Geometry a, Geometry b) {
    return overlay(a, b, DIFFERENCE );
  }

  public static Geometry differenceBA(Geometry a, Geometry b) {
      return overlay(b, a, DIFFERENCE );
  }

  public static Geometry intersection(Geometry a, Geometry b) {
    return overlay(a, b, INTERSECTION );
  }

  public static Geometry symDifference(Geometry a, Geometry b) {
    return overlay(a, b, SYMDIFFERENCE );
  }

  public static Geometry union(Geometry a, Geometry b) {
    return overlay(a, b, UNION );
  }

  private static Geometry overlay(Geometry a, Geometry b, int opCode) {
    OverlayNG overlay = new OverlayNG(a, b, opCode );
    overlay.setStrictMode(true);
    return overlay.getResult();

  }
}
