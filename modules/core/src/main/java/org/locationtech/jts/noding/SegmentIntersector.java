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

/**
 * Processes possible intersections detected by a {@link Noder}.
 * The {@link SegmentIntersector} is passed to a {@link Noder}.
 * The {@link SegmentIntersector#processIntersections(SegmentString, int, SegmentString, int)} method is called whenever the {@link Noder}
 * detects that two SegmentStrings <i>might</i> intersect.
 * This class may be used either to find all intersections, or
 * to detect the presence of an intersection.  In the latter case,
 * Noders may choose to short-circuit their computation by calling the
 * {@link #isDone()} method.
 * This class is an example of the <i>Strategy</i> pattern.
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
   * Reports whether the client of this class
   * needs to continue testing all intersections in an arrangement.
   * 
   * @return true if there is no need to continue testing segments
   */
  boolean isDone();
}
