package test.jts.perf.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.math.DD;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Test the accuracy of DD orientation index computation,
 * using the built-in double value conversion and an experimental
 * conversion approach with better decimal accuracy.
 * 
 * @author Martin Davis
 *
 */
public class DDOrientationIndexAccuracyTest extends GeometryTestCase {

	public static void main(String args[]) {
		TestRunner.run(DDOrientationIndexAccuracyTest.class);
	}

	public DDOrientationIndexAccuracyTest(String name) {
		super(name);
	}

	public void testRightTriangleForDeterminant() {
		checkLine45(1, 100, 100);
	}

	private void checkLine45(int width, int nPts, double precision) {
		Coordinate p1 = new Coordinate(0, width);
		Coordinate p2 = new Coordinate(width, 0);
		for (int i = 0; i <= nPts; i++) {
			double d = width / (double) nPts;
			Coordinate q = new Coordinate(0.0 + i * d, width - i * d);
			PrecisionModel pm = new PrecisionModel(precision);
			pm.makePrecise(q);
			checkPointOnSeg(p1, p2, q);
		}
	}
	


	private void checkPointOnSeg(Coordinate p1, Coordinate p2, Coordinate q) {
		System.out.println("  Pt: " + WKTWriter.toPoint(q) + "  seg: " + WKTWriter.toLineString(p1, p2)
		+ " --- DDstd = " + orientationDet(p1, p2, q, DD_STD)
		+ " --- DDdec = " + orientationDet(p1, p2, q, DD_DEC)
			);
	}

	public static DD orientationDet(Coordinate p1, Coordinate p2, Coordinate q, DDConverter conv) {
		// normalize coordinates
		DD dx1 = conv.convert(p2.x).selfAdd(conv.convert(-p1.x));
		DD dy1 = conv.convert(p2.y).selfAdd(conv.convert(-p1.y));
		DD dx2 = conv.convert(q.x).selfAdd(conv.convert(-p2.x));
		DD dy2 = conv.convert(q.y).selfAdd(conv.convert(-p2.y));

		// sign of determinant - unrolled for performance
		return dx1.selfMultiply(dy2).selfSubtract(dy1.selfMultiply(dx2));
	}

	private static final boolean USE_ACCURATE_CONVERSION = false;
	
	private static DD convertToDD(double x) {
		if (USE_ACCURATE_CONVERSION) {
		// convert more accurately to DD from decimal representation
		// very slow though - should be a better way
			return DD.valueOf(x + "");
		}
		
		// current built-in conversion - introduces jitter
		return DD.valueOf(x);
	}

	static interface DDConverter {
		DD convert(double x);
	}
	static final DDConverter DD_STD = new DDConverter() {
		public DD convert(double x) {
			return DD.valueOf(x);
		}
	};
	static final DDConverter DD_DEC = new DDConverter() {
		public DD convert(double x) {
			return DD.valueOf(x + "");
		}
	};
}
