/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.util;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class GeometryEditorTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(GeometryFixerTest.class);
  }

  public GeometryEditorTest(String name) {
    super(name);
  }

  public void testCopy() {
    checkCopy("POINT ( 10 20 )");
    checkCopy("MULTIPOINT ( (10 20), (30 40) )");
    checkCopy("LINESTRING(0 0, 10 10)");
    checkCopy("MULTILINESTRING ((50 100, 100 200), (100 100, 150 200))");
    checkCopy("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))");
    checkCopy(
        "MULTIPOLYGON (((100 200, 200 200, 200 100, 100 100, 100 200)), ((300 200, 400 200, 400 100, 300 100, 300 200)))");
    checkCopy(
        "GEOMETRYCOLLECTION (POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200)), LINESTRING (250 100, 350 200), POINT (350 150))");
  }

  public void testCopyEmpty() {
    checkCopy("POINT EMPTY");
    checkCopy("LINESTRING EMPTY");
    checkCopy("POLYGON EMPTY");
    checkCopy("MULTIPOINT EMPTY");
    checkCopy("MULTILINESTRING EMPTY");
    checkCopy("MULTIPOLYGON EMPTY");
    checkCopy("GEOMETRYCOLLECTION EMPTY");
  }

  //========================================================
  
  private void checkCopy(String wkt) {
    Geometry g = read(wkt);
    Geometry g2 = copy(g);
    assertTrue(g.equalsExact(g2));
    // check a copy has been made
    assertTrue(g != g2);
  }

  private static Geometry copy(Geometry g) {
    GeometryEditor editor = new GeometryEditor(g.getFactory());
    return editor.edit(g, new CoordSeqCloneOp(g.getFactory().getCoordinateSequenceFactory()));
  }

  private static class CoordSeqCloneOp extends GeometryEditor.CoordinateSequenceOperation {
    CoordinateSequenceFactory coordinateSequenceFactory;

    public CoordSeqCloneOp(CoordinateSequenceFactory coordinateSequenceFactory) {
      this.coordinateSequenceFactory = coordinateSequenceFactory;
    }

    public CoordinateSequence edit(CoordinateSequence coordSeq, Geometry geometry) {
      return coordinateSequenceFactory.create(coordSeq);
    }
  }
}
