package com.vividsolutions.jts.index.kdtree;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

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
}
