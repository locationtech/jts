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

package org.locationtech.jts.operation.relate;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Additional tests for {@link RelateOp}, especially for the use with a {@link PrecisionModel}.
 *
 * @author FObermaier
 * @since 1.15
 */
public class RelateWithPrecisionModelTest
    extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(RelateTest.class);
  }

  private GeometryFactory fact = new GeometryFactory();
  private WKTReader rdr = new WKTReader(fact);

  public RelateWithPrecisionModelTest(String name)
  {
    super(name);
  }

  public void testRotationInvariant() throws ParseException
  {
    Geometry geomA = rdr.read("MULTIPOLYGON (((0 10, 10 10, 0 0, 0 10)), ((6 5, 10 5, 10 0, 6 0, 6 5)))");
    Geometry geomB = rdr.read("LINESTRING(5 6, 2 9)");
    
    doTestRotationInvariant2(geomA, geomB);
    doTestRotationInvariant(geomA, geomB);
    
    // TODO Add others
  }

  private void doTestRotationInvariant2(Geometry geomA, Geometry geomB) throws ParseException
  {
    PrecisionModel pm = new PrecisionModel(1);
    
    IntersectionMatrix im1 = getIntersectionMatrix(geomA, geomB, pm);
    IntersectionMatrix im2 = getIntersectionMatrix(geomB, geomA, pm);
    
    System.out.println("Geometry A is\n" + geomA.toString());
    System.out.println("Geometry B is\n" + geomB.toString());
    System.out.println("Relate(A, B) is " + im1.toString());
    System.out.println("Relate(B, A) is " + im2.toString());
    IntersectionMatrix im3 = new IntersectionMatrix(im1);
    im3.transpose();
    System.out.println("Relate(A, B).transpose is " + im3.toString());
    
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j ++) {
        assertEquals(im1.get(i,  j), im2.get(j, i));
        assertEquals(im2.get(i,  j), im3.get(i, j));
      }
    }
    Assert.assertTrue("Results are rotational-invariant", true);
    //assertEquals(rop1, rop2);
  }
  
  private static IntersectionMatrix getIntersectionMatrix(Geometry geomA, Geometry geomB,
      PrecisionModel pm)
  {
    RelateOp ro = new RelateOp(geomA, geomB);
    ro.setPrecisionModel(pm);
    
    return ro.getIntersectionMatrix();//.toString();
  }
  
  @SuppressWarnings("null")
  private void doTestRotationInvariant(Geometry geomA, Geometry geomB) throws ParseException
  {
    System.out.println("Geometry A is\n" + geomA.toString());
    System.out.println("Geometry B is\n" + geomB.toString());

    boolean invariant = true;
    PrecisionModel pm = fact.getPrecisionModel();
    PrecisionModel pmFailed = null;
    invariant &= checkIsInvariantToRotation(geomA, geomB, pm, pmFailed);
    invariant &= checkIsInvariantToRotation(geomA, geomB, pm = new PrecisionModel(1000), pmFailed);
    invariant &= checkIsInvariantToRotation(geomA, geomB, pm = new PrecisionModel(100), pmFailed);
    invariant &= checkIsInvariantToRotation(geomA, geomB, pm = new PrecisionModel(10), pmFailed);
    invariant &= checkIsInvariantToRotation(geomA, geomB, pm = new PrecisionModel(1), pmFailed);

    if (invariant)
      assertTrue("Relate computation is invariant for rotation.", invariant);
    else
      assertTrue("Relate computation is variant on rotation for " + pmFailed.toString() + "." , invariant);
  }
  
  private boolean checkIsInvariantToRotation(Geometry geomA, Geometry geomB, 
      PrecisionModel pm, PrecisionModel pmFailed) {
  
    Coordinate c1 = geomA.getCentroid().getCoordinate();
    Coordinate c2 = geomB.getCentroid().getCoordinate();
    Coordinate ct = new Coordinate((c1.y+c2.y)*0.5,(c1.y+c2.y)*0.5);
    
    RelateOp ro = new RelateOp(geomA, geomB);
    ro.setPrecisionModel(pm);
    IntersectionMatrix im = ro.getIntersectionMatrix();
    System.out.println("\nPrecision model is " + pm.toString());
    System.out.println("DE-9IM is " + im.toString());
    
    boolean invariant = true;
    for (int i = 0; i < 360; i++)
    {
      double theta = i / (2*Math.PI);
      AffineTransformation at = AffineTransformation.rotationInstance(theta, ct.x, ct.y);
      
      Geometry tmpGeomA = at.transform(geomA);
      Geometry tmpGeomB = at.transform(geomB);
      
      RelateOp tmpRo = new RelateOp(tmpGeomA, tmpGeomB);
      tmpRo.setPrecisionModel(pm);
      IntersectionMatrix tmpIm = tmpRo.getIntersectionMatrix();
      
      if (!im.toString().equals(tmpIm.toString()))
      {
        System.out.println("Different result at " + i + "°: " +tmpIm.toString());
        invariant = false;
      }
    }

    if (!invariant && pmFailed == null)
      pmFailed = pm;
    
    System.out.println("Result of RelateOp is " + (invariant ? "invariant" : "variant") + " to rotation");
    
    return invariant;
  }
  
  public void testFailingXml1() throws ParseException
  {
    String wktA = 
        "POLYGON((10 100, 10 10, 100 10, 100 100, 10 100),"+ 
              "(90 90, 11 90, 10 10, 90 11, 90 90))";
    String wktB = 
        "POLYGON((10 30, 10 0, 30 10, 30 30, 10 30))";
  
    doTestFailingXml(wktA, wktB);
  }

  // is equal to testFailingXml1
  public void _testFailingXml2() throws ParseException
  {
    String wktA = 
        "POLYGON((10 100, 10 10, 100 10, 100 100, 10 100),"+ 
              "(90 90, 11 90, 10 10, 90 11, 90 90))";
    String wktB = 
        "POLYGON((10 30, 10 10, 30 10, 30 30, 10 30))";
  
    doTestFailingXml(wktA, wktB);
  }

  public void testFailingXml3() throws ParseException
  {
    String wktA = 
        "POLYGON((10 30, 10 10, 30 10, 30 30, 10 30))";
    String wktB = 
        "POLYGON((10 100, 10 10, 100 10, 100 100, 10 100), "+
                    "(90 90, 11 90, 10 10, 90 11, 90 90))";
  
    doTestFailingXml(wktA, wktB);
  }
  
  private void doTestFailingXml(String wktA, String wktB) throws ParseException
  {
    assertTrue(doCheck(new PrecisionModel(10), wktA, wktB));
    //assertTrue(
    doCheck(new PrecisionModel(1), wktA, wktB);
    //);
  }

@SuppressWarnings("unused")
private boolean doCheck(PrecisionModel pm, String wktA, String wktB) throws ParseException {
  
  GeometryFactory tmpGf = new GeometryFactory(pm);
  WKTReader tmpRdr = new WKTReader(tmpGf);
  
  Geometry geomA = tmpRdr.read(wktA);
  Geometry geomB = tmpRdr.read(wktB);
  
  RelateOp ro = new RelateOp(geomA, geomB);
  ro.setPrecisionModel(pm);
  
  try
  {
    IntersectionMatrix im = ro.getIntersectionMatrix();
      System.out.println("\nRelateOp.getIntersectionMatrix does not fail with");
      System.out.println("PrecisionModel" + pm.toString());
    return true;
  }
  catch(TopologyException tex)
  {
      System.out.println("\nRelateOp.getIntersectionMatrix fails with\n" + tex.toString());
      System.out.println("\nPrecisionModel is " + pm.toString());
      System.out.println("Geometry A\n" + wktA);
      System.out.println("Geometry B\n" + wktB);

      return false;
  }}
}