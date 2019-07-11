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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.topology.Position;

/**
 * Represents a single edge in a topology graph,
 * carrying the location label derived from the parent geometr(ies).
 * 
 * @author mdavis
 *
 */
public class Edge {
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
    
    // if location is not known label is not populated for it
    
    // -----  A label
    if (isKnown(0)) {
      int aDimEff = aDepthDelta == 0 ? Dimension.L : Dimension.A;
      switch (aDimEff) {
      case Dimension.A: 
        lbl.setLocationArea(0, locationLeft(aDepthDelta), locationRight(aDepthDelta));
        break;
      case Dimension.L:
        lbl.setLocationLine(0, Location.INTERIOR, OverlayLabel.LOC_UNKNOWN);
      }
    }
    
    // -----  B label
    if (isKnown(1)) {
      int bDimEff = bDepthDelta == 0 ? Dimension.L : Dimension.A;
      switch (bDimEff) {
      case Dimension.A: 
        lbl.setLocationArea(1, locationLeft(bDepthDelta), locationRight(bDepthDelta));
        break;
      case Dimension.L:
        lbl.setLocationLine(1, Location.INTERIOR, OverlayLabel.LOC_UNKNOWN);
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
    switch (depthDelta) {
    case 0: return OverlayLabel.LOC_UNKNOWN;
    case 1: return Location.INTERIOR;
    case -1: return Location.EXTERIOR;
    }
    throw new IllegalStateException("found illegal depth delta " + depthDelta);
  }

  private int locationLeft(int depthDelta) {
    switch (depthDelta) {
    case 0: return OverlayLabel.LOC_UNKNOWN;
    case 1: return Location.EXTERIOR;
    case -1: return Location.INTERIOR;
    }
    throw new IllegalStateException("found illegal depth delta " + depthDelta);

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
    aDim = label.getDimension(0);
    aDepthDelta = depthDelta(label, 0);
    
    bDim = label.getDimension(1);
    bDepthDelta = depthDelta(label, 1);
  }
  
  public void mergeEdge(Edge edge) {
    if (edge.aDim > aDim) aDim = edge.aDim;
    if (edge.bDim > bDim) bDim = edge.bDim;
     
    boolean relDir = relativeDirection(edge);
    int flipFactor = relDir ? 1 : -1;
    aDepthDelta += flipFactor * edge.aDepthDelta;
    bDepthDelta += flipFactor * edge.bDepthDelta;
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
