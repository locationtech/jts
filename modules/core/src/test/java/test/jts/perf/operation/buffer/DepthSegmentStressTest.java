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

package test.jts.perf.operation.buffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.locationtech.jts.geom.LineSegment;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

/**
 * Stress tests {@link DepthSegment} to determine if the compare contract is maintained.
 * 
 * @author Martin Davis
 *
 */
public class DepthSegmentStressTest
extends PerformanceTestCase
{

  public static void main(String args[]) {
    PerformanceTestRunner.run(DepthSegmentStressTest.class);
  }

  public DepthSegmentStressTest(String name)
  {
    super(name);
    setRunSize(new int[] {20});
    setRunIterations(100);
  }

  public void startRun(int size)
  {
    System.out.println("Running with size " + size);
    iter = 0;
  }
  
  private int iter = 0;
  
  public void XXrunSort()
  {
    System.out.println("Iter # " + iter++);
    // do test work here
    List segs = createRandomDepthSegments(100);
    Collections.sort(segs);
  }
  
  public void runMin()
  {
    System.out.println("Iter # " + iter++);
    // do test work here
    List segs = createRandomDepthSegments(100);
    Collections.min(segs);
  }
  
  public void runCompare()
  {
    System.out.println("Iter # " + iter++);
    DepthSegment seg1 = createRandomDepthSegment();
    DepthSegment seg2 = createRandomDepthSegment();
    DepthSegment seg3 = createRandomDepthSegment();
    
    // do test work here
    boolean fails = false;
    if (! isSymmetric(seg1, seg2))
      fails = true;
    if (! isTransitive(seg1, seg2, seg3))
      fails = true;
    
    if (fails ){
      System.out.println("FAILS!");
      throw new RuntimeException("FAILS!");
    }
      
  }
  
  public boolean isSymmetric(DepthSegment seg1, DepthSegment seg2) {
    int cmp12 = seg1.compareTo(seg2);
    int cmp21 = seg2.compareTo(seg1);
    return cmp12 == -cmp21;
  }
  public boolean isTransitive(DepthSegment seg1, DepthSegment seg2, DepthSegment seg3) {
    int cmp12 = seg1.compareTo(seg2);
    int cmp23 = seg2.compareTo(seg3);
    int cmp13 = seg1.compareTo(seg3);
    if (cmp12 > 0 && cmp23 > 0) {
      if (cmp13 <= 0) {
        System.out.println(seg1 + " " + seg2 + " " + seg3);
        return false;
      }
    }
    return true;
  }
  
  public List createRandomDepthSegments(int n)
  {
    List segs = new ArrayList();
    for (int i = 0; i < n; i++) {
      segs.add(createRandomDepthSegment());
    }
    return segs;
  }
  
  int randint(int max) {
    return (int) (max * Math.random());
  }
  
  private DepthSegment createRandomDepthSegment() {
    double scale = 10;
    int max = 10;
    double x0 = randint(max);
    double y0 = randint(max);
    double ang = 2 * Math.PI * Math.random();
    double x1 = Math.rint(x0 + max * Math.cos(ang));
    double y1 = Math.rint(y0 + max * Math.sin(ang));
    LineSegment seg = new LineSegment(x0,y0,x1,y1);
    seg.normalize();
    return new DepthSegment(seg, 0);
  }

  double round(double x, double scale)
  {
    return Math.round(x * scale) / scale;
  }

  /**
   * A segment from a directed edge which has been assigned a depth value
   * for its sides.
   */
  private class DepthSegment
      implements Comparable
  {
    private LineSegment upwardSeg;
    private int leftDepth;

    public DepthSegment(LineSegment seg, int depth)
    {
      // input seg is assumed to be normalized
      upwardSeg = new LineSegment(seg);
      //upwardSeg.normalize();
      this.leftDepth = depth;
    }
    /**
     * Defines a comparision operation on DepthSegments
     * which orders them left to right
     *
     * <pre>
     * DS1 < DS2   if   DS1.seg is left of DS2.seg
     * DS1 > DS2   if   DS1.seg is right of DS2.seg
     * </pre>
     *
     * @param obj
     * @return the comparison value
     */
    public int compareTo(Object obj)
    {
      DepthSegment other = (DepthSegment) obj;
      
      if (! envelopesOverlap(upwardSeg, other.upwardSeg)) 
        return upwardSeg.compareTo(other.upwardSeg);
      // check orientations
      int orientIndex = upwardSeg.orientationIndex(other.upwardSeg);
      if (orientIndex != 0) return orientIndex;
      orientIndex = - other.upwardSeg.orientationIndex(upwardSeg);
      if (orientIndex != 0) return orientIndex;
      // segments cross or are collinear.  Use segment ordering
      return upwardSeg.compareTo(other.upwardSeg);

    }

    public int XcompareTo(Object obj)
    {
      DepthSegment other = (DepthSegment) obj;
      
      // if segments are collinear and vertical compare endpoints
      if (isVertical() && other.isVertical() 
          && upwardSeg.p0.x == other.upwardSeg.p0.x)
        return compareX(this.upwardSeg, other.upwardSeg);
      // check if segments are trivially ordered along X
      if (upwardSeg.maxX() <= other.upwardSeg.minX()) return -1;
      if (upwardSeg.minX() >= other.upwardSeg.maxX()) return 1;
      /**
       * try and compute a determinate orientation for the segments.
       * Test returns 1 if other is left of this (i.e. this > other)
       */
      int orientIndex = upwardSeg.orientationIndex(other.upwardSeg);
      // if orientation is determinate, return it
      if (orientIndex != 0)
        return orientIndex;

      /**
       * If comparison between this and other is indeterminate,
       * try the opposite call order.
       * orientationIndex value is 1 if this is left of other,
       * so have to flip sign to get proper comparison value of
       * -1 if this is leftmost
       */
      if (orientIndex == 0)
        orientIndex = -1 * other.upwardSeg.orientationIndex(upwardSeg);

      // if orientation is determinate, return it
      if (orientIndex != 0)
        return orientIndex;

      // otherwise, segs must be collinear - sort based on minimum X value
      return compareX(this.upwardSeg, other.upwardSeg);
    }

    public boolean isVertical() {
      return upwardSeg.p0.x == upwardSeg.p1.x;
    }
    
    public boolean envelopesOverlap(LineSegment seg1, LineSegment seg2) {
      if (seg1.maxX() <= seg2.minX()) return false;
      if (seg2.maxX() <= seg1.minX()) return false;
      if (seg1.maxY() <= seg2.minY()) return false;
      if (seg2.maxY() <= seg1.minY()) return false;
      return true;
    }
    /**
     * Compare two collinear segments for left-most ordering.
     * If segs are vertical, use vertical ordering for comparison.
     * If segs are equal, return 0.
     * Segments are assumed to be directed so that the second coordinate is >= to the first
     * (e.g. up and to the right).
     *
     * @param seg0 a segment to compare
     * @param seg1 a segment to compare
     * @return
     */
    private int compareX(LineSegment seg0, LineSegment seg1)
    {
      int compare0 = seg0.p0.compareTo(seg1.p0);
      if (compare0 != 0)
        return compare0;
      return seg0.p1.compareTo(seg1.p1);

    }
    public String toString()
    {
      return upwardSeg.toString();
    }

  }

}
