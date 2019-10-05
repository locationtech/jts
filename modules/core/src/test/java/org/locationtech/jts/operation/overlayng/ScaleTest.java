package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class ScaleTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(ScaleTest.class);
  }

  public ScaleTest(String name) { super(name); }
  
  public void testInts() {
    checkScaleAuto("POINT(1 1)", "POINT(10 10)", 1);
  }

  public void testPower10() {
    checkScaleAuto("POINT(100 100)", "POINT(1000 1000)", 1);
  }

  public void testDecimalsDifferent() {
    checkScaleAuto("POINT( 1.123 1.12 )", "POINT( 10.123 10.12345 )", 1e5);
  }

  public void testDecimalsShort() {
    checkScaleAuto("POINT(1 1.12345)", "POINT(10 10)", 1e5);    
  }
  
  public void testDecimalsMany() {
    checkScaleAuto("POINT(1 1.123451234512345)", "POINT(10 10)", 1e12);    
  }
  
  public void testDecimalsAllLong() {
    checkScaleAuto("POINT( 1.123451234512345 1.123451234512345 )", "POINT( 10.123451234512345 10.123451234512345 )", 1e12);    
  }
  
  public void testLargeMagnitude6() {
    checkScaleAuto("POINT( 123123.123451234512345 1 )", "POINT( 10 10 )", 1e8);    
  }
  
  public void testLargeMagnitude9() {
    checkScaleAuto("POINT( 123123123.123451234512345 1 )", "POINT( 10 10 )", 1e5);    
  }
  
  private void checkScaleAuto(String wktA, String wktB, double scaleExpected) {
    Geometry a = read(wktA);
    Geometry b = read(wktB);
    double scale = Scale.autoScale(a, b);
    assertEquals("Auto scale: ", scaleExpected, scale );
  }
}
