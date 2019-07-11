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
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.io.WKTWriter;

public class OverlayEdge extends HalfEdge {

  /**
   * Creates a single OverlayEdge.
   * 
   * @param pts
   * @param lbl 
   * @param direction
   * 
   * @return a new edge based on the given coordinates and direction
   */
  public static OverlayEdge createEdge(Coordinate[] pts, OverlayLabel lbl, boolean direction)
  {
    Coordinate origin;
    Coordinate dirPt;
    if (direction) {
      origin = pts[0];
      dirPt = pts[1];
    }
    else {
      int ilast = pts.length - 1;
      origin = pts[ilast];
      dirPt = pts[ilast-1];
    }
    return new OverlayEdge(origin, dirPt, direction, lbl, pts);
  }

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
  
  private Coordinate[] pts;
  
  /**
   * <code>true</code> indicates direction is forward along segString
   * <code>false</code> is reverse direction
   * The label must be interpreted accordingly.
   */
  private boolean direction;
  private Coordinate dirPt;
  private OverlayLabel label;

  private boolean isInResult = false;
  private boolean isVisited;

  /**
   * Link to next edge in the result.
   * The origin of the edge is the dest of this edge.
   */
  private OverlayEdge nextResultEdge;

  private EdgeRing edgeRing;

  private MaximalEdgeRing maxEdgeRing;

  private OverlayEdge nextResultMaxEdge;

  public OverlayEdge(Coordinate orig, Coordinate dirPt, boolean direction, OverlayLabel label, Coordinate[] pts) {
    super(orig);
    this.dirPt = dirPt;
    this.direction = direction;
    this.pts = pts;
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
    return pts;
  }
  
  public Coordinate[] getCoordinatesOriented() {
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
  
  void setResultNext(OverlayEdge e) {
    // Assert: e.orig() == this.dest();
    nextResultEdge = e;
  }
  
  public OverlayEdge nextResult() {
    return nextResultEdge;
  }
  
  public boolean isResultLinked() {
    return nextResultEdge != null;
  }
  
  void setResultNextMax(OverlayEdge e) {
    // Assert: e.orig() == this.dest();
    nextResultMaxEdge = e;
  }
  
  public OverlayEdge nextResultMax() {
    return nextResultMaxEdge;
  }

  public boolean isResultMaxLinked() {
    return nextResultMaxEdge != null;
  }
  
  public boolean isVisited() {
    return isVisited;
  }
  
  public void setVisited(boolean b) {
    isVisited = true;
  }
  
  public void setEdgeRing(EdgeRing edgeRing) {
    this.edgeRing = edgeRing;
  } 
  public EdgeRing getEdgeRing() {
    return edgeRing;
  } 
  
  public MaximalEdgeRing getEdgeRingMax() {
    return maxEdgeRing;
  }

  public void setEdgeRingMax(MaximalEdgeRing maximalEdgeRing) {
    maxEdgeRing = maximalEdgeRing;
  }
  
  public void setLocationBothSides(int geomIndex, int loc) {
    getLabel().setLocationBothSides(geomIndex, loc);
    symOE().getLabel().setLocationBothSides(geomIndex, loc);
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

  public void mergeSymLabels() {
    OverlayLabel label = getLabel();
    OverlayLabel labelSym = symOE().getLabel();
    label.mergeFlip(labelSym);
    labelSym.mergeFlip(label);
  }

  public String toString() {
    Coordinate orig = orig();
    Coordinate dest = dest();
    String dirPtStr = (pts.length > 2)
        ? ", " + WKTWriter.format(directionPt())
            : "";
    return "OE( "+ WKTWriter.format(orig)
        + dirPtStr
        + " .. " + WKTWriter.format(dest)
        + " ) " 
        + label + (isInResult ? " Res" : "")
        + " / Sym: " + symOE().getLabel()+ (symOE().isInResult() ? " Res" : "")
        ;
  }



}
