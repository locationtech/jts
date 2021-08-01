/*
 * Copyright (c) 2021 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.triangulatepoly;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.triangulatepoly.tri.Tri;

public class ApproximateMedialAxis {

  public static Geometry computeAxis(Geometry geom) {
    ApproximateMedialAxis tt = new ApproximateMedialAxis((Polygon) geom);
    return tt.compute();
  }
  
  private Polygon inputPolygon;
  private GeometryFactory geomFact;
  private List<LineString> lines = new ArrayList<LineString>();
  private Map<Tri, AxisNode> nodeMap = new HashMap<Tri, AxisNode>();
  private Deque<AxisNode> nodeQue = new ArrayDeque<AxisNode>();

  public ApproximateMedialAxis(Polygon polygon) {
    this.inputPolygon = polygon;
    geomFact = inputPolygon.getFactory();
  }
  
  private Geometry compute() {
    ConstrainedDelaunayTriangulator cdt = new ConstrainedDelaunayTriangulator(inputPolygon);
    List<Tri> tris = cdt.triangulatePolygon(inputPolygon);
    
    constructLines(tris);
    return geomFact.createMultiLineString(GeometryFactory.toLineStringArray(lines));
  }
  
  private void constructLines(List<Tri> tris)
  {
    for (Tri tri : tris) {
      if (tri.numAdjacent() == 1) {
        lines.add(constructLeafLine(tri));
      }
    }
    while (! nodeQue.isEmpty()) {
      AxisNode node = nodeQue.pop();
      lines.addAll(constructNodeLines(node));
    }
  }

  private List<LineString> constructNodeLines(AxisNode node) {
    List<LineString> lines = new ArrayList<LineString>();
    if (node.numAdjacent() == 3) {
      // TODO: add lines joining edge pts to circumcentre or centre
      node.addInternalLines(geomFact, lines);
    }
    else if (node.numAdjacent() == 2) {
      Tri tri = node.getTri();
      int exitEdge = node.exitEdge();
      Coordinate exitPt = tri.midpoint(exitEdge);
      
      // TODO: improve this - use circumcentre if acute?
      node.addInternalLinesToExit(geomFact, lines);
      
      Tri triNext = tri.getAdjacent(exitEdge);
      if (triNext.numAdjacent() == 3) {
        int adjNext = triNext.getIndex(tri);
        addNodeEntry(triNext, adjNext, exitPt);
        return lines;
      }
      //TODO: make this better
      lines.add( constructPathLine(tri, exitEdge, exitPt, null)) ;
    }
    //TODO: handle 1-adj nodes - these occur around holes
    return lines;
  }

  private LineString constructLeafLine(Tri triStart) {
    List<Coordinate> pts = new ArrayList<Coordinate>();
    int eAdj = indexOfAdjacent(triStart);
    
    int vOpp = Tri.oppVertex(eAdj);
    Coordinate startPt = triStart.getCoordinate(vOpp);
    Coordinate edgePt = angleBisector(triStart, vOpp);
    pts.add(startPt);

    return constructPathLine(triStart, eAdj, edgePt, pts);
  }

  private LineString constructPathLine(Tri triStart, int eStart, Coordinate pt,
      List<Coordinate> pts) {
    if (pts == null) {
      pts = new ArrayList<Coordinate>();
    }
    pts.add(pt);
    Tri triNext = triStart.getAdjacent(eStart);
    int eAdjNext = triNext.getIndex(triStart);
    extendLine(triNext, eAdjNext, pts);
    return geomFact.createLineString(CoordinateArrays.toCoordinateArray(pts));
  }
  
  private void extendLine(Tri tri, int edge, List<Coordinate> pts) {
    //if (pts.size() > 100) return;
    /**
     * 3 cases:
     * - next has no free edges -> queue for processing
     * - next is 2-adj, next-next has free edge on same side -> add midpoint
     * - next is 2-adj, next-next has free edge on opp side -> add midpoint of next-next
     */
    int numAdj = tri.numAdjacent();
    if (numAdj == 3) {
      addNodeEntry(tri, edge, pts.get(pts.size() - 1));
      return;
    }
    if (numAdj < 2) 
      return;
    
    //--- now are only dealing with 2-Adj triangles
    int eOpp = indexOfAdjacentOther(tri, edge);
    if (isCorridor(tri, eOpp)) {
      Tri triN = tri.getAdjacent(eOpp);
      int eAdjN = triN.getIndex(tri);
      int eOppN = indexOfAdjacentOther(triN, eAdjN);
      Coordinate p = triN.midpoint(eOppN);
      pts.add(p);
      Tri triNN = triN.getAdjacent(eOppN);
      int eOppNN = triNN.getIndex(triN);
      extendLine(triNN, eOppNN, pts);
    }
    else {
      Coordinate p = tri.midpoint(eOpp);
      pts.add(p);
      Tri triNN = tri.getAdjacent(eOpp);
      int eAdjNN = triNN.getIndex(tri);
      extendLine(triNN, eAdjNN, pts);
    }
  }

  private static boolean isCorridor(Tri tri, int eCommon) {
    int eFree = indexOfNonAdjacent(tri);
    int vOppFree = Tri.oppVertex(eFree);
    Coordinate pFreeOpp = tri.getCoordinate(vOppFree);
    
    Tri triNext = tri.getAdjacent(eCommon);
    if (triNext.numAdjacent() != 2)
      return false;

    int eFreeN = indexOfNonAdjacent(triNext);
    int vOppFreeN = Tri.oppVertex(eFreeN);
    Coordinate pFreeOppN = triNext.getCoordinate(vOppFreeN);
    
    return ! pFreeOpp.equals2D(pFreeOppN);
  }

  private void addNodeEntry(Tri tri, int edge, Coordinate pt) {
    AxisNode node = nodeMap.get(tri);
    if (node == null) {
      node = new AxisNode(tri);
      nodeMap.put(tri, node);
    }
    node.addEntry(edge, pt);
    if (node.has2Adjacent()) {
      nodeQue.add(node);
    }
  }
  
  private LineString generateLineAdj1(Tri triStart) {
    int iAdj = indexOfAdjacent(triStart);
    int iOpp = Tri.prev(iAdj);
    Coordinate v0 = triStart.getCoordinate(iOpp);
    Coordinate midOpp = triStart.midpoint(iAdj);
    return line(v0, midOpp);
  }

  private LineString generateLineAdj2(Tri tri) {
    int iNoAdj = indexOfNonAdjacent(tri);
    int iOpp1 = Tri.prev(iNoAdj);
    int iOpp2 = Tri.next(iNoAdj);
    Coordinate v0 = tri.midpoint(iOpp1);
    Coordinate v1 = tri.midpoint(iOpp2);
    return line(v0, v1);
  }

  private LineString[] generateLineAdj3(Tri tri) {
    /**
     * Circumcentre doesn't work because it lies outside obtuse triangles.
     * Centroid is too affected by a side of very different length.
     * Maybe some centre which is biased towards sides with most similar length?
     */
    Coordinate cc = Triangle.circumcentre(tri.getCoordinate(0), 
        tri.getCoordinate(1), tri.getCoordinate(2));
    if (! Triangle.intersects(tri.getCoordinate(0), 
        tri.getCoordinate(1), tri.getCoordinate(2), cc)) {
      return null;
    }
    Coordinate v0 = tri.midpoint(0);
    Coordinate v1 = tri.midpoint(1);
    Coordinate v2 = tri.midpoint(2);
    LineString line0 = line(v0, cc.copy());
    LineString line1 = line(v1, cc.copy());
    LineString line2 = line(v2, cc.copy());
    return new LineString[] { line0, line1, line2 };
  }

  private LineString line(Coordinate p0, Coordinate p1) {
    return geomFact.createLineString(new Coordinate[] { p0, p1 });
  }

  private static int indexOfAdjacent(Tri tri) {
    for (int i = 0; i < 3; i++) {
      if (tri.hasAdjacent(i))
        return i;
    }
    return -1;
  }
  
  private static int indexOfAdjacentOther(Tri tri, int e) {
    for (int i = 0; i < 3; i++) {
      if (i != e && tri.hasAdjacent(i))
        return i;
    }
    return -1;
  }
  
  private static int indexOfNonAdjacent(Tri tri) {
    for (int i = 0; i < 3; i++) {
      if (! tri.hasAdjacent(i))
        return i;
    }
    return -1;
  }

  private static Coordinate angleBisector(Tri tri, int v) {
    return Triangle.angleBisector(
        tri.getCoordinate(Tri.prev(v)),
        tri.getCoordinate(v),
        tri.getCoordinate(Tri.next(v))
        );
  }
}

