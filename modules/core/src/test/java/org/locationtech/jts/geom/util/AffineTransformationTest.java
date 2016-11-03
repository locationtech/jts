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

import java.io.IOException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;


/**
 * @author Martin Davis
 *
 */
public class AffineTransformationTest
	extends TestCase
{
  public AffineTransformationTest(String name)
  {
    super(name);
  }

  public void testRotate1()
  throws IOException, ParseException
  {
    AffineTransformation t = AffineTransformation.rotationInstance(Math.PI/2);
    checkTransformation(10, 0, t, 0, 10);
    checkTransformation(0, 10, t, -10, 0);
    checkTransformation(-10, -10, t, 10, -10);
  }

  public void testRotateAroundPoint1()
  throws IOException, ParseException
  {
    AffineTransformation t = AffineTransformation.rotationInstance(Math.PI/2, 1, 1);
    checkTransformation(1, 1, t, 1, 1);
    checkTransformation(10, 0, t, 2, 10);
    checkTransformation(0, 10, t, -8, 0);
    checkTransformation(-10, -10, t, 12, -10);
  }

  public void testReflectXY1()
  throws IOException, ParseException
  {
    AffineTransformation t = AffineTransformation.reflectionInstance(1, 1);
    checkTransformation(10, 0, t, 0, 10);
    checkTransformation(0, 10, t, 10, 0);
    checkTransformation(-10, -10, t, -10, -10);
    checkTransformation(-3, -4, t, -4, -3);
  }

  public void testReflectXY2()
  throws IOException, ParseException
  {
    AffineTransformation t = AffineTransformation.reflectionInstance(1, -1);
    checkTransformation(10, 0, t, 0, -10);
    checkTransformation(0, 10, t, -10, 0);
    checkTransformation(-10, -10, t, 10, 10);
    checkTransformation(-3, -4, t, 4, 3);
  }

  public void testReflectXYXY1()
  throws IOException, ParseException
  {
    AffineTransformation t = AffineTransformation.reflectionInstance(0, 5, 5, 0);
    checkTransformation(5, 0, t, 5, 0);
    checkTransformation(0, 0, t, 5, 5);
    checkTransformation(-10, -10, t, 15, 15);
  }

  public void testScale1()
  throws IOException, ParseException
  {
    AffineTransformation t = AffineTransformation.scaleInstance(2, 3);
    checkTransformation(10, 0, t, 20, 0);
    checkTransformation(0, 10, t, 0, 30);
    checkTransformation(-10, -10, t, -20, -30);
  }

  public void testShear1()
  throws IOException, ParseException
  {
    AffineTransformation t = AffineTransformation.shearInstance(2, 3);
    checkTransformation(10, 0, t, 10, 30);
  }

  public void testTranslate1()
  throws IOException, ParseException
  {
    AffineTransformation t = AffineTransformation.translationInstance(2, 3);
    checkTransformation(1, 0, t, 3, 3);
    checkTransformation(0, 0, t, 2, 3);
    checkTransformation(-10, -5, t, -8, -2);
  }

  public void testTranslateRotate1()
  throws IOException, ParseException
  {
    AffineTransformation t = AffineTransformation.translationInstance(3, 3)
    								.rotate(Math.PI/2);
    checkTransformation(10, 0, t, -3, 13);
    checkTransformation(-10, -10, t, 7, -7);
  }

  public void testCompose1()
  {
    AffineTransformation t0 = AffineTransformation.translationInstance(10, 0); 
    t0.rotate(Math.PI /2);
    t0.translate(0, -10);
    
    AffineTransformation t1 = AffineTransformation.translationInstance(0, 0);
    t1.rotate(Math.PI /2);
    
    checkTransformation(t0, t1);
  }
  
  public void testCompose2()
  {
    AffineTransformation t0 = AffineTransformation.reflectionInstance(0, 0, 1, 0); 
    t0.reflect(0, 0, 0, -1);
    
    AffineTransformation t1 = AffineTransformation.rotationInstance(Math.PI);
    
    checkTransformation(t0, t1);
  }
  
  public void testComposeRotation1()
  {
    AffineTransformation t0 = AffineTransformation.rotationInstance(1, 10, 10); 
    
    AffineTransformation t1 = AffineTransformation.translationInstance(-10, -10);
    t1.rotate(1);
    t1.translate(10, 10);
    
    checkTransformation(t0, t1);
  }
  
  public void testLineString() throws IOException, ParseException, NoninvertibleTransformationException {
	  checkTransformation("LINESTRING (1 2, 10 20, 100 200)");
		}

  public void testPolygon() throws IOException, ParseException, NoninvertibleTransformationException {
	  checkTransformation("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))");
  }
  public void testPolygonWithHole()
  throws IOException, ParseException, NoninvertibleTransformationException
  {
	  checkTransformation("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0), (1 1, 1 10, 10 10, 10 1, 1 1) )");
  }
  public void testMultiPoint()
  throws IOException, ParseException, NoninvertibleTransformationException
  {
	  checkTransformation("MULTIPOINT (0 0, 1 4, 100 200)");
  }
  public void testMultiLineString()
  throws IOException, ParseException, NoninvertibleTransformationException
  {
	  checkTransformation("MULTILINESTRING ((0 0, 1 10), (10 10, 20 30), (123 123, 456 789))");
  }
  public void testMultiPolygon()
  throws IOException, ParseException, NoninvertibleTransformationException
  {
	  checkTransformation("MULTIPOLYGON ( ((0 0, 100 0, 100 100, 0 100, 0 0), (1 1, 1 10, 10 10, 10 1, 1 1) ), ((200 200, 200 250, 250 250, 250 200, 200 200)) )");
  }
  
  public void testGeometryCollection()
  throws IOException, ParseException, NoninvertibleTransformationException
  {
	  checkTransformation("GEOMETRYCOLLECTION ( POINT ( 1 1), LINESTRING (0 0, 10 10), POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0)) )");
  }
  
  public void testNestedGeometryCollection()
  throws IOException, ParseException, NoninvertibleTransformationException
  {
	  checkTransformation("GEOMETRYCOLLECTION ( POINT (20 20), GEOMETRYCOLLECTION ( POINT ( 1 1), LINESTRING (0 0, 10 10), POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0)) ) )");
  }
  
  public void testCompose3()
  {
    AffineTransformation t0 = AffineTransformation.reflectionInstance(0, 10, 10, 0); 
    t0.translate(-10, -10);
    
    AffineTransformation t1 = AffineTransformation.reflectionInstance(0, 0, -1, 1);
    
    checkTransformation(t0, t1);
  }
  
  /**
   * Checks that a transformation produces the expected result
   * @param x the input pt x
   * @param y the input pt y
   * @param trans the transformation
   * @param xp the expected output x
   * @param yp the expected output y
   */
  void checkTransformation(double x, double y, AffineTransformation trans, double xp, double yp)
  {
    Coordinate p = new Coordinate(x, y);
    Coordinate p2 = new Coordinate();
    trans.transform(p, p2);
    assertEquals(xp, p2.x, .00005);
    assertEquals(yp, p2.y, .00005);
    
    // if the transformation is invertible, test the inverse
    try {
      AffineTransformation invTrans = trans.getInverse();
      Coordinate pInv = new Coordinate();
      invTrans.transform(p2, pInv);
      assertEquals(x, pInv.x, .00005);
      assertEquals(y, pInv.y, .00005);
      
      double det = trans.getDeterminant();
      double detInv = invTrans.getDeterminant();
      assertEquals(det, 1.0 / detInv, .00005);
     
    }
    catch (NoninvertibleTransformationException ex) {
    }
  }
  
  static WKTReader rdr = new WKTReader();
  
  void checkTransformation(String geomStr) throws IOException, ParseException,
      NoninvertibleTransformationException {
    Geometry geom = rdr.read(geomStr);
    AffineTransformation trans = AffineTransformation
        .rotationInstance(Math.PI / 2);
    AffineTransformation inv = trans.getInverse();
    Geometry transGeom = (Geometry) geom.clone();
    transGeom.apply(trans);
    // System.out.println(transGeom);
    transGeom.apply(inv);
    // check if transformed geometry is equal to original
    boolean isEqual = geom.equalsExact(transGeom, 0.0005);
    assertTrue(isEqual);
  }
  
  void checkTransformation(AffineTransformation trans0, AffineTransformation trans1)
  {
    double[] m0 = trans0.getMatrixEntries();
    double[] m1 = trans1.getMatrixEntries();
    for (int i = 0; i < m0.length; i++) {
      assertEquals(m0[i], m1[i], 0.000005);
    }
  }
}
