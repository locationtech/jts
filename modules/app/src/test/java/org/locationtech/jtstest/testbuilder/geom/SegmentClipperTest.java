package org.locationtech.jtstest.testbuilder.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import junit.framework.TestCase;

public class SegmentClipperTest extends TestCase {

  public SegmentClipperTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    String[] testCaseName = {SegmentClipperTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }

  public void testSimple() {
    checkClip(new Coordinate(0, 10), new Coordinate(20, 30), 
        new Envelope(10, 100, 10, 100), 
        new Coordinate(10, 20), new Coordinate(20, 30) );
  }
  public void checkClip(Coordinate p0, Coordinate p1, Envelope env, Coordinate expected0, Coordinate expected1) {

    SegmentClipper.clip(p0, p1, env);
    boolean isOK = expected0.equals2D(p0) && expected1.equals2D(p1);
    if (!isOK) {
      System.out.println("FAIL: " 
          + "Actual = " + p0 + " - " + p1 
          + " , Expected = " + expected0 + " - " + expected1);
    }
  }

  
}
