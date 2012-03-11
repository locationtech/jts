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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @version 1.7
 */
public class TriangleTest extends TestCase
{

  private PrecisionModel precisionModel = new PrecisionModel();

  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel,
      0);

  WKTReader reader = new WKTReader(geometryFactory);

  private static final double TOLERANCE = 1E-5;

  public static void main(String args[])
  {
    TestRunner.run(TriangleTest.class);
  }

  public TriangleTest(String name)
  {
    super(name);
  }

  public void testInterpolateZ() throws Exception
  {
    checkZ("LINESTRING(1 1 0, 2 1 0, 1 2 10)", new Coordinate(1.5, 1.5), 5);
    checkZ("LINESTRING(1 1 0, 2 1 0, 1 2 10)", new Coordinate(1.2, 1.2), 2);
    checkZ("LINESTRING(1 1 0, 2 1 0, 1 2 10)", new Coordinate(0, 0), -10);
  }

  public void checkZ(String wkt, Coordinate p, double expectedValue)
      throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    double z = Triangle.interpolateZ(p, pt[0], pt[1], pt[2]);
    System.out.println("Z = " + z);
    assertEquals(expectedValue, z, 0.000001);
  }

  public void testArea3D() throws Exception
  {
    checkArea3D("POLYGON((0 0 10, 100 0 110, 100 100 110, 0 0 10))",
        7071.067811865475);
    checkArea3D("POLYGON((0 0 10, 100 0 10, 50 100 110, 0 0 10))",
        7071.067811865475);
  }

  public void checkArea3D(String wkt, double expectedValue) throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();
    double area3D = Triangle.area3D(pt[0], pt[1], pt[2]);
    // System.out.println("area3D = " + area3D);
    assertEquals(expectedValue, area3D, TOLERANCE);
  }

  public void testArea() throws Exception
  {
    // CW
    checkArea("POLYGON((10 10, 20 20, 20 10, 10 10))", 50);
    // CCW
    checkArea("POLYGON((10 10, 20 10, 20 20, 10 10))", -50);
    // degenerate point triangle
    checkArea("POLYGON((10 10, 10 10, 10 10, 10 10))", 0);
    // degenerate line triangle
    checkArea("POLYGON((10 10, 20 10, 15 10, 10 10))", 0);
  }

  public void checkArea(String wkt, double expectedValue) throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    double signedArea = Triangle.signedArea(pt[0], pt[1], pt[2]);
    System.out.println("signed area = " + signedArea);
    assertEquals(expectedValue, signedArea, TOLERANCE);

    double area = Triangle.area(pt[0], pt[1], pt[2]);
    assertEquals(Math.abs(expectedValue), area, TOLERANCE);

  }

  public void testAcute() throws Exception
  {
    // right triangle
    checkAcute("POLYGON((10 10, 20 20, 20 10, 10 10))", false);
    // CCW right tri
    checkAcute("POLYGON((10 10, 20 10, 20 20, 10 10))", false);
    // acute
    checkAcute("POLYGON((10 10, 20 10, 15 20, 10 10))", true);
  }

  public void checkAcute(String wkt, boolean expectedValue) throws Exception
  {
    Geometry g = reader.read(wkt);
    Coordinate[] pt = g.getCoordinates();

    boolean isAcute = Triangle.isAcute(pt[0], pt[1], pt[2]);
    System.out.println("isAcute = " + isAcute);
    assertEquals(expectedValue, isAcute);
  }


}
