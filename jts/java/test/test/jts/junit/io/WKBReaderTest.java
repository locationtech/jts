package test.jts.junit.io;

import com.vividsolutions.jts.geom.CoordinateSequenceComparator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests for reading WKB.
 * 
 * @author Martin Davis
 *
 */
public class WKBReaderTest  extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(WKBReaderTest.class);
  }

  private GeometryFactory geomFactory = new GeometryFactory();
  private WKTReader rdr = new WKTReader(geomFactory);

  public void testShortPolygons() throws ParseException
  {
    // one point
    checkWKBGeometry("0000000003000000010000000140590000000000004069000000000000", "POLYGON ((100 200, 100 200, 100 200, 100 200))");
    // two point
    checkWKBGeometry("000000000300000001000000024059000000000000406900000000000040590000000000004069000000000000", "POLYGON ((100 200, 100 200, 100 200, 100 200))");
  }

  public WKBReaderTest(String name) { super(name); }

  public void testSinglePointLineString() throws ParseException
  {
    checkWKBGeometry("00000000020000000140590000000000004069000000000000", "LINESTRING (100 200, 100 200)");
  }

  /**
   * Not yet implemented satisfactorily.
   * 
   * @throws ParseException
   */
  public void XXtestIllFormedWKB() throws ParseException
  {
    // WKB is missing LinearRing entry
    checkWKBGeometry("00000000030000000140590000000000004069000000000000", "POLYGON ((100 200, 100 200, 100 200, 100 200)");
  }


  private static CoordinateSequenceComparator comp2 = new CoordinateSequenceComparator(2);

  private void checkWKBGeometry(String wkbHex, String expectedWKT) throws ParseException
  {
    WKBReader wkbReader = new WKBReader(geomFactory);
    byte[] wkb = WKBReader.hexToBytes(wkbHex);
    Geometry g2 = wkbReader.read(wkb);
    
    Geometry expected = rdr.read(expectedWKT);
    
   boolean isEqual = (expected.compareTo(g2, comp2) == 0);
    assertTrue(isEqual);

 }
}
