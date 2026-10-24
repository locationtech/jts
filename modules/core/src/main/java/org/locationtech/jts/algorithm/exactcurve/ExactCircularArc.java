/*
 * Copyright (c) 2026 Jeroen Bloemscheer.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
/*
 * AI Disclosure: This file was largely AI-generated.
 * The AI-generated portions are made available under CC0-1.0 and not subject to the project's licence.
 * The human contributor has reviewed and verified that the code is correct.
 * SPDX-License-Identifier: EPL-2.0 OR EDL-1.0 and CC0-1.0
 * Assisted-by: Cursor Agent
 * Assisted-by: xAI Grok
 */
package org.locationtech.jts.algorithm.exactcurve;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

/**
 * An exact circular-arc window defined by three control points
 * (start, mid, end), matching GeoTools {@code CircularArc} and
 * GEOS {@code CircularArc} / SQL/MM CIRCULARSTRING segments.
 * <p>
 * Collinear or degenerate windows degrade to the control-point chord
 * (start–mid–end), never a bogus huge circle. Length is the true
 * arc length ({@code radius * sweep}) when circular, and the chord
 * path length when collinear. {@link #isExact()} is {@code true} for
 * both the circular and the exact-chord cases.
 * <p>
 * Full-circle convention: {@code CIRCULARSTRING(s, m, s)} is a circle
 * whose <em>diameter</em> is the segment {@code s–m}
 * (center = midpoint of {@code s} and {@code m},
 * radius = {@code |s-m|/2}, sweep = 2π).
 *
 * @author Jeroen Bloemscheer
 */
public class ExactCircularArc implements ExactCurve {

  /**
   * Default number of linearized segments per quadrant when no
   * finite tolerance is supplied (JTS buffer default).
   */
  public static final int DEFAULT_SEGMENTS_PER_QUADRANT = 8;

  /**
   * Upper bound on segments per quadrant for a zero tolerance.
   */
  public static final int MAX_SEGMENTS_PER_QUADRANT = 128;

  private static final double ANGLE_EPS = 1.0e-12;

  private final Coordinate p0;
  private final Coordinate p1;
  private final Coordinate p2;

  private final boolean collinear;
  private final boolean fullCircle;
  private final Coordinate center;
  private final double radius;
  private final double sweep;
  private final boolean ccw;

  /**
   * Creates an arc through {@code start}, {@code mid}, {@code end}.
   *
   * @param start the start control
   * @param mid the on-arc control
   * @param end the end control
   */
  public ExactCircularArc(Coordinate start, Coordinate mid, Coordinate end) {
    if (start == null || mid == null || end == null) {
      throw new IllegalArgumentException("ExactCircularArc control points must not be null");
    }
    this.p0 = start.copy();
    this.p1 = mid.copy();
    this.p2 = end.copy();

    if (p0.equals2D(p2) && !p0.equals2D(p1)) {
      this.fullCircle = true;
      this.collinear = false;
      this.center = new Coordinate(
          p0.x + (p1.x - p0.x) / 2.0,
          p0.y + (p1.y - p0.y) / 2.0);
      this.radius = p0.distance(center);
      this.sweep = Angle.PI_TIMES_2;
      this.ccw = true;
      return;
    }

    this.fullCircle = false;
    CircleFit fit = fitCircle(p0, p1, p2);
    if (p0.equals2D(p1) && p1.equals2D(p2)
        || Orientation.index(p0, p1, p2) == Orientation.COLLINEAR
        || fit == null) {
      this.collinear = true;
      this.center = null;
      this.radius = Double.POSITIVE_INFINITY;
      this.sweep = 0.0;
      this.ccw = false;
      return;
    }

    this.collinear = false;
    this.center = fit.center;
    this.radius = fit.radius;
    double a0 = Math.atan2(p0.y - center.y, p0.x - center.x);
    double a1 = Math.atan2(p1.y - center.y, p1.x - center.x);
    double a2 = Math.atan2(p2.y - center.y, p2.x - center.x);
    double ccwToEnd = Angle.normalizePositive(a2 - a0);
    double ccwToMid = Angle.normalizePositive(a1 - a0);
    boolean midOnCcw = ccwToMid > ANGLE_EPS && ccwToMid < ccwToEnd - ANGLE_EPS;
    if (ccwToEnd <= ANGLE_EPS) {
      this.sweep = 0.0;
      this.ccw = true;
    }
    else if (midOnCcw || almostEqual(ccwToMid, ccwToEnd)) {
      this.sweep = ccwToEnd;
      this.ccw = true;
    }
    else {
      this.sweep = Angle.PI_TIMES_2 - ccwToEnd;
      this.ccw = false;
    }
  }

