package org.locationtech.jts.algorithm.locate;

import org.locationtech.jts.algorithm.AbstractPointInRingTest;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import junit.textui.TestRunner;

/**
 * Tests IndexedPointInAreaLocator algorithms
 *
 * @version 1.7
 */
public class YStripesPointInAreaLocatorTest extends AbstractPointInRingTest {

	private WKTReader reader = new WKTReader();

	public static void main(String args[]) {
		TestRunner.run(YStripesPointInAreaLocatorTest.class);
	}

	public YStripesPointInAreaLocatorTest(String name) {
		super(name);
	}

	protected void runPtInRing(int expectedLoc, Coordinate pt, String wkt) throws Exception {
		Geometry geom = reader.read(wkt);
		PointOnGeometryLocator loc = new YStripesPointInAreaLocator(geom);
		int result = loc.locate(pt);
		System.out.println(String.format("expected %s. actual %s", expectedLoc, result));
		assertEquals(expectedLoc, result);
	}

}
