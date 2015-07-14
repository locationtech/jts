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

public class KdTreeTest extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(KdTreeTest.class);
  }
  
  public KdTreeTest(String name)
  {
    super(name);
  }

  public void testSinglePoint()
  {
    KdTree index = new KdTree(.001);
    
    KdNode node1 = index.insert(new Coordinate(1,1));
    KdNode node2 = index.insert(new Coordinate(1,1));

    assertTrue("Inserting 2 identical points should create one node", node1 == node2);
    
    Envelope queryEnv = new Envelope(0,10,0,10);
    
    List result = index.query(queryEnv);
    assertTrue(result.size() == 1);
    
    KdNode node = (KdNode) result.get(0);
    assertTrue(node.getCount() == 2);
    assertTrue(node.isRepeated());
  }
  
  public void testMultiplePoint()
  {
    testQuery(
        readCoords(new double[][] {  {1, 1}, {2 ,2}  }), 
        0,
        new Envelope(0,10, 0,10),
        readCoords(new double[][] { {1,1},  {2 ,2}  } ));
  }
  
  public void testSubset()
  {
    testQuery(
        readCoords(new double[][] {  {1, 1}, {2 ,2}, {3, 3}, {4,4}  }), 
        0,
        new Envelope(1.5,3.4, 1.5, 3.5),
        readCoords(new double[][] {  {2 ,2}, {3, 3}  } ));
  }
  
  private void testQuery(Coordinate[] input, double tolerance, Envelope queryEnv,
      Coordinate[] expectedCoord) {
    KdTree index = build(input, tolerance);
    Coordinate[] result = KdTree.toCoordinates(index.query(queryEnv));

    Arrays.sort(result);
    Arrays.sort(expectedCoord);
    boolean isMatch = CoordinateArrays.equals(result, expectedCoord);
    
    assertTrue("Expected results not found", isMatch );
  }

  public void testTolerance()
  {
    KdTree index = build("MULTIPOINT ((0 0), (-.1 1), (.1 1))", 1.0);
    
    Envelope queryEnv = new Envelope(-9, 9, -9, 9);
    
    List result = index.query(queryEnv);
    assertTrue(result.size() == 2);
    assertTrue( ((KdNode) result.get(0))
        .getCoordinate().equals2D(new Coordinate(.1, 1)));
  }
  
  private KdTree build(Coordinate[] coords, double tolerance) {
    final KdTree index = new KdTree(tolerance);
    for (int i = 0; i < coords.length; i++) {
      index.insert(coords[i]);
    }
    return index;
  }
  
  private KdTree build(String wkt, double tolerance) {
    Geometry geom = IOUtil.read(wkt);
    final KdTree index = new KdTree(tolerance);
    geom.apply(new CoordinateFilter() {
      public void filter(Coordinate coord) {
        index.insert(coord);
      }
    });
    return index;
  }
  
  private KdTree build(String wkt) {
    return build(wkt, 0.001);
  }
  
  private Coordinate[] readCoords(double[][] ords) {
    Coordinate[] coords = new Coordinate[ords.length];
    for (int i = 0; i < ords.length; i++) {
      Coordinate c = new Coordinate(ords[i][0], ords[i][1]);
      coords[i] = c;
    }
    return coords;
  }
}
