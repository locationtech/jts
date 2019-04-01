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

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A noder implementation based on {@linkplain AnchorPoint}.
 */
class AnchorPointNoder
  implements Noder
{
  final PrecisionModel precisionModel;
  final LineIntersector lineIntersector;
  final double scaleFactor;

  private MCIndexNoder lastNoder;
  private Collection lastNodedSegStrings;
  private AnchorPointSnapper lastAnchorPointSnapper;

  /**
   * Creates an instance of this class
   *
   * @param precisionModel the precision model to use for the underlying {@linkplain LineIntersector}.
   */
  public AnchorPointNoder(PrecisionModel precisionModel)
  {
    this.precisionModel = precisionModel;
    this.lineIntersector = new RobustLineIntersector();
    this.lineIntersector.setPrecisionModel(precisionModel);

    this.scaleFactor = precisionModel.isFloating() ? 1E10 : precisionModel.getScale();
  }

  @Override
  public void computeNodes(Collection segStrings) {

    Collection current = segStrings;

    MCIndexNoder currentNoder = new MCIndexNoder();
    double maxAnchorDistance = 1d / (this.scaleFactor*2);

    AnchorPointSnapper apx = new AnchorPointSnapper(segStrings, currentNoder.getIndex(), maxAnchorDistance);

    // compute intersections
    List intersections = findInteriorIntersections(current, currentNoder, this.lineIntersector);
    anchor(apx, current, intersections);

    this.lastNoder = currentNoder;
    this.lastAnchorPointSnapper = apx;
    this.lastNodedSegStrings = current;
  }

  @Override
  public Collection getNodedSubstrings() {
    return NodedSegmentString.getNodedSubstrings(this.lastNodedSegStrings);
  }

  /**
   *
   *
   * @return a list of anchor points
   * @param onlyIntersection
   */
  List<AnchorPoint> getAnchorPoints(boolean onlyIntersection){
    if (this.lastAnchorPointSnapper == null)
      return new ArrayList<>();
    return this.lastAnchorPointSnapper.getAnchorPoints(onlyIntersection);
  }

  private void anchor(AnchorPointSnapper aps, Collection segStrings, List intersections)
  {
    computeIntersectionAnchors(aps, intersections);
    computeVertexAnchors(aps, segStrings);
  }

  private void computeIntersectionAnchors(AnchorPointSnapper aps, List intersections) {
    for (Iterator it = intersections.iterator(); it.hasNext(); ) {
      Coordinate intersection = (Coordinate) it.next();
      aps.snap(intersection);
    }
  }

  /**
   * Snaps segments to all vertices.
   *
   * @param edges the list of segment strings to snap together
   */
  private void computeVertexAnchors(AnchorPointSnapper aps, Collection edges)
  {
    for (Iterator i0 = edges.iterator(); i0.hasNext(); ) {
      NodedSegmentString edge0 = (NodedSegmentString) i0.next();
      computeVertexAnchors(aps, edge0);
    }
  }

  /**
   * Snaps segments to the vertices of a Segment String.
   */
  private void computeVertexAnchors(AnchorPointSnapper aps, NodedSegmentString e)
  {
    Coordinate[] pts0 = e.getCoordinates();
    for (int i = 0; i < pts0.length ; i++) {
      boolean isNodeAdded = aps.snap(pts0[i], e, i);
      // if a node is created for a vertex, that vertex must be noded too
      if (isNodeAdded) {
        e.addIntersection(pts0[i], i);
      }
    }
  }
  /**
   * Computes all interior intersections in the collection of {@link SegmentString}s,
   * and returns their {@link Coordinate}s.
   *
   * Does NOT node the segStrings.
   *
   * @return a list of Coordinates for the intersections
   */
  private List findInteriorIntersections(Collection segStrings, MCIndexNoder noder, LineIntersector li)
  {
    InteriorIntersectionFinderAdder intFinderAdder = new InteriorIntersectionFinderAdder(li);
    noder.setSegmentIntersector(intFinderAdder);
    noder.computeNodes(segStrings);

    return intFinderAdder.getInteriorIntersections();
  }
}
