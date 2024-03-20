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
import java.util.List;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Stopwatch;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;


public class PreparedPolygonPointsPerfTest extends PerformanceTestCase
{
  static final int NUM_ITER = 1;
  
  static final int NUM_PTS = 2000;
  
  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  Stopwatch sw = new Stopwatch();

  public static void main(String[] args) {
    PerformanceTestRunner.run(PreparedPolygonPointsPerfTest.class);
  }

  private PreparedGeometry prepGeom;

  private List<Point> testPoints;

  private Geometry sinePoly;

  private IndexedPointInAreaLocator ipa;

  public PreparedPolygonPointsPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 1000});
    setRunIterations(NUM_ITER);
  }

  @Override
  public void startRun(int nPts)
  {
//  	Geometry poly = createCircle(new Coordinate(0, 0), 100, nPts);
  	sinePoly = createSineStar(new Coordinate(0, 0), 100, nPts);
//  	System.out.println(poly);
//  	Geometry target = sinePoly.getBoundary();
    prepGeom = (new PreparedGeometryFactory()).create(sinePoly);
    ipa = new IndexedPointInAreaLocator(sinePoly);
    
    testPoints = createPoints(sinePoly.getEnvelopeInternal(), NUM_PTS);
    
    System.out.println("\n-------  Running with polygon size = " + nPts);
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
  
  public void runCoversNonPrep() {
    for (Point pt : testPoints) {
      sinePoly.covers(pt);
    }
  }
  
  public void runCoversPrepared() {
    for (Point pt : testPoints) {
      prepGeom.covers(pt);
    }
  }
  
  public void runCoversPrepNoCache() {
    for (Point pt : testPoints) {
      PreparedGeometry pg = (new PreparedGeometryFactory()).create(sinePoly);
      pg.covers(pt);
    }
  }
  
  public void runIndexPointInAreaLocator() {
    for (Point pt : testPoints) {
      ipa.locate(pt.getCoordinate());
    }
  }
    
  public void runIntersectsNonPrep() {
    for (Point pt : testPoints) {
      sinePoly.intersects(pt);
    }
  }
  
  public void runIntersectsPrepared() {
    for (Point pt : testPoints) {
      prepGeom.intersects(pt);
    }
  }
  
  public void runIntersectsPrepNoCache() {
    for (Point pt : testPoints) {
      PreparedGeometry pg = (new PreparedGeometryFactory()).create(sinePoly);
      pg.intersects(pt);
    }
  }

}
