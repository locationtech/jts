package com.vividsolutions.jts.precision;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class MinimumClearanceTest extends TestCase {
  public static void main(String args[]) {
    TestRunner.run(MinimumClearanceTest.class);
  }
  
  private GeometryFactory geomFact = new GeometryFactory();
  private WKTReader reader = new WKTReader();

  public MinimumClearanceTest(String name) { super(name); }

  public void test2IdenticalPoints()
  throws ParseException
  {
    runTest("MULTIPOINT ((100 100), (100 100))", 1.7976931348623157E308);
  }
  
  public void test3Points()
  throws ParseException
  {
    runTest("MULTIPOINT ((100 100), (10 100), (30 100))", 20);
  }
  
  public void testTriangle()
  throws ParseException
  {
    runTest("POLYGON ((100 100, 300 100, 200 200, 100 100))", 100);
  }
  
  private void runTest(String wkt, double expectedValue)
  throws ParseException
  {
    Geometry g = reader.read(wkt);
    double rp = MinimumClearance.getDistance(g);
    assertEquals(expectedValue, rp);
  }
}
