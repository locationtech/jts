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
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * A noder which extracts chains of boundary segments 
 * as {@link SegmentString}s from a polygonal coverage.
 * Boundary segments are those which are not duplicated in the input polygonal coverage.
 * Extracting chains of segments minimize the number of segment strings created,
 * which produces a more efficient topological graph structure.
 * <p>
 * This enables fast overlay of polygonal coverages in {@link CoverageUnion}.
 * Using this noder is faster than {@link SegmentExtractingNoder}
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
    HashSet<Segment> boundarySegSet = new HashSet<Segment>();
    BoundaryChainMap[] boundaryChains = new BoundaryChainMap[segStrings.size()];
    addSegments(segStrings, boundarySegSet, boundaryChains);
    markBoundarySegments(boundarySegSet);
    chainList = extractChains(boundaryChains);
    
    //-- check for self-touching nodes and split chains at those nodes
    Set<Coordinate> nodePts = findNodePts(chainList); 
    if (nodePts.size() > 0) {
      chainList = nodeChains(chainList, nodePts);
    }
  }

  private static void addSegments(Collection<SegmentString> segStrings, HashSet<Segment> segSet, 
      BoundaryChainMap[] boundaryChains) {
    int i = 0;
    for (SegmentString ss : segStrings) {
      BoundaryChainMap chainMap = new BoundaryChainMap(ss);
      boundaryChains[i++] = chainMap;
      addSegments( ss, chainMap, segSet );
    }
  }
  
  private static void addSegments(SegmentString segString, BoundaryChainMap chainMap, HashSet<Segment> segSet) {
    for (int i = 0; i < segString.size() - 1; i++) {
      Coordinate p0 = segString.getCoordinate(i);
      Coordinate p1 = segString.getCoordinate(i + 1);
      Segment seg = new Segment(p0, p1, chainMap, i);
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
      seg.markBoundary();
    }
  }

  private static List<SegmentString> extractChains(BoundaryChainMap[] boundaryChains) {
    List<SegmentString> chainList = new ArrayList<SegmentString>();
    for (BoundaryChainMap chainMap : boundaryChains) {
      chainMap.createChains(chainList);
    }
    return chainList;
  }

  private Set<Coordinate> findNodePts(List<SegmentString> segStrings) {
    Set<Coordinate> interorVertices = new HashSet<Coordinate>();
    Set<Coordinate> nodes = new HashSet<Coordinate>();
    for (SegmentString ss : segStrings) {
      //-- endpoints are nodes
      nodes.add(ss.getCoordinate(0));
      nodes.add(ss.getCoordinate(ss.size() - 1));
      
      //-- check for duplicate interior points
      for (int i = 1; i < ss.size() - 1; i++) {
        Coordinate p = ss.getCoordinate(i);
        if (interorVertices.contains(p)) {
          nodes.add(p);
        }
        interorVertices.add(p);
      }
    }
    return nodes;
  }
  
  private List<SegmentString> nodeChains(List<SegmentString> chains, Set<Coordinate> nodePts) {
    List<SegmentString> nodedChains = new ArrayList<SegmentString>();
    for (SegmentString chain : chains) {
      nodeChain(chain, nodePts, nodedChains);
    }
    return nodedChains;
  }
  
  private void nodeChain(SegmentString chain, Set<Coordinate> nodePts, List<SegmentString> nodedChains) {
    int start = 0;
    while (start < chain.size() - 1) {
      int end = findNodeIndex(chain, start, nodePts);
      //-- if no interior nodes found, keep original chain
      if (start == 0 && end == chain.size() - 1) {
        nodedChains.add(chain);
        return;
      }
      nodedChains.add(BasicSegmentString.substring(chain, start, end));
      start = end;
    }
  }

  private int findNodeIndex(SegmentString chain, int start, Set<Coordinate> nodePts) {
    for (int i = start + 1; i < chain.size(); i++) {
      if (nodePts.contains(chain.getCoordinate(i)))
        return i;
    }
    return chain.size() - 1;
  }

  @Override
  public Collection getNodedSubstrings() {
    return chainList;
  }

  private static class BoundaryChainMap {
    private SegmentString segString;
    private boolean[] isBoundary;
    
    public BoundaryChainMap(SegmentString ss) {
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
    private BoundaryChainMap segMap;
    private int index;

    public Segment(Coordinate p0, Coordinate p1, 
        BoundaryChainMap segMap, int index) {
      super(p0, p1);
      this.segMap = segMap;
      this.index = index;
      normalize();
    }
    
    public void markBoundary() {
      segMap.setBoundarySegment(index);
    }
  }
}
