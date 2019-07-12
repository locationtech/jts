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
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.topology.Position;
import org.locationtech.jts.util.Debug;

/**
 * Represents a single edge in a topology graph,
 * carrying the location label derived from the parent geometr(ies).
 * 
 * @author mdavis
 *
 */
public class Edge {
  
  public static boolean isValidPoints(Coordinate[] pts) {
    if (pts.length < 2) return false;
    if (pts[0].equals2D(pts[1])) return false;
    // TODO: is pts > 2 with equal points ever expected?
    if (pts.length > 2) {
      if ( pts[ pts.length-1 ].equals2D(pts[ pts.length - 2 ])) return false;
    }
    return true;
  }
  
  private Coordinate[] pts;
  private OverlayLabel label;
  
  private int aDim = OverlayLabel.DIM_UNKNOWN;
  private int bDim = OverlayLabel.DIM_UNKNOWN;
  private int aDepthDelta = 0;
  private int bDepthDelta = 0;

  public Edge(Coordinate[] pts, OverlayLabel lbl) {
    this.pts = pts;
    this.label = lbl;
    initEdgeLabel(lbl);
  }
  
  public Coordinate[] getCoordinates() {
    return pts;
  }
  
  public OverlayLabel getLabel() {
    return label;
  }

  public Coordinate getCoordinate(int index) {
    return pts[index];
  }
  
  public int size() {
    return pts.length;
  }
  
  public boolean direction() {
    Coordinate[] pts = getCoordinates();
    if (pts.length < 2) {
      throw new IllegalStateException("Edge must have >= 2 points");
    }
    Coordinate p0 = pts[0];
    Coordinate p1 = pts[1];
    
    Coordinate pn0 = pts[pts.length - 1];
    Coordinate pn1 = pts[pts.length - 2];
    
    int cmp = 0;
    int cmp0 = p0.compareTo(pn0);
    if (cmp0 != 0) cmp = cmp0;
    
    if (cmp == 0) {
      int cmp1 = p1.compareTo(pn1);
      if (cmp1 != 0) cmp = cmp1;
    }
    
    if (cmp == 0) {
      throw new IllegalStateException("Edge direction cannot be determined because endpoints are equal");
    }
    
    return cmp == -1 ? true : false;
  }

  /**
   * Compares two coincident edges to determine
   * whether they have the same or opposite direction.
   * 
   * @param edge1 an edge
   * @param edge2 an edge
   * @return true if the edges have the same direction, false if not
   */
  public boolean relativeDirection(Edge edge2) {
    // assert: the edges match (have the same coordinates up to direction)
    if (! getCoordinate(0).equals2D(edge2.getCoordinate(0)))
      return false;
    if (! getCoordinate(1).equals2D(edge2.getCoordinate(1)))
      return false;
    return true;
  }
  
  public OverlayLabel getMergedLabel() {
    OverlayLabel lbl = new OverlayLabel();
    
    /**
     * if location for an input geom is not known, 
     * the label will have dimension = DIM_NOT_PART = DIM_UNKNOWN.
     * 
     * The effective dimension of an edge is:
     * - A, if the edge is an area boundary, indicated by depthDelta != 0
     * - L, if the edge is from a line.  In this case the inArea location
     *      is known to be INTERIOR
     * - L, if the edge is an area collapse.  In this case the inArea location
     *      is not known (since the collapse could be inside or outside the 
     *      resultant reduced area)
     *
     */
    
    // -----  A label
    if (isKnown(0)) {
      int aDimMerge = aDepthDelta == 0 ? Dimension.L : Dimension.A;
      boolean aIsCollape = aDepthDelta == 0 && aDim == Dimension.A;
      switch (aDimMerge) {
      case Dimension.A: 
        lbl.setAreaBoundary(0, locationLeft(aDepthDelta), locationRight(aDepthDelta));
        break;
      case Dimension.L:
        int lineLoc = aIsCollape ? OverlayLabel.LOC_UNKNOWN : Location.INTERIOR;
        lbl.setLine(0, lineLoc);
        break;
      }
    }
    
    // -----  B label
    if (isKnown(1)) {
      int bDimMerge = bDepthDelta == 0 ? Dimension.L : Dimension.A;
      boolean bIsCollape = bDepthDelta == 0 && bDim == Dimension.A;
      switch (bDimMerge) {
      case Dimension.A: 
        lbl.setAreaBoundary(1, locationLeft(bDepthDelta), locationRight(bDepthDelta));
        break;
      case Dimension.L:
        int lineLoc = bIsCollape ? OverlayLabel.LOC_UNKNOWN : Location.INTERIOR;
        lbl.setLine(1, lineLoc);
        break;
      }
    }
    return lbl;
  }
  
  private boolean isKnown(int index) {
    if (index == 0) 
      return aDim != OverlayLabel.DIM_UNKNOWN;
    return bDim != OverlayLabel.DIM_UNKNOWN;
  }

  private int locationRight(int depthDelta) {
    int delSign = delSign(depthDelta);
    switch (delSign) {
    case 0: return OverlayLabel.LOC_UNKNOWN;
    case 1: return Location.INTERIOR;
    case -1: return Location.EXTERIOR;
    }
    throw new IllegalStateException("found illegal depth delta " + depthDelta);
  }

  private int locationLeft(int depthDelta) {
    // TODO: is it always safe to ignore larger depth deltas?
    int delSign = delSign(depthDelta);
    switch (delSign) {
    case 0: return OverlayLabel.LOC_UNKNOWN;
    case 1: return Location.EXTERIOR;
    case -1: return Location.INTERIOR;
    }
    throw new IllegalStateException("found illegal depth delta " + depthDelta + " in edge " + this);

  }

  private static int delSign(int depthDel) {
    if(depthDel > 0) return 1;
    if (depthDel < 0) return -1;
    return 0;
  }

  private static int depthDelta(OverlayLabel label, int index) {
    int locL = label.getLocation(index, Position.LEFT);
    int locR = label.getLocation(index, Position.RIGHT);
    
    int delta = locationDepth(locR) - locationDepth(locL);
    return delta;
  }

  private static int locationDepth(int location) {
    switch (location) {
    case Location.EXTERIOR: return 0;
    case Location.INTERIOR: return 1;
    }
    return -1;
  }

  private void initEdgeLabel(OverlayLabel label) {
    aDim = label.dimension(0);
    aDepthDelta = depthDelta(label, 0);
    
    bDim = label.dimension(1);
    bDepthDelta = depthDelta(label, 1);
  }
  
  public void mergeEdge(Edge edge) {
    if (edge.aDim > aDim) aDim = edge.aDim;
    if (edge.bDim > bDim) bDim = edge.bDim;
     
    boolean relDir = relativeDirection(edge);
    int flipFactor = relDir ? 1 : -1;
    aDepthDelta += flipFactor * edge.aDepthDelta;
    bDepthDelta += flipFactor * edge.bDepthDelta;
    if (aDepthDelta > 1) {
      Debug.println(this);
    }
  }

  public String toString() {
    Coordinate orig = pts[0];
    Coordinate dest = pts[pts.length - 1];
    String dirPtStr = (pts.length > 2)
        ? ", " + WKTWriter.format(pts[1])
            : "";
    String ptsStr = WKTWriter.format(orig)
        + dirPtStr
        + " .. " + WKTWriter.format(dest);
    String aInfo = "A:" + aDepthDelta + OverlayLabel.dimensionSymbol(aDim);
    String bInfo = "B:" + bDepthDelta + OverlayLabel.dimensionSymbol(bDim);
    return "Edge( " + ptsStr
        + " ) " + aInfo + "/" + bInfo;

  }
}
