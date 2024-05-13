/*
 * Copyright (c) 2024 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.relateng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

/**
 * Determines the boundary points of a linear geometry,
 * using a {@link BoundaryNodeRule}.
 * 
 * @author mdavis
 *
 */
class LinearBoundary {
  
  private Map<Coordinate, Integer> vertexDegree = new HashMap<Coordinate, Integer>();
  private boolean hasBoundary;
  private BoundaryNodeRule boundaryNodeRule;
  
  public LinearBoundary(List<LineString> lines, BoundaryNodeRule bnRule) {
    //assert: dim(geom) == 1
    this.boundaryNodeRule = bnRule;
    vertexDegree = computeBoundaryPoints(lines);
    hasBoundary = checkBoundary(vertexDegree);
  }

  private boolean checkBoundary(Map<Coordinate, Integer> vertexDegree) {
    for (int degree : vertexDegree.values()) {
      if (boundaryNodeRule.isInBoundary(degree)) {
        return true;
      }
    }
    return false;
  }

  public boolean isBoundary(Coordinate pt) {
    if (! vertexDegree.containsKey(pt))
      return false;
    int degree = vertexDegree.get(pt);
    return boundaryNodeRule.isInBoundary(degree);
  }
  
  private static Map<Coordinate, Integer> computeBoundaryPoints(List<LineString> lines) {
    Map<Coordinate, Integer> vertexDegree = new HashMap<Coordinate, Integer>();
    for (LineString line : lines) {
      if (line.isEmpty())
        continue;
      addEndpoint(line.getCoordinateN(0), vertexDegree);
      addEndpoint(line.getCoordinateN(line.getNumPoints() - 1), vertexDegree);
    }
    return vertexDegree;
  }

  private static void addEndpoint(Coordinate p, Map<Coordinate, Integer> degree) {
    int dim = 0;
    if (degree.containsKey(p)) {
      dim = degree.get(p);
    }
    dim++;
    degree.put(p, dim);
  }
  
  public Set<Coordinate> getEndPoints() {
    return vertexDegree.keySet();
  }

  public boolean hasBoundary() {
    return hasBoundary;
  }

}
