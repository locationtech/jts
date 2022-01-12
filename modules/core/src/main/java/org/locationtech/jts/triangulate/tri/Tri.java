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
package org.locationtech.jts.triangulate.tri;

import java.util.List;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Assert;

/**
 * A memory-efficient representation of a triangle in a triangulation.
 * Contains three vertices, and links to adjacent Tris for each edge.
 * Tris are constructed independently, and if needed linked
 * into a triangulation using {@link TriangulationBuilder}.
 * <p>
 * An edge of a Tri in a triangulation is called a boundary edge
 * if it has no adjacent triangle.
 * The set of Tris containing boundary edges are called the triangulation border. 
 * 
 * @author Martin Davis
 *
 */
public class Tri {
  
  /**
   * Creates a {@link GeometryCollection} of {@link Polygon}s
   * representing the triangles in a list.
   * 
   * @param triList a list of Tris
   * @param geomFact the GeometryFactory to use
   * @return the polygons for the triangles
   */
  public static Geometry toGeometry(List<Tri> triList, GeometryFactory geomFact) {
    Geometry[] geoms = new Geometry[triList.size()];
    for (int i = 0; i < triList.size(); i++) {
      geoms[i] = triList.get(i).toPolygon(geomFact);
    }
    return geomFact.createGeometryCollection(geoms);
  }

  /**
   * Computes the area of a set of Tris.
   * 
   * @param triList a set of Tris
   * @return the total area of the triangles
   */
  public static double area(List<? extends Tri> triList) {
    double area = 0;
    for (Tri tri : triList) {
      area += tri.getArea();
    }
    return area;
  }
  
  /**
   * Validates a list of Tris.
   * 
   * @param triList the tris to validate
   */
  public static void validate(List<Tri> triList) {
    for (Tri tri : triList) {
      tri.validate();
    }
  }
  
  /**
   * Creates a triangle with the given vertices.
   * The vertices should be oriented clockwise.
   * 
   * @param p0 the first triangle vertex
   * @param p1 the second triangle vertex
   * @param p2 the third triangle vertex
   * @return the created triangle
   */
  public static Tri create(Coordinate p0, Coordinate p1, Coordinate p2) {
    return new Tri(p0, p1, p2);
  }
  
  /**
   * Creates a triangle from an array with three vertex coordinates.
   * The vertices should be oriented clockwise.
   * 
   * @param pts the array of vertex coordinates
   * @return the created triangle
   */
  public static Tri create(Coordinate[] pts) {
    return new Tri(pts[0], pts[1], pts[2]);
  }
  
  protected Coordinate p0;
  protected Coordinate p1;
  protected Coordinate p2;
  
  /**
   * triN is the adjacent triangle across the edge pN - pNN.
   * pNN is the next vertex CW from pN.
   */
  protected Tri tri0;
  protected Tri tri1;
  protected Tri tri2;

  /**
   * Creates a triangle with the given vertices.
   * The vertices should be oriented clockwise.
   * 
   * @param p0 the first triangle vertex
   * @param p1 the second triangle vertex
   * @param p2 the third triangle vertex
   */
  public Tri(Coordinate p0, Coordinate p1, Coordinate p2) {
    this.p0 = p0;
    this.p1 = p1;
    this.p2 = p2;
    //Assert.isTrue( Orientation.CLOCKWISE != Orientation.index(p0, p1, p2), "Tri is not oriented correctly");
  }

  /**
   * Sets the adjacent triangles.
   * The vertices of the adjacent triangles are
   * assumed to match the appropriate vertices in this triangle.
   * 
   * @param tri0 the triangle adjacent to edge 0
   * @param tri1 the triangle adjacent to edge 1
   * @param tri2 the triangle adjacent to edge 2
   */
  public void setAdjacent(Tri tri0, Tri tri1, Tri tri2) {
    this.tri0 = tri0;
    this.tri1 = tri1;
    this.tri2 = tri2;
  }

