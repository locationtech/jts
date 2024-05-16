/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

/**
 * String constants for DE-9IM matrix patterns for topological relationships.
 * These can be used with {@link RelateNG#evaluate(org.locationtech.jts.geom.Geometry, String)}
 * and {@link RelateNG#relate(org.locationtech.jts.geom.Geometry, org.locationtech.jts.geom.Geometry, String)}.
 * 
 * <h3>DE-9IM Pattern Matching</h3>
 * Matrix patterns are specified as a 9-character string 
 * containing the pattern symbols for the DE-9IM 3x3 matrix entries,
 * listed row-wise.
 * The pattern symbols are:
 * <ul>
 * <li><tt>0</tt> - topological interaction has dimension 0
 * <li><tt>1</tt> - topological interaction has dimension 1
 * <li><tt>2</tt> - topological interaction has dimension 2
 * <li><tt>F</tt> - no topological interaction
 * <li><tt>T</tt> - topological interaction of any dimension
 * <li><tt>*</tt> - any topological interaction is allowed, including none
 * </ul>
 * 
 * @author Martin Davis
 *
 */
public class IntersectionMatrixPattern {

  /**
   * A DE-9IM pattern to detect whether two polygonal geometries are adjacent along
   * an edge, but do not overlap.
   */
  public static final String ADJACENT = "F***1****";
  
  /**
   * A DE-9IM pattern to detect a geometry which properly contains another
   * geometry (i.e. which lies entirely in the interior of the first geometry).
   */
  public static final String CONTAINS_PROPERLY = "T**FF*FF*";
  
  /**
   * A DE-9IM pattern to detect if two geometries intersect in their interiors.
   * This can be used to determine if a polygonal coverage contains any overlaps
   * (although not whether they are correctly noded).
   */
  public static final String INTERIOR_INTERSECTS = "T********";

  /**
   * Cannot be instantiated.
   */
  private IntersectionMatrixPattern() {
    
  }
}
