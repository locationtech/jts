
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
package test.jts.junit.precision;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.precision.SimpleGeometryPrecisionReducer;


/**
 * @version 1.7
 */
public class SimpleGeometryPrecisionReducerTest
    extends TestCase
{
  private PrecisionModel pmFloat = new PrecisionModel();
  private PrecisionModel pmFixed1 = new PrecisionModel(1);
  private SimpleGeometryPrecisionReducer reducer = new SimpleGeometryPrecisionReducer(pmFixed1);
  private SimpleGeometryPrecisionReducer reducerKeepCollapse
      = new SimpleGeometryPrecisionReducer(pmFixed1);

  private GeometryFactory gfFloat = new GeometryFactory(pmFloat, 0);
  WKTReader reader = new WKTReader(gfFloat);

  public static void main(String args[]) {
    TestRunner.run(SimpleGeometryPrecisionReducerTest.class);
  }

  public SimpleGeometryPrecisionReducerTest(String name)
  {
      super(name);
      reducerKeepCollapse.setRemoveCollapsedComponents(false);

  }

  public void testSquare()
      throws Exception
  {
    Geometry g = reader.read("POLYGON (( 0 0, 0 1.4, 1.4 1.4, 1.4 0, 0 0 ))");
    Geometry g2 = reader.read("POLYGON (( 0 0, 0 1, 1 1, 1 0, 0 0 ))");
    Geometry gReduce = reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testTinySquareCollapse()
      throws Exception
  {
    Geometry g = reader.read("POLYGON (( 0 0, 0 .4, .4 .4, .4 0, 0 0 ))");
    Geometry g2 = reader.read("POLYGON EMPTY");
    Geometry gReduce = reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testSquareCollapse()
      throws Exception
  {
    Geometry g = reader.read("POLYGON (( 0 0, 0 1.4, .4 .4, .4 0, 0 0 ))");
    Geometry g2 = reader.read("POLYGON EMPTY");
    Geometry gReduce = reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testSquareKeepCollapse()
      throws Exception
  {
    Geometry g = reader.read("POLYGON (( 0 0, 0 1.4, .4 .4, .4 0, 0 0 ))");
    Geometry g2 = reader.read("POLYGON (( 0 0, 0 1, 0 0, 0 0, 0 0 ))");
    Geometry gReduce = reducerKeepCollapse.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testLine()
      throws Exception
  {
    Geometry g = reader.read("LINESTRING ( 0 0, 0 1.4 )");
    Geometry g2 = reader.read("LINESTRING (0 0, 0 1)");
    Geometry gReduce = reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testLineRemoveCollapse()
      throws Exception
  {
    Geometry g = reader.read("LINESTRING ( 0 0, 0 .4 )");
    Geometry g2 = reader.read("LINESTRING EMPTY");
    Geometry gReduce = reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testLineKeepCollapse()
      throws Exception
  {
    Geometry g = reader.read("LINESTRING ( 0 0, 0 .4 )");
    Geometry g2 = reader.read("LINESTRING ( 0 0, 0 0 )");
    Geometry gReduce = reducerKeepCollapse.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }



}
