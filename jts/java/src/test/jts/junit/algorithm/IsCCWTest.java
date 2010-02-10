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
package test.jts.junit.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;

/**
 * Tests CGAlgorithms.isCCW
 * @version 1.7
 */
public class IsCCWTest extends TestCase {

  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(IsCCWTest.class);
  }

  public IsCCWTest(String name) { super(name); }

  public void testCCW() throws Exception
  {
    Coordinate[] pts = getCoordinates("POLYGON ((60 180, 140 240, 140 240, 140 240, 200 180, 120 120, 60 180))");
    assertEquals(CGAlgorithms.isCCW(pts), false);

    Coordinate[] pts2 = getCoordinates("POLYGON ((60 180, 140 120, 100 180, 140 240, 60 180))");
    assertEquals(CGAlgorithms.isCCW(pts2), true);
    // same pts list with duplicate top point - check that isCCW still works
    Coordinate[] pts2x = getCoordinates("POLYGON ((60 180, 140 120, 100 180, 140 240, 140 240, 60 180))");
    assertEquals(CGAlgorithms.isCCW(pts2x), true);
  }

  private Coordinate[] getCoordinates(String wkt)
      throws ParseException
  {
    Geometry geom = reader.read(wkt);
    return geom.getCoordinates();
  }
}