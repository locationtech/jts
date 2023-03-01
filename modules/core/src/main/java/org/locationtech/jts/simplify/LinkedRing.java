/*
 * Copyright (c) 2021 Martin Davis.
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
import org.locationtech.jts.geom.CoordinateList;

class LinkedRing {
  
  private static final int NO_COORD_INDEX = -1;

  private final Coordinate[] coord;
  private int[] next = null;
  private int[] prev = null;
  private int size;
  
  public LinkedRing(Coordinate[] pts) {
    coord = pts;
    size = pts.length - 1;
    next = createNextLinks(size);
    prev = createPrevLinks(size);
  }

  private static int[] createNextLinks(int size) {
    int[] next = new int[size];
    for (int i = 0; i < size; i++) {
      next[i] = i + 1;
    }
    next[size - 1] = 0;
    return next;
  }
  
  private static int[] createPrevLinks(int size) {
    int[] prev = new int[size];
    for (int i = 0; i < size; i++) {
      prev[i] = i - 1;
    }
    prev[0] = size - 1;
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
    return index >= 0 && index < prev.length 
        && prev[index] != NO_COORD_INDEX;
  }
  
  public void remove(int index) {
    int iprev = prev[index];
    int inext = next[index];
    next[iprev] = inext;
    prev[inext] = iprev;
    prev[index] = NO_COORD_INDEX;
    next[index] = NO_COORD_INDEX;
    size--;
  }
  
  public Coordinate[] getCoordinates() {
    CoordinateList coords = new CoordinateList();
    for (int i = 0; i < coord.length - 1; i++) {
      if (prev[i] != NO_COORD_INDEX) {
        coords.add(coord[i].copy(), false);
      }
    }
    coords.closeRing();
    return coords.toCoordinateArray();
  }
}