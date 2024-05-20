/*
 * Copyright (c) 2022 Martin Davis.
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

import org.locationtech.jts.geom.Envelope;

/**
 * The API for strategy classes implementing
 * spatial predicates based on the DE-9IM topology model.
 * Predicate values for specific geometry pairs can be evaluated by {@link RelateNG}.
 * 
 * @author Martin Davis
 */
public interface TopologyPredicate {

  /**
   * Gets the name of the predicate.
   * 
   * @return the predicate name
   */
  String name();
  
  /**
   * Reports whether this predicate requires self-noding for
   * geometries which contain crossing edges
   * (for example, {@link LineString}s, or {@line GeometryCollection}s
   * containing lines or polygons which may self-intersect).
   * Self-noding ensures that intersections are computed consistently
   * in cases which contain self-crossings and mutual crossings.
   * <p>
   * Most predicates require this, but it can
   * be avoided for simple intersection detection
   * (such as in {@link RelatePredicate#intersects()}
   * and {@link RelatePredicate#disjoint()}.
   * Avoiding self-noding improves performance for polygonal inputs.
   * 
   * @return true if self-noding is required.
   */
  default boolean requiresSelfNoding() {
    return true;
  }

  /**
   * Reports whether this predicate requires checking if the source input intersects
   * the Exterior of the target input.
   * This is the case if:
   * <pre>
   * IM[Int(Src), Ext(Tgt)] >= 0 or IM[Bdy(Src), Ext(Tgt)] >= 0
   * </pre> 
   * When not required to evaluate a predicate this permits performance optimization.
   *  
   * @param isSourceA flag indicating which input geometry is the source
   * @return true if the predicate requires checking whether the source intersects the target exterior
   */
  default boolean requiresExteriorCheck(boolean isSourceA) {
    return true;
  }
  
  /**
   * Initializes the predicate for a specific geometric case.
   * This may allow the predicate result to become known
   * if it can be inferred from the dimensions.
   * 
   * @param dimA the dimension of geometry A
   * @param dimB the dimension of geometry B
   * 
   * @see Dimension
   */
  default void init(int dimA, int dimB) {
    //-- default if dimensions provide no information
  }
  
  /**
   * Initializes the predicate for a specific geometric case.
   * This may allow the predicate result to become known
   * if it can be inferred from the envelopes.
   * 
   * @param envA the envelope of geometry A
   * @param envB the envelope of geometry B
   */
  default void init(Envelope envA, Envelope envB) {
  //-- default if envelopes provide no information
  }
  
  /**
   * Updates the entry in the DE-9IM intersection matrix
   * for given {@link Location}s in the input geometries.
   * <p>
   * If this method is called with a {@link Dimension} value 
   * which is less than the current value for the matrix entry,
   * the implementing class should avoid changing the entry 
   * if this would cause information loss.
   * 
   * @param locA the location on the A axis of the matrix
   * @param locB the location on the B axis of the matrix
   * @param dimension the dimension value for the entry
   * 
   * @see Dimension
   * @see Location
   */
  void updateDimension(int locA, int locB, int dimension);
  
  /**
   * Indicates that the value of the predicate can be finalized
   * based on its current state.
   */
  void finish();  
  
  /**
   * Tests if the predicate value is known.
   * 
   * @return true if the result is known
   */
  boolean isKnown();

  /**
   * Gets the current value of the predicate result.
   * The value is only valid if {@link #isKnown()} is true.
   * 
   * @return the predicate result value
   */
  boolean value();

}
