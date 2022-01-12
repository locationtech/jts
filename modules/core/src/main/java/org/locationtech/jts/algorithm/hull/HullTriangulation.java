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
package org.locationtech.jts.algorithm.hull;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.operation.overlayng.CoverageUnion;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.locationtech.jts.triangulate.quadedge.QuadEdge;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.locationtech.jts.triangulate.quadedge.TriangleVisitor;
import org.locationtech.jts.triangulate.tri.Tri;
import org.locationtech.jts.triangulate.tri.TriangulationBuilder;
import org.locationtech.jts.util.Assert;

/**
 * Functions to operate on triangulations represented as
 * lists of {@link HullTri}s.
 * 
 * @author mdavis
 *
 */
class HullTriangulation 
{
  public static List<HullTri> createDelaunayTriangulation(Geometry geom) {
    //TODO: implement a DT on Tris directly?
    DelaunayTriangulationBuilder dt = new DelaunayTriangulationBuilder();
    dt.setSites(geom);
    QuadEdgeSubdivision subdiv = dt.getSubdivision();
    List<HullTri> triList = toTris(subdiv);
    return triList;
  }
  
  private static List<HullTri> toTris(QuadEdgeSubdivision subdiv) {
    HullTriVisitor visitor = new HullTriVisitor();
    subdiv.visitTriangles(visitor, false);
    List<HullTri> triList = visitor.getTriangles();
    TriangulationBuilder.build(triList);
    return triList;
  }
  
  private static class HullTriVisitor implements TriangleVisitor {
    private List<HullTri> triList = new ArrayList<HullTri>();

    public HullTriVisitor() {
    }

    public void visit(QuadEdge[] triEdges) {
      Coordinate p0 = triEdges[0].orig().getCoordinate();
      Coordinate p1 = triEdges[1].orig().getCoordinate();
      Coordinate p2 = triEdges[2].orig().getCoordinate();
      HullTri tri;
      if (Triangle.isCCW(p0, p1, p2)) {
        tri = new HullTri(p0, p2, p1);
      }
      else {
        tri = new HullTri(p0, p1, p2);
      }
      triList.add(tri);
    }
    
    public List<HullTri> getTriangles() {
      return triList;
    }
  }

  /**
   * Creates a polygonal geometry representing the area of a triangulation
   * which may be disconnected or contain holes.
   * 
   * @param triList the triangulation
   * @param geomFactory the geometry factory to use
   * @return the area polygonal geometry
   */
  public static Geometry union(List<? extends Tri> triList, GeometryFactory geomFactory) {
    List<Polygon> polys = new ArrayList<Polygon>();
    for (Tri tri : triList) {
      Polygon poly = tri.toPolygon(geomFactory);
      polys.add(poly);
    }
    return CoverageUnion.union(geomFactory.buildGeometry(polys));
  }
  
  /**
   * Creates a Polygon representing the area of a triangulation
   * which is connected and contains no holes.
   * 
   * @param triList the triangulation
   * @param geomFactory the geometry factory to use
   * @return the area polygon
   */
  public static Geometry traceBoundaryPolygon(List<HullTri> triList, GeometryFactory geomFactory) {
    if (triList.size() == 1) {
      Tri tri = triList.get(0);
      return tri.toPolygon(geomFactory);
    }
    Coordinate[] pts = traceBoundary(triList);
    return geomFactory.createPolygon(pts);
  }
  
  /**
   * Extracts the coordinates of the edges along the boundary of a triangulation,
   * by tracing CW around the border triangles.
   * Assumption: there are at least 2 tris, they are connected,
   * and there are no holes.
   * So each tri has at least one non-boundary edge, and there is only one boundary.
   * 
   * @param triList the triangulation
   * @return the points in the boundary of the triangulation
   */
  private static Coordinate[] traceBoundary(List<HullTri> triList) {
    HullTri triStart = findBorderTri(triList);
    CoordinateList coordList = new CoordinateList();
    HullTri tri = triStart;
    do {
      int boundaryIndex = tri.boundaryIndexCCW();
      //-- add border vertex
      coordList.add(tri.getCoordinate(boundaryIndex).copy(), false);
      int nextIndex = Tri.next(boundaryIndex);
      //-- if next edge is also on boundary, add it and move to next
      if (tri.isBoundary(nextIndex)) {
        coordList.add(tri.getCoordinate(nextIndex).copy(), false);
        boundaryIndex = nextIndex;
      }
      //-- find next border tri CCW around non-boundary edge
      tri = nextBorderTri(tri);
    } while (tri != triStart);
    coordList.closeRing();
    return coordList.toCoordinateArray();
  }
  
  private static HullTri findBorderTri(List<HullTri> triList) {
    for (HullTri tri : triList) {
      if (tri.isBorder()) return tri;
    }
    Assert.shouldNeverReachHere("No border triangles found");
    return null;
  }
  
  public static HullTri nextBorderTri(HullTri triStart) {
    HullTri tri = triStart;
    //-- start at first non-border edge CW
    int index = Tri.next(tri.boundaryIndexCW());
    //-- scan CCW around vertex for next border tri
    do {
      HullTri adjTri = (HullTri) tri.getAdjacent(index);
      if (adjTri == tri)
        throw new IllegalStateException("No outgoing border edge found");
      index = Tri.next(adjTri.getIndex(tri));
      tri = adjTri;
    }
    while (! tri.isBoundary(index));
    return (tri);
  }
}
