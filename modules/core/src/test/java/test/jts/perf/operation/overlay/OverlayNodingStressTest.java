/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.operation.overlay;

import java.util.Random;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.overlay.snap.SnapIfNeededOverlayOp;

import junit.framework.TestCase;

/**
 * Tests Noding checking during overlay.
 * Intended to show that noding check failures due to robustness
 * problems do not occur very often (i.e. that the heuristic is 
 * not triggering so often that a large performance penalty would be incurred.)
 * 
 * The class generates test geometries for input to overlay which contain almost parallel lines
 * - this should cause noding failures relatively frequently.
 *
 * Can also be used to check that the cross-snapping heuristic fix for robustness 
 * failures works well.  If snapping ever fails to fix a case,
 * an exception is thrown.  It is expected (and has been observed)
 * that cross-snapping works extremely well on this dataset.
 *
 * @version 1.7
 */
public class OverlayNodingStressTest
    extends TestCase
{
	private static final int ITER_LIMIT = 10000;
	private static final int BATCH_SIZE = 20;
	
	private Random rand = new Random((long) (Math.PI * 10e8));
	private int failureCount = 0;
	
  public OverlayNodingStressTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(OverlayNodingStressTest.class);
  }

  private double getRand()
  {
  	double r = rand.nextDouble();
  	return r;
  }
  
	public void testNoding()
	{
		int iterLimit = ITER_LIMIT;
		for (int i = 0; i < iterLimit; i++) {
			System.out.println("Iter: " + i
					+ "  Noding failure count = " + failureCount);
			double ang1 = getRand() * Math.PI;
			double ang2 = getRand() * Math.PI;
//			Geometry[] geom = generateGeometryStar(ang1, ang2);
			Geometry[] geom = generateGeometryAccum(ang1, ang2);
			checkIntersection(geom[0], geom[1]);
		}
		System.out.println(
				"Test count = " + iterLimit
				+ "  Noding failure count = " + failureCount 
			);
	}

	public Geometry[] generateGeometryStar(double angle1, double angle2) {
		RotatedRectangleFactory rrFact = new RotatedRectangleFactory();
		Polygon rr1 = rrFact.createRectangle(100, 20, angle1);
		Polygon rr2 = rrFact.createRectangle(100, 20, angle2);

		// this line can be used to test for the presence of noding failures for
		// non-tricky cases
		// Geometry star = rr2;
		Geometry star = rr1.union(rr2);
		return new Geometry[] { star, rr1 };
	}

	private static final double MAX_DISPLACEMENT = 60;

	private Geometry baseAccum = null;
	private int geomCount = 0;
	
	public Geometry[] generateGeometryAccum(double angle1, double angle2) {
		RotatedRectangleFactory rrFact = new RotatedRectangleFactory();
		double basex = angle2 * MAX_DISPLACEMENT - (MAX_DISPLACEMENT / 2);
		Coordinate base = new Coordinate(basex, basex);
		Polygon rr1 = rrFact.createRectangle(100, 20, angle1, base);

		// limit size of accumulated star
		geomCount++;
		if (geomCount >= BATCH_SIZE)
			geomCount = 0;
		if (geomCount == 0)
			baseAccum = null;
		
		if (baseAccum == null)
			baseAccum = rr1;
		else {
		// this line can be used to test for the presence of noding failures for
		// non-tricky cases
		// Geometry star = rr2;
			baseAccum = rr1.union(baseAccum);
		}
		return new Geometry[] { baseAccum, rr1 };
	}

	public void checkIntersection(Geometry base, Geometry testGeom) {

		// this line can be used to test for the presence of noding failures for
		// non-tricky cases
		// Geometry star = rr2;
		System.out.println("Star:");
		System.out.println(base);
		System.out.println("Rectangle:");
		System.out.println(testGeom);
		
		// test to see whether the basic overlay code fails
		try {
			Geometry intTrial = base.intersection(testGeom);
		} catch (Exception ex) {
			failureCount++;
		}
		
		// this will throw an intersection if a robustness error occurs,
		// stopping the run
		Geometry intersection = SnapIfNeededOverlayOp.intersection(base, testGeom);
		System.out.println("Intersection:");
		System.out.println(intersection);
	}
}

class RotatedRectangleFactory
{
	public RotatedRectangleFactory()
	{
		
	}
	
	private static double PI_OVER_2 = Math.PI / 2;
	private GeometryFactory fact = new GeometryFactory();
	
	public Polygon createRectangle(double length, double width, double angle)
	{
		return createRectangle(length, width, angle, new Coordinate(0,0));
	}

	public Polygon createRectangle(double length, double width, double angle, Coordinate base)
	{
		double posx = length / 2 * Math.cos(angle);
		double posy = length / 2 * Math.sin(angle);
		double negx = -posx;
		double negy = -posy;
		double widthOffsetx = (width / 2) * Math.cos(angle + PI_OVER_2);
		double widthOffsety = (width / 2) * Math.sin(angle + PI_OVER_2);
		
		Coordinate[] pts = new Coordinate[] {
				new Coordinate(base.x + posx + widthOffsetx, base.y + posy + widthOffsety),
				new Coordinate(base.x + posx - widthOffsetx, base.y + posy - widthOffsety),
				new Coordinate(base.x + negx - widthOffsetx, base.y + negy - widthOffsety),
				new Coordinate(base.x + negx + widthOffsetx, base.y + negy + widthOffsety),
				new Coordinate(0,0),
		};
		// close polygon
		pts[4] = new Coordinate(pts[0]);
		Polygon poly = fact.createPolygon(fact.createLinearRing(pts), null);
		return poly;
	}
	

}
  