


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
package org.locationtech.jts.geomgraph.index;

/**
 * @version 1.7
 */
public class MonotoneChain {

  MonotoneChainEdge mce;
  int chainIndex;

  public MonotoneChain(MonotoneChainEdge mce, int chainIndex) {
    this.mce = mce;
    this.chainIndex = chainIndex;
  }

  public void computeIntersections(MonotoneChain mc, SegmentIntersector si)
  {
    this.mce.computeIntersectsForChain(chainIndex, mc.mce, mc.chainIndex, si);
  }
}
