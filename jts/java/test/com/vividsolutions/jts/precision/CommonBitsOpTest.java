package com.vividsolutions.jts.precision;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

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
