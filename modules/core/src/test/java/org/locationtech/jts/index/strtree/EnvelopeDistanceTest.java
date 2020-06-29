/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.index.strtree;
import org.locationtech.jts.geom.Envelope;

import junit.framework.TestCase;

/**
 * @version 1.17
 */
public class EnvelopeDistanceTest extends TestCase {

  public EnvelopeDistanceTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    String[] testCaseName = {EnvelopeDistanceTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }

  public void testDisjoint() {
    checkEnvelopeDistance(new Envelope(0, 10, 0, 10), new Envelope(20, 30, 20, 40), 50);
  }

  public void testOverlapping() {
    checkEnvelopeDistance(new Envelope(0, 30, 0, 30), new Envelope(20, 30, 20, 40), 50);
  }

  public void testCrossing() {
    checkEnvelopeDistance(new Envelope(0, 40, 10, 20), new Envelope(20, 30, 0, 30), 50);
  }

  public void testCrossing2() {
    checkEnvelopeDistance(new Envelope(0, 10, 4, 6), new Envelope(4, 6, 0, 10), 14.142135623730951);
  }

  private void checkEnvelopeDistance(Envelope env1, Envelope env2, double expected) {
    double result = EnvelopeDistance.maximumDistance(env1, env2);
    assertEquals(expected, result);
  }


}
