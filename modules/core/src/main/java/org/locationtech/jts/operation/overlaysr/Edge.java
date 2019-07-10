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
package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.geom.Coordinate;

/**
 * Represents a single edge in a topology graph,
 * carrying the location label derived from the parent geometr(ies).
 * 
 * @author mdavis
 *
 */
public class Edge {
  private Coordinate[] pts;
  private OverlayLabel label;

  public Edge(Coordinate[] pts, OverlayLabel lbl) {
    this.pts = pts;
    this.label = lbl;
  }
  
  public Coordinate[] getCoordinates() {
    return pts;
  }
  
  public OverlayLabel getLabel() {
    return label;
  }

  public Coordinate getCoordinate(int index) {
    return pts[index];
  }
  
  public int size() {
    return pts.length;
  }
}
