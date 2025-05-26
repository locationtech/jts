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

package test.jts.perf.operation.buffer;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.overlayng.UnaryUnionNG;

/**
 * Stress tests DepthSegment to determine if the compare contract is maintained.
 * Generates sets of random non-crossing segments, extracts lists 
 * of segments along stabbing lines, and checks the DepthSegment compare
 * method for if it is antisymmetric and transitive.
 * 
 * @author Martin Davis
 *
 */
public class DepthSegmentStressTest
{
  
  private static final int SEG_FIELD_SIZE = 1000;
  private static final int NUM_RUNS = 1000;
  private static final int NUM_RAND_SEGS = 200;

  public static void main(String args[]) {
    (new DepthSegmentStressTest()).run();
  }

  private static GeometryFactory geomFact = new GeometryFactory();
  private List<DepthSegment> segSet;
  private int iter = 0;
  
  public DepthSegmentStressTest()
  {
  }

  private void run() {
    for (int i = 0; i < NUM_RUNS; i++) {
      System.out.println("Run # " + iter++);
      runCompare(NUM_RAND_SEGS);
    }
  }  
  
  public void runCompare(int numRandomSegs)
  {
    segSet = createNodedSegments(numRandomSegs);
    
    for (int i = 0; i < SEG_FIELD_SIZE; i += 10) {
      double queryYOrd = i;
      List<DepthSegment> result = query(segSet, queryYOrd);
      
      
      checkTriplets(result);
    }
  }

  private void checkTriplets(List<DepthSegment> result) {
    for (int i1 = 0; i1 < result.size(); i1++) {
      for (int i2 = i1 + 1; i2 < result.size(); i2++) {
        for (int i3 = i2 + 1; i3 < result.size(); i3++) {
          checkCompareContract(result.get(i1), result.get(i2), result.get(i3));
        }
      }
    }
  }
  
  public void checkCompareContract(DepthSegment seg1, DepthSegment seg2, DepthSegment seg3)
  {
    if (! isSymmetric(seg1, seg2)) {
      isSymmetric(seg1, seg2);
      reportError("Symmetric", seg1, seg2);
    }
    if (! isTransitive(seg1, seg2, seg3)) {
      reportError("Transitive", seg1, seg2);
    }   
  }
  
  private void reportError(String validityCheck, 
      DepthSegment seg1, DepthSegment seg2) {
    System.out.println(validityCheck + " FAILS: " + seg1 + " / " + seg2);
    throw new RuntimeException(validityCheck + " FAILS!");
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
    if (cmp12 == cmp23 && cmp12 != cmp13) {
      System.out.println("Transitive FAIL: " + seg1 + " " + seg2 + " " + seg3);
      return false;
    }
    return true;
  }
  
  //----------------------------------------------------------------
  
  static List<DepthSegment> query(List<DepthSegment> segs, double y) {
    List<DepthSegment> result = new ArrayList<DepthSegment>();
    for (DepthSegment ds : segs) {
      if (y >= ds.minY() && y <= ds.maxY()) 
        result.add(ds);
    }
    return result;
  }
  
  static List<DepthSegment> createNodedSegments(int nSegs) {
    List<DepthSegment> segList = new ArrayList<DepthSegment>();
    Geometry segs = createRandomSegments(nSegs);
    Geometry nodedSegs = UnaryUnionNG.union(segs, new PrecisionModel(1));
    //System.out.println(nodedSegs);
    
    
    for (int i = 0; i < nodedSegs.getNumGeometries(); i++) {
      LineString line = (LineString) nodedSegs.getGeometryN(i);
      Coordinate p0 = line.getCoordinateN(0);
      Coordinate p1 = line.getCoordinateN(1);
      DepthSegment seg = DepthSegment.create(p0, p1);
      segList.add(seg);
    }
    return segList;
  }
  
  static int randint(int max) {
    return (int) (max * Math.random());
  }
  
  static Geometry createRandomSegments(int nSegs) {
    List<Geometry> lines = new ArrayList<Geometry>();

    for (int i = 0; i < nSegs; i++) {
      double x0 = randint(SEG_FIELD_SIZE);
      double y0 = randint(SEG_FIELD_SIZE);
      double x1 = randint(SEG_FIELD_SIZE);
      double y1 = randint(SEG_FIELD_SIZE);
      lines.add(geomFact.createLineString(new Coordinate[] {
          new Coordinate(x0, y0), new Coordinate(x1, y1) }));
    }
    return geomFact.buildGeometry(lines);
  }

