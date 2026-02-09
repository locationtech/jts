/*
 * Copyright (c) 2026 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequences;

/**
 * 
 * Location indexes are always the index of a sequence segment.  
 * This means they are always less than the number of vertices
 * in the sequence. The endpoint in a sequence 
 * has the index of the final segment in the sequence.
 * if the sequence is a ring, the imdex of the final endpoint is
 * normalized to 0.
 * 
 * @author mdavis
 *
 */
public class FacetLocation {
  private CoordinateSequence seq;
  private int index;
  private Coordinate pt;
  
  public FacetLocation(CoordinateSequence seq, int index, Coordinate pt) {
    this.seq = seq;
    this.pt = pt;
    this.index = index;
    if (index >= seq.size())
      this.index = seq.size() - 1;
  }

  public boolean isSameSegment(FacetLocation f) {
    if (seq != f.seq)
      return false;
    if (index == f.index)
      return true;
    //-- check for end pt same as start point of next segment
    if (isNext(index, f.index)) {
      Coordinate endPt = seq.getCoordinate(index + 1);
      return f.pt.equals2D(endPt);
    }
    if (isNext(f.index, index)) {
      Coordinate endPt = f.seq.getCoordinate(index + 1);
      return pt.equals2D(endPt);
    }
    return false;
  }

  private boolean isNext(int index, int index1) {
    if (index1 == index + 1)
      return true;
    if (index1 == 0 && CoordinateSequences.isRing(seq)
        && index1 == seq.size() - 1) {
      return true;
    }
    return false;
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
