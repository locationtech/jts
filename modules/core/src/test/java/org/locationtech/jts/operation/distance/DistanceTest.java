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
package org.locationtech.jts.operation.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;

/**
 * @version 1.7
 */
public class DistanceTest extends BaseDistanceTest {

  public static void main(String args[]) {
    TestRunner.run(DistanceTest.class);
  }

  public DistanceTest(String name) { super(name); }

  @Override
  protected double distance(Geometry g1, Geometry g2) {
    return g1.distance(g2);
  }

  @Override
  protected boolean isWithinDistance(Geometry g1, Geometry g2, double distance) {
    return g1.isWithinDistance(g2, distance);
  }

  @Override
  protected Coordinate[] nearestPoints(Geometry g1, Geometry g2) {
    return DistanceOp.nearestPoints(g1, g2);
  }  
}
