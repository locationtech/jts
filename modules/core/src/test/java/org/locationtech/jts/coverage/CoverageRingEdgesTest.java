/*
 * Copyright (c) 2023 Martin Davis.
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

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class CoverageRingEdgesTest  extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(CoverageRingEdgesTest.class);
  }
  
  public CoverageRingEdgesTest(String name) {
    super(name);
  }
  
  public void testSimple() {
    checkEdges("GEOMETRYCOLLECTION (POLYGON ((100 100, 200 200, 300 100, 200 101, 100 100)), POLYGON ((150 0, 100 100, 200 101, 300 100, 250 0, 150 0)))",
        "MULTILINESTRING ((100 100, 150 0, 250 0, 300 100), (100 100, 200 101, 300 100), (100 100, 200 200, 300 100))");
  }

  private void checkEdges(String wkt, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry[] polygons = toArray(geom);
    List<CoverageEdge> edges = CoverageRingEdges.create(polygons).getEdges();
    MultiLineString edgeLines = toArray(edges, geom.getFactory());
    Geometry expected = read(wktExpected);
    checkEqual(expected, edgeLines);
  }

  private MultiLineString toArray(List<CoverageEdge> edges, GeometryFactory geomFactory) {
    LineString[] lines = new LineString[edges.size()];
    for (int i = 0; i < edges.size(); i++) {
      lines[i] = edges.get(i).toLineString(geomFactory);
    }
    return geomFactory.createMultiLineString(lines);

  }

  private static Geometry[] toArray(Geometry geom) {
    Geometry[] geoms = new Geometry[geom.getNumGeometries()];
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      geoms[i] = geom.getGeometryN(i);
    }
    return geoms;
  }

}
