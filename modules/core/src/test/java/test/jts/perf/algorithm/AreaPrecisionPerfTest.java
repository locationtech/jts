/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

public class AreaPrecisionPerfTest
{
  public static void main(String[] args) throws Exception
  {

    double originX = 1000000;
    double originY = 5000000;
    long start = System.currentTimeMillis();

    for (int nrVertices = 4; nrVertices <= 1000000; nrVertices *= 2) {
      Coordinate[] coordinates = new Coordinate[nrVertices + 1];

      Coordinate vertex;
      for (int i = 0; i <= nrVertices; i++) {
        vertex = new Coordinate(originX
            + (1 + Math.sin((float) i / (float) nrVertices * 2 * Math.PI)),
            originY
                + (1 + Math.cos((float) i / (float) nrVertices * 2 * Math.PI)));
        coordinates[i] = vertex;
      }
      // close ring
      coordinates[nrVertices] = coordinates[0];
      
      Geometry g1 = new GeometryFactory().createLinearRing(coordinates);
      LinearRing[] holes = new LinearRing[] {};
      Polygon polygon = (Polygon) new GeometryFactory().createPolygon(
          (LinearRing) g1, holes);
      System.out.println(polygon);
      
      double area = originalSignedArea(coordinates);
      double area2 = accurateSignedArea(coordinates);
      double exactArea = 0.5 * nrVertices * Math.sin(2 * Math.PI / nrVertices);
      
      double eps = exactArea - area;
      double eps2 = exactArea - area2;
      
      System.out.println(nrVertices + "   orig err: " + eps 
          + "    acc err: " + eps2);
    }
    System.out.println("Time: " + (System.currentTimeMillis() - start) / 1000.0);
  }

  public static double originalSignedArea(Coordinate[] ring)
  {
    if (ring.length < 3)
      return 0.0;
    double sum = 0.0;
    for (int i = 0; i < ring.length - 1; i++) {
      double bx = ring[i].x;
      double by = ring[i].y;
      double cx = ring[i + 1].x;
      double cy = ring[i + 1].y;
      sum += (bx + cx) * (cy - by);
    }
    return -sum / 2.0;
  }

  public static double accurateSignedArea(Coordinate[] ring)
  {
    if (ring.length < 3)
      return 0.0;
    double sum = 0.0;
    // http://en.wikipedia.org/wiki/Shoelace_formula
    double x0 = ring[0].x;
    for (int i = 1; i < ring.length - 1; i++) {
      double x = ring[i].x - x0;
      double y1 = ring[i + 1].y;
      double y2 = ring[i == 0 ? ring.length - 1 : i - 1].y;
      sum += x * (y2 - y1);
    }
    return sum / 2.0;
  }

}
