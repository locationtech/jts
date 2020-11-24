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
package org.locationtech.jts.index.intervalrtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.io.WKTWriter;


/**
 * A static index on a set of 1-dimensional intervals,
 * using an R-Tree packed based on the order of the interval midpoints.
 * It supports range searching,
 * where the range is an interval of the real line (which may be a single point).
 * A common use is to index 1-dimensional intervals which 
 * are the projection of 2-D objects onto an axis of the coordinate system.
 * <p>
 * This index structure is <i>static</i> 
 * - items cannot be added or removed once the first query has been made.
 * The advantage of this characteristic is that the index performance 
 * can be optimized based on a fixed set of items.
 * 
 * @author Martin Davis
 */
public class SortedPackedIntervalRTree <T>
{
  private final List<IntervalRTreeNode<T>> leaves = new ArrayList<>();
  
  /**
   * If root is null that indicates
   * that the tree has not yet been built,   
   * OR nothing has been added to the tree.
   * In both cases, the tree is still open for insertions.
   */
	private IntervalRTreeNode<T> root = null;
	
	public SortedPackedIntervalRTree()
	{
		
	}
	
  /**
   * Adds an item to the index which is associated with the given interval
   * 
   * @param min the lower bound of the item interval
   * @param max the upper bound of the item interval
   * @param item the item to insert
   * 
   * @throws IllegalStateException if the index has already been queried
   */
	public void insert(double min, double max, T item)
	{
    if (root != null)
      throw new IllegalStateException("Index cannot be added to once it has been queried");
    leaves.add(new IntervalRTreeLeafNode<>(min, max, item));
	}
	
  private void init()
  {
    // already built
    if (root != null) return;
    
    /**
     * if leaves is empty then nothing has been inserted.
     * In this case it is safe to leave the tree in an open state
     */
    if (leaves.size() == 0) return;
    
    buildRoot();
  }
  
  private synchronized void buildRoot() 
  {
    if (root != null) return;
    root = buildTree();
  }
  
	private  IntervalRTreeNode<T> buildTree()
	{
	  
    // sort the leaf nodes
    leaves.sort(new IntervalRTreeNode.NodeComparator());
    
    // now group nodes into blocks of two and build tree up recursively
		List<IntervalRTreeNode<T>> src = leaves;
		List<IntervalRTreeNode<T>> temp = null;
		List<IntervalRTreeNode<T>> dest = new ArrayList<>();
		
		while (true) {
			buildLevel(src, dest);
			if (dest.size() == 1)
				return dest.get(0);
      
			temp = src;
			src = dest;
			dest = temp;
		}
	}
	
  private int level = 0;
  
	private void buildLevel(List<IntervalRTreeNode<T>> src, List<IntervalRTreeNode<T>> dest)
  {
    level++;
		dest.clear();
		for (int i = 0; i < src.size(); i += 2) {
			IntervalRTreeNode<T> n1 = src.get(i);
			IntervalRTreeNode<T> n2 = (i + 1 < src.size())
						? src.get(i) : null;
			if (n2 == null) {
				dest.add(n1);
			} else {
				IntervalRTreeNode<T> node = new IntervalRTreeBranchNode<T>(
						src.get(i),
						src.get(i + 1));
//        printNode(node);
//				System.out.println(node);
				dest.add(node);
			}
		}
	}
	
  private void printNode(IntervalRTreeNode<T> node)
  {
    System.out.println(WKTWriter.toLineString(new Coordinate(node.min, level), new Coordinate(node.max, level)));
  }
  
  /**
   * Search for intervals in the index which intersect the given closed interval
   * and apply the visitor to them.
   * 
   * @param min the lower bound of the query interval
   * @param max the upper bound of the query interval
   * @param visitor the visitor to pass any matched items to
   */
	public void query(double min, double max, ItemVisitor<T> visitor)
	{
    init();
    
    // if root is null tree must be empty
    if (root == null) 
      return;
    
		root.query(min, max, visitor);
	}
  
}
