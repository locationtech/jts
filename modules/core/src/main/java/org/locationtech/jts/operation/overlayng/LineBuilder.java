/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;

/**
 * Finds and builds overlay result lines from the overlay graph.
 * Output linework has the following semantics:
 * <ol>
 * <li>Linework is fully noded</li>
 * <li>Nodes in the input are preserved in the output</li>
 * <li>Output may contain more nodes than in the input (in particular, 
 * sequences of coincident line segments are noded at each vertex</li>
 * </ol>
 * 
 * Various strategies are possible for how to 
 * merge graph edges into lines.
 * <ul>
 * <li>This implementation uses the simplest approach of
 * maintaining all nodes arising from noding (which includes
 * all nodes in the input, and possibly others).
 * This matches the current JTS overlay output semantics.</li>
 * <li>Another option is to fully merge output lines
 * from node to node.
 * For rings a node point is chosen arbitrarily.
 * It would also be possible to output LinearRings, 
 * if the input is a LinearRing and is unchanged.
 * This will require additional info from the input linework.</li>
 * </ul>
 * 
 * @author Martin Davis
 *
 */
class LineBuilder {
  
  private GeometryFactory geometryFactory;
  private OverlayGraph graph;
  private int opCode;
  private int inputAreaIndex;
  private boolean hasResultArea;
  
  /**
   * Indicates whether intersections are allowed to produce
   * heterogeneous results including proper boundary touches. 
   * This does not control inclusion of touches along collapses.
   * True provides the original JTS semantics.
   */
  private boolean isAllowMixedResult = ! OverlayNG.STRICT_MODE_DEFAULT;
  
  /**
   * Allow lines created by area topology collapses
   * to appear in the result.
   * True provides the original JTS semantics.
   */
  private boolean isAllowCollapseLines = ! OverlayNG.STRICT_MODE_DEFAULT;

  
  private List<LineString> lines = new ArrayList<LineString>();
  
  /**
   * Creates a builder for linear elements which may be present 
   * in the overlay result.
   * 
   * @param inputGeom the input geometries
   * @param graph the topology graph
   * @param hasResultArea true if an area has been generated for the result
   * @param opCode the overlay operation code
   * @param geomFact the output geometry factory
   */
  public LineBuilder(InputGeometry inputGeom, OverlayGraph graph, boolean hasResultArea, int opCode, GeometryFactory geomFact) {
    this.graph = graph;
    this.opCode = opCode;
    this.geometryFactory = geomFact;
    this.hasResultArea = hasResultArea;
    inputAreaIndex = inputGeom.getAreaIndex();
  }

  public void setStrictMode(boolean isStrictResultMode) {
    isAllowCollapseLines = ! isStrictResultMode;
    isAllowMixedResult = ! isStrictResultMode;
  }
  
  public List<LineString> getLines() {
    markResultLines();
    addResultLines();
    return lines;
  }

  private void markResultLines() {
    Collection<OverlayEdge> edges = graph.getEdges();
    for (OverlayEdge edge : edges) {
      /**
       * If the edge linework is already marked as in the result,
       * it is not included as a line.
       * This occurs when an edge either is in a result area
       * or has already been included as a line.
       */
      if (edge.isInResultEither()) 
        continue;
      if (isResultLine(edge.getLabel())) {
        edge.markInResultLine();
        //Debug.println(edge);
      }
    }
  }
  
  /**
   * Checks if the topology indicated by an edge label
   * determines that this edge should be part of a result line.
   * <p>
   * Note that the logic here relies on the semantic
   * that for intersection lines are only returned if
   * there is no result area components.
   * 
   * @param lbl the label for an edge
   * @return true if the edge should be included in the result
   */
  private boolean isResultLine(OverlayLabel lbl) {
    /**
     * Omit edge which is a boundary of a single geometry
     * (i.e. not a collapse or line edge as well).
     * These are only included if part of a result area.
     * This is a short-circuit for the most common area edge case
     */
    if (lbl.isBoundarySingleton()) return false;
    
    /**
     * Omit edge which is a collapse along a boundary.
     * I.e a result line edge must be from a input line
     * OR two coincident area boundaries.
     * 
     * This logic is only used if not including collapse lines in result.
     */
    if (! isAllowCollapseLines 
        && lbl.isBoundaryCollapse()) return false;

    /**
     * Omit edge which is a collapse interior to its parent area.
     * (E.g. a narrow gore, or spike off a hole)
     */
    if (lbl.isInteriorCollapse()) return false;
    
    /**
     * For ops other than Intersection, omit a line edge
     * if it is interior to the other area.
     * 
     * For Intersection, a line edge interior to an area is included.
     */
    if (opCode != OverlayNG.INTERSECTION) {
      /**
       * Omit collapsed edge in other area interior.
       */
      if (lbl.isCollapseAndNotPartInterior()) return false;

      /**
       * If there is a result area, omit line edge inside it.
       * It is sufficient to check against the input area rather 
       * than the result area, 
       * because if line edges are present then there is only one input area, 
       * and the result area must be the same as the input area. 
       */
      if (hasResultArea && lbl.isLineInArea(inputAreaIndex)) 
        return false;
    }
    
    /**
     * Include line edge formed by touching area boundaries,
     * if enabled.
     */
    if (isAllowMixedResult 
        && opCode == OverlayNG.INTERSECTION && lbl.isBoundaryTouch()) {
      return true;
    }
    
    /**
     * Finally, determine included line edge
     * according to overlay op boolean logic.
     */
    int aLoc = effectiveLocation(lbl, 0);
    int bLoc = effectiveLocation(lbl, 1);
    boolean isInResult = OverlayNG.isResultOfOp(opCode, aLoc, bLoc);
    return isInResult;
  }
  
