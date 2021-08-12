/*
 * Copyright (c) 2019 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder.ui.style;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jtstest.testbuilder.geom.SegmentClipper;

/**
 * Determines a label point for a LineString, 
 * subject to a constraint envelope.
 * The constraint is typically a viewport.
 * 
 * @author mdavis
 *
 */
public class LineLabelBaseline {

  public static LineSegment getBaseline(LineString line, Envelope constraintEnv) {
    LineLabelBaseline labeller = new LineLabelBaseline(line, constraintEnv);
    return labeller.getBaseline();
  }

  private Envelope constraintEnv;
  private LineString line;
  
  public LineLabelBaseline(LineString line, Envelope constraintEnv) {
    this.line = line;
    this.constraintEnv = constraintEnv;
  }

  public LineSegment getBaseline() {    
    // iterate over line to find first visible clip segment
    for (int i = 0; i < line.getNumPoints() - 1; i++) {
      Coordinate seg0 = line.getCoordinateN(i);
      Coordinate seg1 = line.getCoordinateN(i + 1);
      LineSegment seg = clip(seg0, seg1);
      if (seg != null) return seg;
    }
    
    return null;
    //TODO: find clip segment with midpoint closest to window centre?
    //TODO: handle case where start segment of line is almost out of view
  }

  private LineSegment clip(Coordinate p0, Coordinate p1) {
    if (! constraintEnv.intersects(p0, p1))
      return null;
    Coordinate clip0 = new Coordinate(p0);
    Coordinate clip1 = new Coordinate(p1);
    SegmentClipper.clip(clip0, clip1, constraintEnv);
    if (isOnBoundary(constraintEnv, p0, p1))
      return null;
    return new LineSegment(clip0, clip1);
  }

  private boolean isOnBoundary(Envelope env, Coordinate p0, Coordinate p1) {
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

}
