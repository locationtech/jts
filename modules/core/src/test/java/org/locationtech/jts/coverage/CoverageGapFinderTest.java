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

public class CoverageGapFinderTest extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(CoverageGapFinderTest.class);
  }
  
  public CoverageGapFinderTest(String name) {
    super(name);
  }
  
  public void testThreePolygonGap() {
    checkGaps(
        "MULTIPOLYGON (((1 5, 1 9, 5 9, 5 6, 3 5, 1 5)), ((5 9, 9 9, 9 5, 7 5, 5 6, 5 9)), ((1 1, 1 5, 3 5, 7 5, 9 5, 9 1, 1 1)))",
        1,
        "POLYGON ((3 5, 7 5, 5 6, 3 5))"
            );
  }

  private void checkGaps(String wktCoverage, double gapWidth, String wktExpected) {
    Geometry covGeom = read(wktCoverage);
    Geometry[] coverage = toArray(covGeom);
    Geometry actual = CoverageGapFinder.findGaps(coverage, gapWidth);
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
