/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jts.noding;

import java.io.PrintStream;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Represents an intersection point between two {@link SegmentString}s.
 *
 * @version 1.7
 */
public class SegmentNode
    implements Comparable
{
  private final NodedSegmentString segString;
  public final Coordinate coord;   // the point of intersection
  public final int segmentIndex;   // the index of the containing line segment in the parent edge
  private final int segmentOctant;
  private final boolean isInterior;

  public SegmentNode(NodedSegmentString segString, Coordinate coord, int segmentIndex, int segmentOctant) {
    this.segString = segString;
    this.coord = new Coordinate(coord);
    this.segmentIndex = segmentIndex;
    this.segmentOctant = segmentOctant;
    isInterior = ! coord.equals2D(segString.getCoordinate(segmentIndex));
  }

  /**
   * Gets the {@link Coordinate} giving the location of this node.
   * 
   * @return the coordinate of the node
   */
  public Coordinate getCoordinate() 
  {
    return coord;
  }
  
  public boolean isInterior() { return isInterior; }

  public boolean isEndPoint(int maxSegmentIndex)
  {
    if (segmentIndex == 0 && ! isInterior) return true;
    if (segmentIndex == maxSegmentIndex) return true;
    return false;
  }

  /**
   * @return -1 this SegmentNode is located before the argument location;
   * 0 this SegmentNode is at the argument location;
   * 1 this SegmentNode is located after the argument location
   */
  public int compareTo(Object obj)
  {
    SegmentNode other = (SegmentNode) obj;

    if (segmentIndex < other.segmentIndex) return -1;
    if (segmentIndex > other.segmentIndex) return 1;

    if (coord.equals2D(other.coord)) return 0;

    return SegmentPointComparator.compare(segmentOctant, coord, other.coord);
    //return segment.compareNodePosition(this, other);
  }

  public void print(PrintStream out)
  {
    out.print(coord);
    out.print(" seg # = " + segmentIndex);
  }
}
