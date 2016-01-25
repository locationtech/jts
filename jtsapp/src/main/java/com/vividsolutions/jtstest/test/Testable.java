

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
package com.vividsolutions.jtstest.test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.io.ParseException;


/**
 * @version 1.7
 */
public interface Testable {

  boolean isFailed();


  String getFailedMsg();


  void runTest() throws ParseException;


  String getName();


  Geometry getGeometry(int index);


  void setGeometry(int index, Geometry g);


  IntersectionMatrix getIntersectionMatrix();


  String getExpectedIntersectionMatrix();


  void setIntersectionMatrix(IntersectionMatrix im);


  void setExpectedIntersectionMatrix(String expectedIntersectionMatrix);


  void initGeometry() throws ParseException;


  String getDescription();


  boolean isPassed();


  String getWellKnownText(int i);


  void setName(String name);


  void setExpectedConvexHull(Geometry expectedConvexHull);


  void setExpectedBoundary(Geometry boundary);


  void setExpectedIntersection(Geometry expectedIntersection);


  void setExpectedUnion(Geometry expectedUnion);


  void setExpectedDifference(Geometry expectedDifference);


  void setExpectedSymDifference(Geometry expectedSymDifference);

  void setExpectedCentroid(Geometry expectedCentroid);
  //void setExpectedInteriorPoint(Geometry expectedCentroid);


  Geometry getExpectedConvexHull();


  Geometry getExpectedBoundary();


  Geometry getExpectedIntersection();


  Geometry getExpectedUnion();


  Geometry getExpectedDifference();


  Geometry getExpectedSymDifference();
}

