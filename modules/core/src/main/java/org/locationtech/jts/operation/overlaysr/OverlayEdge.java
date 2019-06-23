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
  public void computeLabelling() {
    propagateAreaLabels(0);
    propagateAreaLabels(1);
    nodeMergeSymLabels();
  }

  /**
   * Scan around node CCW and propagate labels for given geometry index
   * until fully populated.
   * 
   * @param geomIndex index of the geometry to propagate
   */
  private void propagateAreaLabels(int geomIndex) {
   // initialize currLoc to location of last L side (if any)
   int currLoc = findLocStart(geomIndex);

    // no labelled sides found, so nothing to propagate
    if (currLoc == Location.NONE) return;

    OverlayEdge e = this;
Debug.println("\nPropagating labels for index " + geomIndex + " : " + this);
Debug.print("BEFORE: " + e.toStringNode());
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
          if (rightLoc != currLoc) {
            Debug.println("side location conflict: " 
          + Location.toLocationSymbol(rightLoc) + " <> " + Location.toLocationSymbol(currLoc) 
          + " for " + e);
            throw new TopologyException("side location conflict", e.getCoordinate());
          }
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
    Debug.print("AFTER: " + e.toStringNode());
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
      label.mergeFlip(labelSym);
      labelSym.mergeFlip(label);
      e = (OverlayEdge) e.oNext();
    } while (e != this);
  }
  
  private final int STATE_SCAN_FOR_INCOMING = 1;
  private final int STATE_LINK_TO_OUTGOING = 2;
  
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
Debug.println("\n------  Linking... ");
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
    + " " + getLabel() 
    + (isInResult() ? " Res" : "-") + "/" + (symOE().isInResult() ? " Res" : "-")
    ;
  }



}
