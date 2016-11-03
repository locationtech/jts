
/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.index;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.index.strtree.AbstractNode;
import org.locationtech.jts.index.strtree.ItemBoundable;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.util.AssertionFailedException;

import junit.framework.TestCase;
import test.jts.index.STRtreeDemo;
import test.jts.util.SerializationUtil;



/**
 * @version 1.7
 */
public class STRtreeTest extends TestCase {
  private GeometryFactory factory = new GeometryFactory();

  public STRtreeTest(String Name_) {
    super(Name_);
  }

  public static void main(String[] args) {
    String[] testCaseName = {STRtreeTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }

  public void testEmptyTreeUsingListQuery()  
  {
    STRtree tree = new STRtree();
    List list = tree.query(new Envelope(0, 0, 1, 1));
    assertTrue(list.isEmpty());
  }
  
  public void testEmptyTreeUsingItemVisitorQuery()  
  {
    STRtree tree = new STRtree();
    tree.query(new Envelope(0,0,1,1), new ItemVisitor() {
      public void visitItem(Object item) {
        assertTrue("Should never reach here", true);
      }
    });  
  }
  
  public void testCreateParentsFromVerticalSlice() {
    doTestCreateParentsFromVerticalSlice(3, 2, 2, 1);
    doTestCreateParentsFromVerticalSlice(4, 2, 2, 2);
    doTestCreateParentsFromVerticalSlice(5, 2, 2, 1);
  }

  public void testSpatialIndex()
  throws Exception
  {
    SpatialIndexTester tester = new SpatialIndexTester();
    tester.setSpatialIndex(new STRtree(4));
    tester.init();
    tester.run();
    assertTrue(tester.isSuccess());
  }

  public void testSerialization()
  throws Exception
  {
    SpatialIndexTester tester = new SpatialIndexTester();
    tester.setSpatialIndex(new STRtree(4));
    tester.init();

    STRtree tree = (STRtree) tester.getSpatialIndex();
    // create the index before serialization
    tree.query(new Envelope());
    
    byte[] data = SerializationUtil.serialize(tree);
    tree = (STRtree) SerializationUtil.deserialize(data);
    
    tester.setSpatialIndex(tree);
    tester.run();
    assertTrue(tester.isSuccess());
  }

  public void testDisallowedInserts() {
    STRtree t = new STRtree(5);
    t.insert(new Envelope(0, 0, 0, 0), new Object());
    t.insert(new Envelope(0, 0, 0, 0), new Object());
    t.query(new Envelope());
    try {
      t.insert(new Envelope(0, 0, 0, 0), new Object());
      assertTrue(false);
    }
    catch (AssertionFailedException e) {
      assertTrue(true);
    }
  }

  public void testQuery() throws Throwable {
    ArrayList geometries = new ArrayList();
    geometries.add(factory.createLineString(new Coordinate[]{
        new Coordinate(0, 0), new Coordinate(10, 10)}));
    geometries.add(factory.createLineString(new Coordinate[]{
        new Coordinate(20, 20), new Coordinate(30, 30)}));
    geometries.add(factory.createLineString(new Coordinate[]{
        new Coordinate(20, 20), new Coordinate(30, 30)}));
    STRtreeDemo.TestTree t = new STRtreeDemo.TestTree(4);
    for (Iterator i = geometries.iterator(); i.hasNext(); ) {
      Geometry g = (Geometry) i.next();
      t.insert(g.getEnvelopeInternal(), new Object());
    }
    t.build();
    try {
      assertEquals(1, t.query(new Envelope(5, 6, 5, 6)).size());
      assertEquals(0, t.query(new Envelope(20, 30, 0, 10)).size());
      assertEquals(2, t.query(new Envelope(25, 26, 25, 26)).size());
      assertEquals(3, t.query(new Envelope(0, 100, 0, 100)).size());
    }
    catch (Throwable x) {
      STRtreeDemo.printSourceData(geometries, System.out);
      STRtreeDemo.printLevels(t, System.out);
      throw x;
    }
  }

  public void testVerticalSlices() {
    doTestVerticalSlices(3, 2, 2, 1);
    doTestVerticalSlices(4, 2, 2, 2);
    doTestVerticalSlices(5, 3, 2, 1);
  }

  public void testRemove() {
    STRtree tree = new STRtree();
    tree.insert(new Envelope(0, 10, 0, 10), "1");
    tree.insert(new Envelope(5, 15, 5, 15), "2");
    tree.insert(new Envelope(10, 20, 10, 20), "3");
    tree.insert(new Envelope(15, 25, 15, 25), "4");
    tree.remove(new Envelope(10, 20, 10, 20), "4");
    assertEquals(3, tree.size());
  }
 
  private void doTestCreateParentsFromVerticalSlice(int childCount,
      int nodeCapacity, int expectedChildrenPerParentBoundable,
      int expectedChildrenOfLastParent) {
    STRtreeDemo.TestTree t = new STRtreeDemo.TestTree(nodeCapacity);
    List parentBoundables
         = t.createParentBoundablesFromVerticalSlice(itemWrappers(childCount), 0);
    for (int i = 0; i < parentBoundables.size() - 1; i++) {//-1
      AbstractNode parentBoundable = (AbstractNode) parentBoundables.get(i);
      assertEquals(expectedChildrenPerParentBoundable, parentBoundable.getChildBoundables().size());
    }
    AbstractNode lastParent = (AbstractNode) parentBoundables.get(parentBoundables.size() - 1);
    assertEquals(expectedChildrenOfLastParent, lastParent.getChildBoundables().size());
  }

  private void doTestVerticalSlices(int itemCount, int sliceCount,
      int expectedBoundablesPerSlice, int expectedBoundablesOnLastSlice) {
    STRtreeDemo.TestTree t = new STRtreeDemo.TestTree(2);
    List[] slices =
        t.verticalSlices(itemWrappers(itemCount), sliceCount);
    assertEquals(sliceCount, slices.length);
    for (int i = 0; i < sliceCount - 1; i++) {//-1
      assertEquals(expectedBoundablesPerSlice, slices[i].size());
    }
    assertEquals(expectedBoundablesOnLastSlice, slices[sliceCount - 1].size());
  }

  private List itemWrappers(int size) {
    ArrayList itemWrappers = new ArrayList();
    for (int i = 0; i < size; i++) {
      itemWrappers.add(new ItemBoundable(new Envelope(0, 0, 0, 0), new Object()));
    }
    return itemWrappers;
  }

}
