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

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * @version 1.7
 */
public class EnvelopeTest extends TestCase {
	private PrecisionModel precisionModel = new PrecisionModel(1);

	private GeometryFactory geometryFactory = new GeometryFactory(precisionModel,
			0);

	WKTReader reader = new WKTReader(geometryFactory);

	public EnvelopeTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		TestRunner.run(EnvelopeTest.class);
	}

	public void testEverything() throws Exception {
		Envelope e1 = new Envelope();
		assertTrue(e1.isNull());
		assertEquals(0, e1.getWidth(), 1E-3);
		assertEquals(0, e1.getHeight(), 1E-3);
		e1.expandToInclude(100, 101);
		e1.expandToInclude(200, 202);
		e1.expandToInclude(150, 151);
		assertEquals(200, e1.getMaxX(), 1E-3);
		assertEquals(202, e1.getMaxY(), 1E-3);
		assertEquals(100, e1.getMinX(), 1E-3);
		assertEquals(101, e1.getMinY(), 1E-3);
		assertTrue(e1.contains(120, 120));
		assertTrue(e1.contains(120, 101));
		assertTrue(!e1.contains(120, 100));
		assertEquals(101, e1.getHeight(), 1E-3);
		assertEquals(100, e1.getWidth(), 1E-3);
		assertTrue(!e1.isNull());

		Envelope e2 = new Envelope(499, 500, 500, 501);
		assertTrue(!e1.contains(e2));
		assertTrue(!e1.intersects(e2));
		e1.expandToInclude(e2);
		assertTrue(e1.contains(e2));
		assertTrue(e1.intersects(e2));
		assertEquals(500, e1.getMaxX(), 1E-3);
		assertEquals(501, e1.getMaxY(), 1E-3);
		assertEquals(100, e1.getMinX(), 1E-3);
		assertEquals(101, e1.getMinY(), 1E-3);

		Envelope e3 = new Envelope(300, 700, 300, 700);
		assertTrue(!e1.contains(e3));
		assertTrue(e1.intersects(e3));

		Envelope e4 = new Envelope(300, 301, 300, 301);
		assertTrue(e1.contains(e4));
		assertTrue(e1.intersects(e4));
	}

	public void testIntersectsEmpty() {
		assertTrue(!new Envelope(-5, 5, -5, 5).intersects(new Envelope()));
		assertTrue(!new Envelope().intersects(new Envelope(-5, 5, -5, 5)));
		assertTrue(!new Envelope().intersects(new Envelope(100, 101, 100, 101)));
		assertTrue(!new Envelope(100, 101, 100, 101).intersects(new Envelope()));
	}

	public void testContainsEmpty() {
		assertTrue(!new Envelope(-5, 5, -5, 5).contains(new Envelope()));
		assertTrue(!new Envelope().contains(new Envelope(-5, 5, -5, 5)));
		assertTrue(!new Envelope().contains(new Envelope(100, 101, 100, 101)));
		assertTrue(!new Envelope(100, 101, 100, 101).contains(new Envelope()));
	}

	public void testExpandToIncludeEmpty() {
		assertEquals(new Envelope(-5, 5, -5, 5), expandToInclude(new Envelope(-5,
				5, -5, 5), new Envelope()));
		assertEquals(new Envelope(-5, 5, -5, 5), expandToInclude(new Envelope(),
				new Envelope(-5, 5, -5, 5)));
		assertEquals(new Envelope(100, 101, 100, 101), expandToInclude(
				new Envelope(), new Envelope(100, 101, 100, 101)));
		assertEquals(new Envelope(100, 101, 100, 101), expandToInclude(
				new Envelope(100, 101, 100, 101), new Envelope()));
	}

	private Envelope expandToInclude(Envelope a, Envelope b) {
		a.expandToInclude(b);
		return a;
	}

	public void testEmpty() {
		assertEquals(0, new Envelope().getHeight(), 0);
		assertEquals(0, new Envelope().getWidth(), 0);
		assertEquals(new Envelope(), new Envelope());
		Envelope e = new Envelope(100, 101, 100, 101);
		e.init(new Envelope());
		assertEquals(new Envelope(), e);
	}

	public void testAsGeometry() throws Exception {
		assertTrue(geometryFactory.createPoint((Coordinate) null).getEnvelope()
				.isEmpty());

		Geometry g = geometryFactory.createPoint(new Coordinate(5, 6))
				.getEnvelope();
		assertTrue(!g.isEmpty());
		assertTrue(g instanceof Point);

		Point p = (Point) g;
		assertEquals(5, p.getX(), 1E-1);
		assertEquals(6, p.getY(), 1E-1);

		LineString l = (LineString) reader.read("LINESTRING(10 10, 20 20, 30 40)");
		Geometry g2 = l.getEnvelope();
		assertTrue(!g2.isEmpty());
		assertTrue(g2 instanceof Polygon);

		Polygon poly = (Polygon) g2;
		poly.normalize();
		assertEquals(5, poly.getExteriorRing().getNumPoints());
		assertEquals(new Coordinate(10, 10), poly.getExteriorRing().getCoordinateN(
				0));
		assertEquals(new Coordinate(10, 40), poly.getExteriorRing().getCoordinateN(
				1));
		assertEquals(new Coordinate(30, 40), poly.getExteriorRing().getCoordinateN(
				2));
		assertEquals(new Coordinate(30, 10), poly.getExteriorRing().getCoordinateN(
				3));
		assertEquals(new Coordinate(10, 10), poly.getExteriorRing().getCoordinateN(
				4));
	}

	public void testSetToNull() throws Exception {
		Envelope e1 = new Envelope();
		assertTrue(e1.isNull());
		e1.expandToInclude(5, 5);
		assertTrue(!e1.isNull());
		e1.setToNull();
		assertTrue(e1.isNull());
	}

	public void testEquals() throws Exception {
		Envelope e1 = new Envelope(1, 2, 3, 4);
		Envelope e2 = new Envelope(1, 2, 3, 4);
		assertEquals(e1, e2);
		assertEquals(e1.hashCode(), e2.hashCode());

		Envelope e3 = new Envelope(1, 2, 3, 5);
		assertTrue(!e1.equals(e3));
		assertTrue(e1.hashCode() != e3.hashCode());
		e1.setToNull();
		assertTrue(!e1.equals(e2));
		assertTrue(e1.hashCode() != e2.hashCode());
		e2.setToNull();
		assertEquals(e1, e2);
		assertEquals(e1.hashCode(), e2.hashCode());
	}

	public void testEquals2() {
		assertTrue(new Envelope().equals(new Envelope()));
		assertTrue(new Envelope(1, 2, 1, 2).equals(new Envelope(1, 2, 1, 2)));
		assertTrue(!new Envelope(1, 2, 1.5, 2).equals(new Envelope(1, 2, 1, 2)));
	}

	public void testCopyConstructor() throws Exception {
		Envelope e1 = new Envelope(1, 2, 3, 4);
		Envelope e2 = new Envelope(e1);
		assertEquals(1, e2.getMinX(), 1E-5);
		assertEquals(2, e2.getMaxX(), 1E-5);
		assertEquals(3, e2.getMinY(), 1E-5);
		assertEquals(4, e2.getMaxY(), 1E-5);
	}

	public void testGeometryFactoryCreateEnvelope()
	throws Exception
	{
		checkExpectedEnvelopeGeometry("POINT (0 0)");
		checkExpectedEnvelopeGeometry("POINT (100 13)");
		checkExpectedEnvelopeGeometry("LINESTRING (0 0, 0 10)");
		checkExpectedEnvelopeGeometry("LINESTRING (0 0, 10 0)");
		
		String poly10 = "POLYGON ((0 10, 10 10, 10 0, 0 0, 0 10))";
		checkExpectedEnvelopeGeometry(poly10);
		
		checkExpectedEnvelopeGeometry("LINESTRING (0 0, 10 10)",
				poly10);
		checkExpectedEnvelopeGeometry("POLYGON ((5 10, 10 6, 5 0, 0 6, 5 10))",
				poly10);

		
	}

	void checkExpectedEnvelopeGeometry(String wktInput)
	throws ParseException
	{
		checkExpectedEnvelopeGeometry(wktInput, wktInput);
	}
	
	void checkExpectedEnvelopeGeometry(String wktInput, String wktEnvGeomExpected)
		throws ParseException
	{
		Geometry input = reader.read(wktInput);
		Geometry envGeomExpected = reader.read(wktEnvGeomExpected);
		
		Envelope env = input.getEnvelopeInternal();
		Geometry envGeomActual = geometryFactory.toGeometry(env);
		boolean isEqual = envGeomActual.equalsNorm(envGeomExpected);
		assertTrue(isEqual);
	}
	
	public void testCompareTo()
	{
	  checkCompareTo(0, new Envelope(), new Envelope());
	  checkCompareTo(0, new Envelope(1,2, 1,2), new Envelope(1,2, 1,2));
	  checkCompareTo(1, new Envelope(2,3, 1,2), new Envelope(1,2, 1,2));
	  checkCompareTo(-1, new Envelope(1,2, 1,2), new Envelope(2,3, 1,2));
	  checkCompareTo(1, new Envelope(1,2, 1,3), new Envelope(1,2, 1,2));
	  checkCompareTo(1, new Envelope(2,3, 1,3), new Envelope(1,3, 1,2));
	}
	
	public void checkCompareTo(int expected, Envelope env1, Envelope env2)
	{
          assertTrue(expected == env1.compareTo(env2));
          assertTrue(-expected == env2.compareTo(env1));
	}
}
