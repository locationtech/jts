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