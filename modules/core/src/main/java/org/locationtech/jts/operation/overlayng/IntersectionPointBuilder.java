/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Extracts Point resultants from an overlay graph
 * created by an Intersection operation
 * between non-Point inputs.
 * Points may be created during intersection
 * if lines or areas touch one another at single points.
 * Intersection is the only overlay operation which can 
 * result in Points from non-Point inputs.
 * <p>
 * Overlay operations where one or more inputs 
 * are Points are handled via a different code path.
 * 
 * 
 * @author Martin Davis
 * 
 * @see OverlayPoints
 *
 */
class IntersectionPointBuilder {

  private GeometryFactory geometryFactory;
  private OverlayGraph graph;
  private List<Point> points = new ArrayList<Point>();
  
  public IntersectionPointBuilder(OverlayGraph graph,
      GeometryFactory geomFact) {
    this.graph = graph;
    this.geometryFactory = geomFact;
  }

  public List<Point> getPoints() {
    addResultPoints();
    return points;
  }

  private void addResultPoints() {
    for (OverlayEdge nodeEdge : graph.getNodeEdges()) {
      if (isResultPoint(nodeEdge)) {
        Point pt = geometryFactory.createPoint(nodeEdge.getCoordinate().copy());
        points.add(pt);
      }
    }
  }

  /**
   * Tests if a node is a result point.
   * This is the case if the node is incident on edges from both
   * inputs, and none of the edges are themselves in the result.
   * 
   * @param nodeEdge an edge originating at the node
   * @return true if this node is a result point
   */
  private boolean isResultPoint(OverlayEdge nodeEdge) {
    boolean isEdgeOfA = false;
    boolean isEdgeOfB = false;
    
    OverlayEdge edge = nodeEdge;
    do {
      if (edge.isInResult()) return false;
      OverlayLabel label = edge.getLabel();
      isEdgeOfA |= isEdgeOf(label, 0);
      isEdgeOfB |= isEdgeOf(label, 1);
      edge = (OverlayEdge) edge.oNext();
    } while (edge != nodeEdge);
    boolean isNodeInBoth = isEdgeOfA && isEdgeOfB;
    return isNodeInBoth;
  }

  private boolean isEdgeOf(OverlayLabel label, int i) {
    return label.isBoundary(i) || label.isLine(i);
  }

}
