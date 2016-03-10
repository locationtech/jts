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

package org.locationtech.jts.index.kdtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;


/**
 * An implementation of a 2-D KD-Tree. KD-trees provide fast range searching on
 * point data.
 * <p>
 * This implementation supports detecting and snapping points which are closer
 * than a given distance tolerance. 
 * If the same point (up to tolerance) is inserted
 * more than once, it is snapped to the existing node.
 * In other words, if a point is inserted which lies within the tolerance of a node already in the index,
 * it is snapped to that node. 
 * When a point is snapped to a node then a new node is not created but the count of the existing node
 * is incremented.  
 * If more than one node in the tree is within tolerance of an inserted point, 
 * the closest and then lowest node is snapped to.
 * 
 * @author David Skea
 * @author Martin Davis
 */
public class KdTree {

  /**
   * Converts a collection of {@link KdNode}s to an array of {@link Coordinate}s.
   * 
   * @param kdnodes
   *          a collection of nodes
   * @return an array of the coordinates represented by the nodes
   */
  public static Coordinate[] toCoordinates(Collection kdnodes) {
    return toCoordinates(kdnodes, false);
  }

  /**
   * Converts a collection of {@link KdNode}s 
   * to an array of {@link Coordinate}s,
   * specifying whether repeated nodes should be represented
   * by multiple coordinates.
   * 
   * @param kdnodes a collection of nodes
   * @param includeRepeated true if repeated nodes should 
   *   be included multiple times
   * @return an array of the coordinates represented by the nodes
   */
  public static Coordinate[] toCoordinates(Collection kdnodes, boolean includeRepeated) {
    CoordinateList coord = new CoordinateList();
    for (Iterator it = kdnodes.iterator(); it.hasNext();) {
      KdNode node = (KdNode) it.next();
      int count = includeRepeated ? node.getCount() : 1;
      for (int i = 0; i < count; i++) {
       coord.add(node.getCoordinate(), true);
      }
    }
    return coord.toCoordinateArray();
  }

  private KdNode root = null;
  private long numberOfNodes;
  private double tolerance;

  /**
   * Creates a new instance of a KdTree with a snapping tolerance of 0.0. (I.e.
   * distinct points will <i>not</i> be snapped)
   */
  public KdTree() {
    this(0.0);
  }

  /**
   * Creates a new instance of a KdTree, specifying a snapping distance
   * tolerance. Points which lie closer than the tolerance to a point already in
   * the tree will be treated as identical to the existing point.
   * 
   * @param tolerance
   *          the tolerance distance for considering two points equal
   */
  public KdTree(double tolerance) {
    this.tolerance = tolerance;
  }

  /**
   * Tests whether the index contains any items.
   * 
   * @return true if the index does not contain any items
   */
  public boolean isEmpty() {
    if (root == null)
      return true;
    return false;
  }

  /**
   * Inserts a new point in the kd-tree, with no data.
   * 
   * @param p
   *          the point to insert
   * @return the kdnode containing the point
   */
  public KdNode insert(Coordinate p) {
    return insert(p, null);
  }

  /**
   * Inserts a new point into the kd-tree.
   * 
   * @param p
   *          the point to insert
   * @param data
   *          a data item for the point
   * @return returns a new KdNode if a new point is inserted, else an existing
   *         node is returned with its counter incremented. This can be checked
   *         by testing returnedNode.getCount() &gt; 1.
   */
  public KdNode insert(Coordinate p, Object data) {
    if (root == null) {
      root = new KdNode(p, data);
      return root;
    }
    
    /**
     * Check if the point is already in the tree, up to tolerance.
     * If tolerance is zero, this phase of the insertion can be skipped.
     */
    if ( tolerance > 0 ) {
      KdNode matchNode = findBestMatchNode(p);
      if (matchNode != null) {
        // point already in index - increment counter
        matchNode.increment();
        return matchNode;
      }
    }
    
    return insertExact(p, data);
  }
    
  /**
   * Finds the node in the tree which is the best match for a point
   * being inserted.
   * The match is made deterministic by returning the lowest of any nodes which
   * lie the same distance from the point.
   * There may be no match if the point is not within the distance tolerance of any
   * existing node.
   * 
   * @param p the point being inserted
   * @return the best matching node
   * @return null if no match was found
   */
  private KdNode findBestMatchNode(Coordinate p) {
    BestMatchVisitor visitor = new BestMatchVisitor(p, tolerance);
    query(visitor.queryEnvelope(), visitor);
    return visitor.getNode();
  }

  static private class BestMatchVisitor implements KdNodeVisitor {

