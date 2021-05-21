/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;

/**
 * Computes the boundary of a {@link Geometry}.
 * Allows specifying the {@link BoundaryNodeRule} to be used.
 * This operation will always return a {@link Geometry} of the appropriate
 * dimension for the boundary (even if the input geometry is empty).
 * The boundary of zero-dimensional geometries (Points) is
 * always the empty {@link GeometryCollection}.
 *
 * @author Martin Davis
 * @version 1.7
 */

public class BoundaryOp
{
  /**
   * Computes a geometry representing the boundary of a geometry.
   * 
   * @param g the input geometry
   * @return the computed boundary
   */
  public static Geometry getBoundary(Geometry g)
  {
    BoundaryOp bop = new BoundaryOp(g);
    return bop.getBoundary();
  }
  
  /**
   * Computes a geometry representing the boundary of a geometry,
   * using an explicit {@link BoundaryNodeRule}.
   * 
   * @param g the input geometry
   * @param bnRule the Boundary Node Rule to use
   * @return the computed boundary
   */
  public static Geometry getBoundary(Geometry g, BoundaryNodeRule bnRule)
  {
    BoundaryOp bop = new BoundaryOp(g, bnRule);
    return bop.getBoundary();
  }
  
  /**
   * Tests if a geometry has a boundary (it is non-empty).
   * The semantics are:
   * <ul>
   * <li>Empty geometries do not have boundaries. 
   * <li>Points do not have boundaries.
   * <li>For linear geometries the existence of the boundary 
   * is determined by the {@link BoundaryNodeRule}.
   * <li>Non-empty polygons always have a boundary.
   * </ul>
   * 
   * @param geom the geometry providing the boundary
   * @param boundaryNodeRule  the Boundary Node Rule to use
   * @return true if the boundary exists
   */
  public static boolean hasBoundary(Geometry geom, BoundaryNodeRule boundaryNodeRule) {
    // Note that this does not handle geometry collections with a non-empty linear element
    if (geom.isEmpty()) return false;
    switch (geom.getDimension()) {
    case Dimension.P: return false;
    /**
     * Linear geometries might have an empty boundary due to boundary node rule.
     */
    case Dimension.L:
      Geometry boundary = BoundaryOp.getBoundary(geom, boundaryNodeRule);
      return ! boundary.isEmpty();
    case Dimension.A: return true;
    }
    return true;
  }
  
  private Geometry geom;
  private GeometryFactory geomFact;
  private BoundaryNodeRule bnRule;

  /**
   * Creates a new instance for the given geometry.
   * 
   * @param geom the input geometry
   */
  public BoundaryOp(Geometry geom)
  {
    this(geom, BoundaryNodeRule.MOD2_BOUNDARY_RULE);
  }

  /**
   * Creates a new instance for the given geometry.
   * 
   * @param geom the input geometry
   * @param bnRule the Boundary Node Rule to use
   */
  public BoundaryOp(Geometry geom, BoundaryNodeRule bnRule)
  {
    this.geom = geom;
    geomFact = geom.getFactory();
    this.bnRule = bnRule;
  }

  /**
   * Gets the computed boundary.
   * 
   * @return the boundary geometry
   */
  public Geometry getBoundary()
  {
    if (geom instanceof LineString) return boundaryLineString((LineString) geom);
    if (geom instanceof MultiLineString) return boundaryMultiLineString((MultiLineString) geom);
    return geom.getBoundary();
  }

  private MultiPoint getEmptyMultiPoint()
  {
    return geomFact.createMultiPoint();
  }

  private Geometry boundaryMultiLineString(MultiLineString mLine)
  {
    if (geom.isEmpty()) {
      return getEmptyMultiPoint();
    }

    Coordinate[] bdyPts = computeBoundaryCoordinates(mLine);

    // return Point or MultiPoint
    if (bdyPts.length == 1) {
      return geomFact.createPoint(bdyPts[0]);
    }
    // this handles 0 points case as well
    return geomFact.createMultiPointFromCoords(bdyPts);
  }

/*
// MD - superseded
  private Coordinate[] computeBoundaryFromGeometryGraph(MultiLineString mLine)
  {
    GeometryGraph g = new GeometryGraph(0, mLine, bnRule);
    Coordinate[] bdyPts = g.getBoundaryPoints();
    return bdyPts;
  }
*/

  private Map endpointMap;

  private Coordinate[] computeBoundaryCoordinates(MultiLineString mLine)
  {
    List bdyPts = new ArrayList();
    endpointMap = new TreeMap();
    for (int i = 0; i < mLine.getNumGeometries(); i++) {
      LineString line = (LineString) mLine.getGeometryN(i);
      if (line.getNumPoints() == 0)
        continue;
      addEndpoint(line.getCoordinateN(0));
      addEndpoint(line.getCoordinateN(line.getNumPoints() - 1));
    }

    for (Iterator it = endpointMap.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry entry = (Map.Entry) it.next();
      Counter counter = (Counter) entry.getValue();
      int valence = counter.count;
      if (bnRule.isInBoundary(valence)) {
        bdyPts.add(entry.getKey());
      }
    }

    return CoordinateArrays.toCoordinateArray(bdyPts);
  }

  private void addEndpoint(Coordinate pt)
  {
    Counter counter = (Counter) endpointMap.get(pt);
    if (counter == null) {
      counter = new Counter();
      endpointMap.put(pt, counter);
    }
    counter.count++;
  }

  private Geometry boundaryLineString(LineString line)
  {
    if (geom.isEmpty()) {
      return getEmptyMultiPoint();
    }

    if (line.isClosed()) {
      // check whether endpoints of valence 2 are on the boundary or not
      boolean closedEndpointOnBoundary = bnRule.isInBoundary(2);
      if (closedEndpointOnBoundary) {
        return line.getStartPoint();
      }
      else {
        return geomFact.createMultiPoint();
      }
    }
    return geomFact.createMultiPoint(new Point[]{
                                     line.getStartPoint(),
                                     line.getEndPoint()
    });
  }
}

/**
 * Stores an integer count, for use as a Map entry.
 *
 * @author Martin Davis
 * @version 1.7
 */
class Counter
{
  /**
   * The value of the count
   */
  int count;
}
