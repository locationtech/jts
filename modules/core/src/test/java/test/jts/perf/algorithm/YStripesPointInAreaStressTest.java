package test.jts.perf.algorithm;

import org.locationtech.jts.algorithm.locate.PointOnGeometryLocator;
import org.locationtech.jts.algorithm.locate.YStripesPointInAreaLocator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class YStripesPointInAreaStressTest extends TestCase {

	public static void main(String args[]) {
		TestRunner.run(YStripesPointInAreaStressTest.class);
	}

	PrecisionModel pmFixed_1 = new PrecisionModel(1.0);

	public YStripesPointInAreaStressTest(String name) {
		super(name);
	}

	public void testGrid() {
		// Use fixed PM to try and get at least some points hitting the boundary
		GeometryFactory geomFactory = new GeometryFactory(pmFixed_1);

		PerturbedGridPolygonBuilder gridBuilder = new PerturbedGridPolygonBuilder(geomFactory);
		gridBuilder.setNumLines(20);
		gridBuilder.setLineWidth(10.0);
		gridBuilder.setSeed(1185072199562L);
		Geometry area = gridBuilder.getGeometry();
 
		PointOnGeometryLocator pia = new YStripesPointInAreaLocator(area);

		PointInAreaStressTester gridTester = new PointInAreaStressTester(geomFactory, area);
		gridTester.setNumPoints(100000);
		gridTester.setPIA(pia);

		boolean isCorrect = gridTester.run();
		assertTrue(isCorrect);
	}
}
