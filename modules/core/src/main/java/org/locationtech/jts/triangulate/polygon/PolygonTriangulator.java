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

/**
 * Computes a triangulation of each polygon in a {@link Geometry}.
 * A polygon triangulation is a non-overlapping set of triangles which
 * cover the polygon and have the same vertices as the polygon.
 * The priority is on performance rather than triangulation quality,
 * so that the output may contain many narrow triangles.
 * <p>
 * Holes are handled by joining them to the shell to form a 
 * (self-touching) polygon shell with no holes.
 * Although invalid, this can be triangulated effectively.
 * <P>
 * For better-quality triangulation use {@link ConstrainedDelaunayTriangulator}.
 * 
 * @see ConstrainedDelaunayTriangulator
 * 
 * @author Martin Davis
 *
 */
public class PolygonTriangulator {
  
  /**
   * Computes a triangulation of each polygon in a geometry.
   * 
   * @param geom a geometry containing polygons
   * @return a GeometryCollection containing the triangle polygons
   */
  public static Geometry triangulate(Geometry geom) {
    PolygonTriangulator triangulator = new PolygonTriangulator(geom);
    return triangulator.getResult();
  }
  
  private final GeometryFactory geomFact;
  private final Geometry inputGeom;
  private List<Tri> triList;

  /**
   * Constructs a new triangulator.
   * 
   * @param inputGeom the input geometry
   */
  public PolygonTriangulator(Geometry inputGeom) {
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
    @SuppressWarnings("unchecked")
    List<Polygon> polys = PolygonExtracter.getPolygons(inputGeom);
    triList = new ArrayList<Tri>();
    for (Polygon poly : polys) {
      if (poly.isEmpty()) continue;
      List<Tri> polyTriList = triangulatePolygon(poly);
      triList.addAll(polyTriList);
    }
  }
 
  /**
   * Computes the triangulation of a single polygon
   * 
   * @return GeometryCollection of triangular polygons
   */
  private List<Tri> triangulatePolygon(Polygon poly) {
    /**
     * Normalize to ensure that shell and holes have canonical orientation.
     * 
     * TODO: perhaps better to just correct orientation of rings?
     */
    Polygon polyNorm = (Polygon) poly.norm();
    Coordinate[] polyShell = PolygonHoleJoiner.join(polyNorm);
    
    List<Tri> triList = PolygonEarClipper.triangulate(polyShell);
    //Tri.validate(triList);

    return triList;
  }

}
