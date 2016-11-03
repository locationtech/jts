

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
package org.locationtech.jts.index.chain;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;
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