  /**
   * Determines the effective location for a line,
   * for the purpose of overlay operation evaluation.
   * Line edges and Collapses are reported as INTERIOR
   * so they may be included in the result
   * if warranted by the effect of the operation on the two edges.
   * (For instance, the intersection of a line edge and a collapsed boundary
   * is included in the result).
   * 
   * @param lbl label of line
   * @param geomIndex index of input geometry
   * 
   * @return the effective location of the line
   */
  private static int effectiveLocation(OverlayLabel lbl, int geomIndex) {
    if (lbl.isCollapse(geomIndex))
      return Location.INTERIOR;
    if (lbl.isLine(geomIndex))
      return Location.INTERIOR;
    return lbl.getLineLocation(geomIndex);
  }
  
  private void addResultLines() {
    Collection<OverlayEdge> edges = graph.getEdges();
    for (OverlayEdge edge : edges) {
      if (! edge.isInResultLine()) continue;
      if (edge.isVisited()) continue;
      
      lines.add( toLine( edge ));
      edge.markVisitedBoth();
    }
  }
  
  private LineString toLine(OverlayEdge edge) {
    boolean isForward = edge.isForward();
    CoordinateList pts = new CoordinateList();
    pts.add(edge.orig(), false);
    edge.addCoordinates(pts);
    
    Coordinate[] ptsOut = pts.toCoordinateArray(isForward);
    LineString line = geometryFactory.createLineString(ptsOut);
    return line;
  }
  
  //-----------------------------------------------
  //----  Maximal line extraction logic
  //-----------------------------------------------
  /**
   * NOT USED currently.
   * Instead the raw noded edges are output.
   * This matches the original overlay semantics.
   * It is also faster.
   */
  /// FUTURE: enable merging via an option switch on OverlayNG
  
  private void addResultLinesMerged() {
    addResultLinesForNodes();
    addResultLinesRings();
  }
  
  private void addResultLinesForNodes() {
    Collection<OverlayEdge> edges = graph.getEdges();
    for (OverlayEdge edge : edges) {
      if (! edge.isInResultLine()) continue;
      if (edge.isVisited()) continue;
      
      /**
       * Choose line start point as a node.
       * Nodes in the line graph are degree-1 or degree >= 3 edges.
       * 
       * This will find all lines originating at nodes
       */
      if (degreeOfLines(edge) != 2) {
        lines.add( buildLine( edge ));
        //Debug.println(edge);
      }
    }
  }
 
  /**
   * Adds lines which form rings (i.e. have only degree-2 vertices).
   */
  private void addResultLinesRings() {
    // TODO: an ordering could be imposed on the endpoints to make this more repeatable
    
    // TODO: preserve input LinearRings if possible?  Would require marking them as such
    Collection<OverlayEdge> edges = graph.getEdges();
    for (OverlayEdge edge : edges) {
      if (! edge.isInResultLine()) continue;
      if (edge.isVisited()) continue;
      
      lines.add( buildLine( edge ));
      //Debug.println(edge);
    }
  }
 
  /**
   * Traverses edges from edgeStart which
   * lie in a single line (have degree = 2).
   * 
   * The direction of the linework is preserved as far as possible.
   * Specifically, the direction of the line is determined 
   * by the start edge direction. This implies
   * that if all edges are reversed, the created line
   * will be reversed to match.
   * This ensures the orientation of linework is faithful to the input
   * in the case of polygon-line overlay.
   * However, this does not provide a consistent orientation 
   * in the case of line-line intersection(where A and B might have different orientations).
   * (Other more complex strategies would be possible.
   * E.g. using the direction of the majority of segments,
   * or preferring the direction of the A edges.)
   * 
   * @param node
   * @return 
   */
  private LineString buildLine(OverlayEdge node) {
    CoordinateList pts = new CoordinateList();
    pts.add(node.orig(), false);
    
    boolean isForward = node.isForward();
    
    OverlayEdge e = node;
    do {
      e.markVisitedBoth();
      e.addCoordinates(pts);
      
      // end line if next vertex is a node
      if (degreeOfLines(e.symOE()) != 2) {
        break;
      }
      e = nextLineEdgeUnvisited(e.symOE());
      // e will be null if next edge has been visited, which indicates a ring
    }
    while (e != null);
    
    Coordinate[] ptsOut = pts.toCoordinateArray(isForward);
    
    LineString line = geometryFactory.createLineString(ptsOut);
    return line;
  }

  /**
   * Finds the next edge around a node which forms
   * part of a result line.
   * 
   * @param node a line edge originating at the node to be scanned
   * @return the next line edge, or null if there is none
   */
  private static OverlayEdge nextLineEdgeUnvisited(OverlayEdge node) {
    OverlayEdge e = node;
    do {
      e = e.oNextOE();
      if (e.isVisited()) continue;
      if (e.isInResultLine()) {
        return e;
      }
    } while (e != node);
    return null;
  }

  /**
   * Computes the degree of the line edges incident on a node
   * @param node node to compute degree for
   * @return degree of the node line edges
   */
  private static int degreeOfLines(OverlayEdge node) {
    int degree = 0;
    OverlayEdge e = node;
    do {
      if (e.isInResultLine()) {
        degree++;
      }
      e = e.oNextOE();
    } while (e != node);
    return degree;
  }

}