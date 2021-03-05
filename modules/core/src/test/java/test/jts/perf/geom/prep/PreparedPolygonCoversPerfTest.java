/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.geom.prep;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.locationtech.jts.util.Stopwatch;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;


public class PreparedPolygonCoversPerfTest extends PerformanceTestCase
{
  static final int NUM_ITER = 10_000;
  
  static final int NUM_PTS = 2000;
  
  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  Stopwatch sw = new Stopwatch();

  public static void main(String[] args) {
    PerformanceTestRunner.run(PreparedPolygonCoversPerfTest.class);
  }

  boolean testFailed = false;

  private PreparedGeometry prepGeom;

  private List<Point> testPoints;

  private Geometry sinePoly;

  public PreparedPolygonCoversPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 1000});
    setRunIterations(1);
  }

  public void startRun(int nPts)
  {
    System.out.println("Running with size " + nPts);
    System.out.println("Iterations per run = " + NUM_ITER);

//  	Geometry poly = createCircle(new Coordinate(0, 0), 100, nPts);
  	sinePoly = createSineStar(new Coordinate(0, 0), 100, nPts);
//  	System.out.println(poly);
//  	Geometry target = sinePoly.getBoundary();
    prepGeom = (new PreparedGeometryFactory()).create(sinePoly);
    
    testPoints = createPoints(sinePoly.getEnvelopeInternal(), NUM_PTS);
  }

  Geometry createSineStar(Coordinate origin, double size, int nPts) {
		SineStarFactory gsf = new SineStarFactory();
		gsf.setCentre(origin);
		gsf.setSize(size);
		gsf.setNumPoints(nPts);
		gsf.setArmLengthRatio(0.1);
		gsf.setNumArms(50);
		Geometry poly = gsf.createSineStar();
		return poly;
	}
  
  List<Point> createPoints(Envelope env, int nPts)
  {
    int nCells = (int) Math.sqrt(nPts);

  	List<Point> geoms = new ArrayList<Point>();
  	double width = env.getWidth();
  	double xInc = width / nCells;
  	double yInc = width / nCells;
  	for (int i = 0; i < nCells; i++) {
    	for (int j = 0; j < nCells; j++) {
    		Coordinate base = new Coordinate(
    				env.getMinX() + i * xInc,
    				env.getMinY() + j * yInc);
    		Point pt = fact.createPoint(base);
    		geoms.add(pt);
    	}
  	}
  	return geoms;
  }
  
  public void runPreparedPolygon() {
    for (int i = 0; i < NUM_ITER; i++) {
      prepGeom = (new PreparedGeometryFactory()).create(sinePoly);
      for (Point pt : testPoints) {
        prepGeom.covers(pt);
        //prepGeom.contains(pt);
      }
    }
  }
  public void runIndexPointInAreaLocator() {
    for (int i = 0; i < NUM_ITER; i++) {
      IndexedPointInAreaLocator ipa = new IndexedPointInAreaLocator(sinePoly);
      for (Point pt : testPoints) {
        ipa.locate(pt.getCoordinate());
      }
    }
  }
    
}
