/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.shape;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;

/**
 * Creates a curved line or polygon using Bezier Curves
 * defined by the segments of the input.
 * 
 */
public class BezierCurve {

  /**
   * Creates a curved line or polygon using Bezier Curves
   * defined by the segments of the input.
   * 
   * @param geom the geometry defining the curve
   * @param alpha curviness parameter (0 = linear, 1 = round, 2 = distorted)
   * @return
   */
  public static Geometry bezierCurve(Geometry geom, double alpha) {
    BezierCurve curve = new BezierCurve(geom, alpha);
    return curve.getResult();
  }
  
  private double minSegmentLength = 0.0;
  private int numVerticesPerSegment = 10;

  private Geometry inputGeom;
  private double alpha;
  private final GeometryFactory geomFactory;
  
  private Coordinate[] bezierCurvePts;
  private CubicBezierInterpolationParam[] interpolationParam;

  /**
   * Creates a new Bezier Curve instance.
   *
   * @param geom geometry defining curve
   * @param alpha curviness parameter (0 = linear, 1 = round, 2 = distorted)
   */
  BezierCurve(Geometry geom, double alpha) {
    this.inputGeom = geom;
    if ( alpha < 0.0 ) alpha = 0;
    this.alpha = alpha;
    this.geomFactory = geom.getFactory();
  }

  public Geometry getResult() {
    bezierCurvePts = new Coordinate[numVerticesPerSegment];
    interpolationParam = CubicBezierInterpolationParam.compute(numVerticesPerSegment);

    if (inputGeom instanceof LineString)
      return bezierLine((LineString) inputGeom);
    if (inputGeom instanceof Polygon)
      return bezierPolygon((Polygon) inputGeom);
    return null;
  }
  
  private LineString bezierLine(LineString ls) {
    Coordinate[] coords = ls.getCoordinates();
    Coordinate[][] control = controlPoints(coords, false, alpha);
    final int N = coords.length;
    CoordinateList curvePts = new CoordinateList();
    for (int i = 0; i < N - 1; i++) {
      addCurve(coords[i], coords[i + 1], control[i][1], control[i + 1][0], curvePts);
    }
    curvePts.add(coords[N - 1], false);
    return geomFactory.createLineString(curvePts.toCoordinateArray());
  }

  private LinearRing bezierRing(LinearRing ring) {
    Coordinate[] coords = ring.getCoordinates();
    Coordinate[][] control = controlPoints(coords, true, alpha);
    CoordinateList curvePts = new CoordinateList();
    final int N = coords.length - 1; 
    for (int i = 0; i < N; i++) {
      int next = (i + 1) % N;
      addCurve(coords[i], coords[next], control[i][1], control[next][0], curvePts);
    }
    curvePts.closeRing();

    return geomFactory.createLinearRing(curvePts.toCoordinateArray());
  }
  
  private Polygon bezierPolygon(Polygon poly) {
    LinearRing shell = bezierRing(poly.getExteriorRing());
    return geomFactory.createPolygon(shell, null);
  }
  
  private void addCurve(Coordinate p0, Coordinate p1,
      Coordinate ctrl0, Coordinate crtl1,
      CoordinateList curvePts) {
    double len = p0.distance(p1);
    if ( len < minSegmentLength ) {
      // segment too short - copy input coordinate
      curvePts.add(new Coordinate(p0));

    } else {
      cubicBezier(p0, p1, ctrl0, crtl1,
          interpolationParam, bezierCurvePts);
      for (int i = 0; i < bezierCurvePts.length - 1; i++) {
        curvePts.add(bezierCurvePts[i], false);
      }
    }
  }
  
