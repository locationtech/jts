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
 * Coordinate subclass supporting XY ordinate.
 * <p>
 * This data object is suitable for use with coordinate sequences dimension 3, measures 1.
 * The {@link Coordinate#Z} field is visible, but intended to be ignored.
 *
 * @since 1.16
 */
public class CoordinateXY extends Coordinate {
  private static final long serialVersionUID = 3532307803472313082L;

/** Standard ordinate index value for, where X is 0 */
  public static final int X = 0;

  /** Standard ordinate index value for, where Y is 1 */
  public static final int Y = 1;

  /** CoordinateXY does not support Z values. */
  public static final int Z = -1;

  /** CoordinateXY does not support M measures. */
   
  public static final int M = -1;

  /** Default constructor */
  public CoordinateXY() {
    super();
  }

  public CoordinateXY(double x, double y) {
    super(x, y, Coordinate.NULL_ORDINATE);
  }

  public CoordinateXY(Coordinate coord) {
    super(coord.x,coord.y);
  }

  public CoordinateXY(CoordinateXY coord) {
    super(coord.x,coord.y);  
  }

  public CoordinateXY copy() {
    return new CoordinateXY(this);
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
  }
  
  @Override
  public double getOrdinate(int ordinateIndex) {
      switch (ordinateIndex) {
      case X: return x;
      case Y: return y;
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
      default:
        throw new IllegalArgumentException("Invalid ordinate index: " + ordinateIndex);
    }
  }
  
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}