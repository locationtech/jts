/*
 * Copyright (c) 2026 grootstebozewolf
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom.curved;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentNode;
import org.locationtech.jts.noding.SegmentNodeList;
import org.locationtech.jts.noding.SegmentString;

/**
 * The {@link SegmentNodeList} for a {@link CurvedSegmentString}: it builds
 * arc-preserving split edges (N-SS, JTS #1195). It reuses the core node
 * bookkeeping and endpoint/collapse handling, overriding only
 * {@link #createSplitEdge} so each split substring is a {@code CurvedSegmentString}
 * whose sub-arcs are recomputed on the original circle (mid at the sub-arc's
 * angular midpoint) via {@link ArcSegmentString#midOnArc}.
 */
final class CurvedSegmentNodeList extends SegmentNodeList {

  CurvedSegmentNodeList(CurvedSegmentString edge) {
    super(edge);
  }

  @Override
  protected SegmentString createSplitEdge(SegmentNode ei0, SegmentNode ei1) {
    Coordinate[] chord = createSplitEdgePts(ei0, ei1);     // chord-point slice (now protected in core)
    CurvedSegmentString edge = (CurvedSegmentString) getEdge();
    int seg0 = ei0.segmentIndex;                            // first arc this slice covers
    List<Coordinate> ctrl = new ArrayList<Coordinate>();
    ctrl.add(chord[0]);
    for (int p = 0; p + 1 < chord.length; p++) {
      double[] a = edge.arc(seg0 + p);                      // the arc piece this chord pair lies on
      double[] mid = ArcSegmentString.midOnArc(a, chord[p].x, chord[p].y, chord[p+1].x, chord[p+1].y);
      ctrl.add(new Coordinate(mid[0], mid[1]));
      ctrl.add(chord[p + 1]);
    }
    return new CurvedSegmentString(
        new CoordinateArraySequence(ctrl.toArray(new Coordinate[0])), edge.getData());
  }
}
