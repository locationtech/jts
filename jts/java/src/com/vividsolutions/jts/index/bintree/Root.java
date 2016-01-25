
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jts.index.bintree;

import com.vividsolutions.jts.index.quadtree.IntervalSize;
import com.vividsolutions.jts.util.Assert;

/**
 * The root node of a single {@link Bintree}.
 * It is centred at the origin,
 * and does not have a defined extent.
 *
 * @version 1.7
 */
public class Root
  extends NodeBase
{

  // the singleton root node is centred at the origin.
  private static final double origin = 0.0;

  public Root()
  {
  }

  /**
   * Insert an item into the tree this is the root of.
   */
  public void insert(Interval itemInterval, Object item)
  {
    int index = getSubnodeIndex(itemInterval, origin);
    // if index is -1, itemEnv must contain the origin.
    if (index == -1) {
      add(item);
      return;
    }
    /**
     * the item must be contained in one interval, so insert it into the
     * tree for that interval (which may not yet exist)
     */
    Node node = subnode[index];
    /**
     *  If the subnode doesn't exist or this item is not contained in it,
     *  have to expand the tree upward to contain the item.
     */

    if (node == null || ! node.getInterval().contains(itemInterval)) {
       Node largerNode = Node.createExpanded(node, itemInterval);
       subnode[index] = largerNode;
    }
    /**
     * At this point we have a subnode which exists and must contain
     * contains the env for the item.  Insert the item into the tree.
     */
    insertContained(subnode[index], itemInterval, item);
//System.out.println("depth = " + root.depth() + " size = " + root.size());
  }

  /**
   * insert an item which is known to be contained in the tree rooted at
   * the given Node.  Lower levels of the tree will be created
   * if necessary to hold the item.
   */
  private void insertContained(Node tree, Interval itemInterval, Object item)
  {
    Assert.isTrue(tree.getInterval().contains(itemInterval));
   /**
    * Do NOT create a new node for zero-area intervals - this would lead
    * to infinite recursion. Instead, use a heuristic of simply returning
    * the smallest existing node containing the query
    */
    boolean isZeroArea = IntervalSize.isZeroWidth(itemInterval.getMin(), itemInterval.getMax());
    NodeBase node;
    if (isZeroArea)
      node = tree.find(itemInterval);
    else
      node = tree.getNode(itemInterval);
    node.add(item);
  }

  /**
   * The root node matches all searches
   */
  protected boolean isSearchMatch(Interval interval)
  {
    return true;
  }


}
