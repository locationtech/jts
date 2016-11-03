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
package org.locationtech.jts.geom.prep;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.GeometricShapeFactory;

public abstract class StressTestHarness 
{
  static final int MAX_ITER = 10000;

  static PrecisionModel pm = new PrecisionModel();
  static GeometryFactory fact = new GeometryFactory(pm, 0);
  static WKTReader wktRdr = new WKTReader(fact);
  static WKTWriter wktWriter = new WKTWriter();

  private int numTargetPts = 1000;

  public StressTestHarness() {
  }

  public void setTargetSize(int nPts)
  {
  	numTargetPts =nPts;
  }
  
  public void run(int nIter)
  {
  	//System.out.println("Running " + nIter + " tests");
//  	Geometry poly = createCircle(new Coordinate(0, 0), 100, nPts);
  	Geometry poly = createSineStar(new Coordinate(0, 0), 100, numTargetPts);
  	//System.out.println(poly);
  	
    //System.out.println();
    //System.out.println("Running with " + nPts + " points");
    run(nIter, poly);
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
		gsf.setNumArms(20);
		Geometry poly = gsf.createSineStar();
		return poly;
	}
  
  Geometry createRandomTestGeometry(Envelope env, double size, int nPts)
  {
  	double width = env.getWidth();
  	double xOffset = width * Math.random();
  	double yOffset = env.getHeight() * Math.random();
    Coordinate basePt = new Coordinate(
    				env.getMinX() + xOffset,
    				env.getMinY() + yOffset);
    Geometry test = createTestCircle(basePt, size, nPts);
    if (test instanceof Polygon && Math.random() > 0.5) {
    	test = test.getBoundary();
    }
    return test;
  }
  
  Geometry createTestCircle(Coordinate base, double size, int nPts)
  {
    GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    Geometry circle = gsf.createCircle();
//    System.out.println(circle);
    return circle;
  }
  
  public void run(int nIter, Geometry target) {
  	int count = 0;
  	while (count < nIter) {
  		count++;
  		Geometry test = createRandomTestGeometry(target.getEnvelopeInternal(), 10, 20);
      
//      System.out.println("Test # " + count);
//  		System.out.println(line);
//  		System.out.println("Test[" + count + "] " + target.getClass() + "/" + test.getClass());
  		boolean isResultCorrect = checkResult(target, test);
  		if (! isResultCorrect) {
  			throw new RuntimeException("Invalid result found");
  		}
  	}
	}
  
  public abstract boolean checkResult(Geometry target, Geometry test);
	

}
