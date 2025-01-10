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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
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


public class PreparedPolygonLinesPerfTest extends PerformanceTestCase
{
  static final int MAX_ITER = 10;
  
  static final int NUM_AOI_PTS = 2000;
  static final int NUM_LINES = 1000;
  static final int NUM_LINE_PTS = 100;
  
  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  Stopwatch sw = new Stopwatch();

  public static void main(String[] args) {
    PerformanceTestRunner.run(PreparedPolygonLinesPerfTest.class);  }

  boolean testFailed = false;

  private Geometry target;

  private List<LineString> lines;

  private PreparedGeometry prepGeom;

  public PreparedPolygonLinesPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 10, 100, 1000, 2000 });
    setRunIterations(MAX_ITER);
  }

  @Override
  public void startRun(int npts)
  {
//  	Geometry poly = createCircle(new Coordinate(0, 0), 100, nPts);
  	Geometry sinePoly = createSineStar(new Coordinate(0, 0), 100, npts);
//  	System.out.println(poly);
//  	Geometry target = sinePoly.getBoundary();
  	target = sinePoly;
  	
    PreparedGeometryFactory pgFact = new PreparedGeometryFactory();
    prepGeom = pgFact.create(target);
 
    lines = createLines(target.getEnvelopeInternal(), NUM_LINES, 1.0, NUM_LINE_PTS);
    
    System.out.println("\n-------  Running with polygon size = " + npts);
  }

  Geometry createCircle(Coordinate origin, double size, int nPts) {
		GeometricShapeFactory gsf = new GeometricShapeFactory();
		gsf.setCentre(origin);
		gsf.setSize(size);
		gsf.setNumPoints(nPts);
		Geometry circle = gsf.createCircle();
		// Polygon gRect = gsf.createRectangle();
		// Geometry g = gRect.getExteriorRing();
		return circle;
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
  
  List<LineString> createLines(Envelope env, int nItems, double size, int nPts)
  {
    int nCells = (int) Math.sqrt(nItems);

  	List<LineString> geoms = new ArrayList<LineString>();
  	double width = env.getWidth();
  	double xInc = width / nCells;
  	double yInc = width / nCells;
  	for (int i = 0; i < nCells; i++) {
    	for (int j = 0; j < nCells; j++) {
    		Coordinate base = new Coordinate(
    				env.getMinX() + i * xInc,
    				env.getMinY() + j * yInc);
    		LineString line = createLine(base, size, nPts);
    		geoms.add(line);
    	}
  	}
  	return geoms;
  }
  
  LineString createLine(Coordinate base, double size, int nPts)
  {
    SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    Geometry circle = gsf.createSineStar();
//    System.out.println(circle);
    return (LineString) circle.getBoundary();
  }
  
  public void runIntersectsNonPrep()
  {
    for (LineString line : lines) {
      boolean result = target.intersects(line);
    }
  } 
  
  public void runIntersectsPrepCached()
  {
    for (LineString line : lines) {
      boolean result = prepGeom.intersects(line);
    }
  } 
  
  public void runIntersectsPrepNotCached()
  {
    for (LineString line : lines) {
      PreparedGeometry pg = (new PreparedGeometryFactory()).create(target);
      boolean result = pg.intersects(line);
    }
  } 
 
  public void runCoversNonPrep()
  {
    for (LineString line : lines) {
      boolean result = target.covers(line);
    }
  } 
  
  public void runCoverPrepCached()
  {
    for (LineString line : lines) {
      boolean result = prepGeom.covers(line);
    }
  } 
  
  public void runCoverPrepNotCached()
  {
    for (LineString line : lines) {
      PreparedGeometry pg = (new PreparedGeometryFactory()).create(target);
      boolean result = pg.covers(line);
    }
  } 
}
