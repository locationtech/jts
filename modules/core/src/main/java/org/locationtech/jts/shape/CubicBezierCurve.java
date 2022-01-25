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
 * and the alpha control parameter controlling curvedness, and
 * the optional skew parameter controlling the shape of the curve at vertices.
 * The Bezier Curves are created to be C2-continuous (smooth) 
 * at each input vertex.
 * <p>
 * Alternatively, the Bezier control points can be supplied explicitly.
 * <p>
 * The result is not guaranteed to be valid, since large alpha values
 * may cause self-intersections.
 */
public class CubicBezierCurve {

  /**
   * Creates a geometry of linearized Cubic Bezier Curves
   * defined by the segments of the input and a parameter
   * controlling how curved the result should be.
   * 
   * @param geom the geometry defining the curve
   * @param alpha curvedness parameter (0 is linear, 1 is round, >1 is increasingly curved)
   * @return the linearized curved geometry
   */
  public static Geometry bezierCurve(Geometry geom, double alpha) {
    CubicBezierCurve curve = new CubicBezierCurve(geom, alpha);
    return curve.getResult();
  }
  
  /**
   * Creates a geometry of linearized Cubic Bezier Curves
   * defined by the segments of the input and a parameter
   * controlling how curved the result should be, with a skew factor
   * affecting the curve shape at each vertex.
   * 
   * @param geom the geometry defining the curve
   * @param alpha curvedness parameter (0 is linear, 1 is round, >1 is increasingly curved)
   * @param skew the skew parameter (0 is none, positive skews towards longer side, negative towards shorter
   * @return the linearized curved geometry
   */
  public static Geometry bezierCurve(Geometry geom, double alpha, double skew) {
    CubicBezierCurve curve = new CubicBezierCurve(geom, alpha, skew);
    return curve.getResult();
  }
  
  /**
   * Creates a geometry of linearized Cubic Bezier Curves
   * defined by the segments of the input
   * and a list (or lists) of control points.
   * <p>
   * Typically the control point geometry 
   * is a {@link LineString} or {@link MultiLineString}
   * containing an element for each line or ring in the input geometry.
   * The list of control points for each linear element must contain two 
   * vertices for each segment (and thus <code>2 * npts - 2</code>).
   * 
   * @param geom the geometry defining the curve
   * @param controlPoints a geometry containing the control point elements.
   * @return the linearized curved geometry
   */
  public static Geometry bezierCurve(Geometry geom, Geometry controlPoints) {
    CubicBezierCurve curve = new CubicBezierCurve(geom, controlPoints);
    return curve.getResult();
  }
  
  private double minSegmentLength = 0.0;
  private int numVerticesPerSegment = 16;

  private Geometry inputGeom;
  private double alpha =-1;
  private double skew = 0;
  private Geometry controlPoints = null;
  private final GeometryFactory geomFactory;
  
  private Coordinate[] bezierCurvePts;
  private double[][] interpolationParam;
  private int controlPointIndex = 0;

  /**
   * Creates a new instance producing a Bezier curve defined by a geometry
   * and an alpha curvedness value.
   *
   * @param geom geometry defining curve
   * @param alpha curvedness parameter (0 = linear, 1 = round, 2 = distorted)
   */
  CubicBezierCurve(Geometry geom, double alpha) {
    this.inputGeom = geom;
    this.geomFactory = geom.getFactory();
    if ( alpha < 0.0 ) alpha = 0;
    this.alpha = alpha;
  }

  /**
   * Creates a new instance producing a Bezier curve defined by a geometry,
   * an alpha curvedness value, and a skew factor.
   * 
   * @param geom geometry defining curve
   * @param alpha curvedness parameter (0 is linear, 1 is round, >1 is increasingly curved)
   * @param skew the skew parameter (0 is none, positive skews towards longer side, negative towards shorter
   */
  CubicBezierCurve(Geometry geom, double alpha, double skew) {
    this.inputGeom = geom;
    this.geomFactory = geom.getFactory();
    if ( alpha < 0.0 ) alpha = 0;
    this.alpha = alpha;
    this.skew  = skew;
  }
  
  /**
   * Creates a new instance producing a Bezier curve defined by a geometry,
   * and a list (or lists) of control points.
   * <p>
   * Typically the control point geometry 
   * is a {@link LineString} or {@link MultiLineString}
   * containing an element for each line or ring in the input geometry.
   * The list of control points for each linear element must contain two 
   * vertices for each segment (and thus <code>2 * npts - 2</code>).
   * 
   * @param geom geometry defining curve
   * @param controlPoints the geometry containing the control points
   */
  CubicBezierCurve(Geometry geom, Geometry controlPoints) {
    this.inputGeom = geom;
    this.geomFactory = geom.getFactory();
    this.controlPoints = controlPoints;
  }
  
  /**
   * Gets the computed linearized Bezier curve geometry.
   * 
   * @return a linearized curved geometry
   */
  public Geometry getResult() {
    bezierCurvePts = new Coordinate[numVerticesPerSegment];
    interpolationParam = computeIterpolationParameters(numVerticesPerSegment);

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
    CoordinateList curvePts = bezierCurve(coords, false);
    curvePts.add(coords[coords.length - 1].copy(), false);
    return geomFactory.createLineString(curvePts.toCoordinateArray());
  }

