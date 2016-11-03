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

package org.locationtech.jts.geom.util;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;


/**
 * Tests {@link AffineTransformationBuilder}.
 * 
 * @author Martin Davis
 */
public class AffineTransformationBuilderTest
		extends TestCase
{
  public AffineTransformationBuilderTest(String name)
  {
    super(name);
  }

  public void testRotate1()
  {
    run(0, 0,    1, 0,    0, 1,
        0, 0,    0, 1,    -1, 0);
  }
    
  public void testRotate2()
  {
    run(0, 0,    1, 0,    0, 1,
        0, 0,    1, 1,    -1, 1);
  }
    
  public void testScale1()
  {
    run(0, 0,    1, 0,    0, 1,
        0, 0,    2, 0,    0, 2);
  }
  
  public void testTranslate1()
  {
    run(0, 0,    1, 0,    0, 1,
        5, 6,    6, 6,    5, 7);
  }
  
  public void testLinear1()
  {
    run(0, 0,    1, 0,    0, 1,
        0, 0,    0, 0,    5, 7);
  }
  
  public void testSingular2()
  {
    // points on a line mapping to collinear points - not uniquely specified
    runSingular(0, 0,    1,   1,     2, 2,
                0, 0,    10, 10,    30, 30);
  }
  
  public void testSingular3()
  {
    // points on a line mapping to collinear points - not uniquely specified
    runSingular(0, 0,    1,   1,     2, 2,
                0, 0,    10, 10,    20, 20);
  }
  
  public void testSingular1()
  {
    // points on a line mapping to non-collinear points - no solution
    runSingular(0, 0,    1, 1,    2, 2,
                0, 0,    1, 2,    1, 3);
  }
  
  public void testSingleControl1()
  {
    run(0, 0,
        5, 6);
  }
  
  public void testDualControl_Translation()
  {
  	run(0, 0,    1, 1,
  			5, 5,    6, 6);
  }

  public void testDualControl_General()
  {
  	run(0, 0,    1, 1,
  			5, 5,    6, 9);
  }

  void run(double p0x, double p0y, 
      double p1x, double p1y, 
      double p2x, double p2y,
      double pp0x, double pp0y,
      double pp1x, double pp1y, 
      double pp2x, double pp2y
      )
  {
    Coordinate p0 = new Coordinate(p0x, p0y);
    Coordinate p1 = new Coordinate(p1x, p1y);
    Coordinate p2 = new Coordinate(p2x, p2y);
    
    Coordinate pp0 = new Coordinate(pp0x, pp0y);
    Coordinate pp1 = new Coordinate(pp1x, pp1y);
    Coordinate pp2 = new Coordinate(pp2x, pp2y);
    
    AffineTransformationBuilder atb = new AffineTransformationBuilder(
        p0, p1, p2,
        pp0, pp1, pp2);
    AffineTransformation trans = atb.getTransformation();
    
    Coordinate dest = new Coordinate();
    assertEqualPoint(pp0, trans.transform(p0, dest));
    assertEqualPoint(pp1, trans.transform(p1, dest));
    assertEqualPoint(pp2, trans.transform(p2, dest));
  }
  
  void run(double p0x, double p0y, 
      double p1x, double p1y, 
      double pp0x, double pp0y,
      double pp1x, double pp1y
      )
  {
    Coordinate p0 = new Coordinate(p0x, p0y);
    Coordinate p1 = new Coordinate(p1x, p1y);
    
    Coordinate pp0 = new Coordinate(pp0x, pp0y);
    Coordinate pp1 = new Coordinate(pp1x, pp1y);
    
    AffineTransformation trans = AffineTransformationFactory.createFromControlVectors(
        p0, p1,
        pp0, pp1);
    
    Coordinate dest = new Coordinate();
    assertEqualPoint(pp0, trans.transform(p0, dest));
    assertEqualPoint(pp1, trans.transform(p1, dest));
  }
  
  void run(double p0x, double p0y, 
      double pp0x, double pp0y
      )
  {
    Coordinate p0 = new Coordinate(p0x, p0y);
    
    Coordinate pp0 = new Coordinate(pp0x, pp0y);
    
    AffineTransformation trans = AffineTransformationFactory.createFromControlVectors(
        p0, pp0);
    
    Coordinate dest = new Coordinate();
    assertEqualPoint(pp0, trans.transform(p0, dest));
  }
  
  
  void runSingular(double p0x, double p0y, 
      double p1x, double p1y, 
      double p2x, double p2y,
      double pp0x, double pp0y,
      double pp1x, double pp1y, 
      double pp2x, double pp2y
      )
  {
    Coordinate p0 = new Coordinate(p0x, p0y);
    Coordinate p1 = new Coordinate(p1x, p1y);
    Coordinate p2 = new Coordinate(p2x, p2y);
    
    Coordinate pp0 = new Coordinate(pp0x, pp0y);
    Coordinate pp1 = new Coordinate(pp1x, pp1y);
    Coordinate pp2 = new Coordinate(pp2x, pp2y);
    
    AffineTransformationBuilder atb = new AffineTransformationBuilder(
        p0, p1, p2,
        pp0, pp1, pp2);
    AffineTransformation trans = atb.getTransformation();
    assertEquals(trans, null);
  }

  private Coordinate ctl0 = new Coordinate(-10, -10);
  private Coordinate ctl1 = new Coordinate(10, 20);
  private Coordinate ctl2 = new Coordinate(10, -20);
  
  public void testTransform1()
  {
    AffineTransformation trans = new AffineTransformation();
    trans.rotate(1);
    trans.translate(10, 10);
    trans.scale(2, 2);
    runTransform(trans, ctl0, ctl1, ctl2);
  }
  
  public void testTransform2()
  {
    AffineTransformation trans = new AffineTransformation();
    trans.rotate(3);
    trans.translate(10, 10);
    trans.scale(2, 10);
    trans.shear(5, 2);
    trans.reflect(5, 8, 10, 2);
    runTransform(trans, ctl0, ctl1, ctl2);
  }
  
  private void runTransform(AffineTransformation trans,
      Coordinate p0,
      Coordinate p1, 
      Coordinate p2)
  {    
    Coordinate pp0 = trans.transform(p0, new Coordinate());
    Coordinate pp1 = trans.transform(p1, new Coordinate());
    Coordinate pp2 = trans.transform(p2, new Coordinate());

    AffineTransformationBuilder atb = new AffineTransformationBuilder(
        p0, p1, p2,
        pp0, pp1, pp2);
    AffineTransformation atbTrans = atb.getTransformation();
    
    Coordinate dest = new Coordinate();
    assertEqualPoint(pp0, atbTrans.transform(p0, dest));
    assertEqualPoint(pp1, atbTrans.transform(p1, dest));
    assertEqualPoint(pp2, atbTrans.transform(p2, dest));
  }
  
  
  private void assertEqualPoint(Coordinate p, Coordinate q)
  {
    assertEquals(p.x, q.x, 0.00005);
    assertEquals(p.y, q.y, 0.00005);
  }

}
