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

import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Position;

/**
 * A structure recording the topological situation
 * for an edge in a topology graph 
 * used during overlay processing. 
 * A label contains the topological {@link Location}s for 
 * one or two input geometries to an overlay operation.
 * An input geometry may be either a Line or an Area.
 * The label locations for each input geometry are populated
 * with the {@Location}s for the edge {@link Position}s
 * when they are created or once they are computed by topological evaluation.
 * A label also records the (effective) dimension of each input geometry.
 * For area edges the role (shell or hole)
 * of the originating ring is recorded, to allow
 * determination of edge handling in collapse cases.
 * <p>
 * In an {@link OverlayGraph} a single label is shared between 
 * the two oppositely-oriented {@ link OverlayEdge}s of a symmetric pair. 
 * Accessors for orientation-sensitive information
 * are parameterized by the orientation of the containing edge.
 * <p>
 * For each input geometry (0 and 1), the label records
 * that an edge is in one of the following states
 * (identified by the <code>dim</code> field).
 * Each state has additional information about the edge topology.
 * <ul>
 * <li>A <b>Boundary</b> edge of an Area (polygon)
 *   <ul>
 *   <li><code>dim</code> = DIM_BOUNDARY</li>
 *   <li><code>locLeft, locRight</code> : the locations of the edge sides for the Area</li>
 *   <li><code>locLine</code> : INTERIOR</li>
 *   <li><code>isHole</code> : whether the 
 * edge is in a shell or a hole (the ring role)</li>
 *   </ul>
 * </li>
 * <li>A <b>Collapsed</b> edge of an input Area 
 * (formed by merging two or more parent edges)
 *   <ul>
 *   <li><code>dim</code> = DIM_COLLAPSE</li>
 *   <li><code>locLine</code> : the location of the edge relative to the effective input Area
 *       (a collapsed spike is EXTERIOR, a collapsed gore or hole is INTERIOR)</li>
 *   <li><code>isHole</code> : <code>true</code> if all parent edges are in holes;
 *                             <code>false</code> if some parent edge is in a shell
 *   </ul>
 * </li>
 * <li>A <b>Line</b> edge from an input line
 *   <ul>
 *   <li><code>dim</code> = DIM_LINE</li>
 *   <li><code>locLine</code> : the location of the edge relative to the Line. 
 *   Initialized to LOC_UNKNOWN to simplify logic.</li>
 *   </ul>
 * </li>
 * <li>An edge which is <b>Not Part</b> of an input geometry
 * (and thus must be part of the other geometry).
 *   <ul>
 *   <li><code>dim</code> = NOT_PART</li>
 *   </ul>
 * </li>
 * </ul>
 * Note that:
 * <ul>
 * <li>an edge cannot be both a Collapse edge and a Line edge in the same input geometry, 
 * because input geometries must be homogeneous in dimension.
 * <li>an edge may be an Boundary edge in one input geometry 
 * and a Line or Collapse edge in the other input.
 * </ul>
 * 
 * @author Martin Davis
 *
 */
class OverlayLabel {
  
  private static final char SYM_UNKNOWN = '#';
  private static final char SYM_BOUNDARY = 'B';
  private static final char SYM_COLLAPSE = 'C';
  private static final char SYM_LINE = 'L';
  
  /**
   * The dimension of an input geometry which is not known
   */
  public static final int DIM_UNKNOWN = -1;
  
  /**
   * The dimension of an edge which is not part of a specified input geometry.
   */
  public static final int DIM_NOT_PART = DIM_UNKNOWN;
  
  /**
   * The dimension of an edge which is a line.
   */
  public static final int DIM_LINE = 1;
  
  /**
   * The dimension for an edge which is part of an input Area geometry boundary.
   */
  public static final int DIM_BOUNDARY = 2;
  
  /**
   * The dimension for an edge which is a collapsed part of an input Area geometry boundary.
   * A collapsed edge represents two or more line segments which have the same endpoints.
   * They usually are caused by edges in valid polygonal geometries
   * having their endpoints become identical due to precision reduction.
   */
  public static final int DIM_COLLAPSE = 3;
  
