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
package test.jts.junit.geom;


import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.geom.*;

/**
 * Unit tests for {@link Envelope}
 *
 * @author Martin Davis
 * @version 1.7
 */
public class CoordinateArraysTest extends TestCase {

  public static void main(String args[]) {
    TestRunner.run(CoordinateArraysTest.class);
  }

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
}