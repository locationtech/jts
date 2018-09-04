
/*
 * Copyright (c) 2018 Felix Obermaier.
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

import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;

import java.io.Serializable;

/**
 * A wrapper around a coordinate sequence that always
 * ensures that a sequence is large enough to store ordinate
 * values.
 */
public class ExtendableCoordinateSequence implements CoordinateSequence, Serializable {

  /** The serial version UID */
  private static final long serialVersionUID = 1167611362034559688L;

  /** The default initial capacity */
  static final int INITIAL_CAPACITY = 10;

  /** The factory to use when extending the current sequence*/
  private final CoordinateSequenceFactory csFactory;
  /** The current sequence */
  private CoordinateSequence sequence;
  /** The current size of the sequence */
  private int size = 0;

  /**
   * Creates an instance of this class using the provided coordinate sequence factory and dimension
   * The coordinate seqeunce factory that is used is determined by
   * {@link GeometryFactory#getDefaultCoordinateSequenceFactory()}.
   * The initial capacity is set to {@link #INITIAL_CAPACITY}.
   *
   * @param dimension a dimension
   */
  public ExtendableCoordinateSequence(int dimension) {
    this(new GeometryFactory().getCoordinateSequenceFactory(), dimension);
  }
  /**
   * Creates an instance of this class using the provided coordinate sequence factory and dimension
   * The initial capacity is set to {@link #INITIAL_CAPACITY}.
   *
   * @param csFactory a coordinate sequence factory
   * @param dimension a dimension
   */
  public ExtendableCoordinateSequence(CoordinateSequenceFactory csFactory, int dimension) {
    this(csFactory, INITIAL_CAPACITY, dimension);
  }

  /**
   * Creates an instance of this class using the provided coordinate sequence factory,
   * initial capacity and dimension
   *
   * @param csFactory           a coordinate sequence factory
   * @param initialCapacity     an initial capacity
   * @param dimension           a dimension
   */
  public ExtendableCoordinateSequence(CoordinateSequenceFactory csFactory, int initialCapacity, int dimension) {

    if (csFactory == null)
      throw new IllegalArgumentException("csFactory must not be null");

    if (initialCapacity < 1)
      throw new IllegalArgumentException("initialCapacity must be > 0");

    this.csFactory = csFactory;
    this.sequence = csFactory.create(initialCapacity, dimension);
  }

  /**
   * Creates an instance of this class using the provided coordinate sequence factory, size and coordinate sequence.
   *
   * @param csFactory a coordinate sequence factory
   * @param size      a size
   * @param sequence  a sequence
   */
  private ExtendableCoordinateSequence(CoordinateSequenceFactory csFactory, int size, CoordinateSequence sequence) {

    /* this is private, we ensure this elsewhere
    if (csFactory == null)
      throw new IllegalArgumentException("csFactory must not be null");

    if (sequence instanceof ExtendableCoordinateSequence)
      throw new IllegalArgumentException("sequence must not be an ExtendableCoordinateSequence");
     */
    this.csFactory = csFactory;
    this.sequence = sequence;
    this.size = size;
  }

  /** @see CoordinateSequence#size() */
  public int size() {
    return this.size;
  }

  /** @see CoordinateSequence#getDimension() */
  public int getDimension() {
    return this.sequence.getDimension();
  }

  /** @see CoordinateSequence#getCoordinate(int) */
  public Coordinate getCoordinate(int index) {
    checkSize(index);
    return this.sequence.getCoordinate(index);
  }

  /** @see CoordinateSequence#getCoordinate(int, Coordinate) */
  public void getCoordinate(int index, Coordinate out) {
    checkSize(index);
    this.sequence.getCoordinate(index, out);
  }

  /** @see CoordinateSequence#getCoordinateCopy(int) */
  public Coordinate getCoordinateCopy(int index) {
    checkSize(index);
    return this.sequence.getCoordinateCopy(index);
  }

  /** @see CoordinateSequence#getX(int) */
  public double getX(int index) {
    checkSize(index);
    return this.sequence.getX(index);
  }

  /** @see CoordinateSequence#getY(int) */
  public double getY(int index) {
    checkSize(index);
    return this.sequence.getY(index);
  }

  /** @see CoordinateSequence#getOrdinate(int, int) */
  public double getOrdinate(int index, int ordinate) {
    checkSize(index);
    return this.sequence.getOrdinate(index, ordinate);
  }

  /** @see CoordinateSequence#setOrdinate(int, int, double) */
  public void setOrdinate(int index, int ordinate, double value) {

    // ensure capacity
    ensureCapacity(index + 1);

    // set value in sequence
    this.sequence.setOrdinate(index, ordinate, value);

    // adjust size
    if (!(index < size)) size = index + 1;
  }

