/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.geom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Test spatial predicate optimizations for rectangles by
 * synthesizing an exhaustive set of test cases.
 *
 * @version 1.7
 */
public class RectanglePredicateSyntheticTest
     extends TestCase
{
  private WKTReader rdr = new WKTReader();
  private GeometryFactory fact = new GeometryFactory();

  double baseX  = 10;
  double baseY  = 10;
  double rectSize = 20;
  double bufSize = 10;
  double testGeomSize = 10;
  double bufferWidth = 1.0;

  Envelope rectEnv = new Envelope(baseX, baseX + rectSize, baseY, baseY + rectSize);
  Geometry rect = fact.toGeometry(rectEnv);

  public static void main(String args[]) {
    TestRunner.run(RectanglePredicateSyntheticTest.class);
  }

  public RectanglePredicateSyntheticTest(String name) { super(name); }

  public void testLines()
  {
    //System.out.println(rect);

    List testGeoms = getTestGeometries();
    for (Iterator i = testGeoms.iterator(); i.hasNext(); ) {
      Geometry testGeom = (Geometry) i.next();
      runRectanglePredicates(rect, testGeom);
    }
  }

  public void testDenseLines()
  {
    //System.out.println(rect);

    List testGeoms = getTestGeometries();
    for (Iterator i = testGeoms.iterator(); i.hasNext(); ) {
      Geometry testGeom = (Geometry) i.next();

      SegmentDensifier densifier = new SegmentDensifier((LineString) testGeom);
      LineString denseLine = (LineString) densifier.densify(testGeomSize / 400);

      runRectanglePredicates(rect, denseLine);
    }
  }

  public void testPolygons()
  {
    List testGeoms = getTestGeometries();
    for (Iterator i = testGeoms.iterator(); i.hasNext(); ) {
      Geometry testGeom = (Geometry) i.next();
      runRectanglePredicates(rect, testGeom.buffer(bufferWidth));
    }
  }

  private List getTestGeometries()
  {
    Envelope testEnv = new Envelope(rectEnv.getMinX() - bufSize, rectEnv.getMaxX() + bufSize,
                                    rectEnv.getMinY() - bufSize, rectEnv.getMaxY() + bufSize);
    List testGeoms = createTestGeometries(testEnv, 5, testGeomSize);
    return testGeoms;
  }

  private void runRectanglePredicates(Geometry rect, Geometry testGeom) {
    boolean intersectsValue = rect.intersects(testGeom);
    boolean relateIntersectsValue = rect.relate(testGeom).isIntersects();
    boolean intersectsOK = intersectsValue == relateIntersectsValue;

    boolean containsValue = rect.contains(testGeom);
    boolean relateContainsValue = rect.relate(testGeom).isContains();
    boolean containsOK = containsValue == relateContainsValue;

    //System.out.println(testGeom);
    if (! intersectsOK || ! containsOK) {
      System.out.println(testGeom);
    }
    assertTrue(intersectsOK);
    assertTrue(containsOK);
  }

  public List createTestGeometries(Envelope env, double inc, double size)
  {
    List testGeoms = new ArrayList();

    for (double y = env.getMinY(); y <= env.getMaxY(); y += inc) {
      for (double x = env.getMinX(); x <= env.getMaxX(); x += inc) {
        Coordinate base = new Coordinate(x, y);
        testGeoms.add(createAngle(base, size, 0));
        testGeoms.add(createAngle(base, size, 1));
        testGeoms.add(createAngle(base, size, 2));
        testGeoms.add(createAngle(base, size, 3));
      }
    }
    return testGeoms;
  }

  public Geometry createAngle(Coordinate base, double size, int quadrant)
  {
    int[][] factor = {
      { 1, 0 },
      { 0, 1 },
      { -1, 0 },
      { 0, -1 } };

    int xFac = factor[quadrant][0];
    int yFac = factor[quadrant][1];

    Coordinate p0 = new Coordinate(base.x + xFac * size, base.y + yFac * size);
    Coordinate p2 = new Coordinate(base.x + yFac * size, base.y + (- xFac) * size);

    return fact.createLineString(new Coordinate[] { p0, base, p2 } );
  }
}