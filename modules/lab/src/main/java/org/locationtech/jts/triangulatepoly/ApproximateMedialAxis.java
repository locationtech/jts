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
package org.locationtech.jts.triangulatepoly;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.triangulatepoly.tri.Tri;

public class ApproximateMedialAxis {

  public static Geometry computeAxis(Geometry geom) {
    ApproximateMedialAxis tt = new ApproximateMedialAxis((Polygon) geom);
    return tt.compute();
  }
  
  private Polygon inputPolygon;
  private GeometryFactory geomFact;
  private List<LineString> lines = new ArrayList<LineString>();

  public ApproximateMedialAxis(Polygon polygon) {
    this.inputPolygon = polygon;
    geomFact = inputPolygon.getFactory();
  }
  
  private Geometry compute() {
    ConstrainedDelaunayTriangulator cdt = new ConstrainedDelaunayTriangulator(inputPolygon);
    List<Tri> tris = cdt.triangulatePolygon(inputPolygon);
    
    for (Tri tri : tris) {
      if (tri.numAdjacent() == 1) {
        lines.add(generateLineAdj1(tri));
      }
      else if (tri.numAdjacent() == 2) {
        lines.add(generateLineAdj2(tri));
      }
      else if (tri.numAdjacent() == 3) {
        LineString[] line3 = generateLineAdj3(tri);
        lines.add(line3[0]);
        lines.add(line3[1]);
        lines.add(line3[2]);
      }

    }
    
    return geomFact.createMultiLineString(GeometryFactory.toLineStringArray(lines));
  }

  private LineString generateLineAdj1(Tri triStart) {
    int iAdj = indexOfAdjacent(triStart);
    int iOpp = Tri.prev(iAdj);
    Coordinate v0 = triStart.getCoordinate(iOpp);
    Coordinate midOpp = triStart.midpoint(iAdj);
    return line(v0, midOpp);
  }

  private LineString generateLineAdj2(Tri tri) {
    int iNoAdj = indexOfNoAdjacent(tri);
    int iOpp1 = Tri.prev(iNoAdj);
    int iOpp2 = Tri.next(iNoAdj);
    Coordinate v0 = tri.midpoint(iOpp1);
    Coordinate v1 = tri.midpoint(iOpp2);
    return line(v0, v1);
  }

  private LineString[] generateLineAdj3(Tri tri) {
    /**
     * Circumcentre doesn't work because it lies outside obtuse triangles.
     * Centroid is too affected by a side of very different length.
     * Maybe some centre which is biased towards sides with most similar length?
     */
    Coordinate cc = Triangle.circumcentre(tri.getCoordinate(0), 
        tri.getCoordinate(1), tri.getCoordinate(2));
    Coordinate v0 = tri.midpoint(0);
    Coordinate v1 = tri.midpoint(1);
    Coordinate v2 = tri.midpoint(2);
    LineString line0 = line(v0, cc.copy());
    LineString line1 = line(v1, cc.copy());
    LineString line2 = line(v2, cc.copy());
    return new LineString[] { line0, line1, line2 };
  }

  private LineString line(Coordinate p0, Coordinate p1) {
    return geomFact.createLineString(new Coordinate[] { p0, p1 });
  }

  private int indexOfAdjacent(Tri tri) {
    for (int i = 0; i < 3; i++) {
      if (tri.hasAdjacent(i))
        return i;
    }
    return -1;
  }
  private int indexOfNoAdjacent(Tri tri) {
    for (int i = 0; i < 3; i++) {
      if (! tri.hasAdjacent(i))
        return i;
    }
    return -1;
  }

}
