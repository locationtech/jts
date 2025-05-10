/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;

/**
 * Utility functions for working with angles.
 * Unless otherwise noted, methods in this class express angles in radians.
 */
@SuppressWarnings("DuplicatedCode") public class Angle
{
  /**
   * The value of 2*Pi
   */
  public static final double PI_TIMES_2 = 2.0 * Math.PI;
  /**
   * The value of Pi/2
   */
  public static final double PI_OVER_2 = Math.PI / 2.0;
  /**
   * The value of Pi/4
   */
  public static final double PI_OVER_4 = Math.PI / 4.0;

  /** Constant representing counterclockwise orientation */
  public static final int COUNTERCLOCKWISE = Orientation.COUNTERCLOCKWISE;

  /** Constant representing clockwise orientation */
  public static final int CLOCKWISE = Orientation.CLOCKWISE;

  /** Constant representing no orientation */
  public static final int NONE = Orientation.COLLINEAR;

  private Angle() {}

  /**
   * Converts from radians to degrees.
   * @param radians an angle in radians
   * @return the angle in degrees
   */
  public static double toDegrees(double radians) {
      return (radians * 180) / (Math.PI);
  }

  /**
   * Converts from degrees to radians.
   *
   * @param angleDegrees an angle in degrees
   * @return the angle in radians
   */
  public static double toRadians(double angleDegrees) {
      return (angleDegrees * Math.PI) / 180.0;
  }


  /**
   * Returns the angle of the vector from p0 to p1,
   * relative to the positive X-axis.
   * The angle is normalized to be in the range [ -Pi, Pi ].
   *
   * @param p0 the initial point of the vector
   * @param p1 the terminal point of the vector
   * @return the normalized angle (in radians) that p0-p1 makes with the positive x-axis.
   */
  public static double angle(Coordinate p0, Coordinate p1) {
      double dx = p1.x - p0.x;
      double dy = p1.y - p0.y;
      return Math.atan2(dy, dx);
  }

  /**
   * Returns the angle of the vector from (0,0) to p,
   * relative to the positive X-axis.
   * The angle is normalized to be in the range ( -Pi, Pi ].
   *
   * @param p the terminal point of the vector
   * @return the normalized angle (in radians) that p makes with the positive x-axis.
   */
  public static double angle(Coordinate p) {
      return Math.atan2(p.y, p.x);
  }


  /**
   * Tests whether the angle between p0-p1-p2 is acute.
   * An angle is acute if it is less than 90 degrees.
   * <p>
   * Note: this implementation is not precise (deterministic) for angles very close to 90 degrees.
   *
   * @param p0 an endpoint of the angle
   * @param p1 the base of the angle
   * @param p2 the other endpoint of the angle
   * @return true if the angle is acute
   */
  public static boolean isAcute(Coordinate p0, Coordinate p1, Coordinate p2)
  {
    // relies on fact that A dot B is positive if A ang B is acute
    double dx0 = p0.x - p1.x;
    double dy0 = p0.y - p1.y;
    double dx1 = p2.x - p1.x;
    double dy1 = p2.y - p1.y;
    double dotprod = dx0 * dx1 + dy0 * dy1;
    return dotprod > 0;
  }

  /**
   * Tests whether the angle between p0-p1-p2 is obtuse.
   * An angle is obtuse if it is greater than 90 degrees.
   * <p>
   * Note: this implementation is not precise (deterministic) for angles very close to 90 degrees.
   *
   * @param p0 an endpoint of the angle
   * @param p1 the base of the angle
   * @param p2 the other endpoint of the angle
   * @return true if the angle is obtuse
   */
  public static boolean isObtuse(Coordinate p0, Coordinate p1, Coordinate p2)
  {
    // relies on fact that A dot B is negative if A ang B is obtuse
    double dx0 = p0.x - p1.x;
    double dy0 = p0.y - p1.y;
    double dx1 = p2.x - p1.x;
    double dy1 = p2.y - p1.y;
    double dotprod = dx0 * dx1 + dy0 * dy1;
    return dotprod < 0;
  }

  /**
   * Returns the unoriented smallest angle between two vectors.
   * The computed angle will be in the range [0, Pi).
   *
   * @param tip1 the tip of one vector
   * @param tail the tail of each vector
   * @param tip2 the tip of the other vector
   * @return the angle between tail-tip1 and tail-tip2
   */
  public static double angleBetween(Coordinate tip1, Coordinate tail,
			Coordinate tip2) {
		double a1 = angle(tail, tip1);
		double a2 = angle(tail, tip2);

		return diff(a1, a2);
  }

  /**
   * Returns the oriented smallest angle between two vectors.
   * The computed angle will be in the range (-Pi, Pi].
   * A positive result corresponds to a counterclockwise
   * (CCW) rotation
   * from v1 to v2;
   * a negative result corresponds to a clockwise (CW) rotation;
   * a zero result corresponds to no rotation.
   *
   * @param tip1 the tip of v1
   * @param tail the tail of each vector
   * @param tip2 the tip of v2
   * @return the angle between v1 and v2, relative to v1
   */
  public static double angleBetweenOriented(Coordinate tip1, Coordinate tail,
			Coordinate tip2) 
  {
		double a1 = angle(tail, tip1);
		double a2 = angle(tail, tip2);
		double angDel = a2 - a1;
		
		// normalize, maintaining orientation
		if (angDel <= -Math.PI)
			return angDel + PI_TIMES_2;
		if (angDel > Math.PI)
			return angDel - PI_TIMES_2;
		return angDel;
  }
  
