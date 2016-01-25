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
package com.vividsolutions.jts.geom;


import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link CoordinateArrays}
 *
 * @author Martin Davis
 * @version 1.7
 */
public class CoordinateArraysTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(CoordinateArraysTest.class);
  }

  private static Coordinate[] COORDS_1 = new Coordinate[] { new Coordinate(1, 1), new Coordinate(2, 2), new Coordinate(3, 3) };
  private static Coordinate[] COORDS_EMPTY = new Coordinate[0];
  
  public CoordinateArraysTest(String name) { super(name); }

  public void testPtNotInList1()
  {
    assertTrue(CoordinateArrays.ptNotInList(
        new Coordinate[] { new Coordinate(1, 1), new Coordinate(2, 2), new Coordinate(3, 3) },
        new Coordinate[] { new Coordinate(1, 1), new Coordinate(1, 2), new Coordinate(1, 3) }
        ).equals2D(new Coordinate(2, 2))
        );
  }
  public void testPtNotInList2()
  {
    assertTrue(CoordinateArrays.ptNotInList(
        new Coordinate[] { new Coordinate(1, 1), new Coordinate(2, 2), new Coordinate(3, 3) },
        new Coordinate[] { new Coordinate(1, 1), new Coordinate(2, 2), new Coordinate(3, 3) }
        ) == null
        );
  }
  public void testEnvelope1()
  {
    assertEquals( CoordinateArrays.envelope(COORDS_1),  new Envelope(1, 3, 1, 3) );
  }
  public void testEnvelopeEmpty()
  {
    assertEquals( CoordinateArrays.envelope(COORDS_EMPTY), new Envelope() );
  }
  public void testIntersection_envelope1()
  {
    assertTrue(CoordinateArrays.equals(
        CoordinateArrays.intersection(COORDS_1, new Envelope(1, 2, 1, 2)),
        new Coordinate[] { new Coordinate(1, 1), new Coordinate(2, 2) }
        ));
  }
  public void testIntersection_envelopeDisjoint()
  {
    assertTrue(CoordinateArrays.equals(
        CoordinateArrays.intersection(COORDS_1, new Envelope(10, 20, 10, 20)),  COORDS_EMPTY )
        );
  }
  public void testIntersection_empty_envelope()
  {
    assertTrue(CoordinateArrays.equals(
        CoordinateArrays.intersection(COORDS_EMPTY, new Envelope(1, 2, 1, 2)), COORDS_EMPTY )
        );
  }
  public void testIntersection_coords_emptyEnvelope()
  {
    assertTrue(CoordinateArrays.equals(
        CoordinateArrays.intersection(COORDS_1, new Envelope()), COORDS_EMPTY )
        );
  }
}