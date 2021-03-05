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

package org.locationtech.jtslab.clean;

import junit.framework.TestCase;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class InvalidHoleRemoverTest extends TestCase {

  private WKTReader reader = new WKTReader();

  public InvalidHoleRemoverTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(InvalidHoleRemoverTest.class);
  }

  public void testNoHole() {
    checkHolesRemoved("POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))", 
        "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))");
  }  
  public void testOneValid() {
    checkHolesRemoved("POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (5 5, 5 2, 8 2, 5 5))", 
        "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (5 5, 5 2, 8 2, 5 5))");
  }
  public void testOneOutside() {
    checkHolesRemoved("POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (11 5, 11 2, 14 2, 11 5))", 
        "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))");
  }
  public void testOneValidOneOutside() {
    checkHolesRemoved("POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (11 5, 11 2, 14 2, 11 5), (2 5, 2 2, 5 2, 2 5))", 
        "POLYGON ((1 1, 1 9, 9 9, 9 1, 1 1), (2 2, 5 2, 2 5, 2 2))");
  }

  public void testOneOverlapping() {
    checkHolesRemoved("POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (2 5, 2 2, 12 2, 2 5))", 
        "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))");
  }

  public void testOneOverlappingMP() {
    checkHolesRemoved("MULTIPOLYGON (((1 9, 9 9, 9 1, 1 1, 1 9), (2 5, 2 2, 12 2, 2 5)), ((21 9, 25 9, 25 5, 21 5, 21 9)))", 
        "MULTIPOLYGON (((1 9, 9 9, 9 1, 1 1, 1 9)), ((21 9, 25 9, 25 5, 21 5, 21 9)))");
  }

  public void testOneOverlappingGC() {
    checkHolesRemoved("GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (2 5, 2 2, 12 2, 2 5)), LINESTRING (15 9, 19 5))", 
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9)), LINESTRING (15 9, 19 5))");
  }

  private void checkHolesRemoved(String inputWKT, String expectedWKT) {
    Geometry input = read(inputWKT);
    Geometry expected = read(expectedWKT);
    
    Geometry actual = InvalidHoleRemover.clean(input);
    checkEqual(expected, actual);
  }

  private void checkEqual(Geometry expected, Geometry actual) {
    Geometry actualNorm = actual.norm();
    boolean equal = actualNorm.equalsExact(expected.norm());
    if (! equal) {
      System.out.println("FAIL - Expected = " + expected
          + " actual = " + actual.norm());
    }
    assertTrue(equal);
  }

  private Geometry read(String wkt) {
    try {
       return reader.read(wkt);
    } catch (ParseException e) {
      throw new RuntimeException(e.getMessage());
    }
    
  }

}
