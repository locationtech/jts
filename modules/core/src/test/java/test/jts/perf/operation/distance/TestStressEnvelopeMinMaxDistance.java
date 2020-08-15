/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.operation.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.index.strtree.EnvelopeDistance;

public class TestStressEnvelopeMinMaxDistance 
{
  static GeometryFactory factory = new GeometryFactory();
  
  public static void main(String[] args) {
    TestStressEnvelopeMinMaxDistance test = new TestStressEnvelopeMinMaxDistance();
    test.test();
  }

  boolean testFailed = false;
  boolean verbose = true;

  public TestStressEnvelopeMinMaxDistance() {
  }

  public void test()
  {
    int sizeX = 6;
    int sizeY = 6;
    
    Coordinate[] pts = createPoints(sizeX, sizeY);
    
    MultiPoint[] boxes = createPointPairs(pts);
    
    run(boxes);
  }

  private void run(MultiPoint[] boxes) {
    int n = boxes.length;
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        run(boxes[i], boxes[j]);
      }
    }
  }

  private void run(MultiPoint a, MultiPoint b) {
    double distance = a.distance(b);
    double minMaxDistance = EnvelopeDistance.minMaxDistance(
        a.getEnvelopeInternal(), b.getEnvelopeInternal());
    
    System.out.println("distance: " + distance
        + "   minMaxDist: " + minMaxDistance);

    if (distance > minMaxDistance) {
      System.out.println("ERROR - distance: " + distance
          + "   minMaxDist: " + minMaxDistance);
    }
  }

  private MultiPoint[] createPointPairs(Coordinate[] pts) {
    int npts = pts.length;
    MultiPoint[] pairs = new MultiPoint[npts * npts];
    
    for (int i = 0; i < npts; i++) {
      for (int j = 0; j < npts; j++) {
        int index = i * npts + j;
        MultiPoint pair = factory.createMultiPointFromCoords(new Coordinate[] { pts[i], pts[j] } );
        pairs[index] = pair;
      }
    }
    
    return pairs;
  }

  private Coordinate[] createPoints(int sizeX, int sizeY) {
    int npts = sizeX * sizeY;
    Coordinate[] pts = new Coordinate[npts];
    for (int x = 0; x < sizeX; x++) {
      for (int y = 0; y < sizeY; y++) {
        pts[x * sizeX + y] = new Coordinate(x, y);
      }
    }
    return pts;
  }

  

}
  
  
