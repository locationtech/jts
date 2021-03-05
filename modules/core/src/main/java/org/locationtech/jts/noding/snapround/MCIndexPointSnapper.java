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

package org.locationtech.jts.noding.snapround;

import org.locationtech.jts.geom.Coordinate;
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
    final Envelope pixelEnv = getSafeEnvelope(hotPixel);
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

  private static final double SAFE_ENV_EXPANSION_FACTOR = 0.75;
  
  /**
   * Returns a "safe" envelope that is guaranteed to contain the hot pixel.
   * The envelope returned is larger than the exact envelope of the 
   * pixel by a safe margin.
   * 
   * @return an envelope which contains the hot pixel
   */
  public Envelope getSafeEnvelope(HotPixel hp)
  {
    double safeTolerance = SAFE_ENV_EXPANSION_FACTOR / hp.getScaleFactor();
    Envelope safeEnv = new Envelope(hp.getCoordinate());
    safeEnv.expandBy(safeTolerance);
    return safeEnv;
  }
  
  public static class HotPixelSnapAction
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

    /**
     * Reports whether the HotPixel caused a
     * node to be added in any target segmentString (including its own).
     * If so, the HotPixel must be added as a node as well.
     * @return true if a node was added in any target segmentString.
     */
    public boolean isNodeAdded() { return isNodeAdded; }

    /**
     * Check if a segment of the monotone chain intersects
     * the hot pixel vertex and introduce a snap node if so.
     * Optimized to avoid noding segments which
     * contain the vertex (which otherwise
     * would cause every vertex to be noded).
     */
    public void select(MonotoneChain mc, int startIndex)
    {
    	NodedSegmentString ss = (NodedSegmentString) mc.getContext();
      /**
       * Check to avoid snapping a hotPixel vertex to the its orginal vertex.
       * This method is called on segments which intersect the
       * hot pixel.
       * If either end of the segment is equal to the hot pixel
       * do not snap.
       */
      if (parentEdge != null && ss == parentEdge) {
        if (startIndex == hotPixelVertexIndex
              || startIndex + 1 == hotPixelVertexIndex
            )
          return;
      }
      // records if this HotPixel caused any node to be added
      isNodeAdded |= addSnappedNode(hotPixel, ss, startIndex);
    }

    /**
     * Adds a new node (equal to the snap pt) to the specified segment
     * if the segment passes through the hot pixel
     *
     * @param segStr
     * @param segIndex
     * @return true if a node was added to the segment
     */
    public boolean addSnappedNode(HotPixel hotPixel, 
        NodedSegmentString segStr,
        int segIndex
        )
    {
      Coordinate p0 = segStr.getCoordinate(segIndex);
      Coordinate p1 = segStr.getCoordinate(segIndex + 1);

      if (hotPixel.intersects(p0, p1)) {
        //System.out.println("snapped: " + snapPt);
        //System.out.println("POINT (" + snapPt.x + " " + snapPt.y + ")");
        segStr.addIntersection(hotPixel.getCoordinate(), segIndex);

        return true;
      }
      return false;
    }
  }

}
