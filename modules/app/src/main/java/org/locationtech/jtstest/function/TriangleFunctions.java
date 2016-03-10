/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.*;
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
    Coordinate[] pts = g.getCoordinates();
    if (pts.length < 3)
      throw new IllegalArgumentException("Input geometry must have at least 3 points");
    return pts;
  }
}
