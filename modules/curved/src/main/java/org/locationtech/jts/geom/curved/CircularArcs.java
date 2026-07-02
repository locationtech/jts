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

/**
 * Analytical helpers for single circular arcs defined by three control points
 * (start, mid, end), per the SQL/MM CIRCULARSTRING model.
 */
final class CircularArcs {

  private CircularArcs() {}

  /**
   * Length of the circular arc through the three control points, i.e.
   * {@code r * theta}. The mid point disambiguates which of the two arcs through
   * the endpoints is meant, so the result is correct for arcs up to a full turn.
   * Collinear (or otherwise degenerate) triples fall back to the chord length
   * {@code |end - start|}, matching the limiting behaviour as the radius grows.
   */
  static double arcLength(double sx, double sy, double mx, double my, double ex, double ey) {
    double chord = Math.hypot(ex - sx, ey - sy);
    // 2 * signed area of (s, m, e); zero iff the three points are collinear.
    double d = 2 * (sx * (my - ey) + mx * (ey - sy) + ex * (sy - my));
    if (d == 0.0) return chord;

    double s2 = sx * sx + sy * sy;
    double m2 = mx * mx + my * my;
    double e2 = ex * ex + ey * ey;
    double cx = (s2 * (my - ey) + m2 * (ey - sy) + e2 * (sy - my)) / d;
    double cy = (s2 * (ex - mx) + m2 * (sx - ex) + e2 * (mx - sx)) / d;
    double r = Math.hypot(sx - cx, sy - cy);
    if (!Double.isFinite(r) || r == 0.0) return chord;

    // Central angle accumulated in the arc's rotational direction (CCW iff the
    // signed area d > 0), going start -> mid -> end. Each step is the positive
    // turn in that direction, so a sub-arc that sweeps more than pi is measured
    // the long way round (an unsigned angle-between-radii would wrongly take the
    // short way). The total is the true sweep, valid up to a full turn.
    double a0 = Math.atan2(sy - cy, sx - cx);
    double am = Math.atan2(my - cy, mx - cx);
    double ae = Math.atan2(ey - cy, ex - cx);
    boolean ccw = d > 0;
    double theta = directedSweep(a0, am, ccw) + directedSweep(am, ae, ccw);
    double len = r * theta;
    return Double.isFinite(len) ? len : chord;
  }

  /**
   * Intersection points of the circular arc through {@code (s, m, e)} with the
   * line segment {@code (p, q)} (N-AL, JTS #1195). Returns each {@code [x, y]}
   * lying on both the segment ({@code 0 <= t <= 1}) and the arc's swept span
   * (the directed sweep start->mid->end). Returns 0, 1, or 2 points; empty for a
   * tangent miss, a degenerate segment, or a collinear (non-circular) arc.
   */
  static double[][] intersectSegment(double sx, double sy, double mx, double my, double ex, double ey,
                                     double px, double py, double qx, double qy) {
    double d = 2 * (sx * (my - ey) + mx * (ey - sy) + ex * (sy - my));
    if (d == 0.0) return new double[0][];               // collinear arc: no circle
    double s2 = sx * sx + sy * sy, m2 = mx * mx + my * my, e2 = ex * ex + ey * ey;
    double cx = (s2 * (my - ey) + m2 * (ey - sy) + e2 * (sy - my)) / d;
    double cy = (s2 * (ex - mx) + m2 * (sx - ex) + e2 * (mx - sx)) / d;
    double r = Math.hypot(sx - cx, sy - cy);
    if (!Double.isFinite(r) || r == 0.0) return new double[0][];

    // segment X(t) = p + t*(q-p); solve |X - C|^2 = r^2
    double dx = qx - px, dy = qy - py;
    double a = dx * dx + dy * dy;
    if (a == 0.0) return new double[0][];               // degenerate segment
    double fx = px - cx, fy = py - cy;
    double bb = 2 * (fx * dx + fy * dy);
    double cc = fx * fx + fy * fy - r * r;
    double disc = bb * bb - 4 * a * cc;
    if (disc < 0) return new double[0][];               // line misses circle
    double sq = Math.sqrt(disc);
    double[] ts = (disc == 0.0) ? new double[]{ -bb / (2 * a) }
                                : new double[]{ (-bb - sq) / (2 * a), (-bb + sq) / (2 * a) };

    double a0 = Math.atan2(sy - cy, sx - cx);
    double am = Math.atan2(my - cy, mx - cx);
    double ae = Math.atan2(ey - cy, ex - cx);
    boolean ccw = d > 0;
    double theta = directedSweep(a0, am, ccw) + directedSweep(am, ae, ccw);

    final double EPS = 1e-9;
    double[][] out = new double[ts.length][];
    int n = 0;
    for (double t : ts) {
      if (t < -EPS || t > 1 + EPS) continue;            // off the segment
      double x = px + t * dx, y = py + t * dy;
      double sweep = directedSweep(a0, Math.atan2(y - cy, x - cx), ccw);
      // on the arc span iff 0 <= sweep <= theta (allow tiny wrap just before start)
      if (sweep <= theta + EPS || sweep >= 2 * Math.PI - EPS) {
        out[n++] = new double[]{ x, y };
      }
    }
    if (n == out.length) return out;
    double[][] trimmed = new double[n][];
    System.arraycopy(out, 0, trimmed, 0, n);
    return trimmed;
  }

