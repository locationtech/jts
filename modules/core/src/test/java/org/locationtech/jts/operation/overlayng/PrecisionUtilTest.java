/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PrecisionUtilTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(PrecisionUtilTest.class);
  }

  public PrecisionUtilTest(String name) { super(name); }
  
  public void testInts() {
    checkRobustScale("POINT(1 1)", "POINT(10 10)", 
        1, 1e12, 1 );
  }

  public void testBNull() {
    checkRobustScale("POINT(1 1)", null, 
        1, 1e13, 1 );
  }

  public void testPower10() {
    checkRobustScale("POINT(100 100)", "POINT(1000 1000)", 
        1, 1e11, 1 );
  }

  public void testDecimalsDifferent() {
    checkRobustScale("POINT( 1.123 1.12 )", "POINT( 10.123 10.12345 )", 
        1e5, 1e12, 1e5 );
  }

  public void testDecimalsShort() {
    checkRobustScale("POINT(1 1.12345)", "POINT(10 10)", 
        1e5, 1e12, 1e5 );    
  }
  
  public void testDecimalsMany() {
    checkRobustScale("POINT(1 1.123451234512345)", "POINT(10 10)", 
        1e12, 1e12, 1e15 );    
  }
  
  public void testDecimalsAllLong() {
    checkRobustScale("POINT( 1.123451234512345 1.123451234512345 )", "POINT( 10.123451234512345 10.123451234512345 )", 
        1e12, 1e12, 1e15 );    
  }
  
  public void testSafeScaleChosen() {
    checkRobustScale("POINT( 123123.123451234512345 1 )", "POINT( 10 10 )", 
        1e8, 1e8, 1e11 );    
  }
  
  public void testSafeScaleChosenLargeMagnitude() {
    checkRobustScale("POINT( 123123123.123451234512345 1 )", "POINT( 10 10 )", 
        1e5, 1e5, 1e8 );    
  }
  
  public void testInherentWithLargeMagnitude() {
    checkRobustScale("POINT( 123123123.12 1 )", "POINT( 10 10 )", 
        1e2, 1e5, 1e2 );    
  }
  
  public void testMixedMagnitude() {
    checkRobustScale("POINT( 1.123451234512345 1 )", "POINT( 100000.12345 10 )", 
        1e8, 1e8, 1e15 );    
  }
  
  public void testInherentBelowSafe() {
    checkRobustScale("POINT( 100000.1234512 1 )", "POINT( 100000.12345 10 )", 
        1e7, 1e8, 1e7 );    
  }
  
  private void checkRobustScale(String wktA, String wktB, double scaleExpected,
      double safeScaleExpected, double inherentScaleExpected ) {
    Geometry a = read(wktA);
    Geometry b = null;
    if (wktB != null) {
      b = read(wktB);
    }
    double robustScale = PrecisionUtil.robustScale(a, b);
    assertEquals("Auto scale: ", scaleExpected, robustScale );
    assertEquals("Inherent scale: ", inherentScaleExpected, PrecisionUtil.inherentScale(a, b) );
    assertEquals("Safe scale: ", safeScaleExpected, PrecisionUtil.safeScale(a, b) );
  }

}
