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

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.algorithm.MinimumBoundingCircle;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.OctagonalEnvelope;
import org.locationtech.jts.operation.BoundaryOp;

public class BoundaryFunctions {  
  public static Geometry boundary(Geometry g) {      return g.getBoundary();  }

  public static Geometry boundaryMod2(Geometry g) {
    return BoundaryOp.getBoundary(g, BoundaryNodeRule.MOD2_BOUNDARY_RULE);
  }
  public static Geometry boundaryEndpoint(Geometry g) {
    return BoundaryOp.getBoundary(g, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE);
  }
  public static Geometry boundaryMonoValentEnd(Geometry g) {
    return BoundaryOp.getBoundary(g, BoundaryNodeRule.MONOVALENT_ENDPOINT_BOUNDARY_RULE);
  }
  public static Geometry boundaryMultiValentEnd(Geometry g) {
    return BoundaryOp.getBoundary(g, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE);
  }
  
}
