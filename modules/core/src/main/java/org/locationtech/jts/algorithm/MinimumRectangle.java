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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Computes the minimum-area rectangle enclosing a {@link Geometry}.
 * Unlike the {@link Envelope, the rectangle may not be axis-parallel.
 * <p>
 * The first step in the algorithm is computing the convex hull of the Geometry.
 * If the input Geometry is known to be convex, a hint can be supplied to
 * avoid this computation.
 * <p>
 * In degenerate cases the minimum enclosing geometry 
 * may be a {@link LineString} or a {@link Point}.
 * </ul>
 * 
 * @see MinimumDiameter
 * @see ConvexHull
 *
 */
public class MinimumRectangle
{
  /**
   * Gets the minimum-area rectangular {@link Polygon} which encloses the input geometry.
   * If the convex hull of the input is degenerate (a line or point)
   * a {@link LineString} or {@link Point} is returned.
   * <p>
   * The minimum rectangle can be used as a generalized representation
   * for the given geometry.
   * 
   * @param geom the geometry
   * @return the minimum rectangle enclosing the geometry
   */
  public static Geometry getMinimumRectangle(Geometry geom) {
    return (new MinimumRectangle(geom)).getMinimumRectangle();
  }
  
  private final Geometry inputGeom;
  private final boolean isConvex;

  private Coordinate[] convexHullPts = null;
  private LineSegment minBaseSeg = new LineSegment();

  /**
   * Compute a minimum rectangle for a given {@link Geometry}.
   *
   * @param inputGeom a Geometry
   */
  public MinimumRectangle(Geometry inputGeom)
  {
    this(inputGeom, false);
  }

  /**
   * Compute a minimum rectangle for a giver {@link Geometry},
   * with a hint if
   * the Geometry is convex
   * (e.g. a convex Polygon or LinearRing,
   * or a two-point LineString, or a Point).
   *
   * @param inputGeom a Geometry which is convex
   * @param isConvex <code>true</code> if the input geometry is convex
   */
  public MinimumRectangle(Geometry inputGeom, boolean isConvex)
  {
    this.inputGeom = inputGeom;
    this.isConvex = isConvex;
  }

  private Geometry getMinimumRectangle()
  {
    if (isConvex) {
      return computeConvex(inputGeom);
    }
    Geometry convexGeom = (new ConvexHull(inputGeom)).getConvexHull();
    return computeConvex(convexGeom);
  }

  private Geometry computeConvex(Geometry convexGeom)
  {
//System.out.println("Input = " + geom);
    if (convexGeom instanceof Polygon)
      convexHullPts = ((Polygon) convexGeom).getExteriorRing().getCoordinates();
    else
      convexHullPts = convexGeom.getCoordinates();

    // special cases for lines or points or degenerate rings
    if (convexHullPts.length == 0) {
    }
    else if (convexHullPts.length == 1) {
      return inputGeom.getFactory().createPoint(convexHullPts[0].copy());
    }
    else if (convexHullPts.length == 2 || convexHullPts.length == 3) {
      //-- Min rectangle is a line. Use the diagonal of the extent
      return computeMaximumLine(convexHullPts, inputGeom.getFactory());
    }
    //TODO: ensure ring is CW
    return computeConvexRing(convexHullPts);
  }

  /**
   * Compute the minimum-area rectangle for a convex ring of {@link Coordinate}s.
   * Leaves the width information in the instance variables.
   * <p>
   * This algorithm uses the standard "rotating calipers" technique, 
   * so is linear in the number of segments.
   *
   * @param ring
   */
  private Polygon computeConvexRing(Coordinate[] ring)
  {
    // for each segment in the ring
    double minRectangleArea = Double.MAX_VALUE;
    int baseSegIndex = -1;
    int maxDiamIndex = -1;
    int maxLeftIndex = -1;
    int maxRightIndex = -1;
    
    int segMaxDiamIndex = 1;
    int segMaxLeftIndex = 1;
    int segMaxRightIndex = 0;

    LineSegment seg = new LineSegment();
    LineSegment segDiam = new LineSegment();
    // for each segment, find the next vertex which is at maximum distance
    for (int i = 0; i < ring.length - 1; i++) {
      seg.p0 = ring[i];
      seg.p1 = ring[i + 1];
      segMaxDiamIndex = findFurthestVertex(ring, seg, segMaxDiamIndex, 0);
      
      Coordinate diamPt = ring[segMaxDiamIndex];
      Coordinate diamBasePt = seg.project(diamPt);  
      segDiam.p0 = diamBasePt;
      segDiam.p1 = diamPt;
      
      segMaxLeftIndex = findFurthestVertex(ring, segDiam, segMaxLeftIndex, 1);
      
      //-- init the max right index
      if (i == 0) {
        segMaxRightIndex = segMaxDiamIndex;
      }
      segMaxRightIndex = findFurthestVertex(ring, segDiam, segMaxRightIndex, -1);
      
      double rectangleWidth = segDiam.distancePerpendicular(ring[segMaxLeftIndex]) 
          + segDiam.distancePerpendicular(ring[segMaxRightIndex]);
      double rectangleArea = segDiam.getLength() * rectangleWidth;
      
      if (rectangleArea < minRectangleArea) {
        //System.out.println("Min Rect area: " + rectangleArea);
        minRectangleArea = rectangleArea;
        baseSegIndex = i;  
        maxDiamIndex = segMaxDiamIndex;
        maxLeftIndex = segMaxLeftIndex;
        maxRightIndex = segMaxRightIndex;
      }
    }
    return computeRectangle(ring[baseSegIndex], ring[baseSegIndex + 1],
        ring[maxDiamIndex], ring[maxLeftIndex], ring[maxRightIndex]);
  }

  private int findFurthestVertex(Coordinate[] pts, LineSegment seg, int startIndex, int orient)
  {
    double maxPerpDistance = orientedDistance(seg, pts[startIndex], orient);
    double nextPerpDistance = maxPerpDistance;
    int maxIndex = startIndex;
    int nextIndex = maxIndex;
    while (isGreaterOrEqual(nextPerpDistance, maxPerpDistance, orient)) {
      maxPerpDistance = nextPerpDistance;
      maxIndex = nextIndex;

      nextIndex = nextIndex(pts, maxIndex);
      if (nextIndex == startIndex)
        break;
      nextPerpDistance = orientedDistance(seg, pts[nextIndex], orient);
    }
    return maxIndex;
  }

  private boolean isGreaterOrEqual(double d1, double d2, int orient) {
    switch (orient) {
    case 0: return Math.abs(d1) >= Math.abs(d2);
    case 1: return d1 >= d2;
    case -1: return d1 <= d2;  
    }
    throw new IllegalArgumentException("Invalid orientation value: " + orient);
  }

  private static double orientedDistance(LineSegment seg, Coordinate p, int orient) {
    double dist = seg.distancePerpendicularOriented(p);
    if (orient == 0) {
      return Math.abs(dist);
    }
    return dist;
  }

  private static int nextIndex(Coordinate[] ring, int index)
  {
    index++;
    if (index >= ring.length - 1) index = 0;
    return index;
  }
  
  private Polygon computeRectangle(Coordinate base0, Coordinate base1, 
      Coordinate para, Coordinate perp1, Coordinate perp2)
  {
    // deltas for the base segment provide slope
    double dx = base1.x - base0.x;
    double dy = base1.y - base0.y;
    
    double minParaC = computeC(dx, dy, base0);
    double maxParaC = computeC(dx, dy, para);
    double minPerpC = computeC(-dy, dx, perp1);
    double maxPerpC = computeC(-dy, dx, perp2);
    
    // compute lines along edges of minimum rectangle
    LineSegment maxPerpLine = computeSegmentForLine(-dx, -dy, maxPerpC);
    LineSegment minPerpLine = computeSegmentForLine(-dx, -dy, minPerpC);
    LineSegment maxParaLine = computeSegmentForLine(-dy, dx, maxParaC);
    LineSegment minParaLine = computeSegmentForLine(-dy, dx, minParaC);
    
    // compute vertices of rectangle (where the para/perp max & min lines intersect)
    Coordinate p0 = maxParaLine.lineIntersection(maxPerpLine);
    Coordinate p1 = minParaLine.lineIntersection(maxPerpLine);
    Coordinate p2 = minParaLine.lineIntersection(minPerpLine);
    Coordinate p3 = maxParaLine.lineIntersection(minPerpLine);
    
    LinearRing shell = inputGeom.getFactory().createLinearRing(
        new Coordinate[] { p0, p1, p2, p3, p0 });
    return inputGeom.getFactory().createPolygon(shell);
  }

  private Polygon computeRectangle2(Coordinate base0, Coordinate base1, 
      Coordinate para, Coordinate perp1, Coordinate perp2)
  {
    // deltas for the base segment of the minimum diameter
    double dx = base1.x - base0.x;
    double dy = base1.y - base0.y;
    
    double minPara = Double.MAX_VALUE;
    double maxPara = -Double.MAX_VALUE;
    double minPerp = Double.MAX_VALUE;
    double maxPerp = -Double.MAX_VALUE;
    
    // compute maxima and minima of lines parallel and perpendicular to base segment
    for (int i = 0; i < convexHullPts.length; i++) {
      
      double paraC = computeC(dx, dy, convexHullPts[i]);
      if (paraC > maxPara) maxPara = paraC;
      if (paraC < minPara) minPara = paraC;
      
      double perpC = computeC(-dy, dx, convexHullPts[i]);
      if (perpC > maxPerp) maxPerp = perpC;
      if (perpC < minPerp) minPerp = perpC;
    }
    
    // compute lines along edges of minimum rectangle
    LineSegment maxPerpLine = computeSegmentForLine(-dx, -dy, maxPerp);
    LineSegment minPerpLine = computeSegmentForLine(-dx, -dy, minPerp);
    LineSegment maxParaLine = computeSegmentForLine(-dy, dx, maxPara);
    LineSegment minParaLine = computeSegmentForLine(-dy, dx, minPara);
    
    // compute vertices of rectangle (where the para/perp max & min lines intersect)
    Coordinate p0 = maxParaLine.lineIntersection(maxPerpLine);
    Coordinate p1 = minParaLine.lineIntersection(maxPerpLine);
    Coordinate p2 = minParaLine.lineIntersection(minPerpLine);
    Coordinate p3 = maxParaLine.lineIntersection(minPerpLine);
    
    LinearRing shell = inputGeom.getFactory().createLinearRing(
        new Coordinate[] { p0, p1, p2, p3, p0 });
    return inputGeom.getFactory().createPolygon(shell);

  }

  
  /**
   * Gets the minimum-area rectangular {@link Polygon} which encloses the input geometry.
   * The rectangle has width equal to the minimum diameter, 
   * and a longer length.
   * If the convex hull of the input is degenerate (a line or point)
   * a {@link LineString} or {@link Point} is returned.
   * <p>
   * The minimum rectangle can be used as an extremely generalized representation
   * for the given geometry.
   * 
   * @return the minimum rectangle enclosing the input (or a line or point if degenerate)
   */
  private Geometry OLDgetMinimumRectangle()
  {
    // deltas for the base segment of the minimum diameter
    double dx = minBaseSeg.p1.x - minBaseSeg.p0.x;
    double dy = minBaseSeg.p1.y - minBaseSeg.p0.y;
    
    double minPara = Double.MAX_VALUE;
    double maxPara = -Double.MAX_VALUE;
    double minPerp = Double.MAX_VALUE;
    double maxPerp = -Double.MAX_VALUE;
    
    // compute maxima and minima of lines parallel and perpendicular to base segment
    for (int i = 0; i < convexHullPts.length; i++) {
      
      double paraC = computeC(dx, dy, convexHullPts[i]);
      if (paraC > maxPara) maxPara = paraC;
      if (paraC < minPara) minPara = paraC;
      
      double perpC = computeC(-dy, dx, convexHullPts[i]);
      if (perpC > maxPerp) maxPerp = perpC;
      if (perpC < minPerp) minPerp = perpC;
    }
    
    // compute lines along edges of minimum rectangle
    LineSegment maxPerpLine = computeSegmentForLine(-dx, -dy, maxPerp);
    LineSegment minPerpLine = computeSegmentForLine(-dx, -dy, minPerp);
    LineSegment maxParaLine = computeSegmentForLine(-dy, dx, maxPara);
    LineSegment minParaLine = computeSegmentForLine(-dy, dx, minPara);
    
    // compute vertices of rectangle (where the para/perp max & min lines intersect)
    Coordinate p0 = maxParaLine.lineIntersection(maxPerpLine);
    Coordinate p1 = minParaLine.lineIntersection(maxPerpLine);
    Coordinate p2 = minParaLine.lineIntersection(minPerpLine);
    Coordinate p3 = maxParaLine.lineIntersection(minPerpLine);
    
    LinearRing shell = inputGeom.getFactory().createLinearRing(
        new Coordinate[] { p0, p1, p2, p3, p0 });
    return inputGeom.getFactory().createPolygon(shell);

  }
  
  /**
   * Creates a line of maximum extent from the provided vertices
   * @param pts the vertices
   * @param factory the geometry factory
   * @return the line of maximum extent
   */
  private static LineString computeMaximumLine(Coordinate[] pts, GeometryFactory factory) {
    //-- find max and min pts for X and Y
    Coordinate ptMinX = null;
    Coordinate ptMaxX = null;
    Coordinate ptMinY = null;
    Coordinate ptMaxY = null;
    for (Coordinate p : pts) {
      if (ptMinX == null || p.getX() < ptMinX.getX()) ptMinX = p;
      if (ptMaxX == null || p.getX() > ptMaxX.getX()) ptMaxX = p;
      if (ptMinY == null || p.getY() < ptMinY.getY()) ptMinY = p;
      if (ptMaxY == null || p.getY() > ptMaxY.getY()) ptMaxY = p;
    }
    Coordinate p0 = ptMinX;
    Coordinate p1 = ptMaxX;
    //-- line is vertical - use Y pts
    if (p0.getX() == p1.getX()) {
      p0 = ptMinY;
      p1 = ptMaxY;
    }
    return factory.createLineString(new Coordinate[] { p0.copy(), p1.copy() });
  }

  private static double computeC(double a, double b, Coordinate p)
  {
    return a * p.y - b * p.x;
  }
  
  private static LineSegment computeSegmentForLine(double a, double b, double c)
  {
    Coordinate p0;
    Coordinate p1;
    /*
    * Line eqn is ax + by = c
    * Slope is a/b.
    * If slope is steep, use y values as the inputs
    */
    if (Math.abs(b) > Math.abs(a)) {
      p0 = new Coordinate(0.0, c/b);
      p1 = new Coordinate(1.0, c/b - a/b);
    }
    else {
      p0 = new Coordinate(c/a, 0.0);
      p1 = new Coordinate(c/a - b/a, 1.0);
    }
    return new LineSegment(p0, p1);
  }
}
