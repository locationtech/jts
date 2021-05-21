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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.io.WKTWriter;

/**
 * Represents the linework for edges in the topology 
 * derived from (up to) two parent geometries.
 * An edge may be the result of the merging of 
 * two or more edges which have the same linework
 * (although possibly different orientations).  
 * In this case the topology information is 
 * derived from the merging of the information in the 
 * source edges.
 * Merged edges can occur in the following situations
 * <ul>
 * <li>Due to coincident edges of polygonal or linear geometries. 
 * <li>Due to topology collapse caused by snapping or rounding
 * of polygonal geometries. 
 * </ul>
 * The source edges may have the same parent geometry,
 * or different ones, or a mix of the two.
 *  
 * @author mdavis
 *
 */
class Edge {
  
  /**
   * Tests if the given point sequence
   * is a collapsed line.
   * A collapsed edge has fewer than two distinct points.
   * 
   * @param pts the point sequence to check
   * @return true if the points form a collapsed line
   */
  public static boolean isCollapsed(Coordinate[] pts) {
    if (pts.length < 2) return true;
    // zero-length line
    if (pts[0].equals2D(pts[1])) return true;
    // TODO: is pts > 2 with equal points ever expected?
    if (pts.length > 2) {
      if ( pts[ pts.length-1 ].equals2D(pts[ pts.length - 2 ])) return true;
    }
    return false;
  }
  
  private Coordinate[] pts;
  
  private int aDim = OverlayLabel.DIM_UNKNOWN;
  private int aDepthDelta = 0;
  private boolean aIsHole = false;
  
  private int bDim = OverlayLabel.DIM_UNKNOWN;
  private int bDepthDelta = 0;
  private boolean bIsHole = false;

  public Edge(Coordinate[] pts, EdgeSourceInfo info) {
    this.pts = pts;
    copyInfo(info);
  }
  
