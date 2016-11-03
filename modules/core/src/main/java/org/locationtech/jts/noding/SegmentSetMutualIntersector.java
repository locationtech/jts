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
package org.locationtech.jts.noding;

import java.util.Collection;

/**
 * An intersector for the red-blue intersection problem.
 * In this class of line arrangement problem,
 * two disjoint sets of linestrings are intersected.
 * <p>
 * Implementing classes must provide a way
 * of supplying the base set of segment strings to 
 * test against (e.g. in the constructor, 
 * for straightforward thread-safety).
 * <p>
 * In order to allow optimizing processing, 
 * the following condition is assumed to hold for each set:
 * <ul>
 * <li>the only intersection between any two linestrings occurs at their endpoints.
 * </ul>
 * Implementations can take advantage of this fact to optimize processing
 * (i.e by avoiding testing for intersections between linestrings
 * belonging to the same set).
 *
 * @author Martin Davis
 * @version 1.10
 */
public interface SegmentSetMutualIntersector
{  
  /**
   * Computes the intersections with a given set of {@link SegmentString}s,
   * using the supplied {@link SegmentIntersector}.
   *
   * @param segStrings a collection of {@link SegmentString}s to node
   * @param segInt the intersection detector to either record intersection occurences
   * 			 or add intersection nodes to the input segment strings.
   */
  void process(Collection segStrings, SegmentIntersector segInt);
}