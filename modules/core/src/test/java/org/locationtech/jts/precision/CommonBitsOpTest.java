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

package org.locationtech.jts.precision;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;

import test.jts.GeometryTestCase;

public class CommonBitsOpTest  extends GeometryTestCase
{

  public CommonBitsOpTest(String name) {
    super(name);
  }

  /**
   * Tests an issue where CommonBitsRemover was not persisting changes to some kinds of CoordinateSequences
   */
  public void testPackedCoordinateSequence() {
    GeometryFactory pcsFactory = new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY);
    Geometry geom0 = read(pcsFactory, "POLYGON ((210 210, 210 220, 220 220, 220 210, 210 210))");
    Geometry geom1 = read("POLYGON ((225 225, 225 215, 215 215, 215 225, 225 225))");
    CommonBitsOp cbo = new CommonBitsOp(true);
    Geometry result = cbo.intersection(geom0, geom1);
    Geometry expected = geom0.intersection(geom1);
    ///Geometry expected = read("POLYGON ((220 215, 215 215, 215 220, 220 220, 220 215))");
    checkEqual(expected, result);
  }
}
