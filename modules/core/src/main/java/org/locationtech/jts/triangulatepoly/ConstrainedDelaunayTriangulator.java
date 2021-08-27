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
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.triangulatepoly.tri.Tri;
import org.locationtech.jts.triangulatepoly.tri.TriDelaunayImprover;
import org.locationtech.jts.triangulatepoly.tri.TriangulationBuilder;

public class ConstrainedDelaunayTriangulator {
  
  /**
   * Computes a Constrained Triangulation of the polygon elements in a geometry.
   * 
   * @param geom the input geometry
   * @return a collection of the computed triangles
   */
  public static Geometry triangulate(Geometry geom) {
    ConstrainedDelaunayTriangulator cdt = new ConstrainedDelaunayTriangulator(geom);
    return cdt.compute();
  }
  
  private final GeometryFactory geomFact;
  private final Geometry inputGeom;

  /**
   * Constructs a new Constrained Delaunay triangulator.
   * 
   * @param inputGeom the input geometry
   */
  public ConstrainedDelaunayTriangulator(Geometry inputGeom) {
    geomFact = new GeometryFactory();
    this.inputGeom = inputGeom;
  }

  private Geometry compute() {
    List<Polygon> polys = PolygonExtracter.getPolygons(inputGeom);
    List<Tri> triList = new ArrayList<Tri>();
    for (Polygon poly : polys) {
      List<Tri> polyTriList = triangulatePolygon(poly);
      triList.addAll(polyTriList);
    }
    return Tri.toGeometry(triList, geomFact);
  }
 
  /**
   * Computes the triangulation of a single polygon.
   * 
   * @param poly the input polygon
   * @return GeometryCollection of triangular polygons
   */
  List<Tri> triangulatePolygon(Polygon poly) {
    /**
     * Normalize to ensure that shell and holes have canonical orientation.
     * 
     * TODO: perhaps better to just correct orientation of rings?
     */
    Polygon polyNorm = (Polygon) poly.norm();
    Coordinate[] polyShell = PolygonHoleJoiner.join(polyNorm);
    List<Tri> triList = PolygonEarClipper.clip(polyShell);
    
    //long start = System.currentTimeMillis();
    TriangulationBuilder.build(triList);
    TriDelaunayImprover.improve(triList);
    //System.out.println("swap used: " + (System.currentTimeMillis() - start) + " milliseconds");

    return triList;
  }

}
