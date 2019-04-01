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
package org.locationtech.jtslab.algorithm;

import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geomgraph.index.EdgeSetIntersector;
import org.locationtech.jts.geomgraph.index.SegmentIntersector;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdNodeVisitor;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jtslab.geomgraph.index.AnchorPointMCSweepLineIntersector;

import java.util.List;

/**
 * An extended version of the {@linkplain RobustLineIntersector}.
 * It is supported by an index of {@linkplain AnchorPoint}s which
 * is used to query for and reuse already existant points.
 */
public class AnchorPointRobustLineIntersector
  extends RobustLineIntersector
{

  /**
   * The default distance that points need to have from anchor points in order
   * to qualify for <i>real</i> points
   */
  private static final double DEFAULT_MIN_DISTANCE_FROM_ANCHOR_POINT = 1E-10;

  /** Index of {@linkplain AnchorPoint}s */
  private final KdTree anchorPoints = new KdTree();

  /** Minimum distance new points must have to already existant {@linkplain AnchorPoint}s. */
  private double minDistanceToAnchorPoint;

  /**
   * Creates an instance of this class using the provided precision model
   *
   * @param precisionModel a precision model
   */
  public AnchorPointRobustLineIntersector(PrecisionModel precisionModel) {
    this(precisionModel, determineMinDistanceFromAnchorPoint(precisionModel));
  }

  /**
   * Creates an instance of this class using the provided distance measure.
   *
   * @param minDistanceToAnchorPoint a distance measure
   */
  public AnchorPointRobustLineIntersector(double minDistanceToAnchorPoint) {
    this(null, minDistanceToAnchorPoint);
  }

  /**
   * Creates an instance of this class using the provided precision model and
   * distance measure.
   *
   * @param precisionModel a precision model
   * @param minDistanceToAnchorPoint a distance measure
   */
  public AnchorPointRobustLineIntersector(PrecisionModel precisionModel, double minDistanceToAnchorPoint) {
    super.setPrecisionModel(precisionModel);
    this.minDistanceToAnchorPoint = minDistanceToAnchorPoint;
  }

  /**
   * Determines the distance points need to have from anchor points in order to qualify
   * as *real* points.
   *
   * @param precisionModel a precision model
   * @return a distance
   */
  private static double determineMinDistanceFromAnchorPoint(PrecisionModel precisionModel) {
    if (precisionModel != null && !precisionModel.isFloating())
      return 1d / (2d * precisionModel.getScale());
    else
      return  DEFAULT_MIN_DISTANCE_FROM_ANCHOR_POINT;
  }

  @Override
  public EdgeSetIntersector create() {
    return new AnchorPointMCSweepLineIntersector(this.anchorPoints);
  }

  @Override
  public void setPrecisionModel(PrecisionModel precisionModel) {
    double minDistanceToAnchorPoint = determineMinDistanceFromAnchorPoint(precisionModel);
    if (minDistanceToAnchorPoint > this.minDistanceToAnchorPoint)
      this.minDistanceToAnchorPoint = minDistanceToAnchorPoint;

    super.setPrecisionModel(precisionModel);
  }

  /**
   * Computes the actual intersection point between the segments {@param p0} to {@param p1} and
   * {@param q0} to {@param q1}.
   *
   * If {@linkplain #minDistanceToAnchorPoint} has a positive value, an attempt is made to query
   * and substitute an {@linkplain AnchorPoint} from {@linkplain #getAnchorPoints()}.
   *
   * @param p1 the starting point of the 1st segment
   * @param p2 the end-point of the 1st segment
   * @param q1 the starting point of the 2nd segment
   * @param q2 the end-point of the 2nd segment
   *
   * @return the intersection point
   *
   */
  @Override
  protected Coordinate intersection(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {

    if (this.minDistanceToAnchorPoint <= 0)
      return super.intersection(p1, p2, q1, q2);

    Coordinate intPt = intersectionWithNormalization(p1, p2, q1, q2);

    // create search envelope for anchor points
    Envelope e = new Envelope(p1, p2);
    e.expandToInclude(new Envelope(q1, q2));

    // create visitor and query anchor points
    AnchorPointVisitor apv = new AnchorPointVisitor(intPt);
    this.anchorPoints.query(e, apv);

    // get the possible anchor point
    AnchorPoint ap = apv.getAnchorPoint();
    if (ap != null) {
      // if anchor point is a input vertex, the intersection is not proper!
      if (ap.fromVertex) this.isProper = false;
      return ap.getCoordinate();
    }

    // apply precision model to intersection point
    if (this.precisionModel != null)
      this.precisionModel.makePrecise(intPt);

    // add intersection point to index!
    this.anchorPoints.insert(intPt, new AnchorPoint(intPt, false));

    return intPt;

  }

  /**
   * Access to the index of {@linkplain AnchorPoint}s.
   * @return an index of {@linkplain AnchorPoint}s.
   */
  public KdTree getAnchorPoints() {
    return this.anchorPoints;
  }

  /**
   * Visitor class that searches for the closest {@linkplain AnchorPoint} to
   * a {@linkplain #testPoint}.
   */
  private class AnchorPointVisitor implements KdNodeVisitor {

    /** the test point*/
    final Coordinate testPoint;

    /** the distance of the closest anchor point to {@linkplain #testPoint} */
    double anchorPointDistance;

    /** the closest anchor point to {@linkplain #testPoint} */
    AnchorPoint anchorPoint;

    /**
     * Creates an instance of this class
     * @param testPoint a test point
     */
    AnchorPointVisitor(Coordinate testPoint) {
      this.testPoint = testPoint;
      this.anchorPointDistance = Double.MAX_VALUE;
    }

    @Override
    public void visit(KdNode node) {
      double distance = node.getCoordinate().distance(testPoint);
      if (distance < anchorPointDistance && distance <= minDistanceToAnchorPoint) {
        this.anchorPointDistance = distance;
        this.anchorPoint = (AnchorPoint) node.getData();
      }
    }

    /**
     * Access to the closest {@linkplain AnchorPoint}.
     * @return an anchor point if found or <c>null</c>.
     */
    public AnchorPoint getAnchorPoint() {
      if (anchorPointDistance == Double.MAX_VALUE)
        return null;

      return this.anchorPoint;
    }
  }
}
