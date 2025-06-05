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
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

/**
 * Models a polygonal coverage as a set of unique {@link CoverageEdge}s,
 * linked to the parent rings in the coverage polygons.
 * Each edge has either one or two parent rings, depending on whether 
 * it is an inner or outer edge of the coverage.
 * The source coverage is represented as a array of polygonal geometries 
 * (either {@link Polygon}s or {@link MultiPolygon}s).
 * <p>
 * Coverage edges are found by identifying vertices which are nodes in the coverage,
 * splitting edges at nodes, and then identifying unique edges.
 * The unique edges are associated to their parent ring (in order),
 * to allow reforming the coverage polygons.
 * <p>
 * One ring in each polygonal geometry is marked as primary. 
 * Primary edges are always retained during coverage processing
 * (such as simplification), to ensure the geometry does not disappear entirely.
 * The primary ring is the shell of the largest polygon in the geometry.
 * 
 * @author Martin Davis
 *
 */
class CoverageRingEdges {
  
  /**
   * Create a new instance for a given coverage.
   * 
   * @param coverage the set of polygonal geometries in the coverage
   * @return the edges of the coverage
   */
  public static CoverageRingEdges create(Geometry[] coverage) {
    CoverageRingEdges edges = new CoverageRingEdges(coverage);
    return edges;
  }
  
  private Geometry[] coverage;
  private Map<LinearRing, List<CoverageEdge>> ringEdgesMap;
  private List<CoverageEdge> edges;
  
  public CoverageRingEdges(Geometry[] coverage) {
    this.coverage = coverage;
    ringEdgesMap = new HashMap<LinearRing, List<CoverageEdge>>();
    edges = new ArrayList<CoverageEdge>();
    build();
  }

  public List<CoverageEdge> getEdges() {
    return edges;
  }
  
  private void build() {
    Set<Coordinate> nodes = findMultiRingNodes(coverage);
    Set<LineSegment> boundarySegs = CoverageBoundarySegmentFinder.findBoundarySegments(coverage);
    nodes.addAll(findBoundaryNodes(boundarySegs));
    HashMap<LineSegment, CoverageEdge> uniqueEdgeMap = new HashMap<LineSegment, CoverageEdge>();
    for (int i = 0; i < coverage.length; i++) {
      //-- geom is a Polygon or MultiPolygon
      Geometry geom = coverage[i];
      int indexLargest = findLargestPolygonIndex(geom);
      for (int ipoly = 0; ipoly < geom.getNumGeometries(); ipoly++) {
        Polygon poly = (Polygon) geom.getGeometryN(ipoly);
        
        //-- skip empty elements. Missing elements are copied in result
        if (poly.isEmpty())
          continue;
        
        //-- largest polygon is the primary one, which is never removed
        boolean isPrimary = ipoly == indexLargest;
        
        //-- extract shell
        LinearRing shell = poly.getExteriorRing();
        addRingEdges(i, shell, isPrimary, nodes, boundarySegs, uniqueEdgeMap);
        //-- extract holes
        for (int ihole = 0; ihole < poly.getNumInteriorRing(); ihole++) {
          LinearRing hole = poly.getInteriorRingN(ihole);
          //-- skip empty holes. Missing rings are copied in result
          if (hole.isEmpty())
            continue;
          //-- holes are never primary
          addRingEdges(i, hole, false, nodes, boundarySegs, uniqueEdgeMap);         
        }
      }
    }
  }

  /**
   * Finds the index of the polygonal element with largest area.
   * 
   * @param geom the polygonal geometry to scan
   * @return the index of the polygonal element with largest area
   */
  private int findLargestPolygonIndex(Geometry geom) {
    if (geom instanceof Polygon)
      return 0;
    int indexLargest = -1;
    double areaLargest = -1;
    for (int ipoly = 0; ipoly < geom.getNumGeometries(); ipoly++) {
      Polygon poly = (Polygon) geom.getGeometryN(ipoly);
      double area = poly.getArea();
      if (area > areaLargest) {
        areaLargest = area;
        indexLargest = ipoly;
      }
    }
    return indexLargest;
  }

  /**
   * Adds the {@link CoverageEdge}s for a ring
   * to the map of ring edges.
   * 
   * @param index the index of the geometry in the coverage
   * @param ring the ring to extract edges from
   * @param isPrimary true if the ring is primary (must not be removed)
   * @param nodes nodes in the coverage
   * @param boundarySegs the coverage boundary segments
   * @param uniqueEdgeMap map of unique edges
   */
  private void addRingEdges(int index, LinearRing ring, boolean isPrimary, 
      Set<Coordinate> nodes, 
      Set<LineSegment> boundarySegs,
      HashMap<LineSegment, CoverageEdge> uniqueEdgeMap) {
    addBoundaryInnerNodes(ring, boundarySegs, nodes);
    List<CoverageEdge> ringEdges = extractRingEdges(index, ring, isPrimary, uniqueEdgeMap, nodes);
    if (ringEdges != null)
      ringEdgesMap.put(ring, ringEdges);
  }

