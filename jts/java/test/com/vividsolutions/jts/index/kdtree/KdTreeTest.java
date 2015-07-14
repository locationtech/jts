package com.vividsolutions.jts.index.kdtree;

import java.util.Arrays;
import java.util.List;

import test.jts.util.IOUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class KdTreeTest extends TestCase {
  public static void main(String args[]) {
    TestRunner.run(KdTreeTest.class);
  }

  public KdTreeTest(String name) {
    super(name);
  }

  public void testSinglePoint() {
    KdTree index = new KdTree(.001);

    KdNode node1 = index.insert(new Coordinate(1, 1));
    KdNode node2 = index.insert(new Coordinate(1, 1));

    assertTrue("Inserting 2 identical points should create one node",
        node1 == node2);

    Envelope queryEnv = new Envelope(0, 10, 0, 10);

    List result = index.query(queryEnv);
    assertTrue(result.size() == 1);

    KdNode node = (KdNode) result.get(0);
    assertTrue(node.getCount() == 2);
    assertTrue(node.isRepeated());
  }

  public void testMultiplePoint() {
    testQuery("MULTIPOINT ( (1 1), (2 2) )", 0,
        new Envelope(0, 10, 0, 10), 
        "MULTIPOINT ( (1 1), (2 2) )");
  }

  public void testSubset() {
    testQuery("MULTIPOINT ( (1 1), (2 2), (3 3), (4 4) )", 
        0, 
        new Envelope(1.5, 3.4, 1.5, 3.5),
        "MULTIPOINT ( (2 2), (3 3) )");
  }

  public void testToleranceFailure() {
    // tree build is incorrect - point within tolerance of existing node is added as new node
    testQuery("MULTIPOINT ( (0 0), (-.1 1), (.1 1) )", 
        1, 
        new Envelope(-9, 9, -9, 9),
        "MULTIPOINT ( (0 0), (-.1 1) )");
  }
  
  public void testTolerance2() {
    testQuery("MULTIPOINT ((10 60), (20 60), (30 60), (30 63))", 
        9, 
        new Envelope(0,99, 0, 99),
        "MULTIPOINT ((10 60), (20 60), (30 60))");
  }
  
  public void testTolerance2_perturbedY() {
    // tree build is incorrect - node within tolerance of point is not found on insert
    testQuery("MULTIPOINT ((10 60), (20 61), (30 60), (30 63))", 
        9, 
        new Envelope(0,99, 0, 99),
        "MULTIPOINT ((10 60), (20 61), (30 60))");
  }
  
  private void testQuery(String wktInput, double tolerance,
      Envelope queryEnv, String wktExpected) {
    KdTree index = build(wktInput, tolerance);
    testQuery(
        index,
        queryEnv,
        IOUtil.read(wktExpected).getCoordinates());
  }

  private void testQuery(KdTree index,
      Envelope queryEnv, Coordinate[] expectedCoord) {
    Coordinate[] result = KdTree.toCoordinates(index.query(queryEnv));

    Arrays.sort(result);
    Arrays.sort(expectedCoord);
    
    assertTrue("Result count = " + result.length + ", expected count = " + expectedCoord.length,
        result.length == expectedCoord.length);
    
    boolean isMatch = CoordinateArrays.equals(result, expectedCoord);
    assertTrue("Expected result coordinates not found", isMatch);
  }

  private KdTree build(String wktInput, double tolerance) {
    final KdTree index = new KdTree(tolerance);
    Coordinate[] coords = IOUtil.read(wktInput).getCoordinates();
    for (int i = 0; i < coords.length; i++) {
      index.insert(coords[i]);
    }
    return index;
  }

}
