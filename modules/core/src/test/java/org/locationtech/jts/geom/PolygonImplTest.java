
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
 * Test for com.vividsolutions.jts.geom.impl.PolygonImpl.
 *
 * @version 1.7
 */
public class PolygonImplTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel(1000);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public PolygonImplTest(String name) { super(name); }

  public static Test suite() { return new TestSuite(PolygonImplTest.class); }

  public void testCopy() throws Exception {
    Polygon p = (Polygon) reader.read("POLYGON((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))");
    p.setUserData(12);
    p.setSRID(4326);
    Polygon copy = p.copy();
    assertEquals(p.getExteriorRing(), copy.getExteriorRing());
    assertEquals(p.getNumInteriorRing(), copy.getNumInteriorRing());
    for(int i=0; i< p.getNumInteriorRing(); i++) {
      assertEquals(p.getInteriorRingN(i), copy.getInteriorRingN(i));
    }
    assertEquals(p.getUserData(), copy.getUserData());
    assertEquals(p.getSRID(), copy.getSRID());
    assertEquals(p.getFactory(), copy.getFactory());
  }

}
