/*
 * Copyright (c) 2019 Martin Davis, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;

public class EnvelopeClipper {
  
  private Envelope env;

  public EnvelopeClipper(Envelope env) {
    this.env = env;
  }
  public LineSegment clip(Coordinate p0, Coordinate p1) {
    Coordinate clip0 = clipEndpoint(env, p0, p1);
    Coordinate clip1 = clipEndpoint(env, p1, p0);
    
    // if clipped segment lies on boundary of envelope
    // return null to indicate this
    if (isOnBoundary(clip0, clip1))
      return null;
    
    return new LineSegment(clip0, clip1);
  }

  private boolean isOnBoundary(Coordinate p0, Coordinate p1) {
    if (p0.x == p1.x) {
      if (p0.x == env.getMinX() || p0.x == env.getMaxX())
        return true;
    }
    if (p0.y == p1.y) {
      if (p0.y == env.getMinY() || p0.x == env.getMaxY())
        return true;
    }
    return false;
  }
  
  private Coordinate clipEndpoint(Envelope env, Coordinate p0, Coordinate p1) {
    Coordinate clipPt = new Coordinate(p0);
    if (p0.x < env.getMinX()) {
      clipPt.x = intersectionLineX(p0, p1, env.getMinX());
    }
    else if (p0.x > env.getMaxX()) {
      clipPt.x = intersectionLineX(p0, p1, env.getMaxX());
    }
    if (p0.y < env.getMinY()) {
      clipPt.y = intersectionLineY(p0, p1, env.getMinY());
    }
    else if (p0.y > env.getMaxX()) {
      clipPt.y = intersectionLineY(p0, p1, env.getMaxY());
    }
    return clipPt;
  }

  private double intersectionLineY(Coordinate a, Coordinate b, double y) {
    // short-circuit if segment is parallel to Y axis
    if (b.x == a.x) return a.x;
    
    double m = (b.x - a.x) / (b.y - a.y);
    double intercept = (y - a.y) * m;
    return a.x + intercept;
  }

  private double intersectionLineX(Coordinate a, Coordinate b, double x) {
    // short-circuit if segment is parallel to X axis
    if (b.y == a.y) return a.y;
    
    double m = (b.y - a.y) / (b.x - a.x);
    double intercept = (x - a.x) * m;
    return a.y + intercept;
  }

}
