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

package test.jts.perf.operation.valid;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.util.Stopwatch;

/**
 * Stress-tests {@link IsValidOp} 
 * by running it on an invalid MultiPolygon with many intersections.
 * In JTS 1.14 and earlier this takes a very long time to run, 
 * since all intersections are computed before the invalid result is returned. 
 * In fact it is only necessary to detect a single intersection in order
 * to determine invalidity, and this provides much faster performance.
 * 
 * @author mdavis
 *
 */
public class ValidStressTest  
{
  public static void main(String args[]) {
    (new ValidStressTest()).runComb();
    (new ValidStressTest()).runStarCrossPoly();
    (new ValidStressTest()).runStarCrossRing();
  }

  public ValidStressTest() {
  }

  public static int SIZE = 10000;
  
  static GeometryFactory geomFact = new GeometryFactory();
  
  public void runComb()
  {
    int size = 400;
    Envelope env =  new Envelope(0,100,0,100);
    Geometry geom = Comb.crossedComb(env, size, geomFact);
    System.out.println(geom);
    checkValid("Crossed combs (size = " + size + " )", geom);
  }

  public void runStarCrossPoly()
  {
    int size = 1000;
    Envelope env =  new Envelope(0,100,0,100);
    Polygon geom = StarCross.star(env, size, geomFact);
    //System.out.println(geom);
    checkValid("StarCross " + geom.getGeometryType() + "   (size = " + size + " )", geom);
  }

  public void runStarCrossRing()
  {
    int size = 1000;
    Envelope env =  new Envelope(0,100,0,100);
    Polygon poly = StarCross.star(env, size, geomFact);
    Geometry geom = poly.getBoundary();
    //System.out.println(geom);
    checkValid("StarCross " + geom.getGeometryType() + "   (size = " + size + " )", geom);
  }

  public void checkValid(String name, Geometry g)
  {
    System.out.println("Running " + name);
    Stopwatch sw = new Stopwatch();
    boolean isValid = g.isValid();
    System.out.println("Is Valid = " + isValid 
        + "           Time: " + sw.getTimeString() );
  }
  
  
}

class StarCross 
{
  public static Polygon star(Envelope env, int nSeg, GeometryFactory geomFact)
  {     
    Coordinate[] pts = new Coordinate[nSeg + 1];
    Coordinate centre = env.centre();
    double len = 0.5 * Math.min(env.getHeight(), env.getWidth());
    double angInc = Math.PI + 2 * Math.PI / nSeg;
    
    double ang = 0;
    for (int i = 0; i < nSeg; i++) {
      double x = centre.x + len * Math.cos(ang);
      double y = centre.x + len * Math.sin(ang);
      pts[i] = new Coordinate(x, y);
      ang += angInc;
    }
    pts[nSeg] = new Coordinate(pts[0]);
    return geomFact.createPolygon(pts);
  }
}

/**
 * Creates comb-like geometries.
 * Crossed combs provide a geometry with a very high ratio of intersections to edges.
 * 
 * @author Martin Davis
 *
 */
class Comb
{
  
  public static MultiPolygon crossedComb(Envelope env, int size, GeometryFactory geomFact) {
    Polygon comb1 = comb(env, size, geomFact);
    Coordinate centre = env.centre();
    AffineTransformation trans = AffineTransformation.rotationInstance(0.5 * Math.PI, centre.x, centre.y);
    Polygon comb2 = (Polygon) trans.transform(comb1);    
    MultiPolygon mp = geomFact.createMultiPolygon(new Polygon[] { comb1, comb2 } );
    return mp;
  }

  public static Polygon comb(Envelope env, int nArms, GeometryFactory geomFact)
  {	
	int npts = 4 * (nArms - 1) + 2 + 2 + 1;
	Coordinate[] pts = new Coordinate[npts];
	double armWidth = env.getWidth() / (2 * nArms - 1);
	double armLen = env.getHeight() - armWidth;
	
	double xBase = env.getMinX();
	double yBase = env.getMinY();
	
	int ipts = 0;
	for (int i = 0; i < nArms; i++) {
		double x1 = xBase + i * 2 * armWidth;
		double y1 = yBase + armLen + armWidth;
		pts[ipts++] = new Coordinate(x1, y1);
		pts[ipts++] = new Coordinate(x1 + armWidth, y1);
		if (i < nArms - 1) {
			pts[ipts++] = new Coordinate(x1 + armWidth, yBase + armWidth);
			pts[ipts++] = new Coordinate(x1 + 2 * armWidth, yBase + armWidth);
		}
	}
	pts[ipts++] = new Coordinate(env.getMaxX(), yBase);
	pts[ipts++] = new Coordinate(xBase, yBase);
	pts[ipts++] = new Coordinate(pts[0]);
	
	return geomFact.createPolygon(pts);
  }

}
