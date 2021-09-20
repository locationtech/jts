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
package org.locationtech.jts.triangulate.polygon;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.triangulate.tri.Tri;
import org.locationtech.jts.triangulate.tri.TriangulationBuilder;

/**
 * Computes the Constrained Delaunay Triangulation of polygons.
 * The Constrained Delaunay Triangulation of a polygon is a set of triangles
 * covering the polygon, with the maximum total interior angle over all 
 * possible triangulations.  It provides the "best quality" triangulation
 * of the polygon.
 * <p>
 * Holes are supported.
 */
public class ConstrainedDelaunayTriangulator {
  
  /**
   * Computes the Constrained Delaunay Triangulation of each polygon element in a geometry.
   * 
   * @param geom the input geometry
   * @return a GeometryCollection of the computed triangle polygons
   */
  public static Geometry triangulate(Geometry geom) {
    ConstrainedDelaunayTriangulator cdt = new ConstrainedDelaunayTriangulator(geom);
    return cdt.getResult();
  }
  
  private final GeometryFactory geomFact;
  private final Geometry inputGeom;
  private List<Tri> triList;

  /**
   * Constructs a new Constrained Delaunay triangulator.
   * 
   * @param inputGeom the input geometry
   */
  public ConstrainedDelaunayTriangulator(Geometry inputGeom) {
    geomFact = inputGeom.getFactory();
    this.inputGeom = inputGeom;
  }

  /**
   * Gets the triangulation as a {@link GeometryCollection} of triangular {@link Polygon}s.
   * 
   * @return a collection of the result triangle polygons
   */
  public Geometry getResult() {
    compute();
    return Tri.toGeometry(triList, geomFact);
  }
  
  /**
   * Gets the triangulation as a list of {@link Tri}s.
   * 
   * @return the list of Tris in the triangulation
   */
  public List<Tri> getTriangles() {
    compute();
    return triList;
  }
  
  private void compute() {
    if (triList != null) return;
    
    List<Polygon> polys = PolygonExtracter.getPolygons(inputGeom);
    triList = new ArrayList<Tri>();
    for (Polygon poly : polys) {
      List<Tri> polyTriList = triangulatePolygon(poly);
      triList.addAll(polyTriList);
    }
  }
 
  /**
   * Computes the triangulation of a single polygon
   * and returns it as a list of {@link Tri}s.
   * 
   * @param poly the input polygon
   * @return list of Tris forming the triangulation
   */
  List<Tri> triangulatePolygon(Polygon poly) {
    /**
     * Normalize to ensure that shell and holes have canonical orientation.
     * 
     * TODO: perhaps better to just correct orientation of rings?
     */
    Polygon polyNorm = (Polygon) poly.norm();
    Coordinate[] polyShell = PolygonHoleJoiner.join(polyNorm);
    List<Tri> triList = PolygonEarClipper.triangulate(polyShell);
    
    //long start = System.currentTimeMillis();
    TriangulationBuilder.build(triList);
    TriDelaunayImprover.improve(triList);
    //System.out.println("swap used: " + (System.currentTimeMillis() - start) + " milliseconds");

    return triList;
  }

}
