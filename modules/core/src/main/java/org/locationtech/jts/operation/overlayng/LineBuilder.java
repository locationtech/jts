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

import org.locationtech.jts.algorithm.PointLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.operation.overlay.OverlayOp;

public class LineBuilder {
  
  private GeometryFactory geometryFactory;
  private OverlayGraph graph;
  private int opCode;
  private int resultAreaIndex;
  private InputGeometry inputGeom;
  private int resultDimension;
  private boolean hasResultArea;

  public LineBuilder(InputGeometry inputGeom, OverlayGraph graph, boolean hasResultArea, int opCode, GeometryFactory geomFact) {
    this.inputGeom = inputGeom;
    this.graph = graph;
    this.opCode = opCode;
    this.geometryFactory = geomFact;
    this.resultAreaIndex = resultAreaIndex(opCode);
    this.hasResultArea = hasResultArea;
    
    resultDimension = OverlayNG.resultDimension(opCode, 
        inputGeom.getGeometry(0), inputGeom.getGeometry(0));
  }

  public List<LineString> getLines() {
    ArrayList<LineString> lines = new ArrayList<LineString>();
    addResultLines(lines);
    return lines;
  }

  private void addResultLines(ArrayList<LineString> lines) {
    Collection<OverlayEdge> edges = graph.getEdges();
    for (OverlayEdge edge : edges) {
      // already included as a line
      if (edge.isVisited()) continue;
      markVisited(edge);
  
      /**
       * Only forward edges need to be considered.
       * This ensures that orientation will be preserved for lines
       */
      if (! edge.isForward()) continue;
      
      if (! isResultLine(edge)) continue;
      
      /**
       * When a line edge is in result, both edges are marked
       */
      edge.markInResultBoth();
      
      LineString line = createLine(edge);
      lines.add(line);
    }
  }
 
  private boolean isResultLine(OverlayEdge edge) {
    /**
     * Skip if edge is already in result,
     * either as a area edge or already as a line
     */
    if (isInResult(edge)) 
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
    
    boolean isInResult = OverlayNG.isResultOfOp(aLoc, bLoc, opCode);
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
  
  private void markVisited(OverlayEdge edge) {
    edge.setVisited(true);
    edge.symOE().setVisited(true);
  }
  
  private LineString createLine(OverlayEdge edge) {
    OverlayEdge forward = edge.isForward() ? edge : edge.symOE();
    Coordinate[] pts = forward.getCoordinates();
    LineString line = geometryFactory.createLineString(pts);
    return line;
  }

  private int resultAreaIndex(int overlayOpCode) {
    int areaIndex = -1;
    if (inputGeom.getDimension(0) == 2) areaIndex = 0;
    if (inputGeom.getDimension(1) == 2) areaIndex = 1;
    
    if (areaIndex < 0) return -1;
    
    switch (overlayOpCode) {
    case OverlayOp.INTERSECTION: return -1;
    case OverlayOp.UNION: return areaIndex;
    case OverlayOp.DIFFERENCE: return (areaIndex <= 0) ? 0 : -1;
    case OverlayOp.SYMDIFFERENCE: return areaIndex;
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

  private static boolean isInResult(OverlayEdge edge) {
    return edge.isInResult() || edge.symOE().isInResult();
  }

  private boolean isInteriorCollapse(int geomIndex, OverlayLabel lbl) {
    return isCollapse(geomIndex, lbl)
        && lbl.getLineLocation(geomIndex) == Location.INTERIOR;
  }

  private boolean isCollapse(int geomIndex, OverlayLabel lbl) {
    return lbl.isCollapse(geomIndex);
  }




}
