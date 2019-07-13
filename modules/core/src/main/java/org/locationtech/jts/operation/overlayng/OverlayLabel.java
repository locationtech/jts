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

import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geomgraph.Position;

/**
 * A label for an {@link OverlayEdge} which records
 * the topological information for the edge
 * in the {@link OverlayGraph} containing it.
 * 
 * A label contains the topological {@link Location}s for 
 * the two overlay parent geometries.
 * A parent geometry may be either a Line or an Area.
 * In both cases, the label locations are populated
 * with the locations for the edge {@link Position}s
 * once they are computed by topological evaluation.
 * The label also records the dimension of each geometry.
 * 
 * @author mdavis
 *
 */
public class OverlayLabel {
  
  public static final int DIM_UNKNOWN = -1;
  public static final int DIM_NOT_PART = DIM_UNKNOWN;
  public static final int DIM_LINE = Dimension.L;
  public static final int DIM_AREA = Dimension.A;
  
  public static int LOC_UNKNOWN = Location.NONE;
  
  public static OverlayLabel createForAreaBoundary(int index, int locLeft, int locRight) {
    return new OverlayLabel(index, locLeft, locRight);
  }
  
  public static OverlayLabel createForLine(int index) {
    return new OverlayLabel(index);
  }
  
  private int aLocLeft = LOC_UNKNOWN;
  private int aLocRight = LOC_UNKNOWN;
  private int aLocInArea = LOC_UNKNOWN;
  private int aDim = DIM_NOT_PART;
  
  private int bLocLeft = LOC_UNKNOWN;
  private int bLocRight = LOC_UNKNOWN;
  private int bLocInArea = LOC_UNKNOWN;
  private int bDim = DIM_NOT_PART;
  
  public OverlayLabel(int index, int locLeft, int locRight)
  {
    setToAreaBoundary(index, locLeft, locRight);
  }

  public OverlayLabel(int index)
  {
    setToLine(index, Location.INTERIOR);
  }

  public OverlayLabel()
  {
  }

  public OverlayLabel(OverlayLabel lbl) {
    this.aLocLeft = lbl.aLocLeft;
    this.aLocRight = lbl.aLocRight;
    this.aLocInArea = lbl.aLocInArea;
    this.aDim = lbl.aDim;
    
    this.bLocLeft = lbl.bLocLeft;
    this.bLocRight = lbl.bLocRight;
    this.bLocInArea = lbl.bLocInArea;
    this.bDim = lbl.bDim;
  }

  public int dimension(int index) {
    if (index == 0)
      return aDim;
    return bDim;
  }
  
  public void setToAreaBoundary(int index, int locLeft, int locRight) {
    if (index == 0) {
      aLocLeft = locLeft;
      aLocRight = locRight;
      aLocInArea = Location.INTERIOR;
      aDim = DIM_AREA;
    }
    else {
      bLocLeft = locLeft;
      bLocRight = locRight;
      bLocInArea = Location.INTERIOR;
      bDim = DIM_AREA;
    }
  }
  
  public void setToLine(int index, int locInArea) {
    int loc = normalizeLocation(locInArea);
    if (index == 0) {
      aDim = DIM_LINE;
      aLocInArea = loc;
    }
    else {
      bDim = DIM_LINE;
      bLocInArea = loc;
    }
  }
  
  public void setLineLocation(int index, int locInArea) {
    // Assert: label dim is L
    int loc = normalizeLocation(locInArea);
    if (index == 0) {
      aLocInArea = loc;
    }
    else {
      bLocInArea = loc;
    }
  }
  
  /**
   * For overlay topology purposes BOUNDARY is the same as INTERIOR.
   * 
   * @param loc the location to normalize
   * @return the normalized location
   */
  private static int normalizeLocation(int loc) {
    if (loc == Location.BOUNDARY) return Location.INTERIOR;
    return loc;
  }

  public boolean isLine() {
    return aDim == DIM_LINE || bDim == DIM_LINE;
  }
  
  public boolean isLine(int index) {
    if (index == 0) {
      return aDim == DIM_LINE;
    }
    return bDim == DIM_LINE;
  }

  public boolean isArea() {
    return aDim == DIM_AREA || bDim == DIM_AREA;
  }
  
  public boolean isAreaBoundary(int index) {
    if (index == 0) {
      return aDim == DIM_AREA;
    }
    return bDim == DIM_AREA;
  }
  
  boolean isUnknownLineLocation(int index) {
    if (index == 0) {
      //if (aDim == DIM_AREA) return false;
      return aLocInArea == LOC_UNKNOWN;
    }
    else {
      //if (bDim == DIM_AREA) return false;
      return bLocInArea == LOC_UNKNOWN;
    }
  }

  /**
   * A collapsed edge is indicated when the parent geometry
   * has dim = A but the label has dim = L.
   * 
   * @param index the index of the parent geometry
   * @param parentDim the dimension of the parent geometry
   * @return true if this label indicates a collapsed edge for the parent geometry
   */
  public boolean isCollapse(int index, int parentDim) {
    return parentDim == 2 && isLine(index);
  }
  
  public int getLineLocation(int index) {
    if (index == 0) {
      return aLocInArea;
    }
    else {
      return bLocInArea;
    }
  }
  
  public boolean isInArea(int index) {
    if (index == 0) {
      return aLocInArea == Location.INTERIOR;
    }
    return bLocInArea == Location.INTERIOR;
  }
  
