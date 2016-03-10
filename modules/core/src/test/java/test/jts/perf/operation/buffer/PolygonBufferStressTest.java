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

package test.jts.perf.operation.buffer;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.locationtech.jts.util.Stopwatch;

/**
 * Stress-tests buffering by repeatedly buffering a geometry
 * using alternate positive and negative distances.
 * 
 * In older versions of JTS this used to quickly cause failure due to robustness
 * issues (bad noding causing topology failures).
 * However by ver 1.13 (at least) this test should pass perfectly.
 * This is due to the many heuristics introduced to improve buffer
 * robustnesss.
 * 
 * 
 * @author Martin Davis
 *
 */
public class PolygonBufferStressTest {

  static final int MAX_ITER = 50;

  static PrecisionModel pm = new PrecisionModel();
  //static PrecisionModel pm = new PrecisionModel(10);

  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  Stopwatch sw = new Stopwatch();

  public static void main(String[] args) {
    PolygonBufferStressTest test = new PolygonBufferStressTest();
    test.test();
  }

  boolean testFailed = false;

  public PolygonBufferStressTest() {
  }

  public void test()
  {
    String geomStr;
    GeometricShapeFactory shapeFact = new GeometricShapeFactory(fact);

    Geometry g = getSampleGeometry();

//    Geometry g = GeometricShapeFactory.createArc(fact, 0, 0, 200.0, 0.0, 6.0, 100);

    //Geometry circle = GeometricShapeFactory.createCircle(fact, 0, 0, 200, 100);
    //Geometry g = circle;

//    Geometry sq = GeometricShapeFactory.createBox(fact, 0, 0, 1, 120);
//    Geometry g = sq.difference(circle);

//    Geometry handle = GeometricShapeFactory.createRectangle(fact, 0, 0, 400, 20, 1);
//    Geometry g = circle.union(handle);

    System.out.println(g);
    test(g);
  }

  private Geometry getSampleGeometry()
  {
    String wkt;
// triangle
//wkt ="POLYGON (( 233 221, 210 172,  262 181, 233 221  ))";

//star polygon with hole
    wkt ="POLYGON ((260 400, 220 300, 80 300, 180 220, 40 200, 180 160, 60 20, 200 80, 280 20, 260 140, 440 20, 340 180, 520 160, 280 220, 460 340, 300 300, 260 400), (260 320, 240 260, 220 220, 160 180, 220 160, 200 100, 260 160, 300 140, 320 180, 260 200, 260 320))";

//star polygon with NO hole
// wkt ="POLYGON ((260 400, 220 300, 80 300, 180 220, 40 200, 180 160, 60 20, 200 80, 280 20, 260 140, 440 20, 340 180, 520 160, 280 220, 460 340, 300 300, 260 400))";

//star polygon with NO hole, 10x size
// wkt ="POLYGON ((2600 4000, 2200 3000, 800 3000, 1800 2200, 400 2000, 1800 1600, 600 200, 2000 800, 2800 200, 2600 1400, 4400 200, 3400 1800, 5200 1600, 2800 2200, 4600 3400, 3000 3000, 2600 4000))";

    Geometry g = null;
    try {
      g = wktRdr.read(wkt);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      testFailed = true;
    }
    return g;
  }
  public void test(Geometry g)
  {
    int maxCount = MAX_ITER;
    //doIteratedBuffer(g, 1, -120.01, maxCount);
    //doIteratedBuffer(g, 1, 2, maxCount);
    doAlternatingIteratedBuffer(g, 1, maxCount);
    if (testFailed) {
      System.out.println("FAILED!");
    }
  }

  public void doIteratedBuffer(Geometry g, double initDist, double distanceInc, int maxCount)
  {
    int i = 0;
    double dist = initDist;
      while (i < maxCount) {
        i++;
        System.out.println("Iter: " + i + " --------------------------------------------------------");

        dist += distanceInc;
        System.out.println("Buffer (" + dist + ")");
        g = getBuffer(g, dist);
//if (((Polygon) g).getNumInteriorRing() > 0)
//  return;
      }
  }

  public void doAlternatingIteratedBuffer(Geometry g, double dist, int maxCount)
  {
    int i = 0;
      while (i < maxCount) {
        i++;
      System.out.println("Iter: " + i + " --------------------------------------------------------");

        dist += 1.0;
      System.out.println("Pos Buffer (" + dist + ")");
        g = getBuffer(g, dist);
      System.out.println("Neg Buffer (" + -dist + ")");
        g = getBuffer(g, -dist);
      }
  }

  private Geometry getBuffer(Geometry geom, double dist)
  {
    Geometry buf = geom.buffer(dist);
    //System.out.println(buf);
    System.out.println(sw.getTimeString());
    if (! buf.isValid()) throw new RuntimeException("buffer not valid!");
    return buf;
  }
}