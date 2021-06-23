/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.valid;

import java.util.List;

import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.strtree.STRtree;

/**
 * Tests whether any holes of a Polygon are
 * nested inside another hole, using a spatial
 * index to speed up the comparisons.
 * <p>
 * Assumes that the holes do not cross and have no collinear segments
 * (so they are properly nested, and there are no duplicate holes).
 * This class does not handle the situation 
 * where every vertex of a hole touches another hole surrounding it.  
 * This is invalid because it disconnects the polygon interior.
 * The disconnected interior condition must be checked elsewhere. 
 *
 * @version 1.7
 */
class IndexedNestedHoleTester
{
  private Polygon polygon;
  private SpatialIndex index;
  private Coordinate nestedPt;

  public IndexedNestedHoleTester(Polygon poly)
  {
    this.polygon = poly;
    loadIndex();
  }

  private void loadIndex()
  {
    index = new STRtree();

    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) polygon.getInteriorRingN(i);
      Envelope env = hole.getEnvelopeInternal();
      index.insert(env, hole);
    }
  }

  /**
   * Gets a point on a nested hole, if one exists.
   * 
   * @return a point on a nested hole, or null if none are nested
   */
  public Coordinate getNestedPoint() { return nestedPt; }

  /**
   * Tests if any hole is nested (contained) within another hole.
   * This is invalid.
   * The nested point will be set to reflect this.
   * @return true if some hole is nested
   */
  public boolean isNested()
  {
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) polygon.getInteriorRingN(i);

      List<LinearRing> results = index.query(hole.getEnvelopeInternal());
      for (LinearRing testHole : results) {
        if (hole == testHole)
          continue;

        /**
         * Hole is not covered by test hole, so cannot be nested
         */
        if (! testHole.getEnvelopeInternal().covers( hole.getEnvelopeInternal()) )
          continue;

        if (isHoleNested(hole, testHole))
          return true;
      }
    }
    return false;
  }

  private boolean isHoleNested(LinearRing hole, LinearRing testHole) {
    Coordinate[] testPts = testHole.getCoordinates();
    for (int i = 0; i < hole.getNumPoints(); i++) {
      Coordinate holePt = hole.getCoordinateN(i);
      int holeLoc = PointLocation.locateInRing(holePt, testPts);
      switch (holeLoc) {
      case Location.EXTERIOR: 
        return false;
      case Location.INTERIOR:
        nestedPt = holePt;
        return true;
      }
      /**
       * The holePt is on the boundary of the test hole,
       * so keep trying points until a non-boundary point is found.
       */
    }
    /**
     * No non-boundary point has been found, so this result may be incorrect.
     * This indicates an invalid disconnected interior situation,
     * which must be checked elsewhere.
     */
    return false;
  }


}
