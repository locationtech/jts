

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
package org.locationtech.jtstest.test;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.io.ParseException;


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

