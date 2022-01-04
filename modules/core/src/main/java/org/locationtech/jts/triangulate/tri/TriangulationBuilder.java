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

import java.util.HashMap;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

/**
 * Builds a triangulation from a set of {@link Tri}s
 * by populating the links to adjacent triangles.
 * 
 * @author mdavis
 *
 */
public class TriangulationBuilder {

  /**
   * Computes the triangulation of a set of {@link Tri}s.
   * 
   * @param triList the list of Tris
   */
  public static void build(List<? extends Tri> triList) {
    new TriangulationBuilder(triList);
  }
  
  private HashMap<TriEdge, Tri> triMap;

  /**
   * Computes the triangulation of a set of {@link Tri}s.
   * 
   * @param triList the list of Tris
   */
  private TriangulationBuilder(List<? extends Tri> triList) {
    triMap = new HashMap<TriEdge, Tri>();
    for (Tri tri : triList) {
      add(tri);
    }
  }

  private Tri find(Coordinate p0, Coordinate p1) {
    TriEdge e = new TriEdge(p0, p1);
    return triMap.get(e);
  }
  
  private void add(Tri tri) {
    Coordinate p0 = tri.getCoordinate(0);
    Coordinate p1 = tri.getCoordinate(1);
    Coordinate p2 = tri.getCoordinate(2);
    
    // get adjacent triangles, if any
    Tri n0 = find(p0, p1);
    Tri n1 = find(p1, p2);
    Tri n2 = find(p2, p0);
    
    tri.setAdjacent(n0, n1, n2);
    addAdjacent(tri, n0, p0, p1);
    addAdjacent(tri, n1, p1, p2);
    addAdjacent(tri, n2, p2, p0);
  }
  
  private void addAdjacent(Tri tri, Tri adj, Coordinate p0, Coordinate p1) {
    /**
     * If adjacent is null, this tri is first one to be recorded for edge
     */
    if (adj == null) {
      triMap.put(new TriEdge(p0, p1), tri);
      return;
    }
    adj.setAdjacent(p1, tri);
  }
}
