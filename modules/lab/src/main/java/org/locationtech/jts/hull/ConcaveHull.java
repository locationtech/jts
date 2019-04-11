/*
 * Copyright (c) 2019 Felix Obermaier.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.hull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.index.strtree.Boundable;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.index.strtree.AbstractNode;
import org.locationtech.jts.index.strtree.ItemBoundable;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.PriorityQueue;

/**
 * A very fast 2D concave hull algorithm. It generates a general outline of a point set.
 * <p>
 * This is inspired by <a href="https://github.com/mapbox/concaveman">mapbox's concaveman algorithm</a>
 * which is released under the ISC license.
 * </p>
 * <b>Algorithm</b>
 * <p>
 * The algorithm is based on ideas from the paper <a href="http://www.iis.sinica.edu.tw/page/jise/2012/201205_10.pdf">
 * A New Concave Hull Algorithm and Concaveness Measure for n-dimensional Datasets, 2012</a> by Jin-Seo Park and
 * Se-Jong Oh.
 * </p>
 * <p>
 * This implementation dramatically improves performance over the one stated in the paper (O(rn), where r is a
 * number of output points, to O(n log n)) by introducing a fast k nearest points to a segment algorithm, a
 * modification of a depth-first kNN R-tree search using a priority queue.
 * </p>
 *
 * @since 1.17
 * @author Felix Obermaier
 */
@SuppressWarnings({"ForLoopReplaceableByForEach", "ManualArrayToCollectionCopy"})
public class ConcaveHull {
  
  private final GeometryFactory factory;
  private final LineIntersector lineIntersector = new RobustLineIntersector();

  private final Coordinate[] coordinates;
  private final double concavity;
  private final double lengthThreshold;

  /**
   * Computes a concave hull for the given geometry
   *
   * @param geom            the input geometry
   */
  public static Geometry compute(Geometry geom) {
    return compute(geom, 0);
  }

  /**
   * Computes a concave hull for the given geometry
   *
   * @param geom            the input geometry
   * @param lengthThreshold when a segment goes below this length threshold, it won't be drilled down further
   */
  public static Geometry compute(Geometry geom, double lengthThreshold) {
    return compute(geom, lengthThreshold, 2);
  }

  /**
   * Computes a concave hull for the given geometry
   *
   * @param geom            the input geometry
   * @param lengthThreshold when a segment goes below this length threshold, it won't be drilled down further
   * @param concavity       a relative measure of concavity; higher value means simpler hull
   */
  public static Geometry compute(Geometry geom, double lengthThreshold, double concavity)
  {
    ConcaveHull ch = new ConcaveHull(geom.getFactory(), geom.getCoordinates(), concavity, lengthThreshold);
    return ch.getConcaveHull();
  }

  /**
   * Creates an instance of this class
   *
   * @param factory         the factory to use when creating the result geometry
   * @param coordinates     the input coordinates
   * @param concavity       a relative measure of concavity; higher value means simpler hull
   * @param lengthThreshold when a segment goes below this length threshold, it won't be drilled down further
   */
  private ConcaveHull(GeometryFactory factory, Coordinate[] coordinates, double concavity, double lengthThreshold) {

    this.factory = factory;
    this.coordinates = coordinates;

    concavity = Math.max(0, concavity);
    this.concavity = concavity;
    this.lengthThreshold = lengthThreshold;
  }

  /**
   * Computes the concave hull
   *
   * @return A concave hull
   */
  private Geometry getConcaveHull() {

    // create start solution
    Geometry convexHull = new ConvexHull(this.coordinates, this.factory).getConvexHull();

    // shortcut based on input or start solution.
    if (convexHull.isEmpty() ||
        convexHull.getDimension() < 2) return convexHull;

    // get the coordinates that are part of the start solution
    Coordinate[] convexHullCoordinates = convexHull.getCoordinates();

    // create search index for non-hull points
    STRtree tree = createCandidatesTree(this.coordinates, convexHullCoordinates);

    // create a double linked list off of the convex hull start solution.
    List<Node> queue = createHullRing(convexHullCoordinates);
    Node first = queue.get(0);

    // create an index for the the segments of the (result) concave hull ring
    SpatialIndex segTree = createSegmentTree(queue);

    // dig the concave holes
    digConcaveHoles(tree, segTree, queue);

    // create the result geometry
    CoordinateSequence arr = toConcaveRingSequence(first);
    return factory.createPolygon(arr);
  }

  /**
   * Builds an array of coordinates off of the double linked list started at {@code first}
   *
   * @param first the first node of the convex hull ring.
   * @return a coordinate array.
   */
  private CoordinateSequence toConcaveRingSequence(Node first) {
    Node node = first;
    ArrayList<Coordinate> concave = new ArrayList<>();
    do {
      concave.add(node.point);
      node = node.nextNode;
    } while(node != first);

    // close coordinate array
    concave.add(first.point);

    Coordinate[] arr = new Coordinate[concave.size()];
    return this.factory.getCoordinateSequenceFactory().create(concave.toArray(arr));
  }

