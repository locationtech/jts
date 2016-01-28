/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */
package org.locationtech.jts.geom;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 * Test named predicate short-circuits
 */
/**
 * @version 1.7
 */
public class IsRectangleTest extends TestCase {

  WKTReader rdr = new WKTReader();

  public static void main(String args[]) {
    TestRunner.run(IsRectangleTest.class);
  }

  public IsRectangleTest(String name) { super(name); }


  public void testValidRectangle() throws Exception
  {
    assertTrue(isRectangle("POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0))"));
  }

  public void testValidRectangle2() throws Exception
  {
    assertTrue(isRectangle("POLYGON ((0 0, 0 200, 100 200, 100 0, 0 0))"));
  }

  public void testRectangleWithHole() throws Exception
  {
    assertTrue(! isRectangle("POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0), (10 10, 10 90, 90 90, 90 10, 10 10) ))"));
  }

  public void testNotRectilinear() throws Exception
  {
    assertTrue(! isRectangle("POLYGON ((0 0, 0 100, 99 100, 100 0, 0 0))"));
  }

  public void testTooManyPoints() throws Exception
  {
    assertTrue(! isRectangle("POLYGON ((0 0, 0 100, 100 50, 100 100, 100 0, 0 0))"));
  }

  public void testTooFewPoints() throws Exception
  {
    assertTrue(! isRectangle("POLYGON ((0 0, 0 100, 100 0, 0 0))"));
  }

  public void testRectangularLinestring() throws Exception
  {
    assertTrue(! isRectangle("LINESTRING (0 0, 0 100, 100 100, 100 0, 0 0)"));
  }

  public void testPointsInWrongOrder() throws Exception
  {
    assertTrue(! isRectangle("POLYGON ((0 0, 0 100, 100 0, 100 100, 0 0))"));
  }

  public boolean isRectangle(String wkt)
      throws Exception
  {
    Geometry a = rdr.read(wkt);
    return a.isRectangle();
  }
}