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
  
  public void testNoopSimple2() {
    checkNoop(readArray(
        "POLYGON ((100 100, 200 200, 300 100, 200 101, 100 100))",
        "POLYGON ((150 0, 100 100, 200 101, 300 100, 250 0, 150 0))" )
    );
  }

  public void testNoopSimple3() {
    checkNoop(readArray(
        "POLYGON ((100 300, 200 200, 100 200, 100 300))",
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((100 100, 200 100, 150 50, 100 100))" )
    );
  }

  public void testNoopHole() {
    checkNoop(readArray(
        "POLYGON ((10 90, 90 90, 90 10, 10 10, 10 90), (20 80, 80 80, 80 20, 20 20, 20 80))",
        "POLYGON ((80 20, 20 20, 20 80, 80 80, 80 20))" )
    );
  }

  public void testNoopMulti() {
    checkNoop(readArray(
        "MULTIPOLYGON (((10 10, 10 50, 50 50, 50 10, 10 10)), ((90 90, 90 50, 50 50, 50 90, 90 90)))",
        "MULTIPOLYGON (((10 90, 50 90, 50 50, 10 50, 10 90)), ((90 10, 50 10, 50 50, 90 50, 90 10)))" )
    );
  }

  //---------------------------------------------
  
  public void testSimple2() {
    checkResult(readArray(
        "POLYGON ((100 100, 200 200, 300 100, 200 101, 100 100))",
        "POLYGON ((150 0, 100 100, 200 101, 300 100, 250 0, 150 0))" ),
        100,
        readArray(
            "POLYGON ((100 100, 200 200, 300 100, 100 100))",
            "POLYGON ((150 0, 100 100, 300 100, 250 0, 150 0))" )
    );
  }

  public void testRingNoCollapse() {
    checkResult(readArray(
        "POLYGON ((10 50, 60 90, 70 50, 60 10, 10 50))" ),
        100000,
        readArray(
            "POLYGON ((10 50, 60 90, 60 10, 10 50))" )
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
