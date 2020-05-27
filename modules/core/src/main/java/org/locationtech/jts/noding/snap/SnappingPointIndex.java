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
package org.locationtech.jts.noding.snap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;

public class SnappingPointIndex {

  private double snapTolerance;

  private KdTree snapPointIndex;
  
  SnappingPointIndex(double snapTolerance) {
    this.snapTolerance = snapTolerance;
    snapPointIndex = new KdTree(snapTolerance);
  }
  
  public Coordinate snap(Coordinate p) {
    /**
     * Inserting the coordinate snaps it to any existing
     * one within tolerance, or adds it.
     */
    KdNode node = snapPointIndex.insert(p);
    return node.getCoordinate();
  }

  public double getTolerance() {
    return snapTolerance;
  }

}
