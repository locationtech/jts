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

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Tests for {@link GeometryFactory}.
 *
 * @version 1.13
 */
public class GeometryFactoryTest extends TestCase {

  PrecisionModel precisionModel = new PrecisionModel();
  GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);

  public static void main(String args[]) {
    TestRunner.run(GeometryFactoryTest.class);
  }

  public GeometryFactoryTest(String name) { super(name); }

  public void testCreateGeometry() throws ParseException
  {
    checkCreateGeometryExact("POINT EMPTY");
    checkCreateGeometryExact("POINT ( 10 20 )");
    checkCreateGeometryExact("LINESTRING EMPTY");
    checkCreateGeometryExact("LINESTRING(0 0, 10 10)");
    checkCreateGeometryExact("MULTILINESTRING ((50 100, 100 200), (100 100, 150 200))");
    checkCreateGeometryExact("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    checkCreateGeometryExact("MULTIPOLYGON (((100 200, 200 200, 200 100, 100 100, 100 200)), ((300 200, 400 200, 400 100, 300 100, 300 200)))");
    checkCreateGeometryExact("GEOMETRYCOLLECTION (POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200)), LINESTRING (250 100, 350 200), POINT (350 150))");
  }
  
  public void testDeepCopy() throws ParseException
  {
    Point g = (Point) read("POINT ( 10 10) ");
    Geometry g2 = geometryFactory.createGeometry(g);
    g.getCoordinateSequence().setOrdinate(0, 0, 99);
    assertTrue(! g.equalsExact(g2));
  }
  
  public void testMultiPointCS()
  {
    GeometryFactory gf = new GeometryFactory(new PackedCoordinateSequenceFactory());
    CoordinateSequence mpSeq = gf.getCoordinateSequenceFactory().create(1, 4);
    mpSeq.setOrdinate(0, 0, 50);
    mpSeq.setOrdinate(0, 1, -2);
    mpSeq.setOrdinate(0, 2, 10);
    mpSeq.setOrdinate(0, 3, 20);
    
    MultiPoint mp = gf.createMultiPoint(mpSeq);
    CoordinateSequence pSeq = ((Point)mp.getGeometryN(0)).getCoordinateSequence();
    assertEquals(4, pSeq.getDimension());
    for (int i = 0; i < 4; i++)
      assertEquals(mpSeq.getOrdinate(0, i), pSeq.getOrdinate(0, i));
  }
  
  private void checkCreateGeometryExact(String wkt) throws ParseException
  {
    Geometry g = read(wkt);
    Geometry g2 = geometryFactory.createGeometry(g);
    assertTrue(g.equalsExact(g2));
  }
  
  private Geometry read(String wkt) throws ParseException
  {
    return reader.read(wkt);
  }
}
