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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.ComponentCoordinateExtracter;
import org.locationtech.jts.geom.util.PointExtracter;

class RelateGeometry {

  public static final boolean GEOM_A = true;
  public static final boolean GEOM_B = false;
  
  public static String name(boolean isA) {
    return isA ? "A" : "B";
  }
  
  private Geometry geom;
  private boolean isPrepared = false;
  
  private Envelope geomEnv;
  private int geomDim = Dimension.FALSE;
  private Set<Coordinate> uniquePoints;
  private BoundaryNodeRule boundaryNodeRule;
  private RelatePointLocator locator;
  private int elementId = 0;
  private boolean hasPoints;
  private boolean hasLines;
  private boolean hasAreas;
  private boolean isLineZeroLen;
  private boolean isGeomEmpty;

  public RelateGeometry(Geometry input) {
    this(input, false, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE);
  }
  
  public RelateGeometry(Geometry input, BoundaryNodeRule bnRule) {
    this(input, false, bnRule);   
  }
  
  public RelateGeometry(Geometry input, boolean isPrepared, BoundaryNodeRule bnRule) {
    this.geom = input;
    this.geomEnv = input.getEnvelopeInternal();
    this.isPrepared = isPrepared;
    this.boundaryNodeRule = bnRule;
    //-- cache geometry metadata
    isGeomEmpty = geom.isEmpty();
    geomDim = input.getDimension();
    analyzeDimensions();
    isLineZeroLen = isZeroLengthLine(geom);
  }
  
  private boolean isZeroLengthLine(Geometry geom) {
    // avoid expensive zero-length calculation if not linear
    if (getDimension() != Dimension.L)
      return false;
    return isZeroLength(geom);
  }

  private void analyzeDimensions() {
    if (isGeomEmpty) {
      return;
    }
    if (geom instanceof Point || geom instanceof MultiPoint) {
      hasPoints = true;
      geomDim = Dimension.P;
      return;
    }
    if (geom instanceof LineString || geom instanceof MultiLineString) {
      hasLines = true;
      geomDim = Dimension.L;
      return;
    }
    if (geom instanceof Polygon || geom instanceof MultiPolygon) {
      hasAreas = true;
      geomDim = Dimension.A;
      return;
    }
    //-- analyze a (possibly mixed type) collection
    Iterator geomi = new GeometryCollectionIterator(geom);
    while (geomi.hasNext()) {
      Geometry elem = (Geometry) geomi.next();
      if (elem.isEmpty()) 
        continue;
      if (elem instanceof Point) {
        hasPoints = true;
        if (geomDim < Dimension.P) geomDim = Dimension.P;
      }
      if (elem instanceof LineString) {
        hasLines = true;
        if (geomDim < Dimension.L) geomDim = Dimension.L;
      }
      if (elem instanceof Polygon) {
        hasAreas = true;
        if (geomDim < Dimension.A) geomDim = Dimension.A;
      }
    }
  }
  
  /**
   * Tests if all geometry linear elements are zero-length.
   * For efficiency the test avoids computing actual length.
   * 
   * @param geom
   * @return
   */
  private static boolean isZeroLength(Geometry geom) {
    Iterator geomi = new GeometryCollectionIterator(geom);
    while (geomi.hasNext()) {
      Geometry elem = (Geometry) geomi.next();
      if (elem instanceof LineString) {
        if (! isZeroLength((LineString) elem))
          return false;
      }
    }
    return true;
  }

  private static boolean isZeroLength(LineString line) {
    if (line.getNumPoints() >= 2) {
      Coordinate p0 = line.getCoordinateN(0);
      for (int i = 0 ; i < line.getNumPoints(); i++) {
        Coordinate pi = line.getCoordinateN(i);
        //-- most non-zero-len lines will trigger this right away 
        if (! p0.equals2D(pi)) 
          return false;
      }
    }
    return true;
  }

  public Geometry getGeometry() {
    return geom;
  }
  
  public boolean isPrepared() {
    return isPrepared;
  }

