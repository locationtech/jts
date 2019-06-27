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

import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geomgraph.Position;

/**
 * A label for an {@link OverlayEdge} which records
 * the topological information for the edge
 * in its {@link OverlayGraph}.
 * 
 * A label contains the topological {@link Location}s for 
 * the two overlay input geometries.
 * An input geometry may be either a Line or an Area.
 * In both cases, the label locations are populated
 * with the locations for the edge {@link Position}s
 * once they are computed by topological evaluation.
 * The label also records the dimension of each geometry.
 * 
 * @author mdavis
 *
 */
public class OverlayLabel {
  
  public static OverlayLabel createAreaLabel(int geomIndex, int locLeft, int locRight) {
    return new OverlayLabel(geomIndex, locLeft, locRight);
  }
  
  public static OverlayLabel createLineLabel(int geomIndex) {
    return new OverlayLabel(geomIndex, Location.INTERIOR);
  }
  
  private static int LOC_UNKNOWN = Location.NONE;
  
  private int aLocLeft = LOC_UNKNOWN;
  private int aLocRight = LOC_UNKNOWN;
  private int aLocOn = LOC_UNKNOWN;
  private boolean aIsArea = false;
  
  private int bLocLeft = LOC_UNKNOWN;
  private int bLocRight = LOC_UNKNOWN;
  private int bLocOn = LOC_UNKNOWN;
  private boolean bIsArea = false;
  
  public OverlayLabel(int geomIndex, int locLeft, int locRight)
  {
    setLocationArea(geomIndex, locLeft, locRight);
  }

  public OverlayLabel(int geomIndex, int locOn)
  {
    setLocationLine(geomIndex, locOn);
  }

  public OverlayLabel()
  {
  }

  public OverlayLabel(OverlayLabel lbl) {
    this.aLocLeft = lbl.aLocLeft;
    this.aLocRight = lbl.aLocRight;
    this.aLocOn = lbl.aLocOn;
    this.aIsArea = lbl.aIsArea;
    
    this.bLocLeft = lbl.bLocLeft;
    this.bLocRight = lbl.bLocRight;
    this.bLocOn = lbl.bLocOn;
    this.bIsArea = lbl.bIsArea;
  }

  public void setLocationArea(int geomIndex, int locLeft, int locRight) {
    setLocationArea(geomIndex, Location.BOUNDARY,locLeft, locRight);
  }

  public void setLocationArea(int geomIndex, int locOn, int locLeft, int locRight) {
    if (geomIndex == 0) {
      aLocLeft = locLeft;
      aLocRight = locRight;
      aLocOn = locOn;
      aIsArea = true;
    }
    else {
      bLocLeft = locLeft;
      bLocRight = locRight;
      bLocOn = locOn;
      bIsArea = true;
    }
  }

  public void setLocationLine(int geomIndex, int loc) {
    if (geomIndex == 0) {
      aLocOn = loc;
      aLocLeft = Location.EXTERIOR;
      aLocRight = Location.EXTERIOR;
      aIsArea = false;
    }
    else {
      bLocOn = loc;
      bLocLeft = Location.EXTERIOR;
      bLocRight = Location.EXTERIOR;
      bIsArea = false;
    }
  }

  /*
  private void setArea(int geomIndex) {
    if (geomIndex == 0) {
      aIsArea = true;
    }
    else {
      bIsArea = true;
    } 
  }
  
  private void setLine(int geomIndex) {
    if (geomIndex == 0) {
      aIsArea = false;
    }
    else {
      bIsArea = false;
    } 
  }
  */
  
  public boolean isLine(int geomIndex) {
    if (geomIndex == 0) {
      return ! aIsArea;
    }
    return ! bIsArea;
  }

  public boolean isLine() {
    return ! aIsArea || ! bIsArea;
  }
  
  public boolean isArea() {
    return aIsArea || bIsArea;
  }
  
  public boolean isArea(int geomIndex) {
    if (geomIndex == 0) {
      return aIsArea;
    }
    return bIsArea;
  }
  public boolean isInteriorArea() {
    return isArea() && (isInteriorArea(0) || isInteriorArea(1));
  }
    
  public boolean isInteriorArea(int geomIndex) {
    return getLocation(geomIndex, Position.LEFT) == Location.INTERIOR
        && getLocation(geomIndex, Position.RIGHT) == Location.INTERIOR;
  }
  
  public boolean isIncomplete() {
    return isUnknown(0) || isUnknown(1);
  }

