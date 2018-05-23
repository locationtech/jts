package org.locationtech.jts.geom;

import test.jts.GeometryTestCase;
import test.jts.GeometryTestData;

public class GeometryCopyTest extends GeometryTestCase {

  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(GeometryCopyTest.class);
  }
  
  public GeometryCopyTest(String name) {
    super(name);
  }
  
  public void testCopy() {
    checkCopy( read( GeometryTestData.WKT_POINT ));
    checkCopy( read( GeometryTestData.WKT_LINESTRING ));
    checkCopy( read( GeometryTestData.WKT_LINEARRING ));
    checkCopy( read( GeometryTestData.WKT_POLY ));
    checkCopy( read( GeometryTestData.WKT_MULTIPOINT ));
    checkCopy( read( GeometryTestData.WKT_MULTILINESTRING ));
    checkCopy( read( GeometryTestData.WKT_MULTIPOLYGON ));
    checkCopy( read( GeometryTestData.WKT_GC ));
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
