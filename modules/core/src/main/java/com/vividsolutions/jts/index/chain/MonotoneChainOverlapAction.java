

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
package com.vividsolutions.jts.index.chain;

import com.vividsolutions.jts.geom.*;

/**
 * The action for the internal iterator for performing
 * overlap queries on a MonotoneChain
 *
 * @version 1.7
 */
public class MonotoneChainOverlapAction
{
  // these envelopes are used during the MonotoneChain search process
  Envelope tempEnv1 = new Envelope();
  Envelope tempEnv2 = new Envelope();

  protected LineSegment overlapSeg1 = new LineSegment();
  protected LineSegment overlapSeg2 = new LineSegment();

  /**
   * This function can be overridden if the original chains are needed
   *
   * @param start1 the index of the start of the overlapping segment from mc1
   * @param start2 the index of the start of the overlapping segment from mc2
   */
  public void overlap(MonotoneChain mc1, int start1, MonotoneChain mc2, int start2)
  {
    mc1.getLineSegment(start1, overlapSeg1);
    mc2.getLineSegment(start2, overlapSeg2);
    overlap(overlapSeg1, overlapSeg2);
  }

  /**
   * This is a convenience function which can be overridden to obtain the actual
   * line segments which overlap
   * @param seg1
   * @param seg2
   */
  public void overlap(LineSegment seg1, LineSegment seg2)
  {
  }
}
