/*
 * Copyright (c) 2019 Felix Obermaier.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/**
 * Functions to compute squared distances between basic geometric structures.
 * Squared distances are useful if only the relative value is of interest, i.e. is
 * point {@code p1} closer to the point {@code p0} than {@code p2}.
 *
 * To compute the <i>squared distance</i> is faster than computing the real distance
 * because there is no call to {@link Math#sqrt(double)} involved.
 *
 * @author Felix Obermaier
 * @since 1.17
 *
 */
public class SquaredDistance {

  /**
   * Computes the squared distance from a point {@code p0} to a point {@code p1}
   *
   * Note: NON-ROBUST!
   *
   * @param p0
   *          a point
   * @param p1
   *          another point
   *
   * @return the squared distance from {@code p0} to {@code p1}
   */
  public static double pointToPoint(Coordinate p0, Coordinate p1) {
    double dx = p1.x - p0.x;
    double dy = p1.y - p0.y;

    return dx*dx + dy*dy;
  }

  /**
   * Computes the squared distance from a point {@code p} to a line segment |{@code A}{@code B}|
   *
   * Note: NON-ROBUST!
   *
   * @param p
   *          the point to compute the distance for
   * @param A
   *          point A of the segment AB
   * @param B
   *          point B of the segment AB
   *
   * @return the squared distance from p to line segment AB
   */
  public static double pointToSegment(Coordinate p, Coordinate A, Coordinate B) {

    double x = A.x;
    double y = A.y;
    double dx = B.x - x;
    double dy = B.y - y;

    if (dx != 0 || dy != 0) {

      double t = ((p.x - x) * dx + (p.y - y) * dy) / (dx * dx + dy * dy);

      if (t > 1) {
        x = B.x;
        y = B.y;

      } else if (t > 0) {
        x += dx * t;
        y += dy * t;
      }
    }

    dx = p.x - x;
    dy = p.y - y;

    return dx * dx + dy * dy;
  }

  /**
   * Computes the squared distance from a point to a sequence of line segments.
   *
   * @param p
   *          a point
   * @param line
   *          a sequence of contiguous line segments defined by their vertices
   *
   * @return the minimum squared distance between the point and the line segments
   */
  public static double pointToSegmentString(Coordinate p, Coordinate[] line)
  {
    if (line.length == 0)
      throw new IllegalArgumentException(
        "Line array must contain at least one vertex");
    // this handles the case of length = 1
    double minDistance = pointToPoint(p, line[0]);
    for (int i = 0; i < line.length - 1; i++) {
      double dist = pointToSegment(p, line[i], line[i + 1]);
      if (dist < minDistance) {
        minDistance = dist;
      }
    }
    return minDistance;
  }

  /**
   * Computes the squared distance from a line segment |{@code A}{@code B}| to
   * a line segment |{@code C}{@code D}|
   *
   * Note: NON-ROBUST!
   *
   * @param A
   *          point A of the segment AB
   * @param B
   *          point B of the segment AB
   * @param C
   *          point C of the segment CD
   * @param D
   *          point D of the segment CD
   */
  public static double segmentToSegment(Coordinate A, Coordinate B, Coordinate C, Coordinate D) {
    return segmentToSegment(A.x, A.y, B.x, B.y, C.x, C.y, D.x, D.y);
  }

  /**
   * Computes the squared distance from a line segment |{@code A}{@code B}| to
   * a line segment |{@code C}{@code D}|
   *
   * Note: NON-ROBUST!
   *
   * @param ax
   *          the x-ordinate of point A of the segment AB
   * @param ay
   *          the y-ordinate of point A of the segment AB
   * @param bx
   *          the x-ordinate of point B of the segment AB
   * @param by
   *          the y-ordinate of point B of the segment AB
   * @param cx
   *          the x-ordinate of point C of the segment CD
   * @param cy
   *          the y-ordinate of point C of the segment CD
   * @param dx
   *          the x-ordinate of point D of the segment CD
   * @param dy
   *          the y-ordinate of point D of the segment CD

   * Direct port of code provided by Dan Sunday
   * <a href="http://geomalgorithms.com/a07-_distance.html"/>
   */
  private static double segmentToSegment(double ax, double ay, double bx, double by,
                                         double cx, double cy, double dx, double dy) {
    double ux = bx - ax;
    double uy = by - ay;
    double vx = dx - cx;
    double vy = dy - cy;
    double wx = ax - cx;
    double wy = ay - cy;
    double a = ux * ux + uy * uy;
    double b = ux * vx + uy * vy;
    double c = vx * vx + vy * vy;
    double d = ux * wx + uy * wy;
    double e = vx * wx + vy * wy;
    double D = a * c - b * b;

    double sc, sN, tc, tN;
    double sD = D;
    double tD = D;

    if (D == 0) {
      sN = 0;
      sD = 1;
      tN = e;
      tD = c;
    } else {
      sN = b * e - c * d;
      tN = a * e - b * d;
      if (sN < 0) {
        sN = 0;
        tN = e;
        tD = c;
      } else if (sN > sD) {
        sN = sD;
        tN = e + b;
        tD = c;
      }
    }

    if (tN < 0.0) {
      tN = 0.0;
      if (-d < 0.0) sN = 0.0;
      else if (-d > a) sN = sD;
      else {
        sN = -d;
        sD = a;
      }
    } else if (tN > tD) {
      tN = tD;
      if ((-d + b) < 0.0) sN = 0;
      else if (-d + b > a) sN = sD;
      else {
        sN = -d + b;
        sD = a;
      }
    }

    sc = sN == 0 ? 0 : sN / sD;
    tc = tN == 0 ? 0 : tN / tD;

    double lcx = (1 - sc) * ax + sc * bx;
    double lcy = (1 - sc) * ay + sc * by;
    double lcx2 = (1 - tc) * cx + tc * dx;
    double lcy2 = (1 - tc) * cy + tc * dy;
    double ldx = lcx2 - lcx;
    double ldy = lcy2 - lcy;

    return ldx * ldx + ldy * ldy;
  }

  /**
   * Computes the squared distance between the segment {@code A}{@code B} and the
   * { @link Envelope } {@code bounds}
   *
   * Note: NON-ROBUST!
   *
   * @param A the starting point of the segment
   * @param B the end point of the segment
   * @param bounds the bounds
   * @return the distance between AB and the envelope.
   */
  public static double segmentToEnvelope(Coordinate A, Coordinate B, Envelope bounds) {
    if (bounds.contains(A) || bounds.contains(B))
      return 0;
    double d1 = segmentToSegment(A.x, A.y, B.x, B.y, bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMinY());
    if (d1 == 0) return 0;
    double d2 = segmentToSegment(A.x, A.y, B.x, B.y, bounds.getMinX(), bounds.getMinY(), bounds.getMinX(), bounds.getMaxY());
    if (d2 == 0) return 0;
    double d3 = segmentToSegment(A.x, A.y, B.x, B.y, bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
    if (d3 == 0) return 0;
    double d4 = segmentToSegment(A.x, A.y, B.x, B.y, bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMaxY());
    if (d4 == 0) return 0;

    return Math.min(Math.min(d1, d2), Math.min(d3, d4));
  }
}