  /**
   * Indicates that the location is currently unknown
   */
  public static int LOC_UNKNOWN = Location.NONE;
  
  
  private int aDim = DIM_NOT_PART;
  private boolean aIsHole = false;
  private int aLocLeft = LOC_UNKNOWN;
  private int aLocRight = LOC_UNKNOWN;
  private int aLocLine = LOC_UNKNOWN;
  
  private int bDim = DIM_NOT_PART;
  private boolean bIsHole = false;
  private int bLocLeft = LOC_UNKNOWN;
  private int bLocRight = LOC_UNKNOWN;
  private int bLocLine = LOC_UNKNOWN;

  
  /**
   * Creates a label for an Area edge.
   * 
   * @param index the input index of the parent geometry
   * @param locLeft the location of the left side of the edge
   * @param locRight the location of the right side of the edge
   * @param isHole whether the edge role is a hole or a shell
   */
  public OverlayLabel(int index, int locLeft, int locRight, boolean isHole)
  {
    initBoundary(index, locLeft, locRight, isHole);
  }

  /**
   * Creates a label for a Line edge.
   * 
   * @param index the input index of the parent geometry
   */
  public OverlayLabel(int index)
  {
    initLine(index);
  }

  /**
   * Creates an uninitialized label.
   * 
   */
  public OverlayLabel()
  {
  }

  /**
   * Creates a label which is a copy of another label.
   * 
   * @param lbl
   */
  public OverlayLabel(OverlayLabel lbl) {
    this.aLocLeft = lbl.aLocLeft;
    this.aLocRight = lbl.aLocRight;
    this.aLocLine = lbl.aLocLine;
    this.aDim = lbl.aDim;
    this.aIsHole = lbl.aIsHole;
    
    this.bLocLeft = lbl.bLocLeft;
    this.bLocRight = lbl.bLocRight;
    this.bLocLine = lbl.bLocLine;
    this.bDim = lbl.bDim;
    this.bIsHole = lbl.bIsHole;
  }

  /**
   * Gets the effective dimension of the given input geometry.
   * 
   * @param index the input geometry index
   * @return the dimension
   * 
   * @see #DIM_UNKNOWN
   * @see #DIM_NOT_PART
   * @see #DIM_LINE
   * @see #DIM_BOUNDARY
   * @see #DIM_COLLAPSE
   */
  public int dimension(int index) {
    if (index == 0)
      return aDim;
    return bDim;
  }
  
  /**
   * Initializes the label for an input geometry which is an Area boundary.
   * 
   * @param index the input index of the parent geometry
   * @param locLeft the location of the left side of the edge
   * @param locRight the location of the right side of the edge
   * @param isHole whether the edge role is a hole or a shell
   */
  public void initBoundary(int index, int locLeft, int locRight, boolean isHole) {
    if (index == 0) {
      aDim = DIM_BOUNDARY;
      aIsHole = isHole;
      aLocLeft = locLeft;
      aLocRight = locRight;
      aLocLine = Location.INTERIOR;
    }
    else {
      bDim = DIM_BOUNDARY;
      bIsHole = isHole;
      bLocLeft = locLeft;
      bLocRight = locRight;
      bLocLine = Location.INTERIOR;
    }
  }
  
  /**
   * Initializes the label for an edge which is the collapse of 
   * part of the boundary of an Area input geometry.
   * The location of the collapsed edge relative to the
   * parent area geometry is initially unknown.
   * It must be determined from the topology of the overlay graph
   * 
   * @param index the index of the parent input geometry
   * @param isHole whether the dominant edge role is a hole or a shell
   */
  public void initCollapse(int index, boolean isHole) {
    if (index == 0) {
      aDim = DIM_COLLAPSE;
      aIsHole = isHole;
    }
    else {
      bDim = DIM_COLLAPSE;
      bIsHole = isHole;
    }
  }
  
  /**
   * Initializes the label for an input geometry which is a Line.
   * 
   * @param index the index of the parent input geometry
   */
  public void initLine(int index) {
    if (index == 0) {
      aDim = DIM_LINE;
      aLocLine = LOC_UNKNOWN;
    }
    else {
      bDim = DIM_LINE;
      bLocLine = LOC_UNKNOWN;
    }
  }
  
