/*
 * Copyright (c) 2019 Martin Davis.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Performs an overlay operation on inputs which are both point geometries.
 * <p>
 * Semantics are:
 * <ul>
 * <li>Points are rounded to the precision model if provided
 * <li>Points with identical XY values are merged to a single point
 * <li>Extended ordinate values are preserved in the output, 
 * apart from merging
 * <li>An empty result is returned as <code>POINT EMPTY</code>
 * </ul>
 * 
 * @author Martin Davis
 *
 */
class OverlayPoints {

  /**
   * Performs an overlay operation on inputs which are both point geometries.
   * 
   * @param geom0 the first geometry argument
   * @param geom1 the second geometry argument
   * @param opCode the code for the desired overlay operation
   * @param pm the precision model to use
   * @return the result of the overlay operation
   */
  public static Geometry overlay(int opCode, Geometry geom0, Geometry geom1, PrecisionModel pm) {
    OverlayPoints overlay = new OverlayPoints(opCode, geom0, geom1, pm);
    return overlay.getResult();
  }

  private int opCode;
  private Geometry geom0;
  private Geometry geom1;
  private PrecisionModel pm;
  private GeometryFactory geometryFactory;
  private ArrayList<Point> resultList;

  /**
   * Creates an instance of an overlay operation on inputs which are both point geometries.
   * 
   * @param geom0 the first geometry argument
   * @param geom1 the second geometry argument
   * @param opCode the code for the desired overlay operation
   * @param pm the precision model to use
   */
  public OverlayPoints(int opCode, Geometry geom0, Geometry geom1, PrecisionModel pm) {
    this.opCode = opCode;
    this.geom0 = geom0;
    this.geom1 = geom1;
    this.pm = pm;
    geometryFactory = geom0.getFactory();
  }
  
  /**
   * Gets the result of the overlay.
   * 
   * @return the overlay result
   */
  public Geometry getResult() {
    Map<Coordinate, Point> map0 = buildPointMap(geom0);
    Map<Coordinate, Point> map1 = buildPointMap(geom1);
    
    resultList = new ArrayList<Point>();
    switch (opCode) {
    case OverlayNG.INTERSECTION: 
      computeIntersection(map0, map1, resultList);
      break;
    case OverlayNG.UNION: 
      computeUnion(map0, map1, resultList);
      break;
    case OverlayNG.DIFFERENCE: 
      computeDifference(map0, map1, resultList);
      break;
    case OverlayNG.SYMDIFFERENCE: 
      computeDifference(map0, map1, resultList);
      computeDifference(map1, map0, resultList);
      break;
    }
    if (resultList.isEmpty())
      return OverlayUtil.createEmptyResult(0, geometryFactory);
    
    return geometryFactory.buildGeometry(resultList);
  }

  private void computeIntersection(Map<Coordinate, Point> map0, Map<Coordinate, Point> map1, 
      ArrayList<Point> resultList) {
    for ( Entry<Coordinate, Point> entry : map0.entrySet()) {
      if (map1.containsKey(entry.getKey())) {
        resultList.add( copyPoint( entry.getValue() ) );
      }
    }
  }

  private void computeDifference(Map<Coordinate, Point> map0, Map<Coordinate, Point> map1, 
      ArrayList<Point> resultList) {
    for ( Entry<Coordinate, Point> entry : map0.entrySet()) {
      if (! map1.containsKey(entry.getKey())) {
        resultList.add( copyPoint( entry.getValue() ) );
      }
    }
  }

  private void computeUnion(Map<Coordinate, Point> map0, Map<Coordinate, Point> map1, 
      ArrayList<Point> resultList) {
    
    // copy all A points
    for (Point p : map0.values()) {
      resultList.add( copyPoint( p ) );
    }
    
    for ( Entry<Coordinate, Point> entry : map1.entrySet()) {
      if (! map0.containsKey(entry.getKey())) {
        resultList.add( copyPoint( entry.getValue() ) );
      }
    }
  }

  private Point copyPoint(Point pt) {
    // if pm is floating, the point coordinate is not changed
    if (OverlayUtil.isFloating(pm))
      return (Point) pt.copy();
    
    // pm is fixed.  Round off X&Y ordinates, copy other ordinates unchanged
    CoordinateSequence seq = pt.getCoordinateSequence();
    CoordinateSequence seq2 = seq.copy();
    seq2.setOrdinate(0, CoordinateSequence.X, pm.makePrecise(seq.getX(0)));
    seq2.setOrdinate(0, CoordinateSequence.Y, pm.makePrecise(seq.getY(0)));
    return geometryFactory.createPoint(seq2);
  }

  private HashMap<Coordinate, Point> buildPointMap(Geometry geom) {
    HashMap<Coordinate, Point> map = new HashMap<Coordinate, Point>();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Geometry elt = geom.getGeometryN(i);
      if (! (elt instanceof Point) ) {
        throw new IllegalArgumentException("Non-point geometry input to point overlay");
      }
      // don't add empty points
      if (elt.isEmpty()) continue;
      
      Point pt = (Point) elt;
      Coordinate p = roundCoord(pt, pm);
      /**
       * Only add first occurrence of a point.
       * This provides the merging semantics of overlay
       */
      if (! map.containsKey(p))
        map.put(p, pt);
    }
    return map;
  }

  /**
   * Round the key point if precision model is fixed.
   * Note: return value is only copied if rounding is performed.
   * 
   * @param pt
   * @return
   */
  static Coordinate roundCoord(Point pt, PrecisionModel pm) {
    Coordinate p = pt.getCoordinate();
    if (OverlayUtil.isFloating(pm)) 
      return p;
    Coordinate p2 = p.copy();
    pm.makePrecise(p2);
    return p2;
  }

}
