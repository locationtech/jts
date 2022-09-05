/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class CoverageSimplifierTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(CoverageSimplifierTest.class);
  }
  
  public CoverageSimplifierTest(String name) {
    super(name);
  }
  
  public void testSimple2() {
    checkNoop(readArray(
        "POLYGON ((100 100, 200 200, 300 100, 200 101, 100 100))",
        "POLYGON ((150 0, 100 100, 200 101, 300 100, 250 0, 150 0))" )
    );
  }

  public void testSimple3() {
    checkNoop(readArray(
        "POLYGON ((100 300, 200 200, 100 200, 100 300))",
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((100 100, 200 100, 150 50, 100 100))" )
    );
  }

  private void checkNoop(Geometry[] input) {
    Geometry[] actual = CoverageSimplifier.simplify(input, 0);
    checkEqual(input, actual);
  }
  
  private void checkResult(Geometry[] input, double tolerance, Geometry[] expected) {
    Geometry[] actual = CoverageSimplifier.simplify(input, tolerance);
    checkEqual(expected, actual);
  }
}
