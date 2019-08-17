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

import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Location;

/**
 * Records information about the origin of 
 * geometry edge linework.
 * This information is preserved through noding
 * and used to construct the overlay topology graph labels.
 * 
 * @author mdavis
 *
 */
public class EdgeInfo {
  private int index;
  private int dim = -1;
  private boolean isHole = false;
  private int depthDelta = 0;
  
  public EdgeInfo(int index, int depthDelta, boolean isHole) {
    this.index = index;
    this.dim = Dimension.A;
    this.depthDelta = depthDelta;
    this.isHole = isHole;
  }
  
  public EdgeInfo(int index) {
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
}