  /**
   * Initializes the label for an edge which is not part of an input geometry.
   * @param index the index of the input geometry
   */
  public void initNotPart(int index) {
    // this assumes locations are initialized to UNKNOWN
    if (index == 0) {
      aDim = DIM_NOT_PART;
    }
    else {
      bDim = DIM_NOT_PART;
    }
  }
  
  /**
   * Sets the line location.
   * 
   * This is used to set the locations for linear edges 
   * encountered during area label propagation.
   * 
   * @param index source to update
   * @param loc location to set
   */
  public void setLocationLine(int index, int loc) {
    if (index == 0) {
      aLocLine = loc;
    }
    else {
      bLocLine = loc;
    }
  }
  
  /**
   * Sets the location of all postions for a given input.
   * 
   * @param index the index of the input geometry
   * @param loc the location to set
   */
  public void setLocationAll(int index, int loc) {
    if (index == 0) {
      aLocLine = loc;
      aLocLeft = loc;
      aLocRight = loc;
    }
    else {
      bLocLine = loc;
      bLocLeft = loc;
      bLocRight = loc;
    }
  }
  
  /**
   * Sets the location for a collapsed edge (the Line position)
   * for an input geometry,
   * depending on the ring role recorded in the label.
   * If the input geometry edge is from a shell, 
   * the location is {@link Location#EXTERIOR}, if it is a hole 
   * it is {@link Location#INTERIOR}.
   * 
   * @param index the index of the input geometry
   */
  public void setLocationCollapse(int index) {
    int loc = isHole(index) ? Location.INTERIOR : Location.EXTERIOR;
    if (index == 0) {
      aLocLine = loc;
    }
    else {
      bLocLine = loc;
    }
  }   

  /**
   * Tests whether at least one of the sources is a Line.
   * 
   * @return true if at least one source is a line
   */
  public boolean isLine() {
    return aDim == DIM_LINE || bDim == DIM_LINE;
  }
  
  /**
   * Tests whether a source is a Line.
   * 
   * @param index the index of the input geometry
   * @return true if the input is a Line
   */
  public boolean isLine(int index) {
    if (index == 0) {
      return aDim == DIM_LINE;
    }
    return bDim == DIM_LINE;
  }

  /**
   * Tests whether an edge is linear (a Line or a Collapse) in an input geometry.
   * 
   * @param index the index of the input geometry
   * @return true if the edge is linear
   */
  public boolean isLinear(int index) {
    if (index == 0) {
      return aDim == DIM_LINE || aDim == DIM_COLLAPSE;
    }
    return bDim == DIM_LINE || bDim == DIM_COLLAPSE;
  }

  /**
   * Tests whether a the source of a label is known.
   * 
   * @param index the index of the source geometry
   * @return true if the source is known
   */
  public boolean isKnown(int index) {
    if (index == 0) {
      return aDim != DIM_UNKNOWN;
    }
    return bDim != DIM_UNKNOWN;
  }

  /**
   * Tests whether a label is for an edge which is not part
   * of a given input geometry.
   * 
   * @param index the index of the source geometry
   * @return true if the edge is not part of the geometry
   */
  public boolean isNotPart(int index) {
    if (index == 0) {
      return aDim == DIM_NOT_PART;
    }
    return bDim == DIM_NOT_PART;
  }

  /**
   * Tests if a label is for an edge which is in the boundary of either source geometry.
   * 
   * @return true if the label is a boundary for either source
   */
  public boolean isBoundaryEither() {
    return aDim == DIM_BOUNDARY || bDim == DIM_BOUNDARY;
  }
  
  /**
   * Tests if a label is for an edge which is in the boundary of both source geometries.
   * 
   * @return true if the label is a boundary for both sources
   */
  public boolean isBoundaryBoth() {
    return aDim == DIM_BOUNDARY && bDim == DIM_BOUNDARY;
  }
  
  /**
   * Tests if the label is a collapsed edge of one area  
   * and is a (non-collapsed) boundary edge of the other area.
   * 
   * @return true if the label is for a collapse coincident with a boundary
   */
  public boolean isBoundaryCollapse() {
    if (isLine()) return false;
    return ! isBoundaryBoth();
  }
  