  /**
   * Detects nodes occurring at vertices which are between a boundary segment 
   * and an inner (shared) segment.  
   * These occur where two polygons are adjacent at the coverage boundary
   * (this is not detected by {@link #findMultiRingNodes(Geometry[])}).
   * 
   * @param ring the ring to extract edges from
   * @param boundarySegs the coverage boundary segments
   * @param nodes nodes in the coverage
   */
  private void addBoundaryInnerNodes(LinearRing ring, Set<LineSegment> boundarySegs, Set<Coordinate> nodes) {
    CoordinateSequence seq = ring.getCoordinateSequence();
    boolean isBdyLast = CoverageBoundarySegmentFinder.isBoundarySegment(boundarySegs, seq, seq.size() - 2);
    boolean isBdyPrev = isBdyLast;
    for (int i = 0; i < seq.size() - 1; i++) {
      boolean isBdy = CoverageBoundarySegmentFinder.isBoundarySegment(boundarySegs, seq, i);
      if (isBdy != isBdyPrev) {
        Coordinate nodePt = seq.getCoordinate(i);
        nodes.add(nodePt);
      }
      isBdyPrev = isBdy;
    }
  }

  /**
   * Extracts the {@link CoverageEdge}s for a ring.
   * 
   * @param index the index of the geometry in the coverage
   * @param ring the ring to extract edges from
   * @param isPrimary true if the ring is primary (must not be removed)
   * @param uniqueEdgeMap map of unique edges
   * @param nodes nodes in the coverage
   * @return null if the ring has too few distinct vertices
   */
  private List<CoverageEdge> extractRingEdges(int index, LinearRing ring, 
      boolean isPrimary, HashMap<LineSegment, CoverageEdge> uniqueEdgeMap, 
      Set<Coordinate> nodes) {
 // System.out.println(ring);
    List<CoverageEdge> ringEdges = new ArrayList<CoverageEdge>();
    
    Coordinate[] pts = ring.getCoordinates();
    pts = CoordinateArrays.removeRepeatedPoints(pts);
    //-- if compacted ring is too short, don't process it
    if (pts.length < 3)
      return null;
    
    int first = findNextNodeIndex(pts, -1, nodes);
    if (first < 0) {
      //-- ring does not contain a node, so edge is entire ring
      CoverageEdge edge = createEdge(pts, -1, -1, index, isPrimary, uniqueEdgeMap);
      ringEdges.add(edge);
    }
    else {
      int start = first;
      int end = start;
      //-- two-node edges are always primary
      boolean isEdgePrimary = true;
      do {
        end = findNextNodeIndex(pts, start, nodes);
        //-- a single-node ring is only retained if specified
        if (end == start) {
          isEdgePrimary = isPrimary;
        }
        CoverageEdge edge = createEdge(pts, start, end, index, isEdgePrimary, uniqueEdgeMap);
//  System.out.println(ringEdges.size() + " : " + edge);
        ringEdges.add(edge);
        start = end;
      } while (end != first);
    }
    return ringEdges;
  }
  
  /**
   * Creates or updates an edge for the given ring or ring section.
   * 
   * @param ring ring to create edge for
   * @param start start index of ring section; -1 indicates edge is entire ring
   * @param end end index of ring section
   * @param index the index of the parent geometry in the coverage
   * @param isPrimary true if the ring is primary (must not be removed)
   * @param uniqueEdgeMap map of edges
   * @return the CoverageEdge for the ring or portion of ring
   */
  private CoverageEdge createEdge(Coordinate[] ring, int start, int end, int index, boolean isPrimary, HashMap<LineSegment, CoverageEdge> uniqueEdgeMap) {
    CoverageEdge edge;
    LineSegment edgeKey = (end == start) ? CoverageEdge.key(ring) : CoverageEdge.key(ring, start, end);
    if (uniqueEdgeMap.containsKey(edgeKey)) {
      edge = uniqueEdgeMap.get(edgeKey);
      //-- update shared attributes
      edge.setPrimary(isPrimary);
    }
    else {
      if (start < 0) {
        edge = CoverageEdge.createEdge(ring, isPrimary);
      }
      else {
        edge = CoverageEdge.createEdge(ring, start, end, isPrimary);
      }
      uniqueEdgeMap.put(edgeKey, edge);
      edges.add(edge);
    }
    edge.addIndex(index);
    edge.incRingCount();
    return edge;
  }

  private int findNextNodeIndex(Coordinate[] ring, int start, Set<Coordinate> nodes) {
    int index = start;
    boolean isScanned0 = false;
    do {
      index = next(index, ring);
      if (index == 0) {
        if (start < 0 && isScanned0) 
          return -1;
        isScanned0 = true;
      }
      Coordinate pt = ring[index];
      if (nodes.contains(pt)) {
        return index;
      }
    } while (index != start);
    return -1;
  }

  private static int next(int index, Coordinate[] ring) {
    index = index + 1;
    if (index >= ring.length - 1)
      index = 0;
    return index;
  }

