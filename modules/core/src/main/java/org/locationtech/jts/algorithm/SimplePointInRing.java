
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
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.*;

/**
 * Tests whether a {@link Coordinate} lies inside
 * a ring, using a linear-time algorithm.
 *
 * @version 1.7
 */
public class SimplePointInRing
  implements PointInRing
{

  private Coordinate[] pts;

  public SimplePointInRing(LinearRing ring)
  {
    pts = ring.getCoordinates();
  }

  public boolean isInside(Coordinate pt)
  {
    return CGAlgorithms.isPointInRing(pt, pts);
  }
}