  /**
   * Tests if a label is for an edge where two 
   * area touch along their boundary.
   * 
   * @return true if the edge is a boundary touch
   */
  public boolean isBoundaryTouch() {
    return isBoundaryBoth()
        && getLocation(0, Position.RIGHT, true) != getLocation(1, Position.RIGHT, true);
  }

  /**
   * Tests if a label is for an edge which is in the boundary of a source geometry.
   * Collapses are not reported as being in the boundary.
   * 
   * @param index the index of the input geometry
   * @return true if the label is a boundary for the source
   */
  public boolean isBoundary(int index) {
    if (index == 0) {
      return aDim == DIM_BOUNDARY;
    }
    return bDim == DIM_BOUNDARY;
  }
  
  /**
   * Tests whether a label is for an edge which is a boundary of one geometry
   * and not part of the other.
   * 
   * @return true if the edge is a boundary singleton
   */
  public boolean isBoundarySingleton() {
    if (aDim == DIM_BOUNDARY && bDim == DIM_NOT_PART) return true;
    if (bDim == DIM_BOUNDARY && aDim == DIM_NOT_PART) return true;
    return false;
  }
  
  /**
   * Tests if the line location for a source is unknown.
   * 
   * @param index the index of the input geometry
   * @return true if the line location is unknown
   */
  public boolean isLineLocationUnknown(int index) {
    if (index == 0) {
      return aLocLine == LOC_UNKNOWN;
    }
    else {
      return bLocLine == LOC_UNKNOWN;
    }
  }

  /**
   * Tests if a line edge is inside a source geometry
   * (i.e. it has location {@link Location#INTERIOR}).
   * 
   * @param index the index of the input geometry
   * @return true if the line is inside the source geometry
   */
  public boolean isLineInArea(int index) {
    if (index == 0) {
      return aLocLine == Location.INTERIOR;
    }
    return bLocLine == Location.INTERIOR;
  }
  
  /**
   * Tests if the ring role of an edge is a hole.
   * 
   * @param index the index of the input geometry
   * @return true if the ring role is a hole
   */
  public boolean isHole(int index) {
    if (index == 0) {
      return aIsHole;
    }
    else {
      return bIsHole;
    }
  }
  
  /**
   * Tests if an edge is a Collapse for a source geometry.
   * 
   * @param index the index of the input geometry
   * @return true if the label indicates the edge is a collapse for the source
   */
  public boolean isCollapse(int index) {
    return dimension(index) == DIM_COLLAPSE;
  }
  
  /**
   * Tests if a label is a Collapse has location {@link Location#INTERIOR}, 
   * to at least one source geometry.
   * 
   * @return true if the label is an Interior Collapse to a source geometry
   */
  public boolean isInteriorCollapse() {
    if (aDim == DIM_COLLAPSE && aLocLine == Location.INTERIOR) return true;
    if (bDim == DIM_COLLAPSE && bLocLine == Location.INTERIOR) return true;
    return false;
  }
  
  /**
   * Tests if a label is a Collapse 
   * and NotPart with location {@link Location#INTERIOR} for the other geometry.
   * 
   * @return true if the label is a Collapse and a NotPart with Location Interior
   */
  public boolean isCollapseAndNotPartInterior() {
    if (aDim == DIM_COLLAPSE && bDim == DIM_NOT_PART && bLocLine == Location.INTERIOR) return true;
    if (bDim == DIM_COLLAPSE && aDim == DIM_NOT_PART && aLocLine == Location.INTERIOR) return true;
    return false;
  }
  
  /**
   * Gets the line location for a source geometry.
   * 
   * @param index the index of the input geometry
   * @return the line location for the source
   */
  public int getLineLocation(int index) {
    if (index == 0) {
      return aLocLine;
    }
    else {
      return bLocLine;
    }
  }
  
  /**
   * Tests if a line is in the interior of a source geometry.
   * 
   * @param index the index of the source geometry
   * @return true if the label is a line and is interior
   */
  public boolean isLineInterior(int index) {
    if (index == 0) {
      return aLocLine == Location.INTERIOR;
    }
    return bLocLine == Location.INTERIOR;
  }
  