  /**
   * Sets the triangle adjacent to the edge originating
   * at a given vertex.
   * The vertices of the adjacent triangles are
   * assumed to match the appropriate vertices in this triangle.
   * 
   * @param pt the edge start point
   * @param tri the adjacent triangle
   */
  public void setAdjacent(Coordinate pt, Tri tri) {
    int index = getIndex(pt);
    setTri(index, tri);
    // TODO: validate that tri is adjacent at the edge specified
  }
  
  /**
   * Sets the triangle adjacent to an edge.
   * The vertices of the adjacent triangle are
   * assumed to match the appropriate vertices in this triangle.
   * 
   * @param edgeIndex the edge triangle is adjacent to
   * @param tri the adjacent triangle
   */
  public void setTri(int edgeIndex, Tri tri) {
    switch (edgeIndex) {
    case 0: tri0 = tri; return;
    case 1: tri1 = tri; return;
    case 2: tri2 = tri; return;
    }
    Assert.shouldNeverReachHere();
  }

  private void setCoordinates(Coordinate p0, Coordinate p1, Coordinate p2) {
    this.p0 = p0;
    this.p1 = p1;
    this.p2 = p2;
    //Assert.isTrue( Orientation.CLOCKWISE != Orientation.index(p0, p1, p2), "Tri is not oriented correctly");
  }

  /**
   * Spits a triangle by a point located inside the triangle. 
   * Creates the three new resulting triangles with adjacent links
   * set correctly.  
   * Returns the new triangle whose 0'th vertex is the splitting point.
   * 
   * @param p the point to insert
   * @return the new triangle whose 0'th vertex is p
   */
  public Tri split(Coordinate p) {
    Tri tt0 = new Tri(p, p0, p1);
    Tri tt1 = new Tri(p, p1, p2);
    Tri tt2 = new Tri(p, p2, p0);
    tt0.setAdjacent(tt2, tri0, tt1);
    tt1.setAdjacent(tt0, tri1, tt2);
    tt2.setAdjacent(tt1, tri2, tt0);
    return tt0;
  }
  
  /**
   * Interchanges the vertices of this triangle and a neighbor 
   * so that their common edge
   * becomes the the other diagonal of the quadrilateral they form.
   * Neighbour triangle links are modified accordingly.
   * 
   * @param index the index of the adjacent tri to flip with
   */
  public void flip(int index) {
    Tri tri = getAdjacent(index);
    int index1 = tri.getIndex(this);

    Coordinate adj0 = getCoordinate(index);
    Coordinate adj1 = getCoordinate(next(index));
    Coordinate opp0 = getCoordinate(oppVertex(index));
    Coordinate opp1 = tri.getCoordinate(oppVertex(index1));
    
    flip(tri, index, index1, adj0, adj1, opp0, opp1);
  }
  
  private void flip(Tri tri, int index0, int index1, Coordinate adj0, Coordinate adj1, Coordinate opp0, Coordinate opp1) {
    //System.out.println("Flipping: " + this + " -> " + tri);
    
    //validate();
    //tri.validate();
    
    this.setCoordinates(opp1, opp0, adj0);
    tri.setCoordinates(opp0, opp1, adj1);
    /**
     *  Order: 0: opp0-adj0 edge, 1: opp0-adj1 edge, 
     *  2: opp1-adj0 edge, 3: opp1-adj1 edge
     */
    Tri[] adjacent = getAdjacentTris(tri, index0, index1);
    this.setAdjacent(tri, adjacent[0], adjacent[2]);
    //--- update the adjacent triangles with new adjacency
    if ( adjacent[2] != null ) {
      adjacent[2].replace(tri, this);
    }
    tri.setAdjacent(this, adjacent[3], adjacent[1]);
    if ( adjacent[1] != null ) {
      adjacent[1].replace(this, tri);
    }
    //validate();
    //tri.validate();
  }
  
