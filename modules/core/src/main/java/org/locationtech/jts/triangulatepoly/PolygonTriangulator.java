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

public class PolygonTriangulator {

  public static Geometry triangulate(Geometry geom) {
    PolygonTriangulator clipper = new PolygonTriangulator(geom);
    return clipper.compute();
  }
  
  public static Geometry triangulateRaw(Geometry geom) {
    PolygonTriangulator clipper = new PolygonTriangulator(geom);
    clipper.setImprove(false);
    return clipper.compute();
  }
  
  private final GeometryFactory gf;
  private final Geometry inputGeom;
  private boolean isImprove = true;

  /**
   * Constructs a new polygon triangulator.
   * 
   * @param inputGeom the input geometry
   */
  public PolygonTriangulator(Geometry inputGeom) {
    gf = new GeometryFactory();
    this.inputGeom = inputGeom;
  }

  /**
   * Sets whether the triangulation quality should be improved
   * after computation.
   * 
   * @param isImproved true if the triangulation quality is improved
   */
  public void setImprove(boolean isImproved) {
    this.isImprove = isImproved;
  }

  private Geometry compute() {
    List<Polygon> polys = PolygonExtracter.getPolygons(inputGeom);
    List<Polygon> triPolys = new ArrayList<Polygon>();
    for (Polygon poly : polys) {
      List<Tri> triList = triangulatePolygon(poly);
      for (Tri tri: triList) {
        triPolys.add(tri.toPolygon(gf));
      }
    }
    return gf.createGeometryCollection(GeometryFactory.toGeometryArray(triPolys));
  }
 
  /**
   * Performs the ear-clipping triangulation
   * 
   * @return GeometryCollection of triangular polygons
   */
  List<Tri> triangulatePolygon(Polygon poly) {
    /**
     * Normalize to ensure that shell and holes have canonical orientation.
     * 
     * TODO: perhaps better to just correct orientation of rings?
     */
    Polygon polyNorm = (Polygon) poly.norm();
    List<Coordinate> polyShell = HoleJoiner.computePoints(polyNorm);
    
    List<Tri> triList = EarClipper.clip(polyShell);
    //Tri.validate(triList);
    
    if ( isImprove ) {
      //long start = System.currentTimeMillis();
      DelaunayImprover improver = new DelaunayImprover();
      improver.improve(triList);
      //System.out.println("improve used: " + (System.currentTimeMillis() - start) + " milliseconds");
    }
    return triList;
  }

}
