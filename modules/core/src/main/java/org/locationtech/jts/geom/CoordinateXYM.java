/*
 * Copyright (c) 2018 Vivid Solutions
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom;

/**
 * Coordinate subclass supporting XYM ordinate.
 * <p>
 * This data object is suitable for use with coordinate sequences dimension 3, measures 1.
 *
 * @since 1.16
 */
public class CoordinateXYM extends Coordinate {
  private static final long serialVersionUID = 2842127537691165613L;

/** Standard ordinate index value for, where X is 0 */
  public static final int X = 0;

  /** Standard ordinate index value for, where Y is 1 */
  public static final int Y = 1;

  /** CoordinateXYM does not support Z values. */
  public static final int Z = -1;

  /**
   * Standard ordinate index value for, where M is 3.
   *
   * <p>This constant assumes XYM coordinate sequence definition, please check this assumption using
   * {@link #getDimension()} and {@link #getMeasures()} before use.
   */
  public static final int M = 2;

  /** Default constructor */
  public CoordinateXYM() {
    super();
    this.m = 0.0;
  }

  public CoordinateXYM(double x, double y, double m) {
    super(x, y, Coordinate.NULL_ORDINATE);
    this.m = m;
  }

  public CoordinateXYM(Coordinate coord) {
    super(coord.x,coord.y);
    m = getM();
  }

  public CoordinateXYM(CoordinateXYM coord) {
    super(coord.x,coord.y);
    m = coord.m;
  }

  public CoordinateXYM copy() {
    return new CoordinateXYM(this);
  }
    
  /** The m-measure. */
  protected double m;

  /** The m-measure, if available. */
  public double getM() {
    return m;
  }

  public void setM(double m) {
    this.m = m;
  }
  
  /** The z-ordinate is not supported */
  @Override
  public double getZ() {
      return NULL_ORDINATE;
  }

  /** The z-ordinate is not supported */
  @Override
  public void setZ(double z) {
      throw new IllegalArgumentException("CoordinateXY dimension 2 does not support z-ordinate");
  }
  
  @Override
  public void setCoordinate(Coordinate other)
  {
    x = other.x;
    y = other.y;
    z = other.getZ();
    m = other.getM();
  }
  
  @Override
  public double getOrdinate(int ordinateIndex) {
      switch (ordinateIndex) {
      case X: return x;
      case Y: return y;
      case M: return m;
      }
      throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
  }
  
  @Override
  public void setOrdinate(int ordinateIndex, double value) {
      switch (ordinateIndex) {
      case X:
        x = value;
        break;
      case Y:
        y = value;
        break;
      case M:
        m = value;
        break;
      default:
        throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
    }
  }
  
  public String toString() {
    return "(" + x + ", " + y + " m=" + getM() + ")";
  }
}
