/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.buffer;

import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.operation.buffer.SubgraphDepthLocater.DepthSegment;

import junit.framework.TestCase;



/**
 * @version 1.7
 */
public class DepthSegmentTest extends TestCase {

  public DepthSegmentTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(DepthSegmentTest.class);
  }

  public void testCompareTipToTail() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(0.7, 0.2, 1.4, 0.9);
    SubgraphDepthLocater.DepthSegment ds1 = depthSeg(0.7, 0.2, 0.3, 1.1);
    checkCompare(ds0, ds1, 1);
  }

  public void testCompare2() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(0.5, 1.0, 0.1, 1.9);
    SubgraphDepthLocater.DepthSegment ds1 = depthSeg(1.0, 0.9, 1.9, 1.4);
    checkCompare(ds0, ds1, -1);
  }

  public void testCompareVertical() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(1, 1, 1, 2);
    SubgraphDepthLocater.DepthSegment ds1 = depthSeg(1, 0, 1, 1);
    checkCompare(ds0, ds1, 1);
  }

  public void testCompareHorizontal() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(1, 1, 1, 1);
    SubgraphDepthLocater.DepthSegment ds1 = depthSeg(0, 1, 1, 1);
    checkCompare(ds0, ds1, 1);
  }

  public void testCompareSameMinX() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(24.0, 96.0,    24.0,   99.0);
    SubgraphDepthLocater.DepthSegment ds1 = depthSeg(24.0, 95.239,  24.816, 99.0);
    checkCompare(ds0, ds1, -1);
  }
  
  public void testCompareOrientBug() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(146.268, -8.42361, 146.263, -8.3875);
    SubgraphDepthLocater.DepthSegment ds1 = depthSeg(146.269, -8.42889, 146.268, -8.42361);
    checkCompare(ds0, ds1, 1);
  }
  
  public void testCompareTouchingAndRight() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(31, 20, 41, 29);
    SubgraphDepthLocater.DepthSegment ds1 = depthSeg(43, 17, 31, 20);
    checkCompare(ds0, ds1, 1);
  }
  
  public void testCompareTouchingAndLeft() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(806, 480, 804, 482);
    SubgraphDepthLocater.DepthSegment ds1 = depthSeg(804, 479, 806, 480);
    checkCompare(ds0, ds1, 1);
  }
  
  public void testCompareEqual() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(1, 1, 2, 2);
    checkCompare(ds0, ds0, 0);
  }
  
  public void testTransitiveHorizontal() {
    checkTransitive(depthSeg(590, 320, 589, 320),
        depthSeg(589, 320, 575, 332),
        depthSeg(582, 320, 589, 320));
  }
  
  public void testTransitiveLeftTwoUp() {
    checkTransitive(depthSeg(930, 570, 921, 602),
        depthSeg(930, 570, 922, 573),
        depthSeg(922, 557, 930, 570));
  }
  
  private void checkTransitive(DepthSegment dsA, DepthSegment dsB, DepthSegment dsC) {
    assertTrue(dsA.isUpward());
    assertTrue(dsB.isUpward());
    assertTrue(dsC.isUpward());

    int compAB = dsA.compareTo(dsB);
    int compBC = dsB.compareTo(dsC);
    int compAC = dsA.compareTo(dsC);
    
    assertEquals("BC not equal to AB", compAB, compBC);
    assertEquals("Comparison is not transitive", compAB, compAC);
  }

  private void checkCompare(
     SubgraphDepthLocater.DepthSegment ds0,
     SubgraphDepthLocater.DepthSegment ds1, 
     int expectedComp) 
  {
    assertTrue(ds0.isUpward());
    assertTrue(ds1.isUpward());
    
    // check compareTo contract - should never have ds1 < ds2 && ds2 < ds1
    int comp0 = ds0.compareTo(ds1);
    int comp1 = ds1.compareTo(ds0);
    assertEquals("Comparator result", expectedComp, comp0);
    assertTrue("Symmetric check", comp0 == -comp1);
  }

  private SubgraphDepthLocater.DepthSegment depthSeg(double x0, double y0, double x1, double y1) {
    LineSegment seg = new LineSegment(x0,y0,x1,y1);
    // DepthSegment compareTo method assumes upward segments
    if (seg.p0.y > seg.p1.y)
      seg.reverse();
    return new SubgraphDepthLocater.DepthSegment(seg, 0);  
  }

}
