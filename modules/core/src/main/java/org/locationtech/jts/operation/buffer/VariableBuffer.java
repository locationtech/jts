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
package org.locationtech.jts.operation.buffer;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * Creates a buffer polygon with a varying buffer distance 
 * at each vertex along a line.
 * <p>
 * Only single lines are supported as input, since buffer widths 
 * are typically specified individually for each line.
 * 
 * @author Martin Davis
 *
 */
public class VariableBuffer {

  /**
   * Creates a buffer polygon along a line with the buffer distance interpolated
   * between a start distance and an end distance.
   *  
   * @param line the line to buffer
   * @param startDistance the buffer width at the start of the line
   * @param endDistance the buffer width at the end of the line
   * @return the variable-distance buffer polygon
   */
  public static Geometry buffer(Geometry line, double startDistance,
      double endDistance) {
    double[] distance = interpolate((LineString) line,
        startDistance, endDistance);
    VariableBuffer vb = new VariableBuffer(line, distance);
    return vb.getResult();
  }

  /**
   * Creates a buffer polygon along a line with the buffer distance interpolated
   * between a start distance, a middle distance and an end distance.
   * The middle distance is attained at
   * the vertex at or just past the half-length of the line.
   * For smooth buffering of a {@link LinearRing} (or the rings of a {@link Polygon})
   * the start distance and end distance should be equal.
   *  
   * @param line the line to buffer
   * @param startDistance the buffer width at the start of the line
   * @param midDistance the buffer width at the middle vertex of the line
   * @param endDistance the buffer width at the end of the line
   * @return the variable-distance buffer polygon
   */
  public static Geometry buffer(Geometry line, double startDistance,
      double midDistance,
      double endDistance) {
    double[] distance = interpolate((LineString) line,
        startDistance, midDistance, endDistance);
    VariableBuffer vb = new VariableBuffer(line, distance);
    return vb.getResult();
  }

  /**
   * Creates a buffer polygon along a line with the distance specified
   * at each vertex.
   * 
   * @param line the line to buffer
   * @param distance the buffer distance for each vertex of the line
   * @return the variable-distance buffer polygon
   */
  public static Geometry buffer(Geometry line, double[] distance) {
    VariableBuffer vb = new VariableBuffer(line, distance);
    return vb.getResult();
  }

  /**
   * Computes a list of values for the points along a line by
   * interpolating between values for the start and end point.
   * The interpolation is
   * based on the distance of each point along the line
   * relative to the total line length.
   * 
   * @param line the line to interpolate along
   * @param startValue the start value 
   * @param endValue the end value
   * @return the array of interpolated values
   */
  private static double[] interpolate(LineString line, 
      double startValue,
      double endValue) {
    startValue = Math.abs(startValue);
    endValue = Math.abs(endValue);
    double[] values = new double[line.getNumPoints()];
    values[0] = startValue;
    values[values.length - 1] = endValue;

    double totalLen = line.getLength();
    Coordinate[] pts = line.getCoordinates();
    double currLen = 0;
    for (int i = 1; i < values.length - 1; i++) {
      double segLen = pts[i].distance(pts[i - 1]);
      currLen += segLen;
      double lenFrac = currLen / totalLen;
      double delta = lenFrac * (endValue - startValue);
      values[i] = startValue + delta;
    }
    return values;
  }
  
  /**
   * Computes a list of values for the points along a line by
   * interpolating between values for the start, middle and end points.
   * The interpolation is
   * based on the distance of each point along the line
   * relative to the total line length.
   * The middle distance is attained at
   * the vertex at or just past the half-length of the line.
   * 
   * @param line the line to interpolate along
   * @param startValue the start value 
   * @param midValue the start value 
   * @param endValue the end value
   * @return the array of interpolated values
   */
  private static double[] interpolate(LineString line, 
      double startValue,
      double midValue,
      double endValue) 
  {
    startValue = Math.abs(startValue);
    midValue = Math.abs(midValue);
    endValue = Math.abs(endValue);
    
    double[] values = new double[line.getNumPoints()];
    values[0] = startValue;
    values[values.length - 1] = endValue;

    Coordinate[] pts = line.getCoordinates();
    double lineLen = line.getLength();
    int midIndex = indexAtLength(pts, lineLen / 2 );
    
    double delMidStart = midValue - startValue;
    double delEndMid = endValue - midValue;
    
    double lenSM = length(pts, 0, midIndex);
    double currLen = 0;
    for (int i = 1; i <= midIndex; i++) {
      double segLen = pts[i].distance(pts[i - 1]);
      currLen += segLen;
      double lenFrac = currLen / lenSM;
      double val = startValue + lenFrac * delMidStart;
      values[i] = val;
    }
    
    double lenME = length(pts, midIndex, pts.length - 1);
    currLen = 0;
    for (int i = midIndex + 1; i < values.length - 1; i++) {
      double segLen = pts[i].distance(pts[i - 1]);
      currLen += segLen;
      double lenFrac = currLen / lenME;
      double val = midValue + lenFrac * delEndMid;       
      values[i] = val;
    }
    return values;
  }
  
