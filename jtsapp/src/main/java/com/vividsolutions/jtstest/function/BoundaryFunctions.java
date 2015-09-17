/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
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
