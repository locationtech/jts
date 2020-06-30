/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.util.Assert;

/**
 * Computes an overlay where one input is Point(s) and one is not.
 * This class supports overlay being used as an efficient way
 * to find points within or outside a polygon.
 * <p>
 * Input semantics are:
 * <ul>
 * <li>Duplicates are removed from Point output 
 * <li>Non-point output is rounded and noded using the given precision model
 * </ul>
 * Output semantics are:
 * <ul>
 * <ii>An empty result is an empty atomic geometry 
 *     with dimension determined by the inputs and the operation,
 *     as per overlay semantics<li>
 * </ul>
 * For efficiency the following optimizations are used:
 * <ul>
 * <li>Input points are not included in the noding of the non-point input geometry
 * (in particular, they do not participate in snap-rounding if that is used).
 * <li>If the non-point input geometry is not included in the output
 * it is not rounded and noded.  This means that points 
 * are compared to the non-rounded geometry.
 * This will be apparent in the result.
 * </ul>
 * 
 * @author Martin Davis
 *
 */
class OverlayMixedPoints {

  public static Geometry overlay(int opCode, Geometry geom0, Geometry geom1, PrecisionModel pm) {
    OverlayMixedPoints overlay = new OverlayMixedPoints(opCode, geom0, geom1, pm);
    return overlay.getResult();
  }

  private final int opCode;
  private final PrecisionModel pm;
  private final Geometry geomPoint;
  private final Geometry geomNonPointInput;
  private final GeometryFactory geometryFactory;
  private final boolean isPointRHS;
  
  private Geometry geomNonPoint;
  private int geomNonPointDim;
  private PointOnGeometryLocator locator;
  private int resultDim;

  public OverlayMixedPoints(int opCode, Geometry geom0, Geometry geom1, PrecisionModel pm) {
    this.opCode = opCode;
    this.pm = pm;
    geometryFactory = geom0.getFactory();
    resultDim = OverlayUtil.resultDimension(opCode, geom0.getDimension(), geom1.getDimension());

    // name the dimensional geometries
    if (geom0.getDimension() == 0) {
      this.geomPoint = geom0;
      this.geomNonPointInput = geom1;
      this.isPointRHS = false;
    }
    else {
      this.geomPoint = geom1;
      this.geomNonPointInput = geom0;
      this.isPointRHS = true;
    }
  }
  
  public Geometry getResult() {
    // reduce precision of non-point input, if required
    geomNonPoint = prepareNonPoint(geomNonPointInput);
    geomNonPointDim = geomNonPoint.getDimension();
    locator = createLocator(geomNonPoint);
    
    Coordinate[] coords = extractCoordinates(geomPoint, pm);

    switch (opCode) {
    case OverlayNG.INTERSECTION: 
      return computeIntersection(coords);
    case OverlayNG.UNION: 
    case OverlayNG.SYMDIFFERENCE: 
      // UNION and SYMDIFFERENCE have same output
      return computeUnion(coords);
    case OverlayNG.DIFFERENCE: 
      return computeDifference(coords);
    }
    Assert.shouldNeverReachHere("Unknown overlay op code");
    return null;
  }

  private PointOnGeometryLocator createLocator(Geometry geomNonPoint) {
    if (geomNonPointDim == 2) {
      return new IndexedPointInAreaLocator(geomNonPoint);
    }
    else {
      return new IndexedPointOnLineLocator(geomNonPoint);
    }
  }

  private Geometry prepareNonPoint(Geometry geomInput) {
    // if non-point not in output no need to node it
    if (resultDim == 0) {
      return geomInput;
    }
    
    // Node and round the non-point geometry for output
    Geometry geomPrep = OverlayNG.union(geomNonPointInput, pm);
    return geomPrep;
  }

  private Geometry computeIntersection(Coordinate[] coords) {
    return createPointResult(findPoints(true, coords));
  }

  private Geometry computeUnion(Coordinate[] coords) {
    List<Point> resultPointList = findPoints(false, coords);
    List<LineString> resultLineList = null;
    if (geomNonPointDim == 1) {
      resultLineList = extractLines(geomNonPoint);
    }
    List<Polygon> resultPolyList = null;
    if (geomNonPointDim == 2) {
      resultPolyList = extractPolygons(geomNonPoint);
    }
    
    return OverlayUtil.createResultGeometry(resultPolyList, resultLineList, resultPointList, geometryFactory);
  }

  private Geometry computeDifference(Coordinate[] coords) {
    if (isPointRHS) {
      return copyNonPoint();
    }
    return createPointResult(findPoints(false, coords));
  }
  
  private Geometry createPointResult(List<Point> points) {
    if (points.size() == 0) {
      return geometryFactory.createEmpty(0);
    }
    else if (points.size() == 1) {
      return points.get(0);
    }
    Point[] pointsArray = GeometryFactory.toPointArray(points);
    return geometryFactory.createMultiPoint( pointsArray );
  }

  private List<Point> findPoints(boolean isCovered, Coordinate[] coords) {
    Set<Coordinate> resultCoords = new HashSet<Coordinate>();
    // keep only points contained
    for (Coordinate coord : coords) {
      if (hasLocation(isCovered, coord)) {
        // copy coordinate to avoid aliasing
        resultCoords.add(coord.copy());
      }
    }
    return createPoints(resultCoords);
  }
  
  private List<Point> createPoints(Set<Coordinate> coords) {
    List<Point> points = new ArrayList<Point>();
    for (Coordinate coord : coords) {
      Point point = geometryFactory.createPoint(coord); 
      points.add(point);
    }
    return points;
  }

  private boolean hasLocation(boolean isCovered, Coordinate coord) {
    boolean isExterior = Location.EXTERIOR == locator.locate(coord);
    if (isCovered) {
      return ! isExterior;
    }
    return isExterior;
  }

  /**
   * Copy the non-point input geometry if not
   * already done by precision reduction process.
   * 
   * @return a copy of the non-point geometry
   */
  private Geometry copyNonPoint() {
    if (geomNonPointInput != geomNonPoint) 
      return geomNonPoint;
    return geomNonPoint.copy();
  }
  
  private static Coordinate[] extractCoordinates(Geometry points, PrecisionModel pm) {
    CoordinateList coords = new CoordinateList();
    int n = points.getNumGeometries();
    for (int i = 0; i < n; i++) {
      Point point = (Point) points.getGeometryN(i);
      if (point.isEmpty()) continue;
      Coordinate coord = OverlayUtil.round(point, pm);
      coords.add(coord, true);
    }
    return coords.toCoordinateArray();
  }
  
  private static List<Polygon> extractPolygons(Geometry geom) {
    List<Polygon> list = new ArrayList<Polygon>();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Polygon poly = (Polygon) geom.getGeometryN(i);
      if(! poly.isEmpty()) {
        list.add(poly);
      }
    }
    return list;
  }

  private static List<LineString> extractLines(Geometry geom) {
    List<LineString> list = new ArrayList<LineString>();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      LineString line = (LineString) geom.getGeometryN(i);
      if (! line.isEmpty()) {
        list.add(line);
      }
    }
    return list;
  }
}

