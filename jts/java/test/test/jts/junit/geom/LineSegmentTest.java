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
package test.jts.junit.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.geom.*;

/**
 * Test named predicate short-circuits
 */
/**
 * @version 1.7
 */
public class LineSegmentTest extends TestCase {

  WKTReader rdr = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(LineSegmentTest.class);
  }

  public LineSegmentTest(String name) { super(name); }

  private static double ROOT2 = Math.sqrt(2);
  
  public void testProjectionFactor()
  {
    // zero-length line
    LineSegment seg = new LineSegment(10, 0, 10, 0);
    assertTrue(Double.isNaN(seg.projectionFactor(new Coordinate(11, 0))));
    
    LineSegment seg2 = new LineSegment(10, 0, 20, 0);
    assertTrue(seg2.projectionFactor(new Coordinate(11, 0)) == 0.1);
    
  }
  
  public void testOffset() throws Exception
  {
    checkOffset(0, 0, 10, 10, 0.0, ROOT2, -1, 1);
    checkOffset(0, 0, 10, 10, 0.0, -ROOT2, 1, -1);
    
    checkOffset(0, 0, 10, 10, 1.0, ROOT2, 9, 11);
    checkOffset(0, 0, 10, 10, 0.5, ROOT2, 4, 6);
    
    checkOffset(0, 0, 10, 10, 0.5, -ROOT2, 6, 4);
    checkOffset(0, 0, 10, 10, 0.5, -ROOT2, 6, 4);
    
    checkOffset(0, 0, 10, 10, 2.0, ROOT2, 19, 21);
    checkOffset(0, 0, 10, 10, 2.0, -ROOT2, 21, 19);
    
    checkOffset(0, 0, 10, 10, 2.0, 5 * ROOT2, 15, 25);
    checkOffset(0, 0, 10, 10, -2.0, 5 * ROOT2, -25, -15);

  }

  void checkOffset(double x0, double y0, double x1, double y1, double segFrac, double offset, 
  		double expectedX, double expectedY)
  {
  	LineSegment seg = new LineSegment(x0, y0, x1, y1);
  	Coordinate p = seg.pointAlongOffset(segFrac, offset);
  	
  	assertTrue(equalsTolerance(new Coordinate(expectedX, expectedY), p, 0.000001));
  }
  
  public static boolean equalsTolerance(Coordinate p0, Coordinate p1, double tolerance)
  {
  	if (Math.abs(p0.x - p1.x) > tolerance) return false;
  	if (Math.abs(p0.y - p1.y) > tolerance) return false;
  	return true;
  }
  
  public void testOrientationIndexCoordinate()
  {
  	LineSegment seg = new LineSegment(0, 0, 10, 10);
  	checkOrientationIndex(seg, 10, 11, 1);
  	checkOrientationIndex(seg, 10, 9, -1);
  	
  	checkOrientationIndex(seg, 11, 11, 0);
  	
  	checkOrientationIndex(seg, 11, 11.0000001, 1);
  	checkOrientationIndex(seg, 11, 10.9999999, -1);
  	
  	checkOrientationIndex(seg, -2, -1.9999999, 1);
  	checkOrientationIndex(seg, -2, -2.0000001, -1);
  }
  
  public void testOrientationIndexSegment()
  {
  	LineSegment seg = new LineSegment(100, 100, 110, 110);
  	
  	checkOrientationIndex(seg, 100, 101, 105, 106, 1);
  	checkOrientationIndex(seg, 100, 99, 105, 96, -1);
  	
  	checkOrientationIndex(seg, 200, 200, 210, 210, 0);
  	
  }
  
  void checkOrientationIndex(double x0, double y0, double x1, double y1, double px, double py, 
  		int expectedOrient)
  {
  	LineSegment seg = new LineSegment(x0, y0, x1, y1);
  	checkOrientationIndex(seg, px, py, expectedOrient);
  }
  
  void checkOrientationIndex(LineSegment seg, 
  		double px, double py, 
  		int expectedOrient)
  {
  	Coordinate p = new Coordinate(px, py);
  	int orient = seg.orientationIndex(p);
  	assertTrue(orient == expectedOrient);
  }
  
  void checkOrientationIndex(LineSegment seg, 
  		double s0x, double s0y, 
  		double s1x, double s1y, 
  		int expectedOrient)
  {
  	LineSegment seg2 = new LineSegment(s0x, s0y, s1x, s1y);
  	int orient = seg.orientationIndex(seg2);
  	assertTrue(orient == expectedOrient);
  }
  

}