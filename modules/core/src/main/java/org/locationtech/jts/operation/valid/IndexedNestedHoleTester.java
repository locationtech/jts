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
 * Assumes that the holes and polygon shell do not cross
 * (are properly nested).
 * Does not check the case where every vertex of a hole touches another
 * hole; this is invalid, and must be checked elsewhere. 
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

  public Coordinate getNestedPoint() { return nestedPt; }

  public boolean isNested()
  {
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) polygon.getInteriorRingN(i);

      List results = index.query(hole.getEnvelopeInternal());
      for (int j = 0; j < results.size(); j++) {
        LinearRing testHole = (LinearRing) results.get(j);
        if (hole == testHole)
          continue;

        /**
         * Hole is not covered by in test hole,
         * so cannot be inside
         */
        if (! testHole.getEnvelopeInternal().covers( hole.getEnvelopeInternal()) )
          continue;

        if (isHoleInsideHole(hole, testHole))
          return true;
      }
    }
    return false;
  }

  private boolean isHoleInsideHole(LinearRing hole, LinearRing testHole) {
    Coordinate[] testPts = testHole.getCoordinates();
    for (int i = 0; i < hole.getNumPoints(); i++) {
      Coordinate holePt = hole.getCoordinateN(i);
      int loc = PointLocation.locateInRing(holePt, testPts);
      switch (loc) {
      case Location.EXTERIOR: return false;
      case Location.INTERIOR:
        nestedPt = holePt;
        return true;
      }
      // location is BOUNDARY, so keep trying points
    }
    return false;
  }


}