  /**
   * Computes the angle of the unoriented bisector 
   * of the smallest angle between two vectors.
   * The computed angle will be in the range (-Pi, Pi].
   * Collinear inputs are handled.
   * 
   * @param tip1 the tip of v1
   * @param tail the tail of each vector
   * @param tip2 the tip of v2
   * @return the angle of the bisector between v1 and v2
   */
  public static double bisector(Coordinate tip1, Coordinate tail,
      Coordinate tip2)
  {
    double angDel = angleBetweenOriented(tip1, tail, tip2);
    double angBi = angle(tail, tip1) + angDel / 2;
    return normalize(angBi);
  }

  /**
	 * Computes the interior angle between two segments of a ring. The ring is
	 * assumed to be oriented in a clockwise direction. The computed angle will be
	 * in the range [0, 2Pi]
	 * 
	 * @param p0
	 *          a point of the ring
	 * @param p1
	 *          the next point of the ring
	 * @param p2
	 *          the next point of the ring
	 * @return the interior angle based at {@code p1}
	 */
  public static double interiorAngle(Coordinate p0, Coordinate p1, Coordinate p2)
  {
    double anglePrev = Angle.angle(p1, p0);
    double angleNext = Angle.angle(p1, p2);
    return normalizePositive(angleNext - anglePrev);
  }

  /**
   * Returns whether an angle must turn clockwise or counterclockwise
   * to overlap another angle.
   *
   * @param ang1 an angle (in radians)
   * @param ang2 an angle (in radians)
   * @return whether a1 must turn CLOCKWISE, COUNTERCLOCKWISE or NONE to
   * overlap a2.
   */
  public static int getTurn(double ang1, double ang2) {
      double crossproduct = Math.sin(ang2 - ang1);

      if (crossproduct > 0) {
          return COUNTERCLOCKWISE;
      }
      if (crossproduct < 0) {
          return CLOCKWISE;
      }
      return NONE;
  }

  /**
   * Computes the normalized value of an angle, which is the
   * equivalent angle in the range ( -Pi, Pi ].
   *
   * @param angle the angle to normalize
   * @return an equivalent angle in the range (-Pi, Pi]
   */
  public static double normalize(double angle)
  {
    while (angle > Math.PI)
      angle -= PI_TIMES_2;
    while (angle <= -Math.PI)
      angle += PI_TIMES_2;
    return angle;
  }

  /**
   * Computes the normalized positive value of an angle, which is the
   * equivalent angle in the range [ 0, 2*Pi ).
   * E.g.:
   * <ul>
   * <li>normalizePositive(0.0) = 0.0
   * <li>normalizePositive(-PI) = PI
   * <li>normalizePositive(-2PI) = 0.0
   * <li>normalizePositive(-3PI) = PI
   * <li>normalizePositive(-4PI) = 0
   * <li>normalizePositive(PI) = PI
   * <li>normalizePositive(2PI) = 0.0
   * <li>normalizePositive(3PI) = PI
   * <li>normalizePositive(4PI) = 0.0
   * </ul>
   *
   * @param angle the angle to normalize, in radians
   * @return an equivalent positive angle
   */
  public static double normalizePositive(double angle)
  {
  	if (angle < 0.0) {
  		while (angle < 0.0)
  			angle += PI_TIMES_2;
  		// in case round-off error bumps the value over 
  		if (angle >= PI_TIMES_2)
  			angle = 0.0;
  	}
  	else {
  		while (angle >= PI_TIMES_2)
  			angle -= PI_TIMES_2;
  		// in case round-off error bumps the value under 
  		if (angle < 0.0)
  			angle = 0.0;
  	}
    return angle;
  }

  /**
   * Computes the unoriented smallest difference between two angles.
   * The angles are assumed to be normalized to the range [-Pi, Pi].
   * The result will be in the range [0, Pi].
   *
   * @param ang1 the angle of one vector (in [-Pi, Pi] )
   * @param ang2 the angle of the other vector (in range [-Pi, Pi] )
   * @return the angle (in radians) between the two vectors (in range [0, Pi] )
   */
  public static double diff(double ang1, double ang2) {
    double delAngle;

    if (ang1 < ang2) {
      delAngle = ang2 - ang1;
    } else {
      delAngle = ang1 - ang2;
    }

    if (delAngle > Math.PI) {
      delAngle = PI_TIMES_2 - delAngle;
    }

    return delAngle;
  }

  /**
   * Computes sin of an angle, snapping near-zero values to zero.
   *
   * @param ang the input angle (in radians)
   * @return the result of the trigonometric function
   */
  public static double sinSnap(double ang) {
    double res = Math.sin(ang);
    if (Math.abs(res) < 5e-16) return 0.0;
    return res;
  }

  /**
   * Computes cos of an angle, snapping near-zero values to zero.
   *
   * @param ang the input angle (in radians)
   * @return the result of the trigonometric function
   */
  public static double cosSnap(double ang) {
    double res = Math.cos(ang);
    if (Math.abs(res) < 5e-16) return 0.0;
    return res;
  }

  /**
   * Projects a point by a given angle and distance.
   * 
   * @param p the point to project
   * @param angle the angle at which to project
   * @param dist the distance to project
   * @return the projected point
   */
  public static Coordinate project(Coordinate p, double angle, double dist) {
    double x = p.getX() + dist * Angle.cosSnap(angle);
    double y = p.getY() + dist * Angle.sinSnap(angle);
    return new Coordinate(x, y);
  }
}
