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
    checkScaleAuto("POINT(1 1)", "POINT(10 10)", 
        1, 1e12, 1 );
  }

  public void testBNull() {
    checkScaleAuto("POINT(1 1)", null, 
        1, 1e13, 1 );
  }

  public void testPower10() {
    checkScaleAuto("POINT(100 100)", "POINT(1000 1000)", 
        1, 1e11, 1 );
  }

  public void testDecimalsDifferent() {
    checkScaleAuto("POINT( 1.123 1.12 )", "POINT( 10.123 10.12345 )", 
        1e5, 1e12, 1e5 );
  }

  public void testDecimalsShort() {
    checkScaleAuto("POINT(1 1.12345)", "POINT(10 10)", 
        1e5, 1e12, 1e5 );    
  }
  
  public void testDecimalsMany() {
    checkScaleAuto("POINT(1 1.123451234512345)", "POINT(10 10)", 
        1e12, 1e12, 1e15 );    
  }
  
  public void testDecimalsAllLong() {
    checkScaleAuto("POINT( 1.123451234512345 1.123451234512345 )", "POINT( 10.123451234512345 10.123451234512345 )", 
        1e12, 1e12, 1e15 );    
  }
  
  public void testSafeScaleChosen() {
    checkScaleAuto("POINT( 123123.123451234512345 1 )", "POINT( 10 10 )", 
        1e8, 1e8, 1e11 );    
  }
  
  public void testSafeScaleChosenLargeMagnitude() {
    checkScaleAuto("POINT( 123123123.123451234512345 1 )", "POINT( 10 10 )", 
        1e5, 1e5, 1e8 );    
  }
  
  public void testInherentWithLargeMagnitude() {
    checkScaleAuto("POINT( 123123123.12 1 )", "POINT( 10 10 )", 
        1e2, 1e5, 1e2 );    
  }
  
  public void testMixedMagnitude() {
    checkScaleAuto("POINT( 1.123451234512345 1 )", "POINT( 100000.12345 10 )", 
        1e8, 1e8, 1e15 );    
  }
  
  public void testInherentBelowSafe() {
    checkScaleAuto("POINT( 100000.1234512 1 )", "POINT( 100000.12345 10 )", 
        1e7, 1e8, 1e7 );    
  }
  
  private void checkScaleAuto(String wktA, String wktB, double autoscaleExpected,
      double safeScaleExpected, double inherentScaleExpected ) {
    Geometry a = read(wktA);
    Geometry b = null;
    if (wktB != null) {
      b = read(wktB);
    }
    double autoScale = PrecisionUtil.robustScale(a, b);
    assertEquals("Auto scale: ", autoscaleExpected, autoScale );
    assertEquals("Inherent scale: ", inherentScaleExpected, PrecisionUtil.inherentScale(a, b) );
    assertEquals("Safe scale: ", safeScaleExpected, PrecisionUtil.safeScale(a, b) );
  }

}
