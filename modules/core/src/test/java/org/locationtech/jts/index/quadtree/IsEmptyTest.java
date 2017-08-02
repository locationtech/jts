package org.locationtech.jts.index.quadtree;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.SpatialIndexTester;

public class IsEmptyTest extends TestCase {

  public static void main(String args[]) {
        TestRunner.run(IsEmptyTest.class);
    }

  public IsEmptyTest(String name) {
        super(name);
    }

  public void testSpatialIndex()
            throws Exception
  {
    Quadtree index = new Quadtree();
    assertTrue(index.size() == 0);
    assertTrue(index.isEmpty());

    index.insert(new Envelope(0,0,1,1), "test");
    assertTrue(index.size() == 1);
    assertTrue(!index.isEmpty());

    index.remove(new Envelope(0,0,1,1), "test");
    assertTrue(index.size() == 0);
    assertTrue(index.isEmpty());
  }
}
