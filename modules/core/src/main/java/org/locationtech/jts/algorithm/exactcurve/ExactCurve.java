/*
 * Copyright (c) 2026 Jeroen Bloemscheer.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
/*
 * AI Disclosure: This file was largely AI-generated.
 * The AI-generated portions are made available under CC0-1.0 and not subject to the project's licence.
 * The human contributor has reviewed and verified that the code is correct.
 * SPDX-License-Identifier: EPL-2.0 OR EDL-1.0 and CC0-1.0
 * Assisted-by: Cursor Agent
 * Assisted-by: xAI Grok
 */
package org.locationtech.jts.algorithm.exactcurve;

import org.locationtech.jts.geom.Coordinate;

/**
 * Thin Year-1 exact-curve protocol (six methods only).
 * <p>
 * {@link org.locationtech.jts.geom.CircularString} composes
 * {@link ExactCircularArc} windows. Analytic extras (radius, sweep,
 * center, collinear, envelope, append-linearize) live on
 * {@code ExactCircularArc}, not here. Java 8; no {@code sealed}.
 * Do not grow a Year-2 Exact* zoo.
 *
 * @author Jeroen Bloemscheer
 */
public interface ExactCurve {

  /**
   * Start of this primitive (copy).
   *
   * @return the start control
   */
  Coordinate getStart();

  /**
   * End of this primitive (copy).
   *
   * @return the end control
   */
  Coordinate getEnd();

  /**
   * Exact length. Collinear windows use the control-point chord path,
   * not a densified approximation. {@link #isExact()} is true in both cases.
   *
   * @return the length
   */
  double length();

  /**
   * Point at parameter {@code t} in {@code [0, 1]} along this primitive
   * (arc-length fraction). Endpoints: {@code t=0} is {@link #getStart()},
   * {@code t=1} is {@link #getEnd()}.
   *
   * @param t parameter in {@code [0, 1]} (clamped)
   * @return a new coordinate on the primitive
   */
  Coordinate pointAt(double t);

  /**
   * Named densify path. Does not mutate this primitive.
   *
   * @param tolerance max distance from the exact curve; {@code 0} uses
   *        the maximum segment count; non-finite uses the default
   * @return a new linearized vertex array
   */
  Coordinate[] toLinear(double tolerance);

  /**
   * Whether {@link #length()} / {@link #pointAt(double)} are exact
   * (not a densified stand-in). Collinear 3-control windows stay
   * {@code true}: they are exact chords.
   *
   * @return {@code true} if this primitive is exact
   */
  boolean isExact();
}
