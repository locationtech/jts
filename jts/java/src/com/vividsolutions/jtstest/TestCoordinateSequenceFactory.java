package com.vividsolutions.jtstest;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.*;

/**
 * Create the CoordinateSequenceFactory to be used in tests
 */
public class TestCoordinateSequenceFactory {

  public static CoordinateSequenceFactory instance()
  {
    return CoordinateArraySequenceFactory.instance();
//    return new PackedCoordinateSequenceFactory();
//    return new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.FLOAT, 2);
  }

  private TestCoordinateSequenceFactory() {
  }
}