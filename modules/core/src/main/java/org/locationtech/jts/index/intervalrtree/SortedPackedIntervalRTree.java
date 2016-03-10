/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
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
public class SortedPackedIntervalRTree 
{
  private List leaves = new ArrayList();
	private IntervalRTreeNode root = null;
	
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
	public void insert(double min, double max, Object item)
	{
    if (root != null)
      throw new IllegalStateException("Index cannot be added to once it has been queried");
    leaves.add(new IntervalRTreeLeafNode(min, max, item));
	}
	
  private void init()
  {
    if (root != null) return;
    buildRoot();
  }
  
  private synchronized void buildRoot() 
  {
    if (root != null) return;
    root = buildTree();
  }
  
	private  IntervalRTreeNode buildTree()
	{
	  
    // sort the leaf nodes
    Collections.sort(leaves, new IntervalRTreeNode.NodeComparator());
    
    // now group nodes into blocks of two and build tree up recursively
		List src = leaves;
		List temp = null;
		List dest = new ArrayList();
		
		while (true) {
			buildLevel(src, dest);
			if (dest.size() == 1)
				return (IntervalRTreeNode) dest.get(0);
      
			temp = src;
			src = dest;
			dest = temp;
		}
	}
	
  private int level = 0;
  
	private void buildLevel(List src, List dest) 
  {
    level++;
		dest.clear();
		for (int i = 0; i < src.size(); i += 2) {
			IntervalRTreeNode n1 = (IntervalRTreeNode) src.get(i);
			IntervalRTreeNode n2 = (i + 1 < src.size()) 
						? (IntervalRTreeNode) src.get(i) : null;
			if (n2 == null) {
				dest.add(n1);
			} else {
				IntervalRTreeNode node = new IntervalRTreeBranchNode(
						(IntervalRTreeNode) src.get(i),
						(IntervalRTreeNode) src.get(i + 1));
//        printNode(node);
//				System.out.println(node);
				dest.add(node);
			}
		}
	}
	
  private void printNode(IntervalRTreeNode node)
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
	public void query(double min, double max, ItemVisitor visitor)
	{
    init();

		root.query(min, max, visitor);
	}
  
}
