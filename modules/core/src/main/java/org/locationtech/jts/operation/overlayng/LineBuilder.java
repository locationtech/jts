/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.util.Debug;

/**
 * Finds and builds overlay result lines from the overlay graph.
 * Output linework has the following semantics:
 * <ol>
 * <li>Linework is fully noded
 * <li>Lines are as long as possible between nodes
 * </ol>
 * 
 * Various strategies are possible for how to 
 * merge graph edges into lines.
 * This implementation uses the approach
 * of having output lines run contiguously from node to node.
 * For rings a node point is chosen arbitrarily.
 * <p>
 * Another possible strategy would be to preserve input linework 
 * as far as possible (i.e. any sections of input lines which are not 
 * coincident with other linework would be preserved).
 * <p>
 * It would also be possible to output LinearRings, 
 * if the input is a LinearRing and is unchanged.
 * This will require additional info from the input linework.
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

  public List<LineString> getLines() {
    markResultLines();
    addResultLines();
    return lines;
  }

  private void markResultLines() {
    Collection<OverlayEdge> edges = graph.getEdges();
    for (OverlayEdge edge : edges) {
      if (isInResult(edge)) 
        continue;
      if (isResultLine(edge.getLabel())) {
        edge.markInResultLine();
        //Debug.println(edge);
      }
    }
  }
  
  /**
   * If the edge linework is already in the result, 
   * this edge does not need to be included as a line.
   * 
   * @param edge an edge of the topology graph
   * @return true if the edge linework is already in the result
   */
  private static boolean isInResult(OverlayEdge edge) {
    return edge.isInResult() || edge.symOE().isInResult();
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
     * Edges which are just collapses along boundaries
     * are not output.
     * In other words, an edge must be from a source line
     * or two (coincident) area boundaries.
     */
    if (lbl.isBoundaryCollapse()) return false;
    
    /**
     * Skip edges that are inside result area, if there is one.
     * It is sufficient to check against an input area rather 
     * than the result area, since 
     * if lines are being included then the result area
     * must be the same as the input area. 
     * This logic relies on the semantic that if both inputs 
     * are areas, lines are only output if there is no 
     * result area.
     */
    if (hasResultArea && lbl.isLineInArea(inputAreaIndex)) 
      return false;
    
    int aLoc = effectiveLocation(0, lbl);
    int bLoc = effectiveLocation(1, lbl);
    
    boolean isInResult = OverlayNG.isResultOfOp(opCode, aLoc, bLoc);
    return isInResult;
  }
  
  /**
   * Determines the effective location for a line,
   * for the purpose of overlay operation evaluation.
   * Line edges and Collapses are reported as INTERIOR
   * so they may be included in the result
   * if warranted by the effect of the operation
   * on the two edges.
   * (For instance, the intersection of line edge and a collapsed boundary
   * is included in the result).
   * 
   * @param geomIndex index of parent geometry
   * @param lbl label of line
   * @return the effective location of the line
   */
  private static int effectiveLocation(int geomIndex, OverlayLabel lbl) {
    if (lbl.isCollapse(geomIndex))
      return Location.INTERIOR;
    if (lbl.isLine(geomIndex))
      return Location.INTERIOR;
    return lbl.getLineLocation(geomIndex);
  }

  //----  Maximal line extraction methods
  
  private void addResultLines() {
    addResultLinesForNodes();
    addResultLinesRings();
  }
  
  /**
   * FUTURE: To implement a strategy preserving input lines,
   * the label must carry an id for each input LineString.
   * The ids are zeroed out whenever two input edges are merged.
   * Additional result nodes are created where there are changes in id
   * at degree-2 nodes.
   * (degree>=3 nodes must be kept as nodes to ensure 
   * output linework is fully noded.
   */
  
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
   * (Other more complex strategies would be possible.
   * E.g. using the direction of the majority of segments,
   * or preferring the direction of the A edges.)
   * 
   * @param node
   * @return 
   */
  private LineString buildLine(OverlayEdge node) {
    // assert: edgeStart degree = 1
    // assert: edgeStart direction = forward
    CoordinateList pts = new CoordinateList();
    pts.add(node.orig(), false);
    
    boolean isForward = node.isForward();
    
    OverlayEdge e = node;
    do {
      e.markVisitedBoth();
      pts.add(e.dest(), false);
      
      // end line if next vertex is a node
      if (degreeOfLines(e.symOE()) != 2) {
        break;
      }
      e = nextLineEdgeUnvisited(e.symOE());
      // e will be null if next edge has been visited, which indicates a ring
    }
    while (e != null);
    
    // add final point of line, if not a ring
    if (e != null)
      pts.add(e.dest(), false);
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