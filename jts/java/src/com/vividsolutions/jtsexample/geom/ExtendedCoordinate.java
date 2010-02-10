
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
package com.vividsolutions.jtsexample.geom;

import com.vividsolutions.jts.geom.*;


/**
 * @version 1.7
 */
public class ExtendedCoordinate
    extends Coordinate
{
    private static final long serialVersionUID = 8527484784733305576L;
  // A Coordinate subclass should provide all of these methods

  /**
   * Default constructor
   */
  public ExtendedCoordinate()
  {
    super();
    this.m = 0.0;
  }

  public ExtendedCoordinate(double x, double y, double z, double m)
  {
    super(x, y, z);
    this.m = m;
  }

  public ExtendedCoordinate(Coordinate coord)
  {
    super(coord);
    if (coord instanceof ExtendedCoordinate)
      m = ((ExtendedCoordinate) coord).m;
    else
      m = Double.NaN;
  }

  public ExtendedCoordinate(ExtendedCoordinate coord)
  {
    super(coord);
    m = coord.m;
  }

  /**
   * An example of extended data.
   * The m variable holds a measure value for linear referencing
   */

  private double m;

  public double getM() { return m; }
  public void setM(double m) { this.m = m; }

  public String toString()
  {
    String stringRep = x + " " + y + " m=" + m;
    return stringRep;
  }
}
