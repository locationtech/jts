/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.index.hprtree;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.ArrayListVisitor;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.util.IntArrayList;

/**
 * A Hilbert-Packed R-tree.  This is a static R-tree
 * which is packed by using the Hilbert ordering 
 * of the tree items.
 * <p>
 * The tree is constructed by sorting the items
 * by the Hilbert code of the midpoint of their envelope.  
 * Then, a set of internal layers is created recursively
 * as follows:
 * <ul>
 * <li>The items/nodes of the previous are partitioned into blocks 
 * of size <code>nodeCapacity</code>
 * <li>For each block a layer node is created with range
 * equal to the envelope of the items/nodess in the block
 * </ul>
 * The internal layers are stored using an array to
 * store the node bounds.
 * The link between a node and its children is 
 * stored implicitly in the indexes of the array.
 * For efficiency, the offsets to the layers
 * within the node array are pre-computed and stored.
 * <p>
 * NOTE: Based on performance testing, 
 * the HPRtree is somewhat faster than the STRtree.
 * It should also be more memory-efficent,
 * due to fewer object allocations.
 * However, it is not clear whether this 
 * will produce a significant improvement 
 * for use in JTS operations.
 * 
 * @see STRtree
 * 
 * 
 * @author Martin Davis
 *
 */
public class HPRtree
  implements SpatialIndex
{
  private static final int ENV_SIZE = 4;

  private static final int HILBERT_LEVEL = 12;

  private static final int DEFAULT_NODE_CAPACITY = 16;
  
  private List<Item> itemsToLoad = new ArrayList<>();

  private final int nodeCapacity;

  private int numItems = 0;

  private final Envelope totalExtent = new Envelope();

  private int[] layerStartIndex;

  private double[] nodeBounds;

  private double[] itemBounds;

  private Object[] itemValues;

  private volatile boolean isBuilt = false;

  /**
   * Creates a new index with the default node capacity.
   */
  public HPRtree() {
    this(DEFAULT_NODE_CAPACITY);
  }
  
  /**
   * Creates a new index with the given node capacity.
   * 
   * @param nodeCapacity the node capacity to use
   */
  public HPRtree(int nodeCapacity) {
    this.nodeCapacity = nodeCapacity;
  }
  
  /**
   * Gets the number of items in the index.
   * 
   * @return the number of items
   */
  public int size() {
    return numItems;
  }
  
  @Override
  public void insert(Envelope itemEnv, Object item) {
    if (isBuilt) {
      throw new IllegalStateException("Cannot insert items after tree is built.");
    }
    numItems++;
    itemsToLoad.add( new Item(itemEnv, item) );
    totalExtent.expandToInclude(itemEnv);
  }

  @Override
  public List query(Envelope searchEnv) {
    build();
    
    if (! totalExtent.intersects(searchEnv)) 
      return new ArrayList();
    
    ArrayListVisitor visitor = new ArrayListVisitor();
    query(searchEnv, visitor);
    return visitor.getItems();
  }

  @Override
  public void query(Envelope searchEnv, ItemVisitor visitor) {
    build();
    if (! totalExtent.intersects(searchEnv)) 
      return;
    if (layerStartIndex == null) {
      queryItems(0, searchEnv, visitor);
    }
    else {
      queryTopLayer(searchEnv, visitor);
    }
  }

  private void queryTopLayer(Envelope searchEnv, ItemVisitor visitor) {
    int layerIndex = layerStartIndex.length - 2;
    int layerSize = layerSize(layerIndex);
    // query each node in layer
    for (int i = 0; i < layerSize; i += ENV_SIZE) {
      queryNode(layerIndex, i, searchEnv, visitor);
    }
  }

  private void queryNode(int layerIndex, int nodeOffset, Envelope searchEnv, ItemVisitor visitor) {
    int layerStart = layerStartIndex[layerIndex];
    int nodeIndex = layerStart + nodeOffset;
    if (! intersects(nodeBounds, nodeIndex, searchEnv)) return;
    if (layerIndex == 0) {
      int childNodesOffset = nodeOffset / ENV_SIZE  * nodeCapacity;
      queryItems(childNodesOffset, searchEnv, visitor);
    }
    else {
      int childNodesOffset = nodeOffset * nodeCapacity;
      queryNodeChildren(layerIndex - 1, childNodesOffset, searchEnv, visitor);
    }
  }

  private static boolean intersects(double[] bounds, int nodeIndex, Envelope env) {
    boolean isBeyond = (env.getMaxX() < bounds[nodeIndex])
    || (env.getMaxY() < bounds[nodeIndex+1])
    || (env.getMinX() > bounds[nodeIndex+2])
    || (env.getMinY() > bounds[nodeIndex+3]);
    return ! isBeyond;
  }
  
  private void queryNodeChildren(int layerIndex, int blockOffset, Envelope searchEnv, ItemVisitor visitor) {
    int layerStart = layerStartIndex[layerIndex];
    int layerEnd = layerStartIndex[layerIndex + 1];
    for (int i = 0; i < nodeCapacity; i++) {
      int nodeOffset = blockOffset + ENV_SIZE * i; 
      // don't query past layer end
      if (layerStart + nodeOffset >= layerEnd) break;
      
      queryNode(layerIndex, nodeOffset, searchEnv, visitor);
    }
  }

  private void queryItems(int blockStart, Envelope searchEnv, ItemVisitor visitor) {
    for (int i = 0; i < nodeCapacity; i++) {
      int itemIndex = blockStart + i;
      // don't query past end of items
      if (itemIndex >= numItems) break;
      if (intersects(itemBounds, itemIndex * ENV_SIZE, searchEnv)) {
        visitor.visitItem(itemValues[itemIndex]);
      }
    }    
  }
  
  private int layerSize(int layerIndex) {
    int layerStart = layerStartIndex[layerIndex];
    int layerEnd = layerStartIndex[layerIndex + 1];
    return layerEnd - layerStart;
  }

  @Override
  public boolean remove(Envelope itemEnv, Object item) {
    // TODO Auto-generated method stub
    return false;
  }
  
  /**
   * Builds the index, if not already built.
   */
  public void build() {
    // skip if already built
    if (!isBuilt) {
      synchronized (this) {
        if (!isBuilt) {
          prepareIndex();
          prepareItems();
          this.isBuilt = true;
        }
      }
    }
  }

  private void prepareIndex() {
    // don't need to build an empty or very small tree
    if (itemsToLoad.size() <= nodeCapacity) return;

    sortItems();

    layerStartIndex = computeLayerIndices(numItems, nodeCapacity);
    // allocate storage
    int nodeCount = layerStartIndex[ layerStartIndex.length - 1 ] / 4;
    nodeBounds = createBoundsArray(nodeCount);

    // compute tree nodes
    computeLeafNodes(layerStartIndex[1]);
    for (int i = 1; i < layerStartIndex.length - 1; i++) {
      computeLayerNodes(i);
    }
  }

  private void prepareItems() {
    // copy item contents out to arrays for querying
    int boundsIndex = 0;
    int valueIndex = 0;
    itemBounds = new double[itemsToLoad.size() * 4];
    itemValues = new Object[itemsToLoad.size()];
    for (Item item : itemsToLoad) {
      Envelope envelope = item.getEnvelope();
      itemBounds[boundsIndex++] = envelope.getMinX();
      itemBounds[boundsIndex++] = envelope.getMinY();
      itemBounds[boundsIndex++] = envelope.getMaxX();
      itemBounds[boundsIndex++] = envelope.getMaxY();
      itemValues[valueIndex++] = item.getItem();
    }
    // and let GC free the original list
    itemsToLoad = null;
  }

  private static double[] createBoundsArray(int size) {
    double[] a = new double[4*size];
    for (int i = 0; i < size; i++) {
      int index = 4*i;
      a[index] = Double.MAX_VALUE;
      a[index+1] = Double.MAX_VALUE;
      a[index+2] = -Double.MAX_VALUE;
      a[index+3] = -Double.MAX_VALUE;
    }
    return a;
  }

  private void computeLayerNodes(int layerIndex) {
    int layerStart = layerStartIndex[layerIndex];
    int childLayerStart = layerStartIndex[layerIndex - 1];
    int layerSize = layerSize(layerIndex);
    int childLayerEnd = layerStart;
    for (int i = 0; i < layerSize; i += ENV_SIZE) {
      int childStart = childLayerStart + nodeCapacity * i;
      computeNodeBounds(layerStart + i, childStart, childLayerEnd);
    }
  }

  private void computeNodeBounds(int nodeIndex, int blockStart, int nodeMaxIndex) {
    for (int i = 0; i <= nodeCapacity; i++ ) {
      int index = blockStart + 4 * i;
      if (index >= nodeMaxIndex) break;
      updateNodeBounds(nodeIndex, nodeBounds[index], nodeBounds[index+1], nodeBounds[index+2], nodeBounds[index+3]);
    } 
  }

  private void computeLeafNodes(int layerSize) {
    for (int i = 0; i < layerSize; i += ENV_SIZE) {
      computeLeafNodeBounds(i, nodeCapacity * i/4);
    }
  }

  private void computeLeafNodeBounds(int nodeIndex, int blockStart) {
    for (int i = 0; i <= nodeCapacity; i++ ) {
      int itemIndex = blockStart + i;
      if (itemIndex >= itemsToLoad.size()) break;
      Envelope env = itemsToLoad.get(itemIndex).getEnvelope();
      updateNodeBounds(nodeIndex, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
    }
  }

  private void updateNodeBounds(int nodeIndex, double minX, double minY, double maxX, double maxY) {
    if (minX < nodeBounds[nodeIndex]) nodeBounds[nodeIndex] = minX;
    if (minY < nodeBounds[nodeIndex+1]) nodeBounds[nodeIndex+1] = minY;
    if (maxX > nodeBounds[nodeIndex+2]) nodeBounds[nodeIndex+2] = maxX;
    if (maxY > nodeBounds[nodeIndex+3]) nodeBounds[nodeIndex+3] = maxY;
  }
  
  private static int[] computeLayerIndices(int itemSize, int nodeCapacity) {
    IntArrayList layerIndexList = new IntArrayList();
    int layerSize = itemSize;
    int index = 0;
    do {
      layerIndexList.add(index);
      layerSize = numNodesToCover(layerSize, nodeCapacity);
      index += ENV_SIZE * layerSize;
    } while (layerSize > 1);
    return layerIndexList.toArray();
  }
  
  /**
   * Computes the number of blocks (nodes) required to 
   * cover a given number of children.
   * 
   * @param nChild
   * @param nodeCapacity
   * @return the number of nodes needed to cover the children
   */
  private static int numNodesToCover(int nChild, int nodeCapacity) {
    int mult = nChild / nodeCapacity;
    int total = mult * nodeCapacity;
    if (total == nChild) return mult;
    return mult + 1;
  }

  /**
   * Gets the extents of the internal index nodes
   * 
   * @return a list of the internal node extents
   */
  public Envelope[] getBounds() {
    int numNodes = nodeBounds.length / 4;
    Envelope[] bounds = new Envelope[numNodes];
    // create from largest to smallest
    for (int i = numNodes - 1; i >= 0; i--) {
      int boundIndex = 4 * i;
      bounds[i] = new Envelope( nodeBounds[boundIndex], nodeBounds[boundIndex+2],
          nodeBounds[boundIndex+1], nodeBounds[boundIndex+3]);
    }
    return bounds;
  }
  
  private void sortItems() {
    HilbertEncoder encoder = new HilbertEncoder(HILBERT_LEVEL, totalExtent);
    int[] hilbertValues = new int[itemsToLoad.size()];
    int pos = 0;
    for (Item item : itemsToLoad) {
      hilbertValues[pos++] = encoder.encode(item.getEnvelope());
    }
    quickSortItemsIntoNodes(hilbertValues, 0, itemsToLoad.size() - 1);
  }

  private void quickSortItemsIntoNodes(int[] values, int lo, int hi) {
    // stop sorting when left/right pointers are within the same node
    // because queryItems just searches through them all sequentially
    if (lo / nodeCapacity < hi / nodeCapacity) {
      int pivot = hoarePartition(values, lo, hi);
      quickSortItemsIntoNodes(values, lo, pivot);
      quickSortItemsIntoNodes(values, pivot + 1, hi);
    }
  }

  private int hoarePartition(int[] values, int lo, int hi) {
    int pivot = values[(lo + hi) >> 1];
    int i = lo - 1;
    int j = hi + 1;

    while (true) {
      do i++; while (values[i] < pivot);
      do j--; while (values[j] > pivot);
      if (i >= j) return j;
      swapItems(values, i, j);
    }
  }

  private void swapItems(int[] values, int i, int j) {
    Item tmpItemp = itemsToLoad.get(i);
    itemsToLoad.set(i, itemsToLoad.get(j));
    itemsToLoad.set(j, tmpItemp);

    int tmpValue = values[i];
    values[i] = values[j];
    values[j] = tmpValue;
  }

}