  private static CircleFit fitCircle(Coordinate p0, Coordinate p1, Coordinate p2) {
    double ax = p0.x;
    double ay = p0.y;
    double bx = p1.x;
    double by = p1.y;
    double cx = p2.x;
    double cy = p2.y;
    double d = 2.0 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by));
    if (!Double.isFinite(d) || d == 0.0) {
      return null;
    }
    double a2 = ax * ax + ay * ay;
    double b2 = bx * bx + by * by;
    double c2 = cx * cx + cy * cy;
    double ux = (a2 * (by - cy) + b2 * (cy - ay) + c2 * (ay - by)) / d;
    double uy = (a2 * (cx - bx) + b2 * (ax - cx) + c2 * (bx - ax)) / d;
    if (!Double.isFinite(ux) || !Double.isFinite(uy)) {
      return null;
    }
    double r = Math.hypot(ax - ux, ay - uy);
    if (!Double.isFinite(r) || r == 0.0) {
      return null;
    }
    return new CircleFit(new Coordinate(ux, uy), r);
  }

  /**
   * Gets the start control point.
   *
   * @return a copy of the start control
   */
  public Coordinate getStart() {
    return p0.copy();
  }

  /**
   * Gets the mid control point.
   *
   * @return a copy of the mid control
   */
  public Coordinate getMid() {
    return p1.copy();
  }

  /**
   * Gets the end control point.
   *
   * @return a copy of the end control
   */
  public Coordinate getEnd() {
    return p2.copy();
  }

  /**
   * Gets the circle center, or {@code null} if this window is a chord.
   *
   * @return the center, or {@code null}
   */
  public Coordinate getCenter() {
    return center == null ? null : center.copy();
  }

  /**
   * Gets the circle radius, or {@link Double#POSITIVE_INFINITY} if collinear.
   *
   * @return the radius
   */
  public double getRadius() {
    return radius;
  }

  /**
   * Tests whether this window is a complete circle (start equals end,
   * mid distinct).
   *
   * @return {@code true} if this is a full circle
   */
  public boolean isFullCircle() {
    return fullCircle;
  }

  public boolean isCollinear() {
    return collinear;
  }

  /**
   * Signed sweep angle in radians ({@code 0} if collinear).
   *
   * @return the sweep magnitude
   */
  public double getSweep() {
    return sweep;
  }

  public boolean isExact() {
    return true;
  }

  public double length() {
    if (collinear) {
      return p0.distance(p1) + p1.distance(p2);
    }
    return radius * sweep;
  }

  /**
   * Alias of {@link #length()}.
   *
   * @return the exact length
   */
  public double getLength() {
    return length();
  }

  public Coordinate pointAt(double t) {
    if (t <= 0.0) {
      return p0.copy();
    }
    if (t >= 1.0) {
      return p2.copy();
    }
    if (collinear || sweep <= ANGLE_EPS) {
      return pointAtChord(t);
    }
    double dir = ccw ? 1.0 : -1.0;
    double a0 = Math.atan2(p0.y - center.y, p0.x - center.x);
    return pointOnCircle(a0 + dir * t * sweep);
  }

  private Coordinate pointAtChord(double t) {
    double d1 = p0.distance(p1);
    double d2 = p1.distance(p2);
    double tot = d1 + d2;
    if (tot == 0.0) {
      return p0.copy();
    }
    double s = t * tot;
    if (s <= d1) {
      return interpolate(p0, p1, d1 == 0.0 ? 0.0 : s / d1);
    }
    return interpolate(p1, p2, d2 == 0.0 ? 0.0 : (s - d1) / d2);
  }

  private static Coordinate interpolate(Coordinate a, Coordinate b, double f) {
    return new Coordinate(a.x + f * (b.x - a.x), a.y + f * (b.y - a.y));
  }

  public Coordinate[] toLinear(double tolerance) {
    List<Coordinate> dest = new ArrayList<Coordinate>();
    appendLinearized(dest, tolerance, true);
    return dest.toArray(new Coordinate[dest.size()]);
  }

  public void expandEnvelope(Envelope envelope) {
    envelope.expandToInclude(p0);
    envelope.expandToInclude(p1);
    envelope.expandToInclude(p2);
    if (collinear || center == null) {
      return;
    }
    expandIfOnArc(envelope, 0.0);
    expandIfOnArc(envelope, Angle.PI_OVER_2);
    expandIfOnArc(envelope, Math.PI);
    expandIfOnArc(envelope, -Angle.PI_OVER_2);
  }

  private void expandIfOnArc(Envelope envelope, double theta) {
    if (containsAngle(theta)) {
      envelope.expandToInclude(
          center.x + radius * Math.cos(theta),
          center.y + radius * Math.sin(theta));
    }
  }

  private boolean containsAngle(double theta) {
    if (fullCircle) {
      return true;
    }
    double a0 = Math.atan2(p0.y - center.y, p0.x - center.x);
    double offset = ccw
        ? Angle.normalizePositive(theta - a0)
        : Angle.normalizePositive(a0 - theta);
    return offset >= -ANGLE_EPS && offset <= sweep + ANGLE_EPS;
  }

  /**
   * Appends a linearized polyline for this window to {@code dest}.
   * Densification is explicit: callers name the tolerance.
   * Collinear windows emit the three control points (a chord path).
   *
   * @param dest destination list
   * @param tolerance max distance from the true arc; {@code 0} uses the
   *        maximum segment count; non-finite uses
   *        {@link #DEFAULT_SEGMENTS_PER_QUADRANT}
   * @param includeStart whether to emit the start control
   */
  public void appendLinearized(List<Coordinate> dest, double tolerance, boolean includeStart) {
    if (includeStart) {
      dest.add(p0.copy());
    }
    if (collinear || sweep <= ANGLE_EPS) {
      if (!p0.equals2D(p1)) {
        dest.add(p1.copy());
      }
      if (!p1.equals2D(p2) || dest.isEmpty()) {
        dest.add(p2.copy());
      }
      else if (includeStart && p0.equals2D(p2) && dest.size() == 1) {
        dest.add(p2.copy());
      }
      return;
    }

    int nSeg = segmentsForTolerance(tolerance);
    double dir = ccw ? 1.0 : -1.0;
    double a0 = Math.atan2(p0.y - center.y, p0.x - center.x);
    double a1 = Math.atan2(p1.y - center.y, p1.x - center.x);
    double midOffset = ccw
        ? Angle.normalizePositive(a1 - a0)
        : Angle.normalizePositive(a0 - a1);
    if (fullCircle && midOffset <= ANGLE_EPS) {
      midOffset = Math.PI;
    }

    boolean midAdded = p0.equals2D(p1);
    for (int i = 1; i < nSeg; i++) {
      double offset = sweep * i / nSeg;
      if (!midAdded && offset >= midOffset - ANGLE_EPS) {
        dest.add(p1.copy());
        midAdded = true;
        if (Math.abs(offset - midOffset) <= ANGLE_EPS) {
          continue;
        }
      }
      dest.add(pointOnCircle(a0 + dir * offset));
    }
    if (!midAdded && !p1.equals2D(p2)) {
      dest.add(p1.copy());
    }
    dest.add(p2.copy());
  }

  private int segmentsForTolerance(double tolerance) {
    double quad = Angle.PI_OVER_2;
    int perQuad;
    if (tolerance == 0.0) {
      perQuad = MAX_SEGMENTS_PER_QUADRANT;
    }
    else if (!Double.isFinite(tolerance) || tolerance < 0) {
      perQuad = DEFAULT_SEGMENTS_PER_QUADRANT;
    }
    else {
      double sagitta = radius * (1.0 - Math.cos(sweep / 2.0));
      if (tolerance >= sagitta) {
        return 1;
      }
      double cosArg = 1.0 - tolerance / radius;
      if (cosArg <= -1.0) {
        perQuad = DEFAULT_SEGMENTS_PER_QUADRANT;
      }
      else {
        double maxStep = 2.0 * Math.acos(Math.min(1.0, Math.max(-1.0, cosArg)));
        if (!Double.isFinite(maxStep) || maxStep <= 0.0) {
          perQuad = DEFAULT_SEGMENTS_PER_QUADRANT;
        }
        else {
          int needed = (int) Math.ceil(sweep / maxStep);
          int maxSeg = MAX_SEGMENTS_PER_QUADRANT * Math.max(1, (int) Math.ceil(sweep / quad));
          return Math.max(1, Math.min(needed, maxSeg));
        }
      }
    }
    int n = (int) Math.ceil(sweep / quad * perQuad);
    return Math.max(1, n);
  }

  private Coordinate pointOnCircle(double angle) {
    return new Coordinate(
        center.x + radius * Math.cos(angle),
        center.y + radius * Math.sin(angle));
  }

  private static boolean almostEqual(double a, double b) {
    return Math.abs(a - b) <= ANGLE_EPS;
  }

  public String toString() {
    return "ExactCircularArc[" + p0 + ", " + p1 + ", " + p2 + "]";
  }

  private static final class CircleFit {
    private final Coordinate center;
    private final double radius;

    private CircleFit(Coordinate center, double radius) {
      this.center = center;
      this.radius = radius;
    }
  }
}