    private double tolerance;
    private KdNode matchNode = null;
    private double matchDist = 0.0;
    private Coordinate p;
    
    public BestMatchVisitor(Coordinate p, double tolerance) {
      this.p = p;
      this.tolerance = tolerance;
    }
    
    public Envelope queryEnvelope() {
      Envelope queryEnv = new Envelope(p);
      queryEnv.expandBy(tolerance);
      return queryEnv;
    }

    public KdNode getNode() {
      return matchNode;
    }

    public void visit(KdNode node) {
      double dist = p.distance(node.getCoordinate());
      boolean isInTolerance =  dist <= tolerance; 
      if (! isInTolerance) return;
      boolean update = false;
      if (matchNode == null
          || dist < matchDist
          // if distances are the same, record the lesser coordinate
          || (matchNode != null && dist == matchDist 
          && node.getCoordinate().compareTo(matchNode.getCoordinate()) < 1))
        update = true;

      if (update) {
        matchNode = node;
        matchDist = dist;
      }
    }
  }
  
  /**
   * Inserts a point known to be beyond the distance tolerance of any existing node.
   * The point is inserted at the bottom of the exact splitting path, 
   * so that tree shape is deterministic.
   * 
   * @param p the point to insert
   * @param data the data for the point
   * @return the created node
   */
  private KdNode insertExact(Coordinate p, Object data) {
    KdNode currentNode = root;
    KdNode leafNode = root;
    boolean isOddLevel = true;
    boolean isLessThan = true;

    /**
     * Traverse the tree, first cutting the plane left-right (by X ordinate)
     * then top-bottom (by Y ordinate)
     */
    while (currentNode != null) {
      // test if point is already a node (not strictly necessary)
      if (currentNode != null) {
        boolean isInTolerance = p.distance(currentNode.getCoordinate()) <= tolerance;

        // check if point is already in tree (up to tolerance) and if so simply
        // return existing node
        if (isInTolerance) {
          currentNode.increment();
          return currentNode;
        }
      }

      if (isOddLevel) {
        isLessThan = p.x < currentNode.getX();
      } else {
        isLessThan = p.y < currentNode.getY();
      }
      leafNode = currentNode;
      if (isLessThan) {
        currentNode = currentNode.getLeft();
      } else {
        currentNode = currentNode.getRight();
      }

      isOddLevel = ! isOddLevel;
    }

    // no node found, add new leaf node to tree
    numberOfNodes = numberOfNodes + 1;
    KdNode node = new KdNode(p, data);
    if (isLessThan) {
      leafNode.setLeft(node);
    } else {
      leafNode.setRight(node);
    }
    return node;
  }

  private void queryNode(KdNode currentNode,
      Envelope queryEnv, boolean odd, KdNodeVisitor visitor) {
    if (currentNode == null)
      return;

    double min;
    double max;
    double discriminant;
    if (odd) {
      min = queryEnv.getMinX();
      max = queryEnv.getMaxX();
      discriminant = currentNode.getX();
    } else {
      min = queryEnv.getMinY();
      max = queryEnv.getMaxY();
      discriminant = currentNode.getY();
    }
    boolean searchLeft = min < discriminant;
    boolean searchRight = discriminant <= max;

    // search is computed via in-order traversal
    if (searchLeft) {
      queryNode(currentNode.getLeft(), queryEnv, !odd, visitor);
    }
    if (queryEnv.contains(currentNode.getCoordinate())) {
      visitor.visit(currentNode);
    }
    if (searchRight) {
      queryNode(currentNode.getRight(), queryEnv, !odd, visitor);
    }

  }

  /**
   * Performs a range search of the points in the index and visits all nodes found.
   * 
   * @param queryEnv
   *          the range rectangle to query
   * @param visitor a visitor to visit all nodes found by the search
   */
  public void query(Envelope queryEnv, KdNodeVisitor visitor) {
    queryNode(root, queryEnv, true, visitor);
  }
  
  /**
   * Performs a range search of the points in the index.
   * 
   * @param queryEnv
   *          the range rectangle to query
   * @return a list of the KdNodes found
   */
  public List query(Envelope queryEnv) {
    final List result = new ArrayList();
    query(queryEnv, result);
    return result;
  }

  /**
   * Performs a range search of the points in the index.
   * 
   * @param queryEnv
   *          the range rectangle to query
   * @param result
   *          a list to accumulate the result nodes into
   */
  public void query(Envelope queryEnv, final List result) {
    queryNode(root, queryEnv, true, new KdNodeVisitor() {

      public void visit(KdNode node) {
        result.add(node);
      }
      
    });
  }
}