  private Coordinate[][] controlPoints(Coordinate[] coords, boolean isRing, double alpha) {
    final int N = isRing ? coords.length - 1 : coords.length;
    double a1 = 1 - alpha;
    Coordinate[][] ctrl = new Coordinate[N][2];

    Coordinate v1 = coords[0];
    Coordinate v2 = coords[1];
    if (isRing) {
      v1 = coords[N - 1];
      v2 = coords[0];
    }
    
    double mid1x = (v1.x + v2.x) / 2.0;
    double mid1y = (v1.y + v2.y) / 2.0;
    double len1 = v1.distance(v2);

    final int start = isRing ? 0 : 1; 
    final int end = isRing ? N : N-1; 
    for (int i = start; i < end; i++) {
      v1 = coords[i];
      v2 = coords[i + 1];

      double mid0x = mid1x;
      double mid0y = mid1y;
      mid1x = (v1.x + v2.x) / 2.0;
      mid1y = (v1.y + v2.y) / 2.0;

      double len0 = len1;
      len1 = v1.distance(v2);

      double p = len0 / (len0 + len1);
      double anchorx = mid0x + p * (mid1x - mid0x);
      double anchory = mid0y + p * (mid1y - mid0y);
      double xdelta = anchorx - v1.x;
      double ydelta = anchory - v1.y;

      ctrl[i][0] = new Coordinate(
          a1 * (v1.x - mid0x + xdelta) + mid0x - xdelta,
          a1 * (v1.y - mid0y + ydelta) + mid0y - ydelta);

      ctrl[i][1] = new Coordinate(
          a1 * (v1.x - mid1x + xdelta) + mid1x - xdelta,
          a1 * (v1.y - mid1y + ydelta) + mid1y - ydelta);
      //System.out.println(WKTWriter.toLineString(v1, ctrl[i][0]));
      //System.out.println(WKTWriter.toLineString(v1, ctrl[i][1]));
    }
    /**
     * For a line, produce a symmetric curve for the first and last segments
     * by using mirrored control points for start and end vertex,
     */
    if (! isRing) {
      ctrl[0][1] = mirrorControlPoint(ctrl[1][0], coords[1], coords[0]);
      ctrl[N - 1][0] = mirrorControlPoint(ctrl[N - 2][1], coords[N - 1], coords[N - 2]);
    }
    return ctrl;
  }

  private static Coordinate mirrorControlPoint(Coordinate c, Coordinate p0, Coordinate p1) {
    double vlinex = p1.x - p0.x;
    double vliney = p1.y - p0.y;
    // rotate line vector by 90
    double vrotx = -vliney;
    double vroty = vlinex;

    double midx = (p0.x + p1.x) / 2;
    double midy = (p0.y + p1.y) / 2;

    return reflectPointInLine(c, new Coordinate(midx, midy), new Coordinate(midx + vrotx, midy + vroty));
  }

  private static Coordinate reflectPointInLine(Coordinate p, Coordinate p0, Coordinate p1) {
    double vx = p1.x - p0.x;
    double vy = p1.y - p0.y;
    double x = p0.x - p.x;
    double y = p0.y - p.y;
    double r = 1 / (vx * vx + vy * vy);
    double rx = p.x + 2 * (x - x * vx * vx * r - y * vx * vy * r);
    double ry = p.y + 2 * (y - y * vy * vy * r - x * vx * vy * r);
    return new Coordinate(rx, ry);
  }

  /**
   * Calculates vertices along a cubic Bezier curve.
   * 
   * @param start start point
   * @param end   end point
   * @param ctrl1 first control point
   * @param ctrl2 second control point
   * @param ip interpolation parameters
   * @param curve array to hold generated points
   */
  private void cubicBezier(final Coordinate start, 
      final Coordinate end, final Coordinate ctrl1, 
      final Coordinate ctrl2, CubicBezierInterpolationParam[] ip, Coordinate[] curve) {

    int n = curve.length;
    curve[0] = new Coordinate(start);
    curve[n - 1] = new Coordinate(end);

    for (int i = 1; i < n - 1; i++) {
      Coordinate c = new Coordinate();

      c.x = ip[i].t[0] * start.x + ip[i].t[1] * ctrl1.x + ip[i].t[2] * ctrl2.x + ip[i].t[3] * end.x;
      c.x /= ip[i].tsum;
      c.y = ip[i].t[0] * start.y + ip[i].t[1] * ctrl1.y + ip[i].t[2] * ctrl2.y + ip[i].t[3] * end.y;
      c.y /= ip[i].tsum;

      curve[i] = c;
    }
  }

  private static final class CubicBezierInterpolationParam {
    double[] t = new double[4];
    double tsum;
    
    /**
     * Gets the interpolation parameters for a Bezier curve approximated by the
     * given number of vertices.
     *
     * @param n number of vertices
     * @return array of {@code InterpPoint} objects holding the parameter values
     */
    private static CubicBezierInterpolationParam[] compute(int n) {
      CubicBezierInterpolationParam[] param = new CubicBezierInterpolationParam[n];

      for (int i = 0; i < n; i++) {
        double t = (double) i / (n - 1);
        double tc = 1.0 - t;

        param[i] = new CubicBezierInterpolationParam();
        param[i].t[0] = tc * tc * tc;
        param[i].t[1] = 3.0 * tc * tc * t;
        param[i].t[2] = 3.0 * tc * t * t;
        param[i].t[3] = t * t * t;
        param[i].tsum = param[i].t[0] + param[i].t[1] + param[i].t[2] + param[i].t[3];
      }
      return param;
    }
  }
  
}