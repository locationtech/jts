/*
 * Copyright (c) 2024 Martin Davis.
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
import java.util.List;
import java.util.Set;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Locates a point on a geometry, including mixed-type collections.
 * The dimension of the containing geometry element is also determined.
 * GeometryCollections are handled with union semantics;
 * i.e. the location of a point is that location of that point
 * on the union of the elements of the collection.
 * <p>
 * Union semantics for GeometryCollections has the following behaviours:
 * <ol>
 * <li>For a mixed-dimension (heterogeneous) collection
 * a point may lie on two geometry elements with different dimensions.
 * In this case the location on the largest-dimension element is reported.
 * <li>For a collection with overlapping or adjacent polygons, 
 * points on polygon element boundaries may lie in the effective interior
 * of the collection geometry.
 * </ol>
 * Prepared mode is supported via cached spatial indexes.
 * <p>
 * Supports specifying the {@link BoundaryNodeRule} to use
 * for line endpoints.
 * 
 * @author Martin Davis
 *
 */
class RelatePointLocator {
  
  private Geometry geom;
  private boolean isPrepared = false;
  private BoundaryNodeRule boundaryRule;
  private AdjacentEdgeLocator adjEdgeLocator;
  private Set<Coordinate> points;
  private List<LineString> lines;
  private List<Geometry> polygons;
  private PointOnGeometryLocator[] polyLocator;
  private LinearBoundary lineBoundary;
  private boolean isEmpty;

  public RelatePointLocator(Geometry geom) {
    this(geom, false, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE);
  }
  
  public RelatePointLocator(Geometry geom, boolean isPrepared, BoundaryNodeRule bnRule) {
    this.geom = geom;
    this.isPrepared = isPrepared;
    this.boundaryRule = bnRule;
    init(geom);
  }

  private void init(Geometry geom) {
    //-- cache empty status, since may be checked many times
    isEmpty = geom.isEmpty();
    extractElements(geom);
    
    if (lines != null) {
      lineBoundary = new LinearBoundary(lines, boundaryRule);
    }
    
    if (polygons != null) {
      polyLocator = isPrepared 
          ? new IndexedPointInAreaLocator[polygons.size()]
              : new SimplePointInAreaLocator[polygons.size()];
    }
  }

  public boolean hasBoundary() {
    return lineBoundary.hasBoundary();
  }
  
  private void extractElements(Geometry geom) {
    if (geom.isEmpty())
      return;
    
    if (geom instanceof Point) {
      addPoint((Point) geom);
    }
    else if (geom instanceof LineString) {
      addLine((LineString) geom);
    }
    else if (geom instanceof Polygon
        || geom instanceof MultiPolygon) {
      addPolygonal(geom);
    }
    else if (geom instanceof GeometryCollection){
      for (int i = 0; i < geom.getNumGeometries(); i++) {
        Geometry g = geom.getGeometryN(i);
        extractElements(g);
      }
    }
  }

  private void addPoint(Point pt) {
    if (points == null) {
      points = new HashSet<Coordinate>();
    }
    points.add(pt.getCoordinate());
  }

  private void addLine(LineString line) {
    if (lines == null) {
      lines = new ArrayList<LineString>();
    }
    lines.add(line);
  }

  private void addPolygonal(Geometry polygonal) {
    if (polygons == null) {
      polygons = new ArrayList<Geometry>();
    }
    polygons.add(polygonal);
  }
  
  public int locate(Coordinate p) {
    return DimensionLocation.location(locateWithDim(p));
  }
  
  /**
   * Locates a line endpoint, as a {@link DimensionLocation}.
   * In a mixed-dim GC, the line end point may also lie in an area.
   * In this case the area location is reported.
   * Otherwise, the dimLoc is either LINE_BOUNDARY 
   * or LINE_INTERIOR, depending on the endpoint valence
   * and the BoundaryNodeRule in place.
   * 
   * @param p the line end point to locate
   * @return the dimension and location of the line end point
   */
  public int locateLineEndWithDim(Coordinate p) {
    //-- if a GC with areas, check for point on area
    if (polygons != null) {
      int locPoly = locateOnPolygons(p, false, null);
      if (locPoly != Location.EXTERIOR)
        return DimensionLocation.locationArea(locPoly);
    }
    //-- not in area, so return line end location
    return lineBoundary.isBoundary(p) 
        ? DimensionLocation.LINE_BOUNDARY 
        : DimensionLocation.LINE_INTERIOR;
  }
  
  /**
   * Locates a point which is known to be a node of the geometry
   * (i.e. a vertex or on an edge).
   * 
   * @param p the node point to locate
   * @param parentPolygonal the polygon the point is a node of
   * @return the location of the node point
   */
  public int locateNode(Coordinate p, Geometry parentPolygonal) {
    return DimensionLocation.location(locateNodeWithDim(p, parentPolygonal));
  }
  
  /**
   * Locates a point which is known to be a node of the geometry,
   * as a {@link DimensionLocation}.
   * 
   * @param p the point to locate
   * @param parentPolygonal the polygon the point is a node of
   * @return the dimension and location of the point
   */
  public int locateNodeWithDim(Coordinate p, Geometry parentPolygonal) {
    return locateWithDim(p, true, parentPolygonal);
  }

