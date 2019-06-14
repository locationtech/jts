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
    if (lbl.aIsArea) this.aIsArea = true;
    if (aLocLeft == Location.NONE) aLocLeft = lbl.aLocLeft;
    if (aLocRight == Location.NONE) aLocRight = lbl.aLocRight;
    if (aLocOn == Location.NONE) aLocOn = lbl.aLocOn;
    
    if (lbl.bIsArea) this.bIsArea = true;
    if (bLocLeft == Location.NONE) bLocLeft = lbl.bLocLeft;
    if (bLocRight == Location.NONE) bLocRight = lbl.bLocRight;
    if (bLocOn == Location.NONE) bLocOn = lbl.bLocOn;
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
