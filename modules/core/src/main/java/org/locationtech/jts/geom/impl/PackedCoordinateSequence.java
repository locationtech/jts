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

import org.locationtech.jts.geom.*;

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

  /** the minimum number of spatial dimensions for coordinates in the sequence */
  private static final int MIN_SPATIAL_DIMENSION = 2;

  /** the default number of dimensions for coordinates in the sequence */
  static final int DEFAULT_DIMENSION = 3;

  /** the default number of measures for coordinates in the sequence */
  static final int DEFAULT_MEASURES = 0;

  /** the initial z-ordinate value */
  private static double INITIAL_Z_VALUE = 0d;

  /**
   * Sets the initial z-ordinate value.
   *
   * @param value a value
   */
  public static void setInitialZValue(double value) {
    INITIAL_Z_VALUE = value;
  }

  /**
   * Gets the initial z-ordinate value.
   */
  public static double getInitialZValue() { return INITIAL_Z_VALUE; }

  /**
   * The dimensions of the coordinates held in the packed array
   */
  protected final int dimension;

  /**
   * Method to check if a dimension value is allowed
   * @param dimension a dimension value
   * @param measures the number of measure dimensions
   */
  private static void checkDimension(int dimension, int measures) {
    if (dimension - measures < PackedCoordinateSequence.MIN_SPATIAL_DIMENSION)
      throw new IllegalArgumentException(
              "Must have at least " + PackedCoordinateSequence.MIN_SPATIAL_DIMENSION + " spatial dimensions ");
  }

  /**
   * The number of measures of the coordinates held in the packed array.
   */
  protected final int measures;

  /**
   * Creates an instance of this class
   * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
   * @param measures the number of measure-ordinates each {@link Coordinate} in this sequence has.
   */
  protected PackedCoordinateSequence(int dimension, int measures ) {
    checkDimension(dimension, measures);
    this.dimension = dimension;
    this.measures = measures;
  }
  
  /**
   * A soft reference to the Coordinate[] representation of this sequence.
   * Makes repeated coordinate array accesses more efficient.
   */
  protected transient SoftReference<Coordinate[]> coordRef;

  /**
   * @see CoordinateSequence#getDimension()
   */
  public int getDimension() {
    return this.dimension;
  }

  /**
   * @see CoordinateSequence#getMeasures()
   */
  @Override
  public int getMeasures() {
    return this.measures;
  }

  /**
   * @see CoordinateSequence#getCoordinate(int)
   */
  public Coordinate getCoordinate(int i) {
    Coordinate[] coords = getCachedCoords();
    if(coords != null)
      return coords[i];
    else
      return getCoordinateInternal(i);
  }
  /**
   * @see CoordinateSequence#getCoordinate(int)
   */
  public Coordinate getCoordinateCopy(int i) {
    return getCoordinateInternal(i);
  }

  /**
   * @see CoordinateSequence#getCoordinate(int)
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
   * @see CoordinateSequence#toCoordinateArray()
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
   * Tests if the {@link #coordRef}, that possibly {@link SoftReference}s
   * a previously created array of {@link Coordinate}s from this
   * sequence, is set and still 'alive'. If so it returns the array.
   *
   * @return an array of {@link Coordinate}s.
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
   * @see CoordinateSequence#getX(int)
   */
  public double getX(int index) {
    return getOrdinate(index, CoordinateSequence.X);
  }

  /**
   * @see CoordinateSequence#getY(int)
   */
  public double getY(int index) {
    return getOrdinate(index, CoordinateSequence.Y);
  }

  /**
   * @see CoordinateSequence#getOrdinate(int, int)
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
    setOrdinate(index, CoordinateSequence.X , value);
  }

  /**
   * Sets the second ordinate of a coordinate in this sequence.
   *
   * @param index  the coordinate index
   * @param value  the new ordinate value
   */
  public void setY(int index, double value) {
    coordRef = null;
    setOrdinate(index, CoordinateSequence.Y, value);
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
   * @param index  the coordinate index
   * @return  the {@link Coordinate} at the given index
   */
  protected abstract Coordinate getCoordinateInternal(int index);

  /**
   * @see java.lang.Object#clone()
   * @see CoordinateSequence#clone()
   * @deprecated
   */
  public abstract Object clone();

  /**
   * @see CoordinateSequence#copy()
   */
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
   * Utility function to get a dimension value from a sequence.
   * If the sequence is <c>null</c> {@linkplain #DEFAULT_DIMENSION} is returned.
   * @param sequence a sequence
   * @return the number of dimensions
   */
  protected static int getDimension(CoordinateSequence sequence) {
    if (sequence == null)
      return DEFAULT_DIMENSION;
    return sequence.getDimension();
  }

  /**
   * Utility function to get a measures value from a sequence.
   * If the sequence is <c>null</c> {@linkplain #DEFAULT_MEASURES} is returned.
   * @param sequence a sequence
   * @return the number of measures
   */
  protected static int getMeasures(CoordinateSequence sequence) {
    if (sequence == null)
      return DEFAULT_MEASURES;
    return sequence.getMeasures();
  }

  /**
   * Utility function to get the size of a sequence.
   * If the sequence is <c>null</c> 0 is returned.
   * @param sequence a sequence
   * @return the size
   */
  protected static int getSize(CoordinateSequence sequence) {
    if (sequence == null)
      return 0;
    return sequence.size();
  }

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
     * @param coords  an array of <c>double</c> values that contains the ordinate values of the sequence
     * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
     * @param measures the number of measure-ordinates each {@link Coordinate} in this sequence has.
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
     * @param coords  an array of <c>float</c> values that contains the ordinate values of the sequence
     * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
     * @param measures the number of measure-ordinates each {@link Coordinate} in this sequence has.
     */
    public Double(float[] coords, int dimension, int measures) {
      super(dimension,measures);
      if (coords.length % dimension != 0) {
        throw new IllegalArgumentException("Packed array does not contain "
                + "an integral number of coordinates");
      }
      this.coords = new double[coords.length];
      for (int i = 0; i < coords.length; i++) {
        this.coords[i] = coords[i];
      }
    }
    
    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     * 
     * @param coordinates an array of {@link Coordinate}s
     * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
     */
    public Double(Coordinate[] coordinates, int dimension) {
      this( coordinates, dimension, DEFAULT_MEASURES);
    }
    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     *
     * @param coordinates an array of {@link Coordinate}s
     * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
     * @param measures the number of measure-ordinates each {@link Coordinate} in this sequence has.
     */
    public Double(Coordinate[] coordinates, int dimension, int measures) {
      super(dimension,measures);

      if (coordinates == null || coordinates.length == 0) {
        coords = new double[0];
        return;
      }

      // Test input coordinate array
      int coordDimension = Coordinates.dimension(coordinates[0]);
      int coordMeasures = Coordinates.measures(coordinates[0]);
      boolean hasZ = coordDimension-coordMeasures > 2;
      boolean addZ = !hasZ & hasZ();
      boolean removeZ = hasZ & !this.hasZ();
      boolean hasZM = this.hasZ() & this.hasM();

      int skipJ = Math.max(0, dimension - coordDimension);
      coords = new double[coordinates.length * this.dimension];
      for (int i = 0, j = 0; i < coordinates.length; i++, j += skipJ) {
        coords[j++] = coordinates[i].x;
        coords[j++] = coordinates[i].y;
        if (coordDimension == 2 || dimension == 2) continue;
        if (addZ) { // increment counter
          j++;
        } else if (!removeZ) { // don't skip if z is to be preserved
          coords[j++] = coordinates[i].getOrdinate(2); // Z or M
        }
        if (coordDimension == 3 || j%dimension == 0) continue;
        coords[j++] = coordinates[i].getOrdinate(3); // M
      }

      if (!hasZ)
        initializeZValues();
    }
    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     *
     * @param coordinates an array of {@link Coordinate}s
     */
    public Double(Coordinate[] coordinates) {
      this(coordinates, DEFAULT_DIMENSION, DEFAULT_MEASURES);
    }

    /**
     * Builds a new empty packed coordinate sequence of a given size and dimension
     *
     * @param size the number of coordinates in this sequence
     * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
     * @param measures the number of measure-ordinates each {@link Coordinate} in this sequence has.
     */
    public Double(int size, int dimension, int measures) {
      super(dimension,measures);
      coords = new double[size * this.dimension];
      initializeZValues();
    }

    /**
     * Creates an instance of this class based on the given sequence
     * @param sequence A {@link CoordinateSequence}
     */
    Double(CoordinateSequence sequence) {
      super(getDimension(sequence), getMeasures(sequence));

      // if sequence is an instance of Double, we can shortcut
      if (sequence instanceof Double) {
        double[] dblCoords = ((Double)sequence).coords;
        this.coords = Arrays.copyOf(dblCoords, dblCoords.length);
      }
      // else we have to copy by hand.
      else {
        int size = getSize(sequence);
        coords = new double[size*dimension];
        for (int i = 0, k = 0; i < size; i++)
          for (int j = 0; j < dimension; j++)
            this.coords[k++] = sequence.getOrdinate(i, j);
      }
    }

    /**
     * @see PackedCoordinateSequence#getCoordinate(int)
     */
    public Coordinate getCoordinateInternal(int i) {
      double x = coords[i * dimension];
      double y = coords[i * dimension + 1];
      if (dimension == 2/* && measures == 0*/) {
        return new CoordinateXY(x, y);
      } else if (dimension == 3 && measures == 0) {
        double z = coords[i * dimension + 2];
        return new Coordinate(x, y, z);
      } else if (dimension == 3 && measures == 1) {
        double m = coords[i * dimension + 2];
        return new CoordinateXYM(x, y, m);
      } else if (dimension == 4 && measures == 1) {
        double z = coords[i * dimension + 2];
        double m = coords[i * dimension + 3];
        return new CoordinateXYZM(x, y, z, m);
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
     * @see CoordinateSequence#size()
     */
    public int size() {
      return coords.length / dimension;
    }

    /**
     * @see java.lang.Object#clone()
     * @see PackedCoordinateSequence#clone()
     * @deprecated
     */
    public Object clone() {
      return copy();
    }

    /**
     * @see PackedCoordinateSequence#size()
     */
    public Double copy() {
      double[] clone = Arrays.copyOf(coords, coords.length);
      return new Double(clone, dimension, measures);
    }
    
    /**
     * @see PackedCoordinateSequence#getOrdinate(int, int)
     *      Beware, for performance reasons the ordinate index is not checked, if
     *      it's over dimensions you may not get an exception but a meaningless
     *      value.
     */
    public double getOrdinate(int index, int ordinate) {
      return coords[index * dimension + ordinate];
    }

    /**
     * @see PackedCoordinateSequence#setOrdinate(int, int, double)
     */
    public void setOrdinate(int index, int ordinate, double value) {
      coordRef = null;
      coords[index * dimension + ordinate] = value;
    }

    /**
     * @see CoordinateSequence#expandEnvelope(Envelope)
     */
    public Envelope expandEnvelope(Envelope env)
    {
      for (int i = 0; i < coords.length; i += dimension ) {
        env.expandToInclude(coords[i], coords[i + 1]);
      }
      return env;
    }

    /**
     * Initializes all z-ordinate values to {@link #INITIAL_Z_VALUE}
     */
    private void initializeZValues()
    {
      // this is the default when creating arrays, so exit
      if (INITIAL_Z_VALUE == 0d)
        return;

      if (hasZ()) {
        for (int i = CoordinateSequence.Z; i < coords.length; i+=dimension)
          coords[i] = INITIAL_Z_VALUE;
      }
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
     * @param coords  an array of <c>float</c> values that contains the ordinate values of the sequence
     * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
     * @param measures the number of measure-ordinates each {@link Coordinate} in this sequence has.
     */
    public Float(float[] coords, int dimension, int measures) {
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
     * @param coords  an array of <c>double</c> values that contains the ordinate values of the sequence
     * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
     * @param measures the number of measure-ordinates each {@link Coordinate} in this sequence has.
     */
    public Float(double[] coords, int dimension, int measures) {
      super(dimension,measures);
      if (coords.length % dimension != 0) {
        throw new IllegalArgumentException("Packed array does not contain "
                + "an integral number of coordinates");
      }
      this.coords = new float[coords.length];
      
      for (int i = 0; i < coords.length; i++) {
        this.coords[i] = (float) coords[i];
      }
    }

    /**
     * Builds a new packed coordinate sequence out of a coordinate array
     *
     * @param coordinates an array of {@link Coordinate}s
     * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
     */
    public Float(Coordinate[] coordinates, int dimension) {
      this( coordinates, dimension, DEFAULT_MEASURES);
    }
    
    /**
     * Constructs a packed coordinate sequence out of a coordinate array
     *
     * @param coordinates an array of {@link Coordinate}s
     * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
     * @param measures the number of measure-ordinates each {@link Coordinate} in this sequence has.
     */
    public Float(Coordinate[] coordinates, int dimension, int measures) {
      super(dimension,measures);

      if (coordinates == null || coordinates.length == 0) {
        coords = new float[0];
        return;
      }

      // Test input coordinate array
      int coordDimension = Coordinates.dimension(coordinates[0]);
      int coordMeasures = Coordinates.measures(coordinates[0]);
      boolean hasZ = coordDimension-coordMeasures > 2;
      boolean addZ = !hasZ & hasZ();
      boolean removeZ = hasZ & !this.hasZ();

      int skipJ = Math.max(0, dimension - coordDimension);
      coords = new float[coordinates.length * this.dimension];
      for (int i = 0, j = 0; i < coordinates.length; i++, j += skipJ) {
        coords[j++] = (float)coordinates[i].x;
        coords[j++] = (float)coordinates[i].y;
        if (coordDimension == 2 || dimension == 2) continue;
        if (addZ) { // increment counter
          j++;
        } else if (!removeZ) { // don't skip if z is to be preserved
          coords[j++] = (float)coordinates[i].getOrdinate(2); // Z or M
        }
        if (coordDimension == 3 || j%dimension == 0) continue;
        coords[j++] = (float)coordinates[i].getOrdinate(3); // M
      }

      if (!hasZ)
        initializeZValues();
    }

    /**
     * Constructs an empty packed coordinate sequence of a given size and dimension
     *
     * @param size the number of coordinates in this sequence
     * @param dimension the total number of ordinates that make up a {@link Coordinate} in this sequence.
     * @param measures the number of measure-ordinates each {@link Coordinate} in this sequence has.
     */
    public Float(int size, int dimension,int measures) {
      super(dimension,measures);
      coords = new float[size * this.dimension];
      initializeZValues();
    }

    /**
     * Creates an instance of this class based on the given sequence
     * @param sequence A {@link CoordinateSequence}
     */
    Float(CoordinateSequence sequence) {
      super(getDimension(sequence), getMeasures(sequence));

      // if sequence is an instance of Double, we can shortcut
      if (sequence instanceof Float) {
        float[] fltCoords = ((Float)sequence).coords;
        this.coords = Arrays.copyOf(fltCoords, fltCoords.length);
      }
      // else we have to copy by hand.
      else {
        int size = getSize(sequence);
        coords = new float[size*dimension];
        for (int i = 0, k = 0; i < size; i++)
          for (int j = 0; j < dimension; j++)
            this.coords[k++] = (float)sequence.getOrdinate(i, j);
      }
    }

    /**
     * @see PackedCoordinateSequence#getCoordinate(int)
     */
    public Coordinate getCoordinateInternal(int i) {
      double x = coords[i * dimension];
      double y = coords[i * dimension + 1];
      if (dimension == 2 && measures == 0) {
        return new CoordinateXY(x, y);
      } else if (dimension == 3 && measures == 0) {
        double z = coords[i * dimension + 2];
        return new Coordinate(x, y, z);
      } else if (dimension == 3 && measures == 1) {
        double m = coords[i * dimension + 2];
        return new CoordinateXYM(x, y, m);
      } else if (dimension == 4 && measures == 1) {
        double z = coords[i * dimension + 2];
        double m = coords[i * dimension + 3];
        return new CoordinateXYZM(x, y, z, m);
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
     * @see CoordinateSequence#size()
     */
    public int size() {
      return coords.length / dimension;
    }

    /**
     * @see java.lang.Object#clone()
     * @see PackedCoordinateSequence#clone()
     * @deprecated
     */
    public Object clone() {
      return copy();
    }

    /**
     * @see PackedCoordinateSequence#copy()
     */
    public Float copy() {
      float[] clone = Arrays.copyOf(coords, coords.length);
      return new Float(clone, dimension, measures);
    }

    /**
     * @see PackedCoordinateSequence#getOrdinate(int, int)
     *      For performance reasons the ordinate index is not checked.
     *      If it is larger than the dimension a meaningless
     *      value may be returned.
     */
    public double getOrdinate(int index, int ordinate) {
      return coords[index * dimension + ordinate];
    }

    /**
     * @see PackedCoordinateSequence#setOrdinate(int, int, double)
     */
    public void setOrdinate(int index, int ordinate, double value) {
      coordRef = null;
      coords[index * dimension + ordinate] = (float) value;
    }

    /**
     * @see CoordinateSequence#expandEnvelope(Envelope)
     */
    public Envelope expandEnvelope(Envelope env)
    {
      for (int i = 0; i < coords.length; i += dimension ) {
        env.expandToInclude(coords[i], coords[i + 1]);
      }
      return env;
    }

    /**
     * Initializes all z-ordinate values to {@link #INITIAL_Z_VALUE}
     */
    private void initializeZValues() {
      // this is the default when creating arrays, so exit
      if (INITIAL_Z_VALUE == 0d)
        return;

      if (hasZ()) {
        for (int i = CoordinateSequence.Z; i < coords.length; i+=dimension)
          coords[i] = (float)INITIAL_Z_VALUE;
      }
    }
  }

}