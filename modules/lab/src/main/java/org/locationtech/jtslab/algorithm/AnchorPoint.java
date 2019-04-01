/*
 * Copyright (c) 2019 Felix Obermaier
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtslab.algorithm;

import org.locationtech.jts.geom.Coordinate;

/**
 * A wrapper around a {@linkplain Coordinate} in order to possibly reuse it
 * during intersection computations.
 */
public class AnchorPoint {
  /** The wrapped coordinate */
  final Coordinate anchorPoint;

  /** a flag indicating it represents an input vertex */
  final boolean fromVertex;

  /**
   * Creates an instance of this class
   *
   * @param anchorPoint a point
   * @param fromVertex a flag indicating that {@param anchorPoint} is a vertex.
   */
  public AnchorPoint(Coordinate anchorPoint, boolean fromVertex) {
    this.anchorPoint = anchorPoint;
    this.fromVertex = fromVertex;
  }

  /**
   * Access to the wrapped {@linkplain Coordinate}.
   * @return the wrapped coordinate.
   */
  public Coordinate getCoordinate() { return this.anchorPoint; }

  /**
   * Indicates  to the wrapped {@linkplain Coordinate}.
   * @return <c>true</c> if {@linkplain #getCoordinate()} is a vertex from the input.
   */
  public boolean getFromVertex() { return this.fromVertex; }

  public String toString() {
    return "[AP" + this.anchorPoint + ", v=" + (this.fromVertex ? "T":"F") + "]";
  }
}
