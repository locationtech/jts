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
package org.locationtech.jts.hull;

import java.util.*;
import java.util.function.Predicate;

import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.index.strtree.*;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.PriorityQueue;
import org.locationtech.jtslab.clean.HoleRemover;

/**
 * A very fast 2D concave hull algorithm. It generates a general outline of a point set.
 * <p>
 * This is a direct port of <a href="https://github.com/mapbox/concaveman">mapbox's concaveman algorithm</a>
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
 * @version 1.17
 * @author Vladimir Agafonkin, Felix Obermaier
 */
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
    SpatialIndex tree = createCandidatesIndex(this.coordinates, convexHullCoordinates);

    // create a double linked list off of the convex hull start solution.
    List<Node> queue = createHullRing(convexHullCoordinates);
    Node first = queue.get(0);

    // create an index for the the segments of the (result) concave hull ring
    SpatialIndex segTree = createSegmentIndex(queue);

    // dig the concave holes
    digConcaveHoles(tree, segTree, queue);

    // create the result geometry
    Coordinate[] arr = toConcaveRingArray(first);
    return factory.createPolygon(arr);
  }

  /**
   * Builds an array of coordinates off of the double linked list started at {@param node}
   *
   * @param first the first node of the convex hull ring.
   * @return a coordinate array.
   */
  private static Coordinate[] toConcaveRingArray(Node first) {
    Node node = first;
    ArrayList<Coordinate> concave = new ArrayList<>();
    do {
      concave.add(node.Point);
      node = node.Next;
    } while(node != first);

    // close coordinate array
    concave.add(first.Point);

    Coordinate[] arr = new Coordinate[concave.size()];
    return concave.toArray(arr);
  }

  /**
   * Iterates over all segments
   *
   * @param candidateIndex an index containing all input points that are not part of the concave hull
   * @param segmentIndex   an index containing all segments of the concave hull
   * @param nodesQueue     a list of nodes that need to be investigated
   */
  private void digConcaveHoles(SpatialIndex candidateIndex, SpatialIndex segmentIndex, List<Node> nodesQueue) {

    while(nodesQueue.size() > 0) {
      Node node = nodesQueue.remove(0);
      Coordinate a = node.Point;
      Coordinate b = node.Next.Point;

      // skip the edge if it is already short enough
      double length = a.distance(b);
      if (length < this.lengthThreshold) continue;

      double maxLength = length / this.concavity;
      Coordinate p = findCandidate((STRtree) candidateIndex,
        node.Prev.Point, a, b, node.Next.Next.Point,
        maxLength, segmentIndex);

      // if we found a connection and it satisfies our concavity measure
      if (p != null && Math.min(p.distance(a), p.distance(b)) <= maxLength) {
        // connect the edge endpoints through this point and add 2 new edges to the queue
        nodesQueue.add(node);
        nodesQueue.add(createNode(p, node));

        // update candidate and segment indexes
        candidateIndex.remove(new Envelope(p), p);
        segmentIndex.remove(node.Envelope, node);

        updateEnvelope(node);
        segmentIndex.insert(node.Envelope, node);
        updateEnvelope(node.Next);
        segmentIndex.insert(node.Next.Envelope, node.Next);
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
    last.Next = first;
    first.Prev = last;

    return res;
  }

  /**
   * Utility function to create the initial index of points that are not part of the start solution convex hull
   *
   * @param coordinates       am array of coordinates
   * @param convexHullPoints  an array of the convex hull coordinates that make up the start solution
   * @return A STRtree index of points
   */
  private static SpatialIndex createCandidatesIndex(Coordinate[] coordinates, Coordinate[] convexHullPoints) {
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
  private static SpatialIndex createSegmentIndex(List<Node> queue) {
    Quadtree res = new Quadtree();
    for (int i = 0; i < queue.size(); i++) {
      Node item = queue.get(i);
      updateEnvelope(item);
      res.insert(item.Envelope, item);
    }
    return res;
  }


  private static Set<Coordinate> getConvexHullPointSet(Coordinate[] convexHullPoints) {
    Set<Coordinate> res = new HashSet<>();
    for (int i = 0; i < convexHullPoints.length - 1; i++) {
      res.add(convexHullPoints[i]);
    }
    return res;
  }

  private Coordinate findCandidate(SpatialIndex tree, Coordinate a, Coordinate b, Coordinate c, Coordinate d,
                                   double maxDistance, SpatialIndex segTree) {

    PriorityQueue queue = new PriorityQueue();
    AbstractNode node = ((STRtree)tree).getRoot();

    // search through the point R-tree with a depth-first search using a priority queue
    // in the order of distance to the edge (b, c)
    while (node != null) {
      List children = node.getChildBoundables();
      for (int i = 0; i < children.size(); i++) {
        Object child = children.get(i);

        double dist;
        if (isLeaf(child))
          dist = Distance.pointToSegment((Coordinate) ((ItemBoundable)child).getItem(), b, c);
        else
          dist = Distance.segmentToEnvelope(b,c, (Envelope)((AbstractNode)child).getBounds());

        // skip the node if it's too far away
        if (dist > maxDistance) continue;

        // add node to queue
        queue.add(new PqItemDistance(child, dist));
      }

      while (queue.size() > 0 && isLeaf(((PqItemDistance)queue.peek()).item)) {
        PqItemDistance item = (PqItemDistance)queue.poll();
        Coordinate p = (Coordinate) ((ItemBoundable)item.item).getItem();

        // skip all points that are as close to adjacent edges (a,b) and (c,d),
        // and points that would introduce self-intersections when connected
        double d0 = Distance.pointToSegment(p, a, b);
        double d1 = Distance.pointToSegment(p, c, d);
        if (item.distance < d0 && item.distance < d1 &&
          noProperIntersections(b, p, segTree) &&
          noProperIntersections(c, p, segTree)) {
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

  private boolean noProperIntersections(Coordinate a, Coordinate b, SpatialIndex segmentIndex) {

    Envelope searchEnv = new Envelope(a);
    searchEnv.expandToInclude(b);
    List edges = segmentIndex.query(searchEnv);

    for (int i = 0; i < edges.size(); i++) {
      Node n = (Node)edges.get(i);
      lineIntersector.computeIntersection(n.Point, n.Next.Point, a, b);
      if (lineIntersector.hasIntersection() && lineIntersector.isProper()) return false;
    }
    return true;
  }

  private class PqItemDistance implements Comparable<PqItemDistance>
  {
    private final Object item;
    private final double distance;

    PqItemDistance(Object item, double distance) {
      this.item = item;
      this.distance = distance;
    }

    public int compareTo(PqItemDistance other) {
      return Double.compare(this.distance, other.distance);
    }
  }

  private static boolean isLeaf(Object node) {
    if (node instanceof ItemBoundable)
      return true;
    if (!(node instanceof AbstractNode))
      Assert.shouldNeverReachHere();
    return false;
  }

  private static void updateEnvelope(Node node) {
    node.Envelope.init();
    node.Envelope.expandToInclude(node.Point);
    node.Envelope.expandToInclude(node.Next.Point);
  }

  /*
  private static boolean hasChildren(Object peek)
  {
    PqItemDistance item = (PqItemDistance)peek;
    if (item.item instanceof AbstractNode) {
      AbstractNode an = (AbstractNode)item.item;
      return an.size() > 0;
    }
    return false;
  }

  private static double sqPtPtDistance(Coordinate p0, Coordinate p1) {
    double dx = p1.x - p0.x;
    double dy = p1.y - p0.y;

    return dx*dx + dy*dy;
  }

  private static double sqPtSegDistance(Coordinate p, Coordinate p1, Coordinate p2) {

    double x = p1.x;
    double y = p1.y;
    double dx = p2.x - x;
    double dy = p2.y - y;

    if (dx != 0 || dy != 0) {

      double t = ((p.x - x) * dx + (p.y - y) * dy) / (dx * dx + dy * dy);

      if (t > 1) {
        x = p2.x;
        y = p2.y;

      } else if (t > 0) {
        x += dx * t;
        y += dy * t;
      }
    }

    dx = p.x - x;
    dy = p.y - y;

    return dx * dx + dy * dy;
  }

  private static double sqSegSegDistance(Coordinate p1, Coordinate p2,
                                         double q0x, double q0y, double q1x, double q1y) {
    return sqSegSegDistance(p1.x, p1.y, p2.x, p2.y, q0x, q0y, q1x, q1y);
  }

  private static double sqSegSegDistance(double x0, double y0, double x1, double y1,
                                         double x2, double y2, double x3, double y3) {
    double ux = x1 - x0;
    double uy = y1 - y0;
    double vx = x3 - x2;
    double vy = y3 - y2;
    double wx = x0 - x2;
    double wy = y0 - y2;
    double a = ux * ux + uy * uy;
    double b = ux * vx + uy * vy;
    double c = vx * vx + vy * vy;
    double d = ux * wx + uy * wy;
    double e = vx * wx + vy * wy;
    double D = a * c - b * b;

    double sc, sN, tc, tN;
    double sD = D;
    double tD = D;

    if (D == 0) {
      sN = 0;
      sD = 1;
      tN = e;
      tD = c;
    } else {
      sN = b * e - c * d;
      tN = a * e - b * d;
      if (sN < 0) {
        sN = 0;
        tN = e;
        tD = c;
      } else if (sN > sD) {
        sN = sD;
        tN = e + b;
        tD = c;
      }
    }

    if (tN < 0.0) {
      tN = 0.0;
      if (-d < 0.0) sN = 0.0;
      else if (-d > a) sN = sD;
      else {
        sN = -d;
        sD = a;
      }
    } else if (tN > tD) {
      tN = tD;
      if ((-d + b) < 0.0) sN = 0;
      else if (-d + b > a) sN = sD;
      else {
        sN = -d + b;
        sD = a;
      }
    }

    sc = sN == 0 ? 0 : sN / sD;
    tc = tN == 0 ? 0 : tN / tD;

    double cx = (1 - sc) * x0 + sc * x1;
    double cy = (1 - sc) * y0 + sc * y1;
    double cx2 = (1 - tc) * x2 + tc * x3;
    double cy2 = (1 - tc) * y2 + tc * y3;
    double dx = cx2 - cx;
    double dy = cy2 - cy;

    return dx * dx + dy * dy;
  }

  private static double sqSegBoxDistance(Coordinate a, Coordinate b, Envelope bounds) {
    if (bounds.contains(a) || bounds.contains(b))
      return 0;
    double d1 = sqSegSegDistance(a, b, bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMinY());
    if (d1 == 0) return 0;
    double d2 = sqSegSegDistance(a, b, bounds.getMinX(), bounds.getMinY(), bounds.getMinX(), bounds.getMaxY());
    if (d2 == 0) return 0;
    double d3 = sqSegSegDistance(a, b, bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
    if (d3 == 0) return 0;
    double d4 = sqSegSegDistance(a, b, bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMaxY());
    if (d4 == 0) return 0;
    return Math.min(Math.min(d1, d2), Math.min(d3, d4));
  }
  */

  private static Node createNode(Coordinate c, Node previous) {
    Node res = new Node(c);

    if (previous == null) {
      res.Prev = res;
      res.Next = res;
    }
    else {
      res.Next = previous.Next;
      res.Prev = previous;
      previous.Next.Prev = res;
      previous.Next = res;
    }

    return res;
  }

  private static class Node {
    Node Prev = null;
    final Envelope Envelope;
    final Coordinate Point;
    Node Next = null;

    Node(Coordinate point) {
      Point = point;
      Envelope = new Envelope();
    }
  }
}