class AxisNode {
  
  private Tri tri;
  
  private Coordinate p0;
  private Coordinate p1;
  private Coordinate p2;
  
  public AxisNode(Tri tri) {
    this.tri = tri;
  }
  
  public void addInternalLines(GeometryFactory geomFact, List<LineString> lines) {
    Coordinate cc = circumcentre();
    if (! intersects(cc)) {
      cc = tri.midpoint(longestEdge());
    }
    addInternalLines(cc, geomFact, lines);
  }

  private Coordinate centrePoint() {
    Coordinate cc = circumcentre();
    if (! intersects(cc)) {
      //cc = centroid();
      //TODO: for obtuse triangle use intersection point of longest edge and segment to circumcentre
      cc = tri.midpoint(longestEdge());
    }
    return cc;
  }

  public void addInternalLines(Coordinate p, GeometryFactory geomFact, List<LineString> lines) {
    if (p0 != null) lines.add(createLine(p0, p, geomFact, lines));
    if (p1 != null) lines.add(createLine(p1, p, geomFact, lines));
    if (p2 != null) lines.add(createLine(p2, p, geomFact, lines));
  }
  
  public void addInternalLinesToExit(GeometryFactory geomFact, List<LineString> lines) {
    int exitEdge = exitEdge();
    Coordinate exitPt = tri.midpoint(exitEdge);
    Coordinate adjEndpoint = exitPt;
    Coordinate cc = circumcentre();
    if (intersects(cc)) {
      lines.add(createLine(cc, exitPt, geomFact, lines));
      adjEndpoint = cc;
    }
    
    if (exitEdge != 0 && p0 != null) lines.add(createLine(p0, adjEndpoint, geomFact, lines));
    if (exitEdge != 1 && p1 != null) lines.add(createLine(p1, adjEndpoint, geomFact, lines));
    if (exitEdge != 2 && p2 != null) lines.add(createLine(p2, adjEndpoint, geomFact, lines));
    
  }

