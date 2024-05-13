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
package org.locationtech.jts.operation.relateng;

import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainOverlapAction;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentString;

class EdgeSegmentOverlapAction
    extends MonotoneChainOverlapAction
{
  private SegmentIntersector si = null;

  public EdgeSegmentOverlapAction(SegmentIntersector si)
  {
    this.si = si;
  }

  public void overlap(MonotoneChain mc1, int start1, MonotoneChain mc2, int start2)
  {
    SegmentString ss1 = (SegmentString) mc1.getContext();
    SegmentString ss2 = (SegmentString) mc2.getContext();
    si.processIntersections(ss1, start1, ss2, start2);
  }

}