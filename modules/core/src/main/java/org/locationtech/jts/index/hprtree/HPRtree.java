/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.index.hprtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.ArrayListVisitor;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.SpatialIndex;

/**
 * A Hilbert-Packed R-tree.
 * 
 * @author Martin Davis
 *
 */
public class HPRtree  
  implements SpatialIndex
{
  private static final int HILBERT_LEVEL = 12;

  private static int FANOUT = 4;
  
  private List<Item> items = new ArrayList<Item>();
  
  private int itemCount = 0;
  private int fanOut = FANOUT;

  private Envelope totalExtent = new Envelope();

  private int[] layerStartIndex;

  private double[] nodeMinX;

  private double[] nodeMinY;

  private double[] nodeMaxX;

  private double[] nodeMaxY;

  private boolean isBuilt = false;

  public HPRtree() {
    
  }
  
  public HPRtree(int fanOut) {
    this.fanOut = fanOut;
  }
  
  public int size() {
    return items.size();
  }
  
  @Override
  public void insert(Envelope itemEnv, Object item) {
    if (isBuilt) {
      throw new IllegalStateException("Cannot insert items after tree is built.");
    }
    if (items != null) {
      items.add( new Item(itemEnv, item) );
      totalExtent.expandToInclude(itemEnv);
      itemCount++;
    }
    else {
      //TODO: add item to list to be built later
    }
  }

  @Override
  public List query(Envelope searchEnv) {
    
    if (! totalExtent.intersects(searchEnv)) 
      return new ArrayList();
    
    ArrayListVisitor visitor = new ArrayListVisitor();
    query(searchEnv, visitor);
    return visitor.getItems();
  }

  @Override
  public void query(Envelope searchEnv, ItemVisitor visitor) {
    if (! totalExtent.intersects(searchEnv)) 
      return;
    build();
    if (layerStartIndex == null) {
      queryItems(0, searchEnv, visitor);
    }
    else {
      queryTopLayer(layerStartIndex.length - 2, searchEnv, visitor);
    }
  }

  private void queryTopLayer(int layerIndex, Envelope searchEnv, ItemVisitor visitor) {
    int layerSize = layerSize(layerIndex);
    // query each node in layer
    for (int i = 0; i < layerSize; i++) {
      queryNode(layerIndex, i, searchEnv, visitor);
    }
  }

  private void queryNode(int layerIndex, int i, Envelope searchEnv, ItemVisitor visitor) {
    int layerStart = layerStartIndex[layerIndex];
    int nodeIndex = layerStart + i;
    if (! intersectsBounds(nodeIndex, searchEnv)) return;
    if (layerIndex == 0) {
      queryItems(i * fanOut, searchEnv, visitor);
    }
    else 
      queryNodeSpan(layerIndex - 1, i * fanOut, searchEnv, visitor);
  }

  private void queryNodeSpan(int layerIndex, int nodeSpanStart, Envelope searchEnv, ItemVisitor visitor) {
    int layerEnd = layerEndIndex(layerIndex);
    for (int i = 0; i < fanOut; i++) {
      int nodeIndex = nodeSpanStart + i; 
      // don't query past layer
      if (nodeIndex >= layerEnd) break;
      
      queryNode(layerIndex, nodeIndex, searchEnv, visitor);
    }
  }

  private void queryItems(int itemSpanStart, Envelope searchEnv, ItemVisitor visitor) {
    for (int i = 0; i < fanOut; i++) {
      int itemIndex = itemSpanStart + i; 
      // don't query past end of items
      if (itemIndex >= items.size()) break;
      
      // query the item if its envelope intersects search env
      Item item = items.get(itemIndex);
      if (item.getEnvelope().intersects(searchEnv)) {
        visitor.visitItem(item.getItem());
      }
    }    
  }

  private int layerEndIndex(int layerIndex) {
    return layerStartIndex[layerIndex + 1];
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
  
  public synchronized void build() {
    // skip if already built
    if (isBuilt) return;
    isBuilt  = true;
    // don't need to build an empty or very small tree
    if (items.size() <= fanOut) return;

    sortItems();
    layerStartIndex = computeLayerIndices(items.size(), fanOut);
    // allocate storage
    int nodeCount = layerStartIndex[ layerStartIndex.length - 1 ];
    nodeMinX = createBoundArray(nodeCount, Double.MAX_VALUE);
    nodeMinY = createBoundArray(nodeCount, Double.MAX_VALUE);
    nodeMaxX = createBoundArray(nodeCount, -Double.MAX_VALUE);
    nodeMaxY = createBoundArray(nodeCount, -Double.MAX_VALUE);
    
    // compute tree nodes
    computeLeafNodes(layerStartIndex[1]);
    for (int i = 1; i < layerStartIndex.length - 1; i++) {
      computeLayerNodes(i);
    }
  }

  private static double[] createBoundArray(int size, double value) {
    double[] a = new double[size];
    Arrays.fill(a, value);
    return a;
  }

  private void computeLayerNodes(int layerIndex) {
    int layerStart = layerStartIndex[layerIndex];
    int layerEnd = layerStartIndex[layerIndex + 1];
    int layerSize = layerEnd - layerStart;
    int childLayerEnd = layerStart;
    for (int i = 0; i < layerSize; i++) {
      int childStart = layerStartIndex[layerIndex - 1] + fanOut * i;
      computeNodeBounds(layerStart + i, childStart, childLayerEnd);
      //System.out.println("Layer: " + layerIndex + " node: " + i + " - " + getNodeEnvelope(layerStart + i));
    }
  }

  private void computeNodeBounds(int nodeIndex, int nodeSpanStart, int nodeMaxIndex) {
    for (int i = 0; i <= fanOut; i++ ) {
      int index = nodeSpanStart + i;
      if (index >= nodeMaxIndex) break;
      updateNodeBounds(nodeIndex, nodeMinX[index], nodeMinY[index], nodeMaxX[index], nodeMaxY[index]);
    } 
  }

  private void computeLeafNodes(int layerSize) {
    for (int i = 0; i < layerSize; i++) {
      computeLeafNodeBounds(i, fanOut * i);
    }
  }

  private void computeLeafNodeBounds(int nodeIndex, int itemSpanStart) {
    for (int i = 0; i <= fanOut; i++ ) {
      int itemIndex = itemSpanStart + i;
      if (itemIndex >= items.size()) break;
      Envelope env = items.get(itemIndex).getEnvelope();
      updateNodeBounds(nodeIndex, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
    }
  }

  private void updateNodeBounds(int i, double minX, double minY, double maxX, double maxY) {
    if (minX < nodeMinX[i]) nodeMinX[i] = minX;
    if (minY < nodeMinY[i]) nodeMinY[i] = minY;
    if (maxX > nodeMaxX[i]) nodeMaxX[i] = maxX;
    if (maxY > nodeMaxY[i]) nodeMaxY[i] = maxY;
  }

  private boolean intersectsBounds(int i, Envelope env) {
    if (env.getMaxX() < nodeMinX[i]) return false;
    if (env.getMaxY() < nodeMinY[i]) return false;
    if (env.getMinX() > nodeMaxX[i]) return false;
    if (env.getMinY() > nodeMaxY[i]) return false;
    return true;
  }

  private Envelope getNodeEnvelope(int i) {
    return new Envelope(nodeMinX[i], nodeMaxX[i], nodeMinY[i], nodeMaxY[i]);
  }
  
  private static int[] computeLayerIndices(int itemSize, int fanOut) {
    List<Integer> layerIndexList = new ArrayList<Integer>();
    int layerSize = itemSize;
    int index = 0;
    do {
      layerIndexList.add(index);
      layerSize = nextMultiple(layerSize, fanOut);
      index += layerSize;
    } while (layerSize > 1);
    return toIntArray(layerIndexList);
  }

  private static int[] toIntArray(List<Integer> list) {
    int[] array = new int[list.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = list.get(i);
    }
    return array;
  }

  private static int nextMultiple(int n, int base) {
    int mult = n / base;
    int lower = mult * base;
    if (lower == n) return mult;
    return mult + 1;
  }

  private void sortItems() {
    ItemComparator comp = new ItemComparator(new HilbertEncoder(HILBERT_LEVEL, totalExtent));
    Collections.sort(items, comp);
  }
  
  static class ItemComparator implements Comparator<Item> {

    private HilbertEncoder encoder;
    
    public ItemComparator(HilbertEncoder encoder) {
      this.encoder = encoder;
    }

    @Override
    public int compare(Item item1, Item item2) {
      int hcode1 = encoder.encode(item1.getEnvelope());
      int hcode2 = encoder.encode(item2.getEnvelope());
      return Integer.compare(hcode1, hcode2);
    }
  }

}
