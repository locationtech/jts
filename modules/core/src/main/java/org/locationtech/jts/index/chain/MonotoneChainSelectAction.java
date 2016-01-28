

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
package org.locationtech.jts.index.chain;

import org.locationtech.jts.geom.*;
/**
 * The action for the internal iterator for performing
 * envelope select queries on a MonotoneChain
 *
 * @version 1.7
 */
public class MonotoneChainSelectAction
{
  // these envelopes are used during the MonotoneChain search process
  Envelope tempEnv1 = new Envelope();

  LineSegment selectedSegment = new LineSegment();

  /**
   * This method is overridden 
   * to process a segment 
   * in the context of the parent chain.
   * 
   * @param mc the parent chain
   * @param startIndex the index of the start vertex of the segment being processed
   */
  public void select(MonotoneChain mc, int startIndex)
  {
    mc.getLineSegment(startIndex, selectedSegment);
    // call this routine in case select(segmenet) was overridden
    select(selectedSegment);
  }

  /**
   * This is a convenience method which can be overridden to obtain the actual
   * line segment which is selected.
   * 
   * @param seg
   */
  public void select(LineSegment seg)
  {
  }
}
