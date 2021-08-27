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
package org.locationtech.jts.triangulatepoly.tri;

import java.util.List;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.util.Assert;

/**
 * A memory-efficient representation of a triangle in a triangulation.
 * Contains three vertices, and links to adjacent Tris for each edge.
 * Tris are constructed independently, and if needed linked
 * into a triangulation using {@link TriangulationBuilder}.
 * 
 * @author mdavis
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

  public static void validate(List<Tri> triList) {
    for (Tri tri : triList) {
      tri.validate();
    }
  }
  
  public static Tri create(Coordinate p0, Coordinate p1, Coordinate p2) {
    return new Tri(p0, p1, p2);
  }
  
  public static Tri create(Coordinate[] pts) {
    return new Tri(pts[0], pts[1], pts[2]);
  }
  
  private Coordinate p0;
  private Coordinate p1;
  private Coordinate p2;
  
  /**
   * triN is the adjacent triangle across the edge pN - pNN.
   * pNN is the next vertex CW from pN.
   */
  private Tri tri0;
  private Tri tri1;
  private Tri tri2;

  public Tri(Coordinate p0, Coordinate p1, Coordinate p2) {
    this.p0 = p0;
    this.p1 = p1;
    this.p2 = p2;
    //Assert.isTrue( Orientation.CLOCKWISE != Orientation.index(p0, p1, p2), "Tri is not oriented correctly");
  }

  public void setAdjacent(Tri tri0, Tri tri1, Tri tri2) {
    this.tri0 = tri0;
    this.tri1 = tri1;
    this.tri2 = tri2;
  }

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

  public void setAdjacent(Coordinate pt, Tri tri) {
    int index = getIndex(pt);
    setTri(index, tri);
    // TODO: validate that tri is adjacent at the edge specified
  }
  
  /**
   * Swap triOld with triNew
   * 
   * @param triOld
   * @param triNew
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
   * Spits a triangle by a point located inside the triangle. Returns a new
   * triangle whose 0'th vertex is p
   * 
   * @param p the point to insert
   * @return a new triangle whose 0'th vertex is p
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
  
  public void swap(Tri tri) {
    int index0 = getIndex(tri);
    int index1 = tri.getIndex(this);

    Coordinate adj0 = getCoordinate(index0);
    Coordinate adj1 = getCoordinate(next(index0));
    Coordinate opp0 = getCoordinate(oppVertex(index0));
    Coordinate opp1 = tri.getCoordinate(oppVertex(index1));
    
    swap(tri, index0, index1, adj0, adj1, opp0, opp1);
  }
  
  public void swap(Tri tri, int index0, int index1, Coordinate adj0, Coordinate adj1, Coordinate opp0, Coordinate opp1) {
    //System.out.println("Swapping: " + this + " -> " + tri);
    
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
   * 
   * Order: 0: opp0-adj0 edge, 1: opp0-adj1 edge, 
   *  2: opp1-adj0 edge, 3: opp1-adj1 edge
   *  
   * @param tri
   * @param index0
   * @param index1
   * @return
   */
  private Tri[] getAdjacentTris(Tri tri, int index0, int index1) {
    Tri[] adj = new Tri[4];
    adj[0] = getAdjacent(prev(index0));
    adj[1] = getAdjacent(next(index0));
    adj[2] = tri.getAdjacent(next(index1));
    adj[3] = tri.getAdjacent(prev(index1));
    return adj;
  }

  public void validate() {
    if ( Orientation.CLOCKWISE != Orientation.index(p0, p1, p2) ) {
      throw new IllegalArgumentException("Tri is not oriented correctly");
    }

    validateAdjacent(0);
    validateAdjacent(1);
    validateAdjacent(2);
  }
  
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

  public Coordinate getCoordinate(int i) {
    if ( i == 0 ) {
      return p0;
    }
    if ( i == 1 ) {
      return p1;
    }
    return p2;
  }

  public int getIndex(Coordinate p) {
    if ( p0.equals2D(p) )
      return 0;
    if ( p1.equals2D(p) )
      return 1;
    if ( p2.equals2D(p) )
      return 2;
    return -1;
  }

  public int getIndex(Tri tri) {
    if ( tri0 == tri )
      return 0;
    if ( tri1 == tri )
      return 1;
    if ( tri2 == tri )
      return 2;
    return -1;
  }
  
  public Tri getAdjacent(int i) {
    switch(i) {
    case 0: return tri0;
    case 1: return tri1;
    case 2: return tri2;
    }
    Assert.shouldNeverReachHere();
    return null;
  }

  public boolean hasAdjacent(int i) {
    return null != getAdjacent(i);
  }

  public boolean isAdjacent(Tri tri) {
    return getIndex(tri) >= 0;
  }

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

  public static int next(int i) {
    switch (i) {
    case 0: return 1;
    case 1: return 2;
    case 2: return 0;
    }
    return -1;
  }

  public static int prev(int i) {
    switch (i) {
    case 0: return 2;
    case 1: return 0;
    case 2: return 1;
    }
    return -1;
  }

  public static int oppVertex(int edgeIndex) {
    return prev(edgeIndex);
  }

  public static int oppEdge(int vertexIndex) {
    return next(vertexIndex);
  }


  public Coordinate midpoint(int edgeIndex) {
    Coordinate p0 = getCoordinate(edgeIndex);
    Coordinate p1 = getCoordinate(next(edgeIndex));
    double midX = (p0.getX() + p1.getX()) / 2;
    double midY = (p0.getY() + p1.getY()) / 2;
    return new Coordinate(midX, midY);
  }
  
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