  public int getLocation(int index, int position) {
    if (index == 0) {
      switch (position) {
        case Position.LEFT: return aLocLeft;
        case Position.RIGHT: return aLocRight;
        case Position.ON: return aLocInArea;
      }
    }
    switch (position) {
      case Position.LEFT: return bLocLeft;
      case Position.RIGHT: return bLocRight;
      case Position.ON: return bLocInArea;
    }
    return LOC_UNKNOWN;
  }
  
  public int getLocationSideOrLine(int index, int position) {
    if (index == 0) {
      if (isAreaBoundary(index)) {
        switch (position) {
          case Position.LEFT: return aLocLeft;
          case Position.RIGHT: return aLocRight;
        }
      }
      return aLocInArea;
    }
    if (isAreaBoundary(index)) {
      switch (position) {
        case Position.LEFT: return bLocLeft;
        case Position.RIGHT: return bLocRight;
      }
    }
    return bLocInArea;
  }

  /**
   * Gets the area location for the given source.
   * 
   * @param index the source index
   * @return the area location for the source
   */
  public int getLocation(int index) {
    if (index == 0) {
      return aLocInArea;
    }
    return bLocInArea;
  }

  public boolean hasSides(int index) {
    if (index == 0) {
      return aLocLeft != LOC_UNKNOWN
          || aLocRight != LOC_UNKNOWN;
    }
    return bLocLeft != LOC_UNKNOWN
        || bLocRight != LOC_UNKNOWN;
  }
  
  /*
  public void setLocation(int index, int position, int location) {
    if (index == 0) {
      switch (position) {
        case Position.LEFT: aLocLeft = location; return;
        case Position.RIGHT: aLocRight = location; return;
        case Position.ON: aLocInArea = location; return;
      }
    }
    switch (position) {
      case Position.LEFT: bLocLeft = location; return;
      case Position.RIGHT: bLocRight = location; return;
      case Position.ON: bLocInArea = location; return;
    }
  }
  */
  
  public OverlayLabel copy() {
    return new OverlayLabel(this);
  }
    
  public OverlayLabel copyFlip() {
    OverlayLabel lbl = new OverlayLabel();
    
    lbl.aLocLeft = this.aLocRight;
    lbl.aLocRight = this.aLocLeft;
    lbl.aLocInArea = this.aLocInArea;
    lbl.aDim = this.aDim;
    
    lbl.bLocLeft = this.bLocRight;
    lbl.bLocRight = this.bLocLeft;
    lbl.bLocInArea = this.bLocInArea;
    lbl.bDim = this.bDim;
    
    return lbl;
  }
  
  /**
   * Merge a label into this label. 
   * 
   * @param lbl
   */
  /*
  public void merge(OverlayLabel lbl)
  {
    if (aLocInArea == LOC_UNKNOWN) aLocInArea = lbl.aLocInArea;
    if (aLocLeft == LOC_UNKNOWN) aLocLeft = lbl.aLocLeft;
    if (aLocRight == LOC_UNKNOWN) aLocRight = lbl.aLocRight;
    // TODO: should this error if dim is different?
    if (aDim == DIM_UNKNOWN) aDim = lbl.aDim;
    
    if (bLocInArea == LOC_UNKNOWN) bLocInArea = lbl.bLocInArea;
    if (bLocLeft == LOC_UNKNOWN) bLocLeft = lbl.bLocLeft;
    if (bLocRight == LOC_UNKNOWN) bLocRight = lbl.bLocRight;
    // TODO: should this error if dim is different?
    if (bDim == DIM_UNKNOWN) bDim = lbl.bDim;
  }
  */
  
  /**
   * Merge a label into this label, 
   * flipping the side values.
   * 
   * @param lbl
   */
  public void mergeFlip(OverlayLabel lbl)
  {
    if (aLocInArea == LOC_UNKNOWN) aLocInArea = lbl.aLocInArea;
    if (aLocLeft == LOC_UNKNOWN) aLocLeft = lbl.aLocRight;
    if (aLocRight == LOC_UNKNOWN) aLocRight = lbl.aLocLeft;
    // TODO: should this error if dim is different?
    if (aDim == DIM_UNKNOWN) aDim = lbl.aDim;
   
    if (bLocInArea == LOC_UNKNOWN) bLocInArea = lbl.bLocInArea;
    if (bLocLeft == LOC_UNKNOWN) bLocLeft = lbl.bLocRight;
    if (bLocRight == LOC_UNKNOWN) bLocRight = lbl.bLocLeft;
    // TODO: should this error if dim is different?
    if (bDim == DIM_UNKNOWN) bDim = lbl.bDim;
  }
  
  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("A:");
    buf.append(locationString(0));
    buf.append("/B:");
    buf.append(locationString(1));
    return buf.toString();
  }

  private String locationString(int index) {
    StringBuilder buf = new StringBuilder();
    if (isAreaBoundary(index)) {
      buf.append( Location.toLocationSymbol( index == 0 ? aLocLeft : bLocLeft ) );
      buf.append( Location.toLocationSymbol( index == 0 ? aLocRight : bLocRight ) );
    }
    else {
      buf.append( Location.toLocationSymbol( index == 0 ? aLocInArea : bLocInArea ));
    }
    buf.append( dimensionSymbol(index == 0 ? aDim : bDim) );
    return buf.toString();
  }

  public static char dimensionSymbol(int dim) {
    switch (dim) {
    case DIM_LINE: return 'L';
    case DIM_AREA: return 'A';
    }
    return '#';
  }


}
