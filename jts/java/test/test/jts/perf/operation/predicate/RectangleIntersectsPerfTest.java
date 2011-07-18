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
package test.jts.perf.operation.predicate;

import java.util.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.prep.*;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import com.vividsolutions.jts.util.Stopwatch;

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
