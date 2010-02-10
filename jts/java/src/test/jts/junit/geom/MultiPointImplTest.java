
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Test for com.vividsolutions.jts.geom.impl.MultiPointImpl.
 *
 * @version 1.7
 */
public class MultiPointImplTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel(1000);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public MultiPointImplTest(String name) { super(name); }

  public static Test suite() { return new TestSuite(MultiPointImplTest.class); }

/**
 * @todo Enable when #isSimple implemented
 */
//  public void testIsSimple1() throws Exception {
//    MultiPoint m = (MultiPoint) reader.read("MULTIPOINT(1.111 2.222, 3.333 4.444, 5.555 6.666)");
//    assertTrue(m.isSimple());
//  }

/**
 * @todo Enable when #isSimple implemented
 */
//  public void testIsSimple2() throws Exception {
//    MultiPoint m = (MultiPoint) reader.read("MULTIPOINT(1.111 2.222, 3.333 4.444, 3.333 4.444)");
//    assertTrue(! m.isSimple());
//  }

  public void testGetGeometryN() throws Exception {
    MultiPoint m = (MultiPoint) reader.read("MULTIPOINT(1.111 2.222, 3.333 4.444, 3.333 4.444)");
    Geometry g = m.getGeometryN(1);
    assertTrue(g instanceof Point);
    Point p = (Point) g;
    Coordinate externalCoordinate = new Coordinate();
    Coordinate internal = p.getCoordinate();
    externalCoordinate.x = internal.x;
    externalCoordinate.y = internal.y;
    assertEquals(3.333, externalCoordinate.x, 1E-10);
    assertEquals(4.444, externalCoordinate.y, 1E-10);
  }

  public void testGetEnvelope() throws Exception {
    MultiPoint m = (MultiPoint) reader.read("MULTIPOINT(1.111 2.222, 3.333 4.444, 3.333 4.444)");
    Envelope e = m.getEnvelopeInternal();
    assertEquals(1.111, e.getMinX(), 1E-10);
    assertEquals(3.333, e.getMaxX(), 1E-10);
    assertEquals(2.222, e.getMinY(), 1E-10);
    assertEquals(4.444, e.getMaxY(), 1E-10);
  }

  public void testEquals() throws Exception {
    MultiPoint m1 = (MultiPoint) reader.read("MULTIPOINT(5 6, 7 8)");
    MultiPoint m2 = (MultiPoint) reader.read("MULTIPOINT(5 6, 7 8)");
    assertTrue(m1.equals(m2));
  }

}
