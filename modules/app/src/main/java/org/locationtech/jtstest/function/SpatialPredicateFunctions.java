/*
 * Copyright (c) 2016 Vivid Solutions.
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

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.relate.RelateOp;
import org.locationtech.jts.operation.relateng.IntersectionMatrixPattern;
import org.locationtech.jts.operation.relateng.RelateNG;

/**
 * Implementations for spatial predicate functions.
 * 
 * @author Martin Davis
 * 
 */
public class SpatialPredicateFunctions {

  public static boolean intersects(Geometry a, Geometry b) {    return a.intersects(b);    }
  public static boolean crosses(Geometry a, Geometry b) {    return a.crosses(b);    }
  public static boolean disjoint(Geometry a, Geometry b) {    return a.disjoint(b);    }
  public static boolean equals(Geometry a, Geometry b) {    return a.equals(b);    }
  public static boolean contains(Geometry a, Geometry b) {    return a.contains(b);    }
  public static boolean covers(Geometry a, Geometry b) {    return a.covers(b);    }
  public static boolean coveredBy(Geometry a, Geometry b) {    return a.coveredBy(b);    }
  public static boolean within(Geometry a, Geometry b) {    return a.within(b);    }
  public static boolean overlaps(Geometry a, Geometry b) {    return a.overlaps(b);    }
  public static boolean touches(Geometry a, Geometry b) {    return a.touches(b);    }
  
  public static boolean interiorIntersects(Geometry a, Geometry b) { 
    return a.relate(b, IntersectionMatrixPattern.INTERIOR_INTERSECTS);
  }
  
  public static boolean adjacent(Geometry a, Geometry b) { 
    return a.relate(b, IntersectionMatrixPattern.ADJACENT);
  }
  
  public static boolean containsProperly(Geometry a, Geometry b) {    
    return a.relate(b, IntersectionMatrixPattern.CONTAINS_PROPERLY);
  }
  
  public static String relateMatrix(Geometry a, Geometry b) {
    return a.relate(b).toString();
  }
  public static boolean relate(Geometry a, Geometry b, String mask) {
    return a.relate(b, mask);
  }
  public static String relateEndpoint(Geometry a, Geometry b) {
    return RelateOp.relate(a, b, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE).toString();
  }
  public static String relateMultiValent(Geometry a, Geometry b) {
    return RelateOp.relate(a, b, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE).toString();
  }
  public static String relateMonoValent(Geometry a, Geometry b) {
    return RelateOp.relate(a, b, BoundaryNodeRule.MONOVALENT_ENDPOINT_BOUNDARY_RULE).toString();
  }
}
