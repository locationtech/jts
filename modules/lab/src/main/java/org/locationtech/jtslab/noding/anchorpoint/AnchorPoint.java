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
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.noding.NodedSegmentString;

/**
 * An anchor point
 */
class AnchorPoint {

  /** The actual anchor point */
  private final Coordinate anchorPoint;
  /** the maximum distance for coordinates to test to {@linkplain #anchorPoint}. */
  private final double maxAnchorDistance;
  /** An envelope around {@linkplain #anchorPoint} */
  private final Envelope anchorEnvelope;
  /** A utility value to pre-exclude points from more expensive tests. */
  private final double maxAnchorDistanceDiag;
  private final boolean fromVertex;

  private int usedCounter;
  /**
   * Creates an instance of this class
   *
   * @param anchorPoint       the anchor point coordinate
   * @param maxAnchorDistance the maximum distance for coordinates to be anchored to {@linkplain #anchorPoint}.
   */
  public AnchorPoint(Coordinate anchorPoint, double maxAnchorDistance) {
    this(anchorPoint, maxAnchorDistance, false);
  }

  public AnchorPoint(Coordinate anchorPoint, double maxAnchorDistance, boolean notUsed) {
    this.anchorPoint = anchorPoint;
    this.maxAnchorDistance = maxAnchorDistance;
    this.maxAnchorDistanceDiag = maxAnchorDistance * Math.sqrt(2);

    this.anchorEnvelope = new Envelope(anchorPoint);
    this.anchorEnvelope.expandBy(maxAnchorDistance);
    this.fromVertex = notUsed;

    usedCounter = notUsed ? 0 : 1;
  }

  /**
   * Adds a new node (equal to the {@linkplain #anchorPoint} to the specified segment
   * if the segment is close enough to {@linkplain #anchorPoint}
   *
   * @param nodedSegmentString  a string of segments
   * @param index   the index of the segment to test
   * @return true if a node was added to the segment
   */
  public boolean addAnchoredNode(NodedSegmentString nodedSegmentString, int index) {

    Coordinate p0 = nodedSegmentString.getCoordinate(index);
    Coordinate p1 = nodedSegmentString.getCoordinate(index + 1);

    final LineSegment ls =  new LineSegment(p0, p1);
    double distance = ls.distancePerpendicular(this.anchorPoint);
    if (distance > this.maxAnchorDistanceDiag)
      return false;

    double projectFactor = ls.projectionFactor(this.anchorPoint);
    if (0d <= projectFactor && projectFactor <= 1d) {
      Coordinate p = ls.project(anchorPoint);
      if (this.anchorEnvelope.intersects(p)) {
        if (p.x == this.anchorEnvelope.getMaxX() ||
            p.y == this.anchorEnvelope.getMaxY()) return false;

        if (projectFactor == 0d)
          p0.setCoordinate(this.anchorPoint);

        if (projectFactor == 1d)
          p1.setCoordinate(this.anchorPoint);

        nodedSegmentString.addIntersection(this.anchorPoint, index);
        usedCounter++;
        return true;
      }
    }

    return false;
  }

  /**
   * Get the anchor point coordinate.
   *
   * @return a coordinate
   */
  public Coordinate getCoordinate() { return this.anchorPoint; }

  /**
   * Gets a value indicating that this {@linkplain #anchorPoint} has been used for noding
   *
   * @return a distance threshold
   */
  public boolean getUsed() { return this.usedCounter > 0; }

  /**
   * Gets a value indicating that this {@linkplain #anchorPoint} was created by a vertex
   *
   * @return a distance threshold
   */
  public boolean getFromVertex() { return this.fromVertex; }

  /**
   * Gets the distance value that coordinates must have to
   * {@linkplain #anchorPoint} <b>not</b> be anchored.
   *
   * @return a distance threshold
   */
  public double getMaxAnchorDistance() { return this.maxAnchorDistance; }

}
