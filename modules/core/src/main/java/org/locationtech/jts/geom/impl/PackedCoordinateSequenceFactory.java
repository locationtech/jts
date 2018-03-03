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

import java.io.Serializable;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Coordinates;

/**
 * Builds packed array coordinate sequences. The array data type can be either
 * double or float, and defaults to float.
 */
public class PackedCoordinateSequenceFactory implements
    CoordinateSequenceFactory, Serializable
{
  private static final long serialVersionUID = -3558264771905224525L;

  /** Type code for a factory that creates {@link PackedCoordinateSequence.Double} sequences*/
  public static final int DOUBLE = 0;
  /** Type code for a factory that creates {@link PackedCoordinateSequence.Float} sequences*/
  public static final int FLOAT = 1;

  public static final PackedCoordinateSequenceFactory DOUBLE_FACTORY =
      new PackedCoordinateSequenceFactory(DOUBLE);
  
  public static final PackedCoordinateSequenceFactory FLOAT_FACTORY =
      new PackedCoordinateSequenceFactory(FLOAT);

  private int type = DOUBLE;

  /**
   * Creates a new PackedCoordinateSequenceFactory
   * of type DOUBLE.
   */
  public PackedCoordinateSequenceFactory(){
    this(DOUBLE);
  }

  /**
   * Creates a new PackedCoordinateSequenceFactory
   * of the given type.
   * Acceptable type values are
   * {@linkplain PackedCoordinateSequenceFactory#FLOAT}or
   * {@linkplain PackedCoordinateSequenceFactory#DOUBLE}
   */
  public PackedCoordinateSequenceFactory(int type){
    this.type = type;
  }

  /**
   * Returns the type of packed coordinate sequences this factory builds, either
   * {@linkplain PackedCoordinateSequenceFactory#FLOAT} or
   * {@linkplain PackedCoordinateSequenceFactory#DOUBLE}
   */
  public int getType() {
    return type;
  }

  /**
   * Sets the type of packed coordinate sequences this factory builds,
   * acceptable values are {@linkplain PackedCoordinateSequenceFactory#FLOAT}or
   * {@linkplain PackedCoordinateSequenceFactory#DOUBLE}
   */
  public void setType(int type) {
    if (type != DOUBLE && type != FLOAT)
      throw new IllegalArgumentException("Unknown type " + type);
    this.type = type;
  }


  public int getDimension() { return dimension; }

  public void setDimension(int dimension) {
    PackedCoordinateSequence.checkDimension(dimension);
    this.dimension = dimension;
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(org.locationtech.jts.geom.Coordinate[])
   */
  public CoordinateSequence create(Coordinate[] coordinates) {
    int dimension = 3;
    int measures = 0;
    if (coordinates != null && coordinates.length > 1 && coordinates[0] != null) {
      Coordinate first = coordinates[0];
      dimension = Coordinates.dimension(first);
      measures = Coordinates.measures(first);
    }
    if (type == DOUBLE) {
      return new PackedCoordinateSequence.Double(coordinates, dimension, measures);
    } else {
      return new PackedCoordinateSequence.Float(coordinates,  dimension, measures);
    }
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(org.locationtech.jts.geom.CoordinateSequence)
   */
  public CoordinateSequence create(CoordinateSequence coordSeq) {
    int dimension = coordSeq.getDimension();
    int measures = coordSeq.getMeasures();
    if (type == DOUBLE) {

      return coordSeq != null
              ? new PackedCoordinateSequence.Double(coordSeq)
              : new PackedCoordinateSequence.Double(0, this.dimension);
    } else {
      return coordSeq != null
              ? new PackedCoordinateSequence.Float(coordSeq)
              : new PackedCoordinateSequence.Float(0, this.dimension);
    }
  }

  /**
   * Create a packed coordinate sequence from the provided array. 
   * 
   * @param packedCoordinates 
   * @param dimension
   * @return Packaged coordinate seqeunce of the requested type
   */
  public CoordinateSequence create(double[] packedCoordinates, int dimension) {
    return create( packedCoordinates, dimension, 0 );
  }
  
  /**
   * Create a packed coordinate sequence from the provided array. 
   * 
   * @param packedCoordinates 
   * @param dimension
   * @param measures
   * @return Packaged coordinate seqeunce of the requested type
   */
  public CoordinateSequence create(double[] packedCoordinates, int dimension, int measures) {
    if (type == DOUBLE) {
      return new PackedCoordinateSequence.Double(packedCoordinates, dimension, measures);
    } else {
      return new PackedCoordinateSequence.Float(packedCoordinates, dimension, measures);
    }
  }
  /**
   * Create a packed coordinate sequence from the provided array. 
   * 
   * @param packedCoordinates 
   * @param dimension
   * @return Packaged coordinate seqeunce of the requested type
   */
  public CoordinateSequence create(float[] packedCoordinates, int dimension) {
    return create( packedCoordinates, dimension, 0 );
  }

  /**
   * @param packedCoordinates
   * @param dimension
   * @param measures
   * @return Packaged coordinate seqeunce of the requested type
   */
  public CoordinateSequence create(float[] packedCoordinates, int dimension, int measures) {
    if (type == DOUBLE) {
      return new PackedCoordinateSequence.Double(packedCoordinates, dimension, measures);
    } else {
      return new PackedCoordinateSequence.Float(packedCoordinates, dimension, measures);
    }
  }

  /**
   * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(int, int)
   */
  public CoordinateSequence create(int size, int dimension) {
    if (type == DOUBLE) {
      return new PackedCoordinateSequence.Double(size, dimension, 0);
    } else {
      return new PackedCoordinateSequence.Float(size, dimension, 0 );
    }
  }
  
  /**
   * @see org.locationtech.jts.geom.CoordinateSequenceFactory#create(int, int, int)
   */
  public CoordinateSequence create(int size, int dimension, int measures) {
    if (type == DOUBLE) {
      return new PackedCoordinateSequence.Double(size, dimension, measures);
    } else {
      return new PackedCoordinateSequence.Float(size, dimension, measures);
    }
  }
}