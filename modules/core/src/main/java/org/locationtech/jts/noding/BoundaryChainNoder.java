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
 * A noder which extracts chains of boundary segments 
 * as {@link SegmentString}s.
 * Boundary segments are those which are not duplicated in the input.
 * The segment strings are extracted in a way that maximises their length,
 * and minimizes the total number of edges.
 * This produces the most efficient topological graph structure.
 * <p>
 * Segments which are not on the boundary are those which 
 * have an identical segment in another polygon ring.
 * <p>
 * This enables fast overlay of polygonal coverages in {@link CoverageUnion}.
 * This noder is faster than {@link SegmentExtractingNoder}
 * and {@link BoundarySegmentNoder}.
 * <p>
 * No precision reduction is carried out. 
 * If that is required, another noder must be used (such as a snap-rounding noder),
 * or the input must be precision-reduced beforehand.
 * 
 * @author Martin Davis
 *
 */
public class BoundaryChainNoder implements Noder {

  private List<SegmentString> chainList;
  
  /**
   * Creates a new boundary-extracting noder.
   */
  public BoundaryChainNoder() {
    
  }

  @Override
  public void computeNodes(Collection segStrings) {
    HashSet<Segment> segSet = new HashSet<Segment>();
    BoundarySegmentMap[] bdySections = new BoundarySegmentMap[segStrings.size()];
    addSegments(segStrings, segSet, bdySections);
    markBoundarySegments(segSet);
    chainList = extractChains(bdySections);
  }

  private static void addSegments(Collection<SegmentString> segStrings, HashSet<Segment> segSet, 
      BoundarySegmentMap[] includedSegs) {
    int i = 0;
    for (SegmentString ss : segStrings) {
      BoundarySegmentMap segInclude = new BoundarySegmentMap(ss);
      includedSegs[i++] = segInclude;
      addSegments( ss, segInclude, segSet );
    }
  }
  
  private static void addSegments(SegmentString segString, BoundarySegmentMap segInclude, HashSet<Segment> segSet) {
    for (int i = 0; i < segString.size() - 1; i++) {
      Coordinate p0 = segString.getCoordinate(i);
      Coordinate p1 = segString.getCoordinate(i + 1);
      Segment seg = new Segment(p0, p1, segInclude, i);
      if (segSet.contains(seg)) {
        segSet.remove(seg);
      }
      else {
        segSet.add(seg);
      }
    }
  }
  
  private static void markBoundarySegments(HashSet<Segment> segSet) {
    for (Segment seg : segSet) {
      seg.markInBoundary();
    }
  }

  private static List<SegmentString> extractChains(BoundarySegmentMap[] sections) {
    List<SegmentString> sectionList = new ArrayList<SegmentString>();
    for (BoundarySegmentMap sect : sections) {
      sect.createChains(sectionList);
    }
    return sectionList;
  }

  @Override
  public Collection getNodedSubstrings() {
    return chainList;
  }

  private static class BoundarySegmentMap {
    private SegmentString segString;
    private boolean[] isBoundary;
    
    public BoundarySegmentMap(SegmentString ss) {
      this.segString = ss;
      isBoundary = new boolean[ss.size() - 1];
    }
    
    public void setBoundarySegment(int index) {
      isBoundary[index] = true;
    }
    
    public void createChains(List<SegmentString> chainList) {
      int endIndex = 0;
      while (true) {
        int startIndex = findChainStart(endIndex); 
        if (startIndex >= segString.size() - 1)
          break;
        endIndex = findChainEnd(startIndex);
        SegmentString ss = createChain(segString, startIndex, endIndex);
        chainList.add(ss);
      }
    }

    private static SegmentString createChain(SegmentString segString, int startIndex, int endIndex) {
      Coordinate[] pts = new Coordinate[endIndex - startIndex + 1];
      int ipts = 0;
      for (int i = startIndex; i < endIndex + 1; i++) {
        pts[ipts++] = segString.getCoordinate(i).copy();
      }
      return new BasicSegmentString(pts, segString.getData());
    }

    private int findChainStart(int index) {
      while (index < isBoundary.length && ! isBoundary[index]) {
        index++;
      }
      return index;
    }

    private int findChainEnd(int index) {
      index++;
      while (index < isBoundary.length && isBoundary[index]) {
        index++;
      }
      return index;
    }
  }
  
  private static class Segment extends LineSegment {
    private BoundarySegmentMap segMap;
    private int index;

    public Segment(Coordinate p0, Coordinate p1, 
        BoundarySegmentMap segMap, int index) {
      super(p0, p1);
      this.segMap = segMap;
      this.index = index;
      normalize();
    }
    
    public void markInBoundary() {
      segMap.setBoundarySegment(index);
    }
  }
}
