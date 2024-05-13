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

import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Location;

class DimensionLocation {

  public static final int EXTERIOR = Location.EXTERIOR;
  public static final int POINT_INTERIOR = 103;
  public static final int LINE_INTERIOR = 110;
  public static final int LINE_BOUNDARY = 111;
  public static final int AREA_INTERIOR = 120;
  public static final int AREA_BOUNDARY = 121;
    
  public static int locationArea(int loc) {
    switch (loc) {
    case Location.INTERIOR: return AREA_INTERIOR;
    case Location.BOUNDARY: return AREA_BOUNDARY;
    }
    return EXTERIOR;
  }
  
  public static int locationLine(int loc) {
    switch (loc) {
    case Location.INTERIOR: return LINE_INTERIOR;
    case Location.BOUNDARY: return LINE_BOUNDARY;
    }
    return EXTERIOR;
  }
  
  public static int locationPoint(int loc) {
    switch (loc) {
    case Location.INTERIOR: return POINT_INTERIOR;
    }
    return EXTERIOR;
  }
  
  public static int location(int dimLoc) {
    switch (dimLoc) {
    case POINT_INTERIOR:
    case LINE_INTERIOR:
    case AREA_INTERIOR:
      return Location.INTERIOR;
    case LINE_BOUNDARY:
    case AREA_BOUNDARY:
      return Location.BOUNDARY;
    }
    return Location.EXTERIOR;
  }
  
  public static int dimension(int dimLoc) {
    switch (dimLoc) {
    case POINT_INTERIOR:
      return Dimension.P;
    case LINE_INTERIOR:
    case LINE_BOUNDARY:
      return Dimension.L;
    case AREA_INTERIOR:
    case AREA_BOUNDARY:
      return Dimension.A;
    }
    return Dimension.FALSE;
  }
  
  public static int dimension(int dimLoc, int exteriorDim) {
     if (dimLoc == EXTERIOR)
      return exteriorDim;
    return dimension(dimLoc);
  }

}
