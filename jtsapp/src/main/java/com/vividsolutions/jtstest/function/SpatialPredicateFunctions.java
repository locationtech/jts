/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jtstest.function;

import com.vividsolutions.jts.geom.Geometry;

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
