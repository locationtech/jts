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

package org.locationtech.jts.simplify;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;


/**
 * @version 1.7
 */
public class VWSimplifierTest
    extends TestCase
{
  public VWSimplifierTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(VWSimplifierTest.class);
  }

  public void testEmptyPolygon() throws Exception {
    String geomStr = "POLYGON(EMPTY)";
    new GeometryOperationValidator(
        VWSimplifierResult.getResult(
        geomStr,
        1))
        .setExpectedResult(geomStr)
        .test();
  }

  public void testPolygonNoReduction() throws Exception {
    new GeometryOperationValidator(
        VWSimplifierResult.getResult(
      "POLYGON ((20 220, 40 220, 60 220, 80 220, 100 220, 120 220, 140 220, 140 180, 100 180, 60 180, 20 180, 20 220))",
        1.0))
        .test();
  }
  public void testPolygonSpikeInShell() throws Exception {
    new GeometryOperationValidator(
        VWSimplifierResult.getResult(
      "POLYGON ((1721355.3 693015.146, 1721318.687 693046.251, 1721306.747 693063.038, 1721367.025 692978.29, 1721355.3 693015.146))",
        10.0))
        .setExpectedResult("POLYGON ((1721355.3 693015.146, 1721367.025 692978.29, 1721318.687 693046.251, 1721355.3 693015.146))")
        .test();
  }
  public void testPolygonSpikeInHole() throws Exception {
    new GeometryOperationValidator(
        VWSimplifierResult.getResult(
      "POLYGON ((1721270 693090, 1721400 693090, 1721400 692960, 1721270 692960, 1721270 693090), (1721355.3 693015.146, 1721318.687 693046.251, 1721306.747 693063.038, 1721367.025 692978.29, 1721355.3 693015.146))",
        10.0))
        .setExpectedResult("POLYGON ((1721270 693090, 1721400 693090, 1721400 692960, 1721270 692960, 1721270 693090), (1721355.3 693015.146, 1721318.687 693046.251, 1721367.025 692978.29, 1721355.3 693015.146))")
        .test();
  }

 
}

class VWSimplifierResult
{
  private static WKTReader rdr = new WKTReader();

  public static Geometry[] getResult(String wkt, double tolerance)
    throws ParseException
  {
    Geometry[] ioGeom = new Geometry[2];
    ioGeom[0] = rdr.read(wkt);
    ioGeom[1] = VWSimplifier.simplify(ioGeom[0], tolerance);
    //System.out.println(ioGeom[1]);
    return ioGeom;
  }
}