  boolean isUnknown(int geomIndex) {
    /*
    if (geomIndex == 0) {
      return aLocLeft == LOC_UNKNOWN && aLocRight == LOC_UNKNOWN && aLocOn == LOC_UNKNOWN;
    }
    return bLocLeft == LOC_UNKNOWN && bLocRight == LOC_UNKNOWN && bLocOn == LOC_UNKNOWN;
    */
    return ! hasLocation(geomIndex);
  }

  public int getLocation(int geomIndex, int position) {
    if (geomIndex == 0) {
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

  public int getLocation(int geomIndex) {
    if (geomIndex == 0) {
      return aLocOn;
    }
    return bLocOn;
  }

  public boolean hasLocation(int geomIndex) {
    if (geomIndex == 0) {
      return aLocLeft != LOC_UNKNOWN
          || aLocRight != LOC_UNKNOWN
          || aLocOn != LOC_UNKNOWN;
    }
    return bLocLeft != LOC_UNKNOWN
        || bLocRight != LOC_UNKNOWN
        || bLocOn != LOC_UNKNOWN;
  }

  public boolean hasLocation(int geomIndex, int position) {
    return LOC_UNKNOWN != getLocation(geomIndex, position);
  }
  
  public void setLocation(int geomIndex, int position, int location) {
    if (geomIndex == 0) {
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
  
  public void setLocationBothSides(int geomIndex, int loc) {
    if (geomIndex == 0) {
      aLocLeft = loc;
      aLocRight = loc;
    }
    else {
      bLocLeft = loc;
      bLocRight = loc;
    }
  }

  public void setLocationsAll(int geomIndex, int loc) {
    if (geomIndex == 0) {
      if (aLocOn == LOC_UNKNOWN && loc == Location.INTERIOR) {
        aIsArea = true;
      }
      aLocLeft = loc;
      aLocRight = loc;
      aLocOn = loc;
    }
    else {
      if (bLocOn == LOC_UNKNOWN && loc == Location.INTERIOR) {
        bIsArea = true;
      }
      bLocLeft = loc;
      bLocRight = loc;
      bLocOn = loc;
    }
  }
  
  public OverlayLabel createFlipped() {
    OverlayLabel lbl = new OverlayLabel();
    
    lbl.aLocLeft = this.aLocRight;
    lbl.aLocRight = this.aLocLeft;
    lbl.aLocOn = this.aLocOn;
    lbl.aIsArea = this.aIsArea;
    
    lbl.bLocLeft = this.bLocRight;
    lbl.bLocRight = this.bLocLeft;
    lbl.bLocOn = this.bLocOn;
    lbl.bIsArea = this.bIsArea;
    
    return lbl;
  }
  
  public void merge(OverlayLabel lbl)
  {
    /**
     * Lines can change into Areas, but not vice-versa
     */
    if (aLocOn == LOC_UNKNOWN) aLocOn = lbl.aLocOn;
    if (lbl.aIsArea) {
      aIsArea = true;
      if (aLocLeft == LOC_UNKNOWN) aLocLeft = lbl.aLocLeft;
      if (aLocRight == LOC_UNKNOWN) aLocRight = lbl.aLocRight;
    }
    
    if (bLocOn == LOC_UNKNOWN) bLocOn = lbl.bLocOn;
    if (lbl.bIsArea) {
      bIsArea = true;
      if (bLocLeft == LOC_UNKNOWN) bLocLeft = lbl.bLocLeft;
      if (bLocRight == LOC_UNKNOWN) bLocRight = lbl.bLocRight;
    }
  }
  
  public void mergeFlip(OverlayLabel lbl)
  {
    if (aLocOn == LOC_UNKNOWN) aLocOn = lbl.aLocOn;
    if (aLocLeft == LOC_UNKNOWN) aLocLeft = lbl.aLocRight;
    if (aLocRight == LOC_UNKNOWN) aLocRight = lbl.aLocLeft;
    
    if (bLocOn == LOC_UNKNOWN) bLocOn = lbl.bLocOn;
    if (bLocLeft == LOC_UNKNOWN) bLocLeft = lbl.bLocRight;
    if (bLocRight == LOC_UNKNOWN) bLocRight = lbl.bLocLeft;
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
    boolean isArea = index == 0 ? aIsArea : bIsArea;
    buf.append(isArea ? 'A' : 'L');
    return buf.toString();
  }


}
;