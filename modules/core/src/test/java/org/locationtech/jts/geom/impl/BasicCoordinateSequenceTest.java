
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package org.locationtech.jts.geom.impl;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;

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
        CoordinateSequence s2 = (CoordinateSequence) s1.clone();
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

    CoordinateSequence s2 = (CoordinateSequence) s1.clone();
    assertTrue(s1.getDimension() == s2.getDimension());
    assertTrue(s1.getCoordinate(0).equals(s2.getCoordinate(0)));
    assertTrue(s1.getCoordinate(0) != s2.getCoordinate(0));
  }
}
