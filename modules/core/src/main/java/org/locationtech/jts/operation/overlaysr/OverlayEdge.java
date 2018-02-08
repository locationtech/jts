package org.locationtech.jts.operation.overlaysr;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.SegmentString;

public class OverlayEdge extends HalfEdge {

  private SegmentString edge;
  private boolean direction;
  private Coordinate dirPt;

  public OverlayEdge(Coordinate orig, Coordinate dirPt, boolean direction, SegmentString segString) {
    super(orig);
    this.dirPt = dirPt;
    this.direction = direction;
    this.edge = segString;
  }

  public Coordinate directionPt() {
    return dirPt;
  }
}
