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

import org.locationtech.jts.algorithm.Distance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.triangulatepoly.tri.Tri;

public class ApproximateMedialAxis {

  public static Geometry computeAxis(Geometry geom) {
    ApproximateMedialAxis tt = new ApproximateMedialAxis((Polygon) geom);
    return tt.compute();
  }
  
  public static Geometry axisPointSegment(Geometry pt, Geometry seg) {
    Coordinate p = pt.getCoordinate();
    Coordinate[] pts = seg.getCoordinates();
    Coordinate axisPt = medialAxisPoint(p, pts[0], pts[1]);
    return pt.getFactory().createPoint(axisPt);
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
        lines.add( constructLeafLine(tri) );
      }
    }
    while (! nodeQue.isEmpty()) {
      AxisNode node = nodeQue.pop();
      lines.addAll( constructNodeLines(node) );
    }
  }

  private LineString constructLeafLine(Tri triStart) {
    int eAdj = indexOfAdjacent(triStart);
    
    int vOpp = Tri.oppVertex(eAdj);
    Coordinate startPt = triStart.getCoordinate(vOpp);
    Coordinate edgePt = angleBisector(triStart, vOpp);

    return constructPathLine(triStart, eAdj, startPt, edgePt);
  }
  
  private List<LineString> constructNodeLines(AxisNode node) {
    List<LineString> lines = new ArrayList<LineString>();
    if (node.numAdjacent() == 3) {
      node.addInternalLines(lines, geomFact);
    }
    else if (node.numAdjacent() == 2) {
      Tri tri = node.getTri();
      int exitEdge = node.exitEdge();
      Coordinate exitPt = tri.midpoint(exitEdge);
      
      node.addInternalLinesToExit(lines, geomFact);
      
      Tri triNext = tri.getAdjacent(exitEdge);
      /**
       * If next tri is a node as well, queue it
       */
      if (triNext.numAdjacent() == 3) {
        int adjNext = triNext.getIndex(tri);
        createNodeEntryPoint(triNext, adjNext, exitPt);
        return lines;
      }
      //TODO: make this better
      lines.add( constructPathLine(tri, exitEdge, exitPt)) ;
    }
    //TODO: handle 1-adj nodes - these occur around holes
    return lines;
  }
  
  private LineString constructPathLine(Tri triStart, int eStart, 
      Coordinate p0, Coordinate p1)
  {
    ArrayList<Coordinate> pts = new ArrayList<Coordinate>();
    pts.add(p0);
    pts.add(p1);
    return constructPathLine(triStart, eStart, pts);
  }
  
  private LineString constructPathLine(Tri triStart, int eStart, 
      Coordinate p0)
  {
    ArrayList<Coordinate> pts = new ArrayList<Coordinate>();
    pts.add(p0);
    return constructPathLine(triStart, eStart, pts);
  }
  
  private LineString constructPathLine(Tri triStart, int eStart,
      List<Coordinate> pts) {
    Tri triNext = triStart.getAdjacent(eStart);
    int eAdjNext = triNext.getIndex(triStart);
    extendLine(triNext, eAdjNext, pts);
    return geomFact.createLineString(CoordinateArrays.toCoordinateArray(pts));
  }
  
  private void extendLine(Tri tri, int edgeEntry, List<Coordinate> pts) {
    //if (pts.size() > 100) return;
    /**
     * 3 cases:
     * - next has no free edges -> queue for processing
     * - next is 2-adj, next-next has free edge on same side -> add midpoint
     * - next is 2-adj, next-next has free edge on opp side -> add midpoint of next-next
     */
    int numAdj = tri.numAdjacent();
    if (numAdj == 3) {
      createNodeEntryPoint(tri, edgeEntry, pts.get(pts.size() - 1));
      return;
    }
    if (numAdj < 2) {
      return;
    }
    
    //--- now are only dealing with 2-Adj triangles
    int eAdj = indexOfAdjacentOther(tri, edgeEntry);
    if (isTube(tri, eAdj)) {
     /**
      * If this triangle and the next one form a "tube"
      * then optimize the medial axis line construction.
      */
     Tri tri2 = tri.getAdjacent(eAdj);
      Coordinate p = exitPointTube(tri, tri2);
      pts.add(p);
      
      int eAdj2 = tri2.getIndex(tri);
      int eOpp2 = indexOfAdjacentOther(tri2, eAdj2);
      Tri triN = tri2.getAdjacent(eOpp2);
      int eOppN = triN.getIndex(tri2);
      extendLine(triN, eOppN, pts);
    }
    else {
      //--- A "wedge" triangle with one boundary edge
      Coordinate p = exitPointWedge(tri, eAdj);
      pts.add(p);
      Tri triN = tri.getAdjacent(eAdj);
      int eAdjN = triN.getIndex(tri);
      extendLine(triN, eAdjN, pts);
    }
  }

  private Coordinate exitPointWedge(Tri tri, int eExit) {
    int eBdy = indexOfNonAdjacent(tri);
    Coordinate pt = tri.getCoordinate(Tri.oppVertex(eBdy));
    Coordinate p0 = tri.getCoordinate(eBdy);
    Coordinate p1 = tri.getCoordinate(Tri.next(eBdy));
    if (Tri.next(eBdy) != eExit) {
      p0 = tri.getCoordinate(Tri.next(eBdy));
      p1 = tri.getCoordinate(eBdy);
    }
    return medialAxisPoint(pt, p0, p1);
  }

  /**
   * Computes medial axis point on exit edge of a "tube".
   * 
   * @param tri1 the first triangle in the tube 
   * @param tri2 the second triangle in the tube
   * @return medial axis exit point of tube
   */
  private Coordinate exitPointTube(Tri tri1, Tri tri2) {
    
    int eBdy1 = indexOfNonAdjacent(tri1);
    int eBdy2 = indexOfNonAdjacent(tri2);
    //--- Case eBdy1 is eEntry.next
    Coordinate p00 = tri1.getCoordinate(eBdy1);
    Coordinate p01 = tri1.getCoordinate(Tri.next(eBdy1));
    Coordinate p10 = tri2.getCoordinate(Tri.next(eBdy2));
    Coordinate p11 = tri2.getCoordinate(eBdy2);
    
    int eAdj1 = tri1.getIndex(tri2);
    if (Tri.next(eBdy1) != eAdj1) {
      p00 = tri1.getCoordinate(Tri.next(eBdy1));
      p01 = tri1.getCoordinate(eBdy1);
      p10 = tri2.getCoordinate(eBdy2);
      p11 = tri2.getCoordinate(Tri.next(eBdy2));
    }
    Coordinate axisPoint = medialAxisPoint(p00, p01, p10, p11);
    return axisPoint;
  }

  private static final double MEDIAL_AXIS_EPS = .01;

  /**
   * Computes the approximate point where the medial axis  
   * between two line segments
   * intersects the line between the ends of the segments.
   * 
   * @param p00 the start vertex of segment 0
   * @param p01 the end vertex of segment 0
   * @param p10 the start vertex of segment 1
   * @param p11 the end vertex of segment 1
   * @return the approximate medial axis point
   */
  private static Coordinate medialAxisPoint(
      Coordinate p00, Coordinate p01, 
      Coordinate p10, Coordinate p11) {
    double endFrac0 = 0;
    double endFrac1 = 1;
    double eps = 0.0;
    LineSegment edgeExit = new LineSegment(p01, p11);
    double edgeLen = edgeExit.getLength();
    Coordinate axisPt = null;
    do {
      double midFrac = (endFrac0 + endFrac1) / 2;
      axisPt = edgeExit.pointAlong(midFrac);
      double dist0 = Distance.pointToSegment(axisPt, p00, p01);
      double dist1 = Distance.pointToSegment(axisPt, p10, p11);
      if (dist0 > dist1) {
        endFrac1 = midFrac;
       }
      else {
        endFrac0 = midFrac;       
      }
      eps = Math.abs(dist0 - dist1) / edgeLen;
    }
    while (eps > MEDIAL_AXIS_EPS);
    return axisPt;
  }

  /**
   * Computes the approximate point where the medial axis 
   * between a point and a line segment
   * intersects the line between the point and the segment endpoint
   * 
   * @param p the point
   * @param p0 the first vertex of the segment
   * @param p1 the second vertex of the segment
   * @return
   */
  private static Coordinate medialAxisPoint(Coordinate p, Coordinate p0, Coordinate p1) {
    double endFrac0 = 0;
    double endFrac1 = 1;
    double eps = 0.0;
    LineSegment edgeExit = new LineSegment(p, p1);
    double edgeLen = edgeExit.getLength();
    Coordinate axisPt = null;
    do {
      double midFrac = (endFrac0 + endFrac1) / 2;
      axisPt = edgeExit.pointAlong(midFrac);
      double distPt = p.distance(axisPt);
      double distSeg = Distance.pointToSegment(axisPt, p0, p1);
      if (distPt > distSeg) {
        endFrac1 = midFrac;
       }
      else {
        endFrac0 = midFrac;       
      }
      eps = Math.abs(distSeg - distPt) / edgeLen;
    }
    while (eps > MEDIAL_AXIS_EPS);
    return axisPt;
  }

  /**
   * Tests if a triangle and its adjacent tri form a "tube",
   * where the opposite edges of the triangles are on the boundary.
   * 
   * @param tri the triangle to test
   * @param eAdj the edge adjacent to the next triangle
   * @return true if the two triangles form a tube
   */
  private static boolean isTube(Tri tri, int eAdj) {
    Tri triNext = tri.getAdjacent(eAdj);
    if (triNext.numAdjacent() != 2)
      return false;

    int eBdy = indexOfNonAdjacent(tri);
    int vOppBdy = Tri.oppVertex(eBdy);
    Coordinate pOppBdy = tri.getCoordinate(vOppBdy);
    
    int eBdyN = indexOfNonAdjacent(triNext);
    int vOppBdyN = Tri.oppVertex(eBdyN);
    Coordinate pOppBdyN = triNext.getCoordinate(vOppBdyN);
    
    return ! pOppBdy.equals2D(pOppBdyN);
  }

  private void createNodeEntryPoint(Tri tri, int edgeEntry, Coordinate pt) {
    AxisNode node = nodeMap.get(tri);
    if (node == null) {
      node = new AxisNode(tri);
      nodeMap.put(tri, node);
    }
    node.addEntryPoint(edgeEntry, pt);
    if (node.isPathStart()) {
      nodeQue.add(node);
    }
  }
  
  /*
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
  /*
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
*/
  
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
  
  public void addInternalLines(List<LineString> lines, GeometryFactory geomFact) {
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
  
  public void addInternalLinesToExit(List<LineString> lines, GeometryFactory geomFact) {
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

  public void addEntryPoint(int eAdj, Coordinate pt) {
    setEntryPoint(eAdj, pt);
  }
  
  private void setEntryPoint(int i, Coordinate p) {
    switch (i) {
    case 0: p0 = p; return;
    case 1: p1 = p; return;
    case 2: p2 = p; return;
    }
  }
  
  public boolean isPathStart() {
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
