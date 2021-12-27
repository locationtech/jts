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
package org.locationtech.jts.algorithm.hull;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Triangle;

class LinkedRing {
  
  private static final int NO_VERTEX_INDEX = -1;

  private final Coordinate[] vertex;
  private int[] next = null;
  private int[] prev = null;
  private int size;
  
  public LinkedRing(Coordinate[] pts) {
    vertex = pts;
    size = pts.length - 1;
    next = createNextLinks(size);
    prev = createPrevLinks(size);
  }

  public int size() {
    return size;
  }

  public Coordinate getCoordinate(int index) {
    return vertex[index];
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
  
  public boolean isConvex(int index) {
    Coordinate pp = vertex[prev[index]];
    Coordinate p = vertex[index];
    Coordinate pn = vertex[next[index]];
    return Orientation.CLOCKWISE == Orientation.index(pp, p, pn);
  }

  public int next(int i) {
    return next[i];
  }

  public int prev(int i) {
    return prev[i];
  }

  public boolean hasVertex(int index) {
    return index < prev.length 
        && prev[index] != NO_VERTEX_INDEX;
  }
  public double area(int index) {
    Coordinate pp = vertex[prev[index]];
    Coordinate p = vertex[index];
    Coordinate pn = vertex[next[index]];
    return Triangle.area(pp,  p,  pn);
  }
  
  public void remove(int index) {
    int iprev = prev[index];
    int inext = next[index];
    next[iprev] = inext;
    prev[inext] = iprev;
    prev[index] = NO_VERTEX_INDEX;
    next[index] = NO_VERTEX_INDEX;
    size--;
  }
  
  public Coordinate[] getCoordinates() {
    CoordinateList coords = new CoordinateList();
    for (int i = 0; i < vertex.length - 1; i++) {
      if (prev[i] != NO_VERTEX_INDEX) {
        coords.add(vertex[i].copy(), false);
      }
    }
    coords.closeRing();
    return coords.toCoordinateArray();
  }
}