  /**
   * Intersection points of the circular arc through {@code (sA, mA, eA)} with the
   * circular arc through {@code (sB, mB, eB)} (N-AA, JTS #1195). Returns each
   * {@code [x, y]} lying on both arcs' swept spans (each directed sweep
   * start-&gt;mid-&gt;end). Returns 0, 1, or 2 points; empty when the underlying
   * circles miss or are tangent off the spans, when either triple is collinear
   * (no circle), or when the crossings fall outside either span. Two arcs on the
   * same circle (concentric, including coincident) share a sub-arc rather than
   * isolated points and are reported as no intersections.
   */
  static double[][] intersectArc(double sax, double say, double max, double may, double eax, double eay,
                                 double sbx, double sby, double mbx, double mby, double ebx, double eby) {
    double dA = 2 * (sax * (may - eay) + max * (eay - say) + eax * (say - may));
    double dB = 2 * (sbx * (mby - eby) + mbx * (eby - sby) + ebx * (sby - mby));
    if (dA == 0.0 || dB == 0.0) return new double[0][];     // a collinear triple: no circle
    double a2 = sax * sax + say * say, b2 = max * max + may * may, c2 = eax * eax + eay * eay;
    double cax = (a2 * (may - eay) + b2 * (eay - say) + c2 * (say - may)) / dA;
    double cay = (a2 * (eax - max) + b2 * (sax - eax) + c2 * (max - sax)) / dA;
    double rA = Math.hypot(sax - cax, say - cay);
    double p2 = sbx * sbx + sby * sby, q2 = mbx * mbx + mby * mby, t2 = ebx * ebx + eby * eby;
    double cbx = (p2 * (mby - eby) + q2 * (eby - sby) + t2 * (sby - mby)) / dB;
    double cby = (p2 * (ebx - mbx) + q2 * (sbx - ebx) + t2 * (mbx - sbx)) / dB;
    double rB = Math.hypot(sbx - cbx, sby - cby);
    if (!Double.isFinite(rA) || !Double.isFinite(rB) || rA == 0.0 || rB == 0.0) return new double[0][];

    double dx = cbx - cax, dy = cby - cay;
    double dd = Math.hypot(dx, dy);
    final double EPS = 1e-9;
    if (dd == 0.0) return new double[0][];                  // concentric / coincident: no isolated points
    if (dd > rA + rB + EPS || dd < Math.abs(rA - rB) - EPS) return new double[0][];   // circles miss

    // radical line: |X-CA|^2 = rA^2, |X-CB|^2 = rB^2 -> X = mid +/- h * perp
    double a = (rA * rA - rB * rB + dd * dd) / (2 * dd);
    double h2 = rA * rA - a * a;
    double h = h2 > 0 ? Math.sqrt(h2) : 0.0;                // h2 ~ 0: tangent (single point)
    double mx = cax + a * dx / dd, my = cay + a * dy / dd;
    double[][] cand = (h == 0.0)
        ? new double[][]{ { mx - h * dy / dd, my + h * dx / dd } }
        : new double[][]{ { mx - h * dy / dd, my + h * dx / dd }, { mx + h * dy / dd, my - h * dx / dd } };

    double aa0 = Math.atan2(say - cay, sax - cax);
    double aam = Math.atan2(may - cay, max - cax);
    double aae = Math.atan2(eay - cay, eax - cax);
    boolean accw = dA > 0;
    double thetaA = directedSweep(aa0, aam, accw) + directedSweep(aam, aae, accw);
    double ba0 = Math.atan2(sby - cby, sbx - cbx);
    double bam = Math.atan2(mby - cby, mbx - cbx);
    double bae = Math.atan2(eby - cby, ebx - cbx);
    boolean bccw = dB > 0;
    double thetaB = directedSweep(ba0, bam, bccw) + directedSweep(bam, bae, bccw);

    double[][] out = new double[cand.length][];
    int n = 0;
    for (double[] pt : cand) {
      double swA = directedSweep(aa0, Math.atan2(pt[1] - cay, pt[0] - cax), accw);
      if (!(swA <= thetaA + EPS || swA >= 2 * Math.PI - EPS)) continue;
      double swB = directedSweep(ba0, Math.atan2(pt[1] - cby, pt[0] - cbx), bccw);
      if (!(swB <= thetaB + EPS || swB >= 2 * Math.PI - EPS)) continue;
      out[n++] = pt;
    }
    if (n == out.length) return out;
    double[][] trimmed = new double[n][];
    System.arraycopy(out, 0, trimmed, 0, n);
    return trimmed;
  }

  /** Positive angular turn from {@code from} to {@code to} in the given direction, in [0, 2*pi). */
  private static double directedSweep(double from, double to, boolean ccw) {
    double t = ccw ? (to - from) : (from - to);
    double twoPi = 2 * Math.PI;
    t %= twoPi;
    if (t < 0) t += twoPi;
    return t;
  }
}