  /**
   * Iterates over all segments in {@code nodesQueue} and attempts to dig holes for each
   *
   * @param candidatesTree an index containing all input points that are not part of the concave hull
   * @param segmentIndex   an index containing all segments of the concave hull
   * @param nodesQueue     a list of nodes that need to be investigated
   */
  private void digConcaveHoles(STRtree candidatesTree, SpatialIndex segmentIndex, List<Node> nodesQueue) {

    // compute thresholds for concavity and segment length
    double squaredConcavity = this.concavity * this.concavity;
    double squaredLengthThreshold = this.lengthThreshold * this.lengthThreshold;

    // Examine all hull nodes and their segments
    while(nodesQueue.size() > 0) {
      Node node = nodesQueue.remove(0);
      Coordinate a = node.point;
      Coordinate b = node.nextNode.point;

      // skip the segment if it is already short enough
      double lengthMeasure = SquaredDistance.pointToPoint(a, b);
      if (lengthMeasure < squaredLengthThreshold) continue;

      double maxLengthMeasure = lengthMeasure / squaredConcavity;
      Coordinate p = findCandidate(candidatesTree,
        node.prevNode.point, a, b, node.nextNode.nextNode.point,
        maxLengthMeasure, segmentIndex);

      // if we found a connection and it satisfies our concavity measure
      if (p != null) {
        double distanceMeasure = Math.min(SquaredDistance.pointToPoint(p, a), SquaredDistance.pointToPoint(p, b));
        if (distanceMeasure <= maxLengthMeasure) {
          // connect the edge endpoints through this point and add 2 new edges to the queue
          nodesQueue.add(node);
          nodesQueue.add(createNode(p, node));

          // update candidate and segment indexes
          candidatesTree.remove(new Envelope(p), p);
          segmentIndex.remove(node.envelope, node);

          updateNodeEnvelope(node);
          segmentIndex.insert(node.envelope, node);
          updateNodeEnvelope(node.nextNode);
          segmentIndex.insert(node.nextNode.envelope, node.nextNode);
        }
      }
    }
  }

  /**
   * Create a list of double-linked nodes off of the given coordinates.
   * It is assumed that the <b>input is closed</b>.
   *
   * @param coordinates A sequence of coordinates
   * @return A list of segment nodes
   */
  private static List<Node> createHullRing(Coordinate[] coordinates) {
    List<Node> res = new ArrayList<>(coordinates.length-1);
    Node last = null, first = null;
    for (int i = 0; i < coordinates.length - 1; i++) {
      last = createNode(coordinates[i], last);
      if (first == null) first = last;
      res.add(last);
    }

    // close linked list
    //noinspection ConstantConditions
    last.nextNode = first;
    //noinspection ConstantConditions
    first.prevNode = last;

    return res;
  }

  /**
   * Utility function to create the initial index of points that are not part of the start solution convex hull
   *
   * @param coordinates       am array of coordinates
   * @param convexHullPoints  an array of the convex hull coordinates that make up the start solution
   * @return A STRtree index of points
   */
  private static STRtree createCandidatesTree(Coordinate[] coordinates, Coordinate[] convexHullPoints) {
    Set<Coordinate> convexHullPointSet = getConvexHullPointSet(convexHullPoints);
    STRtree tree = new STRtree();
    for (int i = 0; i < coordinates.length; i++) {
      if (!convexHullPointSet.contains(coordinates[i]))
        tree.insert(new Envelope(coordinates[i]), coordinates[i]);
    }
    return tree;
  }

  /**
   * Utility function to create the initial segment index.
   *
   * @param queue A list of segment nodes
   * @return a quadtree index of segment nodes
   */
  private static SpatialIndex createSegmentTree(List<Node> queue) {
    Quadtree res = new Quadtree();
    for (int i = 0; i < queue.size(); i++) {
      Node item = queue.get(i);
      updateNodeEnvelope(item);
      res.insert(item.envelope, item);
    }
    return res;
  }


  /**
   * Utility function to create a {@link Set<Coordinate>} off of the convex hull points
   * @param convexHullPoints the coordinates forming the convex hull.
   * @return a {@link Set<Coordinate>}
   */
  private static Set<Coordinate> getConvexHullPointSet(Coordinate[] convexHullPoints) {
    Set<Coordinate> res = new HashSet<>();
    for (int i = 0; i < convexHullPoints.length - 1; i++) {
      res.add(convexHullPoints[i]);
    }
    return res;
  }

