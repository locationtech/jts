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
package org.locationtech.jts.algorithm.axis;

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
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator;
import org.locationtech.jts.triangulate.tri.Tri;


/**
 * Constructs an approximation to the medial axis of a Polygon, 
 * as a set of linestrings representing the medial axis graph.
 * 
 * @author mdavis
 *
 */
public class ApproximateMedialAxis {

  public static Geometry medialAxis(Geometry geom) {
    ApproximateMedialAxis tt = new ApproximateMedialAxis((Polygon) geom);
    return tt.compute();
  }
  
  /*
   //-- Testing only
  public static Geometry axisPointSegment(Geometry pt, Geometry seg) {
    Coordinate p = pt.getCoordinate();
    Coordinate[] pts = seg.getCoordinates();
    Coordinate axisPt = medialAxisPoint(p, pts[0], pts[1]);
    return pt.getFactory().createPoint(axisPt);
  }
  */
  
  private Polygon inputPolygon;
  private GeometryFactory geomFact;

  private Map<Tri, AxisNode> nodeMap = new HashMap<Tri, AxisNode>();
  private Deque<AxisNode> nodeQue = new ArrayDeque<AxisNode>();

  public ApproximateMedialAxis(Polygon polygon) {
    this.inputPolygon = polygon;
    geomFact = inputPolygon.getFactory();
  }
  
  private Geometry compute() {
    ConstrainedDelaunayTriangulator cdt = new ConstrainedDelaunayTriangulator(inputPolygon);
    List<Tri> tris = cdt.getTriangles();
    
    List<LineString> lines = constructLines(tris);
    return geomFact.createMultiLineString(GeometryFactory.toLineStringArray(lines));
  }
  
  private List<LineString> constructLines(List<Tri> tris)
  {
    List<LineString> lines = new ArrayList<LineString>();
    for (Tri tri : tris) {
      if (tri.numAdjacent() == 1) {
        lines.add( constructLeafLine(tri) );
      }
    }
    while (! nodeQue.isEmpty()) {
      AxisNode node = nodeQue.peek();
      if (node.isPathComplete()) {
        nodeQue.pop();
        node.addInternalLines(lines, geomFact);
        //--- done with this node
      }
      else {
        LineString path = constructPath(node);
         if (path != null)
           lines.add( path );
        //-- node is left in queue for further processing
      }
    }
    return lines;
  }

  private LineString constructLeafLine(Tri triStart) {
    int eAdj = indexOfAdjacent(triStart);
    
    int vOpp = Tri.oppVertex(eAdj);
    Coordinate startPt = triStart.getCoordinate(vOpp);
    Coordinate edgePt = angleBisector(triStart, vOpp);

    return constructPath(triStart, eAdj, startPt, edgePt);
  }

  private LineString constructPath(AxisNode node) {
    Tri tri = node.getTri();
    int freeEdge = node.getNonPathEdge();      
    //Coordinate exitPt = node.getPathPoint(freeEdge);
    Coordinate startPt = node.createPathPoint(freeEdge);
    
    Tri triNext = tri.getAdjacent(freeEdge);
    /**
     * If next tri is a node as well, queue it.
     * No path is constructed, since node internal lines connect
     */
    if (triNext.numAdjacent() == 3) {
      int adjNext = triNext.getIndex(tri);
      addNodePathPoint(triNext, adjNext, startPt);
      return null;
    }
    return constructPath(tri, freeEdge, startPt, null);
  }
  
  private LineString constructPath(Tri triStart, int eStart, 
      Coordinate p0, Coordinate p1)
  {
    ArrayList<Coordinate> pts = new ArrayList<Coordinate>();
    if (p0 != null) pts.add(p0);
    if (p1 != null) pts.add(p1);
    
    Tri triNext = triStart.getAdjacent(eStart);
    int eAdjNext = triNext.getIndex(triStart);
    extendPath(triNext, eAdjNext, pts);
    
    return geomFact.createLineString(CoordinateArrays.toCoordinateArray(pts));
  }
  
  private void extendPath(Tri tri, int edgeEntry, List<Coordinate> pts) {
    //if (pts.size() > 100) return;
    
    //TODO: make this iterative instead of recursive
    
    int numAdj = tri.numAdjacent();
    if (numAdj == 3) {
      addNodePathPoint(tri, edgeEntry, pts.get(pts.size() - 1));
      //--- path terminates at a node (3-adj tri)
      return;
    }
    if (numAdj < 2) {
      //-- leaf node - should never happen, has already been processed
      return;
    }
    
    //--- now are only dealing with 2-Adj triangles
    int eAdj = indexOfAdjacentOther(tri, edgeEntry);
    if (false && isTube(tri, eAdj)) {
     /**
      * This triangle and the next one form a "tube"
      * so use both to construct the medial line.
      */
     Tri tri2 = tri.getAdjacent(eAdj);
      Coordinate p = exitPointTube(tri, tri2);
      pts.add(p);
      
      int eAdj2 = tri2.getIndex(tri);
      int eOpp2 = indexOfAdjacentOther(tri2, eAdj2);
      Tri triN = tri2.getAdjacent(eOpp2);
      int eOppN = triN.getIndex(tri2);
      extendPath(triN, eOppN, pts);
    }
    else {
      /**
       * A "wedge" triangle (with one boundary edge).
       */
      Coordinate p = exitPointWedge(tri, eAdj);
      pts.add(p);
      Tri triN = tri.getAdjacent(eAdj);
      int eAdjN = triN.getIndex(tri);
      extendPath(triN, eAdjN, pts);
    }
  }

