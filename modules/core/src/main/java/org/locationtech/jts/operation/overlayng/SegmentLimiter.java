/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
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
 * Limits the segments in a segment string
 * to those which intersect (overlap) a given envelope.
 * The result is zero or more subsections of the input segments.
 * 
 * @author Martin Davis
 *
 */
public class SegmentLimiter {
  private Envelope limitEnv;
  private CoordinateList ptList;
  private Coordinate lastOutside = null;
  private List<Coordinate[]> sections = null;

  SegmentLimiter(Envelope env) {
    this.limitEnv = env;
  }
  
  /**
   * Test whether an envelope is within the limit envelope.
   * This can be used to determine if a geometry
   * has significant extent, and thus is not collapsed completely.
   * 
   * @param env an envelope
   * @return true if the envelope is covered by the limit envelope
   */
  public boolean isWithinLimit(Envelope env) {
    return limitEnv.covers(env);
  }
  
  List<Coordinate[]> limit(Coordinate[] pts) {
    lastOutside = null;
    ptList = null;
    sections = new ArrayList<Coordinate[]>();
    
    for (int i = 0; i < pts.length; i++) {
      Coordinate p = pts[i];
      if (limitEnv.contains(p)) 
        addInside(p);
      else {
        addOutside(p);
      }
    }
    // finish last section, if any
    finishSection();
    return sections;
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
  
  private void startSection() {
    if (ptList == null) {
      ptList = new CoordinateList();
    }
    if (lastOutside != null) {
      ptList.add(lastOutside, false);
    }
    lastOutside = null;
  }

  private void addInside(Coordinate p) {
    startSection();
    ptList.add(p, false);
  }

  private void addOutside(Coordinate p) {
    if (ptList != null) {
      if (lastOutside != null) {
        finishSection();
        lastOutside = p;
        return;
      }
      else { // lastOutside is null
        // keep this coordinate until next pt is checked
        lastOutside = p;
        return;
      }
    }
    else {
      // if ptList is null, check if this segment crosses the env
      if (lastOutside != null) {
        boolean segIntersects = limitEnv.intersects(lastOutside, p);
        if (segIntersects) {
          Coordinate[] section = new Coordinate[] { lastOutside, p};
          sections.add(section);
          lastOutside = p;
          return;
        }
      }
    }
    lastOutside = p;
  }
  
}
