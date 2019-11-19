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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.util.Debug;

/**
 * Builds overlay result lines from the overlay graph.
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
class LineStringBuilder {
  
  private GeometryFactory geometryFactory;
  private OverlayGraph graph;
  private int opCode;
  private int resultAreaIndex;
  private InputGeometry inputGeom;
  private int resultDimension;
  private boolean hasResultArea;
  private List<LineString> lines = new ArrayList<LineString>();
  
  public LineStringBuilder(InputGeometry inputGeom, OverlayGraph graph, boolean hasResultArea, int opCode, GeometryFactory geomFact) {
    this.inputGeom = inputGeom;
    this.graph = graph;
    this.opCode = opCode;
    this.geometryFactory = geomFact;
    this.resultAreaIndex = resultAreaIndex(opCode);
    this.hasResultArea = hasResultArea;
    
    resultDimension = OverlayUtil.resultDimension(opCode, 
        inputGeom.getDimension(0), inputGeom.getDimension(0));
  }

  public List<LineString> getLines() {
    markResultLines();
    addResultLines();
    return lines;
  }

  private void markResultLines() {
    Collection<OverlayEdge> edges = graph.getEdges();
    for (OverlayEdge edge : edges) {
      if (edge.isInResultLine()) 
        continue;
      if (isResultLine(edge)) {
        edge.markInResultLine();
        //Debug.println(edge);
      }
    }
  }
  
  private void addResultLines() {
    addResultLinesAtNodes();
    addResultLinesRings();
  }
  
  /**
   * To implement a strategy preserving input lines,
   * the label must carry an id for each input LineString.
   * The ids are zeroed out whenever two input edges are merged.
   * Additional result nodes are created where there are changes in id
   * at degree-2 nodes.
   * (degree>=3 nodes must be kept as nodes to ensure 
   * output linework is fully noded.
   */
  
  private void addResultLinesAtNodes() {
    Collection<OverlayEdge> edges = graph.getEdges();
    for (OverlayEdge edge : edges) {
      if (! edge.isInResultLine()) continue;
      if (edge.isVisited()) continue;
      
      /**
       * Choose start point as a node.
       * Nodes are degree-1 or degree>=3 edges.
       * 
       * This will find all lines originating at nodes
       */
      if (degreeLine(edge) != 2) {
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
      if (degreeLine(e.symOE()) != 2) {
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
  private OverlayEdge nextLineEdgeUnvisited(OverlayEdge node) {
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
   * @return degree of the node
   */
  private int degreeLine(OverlayEdge node) {
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
  
  private boolean isResultLine(OverlayEdge edge) {
    /**
     * Skip if edge is already in result,
     * either as a area edge or already as a line
     */
    if (isInResultArea(edge)) 
      return false;
    
    OverlayLabel lbl = edge.getLabel();
    boolean isEffectiveLine = lbl.isLine() || lbl.isBoundaryBoth();
    if (! isEffectiveLine) 
      return false;
    
    /**
     * Skip edges inside result area
     */
    if (hasResultArea && isCoveredByResultArea(edge)) 
      return false;
    
    /**
     * Interior collapsed edges are discarded, 
     * since they will be covered by the result area
     */
    if (isInteriorCollapse(0, lbl)) return false;
    if (isInteriorCollapse(1, lbl)) return false;
    
    int aLoc = effectiveLocation(0, lbl);
    int bLoc = effectiveLocation(1, lbl);
    
    boolean isInResult = OverlayNG.isResultOfOp(opCode, aLoc, bLoc);
    return isInResult;
  }

  /**
   * Tests whether a line edge is covered by the result area (if any).
   * In this case the line will not be added to the result.
   * 
   * @param lbl the line label
   * @return true if the edge lies in the interior of the result area
   */
  private boolean isCoveredByResultArea(OverlayEdge edge) {
    /**
     * If result is not an area, edge can't be covered
     */
    if (resultDimension < 2) return false;
    /**
     * If no inputs are areas, edge can't be covered
     */
    if (resultAreaIndex < 0) return false;
    
    OverlayLabel lbl = edge.getLabel();
    
    //TODO: handle situation when line collapse is an isolated line inside parent geom
    /**
     * It can happen that both inputs are areas, and there end up
     * being collapsed L edges isolated inside the result area. 
     * 
     */

    // TODO: does this need to be computed using the actual result area?
    
    boolean isCovered = lbl.isInArea(resultAreaIndex);
    return isCovered;
  }

  private int resultAreaIndex(int overlayOpCode) {
    int areaIndex = -1;
    if (inputGeom.getDimension(0) == 2) areaIndex = 0;
    if (inputGeom.getDimension(1) == 2) areaIndex = 1;
    
    if (areaIndex < 0) return -1;
    
    switch (overlayOpCode) {
    case OverlayNG.INTERSECTION: return -1;
    case OverlayNG.UNION: return areaIndex;
    case OverlayNG.DIFFERENCE: return (areaIndex <= 0) ? 0 : -1;
    case OverlayNG.SYMDIFFERENCE: return areaIndex;
    }
    return -1;
  }
  
  /**
   * Determines the effective location for this line,
   * forcing collapses to be considered as INTERIOR
   * so they will be included in the result.
   * 
   * @param geomIndex index of parent geometry
   * @param lbl label of line
   * @return the effective location of the line
   */
  private int effectiveLocation(int geomIndex, OverlayLabel lbl) {
    if (isCollapse(geomIndex, lbl))
      return Location.INTERIOR;
    if (inputGeom.isLine(geomIndex) && lbl.isLine(geomIndex))
      return Location.INTERIOR;
    return lbl.getLineLocation(geomIndex);
  }

  private static boolean isInResultArea(OverlayEdge edge) {
    return edge.isInResultArea() || edge.symOE().isInResultArea();
  }

  private boolean isInteriorCollapse(int geomIndex, OverlayLabel lbl) {
    return isCollapse(geomIndex, lbl)
        && lbl.getLineLocation(geomIndex) == Location.INTERIOR;
  }

  private boolean isCollapse(int geomIndex, OverlayLabel lbl) {
    return lbl.isCollapse(geomIndex);
  }




}
