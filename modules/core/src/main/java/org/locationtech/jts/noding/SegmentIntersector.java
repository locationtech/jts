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
package org.locationtech.jts.noding;

/**
 * <p>
 * Processes possible intersections detected by a {@link Noder}.
 * </p>
 * 
 * <p>
 * A {@code SegmentIntersector} is passed to a {@link Noder}, and its
 * {@link #processIntersections(SegmentString, int, SegmentString, int)} method is called 
 * whenever the {@code Noder} detects that two {@code SegmentString}s 
 * <i>might</i> intersect.
 * </p>
 * 
 * <p>
 * This interface can be used either to find all intersections, or to 
 * simply detect the presence of an intersection. If only detection is needed, 
 * implementations may short-circuit further computation by returning {@code true} 
 * from the {@link #isDone()} method.
 * </p>
 * 
 * <p>
 * This class is an example of the <i>Strategy</i> design pattern.
 * </p>
 *
 * @version 1.7
 */
public interface SegmentIntersector
{
  /**
   * This method is called by clients
   * of the {@link SegmentIntersector} interface to process
   * intersections for two segments of the {@link SegmentString}s being intersected.
   */
  void processIntersections(
    SegmentString e0,  int segIndex0,
    SegmentString e1,  int segIndex1
     );
  
  /**
   * Reports whether the client of this class needs to continue 
   * testing all intersections in an arrangement.
   * <p>
   * By default, this method returns {@code false}, indicating that 
   * all possible intersections will be processed.
   * Override this method to return {@code true} if you want
   * to short-circuit further processing (for example, once an intersection is found).
   * </p>
   *
   * @return {@code true} if there is no need to continue testing segments; 
   *         {@code false} to continue finding all intersections
   */
  default boolean isDone() {
	  return false;
  }
}