  public Envelope getEnvelope() {
    return geomEnv;
  }
  
  public int getDimension() {
    return geomDim;
  }

  public boolean hasDimension(int dim) {
    switch (dim) {
    case Dimension.P: return hasPoints;
    case Dimension.L: return hasLines;
    case Dimension.A: return hasAreas;
    }
    return false;
  }
  
  /**
   * Gets the actual non-empty dimension of the geometry.
   * Zero-length LineStrings are treated as Points.
   * 
   * @return the real (non-empty) dimension
   */
  public int getDimensionReal() {
    if (isGeomEmpty) return Dimension.FALSE;
    if (getDimension() == 1 && isLineZeroLen)
      return Dimension.P;
    if (hasAreas) return Dimension.A;
    if (hasLines) return Dimension.L;
    return Dimension.P;
  }

  public boolean hasEdges() {
    return hasLines || hasAreas;
  }
  
  private RelatePointLocator getLocator() {
    if (locator == null) 
      locator = new RelatePointLocator(geom, isPrepared, boundaryNodeRule);
    return locator;
  }
  
  public boolean isNodeInArea(Coordinate nodePt, Geometry parentPolygonal) {
    int loc = getLocator().locateNodeWithDim(nodePt, parentPolygonal);
    return loc == DimensionLocation.AREA_INTERIOR;  
  }
  
  public int locateLineEnd(Coordinate p) {
    return getLocator().locateLineEnd(p);
  }

  /**
   * Locates a vertex of a polygon.
   * A vertex of a Polygon or MultiPolygon is on
   * the {@link Location#BOUNDARY}.
   * But a vertex of an overlapped polygon in a GeometryCollection
   * may be in the {@link Location#INTERIOR}.
   * 
   * @param pt the polygon vertex
   * @return the location of the vertex
   */
  public int locateAreaVertex(Coordinate pt) {
    /**
     * Can pass a null polygon, because the point is an exact vertex,
     * which will be detected as being on the boundary of its polygon
     */
    return locateNode(pt, null);
  }
  
  public int locateNode(Coordinate pt, Geometry parentPolygonal) {
    return getLocator().locateNode(pt, parentPolygonal);
  }
  
  public int locateWithDim(Coordinate pt) {
    int loc = getLocator().locateWithDim(pt);
    return loc;
  }
  
  /**
   * Indicates whether the geometry requires self-noding 
   * for correct evaluation of specific spatial predicates. 
   * Self-noding is required for geometries which may self-cross
   * - i.e. lines, and overlapping elements in GeometryCollections.
   * Self-noding is not required for polygonal geometries,
   * since they can only touch at vertices.
   * 
   * @return true if self-noding is required for this geometry
   */
  public boolean isSelfNodingRequired() {
    if (geom instanceof Point
        || geom instanceof MultiPoint
        || geom instanceof Polygon
        || geom instanceof MultiPolygon)
        return false;
    //-- a GC with a single polygon does not need noding
    if (hasAreas && geom.getNumGeometries() == 1) 
      return false;
    return true;
  }

  /**
   * Tests whether the geometry has polygonal topology.
   * This is not the case if it is a GeometryCollection 
   * containing more than one polygon (since they may overlap
   * or be adjacent).
   * The significance is that polygonal topology allows more assumptions
   * about the location of boundary vertices.
   * 
   * @return true if the geometry has polygonal topology
   */
  public boolean isPolygonal() {
    //TODO: also true for a GC containing one polygonal element (and possibly some lower-dimension elements)
    return geom instanceof Polygon
        || geom instanceof MultiPolygon;
  }
  
  public boolean isEmpty() {
    return isGeomEmpty;
  }

  public boolean hasBoundary() {
    return getLocator().hasBoundary();
  }
   
  public Set<Coordinate> getUniquePoints() {
    //-- will be re-used in prepared mode
    if (uniquePoints == null) {
      uniquePoints = createUniquePoints();
    }
    return uniquePoints;
  } 
  
