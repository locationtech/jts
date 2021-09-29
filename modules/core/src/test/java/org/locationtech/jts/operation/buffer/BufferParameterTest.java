package org.locationtech.jts.operation.buffer;

import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

/**
 * Tests for the effect of buffer parameter values.
 * 
 * @author Martin Davis
 *
 */
public class BufferParameterTest extends GeometryTestCase {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(BufferParameterTest.class);
  }
  
  public BufferParameterTest(String name) {
    super(name);
  }
  
  public void testQuadSegsNeg() {
    checkBuffer("LINESTRING (20 20, 80 20, 80 80)", 
        10.0, -99, 
        "POLYGON ((70 30, 70 80, 80 90, 90 80, 90 20, 80 10, 20 10, 10 20, 20 30, 70 30))");
  }

  public void testQuadSegs0() {
    checkBuffer("LINESTRING (20 20, 80 20, 80 80)", 
        10.0, 0, 
        "POLYGON ((70 30, 70 80, 80 90, 90 80, 90 20, 80 10, 20 10, 10 20, 20 30, 70 30))");
  }

  public void testQuadSegs1() {
    checkBuffer("LINESTRING (20 20, 80 20, 80 80)", 
        10.0, 1, 
        "POLYGON ((70 30, 70 80, 80 90, 90 80, 90 20, 80 10, 20 10, 10 20, 20 30, 70 30))");
  }

  public void testQuadSegs2() {
    checkBuffer("LINESTRING (20 20, 80 20, 80 80)", 
        10.0, 2, 
        "POLYGON ((70 30, 70 80, 72.92893218813452 87.07106781186548, 80 90, 87.07106781186548 87.07106781186548, 90 80, 90 20, 87.07106781186548 12.928932188134524, 80 10, 20 10, 12.928932188134523 12.928932188134524, 10 20, 12.928932188134524 27.071067811865476, 20 30, 70 30))");
  }

  public void testQuadSegs2Bevel() {
    checkBuffer("LINESTRING (20 20, 80 20, 80 80)", 
        10.0, 2, BufferParameters.JOIN_BEVEL,
        "POLYGON ((70 30, 70 80, 72.92893218813452 87.07106781186548, 80 90, 87.07106781186548 87.07106781186548, 90 80, 90 20, 80 10, 20 10, 12.928932188134523 12.928932188134524, 10 20, 12.928932188134524 27.071067811865476, 20 30, 70 30))");
  }

  
  private void checkBuffer(String wkt, double dist, int quadSegs, String wktExpected) {
    checkBuffer( wkt, dist, quadSegs, BufferParameters.JOIN_ROUND, wktExpected);
  }
  
  private void checkBuffer(String wkt, double dist, int quadSegs, int joinStyle, String wktExpected) {
    BufferParameters param = new BufferParameters();
    param.setQuadrantSegments(quadSegs);
    param.setJoinStyle(joinStyle);
    Geometry geom = read(wkt);
    Geometry result = BufferOp.bufferOp(geom, dist, param);
    Geometry expected = read(wktExpected);
    checkEqual(expected, result);
  }
}
