
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package org.locationtech.jts.index;

import org.locationtech.jts.index.quadtree.Quadtree;

import junit.framework.TestCase;
import test.jts.util.SerializationUtil;



/**
 * @version 1.7
 */
public class QuadtreeTest extends TestCase {

  public QuadtreeTest(String Name_) {
    super(Name_);
  }

  public static void main(String[] args) {
    String[] testCaseName = {QuadtreeTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }

  public void testSpatialIndex()
  throws Exception
  {
    SpatialIndexTester tester = new SpatialIndexTester();
    tester.setSpatialIndex(new Quadtree());
    tester.init();
    tester.run();
    assertTrue(tester.isSuccess());
  }
  
  public void testSerialization()
  throws Exception
  {
    SpatialIndexTester tester = new SpatialIndexTester();
    tester.setSpatialIndex(new Quadtree());
    tester.init();
    Quadtree tree = (Quadtree) tester.getSpatialIndex();
    byte[] data = SerializationUtil.serialize(tree);
    tree = (Quadtree) SerializationUtil.deserialize(data);
    tester.setSpatialIndex(tree);
    tester.run();
    assertTrue(tester.isSuccess());
  }

}
