/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package com.vividsolutions.jts.geom;

/**
 * A factory to create concrete instances of {@link CoordinateSequence}s.
 * Used to configure {@link GeometryFactory}s
 * to provide specific kinds of CoordinateSequences.
 *
 * @version 1.7
 */
public interface CoordinateSequenceFactory
{

  /**
   * Returns a {@link CoordinateSequence} based on the given array.
   * Whether the array is copied or simply referenced
   * is implementation-dependent.
   * This method must handle null arguments by creating an empty sequence.
   *
   * @param coordinates the coordinates
   */
  CoordinateSequence create(Coordinate[] coordinates);

  /**
   * Creates a {@link CoordinateSequence} which is a copy
   * of the given {@link CoordinateSequence}.
   * This method must handle null arguments by creating an empty sequence.
   *
   * @param coordSeq the coordinate sequence to copy
   */
  CoordinateSequence create(CoordinateSequence coordSeq);

  /**
   * Creates a {@link CoordinateSequence} of the specified size and dimension.
   * For this to be useful, the {@link CoordinateSequence} implementation must
   * be mutable.
   * <p>
   * If the requested dimension is larger than the CoordinateSequence implementation
   * can provide, then a sequence of maximum possible dimension should be created.
   * An error should not be thrown.
   *
   * @param size the number of coordinates in the sequence
   * @param dimension the dimension of the coordinates in the sequence (if user-specifiable,
   * otherwise ignored)
   */
  CoordinateSequence create(int size, int dimension);

}