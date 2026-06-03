/*
 * Copyright (c) 2026 grootstebozewolf
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.curved;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * A connected sequence of circular arcs, where each consecutive triple of
 * control points (start, mid, end) defines one arc and the end point of one
 * arc is the start point of the next.
 * <p>
 * This is a phase-1 stand-in: the control points are stored as a single
 * {@link CoordinateSequence} (inherited via {@link LineString}) and spatial
 * operations fall through to the parent's polyline behaviour. Native
 * arc-aware algorithms are out of scope for this module today.
 */
public class CircularString extends LineString implements Linearizable {
  private static final long serialVersionUID = 1L;

  public CircularString(CoordinateSequence points, GeometryFactory factory) {
    super(points, factory);
  }

  @Override
  public String getGeometryType() {
    return "CircularString";
  }

  /**
   * B-CC (lineal) guard for CircularString: explicit override of the
   * inherited line boundary contract, for symmetry with the CompoundCurve
   * guard and to assert the intent for curved lineals.
   *
   * <p>CircularString is a 1D lineal; its boundary is therefore the same
   * as LineString: open -> MultiPoint of its two control endpoints
   * (start of first arc, end of last arc); closed -> empty (modulo bnRule).
   * We make this explicit so the contract is visible on the curved subtype.
   */
  @Override
  public Geometry getBoundary() {
    return super.getBoundary();
  }

  @Override
  protected CircularString copyInternal() {
    return new CircularString(getCoordinateSequence().copy(), getFactory());
  }

  @Override
  public Geometry toLinear(double tolerance) {
    return getFactory().createLineString(getCoordinateSequence().copy());
  }

  @Override
  public double getLength() {
    // M-LEN-CS green: analytical sum, not chord sum of controls.
    // Walks the control seq taking every consecutive triple (stride 2) as one arc.
    CoordinateSequence cs = getCoordinateSequence();
    int n = cs.size();
    if (n < 3) return 0.0;
    double len = 0.0;
    for (int i = 0; i + 2 < n; i += 2) {
      len += exactCircularArcLength(
          cs.getX(i), cs.getY(i),
          cs.getX(i + 1), cs.getY(i + 1),
          cs.getX(i + 2), cs.getY(i + 2)
      );
    }
    return len;
  }

  /**
   * Exact arc length for one circular arc given its 3 control points.
   * (Inlined here for main-code use by getLength(); the test CurveRefRunner
   * keeps its own copy for adversarial/hunter isolation.)
   */
  private static double exactCircularArcLength(double sx, double sy,
                                               double mx, double my,
                                               double ex, double ey) {
    double d = 2 * (sx * (my - ey) + mx * (ey - sy) + ex * (sy - my));
    if (Math.abs(d) < 1e-12) {
      return Math.hypot(ex - sx, ey - sy);
    }
    double cx = ((sx * sx + sy * sy) * (my - ey)
               + (mx * mx + my * my) * (ey - sy)
               + (ex * ex + ey * ey) * (sy - my)) / d;
    double cy = ((sx * sx + sy * sy) * (ex - mx)
               + (mx * mx + my * my) * (sx - ex)
               + (ex * ex + ey * ey) * (mx - sx)) / d;
    double r = Math.hypot(sx - cx, sy - cy);
    if (r < 1e-12) {
      return Math.hypot(ex - sx, ey - sy);
    }
    double a0 = Math.atan2(sy - cy, sx - cx);
    double a1 = Math.atan2(my - cy, mx - cx);
    double a2 = Math.atan2(ey - cy, ex - cx);
    double sweep = a2 - a0;
    sweep = ((sweep + Math.PI) % (2 * Math.PI)) - Math.PI;
    double aMidRel = a1 - a0;
    aMidRel = ((aMidRel + Math.PI) % (2 * Math.PI)) - Math.PI;
    if (Math.signum(sweep) * Math.signum(aMidRel) < 0 && Math.abs(sweep) < Math.PI) {
      sweep = (sweep > 0 ? sweep - 2 * Math.PI : sweep + 2 * Math.PI);
    }
    double theta = Math.abs(sweep);
    return r * theta;
  }

  /**
   * Computes the circumcenter (cx, cy) and radius r for the circle through
   * three points defining a circular arc. Returns null if the points are
   * collinear (degenerate arc, treat as line segment).
   */
  static double[] computeCircle(double sx, double sy, double mx, double my, double ex, double ey) {
    double d = 2 * (sx * (my - ey) + mx * (ey - sy) + ex * (sy - my));
    if (Math.abs(d) < 1e-12) {
      return null; // collinear / degenerate
    }
    double cx = ((sx * sx + sy * sy) * (my - ey)
               + (mx * mx + my * my) * (ey - sy)
               + (ex * ex + ey * ey) * (sy - my)) / d;
    double cy = ((sx * sx + sy * sy) * (ex - mx)
               + (mx * mx + my * my) * (sx - ex)
               + (ex * ex + ey * ey) * (mx - sx)) / d;
    double r = Math.hypot(sx - cx, sy - cy);
    if (r < 1e-12) return null;
    return new double[] { cx, cy, r };
  }

