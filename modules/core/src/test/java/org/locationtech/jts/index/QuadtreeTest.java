
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
