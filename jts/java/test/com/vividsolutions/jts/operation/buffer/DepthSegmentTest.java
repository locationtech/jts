
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.operation.buffer;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.PrecisionModel;


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
