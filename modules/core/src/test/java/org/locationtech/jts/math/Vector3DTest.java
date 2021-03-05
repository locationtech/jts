/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.math;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class Vector3DTest extends TestCase {

  private static final double TOLERANCE = 1E-5;
  
  public static void main(String args[]) {
    TestRunner.run(Vector3DTest.class);
  }

  public Vector3DTest(String name) { super(name); }

  public void testLength()
  {
    assertEquals(1.0, create(0,1,0).length(), TOLERANCE);
    assertEquals(1.0, create(0,-1, 0).length(), TOLERANCE);
    assertEquals(Math.sqrt(2.0), create(1,1,0).length(), TOLERANCE);
    assertEquals(5, create(3,4,0).length(), TOLERANCE);
    assertEquals(Math.sqrt(3), create(1,1,1).length(), TOLERANCE);
    assertEquals(Math.sqrt(1+4+9), create(1,2,3).length(), TOLERANCE);
  }

  public void testAdd() {
    assertEquals(create(5,7,9), create(1,2,3).add(create(4,5,6)));
  }
  
  public void testSubtract() {
    assertEquals(create(-3,0,3), create(1,5,9).subtract(create(4,5,6)));
  }
  
  public void testDivide() {
    assertEquals(create(1,2,3), create(2,4,6).divide(2));
  }
  
  public void testDot() {
    assertEquals(20.0, create(2,3,4).dot(create(1,2,3)));
  }
  
  public void testDotABCD() {
    double dot = Vector3D.dot(
        coord(2,3,4), coord(3,4,5),
        coord(0,1,-1), coord(1,5,2));
    assertEquals(8.0, dot);
    assertEquals(dot, create(1,1,1).dot(create(1,4,3)));
  }
  
  public void testNormlize() {
    assertEquals(create(-0.5773502691896258, 0.5773502691896258, 0.5773502691896258), 
        create(-1,1,1).normalize());
    assertEquals(create(0.5773502691896258, 0.5773502691896258, 0.5773502691896258), 
        create(2,2,2).normalize());
    assertEquals(create(0.2672612419124244, 0.5345224838248488, 0.8017837257372732), 
        create(1,2,3).normalize());
  }
  
  static Coordinate coord(double x, double y, double z) {
    return new Coordinate(x,y,z);
  }
  static Vector3D create(double x, double y, double z) {
    return Vector3D.create(x, y, z);
  }
  
  void assertEquals(Vector3D expected, Vector3D actual)
  {
    boolean isEqual = expected.equals(actual);
    if (! isEqual) {
      System.out.println("Expected " + expected + " but actual is " + actual);
    }
    assertTrue(isEqual);
  }
  
  void assertEquals(Vector3D expected, Vector3D actual, double tolerance)
  {
    assertEquals(expected.getX(), actual.getX(), tolerance);
    assertEquals(expected.getY(), actual.getY(), tolerance);
    assertEquals(expected.getZ(), actual.getZ(), tolerance);
  }
}
