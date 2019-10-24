/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.geom;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


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

  public void testCreateEmpty() {
    checkEmpty( geometryFactory.createEmpty(0), Point.class);
    checkEmpty( geometryFactory.createEmpty(1), LineString.class);
    checkEmpty( geometryFactory.createEmpty(2), Polygon.class);

    checkEmpty( geometryFactory.createPoint(), Point.class);
    checkEmpty( geometryFactory.createLineString(), LineString.class);
    checkEmpty( geometryFactory.createPolygon(), Polygon.class);

    checkEmpty( geometryFactory.createMultiPoint(), MultiPoint.class);
    checkEmpty( geometryFactory.createMultiLineString(), MultiLineString.class);
    checkEmpty( geometryFactory.createMultiPolygon(), MultiPolygon.class);
    checkEmpty( geometryFactory.createGeometryCollection(), GeometryCollection.class);
  }

  private void checkEmpty(Geometry geom, Class clz) {
    assertTrue(geom.isEmpty());
    assertTrue( geom.getClass() == clz );
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

  /**
     * CoordinateArraySequences default their dimension to 3 unless explicitly told otherwise.
     * This test ensures that GeometryFactory.createGeometry() recreates the input dimension properly.
   *
   * @throws ParseException
   */
  public void testCopyGeometryWithNonDefaultDimension() throws ParseException
  {
    GeometryFactory gf = new GeometryFactory(CoordinateArraySequenceFactory.instance());
    CoordinateSequence mpSeq = gf.getCoordinateSequenceFactory().create(1, 2);
    mpSeq.setOrdinate(0, 0, 50);
    mpSeq.setOrdinate(0, 1, -2);

    Point g = gf.createPoint(mpSeq);
    CoordinateSequence pSeq = ((Point) g.getGeometryN(0)).getCoordinateSequence();
    assertEquals(2, pSeq.getDimension());

    Point g2 = (Point) geometryFactory.createGeometry(g);
    assertEquals(2, g2.getCoordinateSequence().getDimension());

  }

  public void testShellRingOrientationSetting() {

    // check default
    GeometryFactory gf = new GeometryFactory();
    assertEquals(GeometryFactory.RING_ORIENTATION_DONTCARE,  gf.getRingOrientationForPolygonShell());

    // Check for valid ring orientation values
    gf = new GeometryFactory();
    testShellRingOrientationSetter(gf, GeometryFactory.RING_ORIENTATION_DONTCARE, 0);
    gf = new GeometryFactory();
    testShellRingOrientationSetter(gf, GeometryFactory.RING_ORIENTATION_CW, 0);
    gf = new GeometryFactory();
    testShellRingOrientationSetter(gf, GeometryFactory.RING_ORIENTATION_RIGHT_HAND_RULE, 0);
    gf = new GeometryFactory();
    testShellRingOrientationSetter(gf, GeometryFactory.RING_ORIENTATION_CCW, 0);
    gf = new GeometryFactory();
    testShellRingOrientationSetter(gf, GeometryFactory.RING_ORIENTATION_LEFT_HAND_RULE, 0);

    // check for invalid arguments
    gf = new GeometryFactory();
    testShellRingOrientationSetter(gf, -1, 1);
    testShellRingOrientationSetter(gf, 3, 1);

    // check preventation of setting after first usage
    int ro = gf.getRingOrientationForPolygonShell();
    // don't fail if value does not change
    testShellRingOrientationSetter(gf, ro, 0);
    testShellRingOrientationSetter(gf, GeometryFactory.RING_ORIENTATION_LEFT_HAND_RULE, 2);
  }

  public void testShellRingOrientationEnforcement()
  {
    assertTrue(testShellRingOrientationEnforcement(GeometryFactory.RING_ORIENTATION_DONTCARE));
    assertTrue(testShellRingOrientationEnforcement(GeometryFactory.RING_ORIENTATION_CCW));
    assertTrue(testShellRingOrientationEnforcement(GeometryFactory.RING_ORIENTATION_CW));
  }

  private static boolean testShellRingOrientationEnforcement(int ringOrientation) {
    GeometryFactory gf = new GeometryFactory();
    gf.setRingOrientationForPolygonShell(ringOrientation);
    CoordinateSequenceFactory cf = gf.getCoordinateSequenceFactory();

    CoordinateSequence origShellSequence = createRing(cf, 0, ringOrientation);
    CoordinateSequence[] origHolesSequences = new CoordinateSequence[] {
      createRing(cf, 1, ringOrientation),
      createRing(cf, 2, ringOrientation) };

    LinearRing shell = gf.createLinearRing(origShellSequence.copy());
    LinearRing[] holes = new LinearRing[] {
      gf.createLinearRing(origHolesSequences[0].copy()),
      gf.createLinearRing(origHolesSequences[1].copy()) };

    Polygon polygon = gf.createPolygon(shell, holes);
    switch (ringOrientation) {
      case GeometryFactory.RING_ORIENTATION_CW:
        if (Orientation.isCCW(polygon.getExteriorRing().getCoordinateSequence()))
          return false;
        if (!Orientation.isCCW(polygon.getInteriorRingN(0).getCoordinateSequence()))
          return false;
        if (!Orientation.isCCW(polygon.getInteriorRingN(1).getCoordinateSequence()))
          return false;
        break;
      case GeometryFactory.RING_ORIENTATION_CCW:
        if (!Orientation.isCCW(polygon.getExteriorRing().getCoordinateSequence()))
          return false;
        if (Orientation.isCCW(polygon.getInteriorRingN(0).getCoordinateSequence()))
          return false;
        if (Orientation.isCCW(polygon.getInteriorRingN(1).getCoordinateSequence()))
          return false;
        break;
      case GeometryFactory.RING_ORIENTATION_DONTCARE:
        if (Orientation.isCCW(polygon.getExteriorRing().getCoordinateSequence()) !=
            Orientation.isCCW(origShellSequence))
          return false;
        if (Orientation.isCCW(polygon.getInteriorRingN(0).getCoordinateSequence()) !=
          Orientation.isCCW(origHolesSequences[0]))
          return false;
        if (Orientation.isCCW(polygon.getInteriorRingN(1).getCoordinateSequence()) !=
          Orientation.isCCW(origHolesSequences[1]))
          return false;
        break;
    }

    // Set up a polygon with hole using buffer
    Coordinate pt = new Coordinate(0, 0);
    Geometry c = gf.createPoint(pt);
    Geometry b = c.buffer(10d);
    Geometry s = c.buffer(6d);
    Polygon test = (Polygon)b.difference(s);

    if (ringOrientation == GeometryFactory.RING_ORIENTATION_CW) {
      if (Orientation.isCCW(test.getExteriorRing().getCoordinateSequence()))
        return false;
      if (!Orientation.isCCW(test.getInteriorRingN(0).getCoordinateSequence()))
        return false;
    } else if (ringOrientation == GeometryFactory.RING_ORIENTATION_CCW) {
      if (!Orientation.isCCW(test.getExteriorRing().getCoordinateSequence()))
        return false;
      if (Orientation.isCCW(test.getInteriorRingN(0).getCoordinateSequence()))
        return false;
    }
    return true;
  }

  private static Coordinate[] createCcwRing(int number) {
    if (number == 0)
      return new Coordinate[]{
        new Coordinate(0, 0), new Coordinate(10, 0),
        new Coordinate(10, 10), new Coordinate(0, 10),
        new Coordinate(0, 0)};
    if (number == 1)
      return new Coordinate[]{
        new Coordinate(2, 1), new Coordinate(9, 1),
        new Coordinate(9, 8), new Coordinate(2, 1)
      };

    return new Coordinate[]{
      new Coordinate(1, 2), new Coordinate(8, 9),
      new Coordinate(1, 9), new Coordinate(1, 2)
    };
  }

  private static boolean reverseSequence = false;

  private static CoordinateSequence createRing(CoordinateSequenceFactory factory, int ring, int orientation) {
    Coordinate[] coordinates = createCcwRing(ring);
    if (orientation == GeometryFactory.RING_ORIENTATION_CW)
      CoordinateArrays.reverse(coordinates);
    if (orientation == GeometryFactory.RING_ORIENTATION_DONTCARE)
    {
      if (reverseSequence) CoordinateArrays.reverse(coordinates);
      reverseSequence = !reverseSequence;
    }

    return factory.create(coordinates);
  }

  private static void testShellRingOrientationSetter(GeometryFactory gf, int ringOrientation, int failKind) {
    try {
      gf.setRingOrientationForPolygonShell(ringOrientation);
      if (failKind != 0) fail();
      assertEquals(ringOrientation, gf.getRingOrientationForPolygonShell());
    }
    catch (IllegalArgumentException iae)
    {
      if (failKind != 1) fail();
      return;
    }
    catch (IllegalStateException isa)
    {
      if (failKind != 2) fail();
      return;
    }
    catch (Exception e) {
      fail();
    }
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
