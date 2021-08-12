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
package org.locationtech.jts.operation.union;

import org.locationtech.jts.geom.Geometry;

/**
 * A strategy class that adapts UnaryUnion to different 
 * kinds of overlay algorithms.
 *  
 * @author Martin Davis
 *
 */
public interface UnionStrategy {

  /**
   * Computes the union of two geometries.
   * This method may throw a {@link org.locationtech.jts.geom.TopologyException}
   * if one is encountered.
   * 
   * @param g0 a geometry
   * @param g1 a geometry
   * @return the union of the inputs
   */
  Geometry union(Geometry g0, Geometry g1);
  
  /**
   * Indicates whether the union function operates using 
   * a floating (full) precision model. 
   * If this is the case, then the unary union code 
   * can make use of the {@link OverlapUnion} performance optimization,
   * and perhaps other optimizations as well.
   * Otherwise, the union result extent may not be the same as the extent of the inputs,
   * which prevents using some optimizations.
   * 
   * @return true if the union function operates using floating precision
   */
  boolean isFloatingPrecision();
}
