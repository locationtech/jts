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
package org.locationtech.jts.operation.overlaysr;

import java.util.Comparator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geomgraph.DirectedEdge;
import org.locationtech.jts.geomgraph.Label;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Debug;

public class OverlayEdge extends HalfEdge {

  /**
   * Gets a {@link Comparator} which sorts by the origin Coordinates.
   * 
   * @return a Comparator sorting by origin coordinate
   */
  public static Comparator<OverlayEdge> nodeComparator() {
    return new Comparator<OverlayEdge>() {
      @Override
      public int compare(OverlayEdge e1, OverlayEdge e2) {
        return e1.orig().compareTo(e2.orig());
      }
    };
  }
  
  private SegmentString segString;
  
  /**
   * <code>true</code> indicates direction is forward along segString
   * <code>false</code> is reverse direction
   * The label must be interpreted accordingly.
   */
  private boolean direction;
  private Coordinate dirPt;
  private OverlayLabel label;

  private boolean isInResult = false;

  /**
   * Link to next edge in the result.
   * The origin of the edge is the dest of this edge.
   */
  private OverlayEdge nextResultEdge;

  private EdgeRing edgeRing;

  public OverlayEdge(Coordinate orig, Coordinate dirPt, boolean direction, OverlayLabel label, SegmentString segString) {
    super(orig);
    this.dirPt = dirPt;
    this.direction = direction;
    this.segString = segString;
    this.label = label;
  }

  public boolean isForward() {
    return direction;
  }
  public Coordinate directionPt() {
    return dirPt;
  }
  
  public OverlayLabel getLabel() {
    return label;
  }

  public Coordinate getCoordinate() {
    return orig();
  }
  
  public Coordinate[] getCoordinates() {
    return segString.getCoordinates();
  }
  
  public Coordinate[] getCoordinatesOriented() {
    Coordinate[] pts = segString.getCoordinates();
    if (direction) {
      return pts;
    }
    Coordinate[] copy = pts.clone();
    CoordinateArrays.reverse(copy);
    return copy;
  }
  
  public OverlayEdge symOE() {
    return (OverlayEdge) sym();
  }
  
  public OverlayEdge oNextOE() {
    return (OverlayEdge) oNext();
  }
  
  public boolean isInResult() {
    return isInResult;
  }
  
  public void removeFromResult() {
    isInResult = false;
  }
  
  private void setResultNext(OverlayEdge e) {
    // Assert: e.orig() == this.dest();
    nextResultEdge = e;
  }
  
  public OverlayEdge getResultNext() {
    return nextResultEdge;
  }
  
  public boolean isResultLinked() {
    return nextResultEdge != null;
  }
  
  public void setEdgeRing(EdgeRing edgeRing) {
    this.edgeRing = edgeRing;
  } 
  public EdgeRing getEdgeRing() {
    return edgeRing;
  } 
  /**
   * Scan around node CCW and propagate labels until fully populated.
   * @param node node to compute labelling for
   */
  public void nodeComputeLabelling() {
    nodePropagateAreaLabels(0);
    nodePropagateAreaLabels(1);
    
    // this now is not needed - done during propagation
    //nodeMergeSymLabels();
  }

  /**
   * Scans around a node CCW, propagating the labels
   * for a given area geometry to all edges (and their sym)
   * with unknown locations for that geometry.
   * 
   * @param geomIndex the geometry to propagate locations for
   */
  private void nodePropagateAreaLabels(int geomIndex) {
    OverlayEdge eStart = nodeFindPropStartEdge(geomIndex);
    // no labelled edge found, so nothing to propagate
    if ( eStart == null )
      return;
    
    // initialize currLoc to location of L side
    int currLoc = eStart.getLabel().getLocation(geomIndex, Position.LEFT);
    OverlayEdge e = eStart.oNextOE();

    Debug.println("\npropagateAreaLabels geomIndex = " + geomIndex + " : " + eStart);
    Debug.print("BEFORE: " + eStart.toStringNode());
    
    do {
      OverlayLabel label = e.getLabel();
      /**
       * If location is unknown 
       * they are all set to current location
       */
      if ( ! label.hasLocation(geomIndex) ) {
        e.setLocationAreaBoth(geomIndex, currLoc);
      }
      else {
        /**
         *  Location is known, so update curr loc
         *  (which may change moving from R to L across the edge
         */
        int locRight = e.getLabel().getLocation(geomIndex, Position.RIGHT);
        if (locRight != currLoc) {
          Debug.println("side location conflict: edge R loc " 
        + Location.toLocationSymbol(locRight) + " <>  curr loc " + Location.toLocationSymbol(currLoc) 
        + " for " + e);
          throw new TopologyException("side location conflict", e.getCoordinate());
        }
        int locLeft = e.getLabel().getLocation(geomIndex, Position.LEFT);
        if (locLeft == Location.NONE) {
          Assert.shouldNeverReachHere("found single null side at " + e);
        }
        currLoc = locLeft;
      }
      e = e.oNextOE();
    } while (e != eStart);
    Debug.print("AFTER: " + eStart.toStringNode());
  }
  
  public void setLocationAreaBoth(int geomIndex, int loc) {
    getLabel().setLocationArea(geomIndex, loc, loc, loc);
    symOE().getLabel().setLocationArea(geomIndex, loc, loc, loc);
  }

