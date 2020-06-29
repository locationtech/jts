/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.algorithm;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.math.DD;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests the correctness of Orientation Index computed with DD
 * arithmetic.
 * 
 * This is primarily to capture the case for future research.
 * 
 * @author Martin Davis
 *
 */
public class DDOrientationIndexCorrectTest extends GeometryTestCase {

	public static void main(String args[]) {
		TestRunner.run(DDOrientationIndexCorrectTest.class);
	}

	public DDOrientationIndexCorrectTest(String name) {
		super(name);
	}

  /**
   * This test captures a situation where 
   * the DD orientation apparently fails.
   * 
   * According to the stated decimal representation
   * the two orientations should be equal, but they are not.
   * 
   * Even more disturbingly, the orientationIndexFilter 
   * handles this case, without dropping through to 
   * the actual DD code.  The result is the same, however.
   */
  public void testPointCloseToLine() 
  {
    Coordinate[] pts = {
        new Coordinate( 2.4829102, 48.8726807),
        new Coordinate( 2.4832535, 48.8737106 ),
        new Coordinate( 2.4830818249999997, 48.873195575)
    };
    int orientDD = runDD("Orginal case", pts);
    //System.out.println("DD - Alt: " + orientDD);
    
    Coordinate[] ptsScale = {
        new Coordinate( 24829102, 488726807),
        new Coordinate( 24832535, 488737106 ),
        new Coordinate( 24830818.249999997, 488731955.75)
    };
    int orientSC = runDD("Scaled case", ptsScale);
    //System.out.println("DD - Alt: " + orientDD);
    
    /**
     * Same arrangement as above, but translated 
     * by removing digits before decimal point
     * to reduce numeric precision
     */
    Coordinate[] ptsLowPrec = {
        new Coordinate( 0.4829102, 0.8726807),
        new Coordinate( 0.4832535, 0.8737106 ),
        new Coordinate( 0.4830818249999997, 0.873195575)
    };
    int orientLP = runDD("Lower precision case", ptsLowPrec);
    
    /**
     * By adjusting the point slightly it lies exactly on the line
     */
    Coordinate[] ptOnLineScaled = {
        new Coordinate( 24829102, 488726807),
        new Coordinate( 24832535, 488737106 ),
        new Coordinate( 24830818.25, 488731955.75)
    };
    int orientOLSC = runDD("On-line scaled case", ptOnLineScaled);
    assertTrue(orientOLSC == 0);
    
    /**
     * By adjusting the point slightly it lies exactly on the line
     */
    Coordinate[] ptOnLine = {
        new Coordinate( 2.4829102, 48.8726807),
        new Coordinate( 2.4832535, 48.8737106 ),
        new Coordinate( 2.483081825, 48.873195575)
    };
    int orientOL = runDD("On-line case", ptOnLine);
    //assertTrue(orientOL == 0);
    
    assertTrue("Orignal index not equal to lower-precision index", orientDD == orientLP);

  }

  private int runDD(String desc, Coordinate[] pts) {
    int orientDD = Orientation.index(pts[0], pts[1], pts[2]); 
    //int orientSD = ShewchuksDeterminant.orientationIndex(pts[0], pts[1], pts[2]); 
    //int orientAlt = orientationIndexAlt(pts[0], pts[1], pts[2]); 

    System.out.println(desc + " --------------");
    System.out.println("DD: " + orientDD);
    return orientDD;
  }

	public static int orientationIndexAlt(Coordinate p1, Coordinate p2, Coordinate q) {
		// normalize coordinates
		DD dx1 = toDDAlt(p2.x).selfAdd(toDDAlt(-p1.x));
		DD dy1 = toDDAlt(p2.y).selfAdd(toDDAlt(-p1.y));
		DD dx2 = toDDAlt(q.x).selfAdd(toDDAlt(-p2.x));
		DD dy2 = toDDAlt(q.y).selfAdd(toDDAlt(-p2.y));

		// sign of determinant - unrolled for performance
		DD det = dx1.selfMultiply(dy2).selfSubtract(dy1.selfMultiply(dx2));
		return det.signum();
	}

	private static DD toDDAlt(double x) {
  	// convert more accurately to DD from decimal representation
  	// very slow though - should be a better way
		return DD.valueOf(x + "");
	}
}
