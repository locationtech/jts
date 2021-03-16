/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.geom.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.util.GeometryFixer;

public class GeometryFixerFuzzer {

  private static final int NUM_ITER = 10000;

  public static void main(String[] args) {
    GeometryFixerFuzzer.run();
  }
  
  private static void run() {
    GeometryFixerFuzzer fuzzer = new GeometryFixerFuzzer();
    fuzzer.run(NUM_ITER);
  }

  public GeometryFactory factory = new GeometryFactory();
  
  public GeometryFixerFuzzer() {
    
  }
  
  private void run(int numIter) {
    for (int i = 0; i < numIter; i++) {
      int numHoles = (int) (10 * Math.random());
      Geometry invalidPoly = createRandomPoly(100, numHoles);
      Geometry result = GeometryFixer.fix(invalidPoly);
      boolean isValid = result.isValid();
      String status =  isValid ? "valid" : "INVALID";
      String msg = String.format("%d: Pts - input %d, output %d - %s",
          i, invalidPoly.getNumPoints(), result.getNumPoints(), status);
      //System.out.println(invalidPoly);
      if (! isValid) {
        System.out.println(msg);
        System.out.println(invalidPoly);
      }
    }
  }

  private Geometry createRandomPoly(int numPoints, int numHoles) {
    int numRingPoints = numPoints / (numHoles + 1);
    LinearRing shell = createRandomRing(numRingPoints);
    LinearRing[] holes = new LinearRing[numHoles];
    for (int i = 0; i < numHoles; i++) {
      holes[i] = createRandomRing(numRingPoints);
    }
    return factory.createPolygon(shell, holes);
  }

  private LinearRing createRandomRing(int numPoints) {
    return factory.createLinearRing(createRandomPoints(numPoints));
  }

  private Coordinate[] createRandomPoints(int numPoints) {
    Coordinate[] pts = new Coordinate[numPoints + 1];
    for (int i = 0; i < numPoints; i++) {
      Coordinate p = new Coordinate(randOrd(), randOrd());
      pts[i] = p;
    }
    pts[pts.length - 1] = pts[0].copy();
    return pts;
  }

  private double randOrd() {
    double ord = 100 * Math.random();
    return ord;
  }
}
