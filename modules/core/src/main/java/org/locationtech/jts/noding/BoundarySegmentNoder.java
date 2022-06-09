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
package org.locationtech.jts.noding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * A noder which extracts boundary line segments 
 * as {@link SegmentString}s.
 * Boundary segments are those which are not duplicated in the input.
 * It is appropriate for use with valid polygonal coverages.
 * <p>
 * No precision reduction is carried out. 
 * If that is required, another noder must be used (such as a snap-rounding noder),
 * or the input must be precision-reduced beforehand.
 * 
 * @author Martin Davis
 *
 */
public class BoundarySegmentNoder implements Noder {

  private List<SegmentString> segList;
  
  /**
   * Creates a new segment-dissolving noder.
   */
  public BoundarySegmentNoder() {
    
  }

  @Override
  public void computeNodes(Collection segStrings) {
    HashSet<Segment> segSet = new HashSet<Segment>() ;
    addSegments(segStrings, segSet);
    segList = extractSegments(segSet);
  }

  private static void addSegments(Collection<SegmentString> segStrings, HashSet<Segment> segSet) {
    for (SegmentString ss : segStrings) {
      addSegments( ss, segSet );
    }
  }
  
  private static void addSegments(SegmentString segString, HashSet<Segment> segSet) {
    for (int i = 0; i < segString.size() - 1; i++) {
      Coordinate p0 = segString.getCoordinate(i);
      Coordinate p1 = segString.getCoordinate(i + 1);
      Segment seg = new Segment(p0, p1, segString, i);
      if (segSet.contains(seg)) {
        segSet.remove(seg);
      }
      else {
        segSet.add(seg);
      }
    }
  }
  
  private static List<SegmentString> extractSegments(HashSet<Segment> segSet) {
    List<SegmentString> segList = new ArrayList<SegmentString>();
    for (Segment seg : segSet) {
      SegmentString ss = seg.getSegmentString();
      int i = seg.getIndex();
      Coordinate p0 = ss.getCoordinate(i);
      Coordinate p1 = ss.getCoordinate(i + 1);
      SegmentString segStr = new BasicSegmentString(new Coordinate[] { p0, p1 }, ss.getData());
      segList.add(segStr);
    }
    return segList;
  }

  @Override
  public Collection getNodedSubstrings() {
    return segList;
  }

  static class Segment extends LineSegment {
    private SegmentString segStr;
    private int index;

    public Segment(Coordinate p0, Coordinate p1, 
        SegmentString segStr, int index) {
      super(p0, p1);
      this.segStr = segStr;
      this.index = index;
      normalize();
    }
    
    public SegmentString getSegmentString() {
      return segStr;
    }
    
    public int getIndex() {
      return index;
    }
  }
}
