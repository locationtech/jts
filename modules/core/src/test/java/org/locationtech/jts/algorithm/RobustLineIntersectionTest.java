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

package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests robustness and correctness of RobustLineIntersector
 * in some tricky cases.
 * Failure modes can include exceptions thrown, or incorrect
 * results returned.
 * 
 * @author Owner
 *
 */
public class RobustLineIntersectionTest 
extends TestCase 
{
  private WKTReader reader = new WKTReader();

	public static void main(String args[]) {
		TestRunner.run(RobustLineIntersectionTest.class);
	}

	public RobustLineIntersectionTest(String name) {
		super(name);
	}

	/**
	 * Following cases were failures when using the CentralEndpointIntersector heuristic.
	 * This is because one segment lies at a significant angle to the other,
	 * with only one endpoint is close to the other segment.
	 * The CE heuristic chose the wrong endpoint to return.
	 * The fix is to use a new heuristic which out of the 4 endpoints
	 * chooses the one which is closest to the other segment.
	 * This works in all known failure cases.
	 * 
	 * @throws ParseException
	 */
	  public void testCentralEndpointHeuristicFailure() 
	  throws ParseException
	  {
	    checkIntersection(
	        "LINESTRING (163.81867067 -211.31840378, 165.9174252 -214.1665075)",   
	        "LINESTRING (2.84139601 -57.95412726, 469.59990601 -502.63851732)",
	        1,
	        "POINT (163.81867067 -211.31840378)",
	        0);
	  }

    public void testCentralEndpointHeuristicFailure2() 
    throws ParseException
    {
      checkIntersection(
          "LINESTRING (-58.00593335955 -1.43739086465, -513.86101637525 -457.29247388035)",   
          "LINESTRING (-215.22279674875 -158.65425425385, -218.1208801283 -160.68343590235)",
          1,
          "POINT ( -215.22279674875 -158.65425425385 )",
          0);
    }

    /**
     * Tests a case where intersection point is rounded, 
     * and it is computed as a nearest endpoint.
     * Exposed a bug due to aliasing of endpoint. 
     * 
     * MD 8 Mar 2013
     * 
     * @throws ParseException
     */
    public void testRoundedPointsNotAltered() 
    throws ParseException
    {
      checkInputNotAltered(
          "LINESTRING (-58.00593335955 -1.43739086465, -513.86101637525 -457.29247388035)",   
          "LINESTRING (-215.22279674875 -158.65425425385, -218.1208801283 -160.68343590235)",
          100000 );
    }


  /**
   * Test from Tomas Fa - JTS list 6/13/2012
   * 
   * Fails using original JTS DeVillers determine orientation test.
   * Succeeds using DD and Shewchuk orientation
   * 
   * @throws ParseException
   */
  public void testTomasFa_1() 
  throws ParseException
  {
    checkIntersectionNone(
        "LINESTRING (-42.0 163.2, 21.2 265.2)",
        "LINESTRING (-26.2 188.7, 37.0 290.7)");
  }
  
  /**
   * Test from Tomas Fa - JTS list 6/13/2012
   * 
   * Fails using original JTS DeVillers determine orientation test.
   * Succeeds using DD and Shewchuk orientation
   * 
   * @throws ParseException
   */
  public void testTomasFa_2() 
  throws ParseException
  {
    checkIntersectionNone(
        "LINESTRING (-5.9 163.1, 76.1 250.7)",
        "LINESTRING (14.6 185.0, 96.6 272.6)");
  }
  
  /**
   * Test involving two non-almost-parallel lines.
   * Does not seem to cause problems with basic line intersection algorithm.
   * 
   * @throws ParseException
   */
  public void testLeduc_1() 
  throws ParseException
  {
    checkIntersection(
        "LINESTRING (305690.0434123494 254176.46578338774, 305601.9999843455 254243.19999846347)",
        "LINESTRING (305689.6153764265 254177.33102743194, 305692.4999844298 254171.4999983967)",   
        1,
        "POINT (305690.0434123494 254176.46578338774)",
        0);
  }

	/**
	 * Test from strk which is bad in GEOS (2009-04-14).
	 * 
	 * @throws ParseException
	 */
	public void testGEOS_1() 
	throws ParseException
	{
		checkIntersection(
				"LINESTRING (588750.7429703881 4518950.493668233, 588748.2060409798 4518933.9452804085)",
				"LINESTRING (588745.824857241 4518940.742239175, 588748.2060437313 4518933.9452791475)",		
				1,
				"POINT (588748.2060416829 4518933.945284994)",
				0);
	}

	/**
	 * Test from strk which is bad in GEOS (2009-04-14).
	 * 
	 * @throws ParseException
	 */
	public void testGEOS_2() 
	throws ParseException
	{
		checkIntersection(
				"LINESTRING (588743.626135934 4518924.610969561, 588732.2822865889 4518925.4314047815)",
				"LINESTRING (588739.1191384895 4518927.235700594, 588731.7854614238 4518924.578370095)",		
				1,
				"POINT (588733.8306132929 4518925.319423238)",
				0);
	}

		/**
		 * This used to be a failure case (exception), but apparently works now.
		 * Possibly normalization has fixed this?
		 * 
		 * @throws ParseException
		 */
	public void testDaveSkeaCase() 
		throws ParseException
	{
		checkIntersection(
				"LINESTRING ( 2089426.5233462777 1180182.3877339689, 2085646.6891757075 1195618.7333999649 )",
				"LINESTRING ( 1889281.8148903656 1997547.0560044837, 2259977.3672235999 483675.17050843034 )",
				1,
				new Coordinate[] {
						new Coordinate(2087536.6062609926, 1187900.560566967),
				}, 0);
	}
	
	/**
	 * Outside envelope using HCoordinate method.
	 * 
	 * @throws ParseException
	 */
	public void testCmp5CaseWKT() 
	throws ParseException
	{
		checkIntersection(
				"LINESTRING (4348433.262114629 5552595.478385733, 4348440.849387404 5552599.272022122 )",
				"LINESTRING (4348433.26211463  5552595.47838573,  4348440.8493874   5552599.27202212  )",		
				1,
				new Coordinate[] {
						new Coordinate(4348440.8493874, 5552599.27202212),
				},
				0);
	}

	/**
	 * Result of this test should be the same as the WKT one!
	 * @throws ParseException
	 */
	public void testCmp5CaseRaw() 
	throws ParseException
	{
		checkIntersection(
				new Coordinate[] { 
						new Coordinate(4348433.262114629, 5552595.478385733),
						new Coordinate(4348440.849387404, 5552599.272022122),
						 						
						new Coordinate(4348433.26211463,  5552595.47838573),
						new Coordinate(4348440.8493874,   5552599.27202212)
				},				1,
				new Coordinate[] {
						new Coordinate(4348440.8493874, 5552599.27202212),
				},
				0);
	}

  void checkIntersectionNone(String wkt1, String wkt2)
    throws ParseException
  {
    LineString l1 = (LineString) reader.read(wkt1);
    LineString l2 = (LineString) reader.read(wkt2);
    Coordinate[] pt = new Coordinate[] {
        l1.getCoordinateN(0), l1.getCoordinateN(1),
        l2.getCoordinateN(0), l2.getCoordinateN(1)
    };
    checkIntersection(pt, 0, null, 0);
  }
  
  void checkIntersection(String wkt1, String wkt2,
      int expectedIntersectionNum, 
      Coordinate[] intPt, 
      double distanceTolerance)
    throws ParseException
  {
    LineString l1 = (LineString) reader.read(wkt1);
    LineString l2 = (LineString) reader.read(wkt2);
    Coordinate[] pt = new Coordinate[] {
        l1.getCoordinateN(0), l1.getCoordinateN(1),
        l2.getCoordinateN(0), l2.getCoordinateN(1)
    };
    checkIntersection(pt, expectedIntersectionNum, intPt, distanceTolerance);
  }
  
	void checkIntersection(String wkt1, String wkt2,
			int expectedIntersectionNum, 
			String expectedWKT, 
			double distanceTolerance)
		throws ParseException
	{
		LineString l1 = (LineString) reader.read(wkt1);
		LineString l2 = (LineString) reader.read(wkt2);
		Coordinate[] pt = new Coordinate[] {
				l1.getCoordinateN(0), l1.getCoordinateN(1),
				l2.getCoordinateN(0), l2.getCoordinateN(1)
		};
		Geometry g = reader.read(expectedWKT);
		Coordinate[] intPt = g.getCoordinates();
		checkIntersection(pt, expectedIntersectionNum, intPt, distanceTolerance);
	}
	
	/**
	 * Check that intersection of segment defined by points in pt array
	 * is equal to the expectedIntPt value (up to the given distanceTolerance).
	 * 
	 * @param pt
	 * @param expectedIntersectionNum
	 * @param expectedIntPt the expected intersection points (maybe null if not tested)
	 * @param distanceTolerance tolerance to use for equality test
	 */
	void checkIntersection(Coordinate[] pt, 
			int expectedIntersectionNum, 
			Coordinate[] expectedIntPt,
			double distanceTolerance)
	{
		LineIntersector li = new RobustLineIntersector();
		li.computeIntersection(pt[0], pt[1], pt[2], pt[3]);
		
		int intNum = li.getIntersectionNum();
		assertEquals("Number of intersections not as expected", expectedIntersectionNum, intNum);
		
		if (expectedIntPt != null) {
			assertEquals("Wrong number of expected int pts provided", intNum, expectedIntPt.length);
			// test that both points are represented here
			boolean isIntPointsCorrect = true;
			if (intNum == 1) {
				checkIntPoints(expectedIntPt[0], li.getIntersection(0), distanceTolerance);
			}
			else if (intNum == 2) {
				checkIntPoints(expectedIntPt[1], li.getIntersection(0), distanceTolerance);
				checkIntPoints(expectedIntPt[1], li.getIntersection(0), distanceTolerance);
				
				if (! (equals(expectedIntPt[0],li.getIntersection(0), distanceTolerance) 
						|| equals(expectedIntPt[0],li.getIntersection(1), distanceTolerance) )) {
					checkIntPoints(expectedIntPt[0], li.getIntersection(0), distanceTolerance);
					checkIntPoints(expectedIntPt[0], li.getIntersection(1), distanceTolerance);
				}
				else if (! (equals(expectedIntPt[1],li.getIntersection(0), distanceTolerance) 
						|| equals(expectedIntPt[1],li.getIntersection(1), distanceTolerance) )) { 
					checkIntPoints(expectedIntPt[1], li.getIntersection(0), distanceTolerance);
					checkIntPoints(expectedIntPt[1], li.getIntersection(1), distanceTolerance);
				}
			}
		}
	}
	
	void checkIntPoints(Coordinate expectedPt, Coordinate actualPt, double distanceTolerance)
	{
		boolean isEqual = equals(expectedPt, actualPt, distanceTolerance);
		assertTrue("Int Pts not equal - " 
				+ "expected " + WKTWriter.toPoint(expectedPt) + " VS " 
				+ "actual " + WKTWriter.toPoint(actualPt), isEqual);
	}
	
	public static boolean equals(Coordinate p0, Coordinate p1, double distanceTolerance)
	{
		return p0.distance(p1) <= distanceTolerance;
	}
	
  void checkInputNotAltered(String wkt1, String wkt2, int scaleFactor) throws ParseException
  {
    LineString l1 = (LineString) reader.read(wkt1);
    LineString l2 = (LineString) reader.read(wkt2);
    Coordinate[] pt = new Coordinate[] { l1.getCoordinateN(0),
        l1.getCoordinateN(1), l2.getCoordinateN(0), l2.getCoordinateN(1) };
    checkInputNotAltered(pt, scaleFactor);
  }

	public void checkInputNotAltered(Coordinate[] pt, int scaleFactor)
	  {
	    // save input points
	    Coordinate[] savePt = new Coordinate[4];
	    for (int i = 0; i < 4; i++) {
	      savePt[i] = new Coordinate(pt[i]);
	    }
	    
	    LineIntersector li = new RobustLineIntersector();
	    li.setPrecisionModel(new PrecisionModel(scaleFactor));
	    li.computeIntersection(pt[0], pt[1], pt[2], pt[3]);
	    
	    // check that input points are unchanged
	    for (int i = 0; i < 4; i++) {
	      assertEquals("Input point " + i + " was altered - ", savePt[i], pt[i]);
	    }
	  }
	  

}
