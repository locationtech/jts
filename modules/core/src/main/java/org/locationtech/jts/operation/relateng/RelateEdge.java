/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import java.util.List;

import org.locationtech.jts.algorithm.PolygonNodeTopology;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Position;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Assert;

class RelateEdge {

  public static final boolean IS_FORWARD = true;
  public static final boolean IS_REVERSE = false;
  
  public static RelateEdge create(RelateNode node, Coordinate dirPt, boolean isA, int dim, boolean isForward) {
    if (dim == Dimension.A)
      //-- create an area edge
      return new RelateEdge(node, dirPt, isA, isForward);
    //-- create line edge
    return new RelateEdge(node, dirPt, isA);
  }
  
  public static int findKnownEdgeIndex(List<RelateEdge> edges, boolean isA) {
    for (int i = 0; i < edges.size(); i++) {
      RelateEdge e = edges.get(i);
      if (e.isKnown(isA))
        return i;
    }
    return -1;
  }
  
  public static void setAreaInterior(List<RelateEdge> edges, boolean isA) {
    for (RelateEdge e : edges) {
      e.setAreaInterior(isA);
    }
  }
  
  /**
   * The dimension of an input geometry which is not known
   */
  public static final int DIM_UNKNOWN = -1;
  
  /**
   * Indicates that the location is currently unknown
   */
  private static int LOC_UNKNOWN = Location.NONE;
  
  private RelateNode node;
  private Coordinate dirPt;
  
  private int aDim = DIM_UNKNOWN;
  private int aLocLeft = LOC_UNKNOWN;
  private int aLocRight = LOC_UNKNOWN;
  private int aLocLine = LOC_UNKNOWN;
  
  private int bDim = DIM_UNKNOWN;
  private int bLocLeft = LOC_UNKNOWN;
  private int bLocRight = LOC_UNKNOWN;
  private int bLocLine = LOC_UNKNOWN;
  
  /*
  private int aDim = DIM_UNKNOWN;
  private int aLocLeft = Location.EXTERIOR;
  private int aLocRight = Location.EXTERIOR;
  private int aLocLine = Location.EXTERIOR;
  
  private int bDim = DIM_UNKNOWN;
  private int bLocLeft = Location.EXTERIOR;
  private int bLocRight = Location.EXTERIOR;
  private int bLocLine = Location.EXTERIOR;
  */
  
  public RelateEdge(RelateNode node, Coordinate pt, boolean isA, boolean isForward) {
    this.node = node;
    this.dirPt = pt;
    setLocationsArea(isA, isForward);
  }

  public RelateEdge(RelateNode node, Coordinate pt, boolean isA) {
    this.node = node;
    this.dirPt = pt;
    setLocationsLine(isA);
  }

  public RelateEdge(RelateNode node, Coordinate pt, boolean isA, int locLeft, int locRight, int locLine) {
    this.node = node;
    this.dirPt = pt;
    setLocations(isA, locLeft, locRight, locLine);
  }

  private void setLocations(boolean isA, int locLeft, int locRight, int locLine) {
    if (isA) {
      aDim = 2;
      aLocLeft = locLeft;
      aLocRight = locRight;
      aLocLine = locLine;
    }
    else {
      bDim = 2;
      bLocLeft = locLeft;
      bLocRight = locRight;      
      bLocLine = locLine;
    }
  }
  
  private void setLocationsLine(boolean isA) {
    if (isA) {
      aDim = 1;
      aLocLeft = Location.EXTERIOR;
      aLocRight = Location.EXTERIOR;
      aLocLine = Location.INTERIOR;
    }
    else {
      bDim = 1;
      bLocLeft = Location.EXTERIOR;
      bLocRight = Location.EXTERIOR;      
      bLocLine = Location.INTERIOR;
    }
  }  
  
  private void setLocationsArea(boolean isA, boolean isForward) {
    int locLeft = isForward ? Location.EXTERIOR : Location.INTERIOR;
    int locRight = isForward ? Location.INTERIOR : Location.EXTERIOR;
    if (isA) {
      aDim = 2;
      aLocLeft = locLeft;
      aLocRight = locRight;
      aLocLine = Location.BOUNDARY;
    }
    else {
      bDim = 2;
      bLocLeft = locLeft;
      bLocRight = locRight;      
      bLocLine = Location.BOUNDARY;
    }
  }

  public int compareToEdge(Coordinate edgeDirPt) {
    return PolygonNodeTopology.compareAngle(node.getCoordinate(), this.dirPt, edgeDirPt);
  }

  public void merge(boolean isA, Coordinate dirPt, int dim, boolean isForward) {
    int locEdge = Location.INTERIOR;
    int locLeft = Location.EXTERIOR;
    int locRight = Location.EXTERIOR;
    if (dim == Dimension.A) {
      locEdge = Location.BOUNDARY;
      locLeft = isForward ? Location.EXTERIOR : Location.INTERIOR;
      locRight = isForward ? Location.INTERIOR : Location.EXTERIOR;
    }
    
    if (! isKnown(isA)) {
      setDimension(isA, dim);
      setOn(isA, locEdge);
      setLeft(isA, locLeft);
      setRight(isA, locRight);
      return;
    }

    // Assert: node-dirpt is collinear with node-pt
    mergeDimEdgeLoc(isA, locEdge);
    mergeSideLocation(isA, Position.LEFT, locLeft);
    mergeSideLocation(isA, Position.RIGHT, locRight);
  }
  
