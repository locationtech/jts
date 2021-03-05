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
package org.locationtech.jts.operation.union;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A data structure that represents a partition of a set
 * into disjoint subsets, and allows merging subsets.
 * Set items are represented by integer indices.
 * Initially each item is in its own subset.
 * Client code can merge subsets of items as required for the 
 * algorithm being performed (e.g. set partitioning or clustering).
 * The current partitioning can be computed at any time,
 * and subset items accessed by their indices.
 * 
 * See the Wikipedia article on
 *  <a href='https://en.wikipedia.org/wiki/Disjoint-set_data_structure'>disjointiset data structures</a>.
 * 
 * @author mdavis
 *
 */
public class DisjointSets {
  private int[] parent;
  private int[] partitionSize;
  private int numSets;


  /**
   * Creates a new structure containing a given number of items.
   * 
   * @param size the number of items contained in the set
   */
  public DisjointSets(int size) {
    parent = arrayOfIndex(size); 
    partitionSize = arrayOfValue(size, 1);
    numSets = size;
  }
  
  public boolean isSameSubset(int i, int j) {
    return findRoot(i) == findRoot(j);
  }
  
  public void merge(int i, int j) {
    int rooti = findRoot(i);
    int rootj = findRoot(j);

    // already in same cluster
    if (rooti == rootj) {
        return;
    }

    // merge smaller cluster into larger
    int src = rooti;
    int dest = rootj;
    if ((partitionSize[rootj] > partitionSize[rooti]) 
        || (partitionSize[rooti] == partitionSize[rootj] && rootj <= rooti)) {
      src = rootj;
      dest = rooti;
    }

    parent[src] = parent[dest];
    partitionSize[dest] += partitionSize[src];
    partitionSize[src] = 0;

    numSets--;
  }

  private int findRoot(int i) {
    
    // find set root
    int root = i;
    while(parent[root] != root) {
      // do path compression by halving
      parent[root] = parent[parent[root]];    
      root = parent[root];
    }
    return root;
  }
  
  private static int[] arrayOfIndex(int size) {
    int[] arr = new int[size];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = i;
    }
    return arr;
  }

  private static int[] arrayOfValue(int size, int val) {
    int[] arr = new int[size];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = val;
    }
    return arr;
  }

  public Subsets subsets() {
    //--- sort set items by root and index, 
    Integer[] item = sortItems();
    
    //--- compute start and size of each set
    int[] size = new int[numSets];
    int[] start = new int[numSets];
    int currRoot = findRoot(item[0]);
    start[0] = 0;
    int iSet = 0;
    for (int i = 1; i < item.length; i++) {
      int root = findRoot(item[i]);
      if (root != currRoot) {
        size[iSet] = i - start[iSet];
        iSet++;
        start[iSet] = i;
        currRoot = root;
      }
    }
    size[numSets-1] = item.length - start[numSets-1];
    return new Subsets(item, size, start);
  }

  private Integer[] sortItems() {
    // can only use comparator on Integer array
    Integer[] itemsSort = new Integer[parent.length];
    for (int i = 0; i < itemsSort.length; i++) {
      itemsSort[i] = i;
    }
    Arrays.sort(itemsSort, new Comparator<Integer>() {
      @Override
      public int compare(Integer i1, Integer i2) {
        int root1 = findRoot(i1);
        int root2 = findRoot(i2);
        if (root1 < root2) return -1;
        if (root1 > root2) return 1;
        // in same set - sort by value
        return Integer.compare(i1,  i2);
      }
    });
    return itemsSort;
  }
  
  /**
   * Provides accessors for items in disjoint subsets.
   * 
   * @author mdavis
   *
   */
  public class Subsets {
    private Integer[] item;
    private int[] size;
    private int[] start;
    
    Subsets(Integer[] item, int[] size, int[] start) {
      this.item = item;
      this.size = size;
      this.start = start;
    }
    
    /**
     * Gets the number of disjoint subsets.
     * 
     * @return the number of subsets
     */
    public int getCount() {
      return size.length;
    }
    
    /**
     * Gets the number of items in a given subset.
     * 
     * @param s the number of the subset
     * @return the size of the subset
     */
    public int getSize(int s) {
      return size[s];
    }
  
    /**
     * Gets an item from a subset.
     *  
     * @param s the subset number
     * @param i the index of the item in the subset
     * @return the item
     */
    public int getItem(int s, int i) {
      int index = start[s] + i;
      return item[index];
    }
  }
}
