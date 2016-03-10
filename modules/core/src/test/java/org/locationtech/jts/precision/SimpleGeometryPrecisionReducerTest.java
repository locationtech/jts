
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
package org.locationtech.jts.precision;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;



/**
 * @version 1.7
 */
public class SimpleGeometryPrecisionReducerTest
    extends TestCase
{
  private PrecisionModel pmFloat = new PrecisionModel();
  private PrecisionModel pmFixed1 = new PrecisionModel(1);
  private SimpleGeometryPrecisionReducer reducer = new SimpleGeometryPrecisionReducer(pmFixed1);
  private SimpleGeometryPrecisionReducer reducerKeepCollapse
      = new SimpleGeometryPrecisionReducer(pmFixed1);

  private GeometryFactory gfFloat = new GeometryFactory(pmFloat, 0);
  WKTReader reader = new WKTReader(gfFloat);

  public static void main(String args[]) {
    TestRunner.run(SimpleGeometryPrecisionReducerTest.class);
  }

  public SimpleGeometryPrecisionReducerTest(String name)
  {
      super(name);
      reducerKeepCollapse.setRemoveCollapsedComponents(false);

  }

  public void testSquare()
      throws Exception
  {
    Geometry g = reader.read("POLYGON (( 0 0, 0 1.4, 1.4 1.4, 1.4 0, 0 0 ))");
    Geometry g2 = reader.read("POLYGON (( 0 0, 0 1, 1 1, 1 0, 0 0 ))");
    Geometry gReduce = reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testTinySquareCollapse()
      throws Exception
  {
    Geometry g = reader.read("POLYGON (( 0 0, 0 .4, .4 .4, .4 0, 0 0 ))");
    Geometry g2 = reader.read("POLYGON EMPTY");
    Geometry gReduce = reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testSquareCollapse()
      throws Exception
  {
    Geometry g = reader.read("POLYGON (( 0 0, 0 1.4, .4 .4, .4 0, 0 0 ))");
    Geometry g2 = reader.read("POLYGON EMPTY");
    Geometry gReduce = reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testSquareKeepCollapse()
      throws Exception
  {
    Geometry g = reader.read("POLYGON (( 0 0, 0 1.4, .4 .4, .4 0, 0 0 ))");
    Geometry g2 = reader.read("POLYGON (( 0 0, 0 1, 0 0, 0 0, 0 0 ))");
    Geometry gReduce = reducerKeepCollapse.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testLine()
      throws Exception
  {
    Geometry g = reader.read("LINESTRING ( 0 0, 0 1.4 )");
    Geometry g2 = reader.read("LINESTRING (0 0, 0 1)");
    Geometry gReduce = reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testLineRemoveCollapse()
      throws Exception
  {
    Geometry g = reader.read("LINESTRING ( 0 0, 0 .4 )");
    Geometry g2 = reader.read("LINESTRING EMPTY");
    Geometry gReduce = reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }
  public void testLineKeepCollapse()
      throws Exception
  {
    Geometry g = reader.read("LINESTRING ( 0 0, 0 .4 )");
    Geometry g2 = reader.read("LINESTRING ( 0 0, 0 0 )");
    Geometry gReduce = reducerKeepCollapse.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }



}
