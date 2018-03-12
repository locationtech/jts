
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

/**
 * A wrapper around a coordinate sequence that always
 * ensures that a sequence is large enough to store ordinate
 * values.
 */
public class ExtendableCoordinateSequence implements CoordinateSequence {

  /** The default initial capacity */
  static final int INITIAL_CAPACITY = 12;

  /** The factory to use when extending the current sequence*/
  private final CoordinateSequenceFactory csFactory;
  /** The current sequence */
  private CoordinateSequence sequence;
  /** The current size of the sequence */
  private int size = 0;

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
    ensureCapacity(index);

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

  /** @see CoordinateSequence#clone() */
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
   * @param index an index
   */
  private void ensureCapacity(int index) {
    if (index < 0)
      throw new IllegalArgumentException("index < 0");

    int capacity = getCapacity();
    if (index < capacity)
      return;


    do {
      capacity *= 2;
    } while (index >= capacity);

    CoordinateSequence newSequence = csFactory.create(capacity, this.sequence.getDimension());
    CoordinateSequences.copy(sequence, 0, newSequence, 0, this.sequence.size());
    sequence = newSequence;
    size = index + 1;
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
    builder.append(sequence.getClass().getSimpleName());
    builder.append(String.format(", size=%d, dim=%d, cap=%d)", size, getDimension(), getCapacity()));
    builder.append('[');
    for (int i = 0; i < size; i++)
    {
      if (i > 0) builder.append(',');
      builder.append(sequence.getX(i) + " " + sequence.getY(i));
      for (int j = 2; j < sequence.getDimension(); j++)
        builder.append(" " + sequence.getOrdinate(i, j));
    }
    builder.append(']');
    return builder.toString();
  }
}
