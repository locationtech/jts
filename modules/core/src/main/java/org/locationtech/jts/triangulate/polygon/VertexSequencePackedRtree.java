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
package org.locationtech.jts.triangulate.polygon;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.math.MathUtil;
import org.locationtech.jts.util.IntArrayList;

/**
 * A semi-static spatial index for points which occur 
 * in a spatially-coherent sequence.
 * In particular, this is suitable for indexing the vertices
 * of a {@link LineString} or {@link Polygon} ring.
 * <p>
 * The index is constructed in a batch fashion on a given sequence of coordinates.
 * Coordinates can be removed via the {@link #remove(int)} method.
 * <p>
 * Note that this index queries only the individual points
 * of the input coordinate sequence, 
 * <b>not</b> any line segments which might be lie between them.
 * 
 * @author Martin Davis
 *
 */
class VertexSequencePackedRtree {
  
  /**
   * Number of items/nodes in a parent node.
   * Determined empirically.  Performance is not too sensitive to this.
   */
  private static final int NODE_CAPACITY = 16;
  
  private Coordinate[] items;
  private int[] levelOffset;
  private int nodeCapacity  = NODE_CAPACITY;
  private Envelope[] bounds;

  /**
   * Creates a new tree over the given sequence of coordinates.
   * The sequence should be spatially coherent to provide query performance.
   * 
   * @param pts a sequence of points
   */
  public VertexSequencePackedRtree(Coordinate[] pts) {
    this.items = pts;
    build();
  }

  public Envelope[] getBounds() {
    return bounds.clone();
  }
  
  private void build() {
    levelOffset = computeLevelOffsets();
    bounds = createBounds();
  }

  /**
   * Computes the level offsets.
   * This is the position in the <tt>bounds</tt> array of each level.
   * 
   * The levelOffsets array includes a sentinel value of offset[0] = 0.
   * The top level is always of size 1,
   * and so also indicates the total number of bounds.
   * 
   * @return the level offsets
   */
  private int[] computeLevelOffsets() {
    IntArrayList offsets = new IntArrayList();
    offsets.add(0);
    int levelSize = items.length;
    int currOffset = 0;
    do {
      levelSize = levelNodeCount(levelSize);
      currOffset += levelSize;
      offsets.add(currOffset);
    } while (levelSize > 1);
    return offsets.toArray();
  }

  private int levelNodeCount(int numNodes) {
    return MathUtil.ceil(numNodes, nodeCapacity);
  }
  
  private Envelope[] createBounds() {
    int boundsSize = levelOffset[levelOffset.length - 1] + 1;
    Envelope[] bounds = new Envelope[boundsSize];
    fillItemBounds(bounds);
    
    for (int lvl = 1; lvl < levelOffset.length; lvl++) {
      fillLevelBounds(lvl, bounds);
    }
    return bounds;
  }
  
  private void fillLevelBounds(int lvl, Envelope[] bounds) {
    int levelStart = levelOffset[lvl - 1]; 
    int levelEnd = levelOffset[lvl];
    int nodeStart = levelStart;
    int levelBoundIndex = levelOffset[lvl];
    do {
      int nodeEnd = MathUtil.clampMax(nodeStart + nodeCapacity, levelEnd);
      bounds[levelBoundIndex++] = computeNodeEnvelope(bounds, nodeStart, nodeEnd);
      nodeStart = nodeEnd;
    }
    while (nodeStart < levelEnd);
  }

  private void fillItemBounds(Envelope[] bounds) {
    int nodeStart = 0;
    int boundIndex = 0;
    do {
      int nodeEnd = MathUtil.clampMax(nodeStart + nodeCapacity, items.length);
      bounds[boundIndex++] = computeItemEnvelope(items, nodeStart, nodeEnd);
      nodeStart = nodeEnd;
    }
    while (nodeStart < items.length);
  }

  private static Envelope computeNodeEnvelope(Envelope[] bounds, int start, int end) {
    Envelope env = new Envelope();
    for (int i = start; i < end; i++) {
      env.expandToInclude(bounds[i]);
    }
    return env;
  }
  
  private static Envelope computeItemEnvelope(Coordinate[] items, int start, int end) {
    Envelope env = new Envelope();
    for (int i = start; i < end; i++) {
      env.expandToInclude(items[i]);
    }
    return env;
  }
  
  //------------------------

  /**
   * Queries the index to find all items which intersect an extent.
   * The query result is a list of the indices of input coordinates
   * which intersect the extent.
   * 
   * @param queryEnv the query extent
   * @return an array of the indices of the input coordinates
   */
  public int[] query(Envelope queryEnv) {
    IntArrayList resultList = new IntArrayList();
    int level = levelOffset.length - 1;
    queryNode(queryEnv, level, 0, resultList);
    int[] result = resultList.toArray();
    return result;
  }
  
  private void queryNode(Envelope queryEnv, int level, int nodeIndex, IntArrayList resultList) {
    int boundsIndex = levelOffset[level] + nodeIndex;
    Envelope nodeEnv = bounds[boundsIndex];
    //--- node is empty
    if (nodeEnv == null)
      return;
    if (! queryEnv.intersects(nodeEnv))
      return;
    
    int childNodeIndex = nodeIndex * nodeCapacity;
    if (level == 0) {
      queryItemRange(queryEnv, childNodeIndex, resultList);
    }
    else {
      queryNodeRange(queryEnv, level - 1, childNodeIndex, resultList);
    }
  }

  private void queryNodeRange(Envelope queryEnv, int level, int nodeStartIndex, IntArrayList resultList) {  
    int levelMax = levelSize(level);
    for (int i = 0; i < nodeCapacity; i++) {
      int index = nodeStartIndex + i;
      if (index >= levelMax) 
        return;
      queryNode(queryEnv, level, index, resultList);
    }    
  }

  private int levelSize(int level) {
    return levelOffset[level + 1] - levelOffset[level];
  }

  private void queryItemRange(Envelope queryEnv, int itemIndex, IntArrayList resultList) {
    for (int i = 0; i < nodeCapacity; i++) {
      int index = itemIndex + i;
      if (index >= items.length) 
        return;
      Coordinate p = items[index];
      if (p != null 
          && queryEnv.contains(p))
        resultList.add(index);
    }
  }

  //------------------------
  
  /**
   * Removes the input item at the given index from the spatial index.
   * 
   * @param index the index of the item in the input
   */
  public void remove(int index) {
    items[index] = null;
    
    //--- prune the item parent node if all its items are removed
    int nodeIndex = index / nodeCapacity;
    if (! isItemsNodeEmpty(nodeIndex))
      return;
    
    bounds[nodeIndex] = null;
    
    if (levelOffset.length <= 2)
      return;

    //-- prune the node parent if all children removed
    int nodeLevelIndex = nodeIndex / nodeCapacity;
    if (! isNodeEmpty(1, nodeLevelIndex))
      return;
    int nodeIndex1 = levelOffset[1] + nodeLevelIndex;
    bounds[nodeIndex1] = null;
    
    //TODO: propagate removal up the tree nodes?
  }
  
  private boolean isNodeEmpty(int level, int index) {
    int start = index * nodeCapacity;
    int end = MathUtil.clampMax(start + nodeCapacity, levelOffset[level]);
    for (int i = start; i < end; i++) {
      if (bounds[i] != null) return false;
    }
    return true;
  }

  private boolean isItemsNodeEmpty(int nodeIndex) {
    int start = nodeIndex * nodeCapacity;
    int end = MathUtil.clampMax(start + nodeCapacity, items.length);
    for (int i = start; i < end; i++) {
      if (items[i] != null) return false;
    }
    return true;
  }

}
