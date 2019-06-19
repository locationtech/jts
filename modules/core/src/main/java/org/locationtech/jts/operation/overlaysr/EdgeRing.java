package org.locationtech.jts.operation.overlaysr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.util.Assert;

public class EdgeRing {
  

  
  private OverlayEdge startEdge;
  private List<Coordinate> pts = new ArrayList<Coordinate>();
  private LinearRing ring;
  private boolean isHole;
  private Coordinate[] ringPts;
  private IndexedPointInAreaLocator locator;
  private EdgeRing shell;
  private List<EdgeRing> holes = new ArrayList<EdgeRing>(); // a list of EdgeRings which are holes in this EdgeRing

  public EdgeRing(OverlayEdge start, GeometryFactory geometryFactory) {
    startEdge = start;
    computePoints(start);
    computeRing(geometryFactory);
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
  public void setShell(EdgeRing shell) {
    this.shell = shell;
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
  public EdgeRing getShell() {
    if (isHole()) return shell;
    return this;
  }

  public void addHole(EdgeRing ring) { holes.add(ring); }

  private void computePoints(OverlayEdge start) {
    OverlayEdge de = start;
    boolean isFirstEdge = true;
    do {
      if (de == null)
        throw new TopologyException("Found null edge in ring");
      if (de.getEdgeRing() == this)
        throw new TopologyException("Edge visited twice during ring-building at " + de.getCoordinate());

      //edges.add(de);
//Debug.println(de);
//Debug.println(de.getEdge());
      OverlayLabel label = de.getLabel();
      Assert.isTrue(label.isArea());
      //mergeLabel(label);
      addPoints(de.getCoordinates(), de.isForward(), isFirstEdge);
      isFirstEdge = false;
      de.setEdgeRing(this);
      de = de.getResultNext();
    } while (de != startEdge);  
  }
  
  private void computeRing(GeometryFactory geometryFactory) {
    if (ring != null) return;   // don't compute more than once
    ringPts = toCoordinateArray(pts);
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
  
  private Coordinate[] toCoordinateArray(List<Coordinate> pts) {
    Coordinate[] coord = new Coordinate[pts.size()];
    for (int i = 0; i < pts.size(); i++) {
      coord[i] = (Coordinate) pts.get(i);
    }
    return coord;
  }
  
  protected void addPoints(Coordinate[] edgePts, boolean isForward, boolean isFirstEdge)
  {
    if (isForward) {
      int startIndex = 1;
      if (isFirstEdge) startIndex = 0;
      for (int i = startIndex; i < edgePts.length; i++) {
        pts.add(edgePts[i]);
      }
    }
    else { // is backward
      int startIndex = edgePts.length - 2;
      if (isFirstEdge) startIndex = edgePts.length - 1;
      for (int i = startIndex; i >= 0; i--) {
        pts.add(edgePts[i]);
      }
    }
  }

  /**
   * Find the innermost enclosing shell EdgeRing containing the argument EdgeRing, if any.
   * The innermost enclosing ring is the <i>smallest</i> enclosing ring.
   * The algorithm used depends on the fact that:
   * <br>
   *  ring A contains ring B iff envelope(ring A) contains envelope(ring B)
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
  public EdgeRing findEdgeRingContaining(List erList)
  {
    LinearRing testRing = this.getRing();
    Envelope testEnv = testRing.getEnvelopeInternal();
    Coordinate testPt = testRing.getCoordinateN(0);

    EdgeRing minRing = null;
    Envelope minRingEnv = null;
    for (Iterator it = erList.iterator(); it.hasNext(); ) {
      EdgeRing tryEdgeRing = (EdgeRing) it.next();
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
}