  private Set<Coordinate> createUniquePoints() {
    //-- only called on P geometries
    List<Coordinate> pts = ComponentCoordinateExtracter.getCoordinates(geom);
    Set<Coordinate> set = new HashSet<Coordinate>();
    set.addAll(pts);
    return set;
  }
  
  public List<Point> getEffectivePoints() {
    List<Point> ptListAll = PointExtracter.getPoints(geom);
    
    if (getDimensionReal() <= Dimension.P)
      return ptListAll;
    
    //-- only return Points not covered by another element
    List<Point> ptList = new ArrayList<Point>();
    for (Point p : ptListAll) {
      if (p.isEmpty())
        continue;
      int locDim = locateWithDim(p.getCoordinate());
      if (DimensionLocation.dimension(locDim) == Dimension.P) {
        ptList.add(p);
      }
    }
    return ptList;
  }
  
  /**
   * Extract RelateSegmentStrings from the geometry which 
   * intersect a given envelope.  
   * If the envelope is null all edges are extracted.
   * @param geomA 
   * 
   * @param env the envelope to extract around (may be null)
   * @return a list of RelateSegmentStrings
   */
  public List<RelateSegmentString> extractSegmentStrings(boolean isA, Envelope env) {
    List<RelateSegmentString> segStrings = new ArrayList<RelateSegmentString>();
    extractSegmentStrings(isA, env, geom, segStrings);
    return segStrings;
  }
  
  private void extractSegmentStrings(boolean isA, Envelope env, Geometry geom, List<RelateSegmentString> segStrings) {
    //-- record if parent is MultiPolygon
    MultiPolygon parentPolygonal = null;
    if (geom instanceof MultiPolygon) {
      parentPolygonal = (MultiPolygon) geom;
    }
    
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Geometry g = geom.getGeometryN(i);
      if (g instanceof GeometryCollection) {
        extractSegmentStrings(isA, env, g, segStrings);
      }
      else {
        extractSegmentStringsFromAtomic(isA, g, parentPolygonal, env, segStrings);
      }
    }
  }
  
  private void extractSegmentStringsFromAtomic(boolean isA, Geometry geom, MultiPolygon parentPolygonal, Envelope env, 
      List<RelateSegmentString> segStrings) {
    if (geom.isEmpty())
      return;
    boolean doExtract = env == null || env.intersects(geom.getEnvelopeInternal());
    if (! doExtract)
      return;
    
    elementId++;
    if (geom instanceof LineString) {
      RelateSegmentString ss = RelateSegmentString.createLine(geom.getCoordinates(), isA, elementId, this);
      segStrings.add(ss);
    }
    else if (geom instanceof Polygon) {
      Polygon poly = (Polygon) geom;
      Geometry parentPoly = parentPolygonal != null ? parentPolygonal : poly;
      extractRingToSegmentString(isA, poly.getExteriorRing(), 0, env, parentPoly, segStrings);
      for (int i = 0; i < poly.getNumInteriorRing(); i++) {
        extractRingToSegmentString(isA, poly.getInteriorRingN(i), i+1, env, parentPoly, segStrings);        
      }
    }
  }

  private void extractRingToSegmentString(boolean isA, LinearRing ring, int ringId, Envelope env,
      Geometry parentPoly, List<RelateSegmentString> segStrings) {
    if (ring.isEmpty())
      return;
    if (env != null && ! env.intersects(ring.getEnvelopeInternal()))
      return;

    //-- orient the points if required
    boolean requireCW = ringId == 0;
    Coordinate[] pts = orient(ring.getCoordinates(), requireCW);  
    RelateSegmentString ss = RelateSegmentString.createRing(pts, isA, elementId, ringId, parentPoly, this);
    segStrings.add(ss);  
  }

  public static Coordinate[] orient(Coordinate[] pts, boolean orientCW) {
    boolean isFlipped = orientCW == Orientation.isCCW(pts);
    if (isFlipped) {
      pts = pts.clone();
      CoordinateArrays.reverse(pts);
    }
    return pts;
  }
  
  public String toString() {
    return geom.toString();
  }


}
