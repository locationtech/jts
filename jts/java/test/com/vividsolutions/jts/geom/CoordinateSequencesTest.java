
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
package com.vividsolutions.jts.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import com.vividsolutions.jts.io.WKTReader;


/**
 * @version 1.7
 */
public class CoordinateSequencesTest extends TestCase {

  private PrecisionModel precisionModel = new PrecisionModel();
  private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);
  WKTReader reader = new WKTReader(geometryFactory);
  
  private static final double TOLERANCE = 1E-5;
  
  public static void main(String args[]) {
    TestRunner.run(CoordinateSequencesTest.class);
  }

  public CoordinateSequencesTest(String name) { super(name); }

  public void testCopyToLargerDim() throws Exception
  {
    PackedCoordinateSequenceFactory csFactory = new PackedCoordinateSequenceFactory();
    CoordinateSequence cs2D = createTestSequence(csFactory, 10,  2);
    CoordinateSequence cs3D = csFactory.create(10,  3);
    CoordinateSequences.copy(cs2D,  0, cs3D, 0, cs3D.size());
    assertTrue(CoordinateSequences.isEqual(cs2D, cs3D));
  }

  public void testCopyToSmallerDim() throws Exception
  {
    PackedCoordinateSequenceFactory csFactory = new PackedCoordinateSequenceFactory();
    CoordinateSequence cs3D = createTestSequence(csFactory, 10,  3);
    CoordinateSequence cs2D = csFactory.create(10,  2);
    CoordinateSequences.copy(cs3D,  0, cs2D, 0, cs2D.size());
    assertTrue(CoordinateSequences.isEqual(cs2D, cs3D));
 }
  
  private static CoordinateSequence createTestSequence(CoordinateSequenceFactory csFactory, int size, int dim)
  {
    CoordinateSequence cs = csFactory.create(size,  dim);
    // initialize with a data signature where coords look like [1, 10, 100, ...]
    for (int i = 0; i < size; i++) {
      for (int d = 0; d < dim; d++) {
        cs.setOrdinate(i, d, i * Math.pow(10, d));
      }
    }
    return cs;
  }

}