  private static int indexAtLength(Coordinate[] pts, double targetLen) {
    double len  = 0;
    for (int i = 1; i < pts.length; i++) {
      len += pts[i].distance(pts[i-1]);
      if (len > targetLen)
        return i;
    }
    return pts.length - 1;
  }

  private static double length(Coordinate[] pts, int i1, int i2) {
    double len = 0;
    for (int i = i1 + 1; i <= i2; i++) {
      len += pts[i].distance(pts[i-1]);
    }
    return len;
  }

  private LineString line;
  private double[] distance;
  private GeometryFactory geomFactory;
  private int quadrantSegs = BufferParameters.DEFAULT_QUADRANT_SEGMENTS;

  /**
   * Creates a generator for a variable-distance line buffer.
   * 
   * @param line the linestring to buffer
   * @param distance the buffer distance for each vertex of the line
   */
  public VariableBuffer(Geometry line, double[] distance) {
    this.line = (LineString) line;
    this.distance = distance;
    geomFactory = line.getFactory();
    
    if (distance.length != this.line.getNumPoints()) {
      throw new IllegalArgumentException("Number of distances is not equal to number of vertices");
    }
  }

  /**
   * Computes the buffer polygon.
   * 
   * @return a buffer polygon
   */
  public Geometry getResult() {
    List<Geometry> parts = new ArrayList<Geometry>();

    Coordinate[] pts = line.getCoordinates();
    // construct segment buffers
    for (int i = 1; i < pts.length; i++) {
      double dist0 = distance[i - 1];
      double dist1 = distance[i];
      if (dist0 > 0 || dist1 > 0) {
        Polygon poly = segmentBuffer(pts[i - 1], pts[i], dist0, dist1);
        if (poly != null)
          parts.add(poly);
      }
    }

    GeometryCollection partsGeom = geomFactory
        .createGeometryCollection(GeometryFactory.toGeometryArray(parts));
    Geometry buffer = partsGeom.union();
    
    // ensure an empty polygon is returned if needed
    if (buffer.isEmpty()) {
      return geomFactory.createPolygon();
    }
    return buffer;
  }

  /**
   * Computes a variable buffer polygon for a single segment,
   * with the given endpoints and buffer distances.
   * The individual segment buffers are unioned
   * to form the final buffer.
   * 
   * @param p0 the segment start point
   * @param p1 the segment end point
   * @param dist0 the buffer distance at the start point
   * @param dist1 the buffer distance at the end point
   * @return the segment buffer.
   */
  private Polygon segmentBuffer(Coordinate p0, Coordinate p1,
      double dist0, double dist1) {
    /**
     * Compute for increasing distance only, so flip if needed
     */
    if (dist0 > dist1) {
      return segmentBuffer(p1, p0, dist1, dist0);
    }
        
    // forward tangent line
    LineSegment tangent = outerTangent(p0, dist0, p1, dist1);
    
    // if tangent is null then compute a buffer for largest circle
    if (tangent == null) {
      Coordinate center = p0;
      double dist = dist0;
      if (dist1 > dist0) {
        center = p1;
        dist = dist1;
      }
      return circle(center, dist);
    }
    
    Coordinate t0 = tangent.getCoordinate(0);
    Coordinate t1 = tangent.getCoordinate(1);

    // reverse tangent line on other side of segment
    LineSegment seg = new LineSegment(p0, p1);
    Coordinate tr0 = seg.reflect(t0);
    Coordinate tr1 = seg.reflect(t1);
    
    CoordinateList coords = new CoordinateList();
    coords.add(t0);
    coords.add(t1);

    // end cap
    addCap(p1, dist1, t1, tr1, coords);
    
    coords.add(tr1);
    coords.add(tr0);
    
    // start cap
    addCap(p0, dist0,  tr0, t0, coords);
    
    // close
    coords.add(t0);
    
    Coordinate[] pts = coords.toCoordinateArray();
    Polygon polygon = geomFactory.createPolygon(pts);
    return polygon;
  }
  
