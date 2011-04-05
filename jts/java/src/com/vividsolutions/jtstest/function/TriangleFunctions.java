/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.GeometryMapper;


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
