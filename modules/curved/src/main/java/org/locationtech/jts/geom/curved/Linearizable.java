/*
 * Copyright (c) 2026 grootstebozewolf
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.curved;

import org.locationtech.jts.geom.Geometry;

/**
 * Implemented by geometry types that can be approximated by a non-curved
 * (linear) geometry to within a given coordinate tolerance.
 * <p>
 * In the phase-1 jts-curved implementation, curve geometries are stored
 * as their control points and {@link #toLinear(double)} returns a
 * parent-type geometry built from those control points (no real arc
 * densification is performed). The interface is published now so that
 * downstream consumers can write code that survives the phase where
 * native arc-aware representations land.
 *
 * <h3>Tolerance</h3>
 * Implementations should treat <code>tolerance</code> as the maximum
 * permissible distance between the original curved geometry and its
 * linear approximation. A value of <code>0.0</code> means "use the
 * implementation's default tolerance". Negative values are reserved.
 */
public interface Linearizable {

  /**
   * Returns a non-curved geometry that approximates this geometry to
   * within {@code tolerance} units of distance.
   *
   * @param tolerance maximum permissible distance between original and
   *                  approximation; <code>0.0</code> selects the
   *                  implementation default
   * @return a linearised {@link Geometry} (never {@code null})
   */
  Geometry toLinear(double tolerance);
}
