
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jts.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.algorithm.*;
import com.vividsolutions.jts.geom.*;


/**
 * @version 1.7
 */
public class AngleTest extends TestCase {

  private static final double TOLERANCE = 1E-5;
  
  public static void main(String args[]) {
    TestRunner.run(AngleTest.class);
  }

  public AngleTest(String name) { super(name); }

  public void testAngle() throws Exception
  {
		assertEquals(Angle.angle(new Coordinate(10,0)), 0.0, TOLERANCE);
		assertEquals(Angle.angle(new Coordinate(10,10)), Math.PI/4, TOLERANCE);
		assertEquals(Angle.angle(new Coordinate(0,10)), Math.PI/2, TOLERANCE);
		assertEquals(Angle.angle(new Coordinate(-10,10)), 0.75*Math.PI, TOLERANCE);
		assertEquals(Angle.angle(new Coordinate(-10,0)), Math.PI, TOLERANCE);
		assertEquals(Angle.angle(new Coordinate(-10,-0.1)), -3.131592986903128, TOLERANCE);
		assertEquals(Angle.angle(new Coordinate(-10,-10)), -0.75*Math.PI, TOLERANCE);
  }
  
  public void testIsAcute() throws Exception
  {
  	assertEquals(Angle.isAcute(new Coordinate(10,0), new Coordinate(0,0), new Coordinate(5,10)), true);
  	assertEquals(Angle.isAcute(new Coordinate(10,0), new Coordinate(0,0), new Coordinate(5,-10)), true);
  	// angle of 0
  	assertEquals(Angle.isAcute(new Coordinate(10,0), new Coordinate(0,0), new Coordinate(10,0)), true);
  	
  	assertEquals(Angle.isAcute(new Coordinate(10,0), new Coordinate(0,0), new Coordinate(-5,10)), false);
  	assertEquals(Angle.isAcute(new Coordinate(10,0), new Coordinate(0,0), new Coordinate(-5,-10)), false);

  	
  }
  
  public void testNormalizePositive() throws Exception
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

  public void testNormalize() throws Exception
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


}
