


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
package org.locationtech.jts.geomgraph;

import java.io.PrintStream;

import org.locationtech.jts.geom.Coordinate;

/**
 * Represents a point on an
 * edge which intersects with another edge.
 * <p>
 * The intersection may either be a single point, or a line segment
 * (in which case this point is the start of the line segment)
 * The intersection point must be precise.
 *
 * @version 1.7
 */
public class EdgeIntersection
    implements Comparable
{

  public Coordinate coord;   // the point of intersection
  public int segmentIndex;   // the index of the containing line segment in the parent edge
  public double dist;        // the edge distance of this point along the containing line segment

  public EdgeIntersection(Coordinate coord, int segmentIndex, double dist) {
    this.coord = new Coordinate(coord);
    this.segmentIndex = segmentIndex;
    this.dist = dist;
  }

  public Coordinate getCoordinate() { return coord; }

  public int getSegmentIndex() { return segmentIndex; }

  public double getDistance() { return dist; }

  public int compareTo(Object obj)
  {
    EdgeIntersection other = (EdgeIntersection) obj;
    return compare(other.segmentIndex, other.dist);
  }
  /**
   * @return -1 this EdgeIntersection is located before the argument location
   * @return 0 this EdgeIntersection is at the argument location
   * @return 1 this EdgeIntersection is located after the argument location
   */
  public int compare(int segmentIndex, double dist)
  {
    if (this.segmentIndex < segmentIndex) return -1;
    if (this.segmentIndex > segmentIndex) return 1;
    if (this.dist < dist) return -1;
    if (this.dist > dist) return 1;
    return 0;
  }

  public boolean isEndPoint(int maxSegmentIndex)
  {
    if (segmentIndex == 0 && dist == 0.0) return true;
    if (segmentIndex == maxSegmentIndex) return true;
    return false;
  }

  public void print(PrintStream out)
  {
    out.print(coord);
    out.print(" seg # = " + segmentIndex);
    out.println(" dist = " + dist);
  }
  public String toString()
  {
    return coord + " seg # = " + segmentIndex + " dist = " + dist;
  }
}
