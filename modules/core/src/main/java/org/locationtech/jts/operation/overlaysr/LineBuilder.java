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
package org.locationtech.jts.operation.overlaysr;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.algorithm.PointLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geomgraph.Position;
import org.locationtech.jts.topology.Label;

public class LineBuilder {

  private GeometryFactory geometryFactory;
  private OverlayGraph graph;
  private int opCode;

  public LineBuilder(OverlayGraph graph, int opCode, GeometryFactory geomFact, PointLocator ptlocator) {
    this.graph = graph;
    this.opCode = opCode;
    this.geometryFactory = geomFact;
  }

  public List<LineString> getLines() {
    ArrayList<LineString> lines = new ArrayList<LineString>();
    addResultLines(lines);
    return lines;
  }

  private void addResultLines(ArrayList<LineString> lines) {
    List<OverlayEdge> edges = graph.getEdges();
    for (OverlayEdge edge : edges) {
      if (edge.isVisited()) continue;
      if (! isResultLine(edge)) continue;
      
      LineString line = createLine(edge);
      lines.add(line);
      markVisited(edge);
    }
  }

  private LineString createLine(OverlayEdge edge) {
    OverlayEdge forward = edge.isForward() ? edge : edge.symOE();
    Coordinate[] pts = forward.getCoordinates();
    LineString line = geometryFactory.createLineString(pts);
    return line;
  }

  private boolean isResultLine(OverlayEdge edge) {
    if (edge.isInResult() || edge.symOE().isInResult()) return false;
    OverlayLabel lbl = edge.getLabel();
    if (! lbl.isLine()) return false;

    boolean isInResult = OverlaySR.isResultOfOp(
        lbl.getLocation(0,  Position.ON), 
        lbl.getLocation(1,  Position.ON), 
        opCode);
    return isInResult;
  }

  private void markVisited(OverlayEdge edge) {
    edge.setVisited(true);
    edge.symOE().setVisited(true);
  }

}
