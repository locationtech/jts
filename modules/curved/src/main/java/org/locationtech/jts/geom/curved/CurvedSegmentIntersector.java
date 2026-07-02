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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.NodableSegmentString;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentString;

/**
 * A core {@link SegmentIntersector} that computes true circular-arc intersections
 * (N-SS, JTS #1195). When the core noder offers segment {@code segIndex0} of one
 * {@link CurvedSegmentString} against segment {@code segIndex1} of another, this
 * intersects the corresponding arc pieces with the oracle-pinned
 * {@link CircularArcs} primitives (via {@link ArcSegmentString#intersectPieces})
 * and records each crossing through the generic
 * {@code NodableSegmentString.addIntersection(Coordinate,int)} — no
 * {@code LineIntersector} and no linear assumption. Drive it with the stock core
 * {@code SimpleNoder} (which offers every segment pair, so the arc-bulge envelope
 * problem that makes {@code MCIndexNoder} unsafe does not arise).
 */
public final class CurvedSegmentIntersector implements SegmentIntersector {

  public void processIntersections(SegmentString e0, int segIndex0, SegmentString e1, int segIndex1) {
    if (e0 == e1 && segIndex0 == segIndex1) return;
    if (!(e0 instanceof CurvedSegmentString) || !(e1 instanceof CurvedSegmentString)) return;
    double[] a = ((CurvedSegmentString) e0).arc(segIndex0);
    double[] b = ((CurvedSegmentString) e1).arc(segIndex1);
    for (double[] pt : ArcSegmentString.intersectPieces(a, b)) {
      ((NodableSegmentString) e0).addIntersection(new Coordinate(pt[0], pt[1]), segIndex0);
      ((NodableSegmentString) e1).addIntersection(new Coordinate(pt[0], pt[1]), segIndex1);
    }
  }

  public boolean isDone() { return false; }
}