  private boolean intersects(Coordinate p) {
    return Triangle.intersects(tri.getCoordinate(0), 
        tri.getCoordinate(1), tri.getCoordinate(2), p);
  }

  private Coordinate circumcentre() {
    return Triangle.circumcentre(tri.getCoordinate(0), 
        tri.getCoordinate(1), tri.getCoordinate(2));
  }

  /**
   * Edge opposite obtuse angle, if any
   * @return edge index of longest edge
   */
  private int longestEdge() {
    int e = 0;
    if (edgeLen(1) > edgeLen(e)) {
      e = 1;
    }
    if (edgeLen(2) > edgeLen(e)) {
      e = 2;
    }
    return e;
  }
  
  private double edgeLen(int i) {
    return tri.getCoordinate(i).distance(tri.getCoordinate(Tri.next(i)));
  }
  
  private static LineString createLine(Coordinate p0, Coordinate p1, 
      GeometryFactory geomFact, List<LineString> lines) {
    return geomFact.createLineString(new Coordinate[] {
      p0.copy(), p1.copy()
    });
  }

  public Tri getTri() {
    return tri;
  }

  public void addEntry(int eAdj, Coordinate pt) {
    setEntryPoint(eAdj, pt);
  }
  
  private void setEntryPoint(int i, Coordinate p) {
    switch (i) {
    case 0: p0 = p; return;
    case 1: p1 = p; return;
    case 2: p2 = p; return;
    }
  }
  
  public boolean has2Adjacent() {
    return numAdjacent() >= 2;
  }

  public int numAdjacent() {
    int num = 0;
    if (p0 != null) num++;
    if (p1 != null) num++;
    if (p2 != null) num++;
    return num;
  }
  
  public int exitEdge() {
    if (p0 == null) return 0;
    if (p1 == null) return 1;
    if (p2 == null) return 2;
    return -1;
  }
  
}