  /**
   * Replaces an adjacent triangle with a different one.
   * 
   * @param triOld an adjacent triangle
   * @param triNew the triangle to replace it with
   */
  private void replace(Tri triOld, Tri triNew) {
    if ( tri0 != null && tri0 == triOld ) {
      tri0 = triNew;
    } else if ( tri1 != null && tri1 == triOld ) {
      tri1 = triNew;
    } else if ( tri2 != null && tri2 == triOld ) {
      tri2 = triNew;
    }
  }
  
  /**
   * Computes the degree of a Tri vertex, which is the number of tris containing it.
   * This must be done by searching the entire triangulation, 
   * since the containing tris may not be adjacent or edge-connected. 
   * 
   * @param index the vertex index
   * @param triList the triangulation
   * @return the degree of the vertex
   */
  public int degree(int index, List<? extends Tri> triList) {
    Coordinate v = getCoordinate(index);
    int degree = 0;
    for (Tri tri : triList) {
      for (int i = 0; i < 3; i++) {
        if (v.equals2D(tri.getCoordinate(i)))
          degree++;
      }
    }
    return degree;
  }
  
  /**
   * Removes this tri from the triangulation containing it.
   * All links between the tri and adjacent ones are nulled.
   * 
   * @param triList the triangulation
   */
  public void remove(List<? extends Tri> triList) {
    remove();
    triList.remove(this);
  }
  
  /**
   * Removes this triangle from a triangulation.
   * All adjacent references and the references to this
   * Tri in the adjacent Tris are set to <code>null</code.
   */
  public void remove() {
    remove(0);
    remove(1);
    remove(2);
  }

  private void remove(int index) {
    Tri adj = getAdjacent(index);
    if (adj == null) return;
    adj.setTri(adj.getIndex(this), null);
    setTri(index, null);
  }
  
  /**
   * Gets the triangles adjacent to the quadrilateral
   * formed by this triangle and an adjacent one.
   * The triangles are returned in the following order:
   * <p>
   * Order: 0: opp0-adj0 edge, 1: opp0-adj1 edge, 
   *  2: opp1-adj0 edge, 3: opp1-adj1 edge
   *  
   * @param tri1 an adjacent triangle
   * @param index the index of the common edge in this triangle
   * @param index1 the index of the common edge in the adjacent triangle
   * @return
   */
  private Tri[] getAdjacentTris(Tri triAdj, int index, int indexAdj) {
    Tri[] adj = new Tri[4];
    adj[0] = getAdjacent(prev(index));
    adj[1] = getAdjacent(next(index));
    adj[2] = triAdj.getAdjacent(next(indexAdj));
    adj[3] = triAdj.getAdjacent(prev(indexAdj));
    return adj;
  }

  /**
   * Validates that a tri is correct.
   * Currently just checks that orientation is CW.
   * 
   * @throw IllegalArgumentException if tri is not valid
   */
  public void validate() {
    if ( Orientation.CLOCKWISE != Orientation.index(p0, p1, p2) ) {
      throw new IllegalArgumentException("Tri is not oriented correctly");
    }

    validateAdjacent(0);
    validateAdjacent(1);
    validateAdjacent(2);
  }
  
