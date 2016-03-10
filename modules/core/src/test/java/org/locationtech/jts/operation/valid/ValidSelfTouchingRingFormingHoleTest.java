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

package org.locationtech.jts.operation.valid;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;


/**
 * Tests allowing IsValidOp to validate polygons with
 * Self-Touching Rings forming holes.
 * Mainly tests that configuring {@link IsValidOp} to allow validating
 * the STR validates polygons with this condition, and does not validate
 * polygons with other kinds of self-intersection (such as ones with Disconnected Interiors).
 * Includes some basic tests to confirm that other invalid cases remain detected correctly,
 * but most of this testing is left to the existing XML validation tests.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class ValidSelfTouchingRingFormingHoleTest
    extends TestCase
{
  private static WKTReader rdr = new WKTReader();

  public ValidSelfTouchingRingFormingHoleTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(ValidSelfTouchingRingFormingHoleTest.class);
  }

  /**
   * Tests a geometry with both a shell self-touch and a hole self=touch.
   * This is valid if STR is allowed, but invalid in OGC
   */
  public void testShellAndHoleSelfTouch()
  {
    String wkt = "POLYGON ((0 0, 0 340, 320 340, 320 0, 120 0, 180 100, 60 100, 120 0, 0 0),   (80 300, 80 180, 200 180, 200 240, 280 200, 280 280, 200 240, 200 300, 80 300))";
    checkIsValidSTR(wkt, true);
    checkIsValidDefault(wkt, false);
  }

  /**
   * Tests a geometry representing the same area as in {@link #testShellAndHoleSelfTouch}
   * but using a shell-hole touch and a hole-hole touch.
   * This is valid in OGC.
   */
  public void testShellHoleAndHoleHoleTouch()
  {
    String wkt = "POLYGON ((0 0, 0 340, 320 340, 320 0, 120 0, 0 0),   (120 0, 180 100, 60 100, 120 0),   (80 300, 80 180, 200 180, 200 240, 200 300, 80 300),  (200 240, 280 200, 280 280, 200 240))";
    checkIsValidSTR(wkt, true);
    checkIsValidDefault(wkt, true);
  }

  /**
   * Tests an overlapping hole condition, where one of the holes is created by a shell self-touch.
   * This is never vallid.
   */
  public void testShellSelfTouchHoleOverlappingHole()
  {
    String wkt = "POLYGON ((0 0, 220 0, 220 200, 120 200, 140 100, 80 100, 120 200, 0 200, 0 0),   (200 80, 20 80, 120 200, 200 80))";
    checkIsValidSTR(wkt, false);
    checkIsValidDefault(wkt, false);
  }

  /**
   * Ensure that the Disconnected Interior condition is not validated
   */
  public void testDisconnectedInteriorShellSelfTouchAtNonVertex()
  {
    String wkt = "POLYGON ((40 180, 40 60, 240 60, 240 180, 140 60, 40 180))";
    checkIsValidSTR(wkt, false);
    checkIsValidDefault(wkt, false);
  }

  /**
   * Ensure that the Disconnected Interior condition is not validated
   */
  public void testDisconnectedInteriorShellSelfTouchAtVertex()
  {
    String wkt = "POLYGON ((20 20, 20 100, 140 100, 140 180, 260 180, 260 100, 140 100, 140 20, 20 20))";
    checkIsValidSTR(wkt, false);
    checkIsValidDefault(wkt, false);
  }

  public void testShellCross()
  {
    String wkt = "POLYGON ((20 20, 120 20, 120 220, 240 220, 240 120, 20 120, 20 20))";
    checkIsValidSTR(wkt, false);
    checkIsValidDefault(wkt, false);
  }

  public void testShellCrossAndSTR()
  {
    String wkt = "POLYGON ((20 20, 120 20, 120 220, 180 220, 140 160, 200 160, 180 220, 240 220, 240 120, 20 120,  20 20))";
    checkIsValidSTR(wkt, false);
    checkIsValidDefault(wkt, false);
  }

  private void checkIsValidDefault(String wkt, boolean expected)
  {
    Geometry geom = fromWKT(wkt);
    IsValidOp validator = new IsValidOp(geom);
    boolean isValid = validator.isValid();
    assertTrue(isValid == expected);
  }

  private void checkIsValidSTR(String wkt, boolean expected)
  {
    Geometry geom = fromWKT(wkt);
    IsValidOp validator = new IsValidOp(geom);
    validator.setSelfTouchingRingFormingHoleValid(true);
    boolean isValid = validator.isValid();
    assertTrue(isValid == expected);
  }

  Geometry fromWKT(String wkt)
  {
    Geometry geom = null;
    try {
      geom = rdr.read(wkt);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return geom;
  }

}