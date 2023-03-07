/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.simplify;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.io.WKTWriter;

public class LinkedLine {
  
  private static final int NO_COORD_INDEX = -1;

  private final Coordinate[] coord;
  private boolean isRing;
  private int size;
  private int[] next = null;
  private int[] prev = null;

  public LinkedLine(Coordinate[] pts) {
    coord = pts;
    isRing = CoordinateArrays.isRing(pts);
    size = isRing ? pts.length - 1 : pts.length;
    next = createNextLinks(size);
    prev = createPrevLinks(size);
  }

  public boolean isRing() {
    return isRing;
  }
  
  public boolean isCorner(int i) {
    if (! isRing() 
        && (i == 0 || i == coord.length - 1))
        return false;
    return true;
  }
  
  private int[] createNextLinks(int size) {
    int[] next = new int[size];
    for (int i = 0; i < size; i++) {
      next[i] = i + 1;
    }
    next[size - 1] = isRing ? 0 : NO_COORD_INDEX;
    return next;
  }
  
  private int[] createPrevLinks(int size) {
    int[] prev = new int[size];
    for (int i = 0; i < size; i++) {
      prev[i] = i - 1;
    }
    prev[0] = isRing ? size - 1 : NO_COORD_INDEX;
    return prev;
  }
  
  public int size() {
    return size;
  }

  public int next(int i) {
    return next[i];
  }

  public int prev(int i) {
    return prev[i];
  }
  
  public Coordinate getCoordinate(int index) {
    return coord[index];
  }

  public Coordinate prevCoordinate(int index) {
    return coord[prev(index)];
  }

  public Coordinate nextCoordinate(int index) {
    return coord[next(index)];
  }  
  
  public boolean hasCoordinate(int index) {
    //-- if not a ring, endpoints are alway present
    if (! isRing && (index == 0 || index == coord.length - 1))
        return true;
    return index >= 0 
        && index < prev.length
        && prev[index] != NO_COORD_INDEX;
  }
  
  public void remove(int index) {
    int iprev = prev[index];
    int inext = next[index];
    if (iprev != NO_COORD_INDEX) next[iprev] = inext;
    if (inext != NO_COORD_INDEX) prev[inext] = iprev;
    prev[index] = NO_COORD_INDEX;
    next[index] = NO_COORD_INDEX;
    size--;
  }
  
  public Coordinate[] getCoordinates() {
    CoordinateList coords = new CoordinateList();
    int len = isRing ? coord.length - 1 : coord.length;
    for (int i = 0; i < len; i++) {
      if (hasCoordinate(i)) {
        coords.add(coord[i].copy(), false);
      }
    }
    if (isRing) {
      coords.closeRing();
    }
    return coords.toCoordinateArray();
  }
  
  public String toString() {
    return WKTWriter.toLineString(getCoordinates());
  }
}