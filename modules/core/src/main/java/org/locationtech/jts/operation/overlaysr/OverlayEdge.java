package org.locationtech.jts.operation.overlaysr;

import java.util.Comparator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geomgraph.DirectedEdge;
import org.locationtech.jts.geomgraph.Label;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.util.Assert;

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
  
  public void setEdgeRing(EdgeRing edgeRing) {
    this.edgeRing = edgeRing;
  } 
  public EdgeRing getEdgeRing() {
    return edgeRing;
  } 
  /**
   * Scan around node and propagate labels until fully populated.
   * @param node node to compute labelling for
   */
  public void computeLabelling() {
    propagateAreaLabels(0);
    propagateAreaLabels(1);
  }

  private void propagateAreaLabels(int geomIndex) {
   // initialize currLoc to location of last L side (if any)
   int currLoc = findLocStart(geomIndex);

    // no labelled sides found, so nothing to propagate
    if (currLoc == Location.NONE) return;

    OverlayEdge e = this;
    do {
      OverlayLabel label = e.getLabel();
      // set null ON values to be in current location
      if (! label.hasLocation(geomIndex, Position.ON))
          label.setLocation(geomIndex, Position.ON, currLoc);
      // set side labels (if any)
      if (label.isArea()) {
        int leftLoc   = label.getLocation(geomIndex, Position.LEFT);
        int rightLoc  = label.getLocation(geomIndex, Position.RIGHT);
        // if there is a right location, that is the next location to propagate
        if (rightLoc != Location.NONE) {
//Debug.print(rightLoc != currLoc, this);
          if (rightLoc != currLoc)
            throw new TopologyException("side location conflict", e.getCoordinate());
          if (leftLoc == Location.NONE) {
            Assert.shouldNeverReachHere("found single null side (at " + e.getCoordinate() + ")");
          }
          currLoc = leftLoc;
        }
        else {
          /** 
           * RHS is null - LHS must be null too.
           * This must be an edge from the other geometry, which has no location
           * labelling for this geometry.  This edge must lie wholly inside or outside
           * the other geometry (which is determined by the current location).
           * Assign both sides to be the current location.
           */
          Assert.isTrue(label.getLocation(geomIndex, Position.LEFT) == Location.NONE, "found single null side");
          label.setLocationBothSides(geomIndex, currLoc);
        }
      }
      e = (OverlayEdge) e.oNext();
    } while (e != this);
  }

  private int findLocStart(int geomIndex) {
    int locStart = Location.NONE;
    // Edges are stored in CCW order around the node.
    // As we move around the ring we move from the R to the L side of the edge
    OverlayEdge e = this;
    do {
      OverlayLabel label = e.getLabel();
      if (label.isArea(geomIndex) 
          && label.hasLocation(geomIndex, Position.LEFT))
        locStart = label.getLocation(geomIndex, Position.LEFT);
      e = (OverlayEdge) e.oNext();
    } while (e != this);
    return locStart;
  }

  public void markInResultArea(int overlayOpCode) {
    if (label.isArea()
        //&& ! edge.isInteriorAreaEdge()
        && OverlaySR.isResultOfOp(
              label.getLocation(0, Position.RIGHT),
              label.getLocation(1, Position.RIGHT),
              overlayOpCode)) {
      isInResult  = true;  
    }
  }

  public void nodeMergeSymLabels() {
    OverlayEdge e = this;
    do {
      OverlayLabel label = e.getLabel();
      OverlayLabel labelSym = ((OverlayEdge) e.sym()).getLabel();
      label.merge(labelSym);
      labelSym.merge(label);
      e = (OverlayEdge) e.oNext();
    } while (e != this);
  }
  
  private final int STATE_SCAN_FOR_INCOMING = 1;
  private final int STATE_LINK_TO_OUTGOING = 2;

  private Object resultNext;
  
  /**
   * Traverses the star of OverlayEdges sharing this destination node
   * and links result edges together.
   * To link two edges, the <code>resNext</code> pointer for an incoming result edge
   * is set to the next outgoing result edge.
   * <p>
   * Edges are only linked if:
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
   * - This edge.sym() (incoming edge) is in the result
   * - This edge.sym() is not yet linked
   * - The edge and its sym are NOT both marked as being in the result
   */
  public void linkOriginResultEdges()
  {
    Assert.isTrue(this.isInResult(), "Attempt to link non-result edge");

    OverlayEdge currResultIn = this;
    int state = STATE_LINK_TO_OUTGOING;
    // link edges in CCW order
    OverlayEdge currOut = this;
    OverlayEdge end = currOut;
    do {
      OverlayEdge nextOut = currOut.oNextOE();
      OverlayEdge nextIn = nextOut.symOE();

      // skip edges not in a result area
      //if (! nextOut.getLabel().isArea()) continue;

      switch (state) {
      case STATE_SCAN_FOR_INCOMING:
        if (! nextIn.isInResult()) break;
        currResultIn = nextIn;
        state = STATE_LINK_TO_OUTGOING;
        break;
      case STATE_LINK_TO_OUTGOING:
        if (! nextOut.isInResult()) break;
        currResultIn.setResultNext(nextOut);
        state = STATE_SCAN_FOR_INCOMING;
        break;
      }
      currOut = nextOut;
    } while (currOut != this);
//Debug.print(this);
    if (state == STATE_LINK_TO_OUTGOING) {
//Debug.print(firstOut == null, this);
      throw new TopologyException("no outgoing dirEdge found", getCoordinate());
    }
    
  }

  public String toString() {
    Coordinate orig = orig();
    Coordinate dest = dest();
    return "OE("+orig.x + " " + orig.y
        + ", "
        + dest.x + " " + dest.y
        + ")" + label;
  }

  public boolean isResultLinked() {
    return resultNext != null;
  }


}