  /**
   * Returns the start, mid, end angles for the arc on its circle.
   * The sweep direction is chosen so that mid is on the short arc.
   */
  static double[] computeArcAngles(double cx, double cy,
                                   double sx, double sy, double mx, double my, double ex, double ey) {
    double a0 = Math.atan2(sy - cy, sx - cx);
    double a1 = Math.atan2(my - cy, mx - cx);
    double a2 = Math.atan2(ey - cy, ex - cx);
    double sweep = a2 - a0;
    sweep = ((sweep + Math.PI) % (2 * Math.PI)) - Math.PI;
    double aMidRel = a1 - a0;
    aMidRel = ((aMidRel + Math.PI) % (2 * Math.PI)) - Math.PI;
    if (Math.signum(sweep) * Math.signum(aMidRel) < 0 && Math.abs(sweep) < Math.PI) {
      sweep = (sweep > 0 ? sweep - 2 * Math.PI : sweep + 2 * Math.PI);
    }
    return new double[] { a0, a2, sweep };
  }

  /**
   * Tests whether a point lies on the arc (within the angular sweep), not
   * counting the exact endpoints (for intersection "proper" checks).
   */
  static boolean pointOnArcInterior(double px, double py,
                                    double cx, double cy, double r,
                                    double a0, double a2, double sweep) {
    double ap = Math.atan2(py - cy, px - cx);
    // normalize relative
    double rel = ap - a0;
    rel = ((rel + Math.PI) % (2 * Math.PI)) - Math.PI;
    double absSweep = Math.abs(sweep);
    if (absSweep < 1e-12) return false;
    // check within the directed sweep, with small eps for numeric
    double t = rel / sweep;
    return t > 1e-9 && t < 1 - 1e-9;
  }

  /**
   * Computes the (up to 2) intersection points of two circles.
   * Returns empty list if none or tangent (we treat tangent as not crossing for validity).
   */
  static java.util.List<org.locationtech.jts.geom.Coordinate> circleIntersections(
      double cx1, double cy1, double r1, double cx2, double cy2, double r2) {
    java.util.List<org.locationtech.jts.geom.Coordinate> res = new java.util.ArrayList<>();
    double d = Math.hypot(cx2 - cx1, cy2 - cy1);
    if (d > r1 + r2 + 1e-9 || d + Math.min(r1, r2) < Math.max(r1, r2) - 1e-9 || d < 1e-12) {
      return res;
    }
    double a = (r1 * r1 - r2 * r2 + d * d) / (2 * d);
    double h = Math.sqrt(Math.max(0, r1 * r1 - a * a));
    double xm = cx1 + a * (cx2 - cx1) / d;
    double ym = cy1 + a * (cy2 - cy1) / d;
    if (h < 1e-9) {
      res.add(new org.locationtech.jts.geom.Coordinate(xm, ym));
      return res;
    }
    double xs1 = xm + h * (cy2 - cy1) / d;
    double ys1 = ym - h * (cx2 - cx1) / d;
    double xs2 = xm - h * (cy2 - cy1) / d;
    double ys2 = ym + h * (cx2 - cx1) / d;
    res.add(new org.locationtech.jts.geom.Coordinate(xs1, ys1));
    res.add(new org.locationtech.jts.geom.Coordinate(xs2, ys2));
    return res;
  }

  /**
   * Tests if two arcs (defined by their 3 control points) intersect properly
   * (at a point that is interior to both arcs, not an endpoint).
   * Used for self-intersection checks in rings for V-CP / V-CS.
   */
  static boolean arcsIntersectProper(double[] a1, double[] a2) {  // a1 = {sx,sy,mx,my,ex,ey}
    double[] circ1 = computeCircle(a1[0], a1[1], a1[2], a1[3], a1[4], a1[5]);
    double[] circ2 = computeCircle(a2[0], a2[1], a2[2], a2[3], a2[4], a2[5]);
    if (circ1 == null || circ2 == null) {
      // degenerate: fall back to chord intersection (conservative for now)
      return chordIntersect(a1, a2);
    }
    double cx1 = circ1[0], cy1=circ1[1], r1=circ1[2];
    double cx2 = circ2[0], cy2=circ2[1], r2=circ2[2];
    java.util.List<org.locationtech.jts.geom.Coordinate> pts = circleIntersections(cx1, cy1, r1, cx2, cy2, r2);
    double[] angs1 = computeArcAngles(cx1, cy1, a1[0],a1[1], a1[2],a1[3], a1[4],a1[5]);
    double[] angs2 = computeArcAngles(cx2, cy2, a2[0],a2[1], a2[2],a2[3], a2[4],a2[5]);
    for (org.locationtech.jts.geom.Coordinate p : pts) {
      // skip if very close to any endpoint of either
      if (nearEndpoint(p, a1) || nearEndpoint(p, a2)) continue;
      if (pointOnArcInterior(p.x, p.y, cx1, cy1, r1, angs1[0], angs1[1], angs1[2])
          && pointOnArcInterior(p.x, p.y, cx2, cy2, r2, angs2[0], angs2[1], angs2[2])) {
        return true;
      }
    }
    return false;
  }

