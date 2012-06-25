package test.jts.junit.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.algorithm.*;

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
    computeIntersectionNone(
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
    computeIntersectionNone(
        "LINESTRING (-5.9 163.1, 76.1 250.7)",
        "LINESTRING (14.6 185.0, 96.6 272.6)");
  }
  
  /**
   * Test from strk which is bad in GEOS (2009-04-14).
   * 
   * @throws ParseException
   */
  public void testLeduc_1() 
  throws ParseException
  {
    computeIntersection(
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
		computeIntersection(
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
		computeIntersection(
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
		computeIntersection(
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
		computeIntersection(
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
		computeIntersection(
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

  void computeIntersectionNone(String wkt1, String wkt2)
    throws ParseException
  {
    LineString l1 = (LineString) reader.read(wkt1);
    LineString l2 = (LineString) reader.read(wkt2);
    Coordinate[] pt = new Coordinate[] {
        l1.getCoordinateN(0), l1.getCoordinateN(1),
        l2.getCoordinateN(0), l2.getCoordinateN(1)
    };
    computeIntersection(pt, 0, null, 0);
  }
  
  void computeIntersection(String wkt1, String wkt2,
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
    computeIntersection(pt, expectedIntersectionNum, intPt, distanceTolerance);
  }
  
	void computeIntersection(String wkt1, String wkt2,
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
		computeIntersection(pt, expectedIntersectionNum, intPt, distanceTolerance);
	}
	
	/**
	 * 
	 * @param pt
	 * @param expectedIntersectionNum
	 * @param expectedIntPt the expected intersection points (maybe null if not tested)
	 */
	void computeIntersection(Coordinate[] pt, 
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
				testIntPoints(expectedIntPt[0], li.getIntersection(0), distanceTolerance);
			}
			else if (intNum == 2) {
				testIntPoints(expectedIntPt[1], li.getIntersection(0), distanceTolerance);
				testIntPoints(expectedIntPt[1], li.getIntersection(0), distanceTolerance);
				
				if (! (equals(expectedIntPt[0],li.getIntersection(0), distanceTolerance) 
						|| equals(expectedIntPt[0],li.getIntersection(1), distanceTolerance) )) {
					testIntPoints(expectedIntPt[0], li.getIntersection(0), distanceTolerance);
					testIntPoints(expectedIntPt[0], li.getIntersection(1), distanceTolerance);
				}
				else if (! (equals(expectedIntPt[1],li.getIntersection(0), distanceTolerance) 
						|| equals(expectedIntPt[1],li.getIntersection(1), distanceTolerance) )) { 
					testIntPoints(expectedIntPt[1], li.getIntersection(0), distanceTolerance);
					testIntPoints(expectedIntPt[1], li.getIntersection(1), distanceTolerance);
				}
			}
			//assertTrue("Int Pts not equal", isIntPointsCorrect);
		}
	}
	
	void testIntPoints(Coordinate expectedPt, Coordinate actualPt, double distanceTolerance)
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
	
}
