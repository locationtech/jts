package org.locationtech.jts.operation.overlaysr;

import java.util.Comparator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.TopologyException;
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
  
  private SegmentString edge;
  
  /**
   * <code>true</code> indicates direction is forward along segString
   * <code>false</code> is reverse direction
   * The label must be interpreted accordingly.
   */
  private boolean direction;
  private Coordinate dirPt;
  private OverlayLabel label;

  private boolean isInResult = false;

  public OverlayEdge(Coordinate orig, Coordinate dirPt, boolean direction, OverlayLabel label, SegmentString segString) {
    super(orig);
    this.dirPt = dirPt;
    this.direction = direction;
    this.edge = segString;
    this.label = label;
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
    return edge.getCoordinates();
  }
  
  public boolean isInResult() {
    return isInResult;
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
    int currLoc = findLocStart(geomIndex);

    // no labelled sides found, so no labels to propagate
    if (currLoc == Location.NONE) return;

    for (OverlayEdge e = this; edge != this; e = (OverlayEdge) e.oNext()) {
      OverlayLabel label = e.getLabel();
      // set null ON values to be in current location
      if (label.getLocation(geomIndex, Position.ON) == Location.NONE)
          label.setLocation(geomIndex, Position.ON, currLoc);
      // set side labels (if any)
      if (label.isArea(geomIndex)) {
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
          /** RHS is null - LHS must be null too.
           *  This must be an edge from the other geometry, which has no location
           *  labelling for this geometry.  This edge must lie wholly inside or outside
           *  the other geometry (which is determined by the current location).
           *  Assign both sides to be the current location.
           */
          Assert.isTrue(label.getLocation(geomIndex, Position.LEFT) == Location.NONE, "found single null side");
          label.setLocation(geomIndex, Position.RIGHT, currLoc);
          label.setLocation(geomIndex, Position.LEFT, currLoc);
        }
      }
    }

  }

  private int findLocStart(int geomIndex) {
    int locStart = Location.NONE;
    // Edges are stored in CCW order around the node.
    // As we move around the ring we move from the R to the L side of the edge
    for (OverlayEdge edge = this; edge != this; edge = (OverlayEdge) edge.oNext()) {
      OverlayLabel label = edge.getLabel();
      if (label.isArea(geomIndex) && label.getLocation(geomIndex, Position.LEFT) != Location.NONE)
        locStart = label.getLocation(geomIndex, Position.LEFT);
    }
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

  public void mergeSymLabels() {
    for (OverlayEdge e = this; edge != this; e = (OverlayEdge) e.oNext()) {
      OverlayLabel label = e.getLabel();
      OverlayLabel labelSym = ((OverlayEdge) e.sym()).getLabel();
      label.merge(labelSym);
      labelSym.merge(label);
      
    }
  }
}
