package org.locationtech.jts.coverage;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiLineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class TPVWSimplifierTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(TPVWSimplifierTest.class);
  }
  
  public TPVWSimplifierTest(String name) {
    super(name);
  }
  
  public void testSimpleNoop() {
    checkNoop("MULTILINESTRING ((9 9, 3 9, 1 4, 4 1, 9 1), (9 1, 2 4, 9 9))", 
        2);
  }
    
  public void testSimple() {
    checkSimplify("MULTILINESTRING ((9 9, 3 9, 1 4, 4 1, 9 1), (9 1, 6 3, 2 4, 5 7, 9 9))", 
        2, 
        "MULTILINESTRING ((9 9, 3 9, 1 4, 4 1, 9 1), (9 1, 2 4, 9 9))");
  }
    
  private void checkNoop(String wkt, double tolerance) {
    MultiLineString geom = (MultiLineString) read(wkt);
    Geometry actual = TPVWSimplifier.simplify(geom, tolerance);
    checkEqual(geom, actual);
  }
  
  private void checkSimplify(String wkt, double tolerance, String wktExpected) {
    MultiLineString geom = (MultiLineString) read(wkt);
    Geometry actual = TPVWSimplifier.simplify(geom, tolerance);
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
  
}
