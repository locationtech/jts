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
 * A data structure to represent disjoint (partitioned) sets,
 * and allow merging them.
 * See the Wikipedia article on
 *  <a href='https://en.wikipedia.org/wiki/Disjoint-set_data_structure'>disjointiset data structures</a>.
 * 
 * @author mdavis
 *
 */
public class DisjointSets {
  private int[] parent;
  private int[] partSize;
  private int numSets;
  private int[] parts;
  private Integer[] setItem;
  private int[] setSize;
  private int[] setStart;

  public DisjointSets(int size) {
    parent = arrayIndex(size); 
    partSize = arrayValue(size, 1);
    numSets = size;
  }
  
  public boolean inInSameSet(int i, int j) {
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
    if ((partSize[rootj] > partSize[rooti]) 
        || (partSize[rooti] == partSize[rootj] && rootj <= rooti)) {
      src = rootj;
      dest = rooti;
    }

    parent[src] = parent[dest];
    partSize[dest] += partSize[src];
    partSize[src] = 0;

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
  
  private int[] arrayIndex(int size) {
    int[] arr = new int[size];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = i;
    }
    return arr;
  }

  private static int[] arrayValue(int size, int val) {
    int[] arr = new int[size];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = val;
    }
    return arr;
  }

  private void compute() {
    //--- sort set items by root and index, 
    setItem = sortItems();
    
    //--- compute start and size of each set
    setSize = new int[numSets];
    setStart = new int[numSets];
    int currRoot = findRoot(setItem[0]);
    setStart[0] = 0;
    int iSet = 0;
    for (int i = 1; i < setItem.length; i++) {
      int root = findRoot(setItem[i]);
      if (root != currRoot) {
        setSize[iSet] = i - setStart[iSet];
        iSet++;
        setStart[iSet] = i;
        currRoot = root;
      }
    }
    setSize[numSets-1] = setItem.length - setStart[numSets-1];
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
  
  public int getNumSets() {
    compute();
    return numSets;
  }
  
  public int getSetSize(int s) {
    return setSize[s];
  }

  public int getSetItem(int s, int i) {
    int index = setStart[s] + i;
    return setItem[index];
  }


  
  
}
