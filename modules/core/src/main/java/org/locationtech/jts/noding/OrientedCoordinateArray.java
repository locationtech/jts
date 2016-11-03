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
package org.locationtech.jts.noding;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;

/**
 * Allows comparing {@link Coordinate} arrays
 * in an orientation-independent way.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class OrientedCoordinateArray
    implements Comparable
{
  private Coordinate[] pts;
  private boolean orientation;

  /**
   * Creates a new {@link OrientedCoordinateArray}
   * for the given {@link Coordinate} array.
   *
   * @param pts the coordinates to orient
   */
  public OrientedCoordinateArray(Coordinate[] pts)
  {
    this.pts = pts;
    orientation = orientation(pts);
  }

  /**
   * Computes the canonical orientation for a coordinate array.
   *
   * @param pts the array to test
   * @return <code>true</code> if the points are oriented forwards
   * or <code>false</code if the points are oriented in reverse
   */
  private static boolean orientation(Coordinate[] pts)
  {
    return CoordinateArrays.increasingDirection(pts) == 1;
  }

  /**
   * Compares two {@link OrientedCoordinateArray}s for their relative order
   *
   * @return -1 this one is smaller;
   * 0 the two objects are equal;
   * 1 this one is greater
   */

  public int compareTo(Object o1) {
    OrientedCoordinateArray oca = (OrientedCoordinateArray) o1;
    int comp = compareOriented(pts, orientation,
                               oca.pts, oca.orientation);
/*
    // MD - testing only
    int oldComp = SegmentStringDissolver.ptsComp.compare(pts, oca.pts);
    if ((oldComp == 0 || comp == 0) && oldComp != comp) {
      System.out.println("bidir mismatch");

      boolean orient1 = orientation(pts);
      boolean orient2 = orientation(oca.pts);
      int comp2 = compareOriented(pts, orientation,
                               oca.pts, oca.orientation);
      int oldComp2 = SegmentStringDissolver.ptsComp.compare(pts, oca.pts);
    }
    */
    return comp;
  }

  private static int compareOriented(Coordinate[] pts1,
                                     boolean orientation1,
                                     Coordinate[] pts2,
                                     boolean orientation2)
  {
    int dir1 = orientation1 ? 1 : -1;
    int dir2 = orientation2 ? 1 : -1;
    int limit1 = orientation1 ? pts1.length : -1;
    int limit2 = orientation2 ? pts2.length : -1;

    int i1 = orientation1 ? 0 : pts1.length - 1;
    int i2 = orientation2 ? 0 : pts2.length - 1;
    int comp = 0;
    while (true) {
      int compPt = pts1[i1].compareTo(pts2[i2]);
      if (compPt != 0)
        return compPt;
      i1 += dir1;
      i2 += dir2;
      boolean done1 = i1 == limit1;
      boolean done2 = i2 == limit2;
      if (done1 && ! done2) return -1;
      if (! done1 && done2) return 1;
      if (done1 && done2) return 0;
    }
  }


}
