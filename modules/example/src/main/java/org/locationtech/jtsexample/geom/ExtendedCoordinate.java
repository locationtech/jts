
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
package org.locationtech.jtsexample.geom;

import org.locationtech.jts.geom.*;


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
