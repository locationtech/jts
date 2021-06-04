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
package org.locationtech.jts.operation.valid;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;



/**
 * @version 1.7
 */
public class IsValidTest extends TestCase {

  private PrecisionModel precisionModel = new PrecisionModel();
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(IsValidTest.class);
  }

  public IsValidTest(String name) { super(name); }

  public void testInvalidCoordinate() throws Exception
  {
    Coordinate badCoord = new Coordinate(1.0, Double.NaN);
    Coordinate[] pts = { new Coordinate(0.0, 0.0), badCoord };
    Geometry line = geometryFactory.createLineString(pts);
    IsValidOp isValidOp = new IsValidOp(line);
    boolean valid = isValidOp.isValid();
    TopologyValidationError err = isValidOp.getValidationError();
    Coordinate errCoord = err.getCoordinate();

    assertEquals(TopologyValidationError.INVALID_COORDINATE, err.getErrorType());
    assertTrue(Double.isNaN(errCoord.y));
    assertEquals(false, valid);
  }

  public void testZeroAreaPolygon() throws Exception {
    Geometry g = reader.read(
          "POLYGON((0 0, 0 0, 0 0, 0 0, 0 0))");
    g.isValid();
    assertTrue(true); //No exception thrown [Jon Aquino]
  }

  public void testLineString() throws Exception {
    Geometry g = reader.read(
          "LINESTRING(0 0, 0 0)");
    g.isValid();
    assertTrue(true); //No exception thrown [Jon Aquino]
  }
  
  public void testLinearRingTriangle() throws Exception {
    Geometry g = reader.read(
          "LINEARRING (100 100, 150 200, 200 100, 100 100)");
    assertTrue(g.isValid());
  }

  public void testLinearRingSelfCrossing() throws Exception {
    Geometry g = reader.read(
          "LINEARRING (150 100, 300 300, 100 300, 350 100, 150 100)");
    assertTrue(! g.isValid());
  }

  public void testLinearRingSelfCrossing2() throws Exception {
    Geometry g = reader.read(
          "LINEARRING (0 0, 100 100, 100 0, 0 100, 0 0)");
    assertTrue(! g.isValid());
  }

}
