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

import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;

class CoverageBoundarySegmentFinder implements CoordinateSequenceFilter {
  
  public static Set<LineSegment> findBoundarySegments(Geometry[] geoms) {
    Set<LineSegment> segs = new HashSet<LineSegment>();
    CoverageBoundarySegmentFinder finder = new CoverageBoundarySegmentFinder(segs);
    for (Geometry geom : geoms) {
      geom.apply(finder);
    }
    return segs;
  }

  public static boolean isBoundarySegment(Set<LineSegment> boundarySegs, CoordinateSequence seq, int i) {
    LineSegment seg = createSegment(seq, i);
    return boundarySegs.contains(seg);
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

  private static LineSegment createSegment(CoordinateSequence seq, int i) {
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