  /** @see CoordinateSequence#expandEnvelope(Envelope) */
  public Envelope expandEnvelope(Envelope env) {

    for (int i = 0; i < size; i++ )
      env.expandToInclude(sequence.getX(i), sequence.getY(i));
    return env;
  }

  /** @see CoordinateSequence#toCoordinateArray() */
  public Coordinate[] toCoordinateArray() {
    Coordinate[] res = new Coordinate[this.size];
    for (int i = 0; i < size; i++)
      res[i] = this.sequence.getCoordinate(i);
    return res;
  }

  /**
   * @see CoordinateSequence#clone()
   * @deprecated use {@link #copy()}
   */
  public Object clone() {
    return this.copy();
  }

  /** @see CoordinateSequence#copy() */
  public ExtendableCoordinateSequence copy() {
    return new ExtendableCoordinateSequence(this.csFactory, this.size, this.sequence.copy());
  }

  /**
   * Creates a copy of the <b>used</b> portion of the underlying sequence and returns that.
   * The underlying sequence itself is untouched.
   * 
   * @return A sequence
   */
  public CoordinateSequence truncated() {
    CoordinateSequence res = csFactory.create(this.size, this.sequence.getDimension());
    CoordinateSequences.copy(this.sequence, 0, res, 0, this.size);
    return res;
  }

  /**
   * Gets a value indicating the capacity of the underlying sequence
   *
   * @return the capacity
   */
  public int getCapacity() {
    return this.sequence.size();
  }

  /**
   * Tests if {@code index} is in the allowed bounds. If not, the bounds are extended.
   * 
   * @param minCapacity the minimal capacity
   */
  private void ensureCapacity(int minCapacity) {
    if (minCapacity <= 0)
      throw new IllegalArgumentException("minCapacity < 0");

    // check if the current capacity is sufficient
    int oldCapacity = getCapacity();
    if (minCapacity <= oldCapacity)
      return;

    // compute the new capacity (see ArrayList implementation)
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    // to mimic GrowableCoordinateSequence use this
    //int newCapacity = oldCapacity + 50;
    if (newCapacity < minCapacity) newCapacity = minCapacity;

    // create the new sequence
    CoordinateSequence newSequence = csFactory.create(newCapacity, this.sequence.getDimension());
    if (this.sequence instanceof CoordinateArraySequence) {
      // performance improvement for CoordinateArraySequence
      System.arraycopy(this.sequence.toCoordinateArray(), 0, newSequence.toCoordinateArray(), 0, this.sequence.size());
    }
    else if (this.sequence instanceof PackedCoordinateSequence.Double) {
      // performance improvement for PackedCoordinateSequence.Double
      double[] srcSeq = ((PackedCoordinateSequence.Double)this.sequence).getRawCoordinates();
      double[] tgtSeq = ((PackedCoordinateSequence.Double)newSequence).getRawCoordinates();
      System.arraycopy(srcSeq, 0, tgtSeq, 0, srcSeq.length);
    }
    else if (this.sequence instanceof PackedCoordinateSequence.Float) {
      // performance improvement for PackedCoordinateSequence.Float
      float[] srcSeq = ((PackedCoordinateSequence.Float)this.sequence).getRawCoordinates();
      float[] tgtSeq = ((PackedCoordinateSequence.Float)newSequence).getRawCoordinates();
      System.arraycopy(srcSeq, 0, tgtSeq, 0, srcSeq.length);
    }
    else
    {
      // for all other sequences we need to copy by hand.
      CoordinateSequences.copy(this.sequence, 0, newSequence, 0, this.sequence.size());
    }

    this.sequence = newSequence;
  }

  /**
   * Adds a point to the sequence
   * @param pt a point
   */
  public void add(Coordinate pt) {
    add(pt.x, pt.y, pt.z);
  }

  /**
   * Adds a point defined by {@code x}- and {@code y}-ordinates
   * @param x a x-ordinate
   * @param y a y-ordinate
   */
  public void add(double x, double y) {
    //add(x, y, Double.NaN);
    // get the index for the new sequence
    int index = this.size();

    // add x- and y-ordinates
    this.setOrdinate(index, CoordinateSequence.X, x);
    this.setOrdinate(index, CoordinateSequence.Y, y);
  }

  /**
   * Adds a point defined by {@code x}-, {@code y}- and {@code z}-ordinates
   * @param x a x-ordinate
   * @param y a y-ordinate
   * @param z a z-ordinate
   */
  public void add(double x, double y, double z) {
    //add(x, y, z, Double.NaN);

    // get the index for the new sequence
    int index = this.size();
    // add x- and y-ordinates
    this.setOrdinate(index, CoordinateSequence.X, x);
    this.setOrdinate(index, CoordinateSequence.Y, y);
    if (getDimension() == 2) return;
    this.setOrdinate(index, CoordinateSequence.Z, z);
  }

