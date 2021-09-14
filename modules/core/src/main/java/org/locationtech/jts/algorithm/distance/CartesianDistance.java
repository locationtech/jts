/*
 * Copyright (c) 2021 Felix Obermaier.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.distance;

import org.locationtech.jts.geom.Coordinate;

/**
 * Implementation of a cartesian distance function
 */
public final class CartesianDistance implements DistanceMetric {

  private static final CartesianDistance _instance = new CartesianDistance();

  public static DistanceMetric getInstance() { return _instance;}

  /**
   * Creation of this class is private to prevent creation of other objects
   */
  private CartesianDistance() {}

  /**
   * Computes the cartesian distance between the two points {@code p0} and {@code p2}.
   * @param p0 The first coordinate
   * @param p1 The second coordinate
   * @return The cartesian distance.
   */
  @Override
  public double distance(Coordinate p0, Coordinate p1) {

    return p0.distance(p1);
  }
}
