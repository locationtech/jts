/*
 * Copyright (c) 2023 Martin Davis.
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
import org.locationtech.jts.operation.relateng.IntersectionMatrixPattern;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.locationtech.jts.operation.relateng.RelatePredicate;

public class SpatialPredicateNGFunctions {
  public static boolean contains(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.contains());    
  }
  public static boolean covers(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.covers());    
  }
  public static boolean coveredBy(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.coveredBy());    
  }
  public static boolean disjoint(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.disjoint());    
  }
  public static boolean equals(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.equalsTopo());    
  }
  public static boolean equalsTopo(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.equalsTopo());    
  }
  public static boolean intersects(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.intersects());    
  }
  public static boolean crosses(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.crosses());    
  }
  public static boolean overlaps(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.overlaps());    
  }
  public static boolean touches(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.touches());    
  }
  public static boolean within(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.within());    
  }
  
  public static boolean adjacent(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.matches(IntersectionMatrixPattern.ADJACENT)); 
  }
  
  public static boolean containsProperly(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.matches(IntersectionMatrixPattern.CONTAINS_PROPERLY)); 
  }
  
  public static boolean interiorIntersects(Geometry a, Geometry b) {    
    return RelateNG.relate(a, b, RelatePredicate.matches(IntersectionMatrixPattern.INTERIOR_INTERSECTS)); 
  }
  
  public static boolean relate(Geometry a, Geometry b, String mask) {
    return RelateNG.relate(a, b, mask);  
  }
  public static String relateMatrix(Geometry a, Geometry b) {
    return RelateNG.relate(a, b).toString(); 
  }
  public static String relateEndpoint(Geometry a, Geometry b) {
    return RelateNG.relate(a, b, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE).toString();
  }
  public static String relateMultiValent(Geometry a, Geometry b) {
    return RelateNG.relate(a, b, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE).toString();
  }
  public static String relateMonoValent(Geometry a, Geometry b) {
    return RelateNG.relate(a, b, BoundaryNodeRule.MONOVALENT_ENDPOINT_BOUNDARY_RULE).toString();
  }
}
