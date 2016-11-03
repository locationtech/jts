/*
 * Copyright (c) 2016 Martin Davis.
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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import junit.framework.TestCase;
import junit.textui.TestRunner;


public class RectangleLineIntersectorTest
extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(RectangleLineIntersectorTest.class);
  }

  public RectangleLineIntersectorTest(String name) { super(name); }

   public void test300Points() {
    RectangleLineIntersectorValidator test = new RectangleLineIntersectorValidator();
    test.init(300);
    assertTrue(test.validate());
  }
}

/**
 * Tests optimized RectangleLineIntersector against
 * a brute force approach (which is assumed to be correct).
 * 
 * @author Martin Davis
 *
 */
class RectangleLineIntersectorValidator
{
  private GeometryFactory geomFact = new GeometryFactory();
  
  private double baseX = 0;
  private double baseY = 0;
  private double rectSize = 100;
  private Envelope rectEnv;
  private Coordinate[] pts;
  private boolean isValid = true; 
    
  public RectangleLineIntersectorValidator()
  {
    
  }
  
  public void init(int nPts)
  {
    rectEnv = createRectangle();
    pts = createTestPoints(nPts);
    
  }
  
  public boolean validate()
  {
    run(true, true);
    return isValid;
  }
  
  public void run(boolean useSegInt, boolean useSideInt)
  {
    RectangleLineIntersector rectSegIntersector = new RectangleLineIntersector(rectEnv);
    SimpleRectangleIntersector rectSideIntersector = new SimpleRectangleIntersector(rectEnv);

    for (int i = 0; i < pts.length; i++) {
      for (int j = 0; j < pts.length; j++) {
        if (i == j) continue;
        
        boolean segResult = false;
        if (useSegInt)
          segResult = rectSegIntersector.intersects(pts[i], pts[j]);
        boolean sideResult = false;
        if (useSideInt)
          sideResult = rectSideIntersector.intersects(pts[i], pts[j]);
        
        if (useSegInt && useSideInt)
        {
          if (segResult != sideResult)
            isValid = false;
        }
      }
    }
  }
  
  private Coordinate[] createTestPoints(int nPts)
  {
    Point pt = geomFact.createPoint(new Coordinate(baseX, baseY));
    Geometry circle = pt.buffer(2 * rectSize, nPts/4);
    return circle.getCoordinates();
  }
  
  private Envelope createRectangle()
  {
     Envelope rectEnv = new Envelope(
        new Coordinate(baseX, baseY),
        new Coordinate(baseX + rectSize, baseY + rectSize));
     return rectEnv;
  }
  
}

class SimpleRectangleIntersector
{
  // for intersection testing, don't need to set precision model
  private LineIntersector li = new RobustLineIntersector();

  private Envelope rectEnv;
  /**
   * The corners of the rectangle, in the order:
   *  10
   *  23
   */
  private Coordinate[] corner = new Coordinate[4];

  public SimpleRectangleIntersector(Envelope rectEnv)
  {
    this.rectEnv = rectEnv;
    initCorners(rectEnv);
  }
  
  private void initCorners(Envelope rectEnv)
  {
    corner[0] = new Coordinate(rectEnv.getMaxX(), rectEnv.getMaxY());
    corner[1] = new Coordinate(rectEnv.getMinX(), rectEnv.getMaxY());
    corner[2] = new Coordinate(rectEnv.getMinX(), rectEnv.getMinY());
    corner[3] = new Coordinate(rectEnv.getMaxX(), rectEnv.getMinY());
  }
  
  public boolean intersects(Coordinate p0, Coordinate p1)
  {
    Envelope segEnv = new Envelope(p0, p1);
    if (! rectEnv.intersects(segEnv))
      return false;

    li.computeIntersection(p0, p1, corner[0], corner[1]);
    if (li.hasIntersection()) return true;
    li.computeIntersection(p0, p1, corner[1], corner[2]);
    if (li.hasIntersection()) return true;
    li.computeIntersection(p0, p1, corner[2], corner[3]);
    if (li.hasIntersection()) return true;
    li.computeIntersection(p0, p1, corner[3], corner[0]);
    if (li.hasIntersection()) return true;

    return false;
  }

}