  public Coordinate[] getCoordinates() {
    return pts;
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
    
    return cmp == -1;
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
  
  public OverlayLabel createLabel() {
    OverlayLabel lbl = new OverlayLabel();
    initLabel(lbl, 0, aDim, aDepthDelta, aIsHole);
    initLabel(lbl, 1, bDim, bDepthDelta, bIsHole);
    return lbl;
  }
  
  /**
   * Populates the label for an edge resulting from an input geometry.
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
  private static void initLabel(OverlayLabel lbl, int geomIndex, int dim, int depthDelta, boolean isHole) {
    int dimLabel = labelDim(dim, depthDelta);
    
    switch (dimLabel) {
    case OverlayLabel.DIM_NOT_PART:
      lbl.initNotPart(geomIndex);
      break;
    case OverlayLabel.DIM_BOUNDARY: 
      lbl.initBoundary(geomIndex, locationLeft(depthDelta), locationRight(depthDelta), isHole);
      break;
    case OverlayLabel.DIM_COLLAPSE: 
      lbl.initCollapse(geomIndex, isHole);
      break;
    case OverlayLabel.DIM_LINE:
      lbl.initLine(geomIndex);
      break;
    }
  }

  private static int labelDim(int dim, int depthDelta) {
    if (dim == Dimension.FALSE) 
      return OverlayLabel.DIM_NOT_PART;

    if (dim == Dimension.L) 
      return OverlayLabel.DIM_LINE;
    
    // assert: dim is A
    boolean isCollapse = depthDelta == 0;
    if (isCollapse) return OverlayLabel.DIM_COLLAPSE;
        
    return OverlayLabel.DIM_BOUNDARY;
  }
  
  /**
   * Tests whether the edge is part of a shell in the given geometry.
   * This is only the case if the edge is a boundary.
   * 
   * @param geomIndex the index of the geometry
   * @return true if this edge is a boundary and part of a shell
   */
  private boolean isShell(int geomIndex) {
    if (geomIndex == 0) {
      return aDim == OverlayLabel.DIM_BOUNDARY && ! aIsHole;
    }
    return bDim == OverlayLabel.DIM_BOUNDARY && ! bIsHole;
  }
  
  private static int locationRight(int depthDelta) {
    int delSign = delSign(depthDelta);
    switch (delSign) {
    case 0: return OverlayLabel.LOC_UNKNOWN;
    case 1: return Location.INTERIOR;
    case -1: return Location.EXTERIOR;
    }
    return OverlayLabel.LOC_UNKNOWN;
  }

  private static int locationLeft(int depthDelta) {
    // TODO: is it always safe to ignore larger depth deltas?
    int delSign = delSign(depthDelta);
    switch (delSign) {
    case 0: return OverlayLabel.LOC_UNKNOWN;
    case 1: return Location.EXTERIOR;
    case -1: return Location.INTERIOR;
    }
    return OverlayLabel.LOC_UNKNOWN;
  }

  private static int delSign(int depthDel) {
    if(depthDel > 0) return 1;
    if (depthDel < 0) return -1;
    return 0;
  }

  private void copyInfo(EdgeSourceInfo info) {
    if (info.getIndex() == 0) {
      aDim = info.getDimension();
      aIsHole = info.isHole();
      aDepthDelta = info.getDepthDelta();
    }
    else {
      bDim = info.getDimension();
      bIsHole = info.isHole();
      bDepthDelta = info.getDepthDelta();
    }
  }
  
  /**
   * Merges an edge into this edge,
   * updating the topology info accordingly.
   * 
   * @param edge
   */
  public void merge(Edge edge) {
    /**
     * Marks this
     * as a shell edge if any contributing edge is a shell.
     * Update hole status first, since it depends on edge dim
     */
    aIsHole = isHoleMerged(0, this, edge);
    bIsHole = isHoleMerged(1, this, edge);

    if (edge.aDim > aDim) aDim = edge.aDim;
    if (edge.bDim > bDim) bDim = edge.bDim;
    
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

  private static boolean isHoleMerged(int geomIndex, Edge edge1, Edge edge2) {
    // TOD: this might be clearer with tri-state logic for isHole?
    boolean isShell1 = edge1.isShell(geomIndex);
    boolean isShell2 = edge2.isShell(geomIndex);
    boolean isShellMerged = isShell1 || isShell2;
    // flip since isHole is stored
    return ! isShellMerged;
  }

  public String toString() {
    
    String ptsStr = toStringPts(pts);
    
    String aInfo = infoString(0, aDim, aIsHole, aDepthDelta );
    String bInfo = infoString(1, bDim, bIsHole, bDepthDelta );

    return "Edge( " + ptsStr  + " ) " 
        + aInfo + "/" + bInfo;
  }
  
  public String toLineString() {
    return WKTWriter.toLineString(pts);
  }

  private static String toStringPts(Coordinate[] pts) {
    Coordinate orig = pts[0];
    Coordinate dest = pts[pts.length - 1];
    String dirPtStr = (pts.length > 2)
        ? ", " + WKTWriter.format(pts[1])
            : "";
    String ptsStr = WKTWriter.format(orig)
        + dirPtStr
        + " .. " + WKTWriter.format(dest);
    return ptsStr;
  }

  public static String infoString(int index, int dim, boolean isHole, int depthDelta) {
    return
        (index == 0 ? "A:" : "B:")
        + OverlayLabel.dimensionSymbol(dim)
        + ringRoleSymbol( dim, isHole )
        + Integer.toString(depthDelta);  // force to string
  }
  
  private static String ringRoleSymbol(int dim, boolean isHole) {
    if (hasAreaParent(dim)) return "" + OverlayLabel.ringRoleSymbol(isHole);
    return "";
  }
  
  private static boolean hasAreaParent(int dim) {
    return dim == OverlayLabel.DIM_BOUNDARY || dim == OverlayLabel.DIM_COLLAPSE;
  }
}
