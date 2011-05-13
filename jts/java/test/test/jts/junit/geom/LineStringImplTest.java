
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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Test for com.vividsolutions.jts.geom.impl.LineStringImpl.
 *
 * @version 1.7
 */
public class LineStringImplTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel(1000);
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public LineStringImplTest(String name) { super(name); }

  public static Test suite() { return new TestSuite(LineStringImplTest.class); }

  public void testIsSimple() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING (0 0, 10 10, 10 0, 0 10, 0 0)");
    assertTrue(! l1.isSimple());
    LineString l2 = (LineString) reader.read("LINESTRING (0 0, 10 10, 10 0, 0 10)");
    assertTrue(! l2.isSimple());
  }

  public void testIsCoordinate() throws Exception {
    LineString l = (LineString) reader.read("LINESTRING (0 0, 10 10, 10 0)");
    assertTrue(l.isCoordinate(new Coordinate(0, 0)));
    assertTrue(! l.isCoordinate(new Coordinate(5, 0)));
  }

  public void testUnclosedLinearRing() {
      try {
      geometryFactory.createLinearRing(new Coordinate[]{
          new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 1), new Coordinate(2, 1)});
      assertTrue(false);
      }
      catch (Exception e) {
          assertTrue(e instanceof IllegalArgumentException);
      }
  }

  public void testEquals1() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    assertTrue(l1.equals(l2));
  }

  public void testEquals2() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(3.333 4.444, 1.111 2.222)");
    assertTrue(l1.equals(l2));
  }

  public void testEquals3() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(3.333 4.443, 1.111 2.222)");
    assertTrue(! l1.equals(l2));
  }

  public void testEquals4() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(3.333 4.4445, 1.111 2.222)");
    assertTrue(! l1.equals(l2));
  }

  public void testEquals5() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(3.333 4.4446, 1.111 2.222)");
    assertTrue(! l1.equals(l2));
  }

  public void testEquals6() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444, 5.555 6.666)");
    LineString l2 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444, 5.555 6.666)");
    assertTrue(l1.equals(l2));
  }

  public void testEquals7() throws Exception {
    LineString l1 = (LineString) reader.read("LINESTRING(1.111 2.222, 5.555 6.666, 3.333 4.444)");
    LineString l2 = (LineString) reader.read("LINESTRING(1.111 2.222, 3.333 4.444, 5.555 6.666)");
    assertTrue(!l1.equals(l2));
  }

  public void testGetCoordinates() throws Exception {
    LineString l = (LineString) reader.read("LINESTRING(1.111 2.222, 5.555 6.666, 3.333 4.444)");
    Coordinate[] coordinates = l.getCoordinates();
    assertEquals(new Coordinate(5.555, 6.666), coordinates[1]);
  }

  public void testIsClosed() throws Exception {
    LineString l = (LineString) reader.read("LINESTRING EMPTY");
    assertTrue(l.isEmpty());
    assertTrue(! l.isClosed());

    LinearRing r = geometryFactory.createLinearRing((CoordinateSequence)null);
    assertTrue(r.isEmpty());
    assertTrue(r.isClosed());

    MultiLineString m = geometryFactory.createMultiLineString(
          new LineString[] {l, r});
    assertTrue(! m.isClosed());

    MultiLineString m2 = geometryFactory.createMultiLineString(
          new LineString[] {r});
    assertTrue(! m2.isClosed());
  }

  public void testGetGeometryType() throws Exception {
    LineString l = (LineString) reader.read("LINESTRING EMPTY");
    assertEquals("LineString", l.getGeometryType());
  }

  public void testEquals8() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1000), 0));
    MultiLineString l1 = (MultiLineString) reader.read("MULTILINESTRING((1732328800 519578384, 1732026179 519976285, 1731627364 519674014, 1731929984 519276112, 1732328800 519578384))");
    MultiLineString l2 = (MultiLineString) reader.read("MULTILINESTRING((1731627364 519674014, 1731929984 519276112, 1732328800 519578384, 1732026179 519976285, 1731627364 519674014))");
    assertTrue(l1.equals(l2));
  }

  public void testEquals9() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    MultiLineString l1 = (MultiLineString) reader.read("MULTILINESTRING((1732328800 519578384, 1732026179 519976285, 1731627364 519674014, 1731929984 519276112, 1732328800 519578384))");
    MultiLineString l2 = (MultiLineString) reader.read("MULTILINESTRING((1731627364 519674014, 1731929984 519276112, 1732328800 519578384, 1732026179 519976285, 1731627364 519674014))");
    assertTrue(l1.equals(l2));
  }

  public void testEquals10() throws Exception {
    WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(1), 0));
    Geometry l1 = reader.read("POLYGON((1732328800 519578384, 1732026179 519976285, 1731627364 519674014, 1731929984 519276112, 1732328800 519578384))");
    Geometry l2 = reader.read("POLYGON((1731627364 519674014, 1731929984 519276112, 1732328800 519578384, 1732026179 519976285, 1731627364 519674014))");
    l1.normalize();
    l2.normalize();
    assertTrue(l1.equalsExact(l2));
  }

  public void testFiveZeros() {
    LineString ls = new GeometryFactory().createLineString(new Coordinate[]{
              new Coordinate(0, 0),
              new Coordinate(0, 0),
              new Coordinate(0, 0),
              new Coordinate(0, 0),
              new Coordinate(0, 0)});
    assertTrue(ls.isClosed());
  }

  public void testLinearRingConstructor() throws Exception {
    try {
      LinearRing ring =
        new GeometryFactory().createLinearRing(
          new Coordinate[] {
            new Coordinate(0, 0),
            new Coordinate(10, 10),
            new Coordinate(0, 0)});
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

}