  private void addNodePathPoint(Tri tri, int edgeEntry, Coordinate pt) {
    AxisNode node = nodeMap.get(tri);
    if (node == null) {
      node = new AxisNode(tri);
      nodeMap.put(tri, node);
    }
    node.addPathPoint(edgeEntry, pt);
    nodeQue.add(node);
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
    /**
     * Midpoint produces a straighter line in nearly-parallel corridors, 
     * but is more see-sawed elsewhere. 
     */
    
    return tri.midpoint(eExit);
    //return medialAxisPoint(pt, p0, p1);
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
  /**
   * Axis path points along tri edges
   */
  private Coordinate p0;
  private Coordinate p1;
  private Coordinate p2;
  private boolean isLinesAdded = false;
  
  public AxisNode(Tri tri) {
    this.tri = tri;
  }
  
  public Tri getTri() {
    return tri;
  }

  public void addPathPoint(int edgeIndex, Coordinate p) {
    switch (edgeIndex) {
    case 0: p0 = p; return;
    case 1: p1 = p; return;
    case 2: p2 = p; return;
    }
  }
  
  public Coordinate createPathPoint(int edgeIndex) {
    Coordinate pt = tri.midpoint(edgeIndex);
    addPathPoint(edgeIndex, pt);
    return pt;
  }
  
  public Coordinate getPathPoint(int edgeIndex) {
    switch (edgeIndex) {
    case 0: return p0;
    case 1: return p1;
    case 2: return p2;
    }
    return null;
  }
  
  public boolean isPathComplete() {
    return numPaths() == 3;
  }

  public int numPaths() {
    int num = 0;
    if (p0 != null) num++;
    if (p1 != null) num++;
    if (p2 != null) num++;
    return num;
  }
  
  public int getNonPathEdge() {
    if (p0 == null) return 0;
    if (p1 == null) return 1;
    if (p2 == null) return 2;
    return -1;
  }
  
  public void addInternalLines(List<LineString> lines, GeometryFactory geomFact) {
    //Assert.assertTrue( isPathComplete() );
    if (isLinesAdded) return;
    Coordinate cc = circumcentre();
    if (intersects(cc)) {
      addInternalLines(cc, -1, lines, geomFact);
    }
    else {
      addInternalLinesToEdge(lines, geomFact);
    }
    isLinesAdded = true;
  }
  
  /*
  //--- Using cc int point isn't as good as midpoint
  private void fillEdgePoints(int longEdge, Coordinate cc) {
    if (p0 == null) p0 = medialPoint(0, longEdge, cc);
    if (p1 == null) p1 = medialPoint(1, longEdge, cc);
    if (p2 == null) p2 = medialPoint(2, longEdge, cc);
  }
  
  private Coordinate medialPoint(int edge, int longEdge, Coordinate cc) {
    if (edge != longEdge) {
      return tri.midpoint(edge);
    }
    return intersection(
        tri.getEdgeStart(edge), tri.getEdgeEnd(edge),
        tri.getCoordinate( Tri.oppVertex(edge) ), cc);
  }
  
  private Coordinate intersection(Coordinate p00, Coordinate p01, Coordinate p10, Coordinate p11) {
    LineIntersector li = new RobustLineIntersector();
    li.computeIntersection(p00, p01, p10, p11);
    return li.getIntersection(0);
  }
*/
  
  private void addInternalLinesToEdge(List<LineString> lines, GeometryFactory geomFact) {
    int nodeEdge = longEdge();
    Coordinate nodePt = getPathPoint(nodeEdge);
    addInternalLines(nodePt, nodeEdge, lines, geomFact);
  }
  
  private void addInternalLines(Coordinate p, int skipEdge, List<LineString> lines, GeometryFactory geomFact) {
    if (skipEdge != 0) addLine(p0, p, geomFact, lines);
    if (skipEdge != 1) addLine(p1, p, geomFact, lines);
    if (skipEdge != 2) addLine(p2, p, geomFact, lines);
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
  private int longEdge() {
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
  
  private static void addLine(Coordinate p0, Coordinate p1, 
      GeometryFactory geomFact, List<LineString> lines) {
    LineString line = geomFact.createLineString(new Coordinate[] {
      p0.copy(), p1.copy()
    });
    lines.add(line);
  }
  
}
