/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.axis;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class ApproximateMedialAxisTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(ApproximateMedialAxisTest.class);
  }
  
  public ApproximateMedialAxisTest(String name) {
    super(name);
  }

  public void testQuad() {
    checkTree("POLYGON ((10 10, 30 30, 60 40, 90 70, 90 10, 10 10))"
        ,"GEOMETRYCOLLECTION (POLYGON ((10 10, 20 40, 90 10, 10 10)), POLYGON ((90 90, 20 40, 90 10, 90 90)))");
  }
  
  public void testRandom() {
    checkTree("POLYGON ((200 100, 100 100, 150 200, 250 250, 300 300, 360 400, 500 300, 400 250, 300 200, 300 150, 200 100))"
        ,"GEOMETRYCOLLECTION (POLYGON ((10 10, 20 40, 90 10, 10 10)), POLYGON ((90 90, 20 40, 90 10, 90 90)))");
  }
  
  private void checkTree(String wkt, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry actual = ApproximateMedialAxis.medialAxis(geom);
    Geometry expected = read(wktExpected);
    //checkEqual(expected, actual);
  }
}
