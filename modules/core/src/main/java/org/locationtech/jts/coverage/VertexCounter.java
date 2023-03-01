/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.Geometry;

class VertexCounter implements CoordinateSequenceFilter {

  public static Map<Coordinate, Integer> count(Geometry[] geoms) {
    Map<Coordinate, Integer> vertexCount = new HashMap<Coordinate, Integer>();
    VertexCounter counter = new VertexCounter(vertexCount);
    for (Geometry geom : geoms) {
      geom.apply(counter);
    }
    return vertexCount;
  }

  private Map<Coordinate, Integer> vertexCount;
  
  public VertexCounter(Map<Coordinate, Integer> vertexCount) {
    this.vertexCount = vertexCount;
  }

  @Override
  public void filter(CoordinateSequence seq, int i) {
    //-- for rings don't double-count duplicate endpoint
    if (CoordinateSequences.isRing(seq) && i == 0)
      return;
    Coordinate v = seq.getCoordinate(i);
    int count = vertexCount.containsKey(v) ? vertexCount.get(v) : 0;
    count++;
    vertexCount.put(v, count);
  }

  @Override
  public boolean isDone() {
    return false;
  }

  @Override
  public boolean isGeometryChanged() {
    return false;
  }

}