  static double round(double x, double scale)
  {
    return Math.round(x * scale) / scale;
  }
}

/**
 * A segment from a directed edge which has been assigned a depth value
 * for its sides.
 * 
 * A copy of ubgraphDepthLocater.DepthSegment, which is private.
 */
class DepthSegment
    implements Comparable
{
  public static DepthSegment create(Coordinate p0, Coordinate p1) {
    Coordinate lo = p0;
    Coordinate hi = p1;
    if (p0.getY() > p1.getY()) {
      lo = p1;
      hi = p0;
    }
    LineSegment seg = new LineSegment(lo, hi);
    return new DepthSegment(seg, 0);
  }
  
  private LineSegment upwardSeg;
  private int leftDepth;

  public DepthSegment(LineSegment seg, int depth)
  {
    // input seg is assumed to be upward
    upwardSeg = new LineSegment(seg);
    this.leftDepth = depth;
  }
  
  public double maxY() {
    return upwardSeg.maxY();
  }

  public double minY() {
    return upwardSeg.minY();
  }
  
  /**
   * A comparison operation
   * which orders segments left to right
   * along some horizontal line.
   * If segments don't touch the same line, 
   * or touch at the same point,
   * they are compared in their Y extent.
   * 
   * <p>
   * The definition of the ordering is:
   * <ul>
   * <li>-1 : if DS1.seg is left of or below DS2.seg (DS1 < DS2)
   * <li>1 : if  DS1.seg is right of or above DS2.seg (DS1 > DS2) 
   * <li>0 : if the segments are identical 
   * </ul>
   * 
   * @param obj a DepthSegment
   * @return the comparison value
   */
  public int compareTo(Object obj)
  {
    LineSegment otherSeg = ((DepthSegment) obj).upwardSeg;
    
    /**
     * If segments are disjoint in X, X values provides ordering.
     */
    if (upwardSeg.minX() > otherSeg.maxX())
      return 1;
    if (upwardSeg.maxX() < otherSeg.minX())
      return -1;
    /**
     * The segments Y ranges should intersect, since they intersect same stabbing line.
     * But check for disjoint in Y and provide a result based on Y ordering
     */
    if (upwardSeg.minY() > otherSeg.maxY())
      return 1;
    if (upwardSeg.maxY() < otherSeg.minY())
      return -1;
    
    /**
     * Check if any segment point is left or right
     * of the other segment in its Y extent.
     */
    int comp00 = comparePointInSegYExtent(upwardSeg.p0, otherSeg);
    if (comp00 != 0) return comp00;
    int comp01 = comparePointInSegYExtent(upwardSeg.p1, otherSeg);
    if (comp01 != 0) return comp01;
    int comp10 = -comparePointInSegYExtent(otherSeg.p0, upwardSeg);
    if (comp10 != 0) return comp10;
    int comp11 = -comparePointInSegYExtent(otherSeg.p1, upwardSeg);
    if (comp11 != 0) return comp11;
    
    /**
     * If point checks in Y range are indeterminate,
     * segments (probably?) touch at a point 
     * and lie above and below that point,
     * or on same line
     */
    if (upwardSeg.maxY() > otherSeg.maxY())
      return 1;
    if (upwardSeg.maxY() < otherSeg.maxY())
      return -1;
    
    //-- check for both horizontal
    if (upwardSeg.isHorizontal() && otherSeg.isHorizontal()) {
      if (upwardSeg.minX() < otherSeg.minX())
        return -1;
      if (upwardSeg.minX() > otherSeg.minX())
        return 1;
    }
     
    // assert: segments are equal
    return 0;
  }
  
  private int comparePointInSegYExtent(Coordinate p, LineSegment seg) {
    if (p.y >= seg.minY() && p.y <= seg.maxY()) {
      //-- flip sign, since orientation and order relation are opposite
      int orient = seg.orientationIndex(p);
      switch (orient) {
      case Orientation.LEFT: return -1;
      case Orientation.RIGHT: return 1;
      }
      //-- collinear, so indeterminate
      return 0;
    }
    //-- not computable
    return 0;
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

  public String toString()
  {
    return upwardSeg.toString();
  }

}

