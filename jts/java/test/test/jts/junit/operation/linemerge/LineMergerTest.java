
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
package test.jts.junit.operation.linemerge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.util.Assert;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;



/**
 * @version 1.7
 */
public class LineMergerTest extends TestCase {
  private static WKTReader reader = new WKTReader();

  public LineMergerTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(LineMergerTest.class);
  }

  public void test1() {
    doTest(new String[] {
        "LINESTRING (120 120, 180 140)", "LINESTRING (200 180, 180 140)",
        "LINESTRING (200 180, 240 180)"
      }, new String[] { "LINESTRING (120 120, 180 140, 200 180, 240 180)" });    
  }
  
  public void test2() {
    doTest(new String[]{"LINESTRING (120 300, 80 340)",
      "LINESTRING (120 300, 140 320, 160 320)",
      "LINESTRING (40 320, 20 340, 0 320)",
      "LINESTRING (0 320, 20 300, 40 320)",
      "LINESTRING (40 320, 60 320, 80 340)",
      "LINESTRING (160 320, 180 340, 200 320)",
      "LINESTRING (200 320, 180 300, 160 320)"}, 
      new String[]{
      "LINESTRING (160 320, 180 340, 200 320, 180 300, 160 320)",
      "LINESTRING (40 320, 20 340, 0 320, 20 300, 40 320)",
      "LINESTRING (40 320, 60 320, 80 340, 120 300, 140 320, 160 320)"});
  }  
  
  public void test3() {
    doTest(new String[]{"LINESTRING (0 0, 100 100)", "LINESTRING (0 100, 100 0)"},
      new String[]{"LINESTRING (0 0, 100 100)", "LINESTRING (0 100, 100 0)"});
  }  
  
  public void test4() {
    doTest(new String[]{"LINESTRING EMPTY", "LINESTRING EMPTY"},
      new String[]{});
  }    
  
  public void test5() {
    doTest(new String[]{},
      new String[]{});
  }  

  public void testSingleUniquePoint() {
    doTest(new String[]{"LINESTRING (10642 31441, 10642 31441)", "LINESTRING EMPTY"},
      new String[]{});
  }    
  

  private void doTest(String[] inputWKT, String[] expectedOutputWKT) {
    doTest(inputWKT, expectedOutputWKT, true);
  }
  
  public static void doTest(String[] inputWKT, String[] expectedOutputWKT, boolean compareDirections) {
    LineMerger lineMerger = new LineMerger();
    lineMerger.add(toGeometries(inputWKT));
    compare(toGeometries(expectedOutputWKT), lineMerger.getMergedLineStrings(), compareDirections);
  }

  public static void compare(Collection expectedGeometries,
    Collection actualGeometries, boolean compareDirections) {
    assertEquals("Geometry count, " + actualGeometries,
      expectedGeometries.size(), actualGeometries.size());
    for (Iterator i = expectedGeometries.iterator(); i.hasNext();) {
      Geometry expectedGeometry = (Geometry) i.next();
      assertTrue("Not found: " + expectedGeometry + ", " + actualGeometries,
        contains(actualGeometries, expectedGeometry, compareDirections));
    }
  }

  private static boolean contains(Collection geometries, Geometry g, boolean exact) {
    for (Iterator i = geometries.iterator(); i.hasNext();) {
      Geometry element = (Geometry) i.next();
      if (exact && element.equalsExact(g)) {
        return true;
      }
      if (!exact && element.equalsTopo(g)) {
        return true;
      }      
    }

    return false;
  }

  public static Collection toGeometries(String[] inputWKT) {
    ArrayList geometries = new ArrayList();
    for (int i = 0; i < inputWKT.length; i++) {
      try {
        geometries.add(reader.read(inputWKT[i]));
      } catch (ParseException e) {
        Assert.shouldNeverReachHere();
      }
    }

    return geometries;
  }
}
