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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.UnaryUnionNG;
import org.locationtech.jtstest.geomfunction.Metadata;

public class OverlayNGAutoPMFunctions {
  
  @Metadata(description="Intersection with automatically-determined maximum precision")
  public static Geometry intersection(Geometry a, Geometry b) {
    return OverlayNG.overlayFixedPrecision(a, b, INTERSECTION);
  }
  
  @Metadata(description="Union with automatically-determined maximum precision")
  public static Geometry union(Geometry a, Geometry b) {
    return OverlayNG.overlayFixedPrecision(a, b, UNION);
  }
  
  @Metadata(description="Difference with automatically-determined maximum precision")
  public static Geometry difference(Geometry a, Geometry b) {
    return OverlayNG.overlayFixedPrecision(a, b, DIFFERENCE);
  }  
  
  @Metadata(description="Unary union with automatically-determined maximum precision")
  public static Geometry unaryUnion(Geometry a) {
    return UnaryUnionNG.union(a);
  }
  

  
}
