package test.jts.junit.linearref;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.linearref.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import junit.framework.TestCase;

/**
 * Tests the {@link LocationIndexedLine} class
 */
public class LocationIndexedLineTest
    extends AbstractIndexedLineTest {

  public static void main(String[] args) {
      junit.textui.TestRunner.run(LocationIndexedLineTest.class);
  }

  public LocationIndexedLineTest(String name) {
    super(name);
  }

  public void testMultiLineStringSimple()
      throws Exception
  {
    runExtractLine("MULTILINESTRING ((0 0, 10 10), (20 20, 30 30))",
                   new LinearLocation(0, 0, .5),
                   new LinearLocation(1, 0, .5),
        "MULTILINESTRING ((5 5, 10 10), (20 20, 25 25))");
  }

  public void testMultiLineString2()
      throws Exception
  {
    runExtractLine("MULTILINESTRING ((0 0, 10 10), (20 20, 30 30))",
                   new LinearLocation(0, 0, 1.0),
                   new LinearLocation(1, 0, .5),
        "MULTILINESTRING ((10 10, 10 10), (20 20, 25 25))");
  }

  private void runExtractLine(String wkt,
                               LinearLocation start, LinearLocation end, String expected)
  {
    Geometry geom = read(wkt);
    LocationIndexedLine lil = new LocationIndexedLine(geom);
    Geometry result = lil.extractLine(start, end);
    //System.out.println(result);
    checkExpected(result, expected);
  }

  protected Geometry indicesOfThenExtract(Geometry input, Geometry subLine)
  {
    LocationIndexedLine indexedLine = new LocationIndexedLine(input);
    LinearLocation[] loc = indexedLine.indicesOf(subLine);
    Geometry result = indexedLine.extractLine(loc[0], loc[1]);
    return result;
  }

  protected boolean indexOfAfterCheck(Geometry linearGeom, Coordinate testPt)
  {
    LocationIndexedLine indexedLine = new LocationIndexedLine(linearGeom);
    
    // check locations are consecutive
    LinearLocation loc1 = indexedLine.indexOf(testPt);
    LinearLocation loc2 = indexedLine.indexOfAfter(testPt, loc1);
    if (loc2.compareTo(loc1) <= 0 ) return false;
    
    // check extracted points are the same as the input
    Coordinate pt1 = indexedLine.extractPoint(loc1);
    Coordinate pt2 = indexedLine.extractPoint(loc2);
    if (! pt1.equals2D(testPt)) return false;
    if (! pt2.equals2D(testPt)) return false;
    
    return true;
  }

  protected boolean indexOfAfterCheck(Geometry linearGeom, Coordinate testPt, Coordinate afterPt)
  {
    LocationIndexedLine indexedLine = new LocationIndexedLine(linearGeom);
    
    // check that computed location is after check location
    LinearLocation afterLoc = indexedLine.indexOf(afterPt);
    LinearLocation testLoc = indexedLine.indexOfAfter(testPt, afterLoc);
    if (testLoc.compareTo(afterLoc) < 0 ) return false;
    
    return true;
  }

  protected Coordinate extractOffsetAt(Geometry linearGeom, Coordinate testPt, double offsetDistance)
  {
  	LocationIndexedLine indexedLine = new LocationIndexedLine(linearGeom);
  	LinearLocation index = indexedLine.indexOf(testPt);
    return indexedLine.extractPoint(index, offsetDistance);
  }

}