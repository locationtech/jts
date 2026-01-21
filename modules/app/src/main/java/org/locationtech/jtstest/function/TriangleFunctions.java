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

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.geom.util.GeometryMapper;


public class TriangleFunctions {
  
  public static Geometry centroid(Geometry g)
  {
    return GeometryMapper.map(g, 
        new GeometryMapper.MapOp() {
      public Geometry map(Geometry g) {
        Coordinate[] pts = trianglePts(g);
        Coordinate cc = Triangle.centroid(pts[0], pts[1], pts[2]);
        GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
        return geomFact.createPoint(cc);
      }});
  }
  
  public static Geometry circumcentre(Geometry g)
  {
    return GeometryMapper.map(g, 
        new GeometryMapper.MapOp() {
      public Geometry map(Geometry g) {
        Coordinate[] pts = trianglePts(g);
        Coordinate cc = Triangle.circumcentre(pts[0], pts[1], pts[2]);
        GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
        return geomFact.createPoint(cc);
      }});
  }
  
  public static double circumradius(Geometry g)
  {
      Coordinate[] pts = trianglePts(g);
      return Triangle.circumradius(pts[0], pts[1], pts[2]);
  }
  
  public static Geometry circumcircle(Geometry g, int quadSegs)
  {
    Coordinate[] pts = trianglePts(g);
    Coordinate cc = Triangle.circumcentreDD(pts[0], pts[1], pts[2]);
    Geometry ccPt = g.getFactory().createPoint(cc);
    double cr = Triangle.circumradius(pts[0], pts[1], pts[2]);
    return ccPt.buffer(cr, quadSegs);
  }
  
  public static Geometry circumcentreDD(Geometry g)
  {
    return GeometryMapper.map(g, 
        new GeometryMapper.MapOp() {
      public Geometry map(Geometry g) {
        Coordinate[] pts = trianglePts(g);
        Coordinate cc = Triangle.circumcentreDD(pts[0], pts[1], pts[2]);
        GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
        return geomFact.createPoint(cc);
      }});
  }
  
  public static Geometry perpendicularBisectors(Geometry g)
  {
    Coordinate[] pts = trianglePts(g);
    Coordinate cc = Triangle.circumcentre(pts[0], pts[1], pts[2]);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
    LineString[] line = new LineString[3];
    Coordinate p0 = (new LineSegment(pts[1], pts[2])).closestPoint(cc);
    line[0] = geomFact.createLineString(new Coordinate[] {p0, cc});
    Coordinate p1 = (new LineSegment(pts[0], pts[2])).closestPoint(cc);
    line[1] = geomFact.createLineString(new Coordinate[] {p1, cc});
    Coordinate p2 = (new LineSegment(pts[0], pts[1])).closestPoint(cc);
    line[2] = geomFact.createLineString(new Coordinate[] {p2, cc});
    return geomFact.createMultiLineString(line);
  }
  
  public static Geometry incentre(Geometry g)
  {
    return GeometryMapper.map(g, 
        new GeometryMapper.MapOp() {
      public Geometry map(Geometry g) {
        Coordinate[] pts = trianglePts(g);
        Coordinate cc = Triangle.inCentre(pts[0], pts[1], pts[2]);
        GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
        return geomFact.createPoint(cc);
      }});
  }
  
  public static Geometry angleBisectors(Geometry g)
  {
    Coordinate[] pts = trianglePts(g);
    Coordinate cc = Triangle.inCentre(pts[0], pts[1], pts[2]);
    GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
    LineString[] line = new LineString[3];
    line[0] = geomFact.createLineString(new Coordinate[] {pts[0], cc});
    line[1] = geomFact.createLineString(new Coordinate[] {pts[1], cc});
    line[2] = geomFact.createLineString(new Coordinate[] {pts[2], cc});
    return geomFact.createMultiLineString(line);
  }
  
  private static Coordinate[] trianglePts(Geometry g)
  {
    Coordinate[] pts = CoordinateArrays.copyDeep(g.getCoordinates());
    if (Orientation.isCCW(pts)) {
      CoordinateArrays.reverse(pts);
    }
    if (pts.length < 3)
      throw new IllegalArgumentException("Input geometry must have at least 3 points");
    return pts;
  }
  
  /**
   * Constructs the inner hexagon of a triangle, 
   * created by intersecting the chords of the triangle running from each vertex to
   * points a distance of (side / nSections) from each end of the opposite side.
   * 
   * When the parameter is 3 this provides a visualization of
   * Marion Walter's Theorem (https://en.wikipedia.org/wiki/Marion_Walter#Recognition).
   * The theorem states that if each side of an arbitrary triangle is trisected 
   * and lines are drawn to the opposite vertices, 
   * the area of the hexagon created in the middle is one-tenth the area of the original triangle.
   * 
   * @param g a triangle
   * @param nSections the number of sections to divide each side into (>= 3)
   * @return the inner hexagon
   */
  public static Geometry innerHexagon(Geometry g, int nSections) {
    Coordinate[] pts = trianglePts(g);
    //-- return empty polygon for degenerate cases
    if (nSections < 3) {
      return g.getFactory().createEmpty(2);
    }
    
    LineSegment side0 = new LineSegment(pts[0], pts[1]);
    LineSegment side1 = new LineSegment(pts[1], pts[2]);
    LineSegment side2 = new LineSegment(pts[2], pts[0]);
    
    double frac = 1.0 / nSections;
    LineSegment chord0a = chord(pts[0], side1, frac);
    LineSegment chord0b = chord(pts[0], side1, 1.0 - frac);
    LineSegment chord1a = chord(pts[1], side2, frac);
    LineSegment chord1b = chord(pts[1], side2, 1.0 - frac);
    LineSegment chord2a = chord(pts[2], side0, frac);
    LineSegment chord2b = chord(pts[2], side0, 1.0 - frac);
    
    Coordinate[] hexPts = new Coordinate[7];
    hexPts[0] = chord0a.intersection(chord1b);
    hexPts[1] = chord0a.intersection(chord2b);
    hexPts[2] = chord1a.intersection(chord2b);
    hexPts[3] = chord1a.intersection(chord0b);
    hexPts[4] = chord2a.intersection(chord0b);
    hexPts[5] = chord2a.intersection(chord1b);
    hexPts[6] = hexPts[0].copy();
    
    return g.getFactory().createPolygon(hexPts);
  }

  private static LineSegment chord(Coordinate apex, LineSegment side, double frac) {
    Coordinate opp = side.pointAlong(frac);
    return new LineSegment(apex, opp);
  }
}
