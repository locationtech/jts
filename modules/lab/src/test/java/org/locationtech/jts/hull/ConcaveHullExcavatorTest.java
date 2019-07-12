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
package org.locationtech.jts.hull;

import junit.textui.TestRunner;

import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.algorithm.match.HausdorffSimilarityMeasure;
import org.locationtech.jts.geom.Geometry;

import org.locationtech.jts.io.*;
import test.jts.GeometryTestCase;
import test.jts.TestFiles;

import java.io.IOException;
import java.util.List;

public class ConcaveHullExcavatorTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(ConcaveHullExcavatorTest.class);
  }
  
  public ConcaveHullExcavatorTest(String name) {
    super(name);
  }

  public void testSimple() {
    checkHull(
        "MULTIPOINT ((150 200), (200 160), (250 200), (200 190))",
        0,
        "POLYGON ((150 200, 200 160, 250 200, 200 190, 150 200))"
        );
  }

  public void testPointsOnLine() {

    Geometry input = read("MULTIPOINT((0 0), (0 1), (0 4))");
    Geometry expected = new ConvexHull(input).getConvexHull();
    Geometry actual = ConcaveHullExcavator.compute(input);

    assertEquals(expected, actual);
  }

  public void testThreeUniquePoints() {

    Geometry input = read("MULTIPOINT((0 0), (0 1), (0 0), (1 1))");
    Geometry expected = new ConvexHull(input).getConvexHull();
    Geometry actual = ConcaveHullExcavator.compute(input);

    assertEquals(expected, actual);
  }


  private void checkHull(String inputWKT, double tolerance, String expectedWKT) {
    Geometry input = read(inputWKT);
    Geometry expected = read(expectedWKT);
    expected.normalize();
    Geometry actual = ConcaveHullExcavator.compute(input, tolerance, 1.5);
    actual.normalize();
    checkEqual(expected, actual);
  }

  static WKTWriter wktWriter;
  static {
    wktWriter = new WKTWriter();
    wktWriter.setOutputOrdinates(Ordinate.createXY());
    wktWriter.setFormatted(true);
    wktWriter.setMaxCoordinatesPerLine(3);
    wktWriter.setTab(2);
  }

  public void testPoint1k_1() throws IOException, ParseException {

    String pointsPath = TestFiles.getResourceFilePath("points-1k.wkt");
    Geometry input = readFromFile(pointsPath);
    String resultPath = TestFiles.getResourceFilePath("points-1k-hull.wkt");
    Geometry expected = readFromFile(resultPath);

    Geometry actual = ConcaveHullExcavator.compute(input);
    compareGeometries(expected, actual, 0.0001);
    assertTrue(actual.contains(input));
  }

  public void testPoint1k_2() throws IOException, ParseException {

    String pointsPath = TestFiles.getResourceFilePath("points-1k.wkt");
    Geometry input = readFromFile(pointsPath);
    String resultPath = TestFiles.getResourceFilePath("points-1k-hull2.wkt");
    Geometry expected = readFromFile(resultPath);

    Geometry actual = ConcaveHullExcavator.compute(input, 0.01);
    compareGeometries(expected, actual, 0.0001);
    assertTrue(actual.contains(input));

  }

  private static void compareGeometries(Geometry expected, Geometry actual, double hsmDelta) {

    // Normalize both
    expected.normalize();
    actual.normalize();

    if (!expected.equalsExact(actual)) {
      HausdorffSimilarityMeasure hsm = new HausdorffSimilarityMeasure();
      double m = hsm.measure(expected, actual);
      if (1 - m > hsmDelta) {
        System.out.println("expected:\n" + wktWriter.writeFormatted(expected));
        System.out.println("\n  actual:\n" + wktWriter.writeFormatted(actual));
        System.out.println("\n  Hausdorff similarity:" + m);
      }
      assertEquals("Similarity of "+hsmDelta+" not reached: " + m, 1d, m, hsmDelta);
    }

  }

  private static Geometry readFromFile(String path) throws IOException, ParseException {
    WKTFileReader rdr = new WKTFileReader(path, new WKTReader());
    rdr.setLimit(1);
    List res = rdr.read();
    return (Geometry) res.get(0);
  }

}