  private static boolean chordIntersect(double[] a1, double[] a2) {
    // Simple line segment intersect for degenerate arcs
    org.locationtech.jts.geom.Coordinate p1 = new org.locationtech.jts.geom.Coordinate(a1[0], a1[1]);
    org.locationtech.jts.geom.Coordinate q1 = new org.locationtech.jts.geom.Coordinate(a1[4], a1[5]);
    org.locationtech.jts.geom.Coordinate p2 = new org.locationtech.jts.geom.Coordinate(a2[0], a2[1]);
    org.locationtech.jts.geom.Coordinate q2 = new org.locationtech.jts.geom.Coordinate(a2[4], a2[5]);
    org.locationtech.jts.algorithm.LineIntersector li = new org.locationtech.jts.algorithm.RobustLineIntersector();
    li.computeIntersection(p1, q1, p2, q2);
    if (!li.hasIntersection()) return false;
    // count proper (not endpoint only)
    for (int i=0; i<li.getIntersectionNum(); i++) {
      org.locationtech.jts.geom.Coordinate ip = li.getIntersection(i);
      if (!ip.equals2D(p1) && !ip.equals2D(q1) && !ip.equals2D(p2) && !ip.equals2D(q2)) return true;
    }
    return false;
  }

  private static boolean nearEndpoint(org.locationtech.jts.geom.Coordinate p, double[] arcPts) {
    double eps = 1e-9;
    if (Math.hypot(p.x - arcPts[0], p.y - arcPts[1]) < eps) return true;
    if (Math.hypot(p.x - arcPts[4], p.y - arcPts[5]) < eps) return true;
    return false;
  }

  /**
   * Returns whether this CircularString (viewed as a potential ring or path)
   * is simple: no proper self-intersections of its arcs.
   * For V-CP / V-CS.
   */
  public boolean isSimple() {
    CoordinateSequence cs = getCoordinateSequence();
    int nPts = cs.size();
    if (nPts < 3) return true;
    int nArcs = (nPts - 1) / 2;
    for (int i = 0; i < nArcs; i++) {
      int b = i * 2;
      double[] arc1 = new double[] {
        cs.getX(b), cs.getY(b), cs.getX(b+1), cs.getY(b+1), cs.getX(b+2), cs.getY(b+2)
      };
      for (int j = i + 2; j < nArcs; j++) {  // skip adjacent
        int bb = j * 2;
        double[] arc2 = new double[] {
          cs.getX(bb), cs.getY(bb), cs.getX(bb+1), cs.getY(bb+1), cs.getX(bb+2), cs.getY(bb+2)
        };
        if (arcsIntersectProper(arc1, arc2)) return false;
      }
    }
    // Additional detection for self-overlap / loop-back: repeated control points at non-adjacent positions
    // (e.g. the classic V-CS "loops back over itself" case revisits (0,0) interiorly).
    // This complements geometric arc cross for cases where arcs touch/revisit without proper interior cross.
    java.util.Set<String> seen = new java.util.HashSet<>();
    for (int i = 0; i < nPts; i++) {
      org.locationtech.jts.geom.Coordinate c = cs.getCoordinate(i);
      String key = roundForKey(c.x) + "," + roundForKey(c.y);
      if (seen.contains(key)) {
        // Repeated point: for open path, any interior repeat indicates improper overlap.
        // For closed rings (first==last), the final repeat is expected and checked separately in ring validation.
        if (i != 0 && i != nPts - 1) {
          return false;
        }
      }
      seen.add(key);
    }
    return true;
  }

  private static String roundForKey(double v) {
    // Stable key for point coincidence checks (tolerate tiny numeric noise in WKT parses)
    return String.format(java.util.Locale.ROOT, "%.9f", Math.round(v * 1e9) / 1e9);
  }
}
