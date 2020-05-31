package org.locationtech.jts.noding.snap;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.NodingTestUtil;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class SnappingNoderTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(SnappingNoderTest.class);
  }

  public SnappingNoderTest(String name) { super(name); }
  
  public void testOverlappingLinesWithNearVertex() {
    String wkt1 = "LINESTRING (100 100, 300 100)";
    String wkt2 = "LINESTRING (200 100.1, 400 100)";
    String expected = "MULTILINESTRING ((100 100, 200 100.1), (200 100.1, 300 100), (200 100.1, 300 100), (300 100, 400 100))";
    checkRounding(wkt1, wkt2, 1, expected);
  }
  
  public void testSnappedVertex() {
    String wkt1 = "LINESTRING (100 100, 200 100, 300 100)";
    String wkt2 = "LINESTRING (200 100.3, 400 110)";
    String expected = "MULTILINESTRING ((100 100, 200 100), (200 100, 300 100), (200 100, 400 110))";
    checkRounding(wkt1, wkt2, 1, expected);
  }
  
  public void testSelfSnap() {
    String wkt1 = "LINESTRING (100 200, 100 100, 300 100, 200 99.3, 200 0)";
    String expected = "MULTILINESTRING ((100 200, 100 100, 200 99.3), (200 99.3, 300 100), (300 100, 200 99.3), (200 99.3, 200 0))";
    checkRounding(wkt1, null, 1, expected);
  }
  
  public void testLineDensePointsSelfSnap() {
    String wkt1 = "LINESTRING (1 1, 1.3 1, 1.6 1, 1.9 1, 2.2 1, 2.5 1, 2.8 1, 3.1 1, 3.5 1, 4 1)";
    String expected = "LINESTRING (1 1, 2.2 1, 3.5 1)";
    checkRounding(wkt1, null, 1, expected);
  }
  
  void checkRounding(String wkt1, String wkt2, double snapDist, String expectedWKT)
  {
    Geometry geom1 = read(wkt1);
    Geometry geom2 = null;
    if (wkt2 != null)
      geom2 = read(wkt2);
    
    Noder noder = new SnappingNoder(snapDist);
    Geometry result = NodingTestUtil.nodeValidated(geom1, geom2, noder);    
    
    // only check if expected was provided
    if (expectedWKT == null) return;
    Geometry expected = read(expectedWKT);
    checkEqual(expected, result);
  }


}
