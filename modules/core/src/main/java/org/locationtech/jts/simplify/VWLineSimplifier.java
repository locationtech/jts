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

package org.locationtech.jts.simplify;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Triangle;

/**
 * Simplifies a linestring (sequence of points) using the 
 * Visvalingam-Whyatt algorithm.
 * The Visvalingam-Whyatt algorithm simplifies geometry 
 * by removing vertices while trying to minimize the area changed.
 * 
 * @version 1.7
 */
class VWLineSimplifier
{
  public static Coordinate[] simplify(Coordinate[] pts, double distanceTolerance)
  {
    VWLineSimplifier simp = new VWLineSimplifier(pts, distanceTolerance);
    return simp.simplify();
  }

  private Coordinate[] pts;
  private double tolerance;

  public VWLineSimplifier(Coordinate[] pts, double distanceTolerance)
  {
    this.pts = pts;
    this.tolerance = distanceTolerance * distanceTolerance;
  }

  public Coordinate[] simplify()
  {
    VWLineSimplifier.VWVertex vwLine = VWVertex.buildLine(pts);
    double minArea = tolerance;
    do {
      minArea = simplifyVertex(vwLine);
    } while (minArea < tolerance);
    Coordinate[] simp = vwLine.getCoordinates();
    // ensure computed value is a valid line
    if (simp.length < 2) {
      return new Coordinate[] { simp[0], new Coordinate(simp[0]) };
    }
    return simp;
  }

  private double simplifyVertex(VWLineSimplifier.VWVertex vwLine)
  {
    /**
     * Scan vertices in line and remove the one with smallest effective area.
     */
    // TODO: use an appropriate data structure to optimize finding the smallest area vertex
    VWLineSimplifier.VWVertex curr = vwLine;
    double minArea = curr.getArea();
    VWLineSimplifier.VWVertex minVertex = null;
    while (curr != null) {
      double area = curr.getArea();
      if (area < minArea) {
        minArea = area;
        minVertex = curr;
      }
      curr = curr.next;
    }
    if (minVertex != null && minArea < tolerance) {
      minVertex.remove();
    }
    if (! vwLine.isLive()) return -1;
    return minArea;
  }


  static class VWVertex
  {
    public static VWLineSimplifier.VWVertex buildLine(Coordinate[] pts)
    {
      VWLineSimplifier.VWVertex first = null;
      VWLineSimplifier.VWVertex prev = null;
      for (int i = 0; i < pts.length; i++) {
        VWLineSimplifier.VWVertex v = new VWVertex(pts[i]);
        if (first == null)
          first = v;
        v.setPrev(prev);
        if (prev != null) {
          prev.setNext(v);
          prev.updateArea();
        }
        prev = v;
      }
      return first;
    }
    
    public static double MAX_AREA = Double.MAX_VALUE;
    
    private Coordinate pt;
    private VWLineSimplifier.VWVertex prev;
    private VWLineSimplifier.VWVertex next;
    private double area = MAX_AREA;
    private boolean isLive = true;

    public VWVertex(Coordinate pt)
    {
      this.pt = pt;
    }

    public void setPrev(VWLineSimplifier.VWVertex prev)
    {
      this.prev = prev;
    }

    public void setNext(VWLineSimplifier.VWVertex next)
    {
      this.next = next;
    }

    public void updateArea()
    {
      if (prev == null || next == null) {
        area = MAX_AREA;
        return;
      }
      area = Math.abs(Triangle.area(prev.pt, pt, next.pt));
    }

    public double getArea()
    {
      return area;
    }
    public boolean isLive()
    {
      return isLive;
    }
    public VWLineSimplifier.VWVertex remove()
    {
      VWLineSimplifier.VWVertex tmpPrev = prev;
      VWLineSimplifier.VWVertex tmpNext = next;
      VWLineSimplifier.VWVertex result = null;
      if (prev != null) {
        prev.setNext(tmpNext);
        prev.updateArea();
        result = prev;
      }
      if (next != null) {
        next.setPrev(tmpPrev);
        next.updateArea();
        if (result == null)
          result = next;
      }
      isLive = false;
      return result;
    }
    public Coordinate[] getCoordinates()
    {
      CoordinateList coords = new CoordinateList();
      VWLineSimplifier.VWVertex curr = this;
      do {
        coords.add(curr.pt, false);
        curr = curr.next;
      } while (curr != null);
      return coords.toCoordinateArray();
    }
  }
}