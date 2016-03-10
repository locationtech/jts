

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
package org.locationtech.jts.operation.overlay;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geomgraph.DirectedEdge;
import org.locationtech.jts.geomgraph.DirectedEdgeStar;
import org.locationtech.jts.geomgraph.EdgeRing;
import org.locationtech.jts.geomgraph.Node;

/**
 * A ring of {@link DirectedEdge}s which may contain nodes of degree &gt; 2.
 * A <tt>MaximalEdgeRing</tt> may represent two different spatial entities:
 * <ul>
 * <li>a single polygon possibly containing inversions (if the ring is oriented CW)
 * <li>a single hole possibly containing exversions (if the ring is oriented CCW)
 * </ul>
 * If the MaximalEdgeRing represents a polygon,
 * the interior of the polygon is strongly connected.
 * <p>
 * These are the form of rings used to define polygons under some spatial data models.
 * However, under the OGC SFS model, {@link MinimalEdgeRing}s are required.
 * A MaximalEdgeRing can be converted to a list of MinimalEdgeRings using the
 * {@link #buildMinimalRings() } method.
 *
 * @version 1.7
 * @see org.locationtech.jts.operation.overlay.MinimalEdgeRing
 */
public class MaximalEdgeRing
  extends EdgeRing
{

  public MaximalEdgeRing(DirectedEdge start, GeometryFactory geometryFactory) {
    super(start, geometryFactory);
  }

  public DirectedEdge getNext(DirectedEdge de)
  {
    return de.getNext();
  }
  public void setEdgeRing(DirectedEdge de, EdgeRing er)
  {
    de.setEdgeRing(er);
  }

  /**
   * For all nodes in this EdgeRing,
   * link the DirectedEdges at the node to form minimalEdgeRings
   */
  public void linkDirectedEdgesForMinimalEdgeRings()
  {
    DirectedEdge de = startDe;
    do {
      Node node = de.getNode();
      ((DirectedEdgeStar) node.getEdges()).linkMinimalDirectedEdges(this);
      de = de.getNext();
    } while (de != startDe);
  }

  public List buildMinimalRings()
  {
    List minEdgeRings = new ArrayList();
    DirectedEdge de = startDe;
    do {
      if (de.getMinEdgeRing() == null) {
        EdgeRing minEr = new MinimalEdgeRing(de, geometryFactory);
        minEdgeRings.add(minEr);
      }
      de = de.getNext();
    } while (de != startDe);
    return minEdgeRings;
  }

}
