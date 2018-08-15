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
package org.locationtech.jts.geom.impl;


import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.CoordinateXYZM;
import org.locationtech.jts.geom.Envelope;

/**
 * A {@link CoordinateSequence} implementation based on a packed arrays.
 * In this implementation, {@link Coordinate}s returned by #toArray and #get are copies
 * of the internal values.
 * To change the actual values, use the provided setters.
 * <p>
 * For efficiency, created Coordinate arrays
 * are cached using a soft reference.
 * The cache is cleared each time the coordinate sequence contents are
 * modified through a setter method.
 *
 * @version 1.7
 */
public abstract class PackedCoordinateSequence
    implements CoordinateSequence, Serializable
{
  private static final long serialVersionUID = -3151899011275603L;
  /**
   * The dimensions of the coordinates held in the packed array
   */
  protected int dimension;
  
  /**
   * The number of measures of the coordinates held in the packed array.
   */
  protected int measures;
  
  protected PackedCoordinateSequence(int dimension, int measures ) {
      if (dimension - measures < 2) {
         throw new IllegalArgumentException("Must have at least 2 spatial dimensions");
      }
      this.dimension = dimension;
      this.measures = measures;
  }
  
  /**
   * A soft reference to the Coordinate[] representation of this sequence.
   * Makes repeated coordinate array accesses more efficient.
   */
  protected transient SoftReference<Coordinate[]> coordRef;

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getDimension()
   */
  public int getDimension() {
    return this.dimension;
  }

  @Override
  public int getMeasures() {
    return this.measures;
  }
  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getCoordinate(int)
   */
  public Coordinate getCoordinate(int i) {
    Coordinate[] coords = getCachedCoords();
    if(coords != null)
      return coords[i];
    else
      return getCoordinateInternal(i);
  }
  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getCoordinate(int)
   */
  public Coordinate getCoordinateCopy(int i) {
    return getCoordinateInternal(i);
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getCoordinate(int)
   */
  public void getCoordinate(int i, Coordinate coord) {
    coord.x = getOrdinate(i, 0);
    coord.y = getOrdinate(i, 1);
    if (hasZ()) {
      coord.setZ(getZ(i));
    }
    if (hasM()) {
      coord.setM(getM(i));
    }
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#toCoordinateArray()
   */
  public Coordinate[] toCoordinateArray() {
    Coordinate[] coords = getCachedCoords();
// testing - never cache
    if (coords != null)
      return coords;

    coords = new Coordinate[size()];
    for (int i = 0; i < coords.length; i++) {
      coords[i] = getCoordinateInternal(i);
    }
    coordRef = new SoftReference<Coordinate[]>(coords);

    return coords;
  }

  /**
   * @return
   */
  private Coordinate[] getCachedCoords() {
    if (coordRef != null) {
      Coordinate[] coords = (Coordinate[]) coordRef.get();
      if (coords != null) {
        return coords;
      } else {
        // System.out.print("-");
        coordRef = null;
        return null;
      }
    } else {
      // System.out.print("-");
      return null;
    }

  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getX(int)
   */
  public double getX(int index) {
    return getOrdinate(index, 0);
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getY(int)
   */
  public double getY(int index) {
    return getOrdinate(index, 1);
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequence#getOrdinate(int, int)
   */
  public abstract double getOrdinate(int index, int ordinateIndex);

  /**
   * Sets the first ordinate of a coordinate in this sequence.
   *
   * @param index  the coordinate index
   * @param value  the new ordinate value
   */
  public void setX(int index, double value) {
    coordRef = null;
    setOrdinate(index, 0, value);
  }

  /**
   * Sets the second ordinate of a coordinate in this sequence.
   *
   * @param index  the coordinate index
   * @param value  the new ordinate value
   */
  public void setY(int index, double value) {
    coordRef = null;
    setOrdinate(index, 1, value);
  }

  public String toString()
  {
    return CoordinateSequences.toString(this);
  }

  protected Object readResolve() throws ObjectStreamException {
    coordRef = null;
    return this;
  }
  
  /**
   * Returns a Coordinate representation of the specified coordinate, by always
   * building a new Coordinate object
   *
   * @param index
   * @return
   */
  protected abstract Coordinate getCoordinateInternal(int index);

  /**
   * @see java.lang.Object#clone()
   * @deprecated
   */
  public abstract Object clone();
  
  public abstract PackedCoordinateSequence copy();

  /**
   * Sets the ordinate of a coordinate in this sequence.
   * <br>
   * Warning: for performance reasons the ordinate index is not checked
   * - if it is over dimensions you may not get an exception but a meaningless value.
   *
   * @param index
   *          the coordinate index
   * @param ordinate
   *          the ordinate index in the coordinate, 0 based, smaller than the
   *          number of dimensions
   * @param value
   *          the new ordinate value
   */
  public abstract void setOrdinate(int index, int ordinate, double value);

  /**
   * Packed coordinate sequence implementation based on doubles
   */
  public static class Double extends PackedCoordinateSequence {
    private static final long serialVersionUID = 5777450686367912719L;
    /**
     * The packed coordinate array
     */
    double[] coords;

    /**
     * Builds a new packed coordinate sequence
     *
     * @param coords
     * @param dimension
     * @param measures
     */
    public Double(double[] coords, int dimension, int measures) {
      super(dimension,measures);
      if (coords.length % dimension != 0) {
        throw new IllegalArgumentException("Packed array does not contain "
            + "an integral number of coordinates");
      }
      this.coords = coords;
    }
    
    /**
     * Builds a new packed coordinate sequence out of a float coordinate array
     *
     * @param coordinates
     * @param dimensions
     * @param measures
     */
    public Double(float[] coordinates, int dimension, int measures) {
      super(dimension,measures);
      this.coords = new double[coordinates.length];
      for (int i = 0; i < coordinates.length; i++) {
        this.coords[i] = coordinates[i];
      }
    }
    
    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     * 
     * @param coordinates
     * @param dimension
     */
    public Double(Coordinate[] coordinates, int dimension) {
      this( coordinates, dimension, 0);
    }
    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     *
     * @param coordinates
     * @param dimensions
     * @param measures
     */
    public Double(Coordinate[] coordinates, int dimension, int measures) {
      super(dimension,measures);
      if (coordinates == null)
        coordinates = new Coordinate[0];
      
      coords = new double[coordinates.length * this.dimension];
      for (int i = 0; i < coordinates.length; i++) {
        coords[i * this.dimension] = coordinates[i].x;
        if (this.dimension >= 2)
          coords[i * this.dimension + 1] = coordinates[i].y;
        if (this.dimension >= 3)
          coords[i * this.dimension + 2] = coordinates[i].getOrdinate(2); // Z or M
        if (this.dimension >= 4)
          coords[i * this.dimension + 3] = coordinates[i].getOrdinate(3); // M
      }
    }
    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     *
     * @param coordinates
     */
    public Double(Coordinate[] coordinates) {
      this(coordinates, 3, 0);
    }

    /**
     * Builds a new empty packed coordinate sequence of a given size and dimension
     */
    public Double(int size, int dimension, int measures) {
      super(dimension,measures);	
      coords = new double[size * this.dimension];
    }

    /**
     * @see org.locationtech.jts.geom.CoordinateSequence#getCoordinate(int)
     */
    public Coordinate getCoordinateInternal(int i) {
      double x = coords[i * dimension];
      double y = coords[i * dimension + 1];
      if( dimension == 2 && measures == 0 ) {
	  return new CoordinateXY(x,y);  
      }
      else if (dimension == 3 && measures == 0) {
          double z = coords[i * dimension + 2];
          return new Coordinate(x,y,z);
      }
      else if (dimension == 3 && measures == 1) {
	  double m = coords[i * dimension + 2];     
          return new CoordinateXYM(x,y,m);          
      }
      else if (dimension == 4 && measures == 1) {
	  double z = coords[i * dimension + 2];
	  double m = coords[i * dimension + 3];
	  return new CoordinateXYZM(x,y,z,m);
      }
      return new Coordinate(x, y);
    }

    /**
     * Gets the underlying array containing the coordinate values.
     * 
     * @return the array of coordinate values
     */
    public double[] getRawCoordinates()
    {
      return coords;
    }
    
    /**
     * @see org.locationtech.jts.geom.CoordinateSequence#size()
     */
    public int size() {
      return coords.length / dimension;
    }

    /**
     * @see java.lang.Object#clone()
     * @deprecated
     */
    public Object clone() {
      return copy();
    }
    
    public Double copy() {
      double[] clone = Arrays.copyOf(coords, coords.length);
      return new Double(clone, dimension, measures);
    }
    
    /**
     * @see org.locationtech.jts.geom.CoordinateSequence#getOrdinate(int, int)
     *      Beware, for performance reasons the ordinate index is not checked, if
     *      it's over dimensions you may not get an exception but a meaningless
     *      value.
     */
    public double getOrdinate(int index, int ordinate) {
      return coords[index * dimension + ordinate];
    }

    /**
     * @see com.vividsolutions.jts.geom.PackedCoordinateSequence#setOrdinate(int,
     *      int, double)
     */
    public void setOrdinate(int index, int ordinate, double value) {
      coordRef = null;
      coords[index * dimension + ordinate] = value;
    }

    public Envelope expandEnvelope(Envelope env)
    {
      for (int i = 0; i < coords.length; i += dimension ) {
        env.expandToInclude(coords[i], coords[i + 1]);
      }
      return env;
    }
  }

  /**
   * Packed coordinate sequence implementation based on floats
   */
  public static class Float extends PackedCoordinateSequence {
    private static final long serialVersionUID = -2902252401427938986L;
    /**
     * The packed coordinate array
     */
    float[] coords;

    /**
     * Constructs a packed coordinate sequence from an array of <code>float</code>s
     *
     * @param coords
     * @param dimensions
     */
    public Float(float[] coords, int dimension,int measures) {
	super(dimension,measures);
      if (coords.length % dimension != 0) {
        throw new IllegalArgumentException("Packed array does not contain "
            + "an integral number of coordinates");
      }
      this.coords = coords;
    }

    /**
     * Constructs a packed coordinate sequence from an array of <code>double</code>s
     *
     * @param coordinates
     * @param dimension
     */
    public Float(double[] coordinates, int dimension, int measures) {
	super(dimension,measures);
	this.coords = new float[coordinates.length];
      
      for (int i = 0; i < coordinates.length; i++) {
        this.coords[i] = (float) coordinates[i];
      }
    }

    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     * 
     * @param coordinates
     * @param dimension
     */
    public Float(Coordinate[] coordinates, int dimension) {
      this( coordinates, dimension, 0);
    }
    
    /**
     * Constructs a packed coordinate sequence out of a coordinate array
     *
     * @param coordinates
     */
    public Float(Coordinate[] coordinates, int dimension, int measures) {
	    super(dimension,measures);
      if (coordinates == null)
        coordinates = new Coordinate[0];
      
      coords = new float[coordinates.length * this.dimension];
      for (int i = 0; i < coordinates.length; i++) {
        coords[i * this.dimension] = (float) coordinates[i].x;
        if (this.dimension >= 2)
          coords[i * this.dimension + 1] = (float) coordinates[i].y;
        if (this.dimension >= 3)
          coords[i * this.dimension + 2] = (float) coordinates[i].getZ();
      }
    }

    /**
     * Constructs an empty packed coordinate sequence of a given size and dimension
     */
    public Float(int size, int dimension,int measures) {
	super(dimension,measures);
      coords = new float[size * this.dimension];
    }

    /**
     * @see org.locationtech.jts.geom.CoordinateSequence#getCoordinate(int)
     */
    public Coordinate getCoordinateInternal(int i) {
      double x = coords[i * dimension];
      double y = coords[i * dimension + 1];
      if( dimension == 2 && measures == 0 ) {
	  return new CoordinateXY(x,y);  
      }
      else if (dimension == 3 && measures == 0) {
          double z = coords[i * dimension + 2];
          return new Coordinate(x,y,z);
      }
      else if (dimension == 3 && measures == 1) {
	  double m = coords[i * dimension + 2];     
          return new CoordinateXYM(x,y,m);          
      }
      else if (dimension == 4 && measures == 1) {
	  double z = coords[i * dimension + 2];
	  double m = coords[i * dimension + 3];
	  return new CoordinateXYZM(x,y,z,m);
      }
      return new Coordinate(x, y);
    }

    /**
     * Gets the underlying array containing the coordinate values.
     * 
     * @return the array of coordinate values
     */
    public float[] getRawCoordinates()
    {
      return coords;
    }
    
    /**
     * @see org.locationtech.jts.geom.CoordinateSequence#size()
     */
    public int size() {
      return coords.length / dimension;
    }

    /**
     * @see java.lang.Object#clone()
     * @deprecated
     */
    public Object clone() {
      return copy();
    }
    
    public Float copy() {
      float[] clone = Arrays.copyOf(coords, coords.length);
      return new Float(clone, dimension,measures);
    }

    /**
     * @see org.locationtech.jts.geom.CoordinateSequence#getOrdinate(int, int)
     *      For performance reasons the ordinate index is not checked.
     *      If it is larger than the dimension a meaningless
     *      value may be returned.
     */
    public double getOrdinate(int index, int ordinate) {
      return coords[index * dimension + ordinate];
    }

    /**
     * @see com.vividsolutions.jts.geom.PackedCoordinateSequence#setOrdinate(int,
     *      int, double)
     */
    public void setOrdinate(int index, int ordinate, double value) {
      coordRef = null;
      coords[index * dimension + ordinate] = (float) value;
    }

    public Envelope expandEnvelope(Envelope env)
    {
      for (int i = 0; i < coords.length; i += dimension ) {
        env.expandToInclude(coords[i], coords[i + 1]);
      }
      return env;
    }

  }

}