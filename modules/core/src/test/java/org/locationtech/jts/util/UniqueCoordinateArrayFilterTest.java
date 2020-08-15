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

package org.locationtech.jts.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

/**
 * @version 1.7
 */
public class UniqueCoordinateArrayFilterTest
    extends GeometryTestCase
{
  public UniqueCoordinateArrayFilterTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(UniqueCoordinateArrayFilterTest.class);
  }

  public void testFilter() throws Exception {
    Geometry g = read(
          "MULTIPOINT(10 10, 20 20, 30 30, 20 20, 10 10)");
    UniqueCoordinateArrayFilter f = new UniqueCoordinateArrayFilter();
    g.apply(f);
    assertEquals(3, f.getCoordinates().length);
    assertEquals(new Coordinate(10, 10), f.getCoordinates()[0]);
    assertEquals(new Coordinate(20, 20), f.getCoordinates()[1]);
    assertEquals(new Coordinate(30, 30), f.getCoordinates()[2]);
  }

}