  /**
   * Gets the location for a {@link Position} of an edge of a source
   * for an edge with given orientation.
   * 
   * @param index the index of the source geometry
   * @param position the position to get the location for
   * @param isForward true if the orientation of the containing edge is forward
   * @return the location of the oriented position in the source
   */
  public int getLocation(int index, int position, boolean isForward) {
    if (index == 0) {
      switch (position) {
        case Position.LEFT: return isForward ? aLocLeft : aLocRight;
        case Position.RIGHT: return isForward ? aLocRight : aLocLeft;
        case Position.ON: return aLocLine;
      }
    }
    // index == 1
    switch (position) {
      case Position.LEFT: return isForward ? bLocLeft : bLocRight;
      case Position.RIGHT: return isForward ? bLocRight : bLocLeft;
      case Position.ON: return bLocLine;
    }
    return LOC_UNKNOWN;
  }
  
  /**
   * Gets the location for this label for either
   * a Boundary or a Line edge.
   * This supports a simple determination of
   * whether the edge should be included as a result edge.
   * 
   * @param index the source index
   * @param position the position for a boundary label
   * @param isForward the direction for a boundary label
   * @return the location for the specified position
   */
  public int getLocationBoundaryOrLine(int index, int position, boolean isForward) {
    if (isBoundary(index)) {
      return getLocation(index, position, isForward);
    }
    return getLineLocation(index);
  }
  
  /**
   * Gets the linear location for the given source.
   * 
   * @param index the source geometry index
   * @return the linear location for the source
   */
  public int getLocation(int index) {
    if (index == 0) {
      return aLocLine;
    }
    return bLocLine;
  }

  /**
   * Tests whether this label has side position information 
   * for a source geometry.
   * 
   * @param index the source geometry index
   * @return true if at least one side position is known
   */
  public boolean hasSides(int index) {
    if (index == 0) {
      return aLocLeft != LOC_UNKNOWN
          || aLocRight != LOC_UNKNOWN;
    }
    return bLocLeft != LOC_UNKNOWN
        || bLocRight != LOC_UNKNOWN;
  }
  
  /**
   * Creates a copy of this label.
   * 
   * @return a copy of the label
   */
  public OverlayLabel copy() {
    return new OverlayLabel(this);
  }
    
  public String toString()
  {
    return toString(true);
  }

  public String toString(boolean isForward)
  {
    StringBuilder buf = new StringBuilder();
    buf.append("A:");
    buf.append(locationString(0, isForward));
    buf.append("/B:");
    buf.append(locationString(1, isForward));
    return buf.toString();
  }

  private String locationString(int index, boolean isForward) {
    StringBuilder buf = new StringBuilder();
    if (isBoundary(index)) {
      buf.append( Location.toLocationSymbol( getLocation(index, Position.LEFT, isForward) ) );
      buf.append( Location.toLocationSymbol( getLocation(index, Position.RIGHT, isForward) ) );
    }
    else {
      // is a linear edge
      buf.append( Location.toLocationSymbol( index == 0 ? aLocLine : bLocLine ));
    }
    if (isKnown(index))
      buf.append( dimensionSymbol(index == 0 ? aDim : bDim) );
    if (isCollapse(index)) {
      buf.append( ringRoleSymbol( index == 0 ? aIsHole : bIsHole ));
    }
    return buf.toString();
  }

  /**
   * Gets a symbol for the a ring role (Shell or Hole).
   * 
   * @param isHole true for a hole, false for a shell
   * @return the ring role symbol character
   */
  public static char ringRoleSymbol(boolean isHole) {
    return isHole ? 'h' : 's';
  }

  /**
   * Gets the symbol for the dimension code of an edge.
   * 
   * @param dim the dimension code
   * @return the dimension symbol character
   */
  public static char dimensionSymbol(int dim) {
    switch (dim) {
    case DIM_LINE: return SYM_LINE;
    case DIM_COLLAPSE: return SYM_COLLAPSE;
    case DIM_BOUNDARY: return SYM_BOUNDARY;
    }
    return SYM_UNKNOWN;
  }


}
