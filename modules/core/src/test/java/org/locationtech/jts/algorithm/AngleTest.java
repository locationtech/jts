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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.shape.random.RandomPointsBuilder;

import java.util.Arrays;

/**
 * @version 1.7
 */
public class AngleTest extends TestCase {

  private static final double TOLERANCE = 1E-5;
  
  public static void main(String args[]) {
    TestRunner.run(AngleTest.class);
  }

  public AngleTest(String name) { super(name); }

  public void testAngle()
  {
		assertEquals(Angle.angle(p(10,0)), 0.0, TOLERANCE);
		assertEquals(Angle.angle(p(10,10)), Math.PI/4, TOLERANCE);
		assertEquals(Angle.angle(p(0,10)), Math.PI/2, TOLERANCE);
		assertEquals(Angle.angle(p(-10,10)), 0.75*Math.PI, TOLERANCE);
		assertEquals(Angle.angle(p(-10,0)), Math.PI, TOLERANCE);
		assertEquals(Angle.angle(p(-10,-0.1)), -3.131592986903128, TOLERANCE);
		assertEquals(Angle.angle(p(-10,-10)), -0.75*Math.PI, TOLERANCE);
  }
  
  public void testIsAcute()
  {
    assertEquals(Angle.isAcute(p(10,0), p(0,0), p(5,10)), true);
    assertEquals(Angle.isAcute(p(10,0), p(0,0), p(5,-10)), true);
    // angle of 0
    assertEquals(Angle.isAcute(p(10,0), p(0,0), p(10,0)), true);
    
    assertEquals(Angle.isAcute(p(10,0), p(0,0), p(-5,10)), false);
    assertEquals(Angle.isAcute(p(10,0), p(0,0), p(-5,-10)), false);
  }
  
  public void testIsObtuse()
  {
    assertEquals(Angle.isObtuse(p(10,0), p(0,0), p(5,10)), false);
    assertEquals(Angle.isObtuse(p(10,0), p(0,0), p(5,-10)), false);
    // angle of 0
    assertEquals(Angle.isObtuse(p(10,0), p(0,0), p(10,0)), false);
    
    assertEquals(Angle.isObtuse(p(10,0), p(0,0), p(-5,10)), true);
    assertEquals(Angle.isObtuse(p(10,0), p(0,0), p(-5,-10)), true);
  }
  
  public void testNormalizePositive()
  {
		assertEquals(Angle.normalizePositive(0.0), 0.0, TOLERANCE);
		
		assertEquals(Angle.normalizePositive(-0.5*Math.PI), 1.5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalizePositive(-Math.PI), Math.PI, TOLERANCE);
		assertEquals(Angle.normalizePositive(-1.5*Math.PI), .5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalizePositive(-2*Math.PI), 0.0, TOLERANCE);
		assertEquals(Angle.normalizePositive(-2.5*Math.PI), 1.5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalizePositive(-3*Math.PI), Math.PI, TOLERANCE);	
		assertEquals(Angle.normalizePositive(-4 * Math.PI), 0.0, TOLERANCE);
		
		assertEquals(Angle.normalizePositive(0.5*Math.PI), 0.5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalizePositive(Math.PI), Math.PI, TOLERANCE);
		assertEquals(Angle.normalizePositive(1.5*Math.PI), 1.5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalizePositive(2*Math.PI), 0.0, TOLERANCE);
		assertEquals(Angle.normalizePositive(2.5*Math.PI), 0.5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalizePositive(3*Math.PI), Math.PI, TOLERANCE);	
		assertEquals(Angle.normalizePositive(4 * Math.PI), 0.0, TOLERANCE);
  }

  public void testNormalize()
  {
		assertEquals(Angle.normalize(0.0), 0.0, TOLERANCE);
		
		assertEquals(Angle.normalize(-0.5*Math.PI), -0.5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalize(-Math.PI), Math.PI, TOLERANCE);
		assertEquals(Angle.normalize(-1.5*Math.PI), .5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalize(-2*Math.PI), 0.0, TOLERANCE);
		assertEquals(Angle.normalize(-2.5*Math.PI), -0.5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalize(-3*Math.PI), Math.PI, TOLERANCE);	
		assertEquals(Angle.normalize(-4 * Math.PI), 0.0, TOLERANCE);
		
		assertEquals(Angle.normalize(0.5*Math.PI), 0.5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalize(Math.PI), Math.PI, TOLERANCE);
		assertEquals(Angle.normalize(1.5*Math.PI), -0.5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalize(2*Math.PI), 0.0, TOLERANCE);
		assertEquals(Angle.normalize(2.5*Math.PI), 0.5*Math.PI, TOLERANCE);
		assertEquals(Angle.normalize(3*Math.PI), Math.PI, TOLERANCE);	
		assertEquals(Angle.normalize(4 * Math.PI), 0.0, TOLERANCE);
  }

