package com.vividsolutions.jts.index.kdtree;

import java.util.List;

import test.jts.util.IOUtil;

import com.vividsolutions.jts.geom.Coordinate;
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
    KdTree index = build("MULTIPOINT ((1 1), (2 2))");
    
    Envelope queryEnv = new Envelope(0,10,0,10);
    
    List result = index.query(queryEnv);
    assertTrue(result.size() == 2);
    assertTrue( ((KdNode) result.get(0))
    		.getCoordinate().equals2D(new Coordinate(1, 1)));
    assertTrue( ((KdNode) result.get(1))
    		.getCoordinate().equals2D(new Coordinate(2, 2)));
  }
  
  private KdTree build(String wkt)
  {
	Geometry geom = IOUtil.read(wkt);
	final KdTree index = new KdTree(.001);
	geom.apply(new CoordinateFilter() {
		public void filter(Coordinate coord) {
			index.insert(coord);
		}
	});
	return index;
  }
}
