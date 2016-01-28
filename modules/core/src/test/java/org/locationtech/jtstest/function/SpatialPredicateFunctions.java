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
package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.Geometry;

/**
 * Implementations for spatial predicate functions.
 *
 * @author Martin Davis
 *
 */
public class SpatialPredicateFunctions {

  public static String relate(Geometry a, Geometry b) {
    return a.relate(b).toString();
  }
  public static boolean intersects(Geometry a, Geometry b) {    return a.intersects(b);    }
  public static boolean crosses(Geometry a, Geometry b) {    return a.crosses(b);    }
  public static boolean disjoint(Geometry a, Geometry b) {    return a.disjoint(b);    }
  public static boolean equals(Geometry a, Geometry b) {    return a.equals(b);    }
  public static boolean contains(Geometry a, Geometry b) {    return a.contains(b);    }
  public static boolean covers(Geometry a, Geometry b) {    return a.covers(b);    }
  public static boolean coveredBy(Geometry a, Geometry b) {    return a.coveredBy(b);    }
  public static boolean within(Geometry a, Geometry b) {    return a.within(b);    }
  public static boolean overlaps(Geometry a, Geometry b) {    return a.overlaps(b);    }
  public static boolean touches(Geometry a, Geometry b) {    return a.touches(b);    }
}
