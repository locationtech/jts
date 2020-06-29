/*
 * Copyright (c) 2018 Felix Obermaier
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.io;

import java.util.EnumSet;

/**
 * An enumeration of possible Well-Known-Text or Well-Known-Binary ordinates.
 * <p>
 * Intended to be used as an {@code EnumSet<Ordinate>}, optimized create methods have been provided for {@link #createXY()}, {@link #createXYM()}, {@link #createXYZ()} and {@link #createXYZM()}.
 */
public enum Ordinate {
  /**
   * X-ordinate
   */
  X,
  /**
   * Y-ordinate
   */
  Y,
  /**
   * Z-ordinate
   */
  Z,
  /**
   * Measure-ordinate
   */
  M;

  private static final EnumSet<Ordinate> XY = EnumSet.of(X, Y);
  private static final EnumSet<Ordinate> XYZ = EnumSet.of(X, Y, Z);
  private static final EnumSet<Ordinate> XYM = EnumSet.of(X, Y, M);
  private static final EnumSet<Ordinate> XYZM = EnumSet.of(X, Y, Z, M);

  /**
   * EnumSet of X and Y ordinates, a copy is returned as EnumSets are not immutable.
   * @return EnumSet of X and Y ordinates.
   */
  public static EnumSet<Ordinate> createXY() { return XY.clone(); }
  /**
   * EnumSet of XYZ ordinates, a copy is returned as EnumSets are not immutable.
   * @return EnumSet of X and Y ordinates.
   */
  public static EnumSet<Ordinate> createXYZ() { return XYZ.clone(); }
  /**
   * EnumSet of XYM ordinates, a copy is returned as EnumSets are not immutable.
   * @return EnumSet of X and Y ordinates.
   */
  public static EnumSet<Ordinate> createXYM() { return XYM.clone(); }
  /**
   * EnumSet of XYZM ordinates, a copy is returned as EnumSets are not immutable.
   * @return EnumSet of X and Y ordinates.
   */
  public static EnumSet<Ordinate> createXYZM() { return XYZM.clone(); }
}
