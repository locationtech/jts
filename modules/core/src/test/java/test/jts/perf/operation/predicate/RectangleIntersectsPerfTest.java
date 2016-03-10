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
package test.jts.perf.operation.predicate;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.precision.GeometryPrecisionReducer;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.locationtech.jts.util.Stopwatch;


public class RectangleIntersectsPerfTest 
{
  static final int MAX_ITER = 10;
  
  static final int NUM_AOI_PTS = 2000;
  static final int NUM_LINES = 5000;
  static final int NUM_LINE_PTS = 1000;
  
  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  Stopwatch sw = new Stopwatch();

  public static void main(String[] args) {
  	RectangleIntersectsPerfTest test = new RectangleIntersectsPerfTest();
    test.test();
  }

  boolean testFailed = false;

  public RectangleIntersectsPerfTest() {
  }

  public void test()
  {
//    test(5);
//    test(10);
    test(500);
//    test(1000);
//    test(2000);
    test(100000);
    /*
    test(100);
    test(1000);
    test(2000);
    test(4000);
    test(8000);
    */
  }
  
  void test(int nPts)
  {
    double size = 100;
    Coordinate origin = new Coordinate(0, 0);
  	Geometry sinePoly = createSineStar(origin, size, nPts).getBoundary();
  	/**
  	 * Make the geometry "crinkly" by rounding off the points.
  	 * This defeats the  MonotoneChain optimization in the full relate
  	 * algorithm, and provides a more realistic test.
  	 */
  	Geometry sinePolyCrinkly = GeometryPrecisionReducer.reduce(sinePoly, 
  	    new PrecisionModel(size / 10));
  	Geometry target = sinePolyCrinkly;
  	
    Geometry rect = createRectangle(origin, 5);
//    System.out.println(target);
    //System.out.println("Running with " + nPts + " points");
    testRectangles(target, 100, 5);
  }

  void testRectangles(Geometry target, int nRect, double rectSize)
  {
    Geometry[] rects = createRectangles(target.getEnvelopeInternal(), nRect, rectSize);
    test(rects, target);
  }
  
  void test(Geometry[] rect, Geometry g)
  {
    System.out.println("Target # pts: " + g.getNumPoints()
        + "  -- # Rectangles: " + rect.length
        );

    int maxCount = MAX_ITER;
    Stopwatch sw = new Stopwatch();
    int count = 0;
    for (int i = 0; i < MAX_ITER; i++) {
      for (int j = 0; j < rect.length; j++) {
//      rect[j].relate(g);
        rect[j].intersects(g);
      }
    }
    System.out.println("Finished in " + sw.getTimeString());
    System.out.println();
  }

  /**
   * Creates a set of rectangular Polygons which 
   * cover the given envelope.
   * The rectangles   
   * At least nRect rectangles are created.
   * 
   * @param env
   * @param nRect
   * @param rectSize
   * @return
   */
  Geometry[] createRectangles(Envelope env, int nRect, double rectSize )
  {
    int nSide =  1 + (int)Math.sqrt((double) nRect);
    double dx = env.getWidth() / nSide;
    double dy = env.getHeight() / nSide;

    List rectList = new ArrayList();
    for (int i = 0; i < nSide; i++) {
      for (int j = 0; j < nSide; j++) {
        double baseX = env.getMinX() + i * dx;
        double baseY = env.getMinY() + j * dy;
        Envelope envRect = new Envelope(
            baseX, baseX + dx,
            baseY, baseY + dy);
        Geometry rect = fact.toGeometry(envRect);
        rectList.add(rect);
      }
    }
    return GeometryFactory.toGeometryArray(rectList);
  }
  
  Geometry createRectangle(Coordinate origin, double size) {
    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(4);
    Geometry g = gsf.createRectangle();
    // Polygon gRect = gsf.createRectangle();
    // Geometry g = gRect.getExteriorRing();
    return g;
  }
  
  Geometry createSineStar(Coordinate origin, double size, int nPts) {
		SineStarFactory gsf = new SineStarFactory();
		gsf.setCentre(origin);
		gsf.setSize(size);
		gsf.setNumPoints(nPts);
		gsf.setArmLengthRatio(2);
		gsf.setNumArms(20);
		Geometry poly = gsf.createSineStar();
		return poly;
	}
  
  
}
