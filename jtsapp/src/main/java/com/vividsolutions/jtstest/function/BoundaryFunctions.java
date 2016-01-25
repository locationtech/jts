/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.algorithm.BoundaryNodeRule;
import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.algorithm.MinimumDiameter;
import com.vividsolutions.jts.densify.Densifier;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.OctagonalEnvelope;
import com.vividsolutions.jts.operation.BoundaryOp;

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
