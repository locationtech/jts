/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.coverage.TPVWSimplifier.Edge;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class TPVWSimplifierTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(TPVWSimplifierTest.class);
  }
  
  public TPVWSimplifierTest(String name) {
    super(name);
  }
  
  public void testSimpleNoop() {
    checkNoop("MULTILINESTRING ((9 9, 3 9, 1 4, 4 1, 9 1), (9 1, 2 4, 9 9))", 
        2);
  }
    
  public void testSimple() {
    checkSimplify("MULTILINESTRING ((9 9, 3 9, 1 4, 4 1, 9 1), (9 1, 6 3, 2 4, 5 7, 9 9))", 
        2, 
        "MULTILINESTRING ((9 9, 3 9, 1 4, 4 1, 9 1), (9 1, 2 4, 9 9))");
  }
    
  public void testFreeRing() {
    checkSimplify("MULTILINESTRING ((1 9, 9 9, 9 1), (1 9, 1 1, 9 1), (7 5, 8 8, 2 8, 2 2, 8 2, 7 5))", 
        new int[] { 2 },
        2, 
        "MULTILINESTRING ((1 9, 1 1, 9 1), (1 9, 9 9, 9 1), (8 8, 2 8, 2 2, 8 2, 8 8))");
  }
    
  public void testNoFreeRing() {
    checkSimplify("MULTILINESTRING ((1 19, 19 19, 19 1), (1 19, 1 1, 19 1), (10 10, 9 18, 2 18, 2 2, 7 6, 10 10), (10 10, 11 18, 18 18, 18 2, 13 6, 10 10))", 
        new int[] {  },
        2, 
        "MULTILINESTRING ((1 19, 1 1, 19 1), (1 19, 19 19, 19 1), (10 10, 2 2, 2 18, 9 18, 10 10), (10 10, 11 18, 18 18, 18 2, 10 10))");
  }
    
  public void testConstraint() {
    checkSimplify("MULTILINESTRING ((6 8, 2 8, 2.1 5, 2 2, 6 2, 5.9 5, 6 8))", 
        new int[] {  },
        "MULTILINESTRING ((1 9, 9 9, 6 5, 9 1), (1 9, 1 1, 9 1))",
        1, 
        "MULTILINESTRING ((1 9, 1 1, 9 1), (1 9, 9 9, 6 5, 9 1), (6 8, 2 8, 2 2, 6 2, 5.9 5, 6 8))");
  }
    
  private void checkNoop(String wkt, double tolerance) {
    checkSimplify(wkt, null, null, tolerance, wkt);  
  }
  
  private void checkSimplify(String wkt, double tolerance, String wktExpected) {
    checkSimplify(wkt, null, null, tolerance, wktExpected);  
  }
  
  private void checkSimplify(String wkt, int[] freeRingIndex, 
      double tolerance, String wktExpected) {
    checkSimplify(wkt, freeRingIndex, null, tolerance, wktExpected);
  }
  
  private void checkSimplify(String wkt, int[] freeRingIndex, 
      String wktConstraints,
      double tolerance, String wktExpected) {
    TPVWSimplifier.Edge[] edges = createEdges(wkt, freeRingIndex, wktConstraints, tolerance);
    CornerArea cornerArea = new CornerArea();
    TPVWSimplifier.simplify(edges, cornerArea, 1.0);
    
    Geometry expected = read(wktExpected);
    MultiLineString actual = createResult(edges, expected.getFactory());
    checkEqual(expected, actual);
  }
  
  private TPVWSimplifier.Edge[] createEdges(String wkt, int[] freeRingIndex, String wktConstraints, double tolerance) {
    List<TPVWSimplifier.Edge> edgeList = new ArrayList<TPVWSimplifier.Edge>();
    addEdges(wkt, freeRingIndex, tolerance, edgeList);
    if (wktConstraints != null) {
      addEdges(wktConstraints, null, 0.0, edgeList);
    }
    TPVWSimplifier.Edge[] edges = edgeList.toArray(new TPVWSimplifier.Edge[0]);
    return edges;
  }

  private void addEdges(String wkt, int[] freeRings, double tolerance, List<Edge> edges) {
    MultiLineString lines = (MultiLineString) read(wkt);
    for (int i = 0; i < lines.getNumGeometries(); i++) {
      LineString line = (LineString) lines.getGeometryN(i);
      boolean isRemovable = false;
      boolean isFreeRing = freeRings == null ? false : hasIndex(freeRings, i);
      Edge edge = new Edge(line.getCoordinates(), tolerance, isFreeRing, isRemovable);
      edges.add(edge);
    }
  }
  
  private boolean hasIndex(int[] freeRings, int i) {
    for (int fr : freeRings) {
      if (fr == i)
        return true;
    }
    return false;
  }

  private static MultiLineString createResult(Edge[] edges, GeometryFactory geomFactory) {
    LineString[] result = new LineString[edges.length];
    for (int i = 0; i < edges.length; i++) {
      Coordinate[] pts = edges[i].getCoordinates();
      result[i] = geomFactory.createLineString(pts);
    }
    return geomFactory.createMultiLineString(result);
  }
}
