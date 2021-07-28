package org.locationtech.jts.triangulatepoly;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.math.MathUtil;
import org.locationtech.jts.util.IntArrayList;

public class SequencePackedRtree {
  /**
   * Number of items/nodes in a node.
   * Determined empirically.  Performance is not too sensitive to this.
   */
  private static final int NODE_CAPACITY = 16;
  
  private Coordinate[] items;
  private int[] levelOffset;
  private int nodeCapacity  = NODE_CAPACITY;
  private Envelope[] bounds;

  public SequencePackedRtree(Coordinate[] pts) {
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

  public int[] query(Envelope queryEnv) {
    IntArrayList resultList = new IntArrayList();
    int level = levelOffset.length - 1;
    queryNode(queryEnv, level, 0, resultList);
    /*
    boolean isOK = checkResult(queryEnv, resultList);
    if (! isOK) {
      List<Integer> testList = new ArrayList<Integer>();
      queryNode(queryEnv, level, 0, testList);
      throw new IllegalStateException("SPTtree FAIL!");
    }
    //*/
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

  //--------------------------------
  
  private boolean checkResult(Envelope queryEnv, List<Integer> actual) {
    for (int iRes : actual) {
      if (! queryEnv.contains( items[iRes] )) return false;
    }
    
    List<Integer> res = slowQuery(queryEnv);
    if (res.size() != actual.size())
      return false;
    return true;
  }

  private List<Integer> slowQuery(Envelope queryEnv) {
    List<Integer> res = new ArrayList<Integer>();
    for (int i = 0; i < items.length; i++) {
      if (queryEnv.contains(items[i]))
        res.add(i);
    }
    return res;
  }
}
