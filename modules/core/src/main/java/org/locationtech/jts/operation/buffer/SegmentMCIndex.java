/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.buffer;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainBuilder;
import org.locationtech.jts.index.chain.MonotoneChainSelectAction;
import org.locationtech.jts.index.strtree.STRtree;

/**
 * A spatial index over a segment sequence 
 * using {@link MonotoneChain}s.
 * 
 * @author mdavis
 *
 */
class SegmentMCIndex {
  private STRtree index;
  
  public SegmentMCIndex(Coordinate[] segs) {
    index = buildIndex(segs);
  }
  
  private STRtree buildIndex(Coordinate[] segs) {
    STRtree index = new STRtree();
    List<MonotoneChain> segChains = MonotoneChainBuilder.getChains(segs, segs);
    for (MonotoneChain mc : segChains ) {
      index.insert(mc.getEnvelope(), mc);
    }
    return index;
  }

  public void query(Envelope env, MonotoneChainSelectAction action) {
    index.query(env, new ItemVisitor() {
      public void visitItem(Object item) {
        MonotoneChain testChain = (MonotoneChain) item;
        testChain.select(env, action);
      }
    });
  }
}