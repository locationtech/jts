/*
 * Copyright (c) 2022 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.operation.overlayarea;

import java.io.IOException;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.overlayarea.OverlayArea;
import org.locationtech.jts.operation.overlayarea.SimpleOverlayArea;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class SimpleOverlayAreaPerfTest extends PerformanceTestCase
{
  public static void main(String args[]) {
    PerformanceTestRunner.run(SimpleOverlayAreaPerfTest.class);
  }

  private Polygon quadA;
  private Polygon quadB;
  
  public SimpleOverlayAreaPerfTest(String name) {
    super(name);
    setRunSize(new int[] { 100 });
    setRunIterations(10000);
  }

  @Override
  public void startRun(int size) throws IOException, ParseException
  {
    WKTReader rdr= new WKTReader();
    quadA = (Polygon) rdr.read("POLYGON ((60 80, 9 45, 52.5 5, 80 45, 60 80))");
    quadB = (Polygon) rdr.read("POLYGON ((13.5 60, 72 18, 79.5 65.5, 41.5 75.5, 13.5 60))");
  }

  public void runSimpleOverlayArea() {
    double area = SimpleOverlayArea.intersectionArea(quadA, quadB);
  }
  
  public void runOverlayArea() {
    double area = OverlayArea.intersectionArea(quadA, quadB);
  }
  
  public void runFullOverlayArea() {
    double area = quadA.intersection(quadB).getArea();
  }
  
  public void runOverlayNGArea() {
    double area = OverlayNGRobust.overlay(quadB, quadA, OverlayNG.INTERSECTION).getArea();
  }
}
