/*
 * Copyright (c) 2026 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.geomfunction;

import org.locationtech.jts.geom.Geometry;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests that {@link BaseGeometryFunction#hashCode()} is consistent with
 * {@link BaseGeometryFunction#equals(Object)}: equality is defined by the
 * function signature (name, parameter types, return type), so two functions
 * equal by that definition must report the same hash code.
 */
public class BaseGeometryFunctionTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(BaseGeometryFunctionTest.class);
  }

  public BaseGeometryFunctionTest(String name) { super(name); }

  private static class TestFunction extends BaseGeometryFunction {
    TestFunction(String category, String name, String[] parameterNames,
        Class[] parameterTypes, Class returnType) {
      super(category, name, parameterNames, parameterTypes, returnType);
    }
    public Object invoke(Geometry geom, Object[] args) { return null; }
  }

  public void testEqualsHashCodeContract() {
    // equals() is defined by the signature and ignores parameter names,
    // so these two functions are equal ...
    BaseGeometryFunction a = new TestFunction("cat", "fn",
        new String[] { "x" }, new Class[] { Geometry.class }, Geometry.class);
    BaseGeometryFunction b = new TestFunction("cat", "fn",
        new String[] { "y" }, new Class[] { Geometry.class }, Geometry.class);
    assertTrue(a.equals(b));
    // ... therefore they must report equal hash codes
    assertEquals(a.hashCode(), b.hashCode());
  }
}
