package org.locationtech.jts.noding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

public class FastNodingValidatorTest extends GeometryTestCase {

  private static final String[] VERTEX_INT = new String[] {
      "LINESTRING (100 100, 200 200, 300 300)"
      ,"LINESTRING (100 300, 200 200)"
  };
  private static final String[] INTERIOR_INT = new String[] {
      "LINESTRING (100 100, 300 300)"
      ,"LINESTRING (100 300, 300 100)"
  };
  private static final String[] NO_INT = new String[] {
      "LINESTRING (100 100, 200 200)"
      ,"LINESTRING (200 200, 300 300)"
      ,"LINESTRING (100 300, 200 200)"
  };
  private static final String[] SELF_INTERIOR_INT = new String[] {
      "LINESTRING (100 100, 300 300, 300 100, 100 300)"
  };
  private static final String[] SELF_VERTEX_INT = new String[] {
      "LINESTRING (100 100, 200 200, 300 300, 400 200, 200 200)"
  };

  public FastNodingValidatorTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(FastNodingValidatorTest.class);
  }
  
  public void testInteriorIntersection() {
    checkValid(INTERIOR_INT, false);
    checkIntersection(INTERIOR_INT, "POINT(200 200)");
  }

  public void testVertexIntersection() {
    checkValid(VERTEX_INT, false);
    //checkIntersection(VERTEX_INT, "POINT(200 200)");
  }

  public void testNoIntersection() {
    checkValid(NO_INT, true);
  }

  public void testSelfInteriorIntersection() {
    checkValid(SELF_INTERIOR_INT, false);
  }

  public void testSelfVertexIntersection() {
    checkValid(SELF_VERTEX_INT, false);
  }

  private void checkValid(String[] inputWKT, boolean isValidExpected) {
    List input = readList(inputWKT);
    List segStrings = toSegmentStrings(input); 
    FastNodingValidator fnv = new FastNodingValidator(segStrings);
    boolean isValid = fnv.isValid();

    assertTrue(isValidExpected == isValid);
  }
  
  private void checkIntersection(String[] inputWKT, String expectedWKT) {
    List input = readList(inputWKT);
    Geometry expected = read(expectedWKT);
    Coordinate[] pts = expected.getCoordinates();
    CoordinateList intPtsExpected = new CoordinateList(pts);
    
    List segStrings = toSegmentStrings(input); 
    List intPtsActual = FastNodingValidator.computeIntersections(segStrings);
    
    boolean isSameNumberOfIntersections = intPtsExpected.size() == intPtsActual.size();
    assertTrue(isSameNumberOfIntersections);

    checkIntersections(intPtsActual, intPtsExpected);
  }
  
  private void checkIntersections(List intPtsActual, List intPtsExpected) {
    //TODO: sort intersections so they can be compared
    for (int i = 0; i < intPtsActual.size(); i++) {
      Coordinate ptActual = (Coordinate) intPtsActual.get(i);
      Coordinate ptExpected = (Coordinate) intPtsExpected.get(i);
      
      boolean isEqual = ptActual.equals2D(ptExpected);
      assertTrue(isEqual);
    }
  }

  private static List toSegmentStrings(Collection geoms) {
    List segStrings = new ArrayList();
    for (Object geom : geoms) {
      segStrings.addAll(SegmentStringUtil.extractSegmentStrings((Geometry) geom));
    }
    return segStrings;
  }
  
}
