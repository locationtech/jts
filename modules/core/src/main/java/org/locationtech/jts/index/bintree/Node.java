
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
package org.locationtech.jts.index.bintree;

import org.locationtech.jts.util.Assert;

/**
 * A node of a {@link Bintree}.
 *
 * @version 1.7
 */
public class Node<T>
  extends NodeBase<T>
{
  public static<T> Node<T> createNode(Interval itemInterval)
  {
    Key key = new Key(itemInterval);

//System.out.println("input: " + env + "  binaryEnv: " + key.getEnvelope());
    return new Node<>(key.getInterval(), key.getLevel());
  }

  public static <T>Node<T> createExpanded(Node<T> node, Interval addInterval)
  {
    Interval expandInt = new Interval(addInterval);
    if (node != null) expandInt.expandToInclude(node.interval);

    Node<T> largerNode = createNode(expandInt);
    if (node != null) largerNode.insert(node);
    return largerNode;
  }

  private final Interval interval;
  private final double centre;
  private final int level;

  public Node(Interval interval, int level)
  {
    this.interval = interval;
    this.level = level;
    centre = (interval.getMin() + interval.getMax()) / 2;
  }

  public Interval getInterval() { return interval; }

  protected boolean isSearchMatch(Interval itemInterval)
  {
//    System.out.println(itemInterval + " overlaps " + interval + " : "
//                       + itemInterval.overlaps(interval));
    return itemInterval.overlaps(interval);
  }

  /**
   * Returns the subnode containing the envelope.
   * Creates the node if
   * it does not already exist.
   */
  public Node<T> getNode(Interval searchInterval)
  {
    int subnodeIndex = getSubnodeIndex(searchInterval, centre);
    // if index is -1 searchEnv is not contained in a subnode
    if (subnodeIndex != -1) {
      // create the node if it does not exist
      Node<T> node = getSubnode(subnodeIndex);
      // recursively search the found/created node
      return node.getNode(searchInterval);
    }
    else {
      return this;
    }
  }

  /**
   * Returns the smallest <i>existing</i>
   * node containing the envelope.
   */
  public NodeBase<T> find(Interval searchInterval)
  {
    int subnodeIndex = getSubnodeIndex(searchInterval, centre);
    if (subnodeIndex == -1)
      return this;
    if (subnode[subnodeIndex] != null) {
      // query lies in subnode, so search it
      Node<T> node = subnode[subnodeIndex];
      return node.find(searchInterval);
    }
    // no existing subnode, so return this one anyway
    return this;
  }

  void insert(Node<T> node)
  {
    Assert.isTrue(interval == null || interval.contains(node.interval));
    int index = getSubnodeIndex(node.interval, centre);
    if (node.level == level - 1) {
      subnode[index] = node;
    }
    else {
      // the node is not a direct child, so make a new child node to contain it
      // and recursively insert the node
      Node<T> childNode = createSubnode(index);
      childNode.insert(node);
      subnode[index] = childNode;
    }
  }

  /**
   * get the subnode for the index.
   * If it doesn't exist, create it
   */
  private Node<T> getSubnode(int index)
  {
    if (subnode[index] == null) {
      subnode[index] = createSubnode(index);
    }
    return subnode[index];
  }

  private Node<T> createSubnode(int index)
  {
        // create a new subnode in the appropriate interval

      double min = 0.0;
      double max = 0.0;

      switch (index) {
      case 0:
        min = interval.getMin();
        max = centre;
        break;
      case 1:
        min = centre;
        max = interval.getMax();
        break;
      }
      Interval subInt = new Interval(min, max);
    return new Node<>(subInt, level - 1);
  }

}
