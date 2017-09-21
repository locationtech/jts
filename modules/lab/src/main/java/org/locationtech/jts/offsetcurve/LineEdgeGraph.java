/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.offsetcurve;

import java.util.Collection;

import org.locationtech.jts.edgegraph.EdgeGraph;
import org.locationtech.jts.edgegraph.HalfEdge;
import org.locationtech.jts.edgegraph.MarkHalfEdge;
import org.locationtech.jts.geom.Coordinate;


/**
 * A graph containing {@link LineHalfEdge}s.
 * 
 * @author Martin Davis
 *
 */
class LineEdgeGraph extends EdgeGraph
{
  
  protected HalfEdge createEdge(Coordinate p0)
  {
    return new MarkHalfEdge(p0);
  }
  
  //TODO: Add findEdgeOrig and findEdgeDest methods to EdgeGraph
  public HalfEdge findEdgeOrig(Coordinate p) {
    Collection<HalfEdge> edges = this.getVertexEdges();
    for (HalfEdge e : edges) {
      if (e.orig().equals2D(p)) return e;
    }
    return null;
  }
  
  public HalfEdge findEdgeDest(Coordinate p) {
    Collection<HalfEdge> edges = this.getVertexEdges();
    for (HalfEdge e : edges) {
      if (e.dest().equals2D(p)) return e;
    }
    return null;
  }
  

}
