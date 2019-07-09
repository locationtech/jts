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
