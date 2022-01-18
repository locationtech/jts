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

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryMapper;
import org.locationtech.jts.io.WKTWriter;

/**
 * Creates a curved geometry by replacing the segments
 * of the input with Cubic Bezier Curves.
 * The Bezier control points are determined from the segments of the geometry
 * and the alpha control parameter.
 * The Bezier Curves are created to be C2-continuous (smooth) 
 * at each input vertex.
 * <p>
 * The result is not guaranteed to be valid, since large alpha values
 * may cause self-intersections.
 */
public class CubicBezierCurve {

  /**
   * Creates a geometry using linearized Cubic Bezier Curves
   * defined by the segments of the input.
   * 
   * @param geom the geometry defining the curve
   * @param alpha curviness parameter (0 is linear, 1 is round, >1 is increasingly curved)
   * @return the curved geometry
   */
  public static Geometry bezierCurve(Geometry geom, double alpha) {
    CubicBezierCurve curve = new CubicBezierCurve(geom, alpha);
    return curve.getResult();
  }
  
  /**
   * Creates a geometry using linearized Cubic Bezier Curves
   * defined by the segments of the input, with a skew factor
   * affecting the shape at each vertex.
   * 
   * @param geom the geometry defining the curve
   * @param alpha curviness parameter (0 is linear, 1 is round, >1 is increasingly curved)
   * @param skew the skew parameter (0 is none, positive skews towards longer side, negative towards shorter
   * @return  the curved geometry
   */
  public static Geometry bezierCurve(Geometry geom, double alpha, double skew) {
    CubicBezierCurve curve = new CubicBezierCurve(geom, alpha);
    curve.setSkew(skew);
    return curve.getResult();
  }
  
  private double minSegmentLength = 0.0;
  private int numVerticesPerSegment = 16;

  private Geometry inputGeom;
  private double alpha;
  private double skewFactor = 0;;
  private final GeometryFactory geomFactory;
  
  private Coordinate[] bezierCurvePts;
  private CubicBezierInterpolationParam[] interpolationParam;

  /**
   * Creates a new instance.
   *
   * @param geom geometry defining curve
   * @param alpha curviness parameter (0 = linear, 1 = round, 2 = distorted)
   */
  CubicBezierCurve(Geometry geom, double alpha) {
    this.inputGeom = geom;
    //if ( alpha < 0.0 ) alpha = 0;
    this.alpha = alpha;
    this.geomFactory = geom.getFactory();
  }

  /**
   * Sets a skew factor influencing the shape of the curve corners.
   * 0 is no skew, positive skews towards longer edges, negative skews towards shorter.
   * 
   * @param skewFactor the skew factor
   */
  public void setSkew(double skewFactor) {
    this.skewFactor  = skewFactor;
  }
  
