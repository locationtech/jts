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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * An arc-aware analogue of {@code NodedSegmentString} for arc-aware noding (N-SS,
 * JTS #1195). It carries a control-point sequence read as consecutive arc pieces
 * {@code (p[2i], p[2i+1], p[2i+2])} (a collinear triple is a straight chord) plus
 * an opaque {@code data} context, records intersection nodes per arc piece, and
 * splits itself at those nodes into noded sub-strings.
 * <p>
 * Splitting is arc-aware: an arc cut at a crossing yields sub-arcs on the same
 * circle (each mid point recomputed at the sub-arc's angular midpoint), and a
 * sub-string spans the original arc joints (which are not nodes), breaking only
 * at the recorded crossings — the same rule as linear {@code NodedSegmentString}.
 * The crossings come from the oracle-pinned {@link CircularArcs} primitives.
 */
public final class ArcSegmentString {

  private static final double EPS = 1e-9;

  private final CoordinateSequence pts;
  private final Object data;
  private final Map<Integer, List<double[]>> nodes = new HashMap<Integer, List<double[]>>();

  public ArcSegmentString(CoordinateSequence pts, Object data) {
    this.pts = pts;
    this.data = data;
  }

  public CoordinateSequence getCoordinateSequence() { return pts; }
  public Object getData() { return data; }

  /** Number of arc pieces (consecutive control-point triples). */
  public int numArcs() { return Math.max(0, (pts.size() - 1) / 2); }

  /** The {@code i}-th arc piece as {sx,sy,mx,my,ex,ey}. */
  double[] arc(int i) {
    int b = 2 * i;
    return new double[]{ pts.getX(b), pts.getY(b), pts.getX(b+1), pts.getY(b+1), pts.getX(b+2), pts.getY(b+2) };
  }

  /**
   * Records an intersection node on arc piece {@code arcIndex}. A point coincident
   * with the arc's endpoints (a joint, not an interior crossing) or with an already
   * recorded node is ignored, so no zero-length sub-arc is produced.
   */
  void addNode(int arcIndex, double x, double y) {
    double[] a = arc(arcIndex);
    if (near(x, y, a[0], a[1]) || near(x, y, a[4], a[5])) return;     // at an endpoint joint
    List<double[]> list = nodes.get(arcIndex);
    if (list == null) { list = new ArrayList<double[]>(); nodes.put(arcIndex, list); }
    for (double[] q : list) if (near(x, y, q[0], q[1])) return;       // duplicate
    list.add(new double[]{ x, y });
  }

  /** True iff any node has been recorded on any arc. */
  boolean hasNodes() { return !nodes.isEmpty(); }

  /**
   * Splits this string at its recorded nodes into noded sub-strings, each carrying
   * the same {@code data}. With no nodes, returns this string unchanged.
   */
  public List<ArcSegmentString> getNodedSubstrings() {
    List<ArcSegmentString> out = new ArrayList<ArcSegmentString>();
    if (!hasNodes()) { out.add(this); return out; }
    List<Coordinate> cur = new ArrayList<Coordinate>();
    int nA = numArcs();
    for (int i = 0; i < nA; i++) {
      double[] a = arc(i);
      double[] c = circle(a);                                        // null if collinear (chord)
      List<double[]> breaks = breakPoints(a, c, nodes.get(i));
      for (int j = 0; j + 1 < breaks.size(); j++) {
        double[] p = breaks.get(j), q = breaks.get(j + 1);
        double mx, my;
        if (c != null) { double[] m = arcMid(c, p, q); mx = m[0]; my = m[1]; }
        else { mx = 0.5 * (p[0] + q[0]); my = 0.5 * (p[1] + q[1]); }
        if (cur.isEmpty()) cur.add(new Coordinate(p[0], p[1]));
        cur.add(new Coordinate(mx, my));
        cur.add(new Coordinate(q[0], q[1]));
        boolean endIsNode = (j + 1 < breaks.size() - 1);             // q is an interior crossing
        if (endIsNode) { out.add(build(cur)); cur = new ArrayList<Coordinate>(); }
      }
    }
    if (!cur.isEmpty()) out.add(build(cur));
    return out;
  }

  /** Ordered break points along one arc: start, interior nodes (by sweep), end. */
  private static List<double[]> breakPoints(double[] a, double[] c, List<double[]> ns) {
    List<double[]> bp = new ArrayList<double[]>();
    bp.add(new double[]{ a[0], a[1] });
    if (ns != null && !ns.isEmpty()) {
      List<double[]> sorted = new ArrayList<double[]>(ns);
      if (c != null) {
        final double[] cc = c;
        sorted.sort(new Comparator<double[]>() {
          public int compare(double[] p, double[] q) {
            return Double.compare(sweepFrac(cc, p), sweepFrac(cc, q));
          }
        });
      } else {
        final double[] aa = a;
        sorted.sort(new Comparator<double[]>() {
          public int compare(double[] p, double[] q) {
            return Double.compare(d2(aa[0],aa[1],p[0],p[1]), d2(aa[0],aa[1],q[0],q[1]));
          }
        });
      }
      bp.addAll(sorted);
    }
    bp.add(new double[]{ a[4], a[5] });
    return bp;
  }

  private ArcSegmentString build(List<Coordinate> coords) {
    return new ArcSegmentString(new CoordinateArraySequence(coords.toArray(new Coordinate[0])), data);
  }

  // ---- intersection of two arc/chord pieces (delegates to the oracle-pinned primitives) ----

  static double[][] intersectPieces(double[] a, double[] b) {
    boolean aArc = isArc(a), bArc = isArc(b);
    if (aArc && bArc) {
      return CircularArcs.intersectArc(a[0],a[1],a[2],a[3],a[4],a[5], b[0],b[1],b[2],b[3],b[4],b[5]);
    }
    if (aArc) return CircularArcs.intersectSegment(a[0],a[1],a[2],a[3],a[4],a[5], b[0],b[1], b[4],b[5]);
    if (bArc) return CircularArcs.intersectSegment(b[0],b[1],b[2],b[3],b[4],b[5], a[0],a[1], a[4],a[5]);
    double[] p = segSeg(a[0],a[1],a[4],a[5], b[0],b[1],b[4],b[5]);
    return (p == null) ? new double[0][] : new double[][]{ p };
  }

  // ---- geometry helpers ----

  static boolean isArc(double[] p) {
    return 2 * (p[0]*(p[3]-p[5]) + p[2]*(p[5]-p[1]) + p[4]*(p[1]-p[3])) != 0.0;
  }

  /**
   * Mid control point for the sub-arc of {@code arc} from {@code (px,py)} to
   * {@code (qx,qy)} (both on the arc), recomputed on the arc's circle at the
   * sub-arc's angular midpoint. Collinear (chord) pieces return the chord midpoint.
   */
  static double[] midOnArc(double[] arc, double px, double py, double qx, double qy) {
    double[] c = circle(arc);
    if (c == null) return new double[]{ 0.5*(px+qx), 0.5*(py+qy) };
    return arcMid(c, new double[]{ px, py }, new double[]{ qx, qy });
  }

  /** {cx, cy, r, a0, signedSweep} of the arc, or null if collinear/degenerate. */
  private static double[] circle(double[] p) {
    double sx=p[0],sy=p[1],mx=p[2],my=p[3],ex=p[4],ey=p[5];
    double d = 2 * (sx*(my-ey) + mx*(ey-sy) + ex*(sy-my));
    if (d == 0.0) return null;
    double s2=sx*sx+sy*sy, m2=mx*mx+my*my, e2=ex*ex+ey*ey;
    double cx=(s2*(my-ey)+m2*(ey-sy)+e2*(sy-my))/d;
    double cy=(s2*(ex-mx)+m2*(sx-ex)+e2*(mx-sx))/d;
    double r=Math.hypot(sx-cx,sy-cy);
    if (!Double.isFinite(r) || r==0.0) return null;
    double a0=Math.atan2(sy-cy,sx-cx), am=Math.atan2(my-cy,mx-cx), ae=Math.atan2(ey-cy,ex-cx);
    boolean ccw = d > 0;
    double theta = directedSweep(a0,am,ccw) + directedSweep(am,ae,ccw);
    return new double[]{ cx, cy, r, a0, ccw ? theta : -theta };
  }

  private static double sweepFrac(double[] c, double[] p) {
    boolean ccw = c[4] >= 0;
    return directedSweep(c[3], Math.atan2(p[1]-c[1], p[0]-c[0]), ccw);
  }

  /** Point on the circle at the angular midpoint of the sub-arc from p to q (in the arc's direction). */
  private static double[] arcMid(double[] c, double[] p, double[] q) {
    boolean ccw = c[4] >= 0;
    double ap = Math.atan2(p[1]-c[1], p[0]-c[0]);
    double sw = directedSweep(ap, Math.atan2(q[1]-c[1], q[0]-c[0]), ccw);
    double mid = ap + (ccw ? 1 : -1) * sw / 2;
    return new double[]{ c[0] + c[2]*Math.cos(mid), c[1] + c[2]*Math.sin(mid) };
  }

  private static double directedSweep(double from, double to, boolean ccw) {
    double t = ccw ? (to - from) : (from - to);
    t %= 2 * Math.PI;
    if (t < 0) t += 2 * Math.PI;
    return t;
  }

  private static double[] segSeg(double x1,double y1,double x2,double y2, double x3,double y3,double x4,double y4) {
    double d = (x2-x1)*(y4-y3) - (y2-y1)*(x4-x3);
    if (Math.abs(d) < 1e-12) return null;
    double t = ((x3-x1)*(y4-y3) - (y3-y1)*(x4-x3)) / d;
    double u = ((x3-x1)*(y2-y1) - (y3-y1)*(x2-x1)) / d;
    if (t < -1e-9 || t > 1+1e-9 || u < -1e-9 || u > 1+1e-9) return null;
    return new double[]{ x1 + t*(x2-x1), y1 + t*(y2-y1) };
  }

  private static boolean near(double x1,double y1,double x2,double y2) { return Math.hypot(x1-x2,y1-y2) <= EPS; }
  private static double d2(double x1,double y1,double x2,double y2) { double dx=x1-x2,dy=y1-y2; return dx*dx+dy*dy; }
}
