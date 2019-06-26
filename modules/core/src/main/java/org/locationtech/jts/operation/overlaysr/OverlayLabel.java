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

public class OverlayLabel {
  
  public static OverlayLabel createRingLabel(int geomIndex, int locLeft, int locRight) {
    return new OverlayLabel(geomIndex, locLeft, locRight);
  }
  
  public static OverlayLabel createLineLabel(int geomIndex) {
    return new OverlayLabel(geomIndex);
  }
  
  
  private int aLocLeft = Location.NONE;
  private int aLocRight = Location.NONE;
  private int aLocOn = Location.NONE;
  private boolean aIsArea = false;
  
  private int bLocLeft = Location.NONE;
  private int bLocRight = Location.NONE;
  private int bLocOn = Location.NONE;
  private boolean bIsArea = false;;
  
  public OverlayLabel(int geomIndex, int locLeft, int locRight)
  {
    setLocations(geomIndex, locLeft, locRight);
  }

  public OverlayLabel(int geomIndex)
  {
    setLocation(geomIndex, Position.ON, Location.INTERIOR);
    setLine(geomIndex);
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

  private void setLocations(int geomIndex, int locLeft, int locRight) {
    if (geomIndex == 0) {
      aLocLeft = locLeft;
      aLocRight = locRight;
      aLocOn = Location.BOUNDARY;
      aIsArea = true;
    }
    else {
      bLocLeft = locLeft;
      bLocRight = locRight;
      bLocOn = Location.BOUNDARY;
      bIsArea = true;
    }
  }

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
    if (geomIndex == 0) {
      return aLocLeft == Location.NONE && aLocRight == Location.NONE && aLocOn == Location.NONE;
    }
    return bLocLeft == Location.NONE && bLocRight == Location.NONE && bLocOn == Location.NONE;
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
    return Location.NONE;
  }

  public int getLocation(int geomIndex) {
    if (geomIndex == 0) {
      return aLocOn;
    }
    return bLocOn;
  }

  public boolean hasLocation(int geomIndex) {
    if (geomIndex == 0) {
      return aLocLeft != Location.NONE
          || aLocRight != Location.NONE
          || aLocOn != Location.NONE;
    }
    return bLocLeft != Location.NONE
        || bLocRight != Location.NONE
        || bLocOn != Location.NONE;
  }

  public boolean hasLocation(int geomIndex, int position) {
    return Location.NONE != getLocation(geomIndex, position);
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
      if (aLocOn == Location.NONE && loc == Location.INTERIOR) {
        aIsArea = true;
      }
      aLocLeft = loc;
      aLocRight = loc;
      aLocOn = loc;
    }
    else {
      if (bLocOn == Location.NONE && loc == Location.INTERIOR) {
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
    if (aLocOn == Location.NONE) aLocOn = lbl.aLocOn;
    if (lbl.aIsArea) {
      aIsArea = true;
      if (aLocLeft == Location.NONE) aLocLeft = lbl.aLocLeft;
      if (aLocRight == Location.NONE) aLocRight = lbl.aLocRight;
    }
    
    if (bLocOn == Location.NONE) bLocOn = lbl.bLocOn;
    if (lbl.bIsArea) {
      bIsArea = true;
      if (bLocLeft == Location.NONE) bLocLeft = lbl.bLocLeft;
      if (bLocRight == Location.NONE) bLocRight = lbl.bLocRight;
    }
  }
  
  public void mergeFlip(OverlayLabel lbl)
  {
    /**
     * Lines can change into Areas, but not vice-versa
     */
    if (aLocOn == Location.NONE) aLocOn = lbl.aLocOn;
    if (lbl.aIsArea) {
      aIsArea = true;
      if (aLocLeft == Location.NONE) aLocLeft = lbl.aLocRight;
      if (aLocRight == Location.NONE) aLocRight = lbl.aLocLeft;
    }
    
    if (bLocOn == Location.NONE) bLocOn = lbl.bLocOn;
    if (lbl.bIsArea) {
      bIsArea = true;
      if (bLocLeft == Location.NONE) bLocLeft = lbl.bLocRight;
      if (bLocRight == Location.NONE) bLocRight = lbl.bLocLeft;
    }
  }
  
  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    if (hasLocation(0)) {
      buf.append("A:");
      buf.append(locationString(0));
    }
    if (hasLocation(1)) {
      buf.append(" B:");
      buf.append(locationString(1));
    }
    return buf.toString();
  }

  private String locationString(int index) {
    StringBuilder buf = new StringBuilder();
    char lineLoc = Location.toLocationSymbol( index == 0 ? aLocOn : bLocOn );
    if (isArea(index)) {
      buf.append( Location.toLocationSymbol( index == 0 ? aLocLeft : bLocLeft ) );
      buf.append(lineLoc);
      buf.append( Location.toLocationSymbol( index == 0 ? aLocRight : bLocRight ) );
    }
    else {
      buf.append( lineLoc );
    }
    return buf.toString();
  }


}
