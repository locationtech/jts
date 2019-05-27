package org.locationtech.jts.planargraph;

import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class DirectedEdgeTest extends TestCase
{

  public static void main(String args[]) {
    TestRunner.run(DirectedEdgeTest.class);
  }

  public DirectedEdgeTest(String name)
  {
    super(name);
  }
  
  public void testDirectedEdgeComparator() {
    DirectedEdge d1 = new DirectedEdge(new Node(new Coordinate(0, 0)),
        new Node(new Coordinate(10, 10)), new Coordinate(10, 10), true);
    DirectedEdge d2 = new DirectedEdge(new Node(new Coordinate(0, 0)),
        new Node(new Coordinate(20, 20)), new Coordinate(20, 20), false);
    assertEquals(0, d2.compareTo(d1));
  }

  public void testDirectedEdgeToEdges() {
    DirectedEdge d1 = new DirectedEdge(new Node(new Coordinate(0, 0)),
        new Node(new Coordinate(10, 10)), new Coordinate(10, 10), true);
    DirectedEdge d2 = new DirectedEdge(new Node(new Coordinate(20, 0)),
        new Node(new Coordinate(20, 10)), new Coordinate(20, 10), false);
    List edges = DirectedEdge.toEdges(Arrays.asList(new Object[]{d1, d2}));
    assertEquals(2, edges.size());
    assertNull(edges.get(0));
    assertNull(edges.get(1));
  }
}
