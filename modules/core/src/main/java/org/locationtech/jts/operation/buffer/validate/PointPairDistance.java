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
package org.locationtech.jts.operation.buffer.validate;

import org.locationtech.jts.geom.Coordinate;

/**
 * Contains a pair of points and the distance between them.
 * Provides methods to update with a new point pair with
 * either maximum or minimum distance.
 */
public class PointPairDistance {

  private Coordinate[] pt = { new Coordinate(), new Coordinate() };
  private double distance = Double.NaN;
  private boolean isNull = true;

  public PointPairDistance()
  {
  }

  public void initialize() { isNull = true; }

  public void initialize(Coordinate p0, Coordinate p1)
  {
    pt[0].setCoordinate(p0);
    pt[1].setCoordinate(p1);
    distance = p0.distance(p1);
    isNull = false;
  }

  /**
   * Initializes the points, avoiding recomputing the distance.
   * @param p0
   * @param p1
   * @param distance the distance between p0 and p1
   */
  private void initialize(Coordinate p0, Coordinate p1, double distance)
  {
    pt[0].setCoordinate(p0);
    pt[1].setCoordinate(p1);
    this.distance = distance;
    isNull = false;
  }

  public double getDistance() { return distance; }

  public Coordinate[] getCoordinates() { return pt; }

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
}
