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

package org.locationtech.jtstest.function;

import org.locationtech.jts.algorithm.CGAlgorithmsDD;
import org.locationtech.jts.algorithm.Intersection;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;

public class LineSegmentFunctions
{
  public static boolean intersects(Geometry g1, Geometry g2)
  {
    Coordinate[] pt1 = g1.getCoordinates();
    Coordinate[] pt2 = g2.getCoordinates();
    RobustLineIntersector ri = new RobustLineIntersector();
    ri.computeIntersection(pt1[0], pt1[1], pt2[0], pt2[1]);
    return ri.hasIntersection();
  }
  
  public static Geometry intersection(Geometry g1, Geometry g2)
  {
    Coordinate[] pt1 = g1.getCoordinates();
    Coordinate[] pt2 = g2.getCoordinates();
    RobustLineIntersector ri = new RobustLineIntersector();
    ri.computeIntersection(pt1[0], pt1[1], pt2[0], pt2[1]);
    switch (ri.getIntersectionNum()) {
    case 0:
      // no intersection => return empty point
      return g1.getFactory().createPoint((Coordinate) null);
    case 1:
      // return point
      return g1.getFactory().createPoint(ri.getIntersection(0));
    case 2:
      // return line
      return g1.getFactory().createLineString(
          new Coordinate[] {
              ri.getIntersection(0),
              ri.getIntersection(1)
          });
    }
    return null;
  }
  
  public static Geometry intersectionDD(Geometry g1, Geometry g2)
  {
    Coordinate[] pt1 = g1.getCoordinates();
    Coordinate[] pt2 = g2.getCoordinates();
    
    // first check if there actually is an intersection
    RobustLineIntersector ri = new RobustLineIntersector();
    ri.computeIntersection(pt1[0], pt1[1], pt2[0], pt2[1]);
    if (! ri.hasIntersection()) {
      // no intersection => return empty point
      return g1.getFactory().createPoint((Coordinate) null);
    }
    
    Coordinate intPt = CGAlgorithmsDD.intersection(pt1[0], pt1[1], pt2[0], pt2[1]);
    return g1.getFactory().createPoint(intPt);
  }
  
  public static Geometry lineIntersection(Geometry g1, Geometry g2)
  {
    Coordinate[] pt1 = g1.getCoordinates();
    Coordinate[] pt2 = g2.getCoordinates();
    
    LineSegment line1 = new LineSegment(pt1[0], pt1[1]);
    LineSegment line2 = new LineSegment(pt2[0], pt2[1]);
    
    Coordinate intPt = line1.lineIntersection(line2);
    return g1.getFactory().createPoint(intPt);
  }

  public static Geometry lineIntersectionDD(Geometry g1, Geometry g2)
  {
    Coordinate[] pt1 = g1.getCoordinates();
    Coordinate[] pt2 = g2.getCoordinates();
    
    Coordinate intPt = CGAlgorithmsDD.intersection(pt1[0], pt1[1], pt2[0], pt2[1] );
    // handle parallel case
    if (Double.isNaN(intPt.getX())) {
      intPt = null;
    }
    return g1.getFactory().createPoint(intPt);
  }

  public static Geometry lineSegmentIntersection(Geometry g1, Geometry g2)
  {
    Coordinate[] pt1 = g1.getCoordinates();
    Coordinate[] pt2 = g2.getCoordinates();
    
    Coordinate intPt = Intersection.lineSegment(pt1[0], pt1[1], pt2[0], pt2[1]);
    return g1.getFactory().createPoint(intPt);
  }

  public static Geometry reflectPoint(Geometry g1, Geometry g2)
  {
    Coordinate[] line = g1.getCoordinates();
    Coordinate pt = g2.getCoordinate();
    
    LineSegment seg = new LineSegment(line[0], line[1]);
    Coordinate reflectPt = seg.reflect(pt);
    
    return g1.getFactory().createPoint(reflectPt);
  }

  public static Geometry project(Geometry g1, Geometry g2)
  {
    LineSegment seg1 = toLineSegment(g1);
    Coordinate[] line2 = g2.getCoordinates();
    if (line2.length == 1) {
      Coordinate pt = line2[0];
      Coordinate result = seg1.project(pt); 
      return g1.getFactory().createPoint(result);
    }
    LineSegment seg2 = new LineSegment(line2[0], line2[1]);
    LineSegment result = seg1.project(seg2);
    if (result == null)
      return g1.getFactory().createLineString();
    return result.toGeometry(g1.getFactory());
  }

  private static LineSegment toLineSegment(Geometry g) {
    Coordinate[] line = g.getCoordinates();
    return new LineSegment(line[0], line[1]);
  }

}
