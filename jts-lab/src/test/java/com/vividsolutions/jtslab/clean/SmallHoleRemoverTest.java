/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtslab.clean;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class SmallHoleRemoverTest extends TestCase {

  private WKTReader reader = new WKTReader();

  public SmallHoleRemoverTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(SmallHoleRemoverTest.class);
  }

  public void testNoHole() {
    checkHolesRemoved("POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))", 
        "POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9))");
  }  
  public void testOneLarge() {
    checkHolesRemoved("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200), (130 180, 175 180, 175 136, 130 136, 130 180))", 
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200), (130 180, 175 180, 175 136, 130 136, 130 180))");
  }
  public void testOneSmall() {
    checkHolesRemoved("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200), (130 160, 140 150, 130 150, 130 160))", 
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
  }
  public void testOneLargeOneSmall() {
    checkHolesRemoved("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200), (130 160, 140 150, 130 150, 130 160), (150 190, 190 190, 190 150, 150 150, 150 190))", 
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200), (150 190, 190 190, 190 150, 150 150, 150 190))");
  }


  public void testOneSmallMP() {
    checkHolesRemoved("MULTIPOLYGON (((1 9, 9 9, 9 1, 1 1, 1 9), (2 5, 2 2, 12 2, 2 5)), ((21 9, 25 9, 25 5, 21 5, 21 9)))", 
        "MULTIPOLYGON (((1 9, 9 9, 9 1, 1 1, 1 9)), ((21 9, 25 9, 25 5, 21 5, 21 9)))");
  }

  public void testOneSmallGC() {
    checkHolesRemoved("GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9), (2 5, 2 2, 12 2, 2 5)), LINESTRING (15 9, 19 5))", 
        "GEOMETRYCOLLECTION (POLYGON ((1 9, 9 9, 9 1, 1 1, 1 9)), LINESTRING (15 9, 19 5))");
  }

  private void checkHolesRemoved(String inputWKT, String expectedWKT) {
    Geometry input = read(inputWKT);
    Geometry expected = read(expectedWKT);
    
    Geometry actual = SmallHoleRemover.clean(input, 100);
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
