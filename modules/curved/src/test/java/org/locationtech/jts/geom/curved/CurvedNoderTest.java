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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.noding.SimpleNoder;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * N-SS (#1195) — the CORE slice: arc strings node through the stock core
 * {@link SimpleNoder} + {@link CurvedSegmentIntersector}, and the core
 * {@code NodedSegmentString}/{@code SegmentNodeList} machinery (with two
 * non-behavioral edits) yields arc-preserving {@link CurvedSegmentString} split
 * substrings. Verified with geometric anchors, split reconstruction, and a
 * densified brute-force cross-check.
 */
public class CurvedNoderTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(CurvedNoderTest.class);
  }

  public CurvedNoderTest(String name) { super(name); }

  private static CurvedSegmentString curved(double... xy) {
    Coordinate[] p = new Coordinate[xy.length / 2];
    for (int i = 0; i < p.length; i++) p[i] = new Coordinate(xy[2*i], xy[2*i+1]);
    return new CurvedSegmentString(new CoordinateArraySequence(p), null);
  }

  private static Collection node(CurvedSegmentString... strings) {
    SimpleNoder n = new SimpleNoder();
    n.setSegmentIntersector(new CurvedSegmentIntersector());
    n.computeNodes(Arrays.asList((SegmentString[]) strings));
    return n.getNodedSubstrings();
  }

  // circle A (0,0) r5 right semicircle (open) (0,5)-(5,0)-(0,-5)
  private static CurvedSegmentString circAright() { return curved(0,5, 5,0, 0,-5); }
  // circle B (6,0) r5 left semicircle (open) (6,5)-(1,0)-(6,-5)
  private static CurvedSegmentString circBleft()  { return curved(6,5, 1,0, 6,-5); }

  /** Two open arcs of circles (0,0)r5 and (6,0)r5 node at (3,+/-4) through the core noder. */
  public void testTwoCrossingArcsThroughCoreNoder() {
    Collection subs = node(circAright(), circBleft());
    int fromA = 0, fromB = 0;
    for (Object o : subs) {
      assertTrue("split output is a CurvedSegmentString", o instanceof CurvedSegmentString);
      CurvedSegmentString s = (CurvedSegmentString) o;
      double[] a = s.arc(0);
      assertEquals("mid lies on its circle", 5.0,
          Math.hypot(a[2] - (onA(a) ? 0 : 6), a[3]), 1e-9);
      if (onA(a)) fromA++; else fromB++;
    }
    assertEquals(3, fromA);   // open arc + 2 interior nodes -> 3 sub-arcs
    assertEquals(3, fromB);
  }

  /** A semicircle crossed by a chord at two points splits into three sub-arcs reconstructing the whole. */
  public void testTwoNodesOnOneArcReconstructs() {
    CurvedSegmentString semi = curved(5,0, 0,5, -5,0);          // upper semicircle, centre 0, r5
    CurvedSegmentString chord = curved(-6,3, 0,3, 6,3);         // horizontal chord y=3 (collinear -> chord)
    List<CurvedSegmentString> arcs = new ArrayList<CurvedSegmentString>();
    for (Object o : node(semi, chord)) {
      CurvedSegmentString s = (CurvedSegmentString) o;
      if (onCircle(s.arc(0), 0, 0, 5)) arcs.add(s);
    }
    assertEquals(3, arcs.size());
    double total = 0;
    for (CurvedSegmentString s : arcs) {
      double[] a = s.arc(0);
      assertEquals("mid on circle", 5.0, Math.hypot(a[2], a[3]), 1e-9);
      total += CircularArcs.arcLength(a[0],a[1],a[2],a[3],a[4],a[5]);
    }
    assertEquals(Math.PI * 5, total, 1e-7);
  }

  /** Disjoint arcs are returned unchanged (one whole sub-arc each). */
  public void testDisjointUnchanged() {
    Collection subs = node(circAright(), curved(26,5, 21,0, 26,-5));   // circle (20,0) r5 far away
    assertEquals(2, subs.size());
    for (Object o : subs) {
      double[] a = ((CurvedSegmentString) o).arc(0);
      assertEquals("untouched arc has 1 piece", 1, ((CurvedSegmentString) o).numArcs());
    }
  }

  /** Node points (from the curved split endpoints) match an independent densified brute-force intersection. */
  public void testMatchesDensifiedIntersections() {
    List<double[]> ref = densifiedCrossings(circAright(), circBleft(), 3000);
    // collect interior split endpoints (the node coords) from circle A's substrings
    List<double[]> nodes = new ArrayList<double[]>();
    for (Object o : node(circAright(), circBleft())) {
      CurvedSegmentString s = (CurvedSegmentString) o;
      double[] a0 = s.arc(0);
      if (onCircle(a0, 0, 0, 5)) addNode(nodes, a0[0], a0[1]);   // sub-arc start
    }
    // every densified crossing must coincide with a recorded split endpoint
    for (double[] r : ref) {
      boolean matched = false;
      for (double[] nd : nodes) if (Math.hypot(nd[0]-r[0], nd[1]-r[1]) < 1e-2) { matched = true; break; }
      assertTrue("densified crossing (" + r[0] + "," + r[1] + ") is a split node", matched);
    }
    assertTrue("two crossings", ref.size() == 2);
  }

  // ---- helpers ----

  private static boolean onA(double[] arc) { return onCircle(arc, 0, 0, 5); }

  private static boolean onCircle(double[] arc, double cx, double cy, double r) {
    return Math.abs(Math.hypot(arc[2]-cx, arc[3]-cy) - r) < 1e-6;   // classify by mid point
  }

  private static void addNode(List<double[]> nodes, double x, double y) {
    for (double[] n : nodes) if (Math.hypot(n[0]-x, n[1]-y) < 1e-6) return;
    // skip the original arc endpoints (0,5),(0,-5) — only interior crossings are nodes
    if (Math.hypot(x-0, y-5) < 1e-6 || Math.hypot(x-0, y+5) < 1e-6) return;
    nodes.add(new double[]{ x, y });
  }

  private static List<double[]> densifiedCrossings(CurvedSegmentString a, CurvedSegmentString b, int nPerArc) {
    Coordinate[] pa = densify(a, nPerArc), pb = densify(b, nPerArc);
    List<double[]> out = new ArrayList<double[]>();
    for (int i = 0; i + 1 < pa.length; i++)
      for (int j = 0; j + 1 < pb.length; j++) {
        double[] x = seg(pa[i], pa[i+1], pb[j], pb[j+1]);
        if (x == null) continue;
        boolean dup = false;
        for (double[] o : out) if (Math.hypot(o[0]-x[0], o[1]-x[1]) < 1e-2) { dup = true; break; }
        if (!dup) out.add(x);
      }
    return out;
  }

  private static Coordinate[] densify(CurvedSegmentString s, int n) {
    CoordinateSequence seq = curvedSeq(s);
    List<Coordinate> out = new ArrayList<Coordinate>();
    for (int i = 0; i + 2 < seq.size(); i += 2) {
      double sx=seq.getX(i),sy=seq.getY(i),mx=seq.getX(i+1),my=seq.getY(i+1),ex=seq.getX(i+2),ey=seq.getY(i+2);
      double d=2*(sx*(my-ey)+mx*(ey-sy)+ex*(sy-my));
      double s2=sx*sx+sy*sy,m2=mx*mx+my*my,e2=ex*ex+ey*ey;
      double cx=(s2*(my-ey)+m2*(ey-sy)+e2*(sy-my))/d, cy=(s2*(ex-mx)+m2*(sx-ex)+e2*(mx-sx))/d;
      double r=Math.hypot(sx-cx,sy-cy);
      double a0=Math.atan2(sy-cy,sx-cx), am=Math.atan2(my-cy,mx-cx), ae=Math.atan2(ey-cy,ex-cx);
      boolean ccw=d>0; double th=sweep(a0,am,ccw)+sweep(am,ae,ccw); int dir=ccw?1:-1, start=(i==0)?0:1;
      for (int k=start;k<=n;k++){ double ang=a0+dir*th*k/n; out.add(new Coordinate(cx+r*Math.cos(ang),cy+r*Math.sin(ang))); }
    }
    return out.toArray(new Coordinate[0]);
  }

  /** Reconstruct the original control sequence (chord endpoints + mids) of a single-arc curved string. */
  private static CoordinateSequence curvedSeq(CurvedSegmentString s) {
    double[] a = s.arc(0);
    return new CoordinateArraySequence(new Coordinate[]{
        new Coordinate(a[0],a[1]), new Coordinate(a[2],a[3]), new Coordinate(a[4],a[5]) });
  }

  private static double sweep(double f,double t,boolean ccw){ double x=ccw?(t-f):(f-t); x%=2*Math.PI; if(x<0)x+=2*Math.PI; return x; }

  private static double[] seg(Coordinate p1, Coordinate p2, Coordinate p3, Coordinate p4) {
    double d=(p2.x-p1.x)*(p4.y-p3.y)-(p2.y-p1.y)*(p4.x-p3.x);
    if (Math.abs(d)<1e-15) return null;
    double t=((p3.x-p1.x)*(p4.y-p3.y)-(p3.y-p1.y)*(p4.x-p3.x))/d;
    double u=((p3.x-p1.x)*(p2.y-p1.y)-(p3.y-p1.y)*(p2.x-p1.x))/d;
    if (t<0||t>1||u<0||u>1) return null;
    return new double[]{ p1.x+t*(p2.x-p1.x), p1.y+t*(p2.y-p1.y) };
  }
}
