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
  private boolean aIsHole = false;
  private boolean bIsHole = false;

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
  
  public int dimension(int geomIndex) {
    if (geomIndex == 0) return aDim;
    return bDim;
  }
  
  public OverlayLabel getMergedLabel() {
    OverlayLabel lbl = new OverlayLabel();
    computeInputLabel(lbl, 0, aDim, aDepthDelta, aIsHole);
    computeInputLabel(lbl, 1, bDim, bDepthDelta, bIsHole);
    return lbl;
  }

  /**
   * 
   * If location for an input geom is not known, 
   * the label has dimension = DIM_NOT_PART = DIM_UNKNOWN.
   * 
   * The effective dimension of an edge is:
   * - A, if the edge is an area boundary, indicated by depthDelta != 0
   * - L, if the edge is from a line.  In this case the inArea location
   *      is known to be INTERIOR
   * - L, if the edge is an area collapse.  In this case the inArea location
   *      is not known (since the collapse could be inside or outside the 
   *      resultant reduced area)

   * @param lbl
   * @param geomIndex
   * @param dim
   * @param depthDelta
   */
  /*
  private void OLDsetInputLabel(OverlayLabel lbl, int geomIndex, int dim, int depthDelta, boolean isHole) {
    if (dim == OverlayLabel.DIM_UNKNOWN) return;
    boolean isCollapse = depthDelta == 0 && dim == Dimension.A;
    
    int dimMerge = depthDelta == 0 ? Dimension.L : Dimension.A;
    switch (dimMerge) {
    case Dimension.A: 
      lbl.setToAreaBoundary(geomIndex, locationLeft(depthDelta), locationRight(depthDelta), isHole);
      break;
    case Dimension.L:
      int lineLoc = isCollapse ? OverlayLabel.LOC_UNKNOWN : Location.INTERIOR;
      lbl.setToLine(geomIndex, lineLoc, isHole);
      break;
    }
  } 
  */ 
  
  /**
   * Computes the label for an edge resulting from an input geometry.
   * 
   * <ul>
   * <li>If the edge is not part of the input, the label is left as NOT_PART
   * <li>If input is an Area and the edge is on the boundary
   * (which may include some collapses),
   * edge is marked as an AREA edge and side locations are assigned
   * <li>If input is an Area and the edge is collapsed
   * (depth delta = 0), 
   * the label is set to COLLAPSE.
   * The location will be determined later
   * by evaluating the final graph topology.
   * <li>If input is a Line edge is set to a LINE edge.
   * For line edges the line location is not significant
   * (since there is no parent area for which to determine location).
   * </ul>
   * 
   * @param lbl
   * @param geomIndex
   * @param dim
   * @param depthDelta
   */
  private void computeInputLabel(OverlayLabel lbl, int geomIndex, int dim, int depthDelta, boolean isHole) {
    // not part of the input ==> leave label as NOT_PART
    
    int dimLabel = labelDim(dim, depthDelta);
    
    switch (dimLabel) {
    case OverlayLabel.DIM_NOT_PART:
      // assumes label is initialized correctly
      break;
    case OverlayLabel.DIM_AREA: 
      lbl.initAsAreaBoundary(geomIndex, locationLeft(depthDelta), locationRight(depthDelta), isHole);
      break;
    case OverlayLabel.DIM_COLLAPSE: 
      lbl.initAsCollapse(geomIndex, isHole);
      break;
    case OverlayLabel.DIM_LINE:
      lbl.initAsLine(geomIndex);
      break;
    }
  }

  private int labelDim(int dim, int depthDelta) {
    if (dim == Dimension.FALSE) 
      return OverlayLabel.DIM_NOT_PART;

    if (dim == Dimension.L) 
      return OverlayLabel.DIM_LINE;
    
    // assert: dim is A
    boolean isCollapse = depthDelta == 0;
    if (isCollapse) return OverlayLabel.DIM_COLLAPSE;
        
    return OverlayLabel.DIM_AREA;
  }

  /**
   * Sets the label for an input geometry.
   * 
   * <ul>
   * <li>If the edge is not part of the input, the label is left as NOT_PART
   * <li>If input is an Area and the edge is a complete collapse
   * (depth delta = 0), 
   * the label is left as NOT_PART
   * <li>If input is an Area and the edge is a partial collapse,
   * edge is marked as an Area edge and side locations are assigned
   * <li>If input is a Line edge is marked as a Line edge
   * </ul>
   * 
   * @param lbl
   * @param geomIndex
   * @param dim
   * @param depthDelta
   */
  /*
  private void SKIP_COLLAPSE_setInputLabel(OverlayLabel lbl, int geomIndex, int dim, int depthDelta, boolean isHole) {
    // not part of the input ==> NOT_PART
    if (dim == OverlayLabel.DIM_UNKNOWN) return;
    
    // Area input and collapsed edge ==> NOT_PART
    boolean isCollapse = dim == Dimension.A && depthDelta == 0; 
    if (isCollapse) return;
    
    switch (dim) {
    case Dimension.A: 
      lbl.setToAreaBoundary(geomIndex, locationLeft(depthDelta), locationRight(depthDelta), isHole);
      break;
    case Dimension.L:
      lbl.setToLine(geomIndex, Location.INTERIOR);
      break;
    }
  }
  */
  
  private boolean isKnown(int index) {
    if (index == 0) 
      return aDim != OverlayLabel.DIM_UNKNOWN;
    return bDim != OverlayLabel.DIM_UNKNOWN;
  }

  private boolean isHole(int index) {
    if (index == 0) 
      return aIsHole;
    return bIsHole;    
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
    aIsHole = label.isHole(0);
    aDepthDelta = depthDelta(label, 0);
    
    bDim = label.dimension(1);
    bIsHole = label.isHole(1);
    bDepthDelta = depthDelta(label, 1);
  }
  
  public void mergeEdge(Edge edge) {
    if (edge.aDim > aDim) aDim = edge.aDim;
    if (edge.bDim > bDim) bDim = edge.bDim;
    
    /**
     * Marks this
     * as a shell edge if any contributing edge is a shell.
     */
    aIsHole = mergedRingRole(0, this, edge);
    bIsHole = mergedRingRole(1, this, edge);

    boolean relDir = relativeDirection(edge);
    int flipFactor = relDir ? 1 : -1;
    aDepthDelta += flipFactor * edge.aDepthDelta;
    bDepthDelta += flipFactor * edge.bDepthDelta;
    /*
    if (aDepthDelta > 1) {
      Debug.println(this);
    }
    */
  }

  private boolean mergedRingRole(int geomIndex, Edge edge1, Edge edge2) {
    // TOD: this might be clearer with tri-state logic for isHole
    boolean isShell1 = edge1.isArea(geomIndex) && ! edge1.isHole(geomIndex);
    boolean isShell2 = edge1.isArea(geomIndex) && ! edge1.isHole(geomIndex);
    boolean isShellMerged = isShell1 || isShell2;
    return ! isShellMerged;
  }

  private boolean isArea(int geomIndex) {
    if (geomIndex == 0) return aDim == OverlayLabel.DIM_AREA;
    return bDim == OverlayLabel.DIM_AREA;
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
    String aInfo = "A:" + aDepthDelta 
        + ringRoleSymbol( aDim, aIsHole )
        + OverlayLabel.dimensionSymbol(aDim);
    String bInfo = "B:" + bDepthDelta 
        + ringRoleSymbol( bDim, bIsHole )
        + OverlayLabel.dimensionSymbol(bDim);

    return "Edge( " + ptsStr
        + " ) " + aInfo + "/" + bInfo;
  }

  private String ringRoleSymbol(int dim, boolean isHole) {
    if (dim == OverlayLabel.DIM_NOT_PART) return "";
    return "" + OverlayLabel.ringRoleSymbol(isHole);
  }
}
