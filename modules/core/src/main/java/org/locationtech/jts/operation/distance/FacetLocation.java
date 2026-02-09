package org.locationtech.jts.operation.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequences;

public class FacetLocation {
  private CoordinateSequence seq;
  private int index;
  private Coordinate pt;
  
  public FacetLocation(CoordinateSequence seq, int index, Coordinate pt) {
    this.seq = seq;
    this.index = index;
    this.pt = pt;
  }

  public boolean isSameSegment(FacetLocation f) {
    return seq == f.seq && index == f.index;
  }

  public int getIndex() {
    return index;
  }

  public Coordinate getEndPoint(int i) {
    if (i == 0)
      return seq.getCoordinate(index);
    else 
      return seq.getCoordinate(index + 1);
  }
  
  public int normalize(int index) {
    if (index >= seq.size() - 1
        && CoordinateSequences.isRing(seq)) {
      return 0;
    }
    return index;
  }
}
