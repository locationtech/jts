/*
 * Copyright (c) 2026 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package test.jts.perf.triangulate;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.triangulate.VoronoiChecker;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

/**
 * Generates Voronoi diagrams from point sets which contain nearly cocircular sites.
 * Buffering with circular arcs is an easy way to generate nearly cocircular sites.
 * The {@link VoronoiDiagramBuilder} snapping heuristic produces 
 * valid Voronoi diagrams from this kind of input.
 * 
 * @author mdavis
 *
 */
public class VoronoiCircularStressTest {
  
  private static final int NUM_CASES = 1000;

  public static void main(String args[]) {
    VoronoiCircularStressTest test = new VoronoiCircularStressTest();
    test.run(NUM_CASES);
  }
  
  final static GeometryFactory geomFact = new GeometryFactory();
  
  private static final double WIDTH = 1000;
  
  private void run(int numCases) {
    int numValid = 0;
    for (int i = 0; i < numCases; i++) {
      boolean isValid = runVoronoi(i);
      if (isValid) numValid++;
    }
    System.out.println();
    System.out.format("Cases: %d  Valid: %d  Failures: %d\n",
        numCases, numValid, numCases - numValid);
  }

  private boolean runVoronoi(int i) {
    LineString line = createRandomLine(WIDTH, 4);
    Geometry lineBuf = line.buffer((WIDTH / 100));
    //System.out.println(lineBuf);
    boolean isValid = checkVoronoiValid(lineBuf);
    String msg = isValid ? "Valid" : ">>>>>>>  INVALID ";
    System.out.format("Case %d: %s\n", i, msg);
    return isValid;
  }

  private LineString createRandomLine(double width, int nPts) {
    Coordinate[] pts = new Coordinate[nPts];
    for (int i = 0; i < nPts; i++) {
      double x = width * Math.random();
      double y = width * Math.random();
      pts[i] = new Coordinate(x, y);
    }
    LineString line = geomFact.createLineString(pts);
    return line;
  }
  
  boolean checkVoronoiValid(Geometry sites) {
    Geometry result = null;
    try {
      result = computeVoronoi(sites); 
    }
    catch (TopologyException e) {
      return false;
    }

    return VoronoiChecker.isValid(result);
  }

  private static Geometry computeVoronoi(Geometry sites) {
    VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
    builder.setSites(sites);
    Geometry result = builder.getDiagram(sites.getFactory());
    return result;
  }
}