  /**
   * Returns a circular polygon.
   * 
   * @param center the circle center point
   * @param radius the radius 
   * @return a polygon, or null if the radius is 0
   */
  private Polygon circle(Coordinate center, double radius) {
    if (radius <= 0) 
      return null;
    int nPts = 4 * quadrantSegs; 
    Coordinate[] pts = new Coordinate[nPts + 1];
    double angInc = Math.PI / 2 / quadrantSegs;
    for (int i = 0; i < nPts; i++) {
      pts[i] = projectPolar(center, radius, i * angInc);
    }
    pts[pts.length - 1] = pts[0].copy();
    return geomFactory.createPolygon(pts);
  }

  /**
   * Adds a semi-circular cap CCW around the point p.
   * 
   * @param p the centre point of the cap
   * @param r the cap radius
   * @param t1 the starting point of the cap
   * @param t2 the ending point of the cap
   * @param coords the coordinate list to add to
   */
  private void addCap(Coordinate p, double r, Coordinate t1, Coordinate t2, CoordinateList coords) {
    
    double angStart = Angle.angle(p, t1);
    double angEnd = Angle.angle(p, t2);
    if (angStart < angEnd)
      angStart += 2 * Math.PI;
    
    int indexStart = capAngleIndex(angStart);
    int indexEnd = capAngleIndex(angEnd);
    
    for (int i = indexStart; i > indexEnd; i--) {
      // use negative increment to create points CW
      double ang = capAngle(i);
      coords.add( projectPolar(p, r, ang) );
    }
  }  
  
  /**
   * Computes the angle for the given cap point index.
   * 
   * @param index the fillet angle index
   * @return
   */
  private double capAngle(int index) {
    double capSegAng = Math.PI / 2 / quadrantSegs;
    return index * capSegAng;
  }

  /**
   * Computes the canonical cap point index for a given angle.
   * The angle is rounded down to the next lower
   * index.
   * <p>
   * In order to reduce the number of points created by overlapping end caps,
   * cap points are generated at the same locations around a circle.
   * The index is the index of the points around the circle, 
   * with 0 being the point at (1,0).
   * The total number of points around the circle is 
   * <code>4 * quadrantSegs</code>.
   *  
   * @param ang the angle 
   * @return the index for the angle.
   */
  private int capAngleIndex(double ang) {
    double capSegAng = Math.PI / 2 / quadrantSegs;
    int index = (int) (ang / capSegAng);
    return index;
  }

  /**
   * Computes the two circumference points defining the outer tangent line
   * between two circles.
   * <p>
   * For the algorithm see <a href='https://en.wikipedia.org/wiki/Tangent_lines_to_circles#Outer_tangent'>Wikipedia</a>.
   * 
   * @param c1 the centre of circle 1
   * @param r1 the radius of circle 1
   * @param c2 the centre of circle 2
   * @param r2 the center of circle 2
   * @return the outer tangent line segment, or null if none exists
   */
  private static LineSegment outerTangent(Coordinate c1, double r1, Coordinate c2, double r2) {
    /**
     * If distances are inverted then flip to compute and flip result back.
     */
    if (r1 > r2) {
      LineSegment seg = outerTangent(c2, r2, c1, r1);
      return new LineSegment(seg.p1, seg.p0);
    }
    double x1 = c1.getX();
    double y1 = c1.getY();
    double x2 = c2.getX();
    double y2 = c2.getY();
    // TODO: handle r1 == r2?
    double a3 = - Math.atan2(y2 - y1, x2 - x1);
    
    double dr = r2 - r1;
    double d = Math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1));
    
    double a2 = Math.asin(dr / d);
    // check if no tangent exists
    if (Double.isNaN(a2))
      return null;
    
    double a1 = a3 - a2;
    
    double aa = Math.PI/2 - a1;
    double x3 = x1 + r1 * Math.cos(aa);
    double y3 = y1 + r1 * Math.sin(aa);
    double x4 = x2 + r2 * Math.cos(aa);
    double y4 = y2 + r2 * Math.sin(aa);
    
    return new LineSegment(x3, y3, x4, y4);
  }


  private static Coordinate projectPolar(Coordinate p, double r, double ang) {
    double x = p.getX() + r * snapTrig(Math.cos(ang));
    double y = p.getY() + r * snapTrig(Math.sin(ang));
    return new Coordinate(x, y);
  }
  
  private static final double SNAP_TRIG_TOL = 1e-6;
  
  /**
   * Snap trig values to integer values for better consistency.
   * 
   * @param x the result of a trigonometric function
   * @return x snapped to the integer interval
   */
  private static double snapTrig(double x) {
    if (x > (1 - SNAP_TRIG_TOL)) return 1;
    if (x < (-1 + SNAP_TRIG_TOL)) return -1;
    if (Math.abs(x) < SNAP_TRIG_TOL) return 0;
    return x;
  }
}