  /**
   * Validates that the vertices of an adjacent linked triangle are correct.
   * 
   * @param index the index of the adjacent triangle
   */
  public void validateAdjacent(int index) {
    Tri tri = getAdjacent(index);
    if (tri == null) return;
    
    assert(this.isAdjacent(tri));
    assert(tri.isAdjacent(this));
    
    Coordinate e0 = getCoordinate(index);
    Coordinate e1 = getCoordinate(next(index));
    int indexNeighbor = tri.getIndex(this);
    Coordinate n0 = tri.getCoordinate(indexNeighbor);
    Coordinate n1 = tri.getCoordinate(next(indexNeighbor));
    Assert.isTrue(e0.equals2D(n1), "Edge coord not equal");
    Assert.isTrue(e1.equals2D(n0), "Edge coord not equal");
    
    //--- check that no edges cross
    RobustLineIntersector li = new RobustLineIntersector();
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        Coordinate p00 = getCoordinate(i);
        Coordinate p01 = getCoordinate(next(i));
        Coordinate p10 = tri.getCoordinate(j);
        Coordinate p11 = tri.getCoordinate(next(j));
        li.computeIntersection(p00,  p01,  p10, p11);
        assert(! li.isProper());
      }
    }
  }

  /**
   * Gets the start and end vertex of the edge adjacent to another triangle.
   * 
   * @param neighbor
   * @return
   */
  /*
  //TODO: define when needed 
  public Coordinate[] getEdge(Tri neighbor) {
    int index = getIndex(neighbor);
    int next = next(index);

    Coordinate e0 = getCoordinate(index);
    Coordinate e1 = getCoordinate(next);
    assert (neighbor.hasCoordinate(e0));
    assert (neighbor.hasCoordinate(e1));
    int iN = neighbor.getIndex(e0);
    int iNPrev = prev(iN);
    assert (neighbor.getIndex(e1) == iNPrev);

    return new Coordinate[] { getCoordinate(index), getCoordinate(next) };
  }

  public Coordinate getEdgeStart(int i) {
    return getCoordinate(i);
  }
  
  public Coordinate getEdgeEnd(int i) {
    return getCoordinate(next(i));
  }
  
  public boolean hasCoordinate(Coordinate v) {
    if ( p0.equals(v) || p1.equals(v) || p2.equals(v) ) {
      return true;
    }
    return false;
  }
   */
  
  /**
   * Gets the coordinate for a vertex.
   * This is the start vertex of the edge.
   * 
   * @param index the vertex (edge) index
   * @return the vertex coordinate
   */
  public Coordinate getCoordinate(int index) {
    if ( index == 0 ) {
      return p0;
    }
    if ( index == 1 ) {
      return p1;
    }
    return p2;
  }

  /**
   * Gets the index of the triangle vertex which has a given coordinate (if any).
   * This is also the index of the edge which originates at the vertex.
   * 
   * @param p the coordinate to find
   * @return the vertex index, or -1 if it is not in the triangle
   */
  public int getIndex(Coordinate p) {
    if ( p0.equals2D(p) )
      return 0;
    if ( p1.equals2D(p) )
      return 1;
    if ( p2.equals2D(p) )
      return 2;
    return -1;
  }

  /**
   * Gets the edge index which a triangle is adjacent to (if any),
   * based on the adjacent triangle link.
   * 
   * @param tri the tri to find
   * @return the index of the edge adjacent to the triangle, or -1 if not found
   */
  public int getIndex(Tri tri) {
    if ( tri0 == tri )
      return 0;
    if ( tri1 == tri )
      return 1;
    if ( tri2 == tri )
      return 2;
    return -1;
  }
  
  /**
   * Gets the triangle adjacent to an edge.
   * 
   * @param index the edge index
   * @return the adjacent triangle (may be null)
   */
  public Tri getAdjacent(int index) {
    switch(index) {
    case 0: return tri0;
    case 1: return tri1;
    case 2: return tri2;
    }
    Assert.shouldNeverReachHere();
    return null;
  }

  /**
   * Tests if this tri has any adjacent tris.
   * 
   * @return true if there is at least one adjacent tri
   */
  public boolean hasAdjacent() {
    return hasAdjacent(0) 
        || hasAdjacent(1) || hasAdjacent(2);
  }
  
  /**
   * Tests if there is an adjacent triangle to an edge.
   * 
   * @param index the edge index
   * @return true if there is a triangle adjacent to edge
   */
  public boolean hasAdjacent(int index) {
    return null != getAdjacent(index);
  }

  /**
   * Tests if a triangle is adjacent to some edge of this triangle.
   * 
   * @param tri the triangle to test
   * @return true if the triangle is adjacent
   * @see getIndex(Tri)
   */
  public boolean isAdjacent(Tri tri) {
    return getIndex(tri) >= 0;
  }

  /**
   * Computes the number of triangle adjacent to this triangle.
   * This is a number in the range [0,2].
   * 
   * @return the number of adjacent triangles
   */
  public int numAdjacent() {
    int num = 0;
    if ( tri0 != null )
      num++;
    if ( tri1 != null )
      num++;
    if ( tri2 != null )
      num++;
    return num;
  }

  /**
   * Tests if a tri vertex is interior.
   * A vertex of a triangle is interior if it 
   * is fully surrounded by other triangles.
   * 
   * @param index the vertex index
   * @return true if the vertex is interior
   */
  public boolean isInteriorVertex(int index) {
    Tri curr = this;
    int currIndex = index;
    do {
      Tri adj = curr.getAdjacent(currIndex);
      if (adj == null) return false;
      int adjIndex = adj.getIndex(curr);
      curr = adj;
      currIndex = Tri.next(adjIndex);
    }
    while (curr != this);
    return true;
  }
  
  /**
   * Tests if a tri contains a boundary edge,
   * and thus on the border of the triangulation containing it.
   * 
   * @return true if the tri is on the border of the triangulation
   */
  public boolean isBorder() {
    return isBoundary(0) || isBoundary(1) || isBoundary(2);
  }
  
  /**
   * Tests if an edge is on the boundary of a triangulation.
   * 
   * @param index index of an edge
   * @return true if the edge is on the boundary
   */
  public boolean isBoundary(int index) {
    return ! hasAdjacent(index);
  }
  
  /**
   * Computes the vertex or edge index which is the next one
   * (clockwise) around the triangle.
   * 
   * @param index the index
   * @return the next index value
   */
  public static int next(int index) {
    switch (index) {
    case 0: return 1;
    case 1: return 2;
    case 2: return 0;
    }
    return -1;
  }

  /**
   * Computes the vertex or edge index which is the previous one
   * (counter-clockwise) around the triangle.
   * 
   * @param index the index
   * @return the previous index value
   */
  public static int prev(int index) {
    switch (index) {
    case 0: return 2;
    case 1: return 0;
    case 2: return 1;
    }
    return -1;
  }

  /**
   * Gets the index of the vertex opposite an edge.
   * 
   * @param edgeIndex the edge index
   * @return the index of the opposite vertex
   */
  public static int oppVertex(int edgeIndex) {
    return prev(edgeIndex);
  }

  /**
   * Gets the index of the edge opposite a vertex.
   * 
   * @param vertexIndex the index of the vertex
   * @return the index of the opposite edge
   */
  public static int oppEdge(int vertexIndex) {
    return next(vertexIndex);
  }

  /**
   * Computes a coordinate for the midpoint of a triangle edge.
   * 
   * @param edgeIndex the edge index
   * @return the midpoint of the triangle edge
   */
  public Coordinate midpoint(int edgeIndex) {
    Coordinate p0 = getCoordinate(edgeIndex);
    Coordinate p1 = getCoordinate(next(edgeIndex));
    double midX = (p0.getX() + p1.getX()) / 2;
    double midY = (p0.getY() + p1.getY()) / 2;
    return new Coordinate(midX, midY);
  }
  
  /**
   * Gets the area of the triangle.
   * 
   * @return the area of the triangle
   */
  public double getArea() {
    return Triangle.area(p0, p1, p2);
  }
  
  /**
   * Gets the length of the perimeter of the triangle.
   * 
   * @return the length of the perimeter
   */
  public double getLength() {
    return Triangle.length(p0, p1, p2);
  }
  
  /**
   * Creates a {@link Polygon} representing this triangle.
   * 
   * @param geomFact the geometry factory
   * @return a polygon
   */
  public Polygon toPolygon(GeometryFactory geomFact) {
    return geomFact.createPolygon(
        geomFact.createLinearRing(new Coordinate[] { p0.copy(), p1.copy(), p2.copy(), p0.copy() }), null);
  }

  @Override
  public String toString() {
    return String.format("POLYGON ((%s, %s, %s, %s))", 
        WKTWriter.format(p0), WKTWriter.format(p1), WKTWriter.format(p2),
        WKTWriter.format(p0));
  }

}