  /**
   * Finds nodes in a coverage at vertices which are shared by 3 or more rings.
   * 
   * @param coverage a list of polygonal geometries
   * @return the set of nodes contained in 3 or more rings
   */
  private Set<Coordinate> findMultiRingNodes(Geometry[] coverage) {
    Map<Coordinate, Integer> vertexRingCount = VertexRingCounter.count(coverage);
    Set<Coordinate> nodes = new HashSet<Coordinate>();
    for (Coordinate v : vertexRingCount.keySet()) {
      if (vertexRingCount.get(v) >= 3) {
        nodes.add(v);
      }
    }
    return nodes;
  }

  /**
   * Finds nodes occurring between boundary segments.
   * Nodes on boundaries occur at vertices which have 
   * 3 or more incident boundary segments.
   * This detects situations where two rings touch only at a vertex
   * (i.e. two polygons touch, or a polygon shell touches a hole)
   * These nodes lie in only 2 adjacent rings, 
   * so are not detected by {@link #findMultiRingNodes(Geometry[])}. 
   * 
   * @param boundarySegments
   * @return a set of vertices which are nodes where two rings touch
   */
  private Set<Coordinate> findBoundaryNodes(Set<LineSegment> boundarySegments) {
    Map<Coordinate, Integer> counter = new HashMap<>();
    for (LineSegment seg : boundarySegments) {
      counter.compute(seg.p0, (key, value) -> value == null ? 1 : value + 1);
      counter.compute(seg.p1, (key, value) -> value == null ? 1 : value + 1);
    }
    return counter.entrySet().stream()
        .filter(e->e.getValue() > 2)
        .map(Map.Entry::getKey).collect(Collectors.toSet());
  }

  /**
   * Recreates the polygon coverage from the current edge values.
   * 
   * @return an array of polygonal geometries representing the coverage
   */
  public Geometry[] buildCoverage() {
    Geometry[] result = new Geometry[coverage.length];
    for (int i = 0; i < coverage.length; i++) {
      result[i] = buildPolygonal(coverage[i]);
    }
    return result;
  }

  private Geometry buildPolygonal(Geometry geom) {
    if (geom instanceof MultiPolygon) {
      return buildMultiPolygon((MultiPolygon) geom);
    }
    else {
      return buildPolygon((Polygon) geom);
    }
  }

  private Geometry buildMultiPolygon(MultiPolygon geom) {
    List<Polygon> polyList = new ArrayList<Polygon>();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
       Polygon poly = buildPolygon((Polygon) geom.getGeometryN(i));
       if (poly != null) {
         polyList.add(poly);
       }
    }
    if (polyList.size() == 1) {
      return polyList.get(0);
    }
    Polygon[] polys = GeometryFactory.toPolygonArray(polyList);
    return geom.getFactory().createMultiPolygon(polys);
  }

  /**
   * 
   * @param polygon
   * @return null if the polygon has been removed
   */
  private Polygon buildPolygon(Polygon polygon) {
    LinearRing shell = buildRing(polygon.getExteriorRing());
    if (shell == null) {
      return null;
    }
    if (polygon.getNumInteriorRing() == 0) {
      return polygon.getFactory().createPolygon(shell);
    }
    List<LinearRing> holeList = new ArrayList<LinearRing>();
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      LinearRing hole = polygon.getInteriorRingN(i);
      LinearRing newHole = buildRing(hole);
      if (newHole != null) {
        holeList.add(newHole);
      }
    }
    //LinearRing holes[] = new LinearRing[polygon.getNumInteriorRing()];
    LinearRing holes[] = GeometryFactory.toLinearRingArray(holeList);
    return polygon.getFactory().createPolygon(shell, holes);
  }

  private LinearRing buildRing(LinearRing ring) {
    List<CoverageEdge> ringEdges = ringEdgesMap.get(ring);
    //-- if ring is not in map, must have been invalid.  Just copy original
    if (ringEdges == null)
      return (LinearRing) ring.copy();
    
    boolean isRemoved = ringEdges.size() == 1
        && ringEdges.get(0).getCoordinates().length == 0;
    if (isRemoved)
      return null;
    
    CoordinateList ptsList = new CoordinateList();
    for (int i = 0; i < ringEdges.size(); i++) {
      Coordinate lastPt = ptsList.size() > 0 
                            ? ptsList.getCoordinate(ptsList.size() - 1)
                            : null;
      boolean dir = isEdgeDirForward(ringEdges, i, lastPt);
      ptsList.add(ringEdges.get(i).getCoordinates(), false, dir);
    }
    Coordinate[] pts = ptsList.toCoordinateArray();
    return ring.getFactory().createLinearRing(pts);
  }

  private boolean isEdgeDirForward(List<CoverageEdge> ringEdges, int index, Coordinate prevPt) {
    int size = ringEdges.size();
    if (size <= 1) return true;
    if (index == 0) {
      //-- if only 2 edges, first one can keep orientation
      if (size == 2)
        return true;
      Coordinate endPt0 = ringEdges.get(0).getEndCoordinate();
      return endPt0.equals2D(ringEdges.get(1).getStartCoordinate())
          || endPt0.equals2D(ringEdges.get(1).getEndCoordinate());
    }
    //-- previous point determines required orientation
    return prevPt.equals2D(ringEdges.get(index).getStartCoordinate());
  }

}
