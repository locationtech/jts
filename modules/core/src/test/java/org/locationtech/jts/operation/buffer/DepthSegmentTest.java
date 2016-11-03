
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
package org.locationtech.jts.operation.buffer;

import org.locationtech.jts.geom.LineSegment;

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

  public void testContractTipToTail() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(0.7, 0.2, 1.4, 0.9);
    SubgraphDepthLocater.DepthSegment ds1 = depthSeg(0.3, 1.1, 0.7, 0.2);
    checkContract(ds0, ds1);
  }

  public void testContract2() throws Exception
  {
    SubgraphDepthLocater.DepthSegment ds0 = depthSeg(0.1, 1.9, 0.5, 1.0);
    SubgraphDepthLocater.DepthSegment ds1 = depthSeg(1.0, 0.9, 1.9, 1.4);
    checkContract(ds0, ds1);
  }

  private void checkContract(
     SubgraphDepthLocater.DepthSegment ds0,
     SubgraphDepthLocater.DepthSegment ds1) {
    // should never have ds1 < ds2 && ds2 < ds1
    int cmp0 = ds0.compareTo(ds1);
    int cmp1 = ds1.compareTo(ds0);
    boolean isFail = cmp0 != 0 && cmp0 == cmp1;
    assertTrue(! isFail);
  }

  private SubgraphDepthLocater.DepthSegment depthSeg(double x0, double y0, double x1, double y1) {
    return new SubgraphDepthLocater.DepthSegment(new LineSegment(x0,y0,x1,y1), 0);
  }

}
