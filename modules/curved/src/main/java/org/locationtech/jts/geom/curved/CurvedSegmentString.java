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
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentNodeList;

/**
 * A curved {@link NodedSegmentString} that nodes through the core
 * {@code org.locationtech.jts.noding} framework (N-SS, JTS #1195).
 * <p>
 * Its core coordinate array is the arc <b>chord endpoints</b> (one per arc
 * boundary), so the core noder sees segment {@code i} as arc {@code i}; the arc
 * mid control points are kept alongside so that splitting reconstructs sub-arcs
 * on the original circle. Nodes are recorded by a {@link CurvedSegmentIntersector}
 * (computing true arc intersections) via the inherited
 * {@code addIntersection(Coordinate,int)}, and {@link #createNodeList()} supplies a
 * {@link CurvedSegmentNodeList} so {@code getNodedSubstrings()} yields arc-preserving
 * {@code CurvedSegmentString}s — all driven by the stock core {@code SimpleNoder}.
 */
public final class CurvedSegmentString extends NodedSegmentString {

  private double[] mids;   // mids[2i],mids[2i+1] = mid control point of arc i

  public CurvedSegmentString(CoordinateSequence controlPoints, Object data) {
    super(chordEndpoints(controlPoints), data);
    this.mids = extractMids(controlPoints);
  }

  CurvedSegmentString(Coordinate[] chordEndpoints, double[] mids, Object data) {
    super(chordEndpoints, data);
    this.mids = mids;
  }

  @Override
  protected SegmentNodeList createNodeList() {
    return new CurvedSegmentNodeList(this);
  }

  /** Number of arc pieces (= number of core segments). */
  public int numArcs() { return size() - 1; }

  /** The {@code i}-th arc piece as {sx,sy,mx,my,ex,ey}. */
  double[] arc(int i) {
    Coordinate s = getCoordinate(i), e = getCoordinate(i + 1);
    return new double[]{ s.x, s.y, mids[2*i], mids[2*i+1], e.x, e.y };
  }

  private static Coordinate[] chordEndpoints(CoordinateSequence s) {
    int k = (s.size() - 1) / 2;
    Coordinate[] out = new Coordinate[k + 1];
    for (int i = 0; i <= k; i++) out[i] = new Coordinate(s.getX(2*i), s.getY(2*i));
    return out;
  }

  private static double[] extractMids(CoordinateSequence s) {
    int k = (s.size() - 1) / 2;
    double[] m = new double[2 * k];
    for (int i = 0; i < k; i++) { m[2*i] = s.getX(2*i + 1); m[2*i + 1] = s.getY(2*i + 1); }
    return m;
  }
}
