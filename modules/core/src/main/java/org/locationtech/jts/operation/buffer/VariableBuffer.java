/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
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
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;

/**
 * Creates a buffer polygon with a varying buffer distance 
 * at each vertex along a line.
 * <p>
 * Only single lines are supported as input, since buffer widths 
 * generally need to be specified individually for each line.
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
   * @return the variable-width buffer polygon
   */
  public static Geometry buffer(Geometry line, double startDistance,
      double endDistance) {
    double[] distance = VariableBuffer.interpolate((LineString) line,
        startDistance, endDistance);
    VariableBuffer vb = new VariableBuffer(line, distance);
    return vb.getResult();
  }

  /**
   * Creates a buffer polygon along a line with the distance specified
   * at each vertex.
   * 
   * @param line the line to buffer
   * @param distance the buffer distance for each vertex of the line
   * @return the buffer polygon
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
    for (int i = 1; i < values.length; i++) {
      double segLen = pts[i].distance(pts[i - 1]);
      currLen += segLen;
      double lenFrac = currLen / totalLen;
      double delta = lenFrac * (endValue - startValue);
      values[i] = startValue + delta;
    }
    return values;
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

  public Geometry getResult() {
    List<Geometry> parts = new ArrayList<Geometry>();

    Coordinate[] pts = line.getCoordinates();
    int npts = pts.length;
    // construct segment buffers
    for (int i = 1; i < pts.length; i++) {
      double dist0 = distance[i - 1];
      double dist1 = distance[i];
      if (dist0 > 0 || dist1 > 0) {
        Polygon poly = segmentBuffer(pts[i - 1], pts[i], dist0, dist1);
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

  private Polygon segmentBuffer(Coordinate p0, Coordinate p1,
      double dist0, double dist1) {
    CoordinateList coords = new CoordinateList();
    
    // forward tangent line
    Coordinate t0 = new Coordinate();
    Coordinate t1 = new Coordinate();
    outerTangent(p0, dist0, p1, dist1, t0, t1);
    // reverse tangent line on other side of segment
    Coordinate tr0 = reflect(t0, p0, p1);
    Coordinate tr1 = reflect(t1, p0, p1);
    
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
  
  private void addCap(Coordinate p, double r, Coordinate t1, Coordinate t2, CoordinateList coords) {
    double angCap = angleOfCap(p, t1, t2);  
    double filletAngInc = Math.PI / 2 / quadrantSegs;
    int npts = (int) (angCap / filletAngInc) - 1;
    double angInc = angCap / npts;
    
    double angStart = Angle.angle(p,  t1);
    for (int i = 1; i < npts; i++) {
      // use negative increment to create points CW
      double ang = angStart - i * angInc;
      double x = p.getX() + r * Math.cos(ang);
      double y = p.getY() + r * Math.sin(ang);
      coords.add( new Coordinate(x,y) );
    }
  }

  /**
   * Computes the angle which subtends the 
   * cap based at p and running CCW from tangent point t1
   * to tangent point t2.
   * 
   * @param p the base point of the cap
   * @param t1 the first tangent point
   * @param t2 the second tangent point
   * @return the angle subtended by the cap
   */
  private double angleOfCap(Coordinate p, Coordinate t1, Coordinate t2) {
    double ang = Angle.angleBetweenOriented(t1, p, t2);
    // if ang is CCW, use other circle angle
    if (ang > 0) return 2 * Math.PI - ang;
    
    // ang is CW (negative)
    return -ang;
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
   * @param p1 the computed tangent circumference point on circle 1
   * @param p2 the computed tangent circumference point on circle 2
   */
  private static void outerTangent(Coordinate c1, double r1, Coordinate c2, double r2, Coordinate p1, Coordinate p2) {
    if (r1 > r2) {
      outerTangent(c2, r2, c1, r1, p2, p1);
      return;
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
    
    double a1 = a3 - a2;
    
    double aa = Math.PI/2 - a1;
    double x3 = x1 + r1 * Math.cos(aa);
    double y3 = y1 + r1 * Math.sin(aa);
    double x4 = x2 + r2 * Math.cos(aa);
    double y4 = y2 + r2 * Math.sin(aa);
    p1.setX(x3);
    p1.setY(y3);
    p2.setX(x4);
    p2.setY(y4);
  }
  
  /**
   * Computes the reflection of a point in a line defined
   * by two points.
   * 
   * @param p the point to reflect
   * @param p1 a point on the line of reflection
   * @param p2 a point on the line of reflection
   * @return the reflected point
   */
  private static Coordinate reflect(Coordinate p, Coordinate p1, Coordinate p2) {
    // general line equation
    double A = p2.getY() - p1.getY();
    double B = p1.getX() - p2.getX();
    double C = p1.getY() * (p2.getX() - p1.getX()) - p1.getX()*( p2.getY() - p1.getY() );
    
    // compute reflected point
    double A2plusB2 = A*A + B*B;
    double A2subB2 = A*A - B*B;
    
    double x = p.getX();
    double y = p.getY();
    double rx = ( -A2subB2*x - 2*A*B*y - 2*A*C ) / A2plusB2;
    double ry = ( A2subB2*y - 2*A*B*x - 2*B*C ) / A2plusB2;
    
    return new Coordinate(rx, ry);
  }

}
