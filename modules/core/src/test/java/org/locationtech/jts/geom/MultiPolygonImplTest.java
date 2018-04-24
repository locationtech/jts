
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
 * Test for com.vividsolutions.jts.geom.impl.MultiPolygon.
 *
 * @version 1.7
 */
public class MultiPolygonImplTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel(1000);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public MultiPolygonImplTest(String name) { super(name); }

  public static Test suite() { return new TestSuite(MultiPolygonImplTest.class); }

  public void testCopy() throws Exception {
    MultiPolygon m = (MultiPolygon) reader.read("MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)),((15 5, 40 10, 10 20, 5 10, 15 5)))");
    m.setUserData(12);
    m.setSRID(4326);
    GeometryCollection copy = m.copy();
    assertEquals(m.getNumGeometries(), copy.getNumGeometries());
    for(int i=0; i<m.getNumGeometries(); i++){
      assertEquals(m.getGeometryN(i), copy.getGeometryN(i));
    }
    assertEquals(m.getUserData(), copy.getUserData());
    assertEquals(m.getSRID(), copy.getSRID());
    assertEquals(m.getFactory(), copy.getFactory());
  }

}
