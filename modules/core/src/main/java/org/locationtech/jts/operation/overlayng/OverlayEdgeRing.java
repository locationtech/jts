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

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;

class OverlayEdgeRing {
  
  private OverlayEdge startEdge;
  private LinearRing ring;
  private boolean isHole;
  private Coordinate[] ringPts;
  private IndexedPointInAreaLocator locator;
  private OverlayEdgeRing shell;
  private List<OverlayEdgeRing> holes = new ArrayList<OverlayEdgeRing>(); // a list of EdgeRings which are holes in this EdgeRing

  public OverlayEdgeRing(OverlayEdge start, GeometryFactory geometryFactory) {
    startEdge = start;
    ringPts = computeRingPts(start);
    computeRing(ringPts, geometryFactory);
  }

  public LinearRing getRing() {
    return ring;
  }
  
  /**
   * Tests whether this ring is a hole.
   * @return <code>true</code> if this ring is a hole
   */
  public boolean isHole()
  {
    return isHole;
  }
  
  /**
   * Sets the containing shell ring of a ring that has been determined to be a hole.
   * 
   * @param shell the shell ring
   */
  public void setShell(OverlayEdgeRing shell) {
    this.shell = shell;
    if (shell != null) shell.addHole(this);
  }
  
  /**
   * Tests whether this ring has a shell assigned to it.
   * 
   * @return true if the ring has a shell
   */
  public boolean hasShell() {
    return shell != null;
  }
  
  /**
   * Gets the shell for this ring.  The shell is the ring itself if it is not a hole, otherwise its parent shell.
   * 
   * @return the shell for this ring
   */
  public OverlayEdgeRing getShell() {
    if (isHole()) return shell;
    return this;
  }

  public void addHole(OverlayEdgeRing ring) { holes.add(ring); }

  private Coordinate[] computeRingPts(OverlayEdge start) {
    OverlayEdge edge = start;
    CoordinateList pts = new CoordinateList();
    do {
      if (edge.getEdgeRing() == this)
        throw new TopologyException("Edge visited twice during ring-building at " + edge.getCoordinate(), edge.getCoordinate());

      //edges.add(de);
//Debug.println(de);
//Debug.println(de.getEdge());
      
      // only valid for polygonal output
      //Assert.isTrue(edge.getLabel().isBoundaryEither());
      
      edge.addCoordinates(pts);
      edge.setEdgeRing(this);
      if (edge.nextResult() == null)
        throw new TopologyException("Found null edge in ring", edge.dest());

      edge = edge.nextResult();
    } while (edge != start);
    pts.closeRing();
    return pts.toCoordinateArray();
  }
  
  private void computeRing(Coordinate[] ringPts, GeometryFactory geometryFactory) {
    if (ring != null) return;   // don't compute more than once
    ring = geometryFactory.createLinearRing(ringPts);
    isHole = Orientation.isCCW(ring.getCoordinates());
  }

  /**
   * Computes the list of coordinates which are contained in this ring.
   * The coordinates are computed once only and cached.
   *
   * @return an array of the {@link Coordinate}s in this ring
   */
  private Coordinate[] getCoordinates()
  {
    return ringPts;
  }
  
  /**
   * Finds the innermost enclosing shell OverlayEdgeRing
   * containing this OverlayEdgeRing, if any.
   * The innermost enclosing ring is the <i>smallest</i> enclosing ring.
   * The algorithm used depends on the fact that:
   * <br>
   *  ring A contains ring B if envelope(ring A) contains envelope(ring B)
   * <br>
   * This routine is only safe to use if the chosen point of the hole
   * is known to be properly contained in a shell
   * (which is guaranteed to be the case if the hole does not touch its shell)
   * <p>
   * To improve performance of this function the caller should 
   * make the passed shellList as small as possible (e.g.
   * by using a spatial index filter beforehand).
   * 
   * @return containing EdgeRing, if there is one
   * or null if no containing EdgeRing is found
   */
  public OverlayEdgeRing findEdgeRingContaining(List<OverlayEdgeRing> erList)
  {
    LinearRing testRing = this.getRing();
    Envelope testEnv = testRing.getEnvelopeInternal();
    Coordinate testPt = testRing.getCoordinateN(0);

    OverlayEdgeRing minRing = null;
    Envelope minRingEnv = null;
    for (OverlayEdgeRing tryEdgeRing: erList ) {
      LinearRing tryRing = tryEdgeRing.getRing();
      Envelope tryShellEnv = tryRing.getEnvelopeInternal();
      // the hole envelope cannot equal the shell envelope
      // (also guards against testing rings against themselves)
      if (tryShellEnv.equals(testEnv)) continue;
      
      // hole must be contained in shell
      if (! tryShellEnv.contains(testEnv)) continue;
      
      testPt = CoordinateArrays.ptNotInList(testRing.getCoordinates(), tryEdgeRing.getCoordinates());
 
      boolean isContained = tryEdgeRing.isInRing(testPt);

      // check if the new containing ring is smaller than the current minimum ring
      if (isContained) {
        if (minRing == null
            || minRingEnv.contains(tryShellEnv)) {
          minRing = tryEdgeRing;
          minRingEnv = minRing.getRing().getEnvelopeInternal();
        }
      }
    }
    return minRing;
  }
  
  private PointOnGeometryLocator getLocator() {
    if (locator == null) {
      locator = new IndexedPointInAreaLocator(getRing());
    }
    return locator;
  }
  
  public boolean isInRing(Coordinate pt) {
    /**
     * Use an indexed point-in-polygon for performance
     */
    return Location.EXTERIOR != getLocator().locate(pt);
    //return PointLocation.isInRing(pt, getCoordinates());
  }

  public Coordinate getCoordinate() {
    return ringPts[0];
  }

  /**
   * Computes the {@link Polygon} formed by this ring and any contained holes.
   *
   * @return the {@link Polygon} formed by this ring and its holes.
   */
  public Polygon toPolygon(GeometryFactory factory)
  {
    LinearRing[] holeLR = null;
    if (holes != null) {
      holeLR = new LinearRing[holes.size()];
      for (int i = 0; i < holes.size(); i++) {
        holeLR[i] = (LinearRing) holes.get(i).getRing();
      }
    }
    Polygon poly = factory.createPolygon(ring, holeLR);
    return poly;
  }

  public OverlayEdge getEdge() {
    return startEdge;
  }
}
