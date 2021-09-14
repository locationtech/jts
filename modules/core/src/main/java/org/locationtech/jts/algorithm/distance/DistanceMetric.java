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
 * Base interface for classes that can compute a distance between two {@link Coordinate}s.
 *
 * @author Felix Obermaier
 */
public interface DistanceMetric {
  /**
   * Computes the distance between the two coordinates {@code p0} and {@code p1}.
   * @param p0 The first coordinate
   * @param p1 The second coordinate
   * @return The distance
   */
  double distance(Coordinate p0, Coordinate p1);
}
