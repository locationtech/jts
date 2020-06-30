/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;

/**
 * Limits the segments in a list of segments
 * to those which intersect an envelope.
 * This creates zero or more sections of the input segment sequences,
 * containing only line segments which intersect the limit envelope.
 * Segments are not clipped, since that can move 
 * line segments enough to alter topology,
 * and it happens in the overlay in any case.
 * This can substantially reduce the number of vertices which need to be
 * processed during overlay.
 * <p>
 * This optimization is only applicable to Line geometries,
 * since it does not maintain the closed topology of rings.
 * Polygonal geometries are optimized using the {@link RingClipper}.
 * 
 * @author Martin Davis
 *
 * @see RingClipper
 */
public class LineLimiter {
  private Envelope limitEnv;
  private CoordinateList ptList;
  private Coordinate lastOutside = null;
  private List<Coordinate[]> sections = null;

  /**
   * Creates a new limiter for a given envelope.
   * 
   * @param env the envelope to limit to
   */
  public LineLimiter(Envelope env) {
    this.limitEnv = env;
  }
  
  /**
   * Limits a list of segments.
   * 
   * @param pts the segment sequence to limit
   * @return the sections which intersect the limit envelope
   */
  public List<Coordinate[]> limit(Coordinate[] pts) {
    lastOutside = null;
    ptList = null;
    sections = new ArrayList<Coordinate[]>();
    
    for (int i = 0; i < pts.length; i++) {
      Coordinate p = pts[i];
      if ( limitEnv.intersects(p) ) 
        addPoint(p);
      else {
        addOutside(p);
      }
    }
    // finish last section, if any
    finishSection();
    return sections;
  }

  private void addPoint(Coordinate p) {
    if (p == null) return;
    startSection();
    ptList.add(p, false);
  }

  private void addOutside(Coordinate p) {
    boolean segIntersects = isLastSegmentIntersecting(p);
    if ( ! segIntersects  ) {
      finishSection();
    }
    else {
      addPoint(lastOutside);
      addPoint(p);
    }
    lastOutside = p;
  }
  
  private boolean isLastSegmentIntersecting(Coordinate p) {
    if (lastOutside == null) {
      // last point must have been inside
      if (isSectionOpen())
        return true;
      return false;
    }
    return limitEnv.intersects(lastOutside, p);
  }

  private boolean isSectionOpen() {
    return ptList != null;
  }

  private void startSection() {
    if (ptList == null) {
      ptList = new CoordinateList();
    }
    if (lastOutside != null) {
      ptList.add(lastOutside, false);
    }
    lastOutside = null;
  }  
  
  private void finishSection() {
    if (ptList == null) 
      return;
    // finish off this section
    if (lastOutside != null) {
      ptList.add(lastOutside, false);
      lastOutside = null;
    }

    Coordinate[] section = ptList.toCoordinateArray();
    sections.add(section);
    ptList = null;
  }

}