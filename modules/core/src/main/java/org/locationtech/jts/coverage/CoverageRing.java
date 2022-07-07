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
package org.locationtech.jts.coverage;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.SegmentString;

class CoverageRing extends BasicSegmentString {
  
  public static List<CoverageRing> createRings(Geometry geom)
  {
    List<CoverageRing> rings = new ArrayList<CoverageRing>();
    List<Polygon> polygons = PolygonExtracter.getPolygons(geom);
    for (Polygon poly : polygons) {
      createRings(poly, rings);
    }
    return rings;
  }

  private static void createRings(Polygon poly, List<CoverageRing> rings) {
    rings.add( createRing(poly.getExteriorRing(), true));
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      rings.add( createRing(poly.getInteriorRingN(i), false));
    }
  }

  private static CoverageRing createRing(LinearRing ring, boolean isShell) {
    Coordinate[] pts = ring.getCoordinates();
    boolean isCCW = Orientation.isCCW(pts);
    boolean isInteriorOnRight = isShell ? ! isCCW : isCCW;
    return new CoverageRing(pts, isInteriorOnRight);
  }
  
  public static boolean isAllValid(List<CoverageRing> rings) {
    for (CoverageRing ring : rings) {
      if (! ring.isAllValid())
        return false;
    }
    return true;
  }
  
  private boolean isInteriorOnRight;
  private boolean[] isInvalid;
  private boolean[] isValid;

  public CoverageRing(Coordinate[] pts, boolean isInteriorOnRight) {
    super(pts, null);
    this.isInteriorOnRight = isInteriorOnRight;
    isInvalid = new boolean[size() - 1];
    isValid = new boolean[size() - 1];
  }
  
  public boolean isInteriorOnRight() {
    return isInteriorOnRight;
  }
  
  public boolean isValid(int index) {
    return isValid[index];
  }

  public boolean isInvalid(int index) {
    //return ! isValid[index] && isInvalid[index];
    return isInvalid[index];
  }
  
  public boolean isAllValid() {
    for (int i = 0; i < isValid.length; i++) {
      if (! isValid[i])
        return false;
    }
    return true;
  }

  public boolean isKnown(int i) {
    return isValid[i] || isInvalid[i];
  } 
  
  public Coordinate findVertexPrev(int index, Coordinate pt) {
    int iPrev = index;
    Coordinate prev = getCoordinate(iPrev);
    while (pt.equals2D(prev)) {
      iPrev = prev(iPrev);
      prev = getCoordinate(iPrev);
    }
    return prev;
  }

  public Coordinate findVertexNext(int index, Coordinate pt) {
    //-- safe, since index is always the start of a segment
    int iNext = index + 1;
    Coordinate next = getCoordinate(iNext);
    while (pt.equals2D(next)) {
      iNext = next(iNext);
      next = getCoordinate(iNext);
    }
    return next;
  }
  
  public int prev(int index) {
    if (index == 0)
      return size() - 2;
    return index - 1;
  }
  
  public int next(int index) {
    if (index < size() - 2) 
      return index + 1;
    return 0;
  }
  
  public void markInvalid(int i) {
    if (isValid[i])
      throw new IllegalStateException("Setting valid edge to invalid");
    isInvalid[i] = true;
  }

  public void markValid(int i) {
    if (isInvalid[i])
      throw new IllegalStateException("Setting invalid edge to valid");
    isValid[i] = true;
  }

  public void createInvalidLines(GeometryFactory geomFactory, List<LineString> lines) {
    int endIndex = 0;
    while (true) {
      int startIndex = findInvalidStart(endIndex); 
      if (startIndex >= size() - 1)
        break;
      endIndex = findInvalidEnd(startIndex);
      LineString line = createLine(startIndex, endIndex, geomFactory);
      lines.add(line);
    }
  }

  private int findInvalidStart(int index) {
    while (index < isInvalid.length && ! isInvalid(index)) {
      index++;
    }
    return index;
  }

  private int findInvalidEnd(int index) {
    index++;
    while (index < isInvalid.length && isInvalid(index)) {
      index++;
    }
    return index;
  }
  
  private LineString createLine(int startIndex, int endIndex, GeometryFactory geomFactory) {
    Coordinate[] pts = new Coordinate[endIndex - startIndex + 1];
    int ipts = 0;
    for (int i = startIndex; i < endIndex + 1; i++) {
      pts[ipts++] = getCoordinate(i).copy();
    }
    return geomFactory.createLineString(pts);
  }



}
