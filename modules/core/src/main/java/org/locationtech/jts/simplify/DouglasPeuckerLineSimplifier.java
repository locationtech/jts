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

package org.locationtech.jts.simplify;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.LineSegment;

/**
 * Simplifies a linestring (sequence of points) using
 * the standard Douglas-Peucker algorithm.
 *
 * @version 1.7
 */
class DouglasPeuckerLineSimplifier
{
  public static Coordinate[] simplify(Coordinate[] pts, double distanceTolerance, boolean isPreserveEndpoint)
  {
    DouglasPeuckerLineSimplifier simp = new DouglasPeuckerLineSimplifier(pts);
    simp.setDistanceTolerance(distanceTolerance);
    simp.setPreserveEndpoint(isPreserveEndpoint);
    return simp.simplify();
  }

  private Coordinate[] pts;
  private boolean[] usePt;
  private double distanceTolerance;
  private boolean isPreserveEndpoint = false;

  public DouglasPeuckerLineSimplifier(Coordinate[] pts)
  {
    this.pts = pts;
  }
  /**
   * Sets the distance tolerance for the simplification.
   * All vertices in the simplified linestring will be within this
   * distance of the original linestring.
   *
   * @param distanceTolerance the approximation tolerance to use
   */
  public void setDistanceTolerance(double distanceTolerance) {
    this.distanceTolerance = distanceTolerance;
  }

  private void setPreserveEndpoint(boolean isPreserveEndpoint) {
    this.isPreserveEndpoint  = isPreserveEndpoint;
  }
  
  public Coordinate[] simplify()
  {
    usePt = new boolean[pts.length];
    for (int i = 0; i < pts.length; i++) {
      usePt[i] = true;
    }
    simplifySection(0, pts.length - 1);
    
    CoordinateList coordList = new CoordinateList();
    for (int i = 0; i < pts.length; i++) {
      if (usePt[i])
        coordList.add(pts[i].copy());
    }
    
    if (! isPreserveEndpoint && CoordinateArrays.isRing(pts)) {
      simplifyRingEndpoint(coordList);
    }

   return coordList.toCoordinateArray();
  }

  private void simplifyRingEndpoint(CoordinateList pts) {
    //-- avoid collapsing triangles
    if (pts.size() < 4)
      return;
    //-- base segment for endpoint
    seg.p0 = pts.get(1);
    seg.p1 = pts.get(pts.size() - 2); 
    double distance = seg.distance(pts.get(0));
    if (distance <= distanceTolerance) {
      pts.remove(0);
      pts.remove(pts.size() - 1);
      pts.closeRing();
    }
  }

  private LineSegment seg = new LineSegment();

  private void simplifySection(int i, int j)
  {
    if((i+1) == j) {
      return;
    }
    seg.p0 = pts[i];
    seg.p1 = pts[j];
    double maxDistance = -1.0;
    int maxIndex = i;
    for (int k = i + 1; k < j; k++) {
      double distance = seg.distance(pts[k]);
      if (distance > maxDistance) {
        maxDistance = distance;
        maxIndex = k;
      }
    }
    if (maxDistance <= distanceTolerance) {
      for(int k = i + 1; k < j; k++) {
        usePt[k] = false;
      }
    }
    else {
      simplifySection(i, maxIndex);
      simplifySection(maxIndex, j);
    }
  }

}
