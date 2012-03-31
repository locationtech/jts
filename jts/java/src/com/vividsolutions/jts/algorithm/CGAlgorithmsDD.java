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
package com.vividsolutions.jts.algorithm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.DD;

/**
 * Implements basic computational geometry algorithms using {@link DD} arithmetic.
 * 
 * @author Martin Davis
 *
 */
public class CGAlgorithmsDD
{
  public static int orientationIndex(Coordinate p1, Coordinate p2, Coordinate q)
  {
    DD dx1 = DD.valueOf(p2.x).selfSubtract(p1.x);
    DD dy1 = DD.valueOf(p2.y).selfSubtract(p1.y);
    DD dx2 = DD.valueOf(q.x).selfSubtract(p2.x);
    DD dy2 = DD.valueOf(q.y).selfSubtract(p2.y);

    return signOfDet2x2(dx1, dy1, dx2, dy2);
  }
  
  public static int signOfDet2x2(DD x1, DD y1, DD x2, DD y2)
  {
    DD det = x1.multiply(y2).subtract(y1.multiply(x2));
    if (det.isZero())
      return 0;
    if (det.isNegative())
      return -1;
    return 1;

  }

}
