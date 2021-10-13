/*
 * Copyright (c) 2021 Felix Obermaier.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.match;

import org.locationtech.jts.algorithm.distance.DiscreteFrechetDistance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPoint;

/**
 * Measures the degree of similarity between two
 * {@link Geometry}s using the Fréchet distance metric.
 * The measure is normalized to lie in the range [0, 1].
 * Higher measures indicate a great degree of similarity.
 * <p/>
 * The measure is computed by computing the Fréchet distance
 * between the input geometries, and then normalizing
 * this by dividing it by the diagonal distance across
 * the envelope of the combined geometries.
 * <p/>
 * Note: the input should be normalized, especially when
 * measuring {@link MultiPoint} geometries because for the
 * Fréchet distance the order of {@link Coordinate}s is
 * important.
 *
 * @author Felix Obermaier
 *
 */
public class FrechetSimilarityMeasure implements SimilarityMeasure {

  /**
   * Creates an instance of this class.
   */
  public FrechetSimilarityMeasure()
  { }

  @Override
  public double measure(Geometry g1, Geometry g2) {

    // Check if input is of same type
    if (!g1.getGeometryType().equals(g2.getGeometryType()))
      throw new IllegalArgumentException("g1 and g2 are of different type");

    // Compute the distance
    double frechetDistance = DiscreteFrechetDistance.distance(g1, g2);
    if (frechetDistance == 0d) return 1;

    // Compute envelope diagonal size
    Envelope env = new Envelope(g1.getEnvelopeInternal());
    env.expandToInclude(g2.getEnvelopeInternal());
    double envDiagSize = HausdorffSimilarityMeasure.diagonalSize(env);

    // normalize so that more similarity produces a measure closer to 1
    return 1 - frechetDistance / envDiagSize;
  }
}
