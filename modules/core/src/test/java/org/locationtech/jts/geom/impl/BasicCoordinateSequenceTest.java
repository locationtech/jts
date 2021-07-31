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
package org.locationtech.jts.geom.impl;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import junit.framework.TestCase;



/**
 * @version 1.7
 */
public class BasicCoordinateSequenceTest extends TestCase {
    public BasicCoordinateSequenceTest(String name) {
        super(name);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(BasicCoordinateSequenceTest.class);
    }
    public void testClone() {
        CoordinateSequence s1 = CoordinateArraySequenceFactory.instance().create(
            new Coordinate[] { new Coordinate(1, 2), new Coordinate(3, 4)});
        CoordinateSequence s2 = (CoordinateSequence) s1.copy();
        assertTrue(s1.getCoordinate(0).equals(s2.getCoordinate(0)));
        assertTrue(s1.getCoordinate(0) != s2.getCoordinate(0));
    }

  public void testCloneDimension2() {
    CoordinateSequence s1 = CoordinateArraySequenceFactory.instance()
        .create( 2, 2 );
    s1.setOrdinate(0, 0, 1);
    s1.setOrdinate(0, 1, 2);
    s1.setOrdinate(1, 0, 3);
    s1.setOrdinate(1, 1, 4);

    CoordinateSequence s2 = (CoordinateSequence) s1.copy();
    assertTrue(s1.getDimension() == s2.getDimension());
    assertTrue(s1.getCoordinate(0).equals(s2.getCoordinate(0)));
    assertTrue(s1.getCoordinate(0) != s2.getCoordinate(0));
  }
  
  /**
   * A simple test that using CoordinateXYM works
   * for creation and running a basic function.
   */
  public void testLengthWithXYM() {
    CoordinateXYM[] coords = new CoordinateXYM[2];

    coords[0] = new CoordinateXYM(1, 1, 1);
    coords[1] = new CoordinateXYM(2, 1, 2);

    GeometryFactory factory = new GeometryFactory();
    LineString line = factory.createLineString(coords);

    double len = line.getLength();
    assertEquals(1.0, len);
  }
}
