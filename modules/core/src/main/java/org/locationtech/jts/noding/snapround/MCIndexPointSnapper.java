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

package org.locationtech.jts.noding.snapround;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainSelectAction;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentString;

/**
 * "Snaps" all {@link SegmentString}s in a {@link SpatialIndex} containing
 * {@link MonotoneChain}s to a given {@link HotPixel}.
 *
 * @version 1.7
 */
public class MCIndexPointSnapper
{
  //public static final int nSnaps = 0;

  private STRtree index;

  public MCIndexPointSnapper(SpatialIndex index) {
    this.index = (STRtree) index;
  }

  /**
   * Snaps (nodes) all interacting segments to this hot pixel.
   * The hot pixel may represent a vertex of an edge,
   * in which case this routine uses the optimization
   * of not noding the vertex itself
   *
   * @param hotPixel the hot pixel to snap to
   * @param parentEdge the edge containing the vertex, if applicable, or <code>null</code>
   * @param hotPixelVertexIndex the index of the hotPixel vertex, if applicable, or -1
   * @return <code>true</code> if a node was added for this pixel
   */
  public boolean snap(HotPixel hotPixel, SegmentString parentEdge, int hotPixelVertexIndex)
  {
    final Envelope pixelEnv = hotPixel.getSafeEnvelope();
    final HotPixelSnapAction hotPixelSnapAction = new HotPixelSnapAction(hotPixel, parentEdge, hotPixelVertexIndex);

    index.query(pixelEnv, new ItemVisitor() {
      public void visitItem(Object item) {
        MonotoneChain testChain = (MonotoneChain) item;
        testChain.select(pixelEnv, hotPixelSnapAction);
      }
    }
    );
    return hotPixelSnapAction.isNodeAdded();
  }

  public boolean snap(HotPixel hotPixel)
  {
    return snap(hotPixel, null, -1);
  }

  public class HotPixelSnapAction
      extends MonotoneChainSelectAction
  {
    private HotPixel hotPixel;
    private SegmentString parentEdge;
    // is -1 if hotPixel is not a vertex
    private int hotPixelVertexIndex;
    private boolean isNodeAdded = false;

    public HotPixelSnapAction(HotPixel hotPixel, SegmentString parentEdge, int hotPixelVertexIndex)
    {
      this.hotPixel = hotPixel;
      this.parentEdge = parentEdge;
      this.hotPixelVertexIndex = hotPixelVertexIndex;
    }

    public boolean isNodeAdded() { return isNodeAdded; }

    public void select(MonotoneChain mc, int startIndex)
    {
    	NodedSegmentString ss = (NodedSegmentString) mc.getContext();
      /**
       * Check to avoid snapping a hotPixel vertex to the same vertex.
       * This method is called for segments which intersects the 
       * hot pixel,
       * so need to check if either end of the segment is equal to the hot pixel
       * and if so, do not snap.
       * 
       * Sep 22 2012 - MD - currently do need to snap to every vertex,
       * since otherwise the testCollapse1 test in SnapRoundingTest fails.
       */
      if (parentEdge != null) {
        if (ss == parentEdge && 
            (startIndex == hotPixelVertexIndex
                ))
          return;
      }
      isNodeAdded = hotPixel.addSnappedNode(ss, startIndex);
    }

  }

}
