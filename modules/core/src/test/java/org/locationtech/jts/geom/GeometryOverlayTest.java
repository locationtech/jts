/*
 * Copyright (c) 2020 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests the behaviour of the {@link GeometryOverlay} class.
 * 
 * Currently does not test the reading of the system property.
 * 
 * @author mdavis
 *
 */
public class GeometryOverlayTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(GeometryOverlayTest.class);
  }

  public GeometryOverlayTest(String name) { super(name); }
  
  public void testOverlayNGFixed() {
    GeometryOverlay.setOverlayImpl(GeometryOverlay.OVERLAY_PROPERTY_VALUE_NG);
    PrecisionModel pmFixed = new PrecisionModel(1);
    Geometry expected = read("POLYGON ((1 2, 4 1, 1 1, 1 2))");
    
    checkIntersectionPM(pmFixed, expected);
  }

  public void testOverlayNGFloat() {
    GeometryOverlay.setOverlayImpl(GeometryOverlay.OVERLAY_PROPERTY_VALUE_NG);
    PrecisionModel pmFloat = new PrecisionModel();
    Geometry expected = read("POLYGON ((1 1, 1 2, 4 1.25, 4 1, 1 1))");
    
    checkIntersectionPM(pmFloat, expected);
  }

  private void checkIntersectionPM(PrecisionModel pmFixed, Geometry expected) {
    GeometryFactory geomFactFixed = new GeometryFactory(pmFixed);
    Geometry a = read(geomFactFixed, "POLYGON ((1 1, 1 2, 5 1, 1 1))");
    Geometry b = read(geomFactFixed, "POLYGON ((0 3, 4 3, 4 0, 0 0, 0 3))");
    Geometry actual = a.intersection(b);
    checkEqual(expected, actual);
  }
  
  public void testOverlayOld() {
    // must set overlay method explicitly since order of tests is not deterministic
    GeometryOverlay.setOverlayImpl(GeometryOverlay.OVERLAY_PROPERTY_VALUE_OLD);
    checkIntersectionFails();
  }

  public void testOverlayNG() {
    GeometryOverlay.setOverlayImpl(GeometryOverlay.OVERLAY_PROPERTY_VALUE_NG);
    checkIntersectionSucceeds();
  }

  private void checkIntersectionFails() {
    try {
      tryIntersection();
      fail("Intersection operation should have failed but did not");
    }
    catch (TopologyException ex) {
      // ignore - expected result
    }
  }

  private void checkIntersectionSucceeds() {
    try {
      tryIntersection();
    }
    catch (TopologyException ex) {
      fail("Intersection operation failed.");
    }
  }

  private void tryIntersection() {
    Geometry a = read("POLYGON ((-1120500.000000126 850931.058865365, -1120500.0000001257 851343.3885007716, -1120500.0000001257 851342.2386007707, -1120399.762684411 851199.4941312922, -1120500.000000126 850931.058865365))");
    Geometry b = read("POLYGON ((-1120500.000000126 851253.4627870625, -1120500.0000001257 851299.8179383819, -1120492.1498410008 851293.8417889411, -1120500.000000126 851253.4627870625))");
    Geometry result = a.intersection(b);
  }
}