  /**
   * Attempts to find a candidate ({@code p}). A candidate must fulfill the following constraints:
   * <ul>
   *   <li>Its distance to the segment {@code b}->{@code c} must be {@code <= maxDistanceMeasure}</li>
   *   <li>Its distance to the segment {@code b}->{@code c} must be {@code < } than to the segments {@code a}->{@code b}
   *   or {@code c}->{@code d}</li>
   *   <li>The resulting new edges ({@code b}->{@code p} and {@code p}->{@code c}) must neither
   *   properly intersect with {@code a}->{@code b} nor {@code c}{@code d}</li>
   * </ul>
   *
   * @param candidatesTree a spatial index of candidate points
   * @param a the starting point of the previous segment
   * @param b the starting point of the investigated segment
   * @param c the end-point of the investigated segment
   * @param d the end-point of the next segment
   * @param maxDistanceMeasure a threshold value for the distance
   * @param segmentTree a spatial index of hull edges
   *
   * @return a candidate
   */
  private Coordinate findCandidate(STRtree candidatesTree,
                                   Coordinate a, Coordinate b, Coordinate c, Coordinate d,
                                   double maxDistanceMeasure, SpatialIndex segmentTree) {

    PriorityQueue queue = new PriorityQueue();
    AbstractNode node = candidatesTree.getRoot();

    // search the candidate index with a depth-first search using a priority queue
    // in the order of distance to the segment b->c
    while (node != null) {
      List children = node.getChildBoundables();
      for (int i = 0; i < children.size(); i++) {
        Boundable child = (Boundable)children.get(i);

        double distancMeasure = isLeaf(child)
          ? SquaredDistance.pointToSegment((Coordinate) ((ItemBoundable)child).getItem(), b, c)
          : SquaredDistance.segmentToEnvelope(b, c, (Envelope)child.getBounds());

        // skip the node if it's too far away
        if (distancMeasure > maxDistanceMeasure) continue;

        // add node to queue
        queue.add(new PqItemDistance(child, distancMeasure));
      }

      while (queue.size() > 0 && isLeaf(((PqItemDistance)queue.peek()).item)) {
        PqItemDistance item = (PqItemDistance)queue.poll();
        Coordinate p = (Coordinate) ((ItemBoundable)item.item).getItem();

        // skip all points that are as close to adjacent segments a->b and c->d,
        // and points that would introduce self-intersections when connected
        double distancePAB = SquaredDistance.pointToSegment(p, a, b);
        double distancePCD = SquaredDistance.pointToSegment(p, c, d);
        if (item.distance < distancePAB && item.distance < distancePCD &&
          noProperIntersections(b, p, segmentTree) &&
          noProperIntersections(c, p, segmentTree)) {
            return p;
        }
      }

      PqItemDistance pqNode = (PqItemDistance) queue.poll();
      if (pqNode != null)
        node = (AbstractNode) pqNode.item;
      else
        node = null;
    }

    return null;
  }

  /**
   * Tests if the segment {@code a}-> {code b} does not have any proper intersections with
   * any other hull segments
   * @param a the starting point of the segment
   * @param b the end-point of the segment
   * @param segmentIndex a spatial index of hull segments

   * @return {@code true} if there are no proper intersections
   *
   * @see LineIntersector#hasIntersection()
   * @see LineIntersector#isProper()
   */
  private boolean noProperIntersections(Coordinate a, Coordinate b, SpatialIndex segmentIndex) {

    Envelope searchEnv = new Envelope(a);
    searchEnv.expandToInclude(b);
    List edges = segmentIndex.query(searchEnv);

    for (int i = 0; i < edges.size(); i++) {
      Node n = (Node)edges.get(i);
      lineIntersector.computeIntersection(n.point, n.nextNode.point, a, b);
      if (lineIntersector.hasIntersection() && lineIntersector.isProper()) return false;
    }
    return true;
  }

  /**
   * Predicate function to see if a node is a leaf or not.
   * @param node a node
   * @return {@code true} if the node is a leaf.
   */
  private static boolean isLeaf(Boundable node) {
    if (node instanceof ItemBoundable)
      return true;
    if (!(node instanceof AbstractNode))
      Assert.shouldNeverReachHere();
    return false;
  }

  /**
   * Updates the bounds of a node
   * @param node the node
   */
  private static void updateNodeEnvelope(Node node) {
    node.envelope.init();
    node.envelope.expandToInclude(node.point);
    node.envelope.expandToInclude(node.nextNode.point);
  }

  /**
   * Utility function to create a node for {@code c}.
   * If {@code previous} is not {@code null}, it will be linked into the chain,
   * otherwise linked to itself.
   *
   * @param c the location of the node
   * @param previous the previous node, may be {@code null}
   */
  private static Node createNode(Coordinate c, Node previous) {
    Node res = new Node(c);

    if (previous == null) {
      res.prevNode = res;
      res.nextNode = res;
    }
    else {
      res.nextNode = previous.nextNode;
      res.prevNode = previous;
      previous.nextNode.prevNode = res;
      previous.nextNode = res;
    }

    return res;
  }

  /**
   * A node in a hull ring
   */
  private static class Node {

    /** the previous node */
    Node prevNode = null;

    /** the bounds of the segment from {@linkplain #point} to {@linkplain #nextNode#point} */
    final Envelope envelope;

    /** the coordinate of this node */
    final Coordinate point;

    /** the next node in the ring */
    Node nextNode = null;

    Node(Coordinate point) {
      this.point = point;
      envelope = new Envelope();
    }
  }

  /**
   * An item
   */
  private class PqItemDistance implements Comparable<PqItemDistance>
  {
    private final Boundable item;
    private final double distance;

    PqItemDistance(Boundable item, double distance) {
      this.item = item;
      this.distance = distance;
    }

    public int compareTo(PqItemDistance other) {
      return Double.compare(this.distance, other.distance);
    }
  }
}