  public void testInteriorAngle() {
		Coordinate p1 = p(1, 2);
		Coordinate p2 = p(3, 2);
		Coordinate p3 = p(2, 1);

		// Tests all interior angles of a triangle "POLYGON ((1 2, 3 2, 2 1, 1 2))"
		assertEquals(45, Math.toDegrees(Angle.interiorAngle(p1, p2, p3)), 0.01);
		assertEquals(90, Math.toDegrees(Angle.interiorAngle(p2, p3, p1)), 0.01);
		assertEquals(45, Math.toDegrees(Angle.interiorAngle(p3, p1, p2)), 0.01);
		// Tests interior angles greater than 180 degrees
		assertEquals(315, Math.toDegrees(Angle.interiorAngle(p3, p2, p1)), 0.01);
		assertEquals(270, Math.toDegrees(Angle.interiorAngle(p1, p3, p2)), 0.01);
		assertEquals(315, Math.toDegrees(Angle.interiorAngle(p2, p1, p3)), 0.01);
  }

  /**
   * Tests interior angle calculation using a number of random triangles
   */
  public void testInteriorAngle_randomTriangles() {
		GeometryFactory geometryFactory = new GeometryFactory();
		CoordinateSequenceFactory coordinateSequenceFactory = geometryFactory.getCoordinateSequenceFactory();
		for (int i = 0; i < 100; i++){
			RandomPointsBuilder builder = new RandomPointsBuilder();
			builder.setNumPoints(3);
			Geometry threeRandomPoints = builder.getGeometry();
			Polygon triangle = geometryFactory.createPolygon(
					CoordinateSequences.ensureValidRing(
							coordinateSequenceFactory,
							coordinateSequenceFactory.create(threeRandomPoints.getCoordinates())
					)
			);
			// Triangle coordinates in clockwise order
			Coordinate[] c = Orientation.isCCW(triangle.getCoordinates())
					? triangle.reverse().getCoordinates()
					: triangle.getCoordinates();
			double sumOfInteriorAngles = Angle.interiorAngle(c[0], c[1], c[2])
					+ Angle.interiorAngle(c[1], c[2], c[0])
					+ Angle.interiorAngle(c[2], c[0], c[1]);
			assertEquals(
					i + ": The sum of the angles of a triangle is not equal to two right angles for points: " + Arrays.toString(c),
					Math.PI,
					sumOfInteriorAngles,
					0.01
			);
		}
  }
  
  public void testAngleBisector() {
    assertEquals(45,    Math.toDegrees( Angle.bisector(p(0,1), p(0,0), p(1,0))), 0.01);
    assertEquals(22.5,  Math.toDegrees( Angle.bisector(p(1,1), p(0,0), p(1,0))), 0.01);
    assertEquals(67.5,    Math.toDegrees( Angle.bisector(p(-1,1), p(0,0), p(1,0))), 0.01);
    assertEquals(-45,   Math.toDegrees( Angle.bisector(p(0,-1), p(0,0), p(1,0))), 0.01);
    assertEquals(180,    Math.toDegrees( Angle.bisector(p(-1,-1), p(0,0), p(-1,1))), 0.01);
    
    assertEquals(45, Math.toDegrees(Angle.bisector(p(13,10), p(10,10), p(10,20))), 0.01);
  }

  public void testSinCosSnap() {

    // -720 to 720 degrees with 1 degree increments
    for (int angdeg = -720; angdeg <= 720; angdeg++) {
      double ang = Angle.toRadians(angdeg);

      double rSin = Angle.sinSnap(ang);
      double rCos = Angle.cosSnap(ang);

      double cSin = Math.sin(ang);
      double cCos = Math.cos(ang);
      if ( (angdeg % 90) == 0 ) {
        // not always the same for multiples of 90 degrees
        assertTrue(Math.abs(rSin - cSin) < 1e-15);
        assertTrue(Math.abs(rCos - cCos) < 1e-15);
      } else {
        assertEquals(rSin, cSin);
        assertEquals(rCos, cCos);
      }

    }

    // use radian increments that don't snap to exact degrees or zero
    for (double angrad = -6.3; angrad < 6.3; angrad += 0.013) {

      double rSin = Angle.sinSnap(angrad);
      double rCos = Angle.cosSnap(angrad);

      assertEquals(rSin, Math.sin(angrad));
      assertEquals(rCos, Math.cos(angrad));

    }
  }

  private static Coordinate p(double x, double y) {
    return new Coordinate(x, y);
  }
}
