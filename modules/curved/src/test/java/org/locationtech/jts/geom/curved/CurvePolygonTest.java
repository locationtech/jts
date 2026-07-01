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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link CurvePolygon} structural behaviour (F-CP review follow-ups):
 * the linear control-point view must stay consistent through {@link CurvePolygon#normalize()},
 * and a curved shell whose control polyline cannot form a ring must fail with a clear message.
 */
public class CurvePolygonTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(CurvePolygonTest.class);
  }

  public CurvePolygonTest(String name) { super(name); }

  private final CurvedGeometryFactory gf = new CurvedGeometryFactory();

  private CircularString circularString(double... xy) {
    Coordinate[] pts = new Coordinate[xy.length / 2];
    for (int i = 0; i < pts.length; i++)
      pts[i] = new Coordinate(xy[2 * i], xy[2 * i + 1]);
    return gf.createCircularString(new CoordinateArraySequence(pts));
  }

  /**
   * After normalize(), the legacy linear ring view (getExteriorRing) and the
   * linear geometry returned by toLinear(0) must agree -- both are the
   * control-point view, and normalize() must not desync them.
   */
  public void testNormalizeKeepsLinearViewConsistent() {
    // closed control-point ring that does NOT start at the minimum coordinate,
    // so normalize() scrolls (and possibly reverses) the inherited linear ring.
    CircularString shell = circularString(2,0, 1,1, 0,0, 1,-1, 2,0);
    CurvePolygon cp = gf.createCurvePolygon(shell);
    cp.normalize();

    Polygon linear = (Polygon) cp.toLinear(0.0);
    assertTrue("getExteriorRing() and toLinear(0) must agree after normalize()",
        cp.getExteriorRing().equalsExact(linear.getExteriorRing()));
  }

  /**
   * A curved shell whose control polyline has too few points to form a ring
   * (e.g. a full-circle CIRCULARSTRING with 3 control points) cannot be given a
   * linear ring view in this phase; construction must fail with a clear,
   * actionable message rather than a cryptic LinearRing error.
   */
  public void testCurvedShellTooFewControlPointsGivesClearError() {
    CircularString fullCircle = circularString(0,0, 2,0, 0,0); // 3 control points
    try {
      gf.createCurvePolygon(fullCircle);
      fail("expected IllegalArgumentException for a curved shell that cannot form a linear ring");
    }
    catch (IllegalArgumentException ex) {
      String m = ex.getMessage();
      assertTrue("message should explain the curved control-point ring limitation, was: " + m,
          m != null && m.toLowerCase().contains("control point"));
    }
  }
}
