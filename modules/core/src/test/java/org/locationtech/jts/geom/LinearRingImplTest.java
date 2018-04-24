
/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.geom;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.locationtech.jts.io.WKTReader;


/**
 * Test for com.vividsolutions.jts.geom.impl.LinerRing.
 *
 * @version 1.7
 */
public class LinearRingImplTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel(1000);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public LinearRingImplTest(String name) { super(name); }

  public static Test suite() { return new TestSuite(LinearRingImplTest.class); }

  public void testCopy() throws Exception {
    LinearRing l = (LinearRing) reader.read("LINEARRING(1.111 1.111, 2.222 2.222, 3.333 3.333, 1.111 1.111)");
    l.setUserData(12);
    l.setSRID(4326);
    LineString copy = l.copy();
    assertEquals(l.getCoordinates().length, copy.getCoordinates().length);
    for(int i=0; i<l.getCoordinates().length; i++){
      assertEquals(l.getCoordinates()[i], copy.getCoordinates()[i]);
    }
    assertEquals(l.getUserData(), copy.getUserData());
    assertEquals(l.getSRID(), copy.getSRID());
    assertEquals(l.getFactory(), copy.getFactory());
  }

}
