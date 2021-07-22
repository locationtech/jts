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

import java.util.HashMap;

import org.locationtech.jts.geom.Coordinate;

public class Triangulation {

  private HashMap<TriEdge, Tri> triMap;

  public Triangulation() {
    triMap = new HashMap<TriEdge, Tri>();
  }

  public Tri find(Coordinate p0, Coordinate p1) {
    TriEdge e = new TriEdge(p0, p1);
    return triMap.get(e);
  }

  /**
   * Add triangle represented by coords to TriMap and update its neighbors
   * 
   * @param pts
   * @return
   */
  public Tri add(Coordinate[] pts) {
    Tri tri = new Tri(pts[0], pts[1], pts[2]);
    // get adjacent triangles, if any
    Tri n0 = find(pts[0], pts[1]);
    Tri n1 = find(pts[1], pts[2]);
    Tri n2 = find(pts[2], pts[0]);
    
    tri.setAdjacent(n0, n1, n2);
    addAdjacent(n0, tri, pts[0], pts[1]);
    addAdjacent(n1, tri, pts[1], pts[2]);
    addAdjacent(n2, tri, pts[2], pts[0]);

    return tri;
  }
  
  private void addAdjacent(Tri adj, Tri tri, Coordinate p0, Coordinate p1) {
    /**
     * If adjacent is null, this tri is first one to be recorded in map
     */
    if (adj == null) {
      triMap.put(new TriEdge(p0, p1), tri);
      return;
    }
    adj.setAdjacent(p1, tri);
  }
}