  private LinearRing bezierRing(LinearRing ring) {
    Coordinate[] coords = ring.getCoordinates();
    CoordinateList curvePts = bezierCurve(coords, true);
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
  
  private CoordinateList bezierCurve(Coordinate[] coords, boolean isRing) {
    Coordinate[] control = controlPoints(coords, isRing);
    CoordinateList curvePts = new CoordinateList();
    for (int i = 0; i < coords.length - 1; i++) {
      int ctrlIndex = 2 * i;
      addCurve(coords[i], coords[i + 1], control[ctrlIndex], control[ctrlIndex + 1], curvePts);
    }
    return curvePts;
  }
  
  private Coordinate[] controlPoints(Coordinate[] coords, boolean isRing) {
    if (controlPoints != null) {
      if (controlPointIndex >= controlPoints.getNumGeometries()) {
        throw new IllegalArgumentException("Too few control point elements");
      }
      Geometry ctrlPtsGeom = controlPoints.getGeometryN(controlPointIndex++);
      Coordinate[] ctrlPts = ctrlPtsGeom.getCoordinates();
      
      int expectedNum1 = 2 * coords.length - 2;
      int expectedNum2 = isRing ? coords.length - 1 : coords.length;
      if (expectedNum1 != ctrlPts.length && expectedNum2 != ctrlPts.length) {
        throw new IllegalArgumentException(
            String.format("Wrong number of control points for element %d - expected %d or %d, found %d",
                controlPointIndex-1, expectedNum1, expectedNum2, ctrlPts.length
                  ));
      }
      return ctrlPts;
    }
    return controlPoints(coords, isRing, alpha, skew);
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
  
  //-- chosen to make curve at right-angle corners roughly circular
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
   * Alpha > 1 starts to distort the curve and may introduce self-intersections.
   * <p>
   * The control point array contains a pair of coordinates for each input segment.
   * 
   * @param coords
   * @param isRing
   * @param alpha determines the curviness
   * @return the control point array
   */
  private Coordinate[] controlPoints(Coordinate[] coords, boolean isRing, double alpha, double skew) {
    int N = coords.length;
    int start = 1; 
    int end = N - 1;
    if (isRing) {
      N = coords.length - 1;
      start = 0;
      end = N;
    }
    
    int nControl = 2 * coords.length - 2;
    Coordinate[] ctrl = new Coordinate[nControl];
    for (int i = start; i < end; i++) {
      int iprev = i == 0 ? N - 1 : i - 1;
      Coordinate v0 = coords[iprev];
      Coordinate v1 = coords[i];
      Coordinate v2 = coords[i + 1];

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
      if (skew != 0) {
        double stretch = Math.abs(dist0 - dist1) / Math.max(dist0, dist1);
        int skewIndex = dist0 > dist1 ? 0 : 1;
        if (skew < 0) skewIndex = 1 - skewIndex;
        if (skewIndex == 0) {
          stretch0 += Math.abs(skew) * stretch; 
        }
        else {
          stretch1 += Math.abs(skew) * stretch; 
        }
      }
      Coordinate ctl0 = Angle.project(v1, ang0, stretch0 * len);
      Coordinate ctl1 = Angle.project(v1, ang1, stretch1 * len);

      int index = 2 * i - 1;
      // for a ring case the first control point is for last segment
      int i0 = index < 0 ? nControl - 1 : index;
      ctrl[i0] = ctl0;
      ctrl[index + 1] = ctl1;
     
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
  private void setLineEndControlPoints(Coordinate[] coords, Coordinate[] ctrl) {
    int N = ctrl.length;
    ctrl[0] = mirrorControlPoint(ctrl[1], coords[1], coords[0]);
    ctrl[N - 1] = mirrorControlPoint(ctrl[N - 2], 
        coords[coords.length - 1], coords[coords.length - 2]);
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
   * @param p0 start point
   * @param p1   end point
   * @param ctrl1 first control point
   * @param ctrl2 second control point
   * @param param interpolation parameters
   * @param curve array to hold generated points
   */
  private void cubicBezier(final Coordinate p0, 
      final Coordinate p1, final Coordinate ctrl1, 
      final Coordinate ctrl2, double[][] param, 
      Coordinate[] curve) {

    int n = curve.length;
    curve[0] = new Coordinate(p0);
    curve[n - 1] = new Coordinate(p1);

    for (int i = 1; i < n - 1; i++) {
      Coordinate c = new Coordinate();
      double sum = param[i][0] + param[i][1] +param[i][2] +param[i][3];
      c.x = param[i][0] * p0.x + param[i][1] * ctrl1.x + param[i][2] * ctrl2.x + param[i][3] * p1.x;
      c.x /= sum;
      c.y = param[i][0] * p0.y + param[i][1] * ctrl1.y + param[i][2] * ctrl2.y + param[i][3] * p1.y;
      c.y /= sum;

      curve[i] = c;
    }
  }

  /**
   * Gets the interpolation parameters for a Bezier curve approximated by a
   * given number of vertices.
   *
   * @param n number of vertices
   * @return array of double[4] holding the parameter values
   */
  private static double[][] computeIterpolationParameters(int n) {
    double[][] param = new double[n][4];
    for (int i = 0; i < n; i++) {
      double t = (double) i / (n - 1);
      double tc = 1.0 - t;

      param[i][0] = tc * tc * tc;
      param[i][1] = 3.0 * tc * tc * t;
      param[i][2] = 3.0 * tc * t * t;
      param[i][3] = t * t * t;
    }
    return param;
  }
  
}