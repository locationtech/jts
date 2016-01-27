
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

package com.vividsolutions.jts.algorithm;

import junit.framework.TestCase;

import com.vividsolutions.jts.algorithm.NonRobustLineIntersector;



/**
 * @version 1.7
 */
public class NonRobustLineIntersectorTest extends TestCase {

  private NonRobustLineIntersector li = new NonRobustLineIntersector();

  public NonRobustLineIntersectorTest(String Name_) {
    super(Name_);
  }//public NonRobustLineIntersectorTest(String Name_)

  public static void main(String[] args) {
    String[] testCaseName = {NonRobustLineIntersectorTest.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }//public static void main(String[] args)

  public void testNegativeZero() {
    //MD suggests we ignore this issue for now.
//    li.computeIntersection(new Coordinate(220, 260), new Coordinate(220, 0),
//        new Coordinate(220, 0), new Coordinate(100, 0));
//    assertEquals((new Coordinate(220, 0)).toString(), li.getIntersection(0).toString());
  }

  public void testGetIntersectionNum() {
    //MD: NonRobustLineIntersector may have different semantics for
    //getIntersectionNumber
//    li.computeIntersection(new Coordinate(220, 0), new Coordinate(110, 0),
//        new Coordinate(0, 0), new Coordinate(110, 0));
//    assertEquals(1, li.getIntersectionNum());
  }

}//public class NonRobustLineIntersectorTest extends TestCase
