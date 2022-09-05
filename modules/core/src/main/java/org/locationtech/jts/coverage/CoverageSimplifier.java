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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * Simplifies the boundaries of the polygons in a polygonal coverage
 * while preserving the original coverage topology.
 * An area-based simplification algorithm is used to provide
 * high-quality results.
 * 
 * @author Martin Davis
 *
 */
public class CoverageSimplifier {
  
  public static Geometry[] simplify(Geometry[] coverage, double tolerance) {
    CoverageSimplifier simplifier = new CoverageSimplifier(coverage);
    return simplifier.simplify(tolerance);
  }
  
  Geometry[] input;
  private Map<LineSegment, CoverageEdge> edges;
  private Map<LinearRing, List<CoverageEdge>> ringEdgesMap;
  private GeometryFactory geomFactory;
  
  public CoverageSimplifier(Geometry[] coverage) {
    input = coverage;
    geomFactory = coverage.length > 0 ? coverage[0].getFactory() : null;
    edges = new HashMap<LineSegment, CoverageEdge>();
    ringEdgesMap = new HashMap<LinearRing, List<CoverageEdge>>();
  }
  
  public Geometry[] simplify(double tolerance) {
    extractEdges();
    simplifyEdges(tolerance);
    Geometry[] result = buildPolygons();
    return result;
  }

  private void extractEdges() {
    Set<Coordinate> nodes = findNodes(input);
    Set<LineSegment> boundarySegs = CoverageBoundarySegmentFinder.findBoundarySegments(input);
    for (Geometry geom : input) {
      for (int i = 0; i < geom.getNumGeometries(); i++) {
        Polygon poly = (Polygon) geom.getGeometryN(i);
        LinearRing shell = poly.getExteriorRing();
        findBoundaryNodes(shell, boundarySegs, nodes);
        extractRingEdges(shell, nodes);
        //TODO: handle holes
      }
    }
  }

  private void findBoundaryNodes(LinearRing ring, Set<LineSegment> boundarySegs, Set<Coordinate> nodes) {
    CoordinateSequence seq = ring.getCoordinateSequence();
    boolean isBdyLast = isBoundarySegment(seq, seq.size() - 2, boundarySegs);
    boolean isBdyPrev = isBdyLast;
    for (int i = 0; i < seq.size() - 1; i++) {
      boolean isBdy = isBoundarySegment(seq, i, boundarySegs);
      if (isBdy != isBdyPrev) {
        Coordinate nodePt = seq.getCoordinate(i);
        nodes.add(nodePt);
      }
      isBdyPrev = isBdy;
    }
  }

  private boolean isBoundarySegment(CoordinateSequence seq, int i, Set<LineSegment> boundarySegs) {
    LineSegment seg = CoverageBoundarySegmentFinder.createSegment(seq, i);
    return boundarySegs.contains(seg);
  }

  private void extractRingEdges(LinearRing ring, Set<Coordinate> nodes) {
    int first = findNextNodeIndex(ring, -1, nodes);
    if (first < 0) {
      //-- ring does not contain a node
      throw new IllegalStateException("detached rings not yet handled");
    }
    List<CoverageEdge> ringEdges = new ArrayList<CoverageEdge>();
    int start = first;
    int end = start;
    do {
      end = findNextNodeIndex(ring, start, nodes);
      CoverageEdge edge = CoverageEdge.createEdge(ring, start, end);
      edges.put(edge.getKey(), edge);
      ringEdges.add(edge);
      start = end;
    } while (end != first);
    ringEdgesMap.put(ring, ringEdges);
  }

  private int findNextNodeIndex(LinearRing ring, int start, Set<Coordinate> nodes) {
    int index = start;
    boolean isScanned0 = false;
    do {
      index = next(index, ring);
      if (index == 0) {
        if (start < 0 && isScanned0) 
          return -1;
        isScanned0 = true;
      }
      Coordinate pt = ring.getCoordinateN(index);
      if (nodes.contains(pt)) {
        return index;
      }
    } while (index != start);
    return -1;
  }

  private static int next(int index, LinearRing ring) {
    index = index + 1;
    if (index >= ring.getNumPoints() - 1)
      index = 0;
    return index;
  }

  private Set<Coordinate> findNodes(Geometry[] coverage) {
    Map<Coordinate, Integer> vertexCount = VertexCounter.count(coverage);
    Set<Coordinate> nodes = new HashSet<Coordinate>();
    for (Coordinate v : vertexCount.keySet()) {
      if (vertexCount.get(v) > 2) {
        nodes.add(v);
      }
    }
    return nodes;
  }
  
  private void simplifyEdges(double tolerance) {
    //TODO: implement
  }

  private Geometry[] buildPolygons() {
    Geometry[] result = new Geometry[input.length];
    for (int i = 0; i < input.length; i++) {
      result[i] = buildResult(input[i]);
    }
    return result;
  }

  private Geometry buildResult(Geometry geom) {
    if (geom instanceof MultiPolygon) {
      //TODO: build MultiPoly
      return null;
    }
    else {
      return buildPolygon((Polygon) geom);
    }
  }

  private Geometry buildPolygon(Polygon polygon) {
    LinearRing shell = buildRing(polygon.getExteriorRing());
    //TODO: handle holes
    return geomFactory.createPolygon(shell);
  }

  private LinearRing buildRing(LinearRing ring) {
    List<CoverageEdge> edges = ringEdgesMap.get(ring);
    CoordinateList ptsList = new CoordinateList();
    for (int i = 0; i < edges.size(); i++) {
      Coordinate lastPt = ptsList.size() > 0 
                            ? ptsList.getCoordinate(ptsList.size() - 1)
                            : null;
      boolean dir = isEdgeForward(edges, i, lastPt);
      ptsList.add(edges.get(i).getCoordinates(), false, dir);
    }
    Coordinate[] pts = ptsList.toCoordinateArray();
    return geomFactory.createLinearRing(pts);
  }

  private boolean isEdgeForward(List<CoverageEdge> edges, int index, Coordinate lastPt) {
    int size = edges.size();
    if (size <= 1) return true;
    if (index == 0) {
      //-- if only 2 edges, first one can keep orientation
      if (size == 2)
        return true;
      Coordinate endPt0 = edges.get(0).getEndCoordinate();
      return endPt0.equals2D(edges.get(1).getStartCoordinate())
          || endPt0.equals2D(edges.get(1).getEndCoordinate());
    }
    //-- last point indicates required orientation
    return lastPt.equals2D(edges.get(index).getStartCoordinate());
  }

}
