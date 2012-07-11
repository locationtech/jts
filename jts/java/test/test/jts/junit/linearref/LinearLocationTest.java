package test.jts.junit.linearref;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.linearref.*;
import com.vividsolutions.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Tests methods involving only {@link LinearLocation}s
 * 
 * @author Martin Davis
 *
 */
public class LinearLocationTest 
	extends TestCase
{
  private WKTReader reader = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(LinearLocationTest.class);
  }

  public LinearLocationTest(String name) { super(name); }

  public void testZeroLengthLineString() throws Exception
  {
    Geometry line = reader.read("LINESTRING (10 0, 10 0)");
    LocationIndexedLine indexedLine = new LocationIndexedLine(line);
    LinearLocation loc0 = indexedLine.indexOf(new Coordinate(11, 0));
    assertTrue(loc0.compareTo(new LinearLocation(1, 0.0)) == 0);
  }
  
  public void testRepeatedCoordsLineString() throws Exception
  {
    Geometry line = reader.read("LINESTRING (10 0, 10 0, 20 0)");
    LocationIndexedLine indexedLine = new LocationIndexedLine(line);
    LinearLocation loc0 = indexedLine.indexOf(new Coordinate(11, 0));
    assertTrue(loc0.compareTo(new LinearLocation(1, 0.1)) == 0);
  }
  
  public void testSameSegmentLineString() throws Exception
  {
    Geometry line = reader.read("LINESTRING (0 0, 10 0, 20 0, 30 0)");
    LocationIndexedLine indexedLine = new LocationIndexedLine(line);
    
    LinearLocation loc0 = indexedLine.indexOf(new Coordinate(0, 0));
    LinearLocation loc0_5 = indexedLine.indexOf(new Coordinate(5, 0));
    LinearLocation loc1 = indexedLine.indexOf(new Coordinate (10, 0));
    LinearLocation loc2 = indexedLine.indexOf(new Coordinate (20, 0));
    LinearLocation loc2_5 = indexedLine.indexOf(new Coordinate (25, 0));
    LinearLocation loc3 = indexedLine.indexOf(new Coordinate (30, 0));
    
    assertTrue(loc0.isOnSameSegment(loc0));
    assertTrue(loc0.isOnSameSegment(loc0_5));
    assertTrue(loc0.isOnSameSegment(loc1));
    assertTrue(! loc0.isOnSameSegment(loc2));
    assertTrue(! loc0.isOnSameSegment(loc2_5));
    assertTrue(! loc0.isOnSameSegment(loc3));
   
    assertTrue(loc0_5.isOnSameSegment(loc0));
    assertTrue(loc0_5.isOnSameSegment(loc1));
    assertTrue(! loc0_5.isOnSameSegment(loc2));
    assertTrue(! loc0_5.isOnSameSegment(loc3));

    assertTrue(! loc2.isOnSameSegment(loc0));
    assertTrue(loc2.isOnSameSegment(loc1));
    assertTrue(loc2.isOnSameSegment(loc2));
    assertTrue(loc2.isOnSameSegment(loc3));
    
    assertTrue(loc2_5.isOnSameSegment(loc3));
    
    assertTrue(! loc3.isOnSameSegment(loc0));
    assertTrue(loc3.isOnSameSegment(loc2));
    assertTrue(loc3.isOnSameSegment(loc2_5));
    assertTrue(loc3.isOnSameSegment(loc3));

  }
  public void testSameSegmentMultiLineString() throws Exception
  {
    Geometry line = reader.read("MULTILINESTRING ((0 0, 10 0, 20 0), (20 0, 30 0))");
    LocationIndexedLine indexedLine = new LocationIndexedLine(line);
    
    LinearLocation loc0 = indexedLine.indexOf(new Coordinate(0, 0));
    LinearLocation loc0_5 = indexedLine.indexOf(new Coordinate(5, 0));
    LinearLocation loc1 = indexedLine.indexOf(new Coordinate (10, 0));
    LinearLocation loc2 = indexedLine.indexOf(new Coordinate (20, 0));
    LinearLocation loc2B = new LinearLocation(1, 0, 0.0);
    
    LinearLocation loc2_5 = indexedLine.indexOf(new Coordinate (25, 0));
    LinearLocation loc3 = indexedLine.indexOf(new Coordinate (30, 0));
    
    assertTrue(loc0.isOnSameSegment(loc0));
    assertTrue(loc0.isOnSameSegment(loc0_5));
    assertTrue(loc0.isOnSameSegment(loc1));
    assertTrue(! loc0.isOnSameSegment(loc2));
    assertTrue(! loc0.isOnSameSegment(loc2_5));
    assertTrue(! loc0.isOnSameSegment(loc3));
   
    assertTrue(loc0_5.isOnSameSegment(loc0));
    assertTrue(loc0_5.isOnSameSegment(loc1));
    assertTrue(! loc0_5.isOnSameSegment(loc2));
    assertTrue(! loc0_5.isOnSameSegment(loc3));

    assertTrue(! loc2.isOnSameSegment(loc0));
    assertTrue(loc2.isOnSameSegment(loc1));
    assertTrue(loc2.isOnSameSegment(loc2));
    assertTrue(! loc2.isOnSameSegment(loc3));
    assertTrue(loc2B.isOnSameSegment(loc3));
    
    assertTrue(loc2_5.isOnSameSegment(loc3));
    
    assertTrue(! loc3.isOnSameSegment(loc0));
    assertTrue(! loc3.isOnSameSegment(loc2));
    assertTrue(loc3.isOnSameSegment(loc2B));
    assertTrue(loc3.isOnSameSegment(loc2_5));
    assertTrue(loc3.isOnSameSegment(loc3));
  }
  
  public void testGetSegmentMultiLineString() throws Exception
  {
    Geometry line = reader.read("MULTILINESTRING ((0 0, 10 0, 20 0), (20 0, 30 0))");
    LocationIndexedLine indexedLine = new LocationIndexedLine(line);
    
    LinearLocation loc0 = indexedLine.indexOf(new Coordinate(0, 0));
    LinearLocation loc0_5 = indexedLine.indexOf(new Coordinate(5, 0));
    LinearLocation loc1 = indexedLine.indexOf(new Coordinate (10, 0));
    LinearLocation loc2 = indexedLine.indexOf(new Coordinate (20, 0));
    LinearLocation loc2B = new LinearLocation(1, 0, 0.0);
    
    LinearLocation loc2_5 = indexedLine.indexOf(new Coordinate (25, 0));
    LinearLocation loc3 = indexedLine.indexOf(new Coordinate (30, 0));
    
    LineSegment seg0 = new LineSegment(new Coordinate(0,0), new Coordinate(10, 0));
    LineSegment seg1 = new LineSegment(new Coordinate(10,0), new Coordinate(20, 0));
    LineSegment seg2 = new LineSegment(new Coordinate(20,0), new Coordinate(30, 0));
    
    assertTrue(loc0.getSegment(line).equals(seg0));
    assertTrue(loc0_5.getSegment(line).equals(seg0));
    
    assertTrue(loc1.getSegment(line).equals(seg1));
    assertTrue(loc2.getSegment(line).equals(seg1));
    
    assertTrue(loc2_5.getSegment(line).equals(seg2));
    assertTrue(loc3.getSegment(line).equals(seg2));
  }

  
}
