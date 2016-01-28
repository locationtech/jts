/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Martin Davis
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Martin Davis BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jts.math;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class Vector2DTest extends TestCase {

  private static final double TOLERANCE = 1E-5;
  
  public static void main(String args[]) {
    TestRunner.run(Vector2DTest.class);
  }

  public Vector2DTest(String name) { super(name); }

  public void testLength()
  {
    assertEquals(Vector2D.create(0,1).length(), 1.0, TOLERANCE);
    assertEquals(Vector2D.create(0,-1).length(), 1.0, TOLERANCE);
    assertEquals(Vector2D.create(1,1).length(), Math.sqrt(2.0), TOLERANCE);
    assertEquals(Vector2D.create(3,4).length(), 5, TOLERANCE);
  }
  
  public void testIsParallel() throws Exception
  {
    assertTrue(Vector2D.create(0,1).isParallel(Vector2D.create(0,2)));
    assertTrue(Vector2D.create(1,1).isParallel(Vector2D.create(2,2)));
    assertTrue(Vector2D.create(-1,-1).isParallel(Vector2D.create(2,2)));
    
    assertTrue(! Vector2D.create(1,-1).isParallel(Vector2D.create(2,2)));
  }
  
  public void testToCoordinate()
  {
    assertEquals(Vector2D.create(
        Vector2D.create(1,2).toCoordinate()), 
        Vector2D.create(1,2), TOLERANCE);
  }

  void assertEquals(Vector2D v1, Vector2D v2)
  {
    assertTrue(v1.equals(v2));
  }
  
  void assertEquals(Vector2D v1, Vector2D v2, double tolerance)
  {
    assertEquals(v1.getX(), v2.getX(), tolerance);
    assertEquals(v1.getY(), v2.getY(), tolerance);
  }
}