  /**
   * Gets the computed Bezier curve geometry.
   * 
   * @return the curved geometry
   */
  public Geometry getResult() {
    bezierCurvePts = new Coordinate[numVerticesPerSegment];
    interpolationParam = CubicBezierInterpolationParam.compute(numVerticesPerSegment);

    return GeometryMapper.flatMap(inputGeom, 1, new GeometryMapper.MapOp() {
      
      @Override
      public Geometry map(Geometry geom) {
        if (geom instanceof LineString) {
          return bezierLine((LineString) geom);
        }
        if (geom instanceof Polygon ) {
          return bezierPolygon((Polygon) geom);
        } 
        //-- Points
        return geom.copy();
      }
    });
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
    LinearRing[] holes = null;
    if (poly.getNumInteriorRing() > 0) {
      holes = new LinearRing[poly.getNumInteriorRing()];
      for (int i = 0; i < poly.getNumInteriorRing(); i++) {
        holes[i] = bezierRing(poly.getInteriorRingN(i));
      }
    }
    return geomFactory.createPolygon(shell, holes);
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
  
  //-- makes curve at right-angle corners roughly circular
  private static final double CIRCLE_LEN_FACTOR = 3.0 / 8.0;
  
  /**
   * Creates control points for each vertex of curve.
   * The control points are collinear with each vertex, 
   * thus providing C1-continuity.
   * By default the control vectors are the same length, 
   * which provides C2-continuity (same curvature on each
   * side of vertex.
   * The alpha parameter controls the length of the control vectors.
   * Alpha = 0 makes the vectors zero-length, and hence flattens the curves.
   * Alpha = 1 makes the curve at right angles roughly circular.
   * Alpha > 1 starts to distort the curve and may introduce self-intersections
   * 
   * @param coords
   * @param isRing
   * @param alpha determines the curviness
   * @return
   */
  private Coordinate[][] controlPoints(Coordinate[] coords, boolean isRing, double alpha) {
    final int N = isRing ? coords.length - 1 : coords.length;
    Coordinate[][] ctrl = new Coordinate[N][2];

    Coordinate v0 = coords[0];
    Coordinate v1 = coords[1];
    Coordinate v2 = coords[2];
    if (isRing) {
      v0 = coords[N - 1];
      v1 = coords[0];
      v1 = coords[1];
    }
    
    final int start = isRing ? 0 : 1; 
    final int end = isRing ? N : N - 1; 
    for (int i = start; i < end; i++) {
      int iprev = i == 0 ? N - 1 : i - 1;
      v0 = coords[iprev];
      v1 = coords[i];
      v2 = coords[i + 1];

      double interiorAng = Angle.angleBetweenOriented(v0, v1, v2);
      double orient = Math.signum(interiorAng);
      double angBisect = Angle.bisector(v0, v1, v2);
      double ang0 = angBisect - orient * Angle.PI_OVER_2;
      double ang1 = angBisect + orient * Angle.PI_OVER_2;
      
      double dist0 = v1.distance(v0);
      double dist1 = v1.distance(v2);
      double lenBase = Math.min(dist0, dist1);
      
      double intAngAbs = Math.abs(interiorAng);
      
      //-- make acute corners sharper by shortening tangent vectors
      double sharpnessFactor = intAngAbs >= Angle.PI_OVER_2 ? 1 : intAngAbs / Angle.PI_OVER_2;
      
      double len = alpha * CIRCLE_LEN_FACTOR * sharpnessFactor * lenBase;
      double stretch0 = 1;
      double stretch1 = 1;
      if (skewFactor != 0) {
        double stretch = Math.abs(dist0 - dist1) / Math.max(dist0, dist1);
        int skewIndex = dist0 > dist1 ? 0 : 1;
        if (skewFactor < 0) skewIndex = 1 - skewIndex;
        if (skewIndex == 0) {
          stretch0 += Math.abs(skewFactor) * stretch; 
        }
        else {
          stretch1 += Math.abs(skewFactor) * stretch; 
        }
      }
      Coordinate ctl0 = Angle.project(v1, ang0, stretch0 * len);
      Coordinate ctl1 = Angle.project(v1, ang1, stretch1 * len);

      ctrl[i][0] = ctl0;
      ctrl[i][1] = ctl1;
     
      //System.out.println(WKTWriter.toLineString(v1, ctl0));
      //System.out.println(WKTWriter.toLineString(v1, ctl1));
    }
    if (! isRing) {
      setLineEndControlPoints(coords, ctrl);
    }
    return ctrl;
  }

  /**
   * Sets the end control points for a line.
   * Produce a symmetric curve for the first and last segments
   * by using mirrored control points for start and end vertex.
   * 
   * @param coords
   * @param ctrl
   */
  private void setLineEndControlPoints(Coordinate[] coords, Coordinate[][] ctrl) {
    int N = coords.length;
    ctrl[0][1] = mirrorControlPoint(ctrl[1][0], coords[1], coords[0]);
    ctrl[N - 1][0] = mirrorControlPoint(ctrl[N - 2][1], coords[N - 1], coords[N - 2]);
  }

  /**
   * Creates a control point aimed at the control point at the opposite end of the segment.
   * 
   * Produces overly flat results, so not used currently.
   * 
   * @param c
   * @param p1
   * @param p0
   * @return
   */
  private static Coordinate aimedControlPoint(Coordinate c, Coordinate p1, Coordinate p0) {
    double len = p1.distance(c);
    double ang = Angle.angle(p0, p1);
    return Angle.project(p0, ang, len);
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