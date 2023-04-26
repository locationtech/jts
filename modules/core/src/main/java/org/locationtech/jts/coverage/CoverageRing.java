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
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.noding.BasicSegmentString;

class CoverageRing extends BasicSegmentString {
  
  public static List<CoverageRing> createRings(Geometry geom)
  {
    List<Polygon> polygons = PolygonExtracter.getPolygons(geom);
    return createRings(polygons);
  }

  public static List<CoverageRing> createRings(List<Polygon> polygons) {
    List<CoverageRing> rings = new ArrayList<CoverageRing>();
    for (Polygon poly : polygons) {
      createRings(poly, rings);
    }
    return rings;   
  }

  private static void createRings(Polygon poly, List<CoverageRing> rings) {
    if (poly.isEmpty())
      return;
    addRing(poly.getExteriorRing(), true, rings);
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      addRing( poly.getInteriorRingN(i), false, rings);
    }
  }

  private static void addRing(LinearRing ring, boolean isShell, List<CoverageRing> rings) {
    if (ring.isEmpty())
      return;
    rings.add( createRing(ring, isShell));
  }
  
  private static CoverageRing createRing(LinearRing ring, boolean isShell) {
    Coordinate[] pts = ring.getCoordinates();
    if (CoordinateArrays.hasRepeatedOrInvalidPoints(pts)) {
      pts = CoordinateArrays.removeRepeatedOrInvalidPoints(pts);
    }
    boolean isCCW = Orientation.isCCW(pts);
    boolean isInteriorOnRight = isShell ? ! isCCW : isCCW;
    return new CoverageRing(pts, isInteriorOnRight);
  }
  
  /**
   * Tests if all rings have known status (matched or invalid)
   * for all segments.
   * 
   * @param rings a list of rings
   * @return true if all ring segments have known status
   */
  public static boolean isKnown(List<CoverageRing> rings) {
    for (CoverageRing ring : rings) {
      if (! ring.isKnown())
        return false;
    }
    return true;
  }
  
  private boolean isInteriorOnRight;
  private boolean[] isInvalid;
  private boolean[] isMatched;

  private CoverageRing(Coordinate[] pts, boolean isInteriorOnRight) {
    super(pts, null);
    this.isInteriorOnRight = isInteriorOnRight;
    isInvalid = new boolean[size() - 1];
    isMatched = new boolean[size() - 1];
  }
  
  /**
   * Reports if the ring has canonical orientation,
   * with the polygon interior on the right (shell is CW).
   * 
   * @return true if the polygon interior is on the right
   */
  public boolean isInteriorOnRight() {
    return isInteriorOnRight;
  }

  
  /**
   * Marks a segment as invalid.
   * 
   * @param i the segment index
   */
  public void markInvalid(int i) {
    isInvalid[i] = true;
  }

  /**
   * Marks a segment as valid.
   * 
   * @param i the segment index
   */
  public void markMatched(int i) {
    //if (isInvalid[i])
    //  throw new IllegalStateException("Setting invalid edge to matched");
    isMatched[i] = true;
  }
  
  /**
   * Tests if all segments in the ring have known status
   * (matched or invalid).
   * 
   * @return true if all segments have known status
   */
  public boolean isKnown() {
    for (int i = 0; i < isMatched.length; i++) {
      if (! (isMatched[i] && isInvalid[i]))
        return false;
    }
    return true;
  }
  
  /**
   * Tests if a segment is marked invalid.
   * 
   * @param index the segment index
   * @return true if the segment is invalid
   */
  public boolean isInvalid(int index) {
    return isInvalid[index];
  }
  
  /**
   * Tests whether all segments are invalid.
   * 
   * @return true if all segments are invalid
   */
  public boolean isInvalid() {
    for (int i = 0; i < isInvalid.length; i++) {
      if (! isInvalid[i])
        return false;
    }
    return true;
  }

  /**
   * Tests whether any segment is invalid.
   * 
   * @return true if some segment is invalid
   */
  public boolean hasInvalid() {
    for (int i = 0; i < isInvalid.length; i++) {
      if (isInvalid[i])
        return true;
    }
    return false;
  }

  /**
   * Tests whether the matched/invalid state of a ring segment is known.
   * 
   * @param i the index of the ring segment
   * @return true if the segment state is known
   */
  public boolean isKnown(int i) {
    return isMatched[i] || isInvalid[i];
  } 
  
  /**
   * Finds the previous vertex in the ring which is distinct from a given coordinate value.
   * 
   * @param index the index to start the search
   * @param pt a coordinate value (which may not be a ring vertex)
   * @return the previous distinct vertex in the ring
   */
  public Coordinate findVertexPrev(int index, Coordinate pt) {
    int iPrev = index;
    Coordinate prev = getCoordinate(iPrev);
    while (pt.equals2D(prev)) {
      iPrev = prev(iPrev);
      prev = getCoordinate(iPrev);
    }
    return prev;
  }

  /**
   * Finds the next vertex in the ring which is distinct from a given coordinate value.
   * 
   * @param index the index to start the search
   * @param pt a coordinate value (which may not be a ring vertex)
   * @return the next distinct vertex in the ring
   */
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
  
  /**
   * Gets the index of the previous segment in the ring.
   * 
   * @param index a segment index
   * @return the index of the previous segment
   */
  public int prev(int index) {
    if (index == 0)
      return size() - 2;
    return index - 1;
  }
  
  /**
   * Gets the index of the next segment in the ring.
   * 
   * @param index a segment index
   * @return the index of the next segment
   */
  public int next(int index) {
    if (index < size() - 2) 
      return index + 1;
    return 0;
  }

  public void createInvalidLines(GeometryFactory geomFactory, List<LineString> lines) {
    //-- empty case
    if (! hasInvalid()) {
      return;
    }
    //-- entire ring case
    if (isInvalid()) {
      LineString line = createLine(0, size() - 1, geomFactory);
      lines.add(line);
      return;
    }
    
    //-- find first end after index 0, to allow wrap-around
    int startIndex = findInvalidStart(0);
    int firstEndIndex = findInvalidEnd(startIndex);
    int endIndex = firstEndIndex;
    while (true) {
      startIndex = findInvalidStart(endIndex); 
      endIndex = findInvalidEnd(startIndex);
      LineString line = createLine(startIndex, endIndex, geomFactory);
      lines.add(line);
      if (endIndex == firstEndIndex)
        break;
    }
  }

  private int findInvalidStart(int index) {
    while (! isInvalid(index)) {
      index = nextMarkIndex(index);
    }
    return index;
  }

  private int findInvalidEnd(int index) {
    index = nextMarkIndex(index);
    while (isInvalid(index)) {
      index = nextMarkIndex(index);
    }
    return index;
  }
  
  private int nextMarkIndex(int index) {
    if (index >= isInvalid.length - 1) {
      return 0;
    }
    return index + 1;
  }

  /**
   * Creates a line from a sequence of ring segments between startIndex and endIndex (inclusive).
   * If the endIndex < startIndex the sequence wraps around the ring endpoint.
   * 
   * @param startIndex
   * @param endIndex
   * @param geomFactory
   * @return a line representing the section
   */
  private LineString createLine(int startIndex, int endIndex, GeometryFactory geomFactory) {
    Coordinate[] pts = endIndex < startIndex ?
          extractSectionWrap(startIndex, endIndex)
        : extractSection(startIndex, endIndex);    
    return geomFactory.createLineString(pts);
  }

  private Coordinate[] extractSection(int startIndex, int endIndex) {
    int size = endIndex - startIndex + 1;
    Coordinate[] pts = new Coordinate[size];
    int ipts = 0;
    for (int i = startIndex; i <= endIndex; i++) {
      pts[ipts++] = getCoordinate(i).copy();
    }
    return pts;
  }

  private Coordinate[] extractSectionWrap(int startIndex, int endIndex) {
    int size = endIndex + (size() - startIndex);
    Coordinate[] pts = new Coordinate[size];
    int index = startIndex;
    for (int i = 0; i < size; i++) {
      pts[i] = getCoordinate(index).copy();
      index = nextMarkIndex(index);
    }
    return pts;
  }

}
