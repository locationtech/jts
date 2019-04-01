/*
 * Copyright (c) 2019 Felix Obermaier
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtslab.noding.anchorpoint;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.chain.MonotoneChain;
import org.locationtech.jts.index.chain.MonotoneChainSelectAction;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentString;

import java.util.*;

/**
 * Anchor point snapper attempts to snap coordinates that are the result of intersection
 * computation to already existing {@linkplain AnchorPoint}s.
 *
 * If that is not possible, but required, a new {@linkplain AnchorPoint} is created.
 *
 * @version 1.17
 * @author Felix Obermaier
 */
class AnchorPointSnapper {

  /** An index of {@linkplain MonotoneChain}s. */
  final STRtree index;

  /** An index of already created {@linkplain AnchorPoint}s. */
  final KdTree ptIndex;

  /**
   * The distance coordinates must have from an {@linkplain AnchorPoint} to <b>not</b>
   * be anchored/snapped to it.
   * */
  final double maxAnchorDistance;

  /**
   * Creates an instance of this class
   * @param segStrings        A collection of segment strings
   * @param index             the index of {@linkplain MonotoneChain}s.
   * @param maxAnchorDistance the distance coordinates must have from an {@linkplain AnchorPoint} to <b>not</b>
   */
  AnchorPointSnapper(Collection segStrings, SpatialIndex index, double maxAnchorDistance) {
    this.index = (STRtree)index;
    this.maxAnchorDistance = maxAnchorDistance;
    this.ptIndex = new KdTree();
    AddVertexAnchorNodes(segStrings);
  }

  /**
   * Fills the search anchor point index with segment string vertices.
   * @param segStrings the segment strings
   */
  private void AddVertexAnchorNodes(Collection segStrings) {
    Iterator it = segStrings.iterator();
    while (it.hasNext()) {
      SegmentString ss = (SegmentString)it.next();
      for (int i = 0; i < ss.size(); i++) {
        Coordinate c = ss.getCoordinate(i);
        this.ptIndex.insert(c, new AnchorPoint(c, maxAnchorDistance, true));
      }
    }
  }

  /**
   * Gets all  {@linkplain AnchorPoint}s used by this anchor point snapper.
   *
   * @param onlyIntersections return only anchor points that were created from intersections
   */
  public List<AnchorPoint> getAnchorPoints(boolean onlyIntersections) {
    ArrayList<KdNode> anchorNodes = new ArrayList<>();
    this.ptIndex.query(
      new Envelope(Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MAX_VALUE),
      anchorNodes);

    List<AnchorPoint> res = new ArrayList<>();
    for (int i = 0; i < anchorNodes.size(); i++)
    {
      AnchorPoint ap = (AnchorPoint) anchorNodes.get(i).getData();
      if (ap.getUsed())
        if (!onlyIntersections || !ap.getFromVertex())
          res.add((AnchorPoint)anchorNodes.get(i).getData());
    }
    return res;
  }

  /**
   * Attempts to snap a {@linkplain Coordinate} to an {@linkplain AnchorPoint}.

   * @param c the coordinate
   *
   * @return <pre>true</pre> if the coordinate was snapped.
   */
  boolean snap(Coordinate c) {
    return snap(c, null, -1);
  }

  /**
   * Attempts to snap a {@linkplain Coordinate} to an {@linkplain AnchorPoint}.

   * @param c          the coordinate
   * @param parentEdge the segment string that <pre>c</pre> belongs.to.
   * @param index      the index of the segment that <pre>c</pre> is the starting point of.
   *
   * @return <pre>true</pre> if the coordinate was snapped.
   */
  boolean snap(Coordinate c, SegmentString parentEdge, int index) {

    // search for anchor points
    Envelope e = new Envelope(c);
    e.expandBy(maxAnchorDistance);
    List<KdNode> anchoredPoints = new ArrayList<>();
    this.ptIndex.query(e, anchoredPoints);

    // get or create anchor point
    boolean newAnchorPoint = anchoredPoints.size() == 0;
    if (newAnchorPoint) {
      anchoredPoints.add(new KdNode(c, new AnchorPoint(c, maxAnchorDistance)));
    } else {
      anchoredPoints.sort(new AnchorPointDistanceComparator(c));
    }

    for (int i = 0; i < anchoredPoints.size(); i++) {
      AnchorPoint ap = (AnchorPoint) anchoredPoints.get(i).getData();
      final AnchorSnapAction asa = new AnchorSnapAction(ap, parentEdge, index);
      this.index.query(e, new ItemVisitor() {
        public void visitItem(Object item) {
          MonotoneChain testChain = (MonotoneChain) item;
          testChain.select(e, asa);
        }
      });

      // if the anchor point was not in the index before, add it now.
      if (asa.isNodeAdded() && newAnchorPoint)
        ptIndex.insert(c, ap);

      if (asa.isNodeAdded())
      {
        c.setCoordinate(ap.getCoordinate());
        return true;
      }
    }
    return false;
  }

  static class AnchorPointDistanceComparator implements Comparator<KdNode> {

    private final Coordinate c;

    public AnchorPointDistanceComparator(Coordinate c) {
      this.c = c;
    }

    public int compare(KdNode kd1, KdNode kd2) {
      double distToKd1 = c.distance(kd1.getCoordinate());
      double distToKd2 = c.distance(kd2.getCoordinate());

      if (distToKd1 < distToKd2) return -1;
      if (distToKd1 > distToKd2) return 1;
      return 0;
    }
  }

  static class AnchorSnapAction extends MonotoneChainSelectAction {

    private final AnchorPoint anchorPoint;
    private final SegmentString parentEdge;
    private final int coordinateIndex;
    private boolean isNodeAdded;

    public AnchorSnapAction(AnchorPoint anchorPoint, SegmentString parentEdge, int coordinateIndex) {
      this.anchorPoint = anchorPoint;
      this.parentEdge = parentEdge;
      this.coordinateIndex = coordinateIndex;
    }

    boolean isNodeAdded() {return this.isNodeAdded;}

    public void select(MonotoneChain mc, int startIndex) {
      NodedSegmentString ss = (NodedSegmentString) mc.getContext();

      if (this.parentEdge != null){
        if (ss == parentEdge && startIndex == this.coordinateIndex)
          return;
      }

      this.isNodeAdded = anchorPoint.addAnchoredNode(ss, startIndex);
    }
  }
}
