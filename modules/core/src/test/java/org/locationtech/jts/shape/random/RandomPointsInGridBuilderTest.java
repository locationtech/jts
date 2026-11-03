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
 * Tests {@link RandomPointsInGridBuilder}.
 *
 * @author woogi-kim
 *
 */
public class RandomPointsInGridBuilderTest
extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(RandomPointsInGridBuilderTest.class);
  }

  public RandomPointsInGridBuilderTest(String name)
  {
    super(name);
  }

  public void testDefault() {
    RandomPointsInGridBuilder builder = new RandomPointsInGridBuilder(getGeometryFactory());
    builder.setExtent(new Envelope(0, 10, 0, 10));
    builder.setNumPoints(9);
    Geometry result = builder.getGeometry();
    assertEquals(9, result.getNumGeometries());
  }

  public void testReproducibleWithSameSeed() {
    Geometry result1 = createGridPoints(42, 25);
    Geometry result2 = createGridPoints(42, 25);
    assertTrue(result1.equalsExact(result2));
  }

  public void testDifferentSeedProducesDifferentResult() {
    Geometry result1 = createGridPoints(42, 25);
    Geometry result2 = createGridPoints(99, 25);
    assertFalse(result1.equalsExact(result2));
  }

  public void testReproducibleWithConstrainedToCircle() {
    Geometry result1 = createGridPointsInCircle(42, 25);
    Geometry result2 = createGridPointsInCircle(42, 25);
    assertTrue(result1.equalsExact(result2));
  }

  private Geometry createGridPoints(long seed, int numPts) {
    RandomPointsInGridBuilder builder = new RandomPointsInGridBuilder(getGeometryFactory());
    builder.setExtent(new Envelope(0, 10, 0, 10));
    builder.setNumPoints(numPts);
    builder.setRandom(new Random(seed));
    return builder.getGeometry();
  }

  private Geometry createGridPointsInCircle(long seed, int numPts) {
    RandomPointsInGridBuilder builder = new RandomPointsInGridBuilder(getGeometryFactory());
    builder.setExtent(new Envelope(0, 10, 0, 10));
    builder.setNumPoints(numPts);
    builder.setConstrainedToCircle(true);
    builder.setRandom(new Random(seed));
    return builder.getGeometry();
  }
}