  /**
   * Area edges override Line edges.  
   * Merging edges of same dimension is a no-op for 
   * the dimension and on location.
   * But merging an area edge into a line edge
   * sets the dimension to A and the location to BOUNDARY.
   * 
   * @param isA
   * @param locEdge
   */
  private void mergeDimEdgeLoc(boolean isA, int locEdge) {
    //TODO: this logic needs work - ie handling A edges marked as Interior
    int dim = locEdge == Location.BOUNDARY ? Dimension.A : Dimension.L;
    if (dim == Dimension.A && dimension(isA) == Dimension.L) {
      setDimension(isA, dim);
      setOn(isA, Location.BOUNDARY);
    }
  }

  private void mergeSideLocation(boolean isA, int pos, int loc) {
    int currLoc = location(isA, pos);
    //-- INTERIOR takes precedence over EXTERIOR
    if (currLoc != Location.INTERIOR) {
      setLocation(isA, pos, loc);
    }
  }

  private void setDimension(boolean isA, int dimension) {
    if (isA) {
      aDim = dimension;
    }
    else {
      bDim = dimension;
    }
  }

  public void setLocation(boolean isA, int pos, int loc) {
    switch (pos) {
    case Position.LEFT: 
      setLeft(isA, loc);
      break;
    case Position.RIGHT: 
      setRight(isA, loc);
      break;
    case Position.ON: 
      setOn(isA, loc);
      break;
    }
  }
  
  public void setAllLocations(boolean isA, int loc) {
    setLeft(isA, loc);
    setRight(isA, loc);
    setOn(isA, loc);
  }
  
  public void setUnknownLocations(boolean isA, int loc) {
    if (! isKnown(isA, Position.LEFT)) {
      setLocation(isA, Position.LEFT, loc);
    }
    if (! isKnown(isA, Position.RIGHT)) {
      setLocation(isA, Position.RIGHT, loc);
    }
    if (! isKnown(isA, Position.ON)) {
      setLocation(isA, Position.ON, loc);
    }  
  }
  
  private void setLeft(boolean isA, int loc) {
    if (isA) {
      aLocLeft = loc;
    }
    else {
      bLocLeft = loc;
    }
  }

  private void setRight(boolean isA, int loc) {
    if (isA) {
      aLocRight = loc;
    }
    else {
      bLocRight = loc;
    }
  }

  private void setOn(boolean isA, int loc) {
    if (isA) {
      aLocLine = loc;
    }
    else {
      bLocLine = loc;
    }
  }
  
  public int location(boolean isA, int position) {
    if (isA) {
      switch (position) {
      case Position.LEFT: return aLocLeft;
      case Position.RIGHT: return aLocRight;
      case Position.ON: return aLocLine;
      }
    }
    else {
      switch (position) {
      case Position.LEFT: return bLocLeft;
      case Position.RIGHT: return bLocRight;
      case Position.ON: return bLocLine;
      }  
    }
    Assert.shouldNeverReachHere();
    return LOC_UNKNOWN;
  }
  
  private int dimension(boolean isA) {
    return isA ? aDim : bDim;
  }

  private boolean isKnown(boolean isA) {
    if (isA)
      return aDim != DIM_UNKNOWN;
    return bDim != DIM_UNKNOWN;
  }

  private boolean isKnown(boolean isA, int pos) {
    return location(isA, pos) != LOC_UNKNOWN;
  } 
  
  public boolean isInterior(boolean isA, int position) {
    return location(isA, position) == Location.INTERIOR;
  }
  
  public void setDimLocations(boolean isA, int dim, int loc) {
    if (isA) {
      aDim = dim;
      aLocLeft = loc;
      aLocRight = loc;
      aLocLine = loc;
    }
    else {
      bDim = dim;
      bLocLeft = loc;
      bLocRight = loc;
      bLocLine = loc;
    }
  }

  public void setAreaInterior(boolean isA) {
    if (isA) {
      aLocLeft = Location.INTERIOR;
      aLocRight = Location.INTERIOR;
      aLocLine = Location.INTERIOR;
    }
    else {
      bLocLeft = Location.INTERIOR;
      bLocRight = Location.INTERIOR;
      bLocLine = Location.INTERIOR;
    }    
  }

  public String toString() {
    return WKTWriter.toLineString(node.getCoordinate(), dirPt) 
        + " - " + labelString();
  }

  private String labelString() {
    StringBuilder buf = new StringBuilder();
    buf.append("A:");
    buf.append(locationString(RelateGeometry.GEOM_A));
    buf.append("/B:");
    buf.append(locationString(RelateGeometry.GEOM_B));
    return buf.toString();
  }

  private String locationString(boolean isA) {
    StringBuilder buf = new StringBuilder();
    buf.append(Location.toLocationSymbol(location(isA, Position.LEFT)));
    buf.append(Location.toLocationSymbol(location(isA, Position.ON)));
    buf.append(Location.toLocationSymbol(location(isA, Position.RIGHT)));
    return buf.toString();
  }

}
