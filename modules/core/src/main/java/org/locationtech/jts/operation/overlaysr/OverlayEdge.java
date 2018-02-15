package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.topology.Label;

public class OverlayEdge extends HalfEdge {

  private SegmentString edge;
  
  /**
   * true indicates direction is forward along segString
   */
  private boolean direction;
  private Coordinate dirPt;
  private Label label;

  public OverlayEdge(Coordinate orig, Coordinate dirPt, boolean direction, SegmentString segString) {
    super(orig);
    this.dirPt = dirPt;
    this.direction = direction;
    this.edge = segString;
    this.label = (Label) segString.getData();
  }

  public Coordinate directionPt() {
    return dirPt;
  }
}
