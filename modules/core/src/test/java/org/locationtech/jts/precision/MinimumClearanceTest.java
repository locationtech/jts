/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.precision;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class MinimumClearanceTest extends TestCase {
  public static void main(String args[]) {
    TestRunner.run(MinimumClearanceTest.class);
  }
  
  private GeometryFactory geomFact = new GeometryFactory();
  private WKTReader reader = new WKTReader();

  public MinimumClearanceTest(String name) { super(name); }

  public void test2IdenticalPoints()
  throws ParseException
  {
    runTest("MULTIPOINT ((100 100), (100 100))", 1.7976931348623157E308);
  }
  
  public void test3Points()
  throws ParseException
  {
    runTest("MULTIPOINT ((100 100), (10 100), (30 100))", 20);
  }
  
  public void testTriangle()
  throws ParseException
  {
    runTest("POLYGON ((100 100, 300 100, 200 200, 100 100))", 100);
  }
  
  private void runTest(String wkt, double expectedValue)
  throws ParseException
  {
    Geometry g = reader.read(wkt);
    double rp = MinimumClearance.getDistance(g);
    assertEquals(expectedValue, rp);
  }
}
