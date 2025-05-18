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

package org.locationtech.jts.index.kdtree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;

/**
 * A 2D <a href='https://en.wikipedia.org/wiki/K-d_tree'>KD-Tree</a> spatial
 * index for efficient point query and retrieval.
 * <p>
 * KD-trees provide fast range searching and fast lookup for point data. The
 * tree is built dynamically by inserting points. The tree supports queries by
 * location and range, and for point equality. For querying, an internal stack
 * is used instead of recursion to avoid overflow.
 * <p>
 * This implementation supports detecting and snapping points which are closer
 * than a given distance tolerance. If the same point (up to tolerance) is
 * inserted more than once, it is snapped to the existing node. In other words,
 * if a point is inserted which lies within the tolerance of a node already in
 * the index, it is snapped to that node. When an inserted point is snapped to a
 * node then a new node is not created but the count of the existing node is
 * incremented. If more than one node in the tree is within tolerance of an
 * inserted point, the closest and then lowest node is snapped to.
 * <p>
 * The structure of a KD-Tree depends on the order of insertion of the points. A
 * tree may become unbalanced if the inserted points are coherent (e.g.
 * monotonic in one or both dimensions). A perfectly balanced tree has depth of
 * only log2(N), but an unbalanced tree may be much deeper. This has a serious
 * impact on query efficiency. One solution to this is to randomize the order of
 * points before insertion (e.g. by using <a href=
 * "https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates
 * shuffling</a>).
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
  public static Coordinate[] toCoordinates(Collection<KdNode> kdnodes) {
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
  public static Coordinate[] toCoordinates(Collection<KdNode> kdnodes, boolean includeRepeated) {
    CoordinateList coord = new CoordinateList();
    for (Iterator<KdNode> it = kdnodes.iterator(); it.hasNext();) {
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
  private final double tolerance;
  private final double toleranceSq;

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
    this.toleranceSq = tolerance*tolerance;
  }

  /**
   * Gets the root node of this tree.
   * 
   * @return the root node of the tree
   */
  public KdNode getRoot() {
    return root;
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
      root = new KdNode(p, data, true);
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
   * Finds the nearest node in the tree to the given query point.
   * 
   * @param query the query point
   * @return the nearest node, or null if the tree is empty
   */
	public KdNode nearestNeighbor(final Coordinate query) {
		if (root == null)
			return null;

		KdNode bestNode = null;
		double bestDistSq = Double.POSITIVE_INFINITY;

		Deque<KdNode> stack = new ArrayDeque<>();
		stack.push(root);

		while (!stack.isEmpty()) {
			KdNode node = stack.pop();
			if (node == null)
				continue;

			// 1. visit this node
			double dSq = query.distanceSq(node.getCoordinate());
			if (dSq < bestDistSq) {
				bestDistSq = dSq;
				bestNode = node;
				if (dSq == 0)
					break; // perfect hit
			}

			// 2. decide which child to explore first
			boolean axisIsX = node.isAxisX();
			double diff = axisIsX ? query.x - node.getCoordinate().x : query.y - node.getCoordinate().y;

			KdNode nearChild = (diff < 0) ? node.getLeft() : node.getRight();
			KdNode farChild = (diff < 0) ? node.getRight() : node.getLeft();

			// 3. depth-first: push far side only if it can still win
			if (farChild != null && diff * diff < bestDistSq) {
				stack.push(farChild);
			}
			if (nearChild != null)
				stack.push(nearChild);
		}
		return bestNode;
	}

	/**
	 * Finds the nearest N nodes in the tree to the given query point.
	 * 
	 * @param query the query point
	 * @param n     the number of nearest nodes to find
	 * @return a list of the nearest nodes, sorted by distance (closest first), or
	 *         an empty list if the tree is empty.
	 */
	public List<KdNode> nearestNeighbors(final Coordinate query, final int k) {
		if (root == null || k <= 0) {
			return Collections.emptyList();
		}

		final PriorityQueue<Neighbor> heap = new PriorityQueue<>(k);
		double worstDistSq = Double.POSITIVE_INFINITY; // updated when heap full

		// depth-first search with an explicit stack
		final Deque<NNStackFrame> stack = new ArrayDeque<>();
		KdNode node = root; // the subtree we are about to visit

		while (node != null || !stack.isEmpty()) {

			// a) descend
			if (node != null) {

				// visit the current node
				double distSq = query.distanceSq(node.getCoordinate());

				if (heap.size() < k) { // not full yet
					heap.offer(new Neighbor(node, distSq));
					if (heap.size() == k)
						worstDistSq = heap.peek().distSq;
				} else if (distSq < worstDistSq) { // better than worst
					heap.poll(); // discard worst
					heap.offer(new Neighbor(node, distSq));
					worstDistSq = heap.peek().distSq; // new worst
				}

				// choose near / far child
				boolean axisIsX = node.isAxisX();
				double split = axisIsX ? node.getCoordinate().x : node.getCoordinate().y;
				double diff = axisIsX ? query.x - split : query.y - split;

				KdNode nearChild = (diff < 0) ? node.getLeft() : node.getRight();
				KdNode farChild = (diff < 0) ? node.getRight() : node.getLeft();

				// push the far branch (if it exists) together with split info
				if (farChild != null) {
					stack.push(new NNStackFrame(farChild, axisIsX, split));
				}

				// tail-recurse into the near branch
				node = nearChild;
			}

			// b) backtrack
			else { // stack not empty
				NNStackFrame sf = stack.pop();

				double diff = sf.parentSplitAxis ? query.x - sf.parentSplitValue : query.y - sf.parentSplitValue;
				double diffSq = diff * diff;

				if (heap.size() < k || diffSq < worstDistSq) {
					node = sf.node; // explore that side
				} else {
					node = null; // prune whole subtree
				}
			}
		}

		List<KdNode> result = new ArrayList<>(heap.size());
		while (!heap.isEmpty())
			result.add(heap.poll().node); // worst -> best
		Collections.reverse(result); // best -> worst
		return result;
	}

	/**
	 * Internal helper used by nearest-neighbour search.
	 */
	private static final class Neighbor implements Comparable<Neighbor> {
		final KdNode node;
		final double distSq; // pre-computed once

		Neighbor(KdNode node, double distSq) {
			this.node = node;
			this.distSq = distSq;
		}

		// “Reverse” ordering -> max-heap (peek == farthest of the N kept so far).
		@Override
		public int compareTo(Neighbor o) {
			return Double.compare(o.distSq, this.distSq);
		}
	}

	/**
	 * One entry of the explicit depth-first-search stack used by the query
	 * algorithm.
	 */
	private static class NNStackFrame {
		KdNode node;
		boolean parentSplitAxis;
		double parentSplitValue;

		NNStackFrame(KdNode node, boolean parentSplitAxis, double parentSplitValue) {
			this.node = node;
			this.parentSplitAxis = parentSplitAxis;
			this.parentSplitValue = parentSplitValue;
		}
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
   * @return the best matching node. null if no match was found.
   */
  public KdNode findBestMatchNode(Coordinate p) {
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
		// 1. empty tree: create root (splits on X by convention)
		if (root == null) {
			numberOfNodes = 1;
			return root = new KdNode(p, data, true);
		}

		// 2. walk down until we hit a null child
		KdNode parent = null;
		KdNode curr = root;
		boolean goLeft = true; // will stay tied to ‘parent’ once we exit loop

		while (curr != null) {

			final double distSq = p.distanceSq(curr.getCoordinate());
			if (distSq <= toleranceSq) { // duplicate (within tol)
				curr.increment();
				return curr;
			}

			parent = curr;
			if (curr.isAxisX()) { // node splits on X
				goLeft = p.x < curr.getCoordinate().x;
			} else { // node splits on Y
				goLeft = p.y < curr.getCoordinate().y;
			}
			curr = goLeft ? curr.getLeft() : curr.getRight();
		}

		// 3. Insert new leaf (child axis is the opposite one)
		final boolean childAxisIsX = !parent.isAxisX();
		KdNode leaf = new KdNode(p, data, childAxisIsX);
		if (goLeft)
			parent.setLeft(leaf);
		else
			parent.setRight(leaf);

		++numberOfNodes;
		return leaf;
	}

	/**
	 * Performs a range search of the points in the index and visits all nodes
	 * found.
	 * 
	 * @param queryEnv the range rectangle to query
	 * @param visitor  a visitor to visit all nodes found by the search
	 */
	public void query(final Envelope queryEnv, final KdNodeVisitor visitor) {
		if (root == null)
			return;

		final double minX = queryEnv.getMinX();
		final double maxX = queryEnv.getMaxX();
		final double minY = queryEnv.getMinY();
		final double maxY = queryEnv.getMaxY();

		// dfs with stack
		final Deque<KdNode> stack = new ArrayDeque<>();
		stack.push(root);

		while (!stack.isEmpty()) {
			KdNode node = stack.pop();
			if (node == null)
				continue;

			Coordinate pt = node.getCoordinate();
			double x = pt.x;
			double y = pt.y;

			if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
				visitor.visit(node);
			}

			boolean axisIsX = node.isAxisX();

			if (axisIsX) { // node splits on X
				if (minX <= x && node.getLeft() != null)
					stack.push(node.getLeft());
				if (maxX >= x && node.getRight() != null)
					stack.push(node.getRight());
			} else { // node splits on Y
				if (minY <= y && node.getLeft() != null)
					stack.push(node.getLeft());
				if (maxY >= y && node.getRight() != null)
					stack.push(node.getRight());
			}
		}
	}

  /**
   * Performs a range search of the points in the index.
   * 
   * @param queryEnv the range rectangle to query
   * @return a list of the KdNodes found
   */
  public List<KdNode> query(Envelope queryEnv) {
    final List<KdNode> result = new ArrayList<KdNode>();
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
  public void query(Envelope queryEnv, final List<KdNode> result) {
    query(queryEnv, new KdNodeVisitor() {

      public void visit(KdNode node) {
        result.add(node);
      }
      
    });
  }

  /**
   * Searches for a given point in the index and returns its node if found.
   * 
   * @param queryPt the point to query
   * @return the point node, if it is found in the index, or null if not
   */
  public KdNode query(Coordinate queryPt) {
    KdNode currentNode = root;
    boolean isXLevel = true;
    
    while (currentNode != null) {
      if ( currentNode.getCoordinate().equals2D(queryPt) )
        return currentNode;

      boolean searchLeft = currentNode.isPointOnLeft(isXLevel, queryPt);
      if ( searchLeft ) {
        currentNode = currentNode.getLeft();
      } else {
        currentNode = currentNode.getRight();
      }
      isXLevel = ! isXLevel;
    }
    //-- point not found
    return null;           
  }
  
	/**
	 * Performs an in-order traversal of the tree, collecting and returning all
	 * nodes that have been inserted.
	 * 
	 * @return A list containing all nodes in the KdTree. Returns an empty list if
	 *         the tree is empty.
	 */
	public List<KdNode> getNodes() {
		List<KdNode> nodeList = new ArrayList<>();
		if (root == null) {
			return nodeList; // empty list for empty tree
		}

		Deque<KdNode> stack = new ArrayDeque<>();
		KdNode currentNode = root;

		while (currentNode != null || !stack.isEmpty()) {
			if (currentNode != null) {
				stack.push(currentNode);
				currentNode = currentNode.getLeft();
			} else {
				currentNode = stack.pop();
				nodeList.add(currentNode);
				currentNode = currentNode.getRight();
			}
		}
		return nodeList;
	}

  /**
   * Computes the depth of the tree.
   * 
   * @return the depth of the tree
   */
  public int depth() {
    return depthNode(root);
  }
  
  private int depthNode(KdNode currentNode) {
    if (currentNode == null)
      return 0;

    int dL = depthNode(currentNode.getLeft());
    int dR = depthNode(currentNode.getRight());
    return 1 + (dL > dR ? dL : dR);
  }
  
  /**
   * Computes the size (number of items) in the tree.
   * 
   * @return the size of the tree
   */
  public int size() {
    return sizeNode(root);
  }
  
  private int sizeNode(KdNode currentNode) {
    if (currentNode == null)
      return 0;

    int sizeL = sizeNode(currentNode.getLeft());
    int sizeR = sizeNode(currentNode.getRight());
    return 1 + sizeL + sizeR;
  }
  
}