  /**
   * Computes the topological location ({@link Location}) of a single point
   * in a Geometry, as well as the dimension of the geometry element the point
   * is located in (if not in the Exterior).
   * It handles both single-element and multi-element Geometries.
   * The algorithm for multi-part Geometries
   * takes into account the SFS Boundary Determination Rule.
   *
   * @param p the point to locate
   * @return the {@link Location} of the point relative to the input Geometry
   */
  public int locateWithDim(Coordinate p) {
    return locateWithDim(p, false, null);
  }

  /**
   * Computes the topological location ({@link Location}) of a single point
   * in a Geometry, as well as the dimension of the geometry element the point
   * is located in (if not in the Exterior).
   * It handles both single-element and multi-element Geometries.
   * The algorithm for multi-part Geometries
   * takes into account the SFS Boundary Determination Rule.
   *
   * @param p the coordinate to locate
   * @param isNode whether the coordinate is a node (on an edge) of the geometry
   * @param polygon 
   * @return the {@link Location} of the point relative to the input Geometry
   */
  private int locateWithDim(Coordinate p, boolean isNode, Geometry parentPolygonal)
  {
    if (isEmpty) return DimensionLocation.EXTERIOR;
    
    /**
     * In a polygonal geometry a node must be on the boundary.
     * (This is not the case for a mixed collection, since 
     * the node may be in the interior of a polygon.)
     */
    if (isNode && (geom instanceof Polygon || geom instanceof MultiPolygon))
      return DimensionLocation.AREA_BOUNDARY;
    
    int dimLoc = computeDimLocation(p, isNode, parentPolygonal);
    return dimLoc;
  }

  private int computeDimLocation(Coordinate p, boolean isNode, Geometry parentPolygonal) {
    //-- check dimensions in order of precedence
    if (polygons != null) {
      int locPoly = locateOnPolygons(p, isNode, parentPolygonal);
      if (locPoly != Location.EXTERIOR)
        return DimensionLocation.locationArea(locPoly);
    }
    if (lines != null) {
      int locLine = locateOnLines(p, isNode);
      if (locLine != Location.EXTERIOR)
        return DimensionLocation.locationLine(locLine);
    }
    if (points != null) {
      int locPt = locateOnPoints(p);
      if (locPt != Location.EXTERIOR)
        return DimensionLocation.locationPoint(locPt);
    }
    return DimensionLocation.EXTERIOR;
  }

  private int locateOnPoints(Coordinate p) {
    if (points.contains(p)) {
      return Location.INTERIOR;
    }
    return Location.EXTERIOR;
  }
  
  private int locateOnLines(Coordinate p, boolean isNode) {
    if (lineBoundary != null 
          && lineBoundary.isBoundary(p)) {
        return Location.BOUNDARY;
    }
    //-- must be on line, in interior
    if (isNode)
      return Location.INTERIOR;
    
    //TODO: index the lines
    for (LineString line : lines) {
      //-- have to check every line, since any/all may contain point
      int loc = locateOnLine(p, isNode, line);
      if (loc != Location.EXTERIOR)
        return loc;
      //TODO: minor optimization - some BoundaryNodeRules can short-circuit
    }
    return Location.EXTERIOR;
  }

  private int locateOnLine(Coordinate p, boolean isNode, LineString l)
  {
    // bounding-box check
    if (! l.getEnvelopeInternal().intersects(p)) 
      return Location.EXTERIOR;
    
    CoordinateSequence seq = l.getCoordinateSequence();
    if (PointLocation.isOnLine(p, seq)) {
      return Location.INTERIOR;
    }
    return Location.EXTERIOR;
  }
  
  private int locateOnPolygons(Coordinate p, boolean isNode, Geometry parentPolygonal) {
    int numBdy = 0;
    //TODO: use a spatial index on the polygons
    for (int i = 0; i < polygons.size(); i++) {
      int loc = locateOnPolygonal(p, isNode, parentPolygonal, i);
      if (loc == Location.INTERIOR) {
        return Location.INTERIOR;
      }
      if (loc == Location.BOUNDARY) {
        numBdy += 1;
      }
    }
    if (numBdy == 1) {
      return Location.BOUNDARY;
    }
    //-- check for point lying on adjacent boundaries
    else if (numBdy > 1) {
      if (adjEdgeLocator == null) {
        adjEdgeLocator = new AdjacentEdgeLocator(geom);
      }
      return adjEdgeLocator.locate(p);
    }
    return Location.EXTERIOR;
  }

  private int locateOnPolygonal(Coordinate p, boolean isNode, Geometry parentPolygonal, int index) {
    Geometry polygonal = polygons.get(index);
    if (isNode && parentPolygonal == polygonal) {
      return Location.BOUNDARY;
    }
    PointOnGeometryLocator locator = getLocator(index);
    return locator.locate(p);
  }

  private PointOnGeometryLocator getLocator(int index) {
    PointOnGeometryLocator locator = polyLocator[index];
    if (locator == null) {
      Geometry polygonal = polygons.get(index);
      locator = isPrepared 
          ? new IndexedPointInAreaLocator(polygonal)
              : new SimplePointInAreaLocator(polygonal);
      polyLocator[index] = locator;
    }
    return locator;
  }

}
