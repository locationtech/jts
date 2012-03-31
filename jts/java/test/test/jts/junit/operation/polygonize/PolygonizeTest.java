
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
package test.jts.junit.operation.polygonize;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.util.Assert;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;



/**
 * @version 1.7
 */
public class PolygonizeTest extends TestCase {
  private WKTReader reader = new WKTReader();

  public PolygonizeTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(PolygonizeTest.class);
  }

  public void test1() {
    doTest(new String[]{"LINESTRING EMPTY", "LINESTRING EMPTY"},
      new String[]{});
  }

  public void test2() {
    doTest(new String[]{
"LINESTRING (100 180, 20 20, 160 20, 100 180)",
"LINESTRING (100 180, 80 60, 120 60, 100 180)",
    },
    new String[]{
"POLYGON ((100 180, 120 60, 80 60, 100 180))",
"POLYGON ((100 180, 160 20, 20 20, 100 180), (100 180, 80 60, 120 60, 100 180))"
    });
  }

  public void test3() {
    doTest(new String[]{
        "LINESTRING (0 0, 4 0)",
        "LINESTRING (4 0, 5 3)",
"LINESTRING (5 3, 4 6, 6 6, 5 3)",
"LINESTRING (5 3, 6 0)",
"LINESTRING (6 0, 10 0, 5 10, 0 0)",
"LINESTRING (4 0, 6 0)"
    },
    new String[]{
"POLYGON ((5 3, 4 0, 0 0, 5 10, 10 0, 6 0, 5 3), (5 3, 6 6, 4 6, 5 3))",
"POLYGON ((5 3, 4 6, 6 6, 5 3))",
"POLYGON ((4 0, 5 3, 6 0, 4 0))"
    });
  }

/*
  public void test2() {
    doTest(new String[]{

"LINESTRING(20 20, 20 100)",
"LINESTRING  (20 100, 20 180, 100 180)",
"LINESTRING  (100 180, 180 180, 180 100)",
"LINESTRING  (180 100, 180 20, 100 20)",
"LINESTRING  (100 20, 20 20)",
"LINESTRING  (100 20, 20 100)",
"LINESTRING  (20 100, 100 180)",
"LINESTRING  (100 180, 180 100)",
"LINESTRING  (180 100, 100 20)"
    },
      new String[]{});
  }
*/

  private void doTest(String[] inputWKT, String[] expectedOutputWKT) {
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(toGeometries(inputWKT));
    compare(toGeometries(expectedOutputWKT), polygonizer.getPolygons());
  }

  private void compare(Collection expectedGeometries,
    Collection actualGeometries) {
    assertEquals("Geometry count - expected " + expectedGeometries.size()
        + " but actual was " + actualGeometries.size()
        + " in " + actualGeometries,
      expectedGeometries.size(), actualGeometries.size());
    for (Iterator i = expectedGeometries.iterator(); i.hasNext();) {
      Geometry expectedGeometry = (Geometry) i.next();
      assertTrue("Expected to find: " + expectedGeometry + " in Actual result:" + actualGeometries,
        contains(actualGeometries, expectedGeometry));
    }
  }

  private boolean contains(Collection geometries, Geometry g) {
    for (Iterator i = geometries.iterator(); i.hasNext();) {
      Geometry element = (Geometry) i.next();
      if (element.equalsNorm(g)) {
        return true;
      }
    }

    return false;
  }

  private Collection toGeometries(String[] inputWKT) {
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
