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
package org.locationtech.jts.algorithm.construct;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;

/**
 * Computes the Maximum Inscribed Circle for some kinds of convex polygons.
 * It determines the circle center point by computing Voronoi node points
 * and testing them for distance to generating edges.
 * This is more precise than iterated approximation, 
 * and faster for small polygons (such as triangles and convex quadrilaterals).
 * 
 * @author Martin Davis
 *
 */
class ExactMaxInscribedCircle {
  
  /**
   * Tests whether a given geometry is supported by this class.
   * Currently only triangles and convex quadrilaterals are supported.
   * 
   * @param geom an areal geometry
   * @return true if the geometry shape can be evaluated
   */
  public static boolean isSupported(Geometry geom) {
    if (! isSimplePolygon(geom)) 
      return false;
    Polygon polygon = (Polygon) geom;
    if (isTriangle(polygon)) 
      return true;
    if (isQuadrilateral(polygon) && isConvex(polygon))
      return true;
    return false;
  }

  private static boolean isSimplePolygon(Geometry geom) {
    return geom instanceof Polygon
        && ((Polygon) geom).getNumInteriorRing() == 0; 
  }

  private static boolean isTriangle(Polygon polygon) {
    return polygon.getNumPoints() == 4;
  }
  
  private static boolean isQuadrilateral(Polygon polygon) {
    return polygon.getNumPoints() == 5;
  }

  public static Coordinate[] computeRadius(Polygon polygon) {
    Coordinate[] ring = polygon.getExteriorRing().getCoordinates();
    if (ring.length == 4)
      return computeTriangle(ring);
    else if (ring.length == 5)
      return computeConvexQuadrilateral(ring);
    throw new IllegalArgumentException("Input must be a triangle or convex quadrilateral");
  }
  
  private static Coordinate[] computeTriangle(Coordinate[] ring) {
    Coordinate center = Triangle.inCentre(ring[0], ring[1], ring[2]);
    LineSegment seg = new LineSegment(ring[0], ring[1]);
    Coordinate radius = seg.project(center);
    return new Coordinate[] { center, radius };
  }

  /**
   * The Voronoi nodes of a convex polygon occur at the intersection point
   * of two bisectors of each triplet of edges.
   * The Maximum Inscribed Circle center is the node
   * with the farthest distance from the generating edges.
   * For a quadrilateral there are 4 distinct edge triplets, 
   * at each edge with its adjacent edges. 
   * 
   * @param ring the polygon ring
   * @return an array containing the incircle center and radius points
   */
  private static Coordinate[] computeConvexQuadrilateral(Coordinate[] ring) {
    Coordinate[] ringCW = CoordinateArrays.orient(ring, true);
    
    double diameter = CoordinateArrays.envelope(ringCW).getDiameter();
    //-- expand diameter for robustness
    double diamWithTolerance = 2 * diameter;
    
    //-- compute corner bisectors
    LineSegment[] bisector = computeBisectors(ringCW, diamWithTolerance);
    //-- compute nodes and find interior one farthest from sides
    double maxDist = -1;
    Coordinate center = null;
    Coordinate radius = null;
    for (int i = 0; i < 4; i++) {
      LineSegment b1 = bisector[i];
      int i2 = (i + 1) % 4;
      LineSegment b2 = bisector[i2];

      Coordinate nodePt = b1.intersection(b2);
      //-- if bisector segments don't intersect node is outside polygon
      if (nodePt == null) {
        continue;
      }
      
      //-- only interior nodes are considered
      if (! isPointInConvexRing(ringCW, nodePt)) {
        continue;
      }
      
      //-- check if node is further than current max center
      Coordinate r = nearestEdgePt(ringCW, nodePt);
      double dist = nodePt.distance(r);
      if (maxDist < 0 || dist > maxDist) {
        center = nodePt;
        radius = r;
        maxDist = dist;
        //System.out.println(WKTWriter.toLineString(center, radius));
      }
    }
    return new Coordinate[] { center, radius };
  }

  private static LineSegment[] computeBisectors(Coordinate[] ptsCW, double diameter) {
    LineSegment[] bisector = new LineSegment[4];
    for (int i = 0; i < 4; i++) {
      bisector[i] = computeConvexBisector(ptsCW, i, diameter);
    }
    return bisector;
  }

  private static Coordinate nearestEdgePt(Coordinate[] ring, Coordinate pt) {
    Coordinate nearestPt = null;
    double minDist = -1;
    for (int i = 0; i < ring.length - 1; i++) {
      LineSegment edge = new LineSegment(ring[i], ring[i + 1]);
      Coordinate r = edge.closestPoint(pt);
      double dist = pt.distance(r);
      if (minDist < 0 || dist < minDist) {
        minDist = dist;
        nearestPt = r;
      }
    }
    return nearestPt;
  }

  private static LineSegment computeConvexBisector(Coordinate[] pts, int index, double len) {
    Coordinate basePt = pts[index];
    int iPrev = index == 0 ? pts.length - 2 : index - 1;
    int iNext = index >= pts.length ? 0 : index + 1;
    Coordinate pPrev = pts[iPrev];
    Coordinate pNext = pts[iNext];
    
    //-- this should never happen, since only convex quads are handled
    if (isConcave(pPrev, basePt, pNext)) 
      throw new IllegalStateException("Input is not convex");
    
    double bisectAng = Angle.bisector(pPrev, basePt, pNext);
    Coordinate endPt = Angle.project(basePt, bisectAng, len);
    return new LineSegment(basePt.copy(), endPt);
  }
  
  private static boolean isConvex(Polygon polygon) {
    LinearRing shell = polygon.getExteriorRing();
    return isConvex(shell.getCoordinateSequence());
  }
  
  private static boolean isConvex(CoordinateSequence ring) {
    /**
     * A ring cannot be all concave, so if it has a consistent
     * orientation it must be convex.
     */
    int n = ring.size();
    if (n < 4) 
      return false;
    //-- triangles must be convex
    if (n == 4)
      return true;
    //-- check for all convex or collinear angles
    int ringOrient = 0;
    for (int i = 0; i < n - 1; i++) {
      int i1 = i + 1;
      int i2 = (i1 >= n - 1) ? 1 : i1 + 1;
      int orient = Orientation.index(ring.getCoordinate(i), 
          ring.getCoordinate(i1), ring.getCoordinate(i2));
      if (orient == Orientation.COLLINEAR)
        continue;
      if (ringOrient == 0) {
        ringOrient = orient;
      }
      else if (orient != ringOrient) {
          return false;
      }
    }
    return true;
  }

  private static boolean isConcave(Coordinate p0, Coordinate p1, Coordinate p2) {
    return Orientation.COUNTERCLOCKWISE == Orientation.index(p0, p1, p2);
  }

  private static boolean isPointInConvexRing(Coordinate[] ringCW, Coordinate p) {
    for (int i = 0; i < ringCW.length - 1; i++) {
      Coordinate p0 = ringCW[i];
      Coordinate p1 = ringCW[i + 1];
      int orient = Orientation.index(p0, p1, p);
      if (orient == Orientation.COUNTERCLOCKWISE)
        return false;
    }
    return true;
  }
}
