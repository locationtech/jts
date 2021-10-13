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

package org.locationtech.jts.algorithm.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;

/**
 * Contains a pair of points and the distance between them.
 * Provides methods to update with a new point pair with
 * either maximum or minimum distance.
 */
public class PointPairDistance {

  private final Coordinate[] pt = { new Coordinate(), new Coordinate() };
  private double distance = Double.NaN;
  private boolean isNull = true;

  /**
   * Creates an instance of this class
   */
  public PointPairDistance()
  {
  }

  /**
   * Initializes this instance.
   */
  public void initialize() { isNull = true; }

  /**
   * Initializes the points, computing the distance between them.
   * @param p0 the 1st point
   * @param p1 the 2nd point
   */
  public void initialize(Coordinate p0, Coordinate p1)  {
    initialize(p0, p1, p0.distance(p1));
  }

  /**
   * Initializes the points, avoiding recomputing the distance.
   * @param p0 the 1st point
   * @param p1 the 2nd point
   * @param distance the distance between p0 and p1
   */
  void initialize(Coordinate p0, Coordinate p1, double distance)
  {
    pt[0].setCoordinate(p0);
    pt[1].setCoordinate(p1);
    this.distance = distance;
    isNull = false;
  }

  /**
   * Gets the distance between the paired points
   * @return the distance between the paired points
   */
  public double getDistance() { return distance; }

  /**
   * Gets the paired points
   * @return the paired points
   */
  public Coordinate[] getCoordinates() { return pt; }

  /**
   * Gets one of the paired points
   * @param i the index of the paired point (0 or 1)
   * @return A point
   */
  public Coordinate getCoordinate(int i) { return pt[i]; }

  public void setMaximum(PointPairDistance ptDist)
  {
    setMaximum(ptDist.pt[0], ptDist.pt[1]);
  }

  public void setMaximum(Coordinate p0, Coordinate p1)
  {
    if (isNull) {
      initialize(p0, p1);
      return;
    }
    double dist = p0.distance(p1);
    if (dist > distance)
      initialize(p0, p1, dist);
  }

  public void setMinimum(PointPairDistance ptDist)
  {
    setMinimum(ptDist.pt[0], ptDist.pt[1]);
  }

  public void setMinimum(Coordinate p0, Coordinate p1)
  {
    if (isNull) {
      initialize(p0, p1);
      return;
    }
    double dist = p0.distance(p1);
    if (dist < distance)
      initialize(p0, p1, dist);
  }

  public String toString()
  {
  	return WKTWriter.toLineString(pt[0], pt[1]);
  }
}