  /**
   * Adds a point defined by {@code x}-, {@code y}-, {@code z}- and {@code m}-ordinates
   * @param x a x-ordinate
   * @param y a y-ordinate
   * @param z a z-ordinate
   * @param m a m-ordinate
   */
  public void add(double x, double y, double z, double m) {
    // get the index for the new sequence
    int index = this.size();

    // add x- and y-ordinates
    this.setOrdinate(index, CoordinateSequence.X, x);
    this.setOrdinate(index, CoordinateSequence.Y, y);
    if (getDimension() == 2) return;
    // add z-ordinate
    this.setOrdinate(index, CoordinateSequence.Z, z);
    if (getDimension() == 3) return;
    // add m-ordinate
    this.setOrdinate(index, CoordinateSequence.M, m);
  }

  /**
   * Inserts a point at the given index
   * @param index an index
   * @param p     a point
   */
  public void insertAt(int index, Coordinate p) {
    insertAt(index, p.x, p.y, p.z);
  }

  /**
   * Inserts a point defined by {@code x}- and {@code y}-ordinates
   * at the given index.
   * @param index an index
   * @param x a x-ordinate
   * @param y a y-ordinate
   */
  public void insertAt(int index, double x, double y) {
    insertAt(index, x, y, Double.NaN);
  }

  /**
   * Inserts a point defined by {@code x}-, {@code y}- and {@code m}-ordinates
   * at the given index.
   * @param index an index
   * @param x a x-ordinate
   * @param y a y-ordinate
   * @param z a z-ordinate
   */
  public void insertAt(int index, double x, double y, double z) {
    insertAt(index, x, y, z, Double.NaN);
  }

  /**
   * Inserts a point defined by {@code x}-, {@code y}-, {@code z}- and {@code m}-ordinates
   * at the given index.
   * @param index an index
   * @param x a x-ordinate
   * @param y a y-ordinate
   * @param z a z-ordinate
   * @param m a m-ordinate
   */
  public void insertAt(int index, double x, double y, double z, double m) {

    // ensure capacity
    ensureCapacity(size()+1);

    // make space
    if (this.sequence instanceof CoordinateArraySequence) {
      // performance improvement for CoordinateArraySequence
      System.arraycopy(this.sequence.toCoordinateArray(), index,
                       this.sequence.toCoordinateArray(), index + 1,
                      this.size() - index);
    }
    else if (this.sequence instanceof PackedCoordinateSequence.Float) {
      // performance improvement for PackedCoordinateSequence.Float
      PackedCoordinateSequence.Float fseq = (PackedCoordinateSequence.Float)this.sequence;
      System.arraycopy(fseq.getRawCoordinates(), index * fseq.getDimension(),
                       fseq.getRawCoordinates(), (index + 1)* fseq.getDimension(),
                      (this.size() - index) * fseq.getDimension());
    }
    else if (this.sequence instanceof PackedCoordinateSequence.Double) {
      // performance improvement for PackedCoordinateSequence.Double
      PackedCoordinateSequence.Double dseq = (PackedCoordinateSequence.Double)this.sequence;
      System.arraycopy(dseq.getRawCoordinates(), index * dseq.getDimension(),
                       dseq.getRawCoordinates(), (index + 1)* dseq.getDimension(),
                      (this.size() - index) * dseq.getDimension());
    }
    else
    {
      // for all other sequences we need to copy by hand.
      CoordinateSequences.copy(sequence, index, sequence, index + 1, this.sequence.size() - index);
    }

    // add x- and y-ordinates
    this.size++;
    this.setOrdinate(index, CoordinateSequence.X, x);
    this.setOrdinate(index, CoordinateSequence.Y, y);
    if (getDimension() == 2) return;
    // add z-ordinate
    this.setOrdinate(index, CoordinateSequence.Z, z);
    if (getDimension() == 3) return;
    // add m-ordinate
    this.setOrdinate(index, CoordinateSequence.M, m);
  }

  /**
   * Checks if the size is within the allowed bounds
   * 
   * @param index an index
   */
  private void checkSize(int index) throws IllegalArgumentException {
    if (0 <= index && index < this.size)
      return;

    throw new IllegalArgumentException(
            String.format("index is %d and must be in the range [0, %d)", index, this.size));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("EXT(");
    builder.append(this.sequence.getClass().getSimpleName());
    builder.append(String.format(", size=%d, dim=%d, cap=%d)", size, getDimension(), getCapacity()));
    builder.append('[');
    for (int i = 0; i < size; i++)
    {
      if (i > 0) builder.append(',');
      builder.append(this.sequence.getX(i));
      builder.append(' ');
      builder.append(this.sequence.getY(i));
      for (int j = 2; j < this.sequence.getDimension(); j++) {
        builder.append(' ');
        builder.append(this.sequence.getOrdinate(i, j));
      }
    }
    builder.append(']');
    return builder.toString();
  }
}
