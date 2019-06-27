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
  public static final int DIM_LINE = Dimension.L;
  public static final int DIM_AREA = Dimension.A;
  
  public static OverlayLabel createAreaLabel(int index, int locLeft, int locRight) {
    return new OverlayLabel(index, locLeft, locRight);
  }
  
  public static OverlayLabel createLineLabel(int index) {
    return new OverlayLabel(index, Location.INTERIOR);
  }
  
  private static int LOC_UNKNOWN = Location.NONE;
  
  private int aLocLeft = LOC_UNKNOWN;
  private int aLocRight = LOC_UNKNOWN;
  private int aLocOn = LOC_UNKNOWN;
  private int aDim = DIM_UNKNOWN;
  
  private int bLocLeft = LOC_UNKNOWN;
  private int bLocRight = LOC_UNKNOWN;
  private int bLocOn = LOC_UNKNOWN;
  private int bDim = DIM_UNKNOWN;
  
  public OverlayLabel(int index, int locLeft, int locRight)
  {
    setLocationArea(index, locLeft, locRight);
  }

  public OverlayLabel(int index, int locOn)
  {
    setLocationLine(index, locOn);
  }

  public OverlayLabel()
  {
  }

  public OverlayLabel(OverlayLabel lbl) {
    this.aLocLeft = lbl.aLocLeft;
    this.aLocRight = lbl.aLocRight;
    this.aLocOn = lbl.aLocOn;
    this.aDim = lbl.aDim;
    
    this.bLocLeft = lbl.bLocLeft;
    this.bLocRight = lbl.bLocRight;
    this.bLocOn = lbl.bLocOn;
    this.bDim = lbl.bDim;
  }

  public void setLocationArea(int index, int locLeft, int locRight) {
    setLocationArea(index, Location.BOUNDARY,locLeft, locRight);
  }

  public void setLocationArea(int index, int locOn, int locLeft, int locRight) {
    if (index == 0) {
      aLocLeft = locLeft;
      aLocRight = locRight;
      aLocOn = locOn;
      aDim = DIM_AREA;
    }
    else {
      bLocLeft = locLeft;
      bLocRight = locRight;
      bLocOn = locOn;
      bDim = DIM_AREA;
    }
  }

  public void setLocationLine(int index, int loc) {
    if (index == 0) {
      aLocOn = loc;
      aLocLeft = Location.EXTERIOR;
      aLocRight = Location.EXTERIOR;
      aDim = DIM_LINE;
    }
    else {
      bLocOn = loc;
      bLocLeft = Location.EXTERIOR;
      bLocRight = Location.EXTERIOR;
      bDim = DIM_LINE;
    }
  }
  
  public boolean isLine(int index) {
    if (index == 0) {
      return aDim == DIM_LINE;
    }
    return bDim == DIM_LINE;
  }

  public boolean isLine() {
    return aDim == DIM_LINE || bDim == DIM_LINE;
  }
  
  public boolean isArea() {
    return aDim == DIM_AREA || bDim == DIM_AREA;
  }
  
  public boolean isArea(int index) {
    if (index == 0) {
      return aDim == DIM_AREA;
    }
    return bDim == DIM_AREA;
  }
  public boolean isInteriorArea() {
    return isArea() && (isInteriorArea(0) || isInteriorArea(1));
  }
    
  public boolean isInteriorArea(int index) {
    return getLocation(index, Position.LEFT) == Location.INTERIOR
        && getLocation(index, Position.RIGHT) == Location.INTERIOR;
  }
  
  public boolean isIncomplete() {
    return isUnknown(0) || isUnknown(1);
  }

  boolean isUnknown(int index) {
    /*
    if (geomIndex == 0) {
      return aLocLeft == LOC_UNKNOWN && aLocRight == LOC_UNKNOWN && aLocOn == LOC_UNKNOWN;
    }
    return bLocLeft == LOC_UNKNOWN && bLocRight == LOC_UNKNOWN && bLocOn == LOC_UNKNOWN;
    */
    return ! hasLocation(index);
  }

  public int getLocation(int index, int position) {
    if (index == 0) {
      switch (position) {
        case Position.LEFT: return aLocLeft;
        case Position.RIGHT: return aLocRight;
        case Position.ON: return aLocOn;
      }
    }
    switch (position) {
      case Position.LEFT: return bLocLeft;
      case Position.RIGHT: return bLocRight;
      case Position.ON: return bLocOn;
    }
    return LOC_UNKNOWN;
  }

  /**
   * Gets the ON location for the given source.
   * 
   * @param index
   * @return the ON location for the source
   */
  public int getLocation(int index) {
    if (index == 0) {
      return aLocOn;
    }
    return bLocOn;
  }

  public boolean hasLocation(int index) {
    if (index == 0) {
      return aLocLeft != LOC_UNKNOWN
          || aLocRight != LOC_UNKNOWN
          || aLocOn != LOC_UNKNOWN;
    }
    return bLocLeft != LOC_UNKNOWN
        || bLocRight != LOC_UNKNOWN
        || bLocOn != LOC_UNKNOWN;
  }

  public boolean hasLocation(int index, int position) {
    return LOC_UNKNOWN != getLocation(index, position);
  }
  
  public void setLocation(int index, int position, int location) {
    if (index == 0) {
      switch (position) {
        case Position.LEFT: aLocLeft = location; return;
        case Position.RIGHT: aLocRight = location; return;
        case Position.ON: aLocOn = location; return;
      }
    }
    switch (position) {
      case Position.LEFT: bLocLeft = location; return;
      case Position.RIGHT: bLocRight = location; return;
      case Position.ON: bLocOn = location; return;
    }
  }
  
  public void setLocationBothSides(int index, int loc) {
    if (index == 0) {
      aLocLeft = loc;
      aLocRight = loc;
    }
    else {
      bLocLeft = loc;
      bLocRight = loc;
    }
  }

  public void setLocationsAll(int index, int loc) {
    if (index == 0) {
      aLocLeft = loc;
      aLocRight = loc;
      aLocOn = loc;
    }
    else {
      bLocLeft = loc;
      bLocRight = loc;
      bLocOn = loc;
    }
  }
  
  public OverlayLabel copy() {
    return new OverlayLabel(this);
  }
    
  public OverlayLabel copyFlip() {
    OverlayLabel lbl = new OverlayLabel();
    
    lbl.aLocLeft = this.aLocRight;
    lbl.aLocRight = this.aLocLeft;
    lbl.aLocOn = this.aLocOn;
    lbl.aDim = this.aDim;
    
    lbl.bLocLeft = this.bLocRight;
    lbl.bLocRight = this.bLocLeft;
    lbl.bLocOn = this.bLocOn;
    lbl.bDim = this.bDim;
    
    return lbl;
  }
  
  /**
   * Merge a label into this label. 
   * 
   * @param lbl
   */
  public void merge(OverlayLabel lbl)
  {
    if (aLocOn == LOC_UNKNOWN) aLocOn = lbl.aLocOn;
    if (aLocLeft == LOC_UNKNOWN) aLocLeft = lbl.aLocLeft;
    if (aLocRight == LOC_UNKNOWN) aLocRight = lbl.aLocRight;
    // TODO: should this error if dim is different?
    if (aDim == DIM_UNKNOWN) aDim = lbl.aDim;
    
    if (bLocOn == LOC_UNKNOWN) bLocOn = lbl.bLocOn;
    if (bLocLeft == LOC_UNKNOWN) bLocLeft = lbl.bLocLeft;
    if (bLocRight == LOC_UNKNOWN) bLocRight = lbl.bLocRight;
    // TODO: should this error if dim is different?
    if (bDim == DIM_UNKNOWN) bDim = lbl.bDim;
  }
  
  /**
   * Merge a label into this label, 
   * flipping the side values.
   * 
   * @param lbl
   */
  public void mergeFlip(OverlayLabel lbl)
  {
    if (aLocOn == LOC_UNKNOWN) aLocOn = lbl.aLocOn;
    if (aLocLeft == LOC_UNKNOWN) aLocLeft = lbl.aLocRight;
    if (aLocRight == LOC_UNKNOWN) aLocRight = lbl.aLocLeft;
    // TODO: should this error if dim is different?
    if (aDim == DIM_UNKNOWN) aDim = lbl.aDim;
   
    if (bLocOn == LOC_UNKNOWN) bLocOn = lbl.bLocOn;
    if (bLocLeft == LOC_UNKNOWN) bLocLeft = lbl.bLocRight;
    if (bLocRight == LOC_UNKNOWN) bLocRight = lbl.bLocLeft;
    // TODO: should this error if dim is different?
    if (bDim == DIM_UNKNOWN) bDim = lbl.bDim;
  }
  
  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    if (hasLocation(0)) {
      buf.append("A:");
      buf.append(locationString(0));
    }
    if (hasLocation(1)) {
      buf.append("/B:");
      buf.append(locationString(1));
    }
    return buf.toString();
  }

  private String locationString(int index) {
    StringBuilder buf = new StringBuilder();
    buf.append( Location.toLocationSymbol( index == 0 ? aLocLeft : bLocLeft ) );
    buf.append( Location.toLocationSymbol( index == 0 ? aLocOn : bLocOn ));
    buf.append( Location.toLocationSymbol( index == 0 ? aLocRight : bLocRight ) );
    buf.append( dimensionSymbol(index == 0 ? aDim : bDim) );
    return buf.toString();
  }

  private char dimensionSymbol(int dim) {
    switch (dim) {
    case DIM_LINE: return 'L';
    case DIM_AREA: return 'A';
    }
    return '?';
  }

}
