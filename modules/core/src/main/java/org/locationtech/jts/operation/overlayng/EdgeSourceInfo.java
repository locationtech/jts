/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Dimension;

/**
 * Records topological information about an 
 * edge representing a piece of linework (lineString or polygon ring)
 * from a single source geometry.
 * This information is carried through the noding process
 * (which may result in many noded edges sharing the same information object).
 * It is then used to populate the topology info fields
 * in {@link Edge}s (possibly via merging).
 * That information is used to construct the topology graph {@link OverlayLabel}s.
 * 
 * @author mdavis
 *
 */
class EdgeSourceInfo {
  private int index;
  private int dim = -999;
  private boolean isHole = false;
  private int depthDelta = 0;
  
  public EdgeSourceInfo(int index, int depthDelta, boolean isHole) {
    this.index = index;
    this.dim = Dimension.A;
    this.depthDelta = depthDelta;
    this.isHole = isHole;
  }
  
  public EdgeSourceInfo(int index) {
    this.index = index;
    this.dim = Dimension.L;
  }
  
  public int getIndex() {
    return index;
  }
  
  public int getDimension() {
    return dim;
  }
  public int getDepthDelta() {
    return depthDelta;
  }
  
  public boolean isHole() {
    return isHole;
  }
  
  public String toString() {
    return Edge.infoString(index, dim, isHole, depthDelta);
  }
}
