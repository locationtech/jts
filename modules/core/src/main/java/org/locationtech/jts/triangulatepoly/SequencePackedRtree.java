package org.locationtech.jts.triangulatepoly;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

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

  public int[] query(Envelope queryEnv) {
    List<Integer> resultList = new ArrayList<Integer>();
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
    int[] result = toIntArray(resultList);
    return result;
  }
  
  private void queryNode(Envelope queryEnv, int level, int nodeIndex, List<Integer> result) {
    int lvlOffset = levelOffset[level];
    int boundsIndex = lvlOffset + nodeIndex;
    Envelope nodeBounds = bounds[boundsIndex];
    if (! queryEnv.intersects(nodeBounds))
      return;
    
    int childNodeIndex = nodeCapacity * nodeIndex;
    if (level == 0) {
      queryItemRange(queryEnv, childNodeIndex, result);
    }
    else {
      queryNodeRange(queryEnv, level - 1, childNodeIndex, result);
    }
  }

  private void queryNodeRange(Envelope queryEnv, int level, int nodeBlockIndex, List<Integer> result) {  
    int indexMax = levelOffset[level + 1] - levelOffset[level];
    for (int i = 0; i < nodeCapacity; i++) {
      int index = nodeBlockIndex + i;
      if (index >= indexMax) 
        return;
      queryNode(queryEnv, level, index, result);
    }    
  }

  private void queryItemRange(Envelope queryEnv, int itemIndex, List<Integer> result) {
    for (int i = 0; i < nodeCapacity; i++) {
      int index = itemIndex + i;
      if (index >= items.length) 
        return;
      Coordinate p = items[index];
      if (p != null && queryEnv.contains(p))
        result.add(index);
    }
  }

  public void remove(int index) {
    items[index] = null;
    //TODO: propagate removal up the tree nodes
  }
  
  private void build() {
    levelOffset = computeLevelOffsets();
    bounds = computeBounds();
  }

  private int[] computeLevelOffsets() {
    List<Integer> offsets = new ArrayList<Integer>();
    offsets.add(0);
    int levelSize = items.length;
    int currOffset = 0;
    do {
      
      levelSize = levelSize(levelSize);
      
      currOffset += levelSize;
      offsets.add(currOffset);
    } while (levelSize > 1);
    return toIntArray(offsets);
  }

  private int levelSize(int nodeNum) {
    int size = nodeNum / nodeCapacity;
    if (size * nodeCapacity < nodeNum) 
      size++;
    return size;
  }
  
  private Envelope[] computeBounds() {
    int boundsSize = levelOffset[levelOffset.length - 1] + 1;
    Envelope[] bounds = new Envelope[boundsSize];
    computeItemBounds(bounds);
    
    for (int lvl = 1; lvl < levelOffset.length; lvl++) {
      computeLevelBounds(bounds, lvl);
    }
    return bounds;
  }
  
  private void computeLevelBounds(Envelope[] bounds, int lvl) {
    int childLevelStart = levelOffset[lvl - 1]; 
    int childLevelEnd = levelOffset[lvl];
    int nodeStart = childLevelStart;
    int levelBoundIndex = levelOffset[lvl];
    do {
      int nodeEnd = nodeStart + nodeCapacity;
      if (nodeEnd > childLevelEnd) nodeEnd = childLevelEnd;
      bounds[levelBoundIndex++] = computeNodeEnvelope(bounds, nodeStart, nodeEnd);
      nodeStart = nodeEnd;
    }
    while (nodeStart < childLevelEnd);
  }

  private void computeItemBounds(Envelope[] bounds) {
    int nodeStart = 0;
    int boundIndex = 0;
    do {
      int nodeEnd = nodeStart + nodeCapacity;
      if (nodeEnd > items.length) nodeEnd = items.length;
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

  private static int[] toIntArray(List<Integer> list) {
    int[] array = new int[list.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = list.get(i);
    }
    return array;
  }
  
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
