package org.locationtech.jts.io;

import java.util.EnumSet;

/**
 * An enumeration of possible Well-Known-Text ordinates
 */
public enum WKTOrdinates {
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

  private static final EnumSet<WKTOrdinates> XY = EnumSet.of(X, Y);
  private static final EnumSet<WKTOrdinates> XYZ = EnumSet.of(X, Y, Z);
  private static final EnumSet<WKTOrdinates> XYM = EnumSet.of(X, Y, M);
  private static final EnumSet<WKTOrdinates> XYZM = EnumSet.of(X, Y, Z, M);

  public static EnumSet<WKTOrdinates> getXY() { return XY.clone(); }
  public static EnumSet<WKTOrdinates> getXYZ() { return XYZ.clone(); }
  public static EnumSet<WKTOrdinates> getXYM() { return XYM.clone(); }
  public static EnumSet<WKTOrdinates> getXYZM() { return XYZM.clone(); }
}
