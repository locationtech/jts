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

package org.locationtech.jtstest;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.*;

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