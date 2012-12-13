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
package test.jts.junit.geom.prep;


import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.SineStarFactory;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.util.GeometricShapeFactory;

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
  	System.out.println("Running " + nIter + " tests");
//  	Geometry poly = createCircle(new Coordinate(0, 0), 100, nPts);
  	Geometry poly = createSineStar(new Coordinate(0, 0), 100, numTargetPts);
  	System.out.println(poly);
  	
    System.out.println();
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
