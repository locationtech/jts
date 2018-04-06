package org.locationtech.jts.geom;

import test.jts.GeometryTestCase;
import test.jts.TestData;

public class GeometryCopyTest extends GeometryTestCase {

  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(GeometryCopyTest.class);
  }
  
  public GeometryCopyTest(String name) {
    super(name);
  }
  
  public void testCopy() {
    checkCopy( read( TestData.WKT_POINT ));
    checkCopy( read( TestData.WKT_LINESTRING ));
    checkCopy( read( TestData.WKT_LINEARRING ));
    checkCopy( read( TestData.WKT_POLY ));
    checkCopy( read( TestData.WKT_MULTIPOINT ));
    checkCopy( read( TestData.WKT_MULTILINESTRING ));
    checkCopy( read( TestData.WKT_MULTIPOLYGON ));
    checkCopy( read( TestData.WKT_GC ));
  }

  private void checkCopy(final Geometry g) {
    int SRID = 123;
    g.setSRID(SRID );
    
    Object DATA = new Integer(999);
    g.setUserData(DATA);
    
    Geometry copy = g.copy();
    
    assertEquals(g.getSRID(), copy.getSRID());
    assertEquals(g.getUserData(), copy.getUserData());
    
    //TODO: use a test which checks all ordinates of CoordinateSequences
    assertTrue( g.equalsExact(copy) );
  }
}
