package org.locationtech.jts.coverage;

import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;

public class CoverageBoundarySegmentFinder implements CoordinateSequenceFilter {
  
  public static Set<LineSegment> findBoundarySegments(Geometry[] geoms) {
    Set<LineSegment> segs = new HashSet<LineSegment>();
    CoverageBoundarySegmentFinder finder = new CoverageBoundarySegmentFinder(segs);
    for (Geometry geom : geoms) {
      geom.apply(finder);
    }
    return segs;
  }

  private Set<LineSegment> boundarySegs;

  public CoverageBoundarySegmentFinder(Set<LineSegment> segs) {
    this.boundarySegs = segs;
  }

  @Override
  public void filter(CoordinateSequence seq, int i) {
    //-- final point does not start a segment
    if (i >= seq.size() - 1)
      return;
    LineSegment seg = createSegment(seq, i);
    if (boundarySegs.contains(seg)) {
      boundarySegs.remove(seg);
    }
    else {
      boundarySegs.add(seg);
    }
  }

  public static LineSegment createSegment(CoordinateSequence seq, int i) {
    LineSegment seg = new LineSegment(seq.getCoordinate(i), seq.getCoordinate(i + 1));
    seg.normalize();
    return seg;
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
