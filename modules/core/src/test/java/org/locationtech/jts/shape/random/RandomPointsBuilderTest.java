/*
 * Copyright (c) 2026 woogi-kim.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.shape.random;

import java.util.Random;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests {@link RandomPointsBuilder}.
 *
 * @author woogi-kim
 *
 */
public class RandomPointsBuilderTest
extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(RandomPointsBuilderTest.class);
  }

  public RandomPointsBuilderTest(String name)
  {
    super(name);
  }

  public void testDefault() {
    RandomPointsBuilder builder = new RandomPointsBuilder(getGeometryFactory());
    builder.setExtent(new Envelope(0, 10, 0, 10));
    builder.setNumPoints(10);
    Geometry result = builder.getGeometry();
    assertEquals(10, result.getNumGeometries());
  }

  public void testReproducibleWithSameSeed() {
    Geometry result1 = createPoints(42, 100);
    Geometry result2 = createPoints(42, 100);
    assertTrue(result1.equalsExact(result2));
  }

  public void testDifferentSeedProducesDifferentResult() {
    Geometry result1 = createPoints(42, 100);
    Geometry result2 = createPoints(99, 100);
    assertFalse(result1.equalsExact(result2));
  }

  public void testReproducibleWithPolygonalExtent() {
    Geometry mask = read("POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0))");
    Geometry result1 = createPointsInPolygon(mask, 42, 50);
    Geometry result2 = createPointsInPolygon(mask, 42, 50);
    assertTrue(result1.equalsExact(result2));
  }

  private Geometry createPoints(long seed, int numPts) {
    RandomPointsBuilder builder = new RandomPointsBuilder(getGeometryFactory());
    builder.setExtent(new Envelope(0, 10, 0, 10));
    builder.setNumPoints(numPts);
    builder.setRandom(new Random(seed));
    return builder.getGeometry();
  }

  private Geometry createPointsInPolygon(Geometry mask, long seed, int numPts) {
    RandomPointsBuilder builder = new RandomPointsBuilder(getGeometryFactory());
    builder.setExtent(mask);
    builder.setNumPoints(numPts);
    builder.setRandom(new Random(seed));
    return builder.getGeometry();
  }
}
