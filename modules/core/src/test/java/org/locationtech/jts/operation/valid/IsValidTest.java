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


}
