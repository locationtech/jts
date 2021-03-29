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
 * Set items are represented by integer indices
 * (which will typically be an index into an array
 * of the objects actually being partitioned).
 * Initially each item is in its own subset.
 * Client code can merge subsets of items as required for the 
 * algorithm being performed (e.g. set partitioning or clustering).
 * The current partitioning can be computed at any time,
 * and subset items accessed
 * using the {@link Subsets} accessor.
 * <p>
 * See the Wikipedia article on
 *  <a href='https://en.wikipedia.org/wiki/Disjoint-set_data_structure'>disjoint set data structures</a>.
 * 
 * @author Martin Davis
 *
 */
public class DisjointSets 
{
  private int[] parent;
  private int[] setSize;
  private int numSets;

  /**
   * Creates a new set containing a given number of items.
   * 
   * @param size the number of items contained in the set
   */
  public DisjointSets(int size) {
    parent = arrayOfIndex(size); 
    setSize = arrayOfValue(size, 1);
    numSets = size;
  }
  
  /**
   * Tests if two items are in the same subset.
   * 
   * @param i an item index
   * @param j another item index
   * @return true if items are in the same subset
   */
  public boolean isInSameSubset(int i, int j) {
    return findRoot(i) == findRoot(j);
  }
  
  /**
   * Merges two subsets containing the given items.
   * Note that the items do not have to be the roots of
   * their respective subsets.
   * If the items are in the same subset
   * the partitioning does not change.
   * 
   * @param i an item index
   * @param j another item index
   */
  public void merge(int i, int j) {
    int rooti = findRoot(i);
    int rootj = findRoot(j);

    // already in same subset
    if (rooti == rootj) {
        return;
    }

    // merge smaller subset into larger
    int src = rooti;
    int dest = rootj;
    if ((setSize[rootj] > setSize[rooti]) 
        || (setSize[rooti] == setSize[rootj] && rootj <= rooti)) {
      src = rootj;
      dest = rooti;
    }

    parent[src] = parent[dest];
    setSize[dest] += setSize[src];
    setSize[src] = 0;

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

  /**
   * Gets a representation of the current partitioning.
   * This creates a snapshot of the partitioning;
   * the set can be merged further after this call.
   * 
   * @return an representation of the current subset partitioning.
   */
  public Subsets subsets() {
    if (numSets == 0) {
      return new Subsets();
    }
    
    //--- sort set items by root and index, 
    Integer[] items = itemsSortedBySubset();
    
    //--- compute start and size of each set
    int[] size = new int[numSets];
    int[] start = new int[numSets];
    int currRoot = findRoot(items[0]);
    start[0] = 0;
    int iSet = 0;
    for (int i = 1; i < items.length; i++) {
      int root = findRoot(items[i]);
      if (root != currRoot) {
        size[iSet] = i - start[iSet];
        iSet++;
        start[iSet] = i;
        currRoot = root;
      }
    }
    size[numSets-1] = items.length - start[numSets-1];
    return new Subsets(items, size, start);
  }

  private Integer[] itemsSortedBySubset() {
    // can only use comparator on Integer array
    Integer[] itemsSort = arrayOfIntegerIndex(parent.length);
    // sort items by their subset root
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
  
  private static int[] arrayOfIndex(int size) {
    int[] arr = new int[size];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = i;
    }
    return arr;
  }

  private static Integer[] arrayOfIntegerIndex(int size) {
    Integer[] arr = new Integer[size];
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
  
  /**
   * A representation of a partition of a set of items into disjoint subsets.
   * It provides accessors for the number of subsets, 
   * the size of each subset, and the items of each subset.
   * <p>
   * The item indices in each subset are sorted.  
   * This means that the item ordering is stable; that is,
   * the items have the same order they did in the original set.
   */
  public class Subsets {
    private Integer[] item;
    private int[] size;
    private int[] start;
    
    Subsets() {
      this.item = null;
      this.size = new int[0];
      this.start = null;
    }
    
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
     * @param s the subset index
     * @return the size of the subset
     */
    public int getSize(int s) {
      if (s >= size.length) {
        throw new IllegalArgumentException("Subset index out of range: " + s);
      }
      return size[s];
    }
  
    /**
     * Gets an item from a subset.
     *  
     * @param s the subset index
     * @param i the index of the item in the subset
     * @return the item
     */
    public int getItem(int s, int i) {
      if (s >= size.length) {
        throw new IllegalArgumentException("Subset index out of range: " + s);
      }
      int index = start[s] + i;
      if (index >= item.length) {
        throw new IllegalArgumentException("Item index out of range: " + i);
      }
      return item[index];
    }
  }
}
