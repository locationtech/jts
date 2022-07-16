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

public class CoverageUnionTest extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(CoverageUnionTest.class);
  }
  
  public CoverageUnionTest(String name) {
    super(name);
  }
  
  public void testChessboard4() {
    checkUnion(
        "MULTIPOLYGON (((1 9, 5 9, 5 5, 1 5, 1 9)), ((5 9, 9 9, 9 5, 5 5, 5 9)), ((1 5, 5 5, 5 1, 1 1, 1 5)), ((5 5, 9 5, 9 1, 5 1, 5 5)))",
        "POLYGON ((5 9, 9 9, 9 5, 9 1, 5 1, 1 1, 1 5, 1 9, 5 9))"
            );
  }

  private void checkUnion(String wktCoverage, String wktExpected) {
    Geometry covGeom = read(wktCoverage);
    Geometry[] coverage = toArray(covGeom);
    Geometry actual = CoverageUnion.union(coverage);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }

  private static Geometry[] toArray(Geometry geom) {
    Geometry[] geoms = new Geometry[geom.getNumGeometries()];
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      geoms[i]= geom.getGeometryN(i);
    }
    return geoms;
  }
}
