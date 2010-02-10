
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;


/**
 * @version 1.7
 */
public class AreaLengthTest extends TestCase {

  private PrecisionModel precisionModel = new PrecisionModel();
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);
  
  private static final double TOLERANCE = 1E-5;
  
  public static void main(String args[]) {
    TestRunner.run(AreaLengthTest.class);
  }

  public AreaLengthTest(String name) { super(name); }

  public void testLength() throws Exception
  {
  	checkLength("MULTIPOINT (220 140, 180 280)", 0.0);
    checkLength("LINESTRING (220 140, 180 280)", 145.6021977);
    checkLength("LINESTRING (0 0, 100 100)", 141.4213562373095);
    checkLength("POLYGON ((20 20, 40 20, 40 40, 20 40, 20 20))", 80.0);
    checkLength("POLYGON ((20 20, 40 20, 40 40, 20 40, 20 20), (25 35, 35 35, 35 25, 25 25, 25 35))", 120.0);
  }

  public void testArea() throws Exception
  {
  	checkArea("MULTIPOINT (220 140, 180 280)", 0.0);
  	checkArea("LINESTRING (220 140, 180 280)", 0.0);
  	checkArea("POLYGON ((20 20, 40 20, 40 40, 20 40, 20 20))", 400.0);
  	checkArea("POLYGON ((20 20, 40 20, 40 40, 20 40, 20 20), (25 35, 35 35, 35 25, 25 25, 25 35))", 300.0);
  }

  public void checkLength(String wkt, double expectedValue) throws Exception {
		Geometry g = reader.read(wkt);
		double len = g.getLength();
//		System.out.println(len);
		assertEquals(expectedValue, len, TOLERANCE);
	}

	public void checkArea(String wkt, double expectedValue) throws Exception {
		Geometry g = reader.read(wkt);
		assertEquals(expectedValue, g.getArea(), TOLERANCE);
	}

}
