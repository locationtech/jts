package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.geomgraph.TopologyLocation;

public class OverlayLabel {
  
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
      aIsArea = true;
    }
    else {
      bLocLeft = locLeft;
      bLocRight = locRight;
      bIsArea = true;
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

  public OverlayLabel createFlipped() {
    OverlayLabel lbl = new OverlayLabel();
    
    lbl.aLocLeft = this.bLocLeft;
    lbl.aLocRight = this.bLocRight;
    lbl.aLocOn = this.bLocOn;
    lbl.aIsArea = this.bIsArea;
    
    lbl.bLocLeft = this.aLocLeft;
    lbl.bLocRight = this.aLocRight;
    lbl.bLocOn = this.aLocOn;
    lbl.bIsArea = this.aIsArea;
    
    return lbl;
  }
  
  public void merge(OverlayLabel lbl)
  {
    /**
     * Lines can change into Areas, but not vice-versa
     */
    if (lbl.aIsArea) this.aIsArea = true;
    if (aLocLeft == Location.NONE) aLocLeft = lbl.aLocLeft;
    if (aLocRight == Location.NONE) aLocRight = lbl.aLocRight;
    if (aLocOn == Location.NONE) aLocOn = lbl.aLocOn;
    
    if (lbl.bIsArea) this.bIsArea = true;
    if (bLocLeft == Location.NONE) bLocLeft = lbl.bLocLeft;
    if (bLocRight == Location.NONE) bLocRight = lbl.bLocRight;
    if (bLocOn == Location.NONE) bLocOn = lbl.bLocOn;
  }
  
}