  /**
   * Finds a node edge which has a labelling for this geom.
   * 
   * @param geomIndex
   * @return labelled edge, or null if no edges are labelled
   */
  private OverlayEdge nodeFindPropStartEdge(int geomIndex) {
    OverlayEdge e = this;
    do {
      OverlayLabel label = e.getLabel();
      if (label.hasLocation(geomIndex)) {
        return e;
      }
      e = (OverlayEdge) e.oNext();
    } while (e != this);
    return null;
  }

  public void markInResultArea(int overlayOpCode) {
    if (label.isArea()
        //&& ! label.isInteriorArea()
        && OverlaySR.isResultOfOp(
              label.getLocation(0, Position.RIGHT),
              label.getLocation(1, Position.RIGHT),
              overlayOpCode)) {
      isInResult  = true;  
    }
  }

  public void nodeMergeSymLabels() {
    Debug.println("\nnodeMergeSymLabels-------- ");
    Debug.println("BEFORE: " + this.toStringNode());
    OverlayEdge e = this;
    do {
      e.mergeSymLabels();
      e = (OverlayEdge) e.oNext();
    } while (e != this);
    Debug.println("AFTER: " + this.toStringNode());
  }

  public void mergeSymLabels() {
    OverlayLabel label = getLabel();
    OverlayLabel labelSym = symOE().getLabel();
    label.mergeFlip(labelSym);
    labelSym.mergeFlip(label);
  }
  
  private final int STATE_SCAN_FOR_INCOMING = 1;
  private final int STATE_LINK_TO_OUTGOING = 2;

  private boolean isVisited;
  
  /**
   * Traverses the star of OverlayEdges around this node
   * and links result edges together.
   * To link two edges, the <code>resNext</code> pointer 
   * for an <b>incoming</b> result edge
   * is set to the next <b>outgoing</b> result edge.
   * <p>
   * Edges are linked only if:
   * <ul>
   * <li>they belong to an area (i.e. they have sides)
   * <li>they are marked as being in the result
   * </ul>
   * <p>
   * Edges are linked in CCW order (the order they are stored).
   * This means that rings have their face on the Right
   * (in other words,
   * the topological location of the face is given by the RHS label of the DirectedEdge).
   * This produces rings with CW orientation.
   * <p>
   * PRECONDITIONS: 
   * - This edge is in the result
   * - This edge is not yet linked
   * - The edge and its sym are NOT both marked as being in the result
   */
  public void linkOriginResultEdges()
  {
    Assert.isTrue(this.isInResult(), "Attempt to link non-result edge");
    Assert.isTrue(! this.symOE().isInResult(), "Found both half-edges in result");

    OverlayEdge currResultIn = null;
    int state = STATE_SCAN_FOR_INCOMING;
    // link edges in CCW order
    OverlayEdge endOut = this.oNextOE();
    OverlayEdge currOut = endOut;
Debug.println("\n------  Linking result area edges... ");
Debug.print("BEFORE: " + this.toStringNode());
    do {
      OverlayEdge currIn = currOut.symOE();
 
      // skip edges not in a result area
      //if (! nextOut.getLabel().isArea()) continue;

      switch (state) {
      case STATE_SCAN_FOR_INCOMING:
        if (! currIn.isInResult()) break;
        currResultIn = currIn;
        Debug.println("Found result in-edge:  " + currResultIn);
        state = STATE_LINK_TO_OUTGOING;
        break;
      case STATE_LINK_TO_OUTGOING:
        if (! currOut.isInResult()) break;
        currResultIn.setResultNext(currOut);
        Debug.println("Linked:  " + currResultIn + " -> " + currOut);
        state = STATE_SCAN_FOR_INCOMING;
        break;
      }
      currOut = currOut.oNextOE();
    } while (currOut != endOut);
    Debug.print("AFTER: " + this.toStringNode());
    if (state == STATE_LINK_TO_OUTGOING) {
//Debug.print(firstOut == null, this);
      throw new TopologyException("no outgoing dirEdge found", getCoordinate());
    }
    
  }

  public String toString() {
    Coordinate orig = orig();
    Coordinate dest = dest();
    String dirPtStr = (segString.size() > 2)
        ? ", " + WKTWriter.format(directionPt())
            : "";
    return "OE( "+ WKTWriter.format(orig)
        + dirPtStr
        + ".." + WKTWriter.format(dest)
        + " ) " + label 
        + " Sym: " + symOE().getLabel()
        + (isInResult ? " Res" : "");
  }

  public String toStringNode() {
    Coordinate orig = orig();
    Coordinate dest = dest();
    StringBuilder sb = new StringBuilder();
    sb.append("Node( "+WKTWriter.format(orig) + " )" + "\n");
    OverlayEdge e = this;
    do {
      sb.append("  -> " + e);
      if (e.isResultLinked()) {
        sb.append(" Link: ");
        sb.append(e.getResultNext());
      }
      sb.append("\n");
      e = e.oNextOE();
    } while (e != this);
    return sb.toString(); 
  }

  private String toStringNodeEdge() {
    return "  -> (" + WKTWriter.format(dest()) 
    + " " + getLabel() + " Sym: " + symOE().getLabel()
    + (isInResult() ? " Res" : "-") + "/" + (symOE().isInResult() ? " Res" : "-")
    ;
  }

  public boolean isVisited() {
    return isVisited;
  }
  
  public void setVisited(boolean b) {
    isVisited = true;
  }

}
