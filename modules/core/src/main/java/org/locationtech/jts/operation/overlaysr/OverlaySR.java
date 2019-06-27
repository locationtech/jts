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
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.algorithm.PointLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geomgraph.Label;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.util.Debug;

public class OverlaySR {
  /**
   * Tests whether a point with a given topological {@link Label}
   * relative to two geometries is contained in 
   * the result of overlaying the geometries using
   * a given overlay operation.
   * <p>
   * The method handles arguments of {@link Location#NONE} correctly
   * 
   * @param label the topological label of the point
   * @param opCode the code for the overlay operation to test
   * @return true if the label locations correspond to the overlayOpCode
   */
  public static boolean isResultOfOp(OverlayLabel label, int opCode)
  {
    int loc0 = label.getLocation(0);
    int loc1 = label.getLocation(1);
    return isResultOfOp(loc0, loc1, opCode);
  }

  /**
   * Tests whether a point with given {@link Location}s
   * relative to two geometries is contained in 
   * the result of overlaying the geometries using
   * a given overlay operation.
   * <p>
   * The method handles arguments of {@link Location#NONE} correctly
   *
   * @param loc0 the code for the location in the first geometry 
   * @param loc1 the code for the location in the second geometry 
   * @param overlayOpCode the code for the overlay operation to test
   * @return true if the locations correspond to the overlayOpCode
   */
  public static boolean isResultOfOp(int loc0, int loc1, int overlayOpCode)
  {
    if (loc0 == Location.BOUNDARY) loc0 = Location.INTERIOR;
    if (loc1 == Location.BOUNDARY) loc1 = Location.INTERIOR;
    switch (overlayOpCode) {
    case OverlayOp.INTERSECTION:
      return loc0 == Location.INTERIOR
          && loc1 == Location.INTERIOR;
    case OverlayOp.UNION:
      return loc0 == Location.INTERIOR
          || loc1 == Location.INTERIOR;
    case OverlayOp.DIFFERENCE:
      return loc0 == Location.INTERIOR
          && loc1 != Location.INTERIOR;
    case OverlayOp.SYMDIFFERENCE:
      return   (     loc0 == Location.INTERIOR &&  loc1 != Location.INTERIOR)
            || (     loc0 != Location.INTERIOR &&  loc1 == Location.INTERIOR);
    }
    return false;
  }
  
  /**
   * Computes an overlay operation for 
   * the given geometry arguments.
   * 
   * @param geom0 the first geometry argument
   * @param geom1 the second geometry argument
   * @param opCode the code for the desired overlay operation
   * @return the result of the overlay operation
   */
  public static Geometry overlay(Geometry geom0, Geometry geom1, PrecisionModel pm, int opCode)
  {
    OverlaySR gov = new OverlaySR(geom0, geom1, pm, opCode);
    Geometry geomOv = gov.getResultGeometry();
    return geomOv;
  }
  
  private static final int DIM_UNKNOWN = -1;
  private static final int DIM_LINE = Dimension.L;
  private static final int DIM_AREA = Dimension.A;

  private static final PointLocator ptLocator = new PointLocator();
  private Geometry[] geom;
  private GeometryFactory geomFact;
  private PrecisionModel pm;
  private boolean isOutputEdges;
  private boolean isOutputResultEdges;
  private OverlayGraph graph;
  private int opCode;

  public OverlaySR(Geometry geom0, Geometry geom1, PrecisionModel pm, int opCode) {
    this.pm = pm;
    this.opCode = opCode;
    geomFact = geom0.getFactory();
    geom = new Geometry[] { geom0, geom1 };
  }  
  
  public void setOutputEdges(boolean isOutputEdges ) {
    this.isOutputEdges = isOutputEdges;
  }
  
  public void setOutputResultEdges(boolean isOutputResultEdges ) {
    this.isOutputResultEdges = isOutputResultEdges;
  }
  
  public Geometry getResultGeometry() {
    Geometry resultGeom = computeOverlay();
    return resultGeom;
  }


  private int dimension(int geomIndex) {
    // TODO: any edge cases that need to be handled?
    return geom[geomIndex].getDimension();
  }

  private int locatePoint(Coordinate pt, int geomIndex) {
    // TODO: use indexed locator here?
    return ptLocator.locate(pt, geom[geomIndex]);
  }
  
  private Geometry computeOverlay() {
    Collection<SegmentString> edges = node();
    Collection<SegmentString> edgesMerged = merge(edges);
    graph = buildTopology(edgesMerged);
    graph.markResultAreaEdges(opCode);
    graph.cancelDuplicateResultAreaEdges();
    List<OverlayEdge> resultAreaEdges = graph.getResultAreaEdges();
    graph.linkResultAreaEdges(resultAreaEdges);
    
    //return toLines(edges, geomFact );
    if (isOutputEdges || isOutputResultEdges) {
      return toLines(graph, geomFact);
    }
    
    return createResult(opCode, resultAreaEdges);
  }

  private Geometry createResult(int opCode, List<OverlayEdge> resultAreaEdges) {
    PolygonBuilder polyBuilder = new PolygonBuilder(resultAreaEdges, geomFact);
    List<Polygon> resultPolyList = polyBuilder.getPolygons();
    
    LineBuilder lineBuilder = new LineBuilder(graph, opCode, geomFact, ptLocator);
    List<LineString> resultLineList = lineBuilder.getLines();

    //List<LineString> resultLineList = new ArrayList<LineString>();
    List<Point> resultPointList = new ArrayList<Point>();
    // gather the results from all calculations into a single Geometry for the result set
    Geometry resultGeom = buildGeometry(resultPointList, resultLineList, resultPolyList, opCode);
    return resultGeom;
  }

  private Geometry buildGeometry(List<Point> resultPointList, List<LineString> resultLineList, List<Polygon> resultPolyList, int opcode) {
    List<Geometry> geomList = new ArrayList<Geometry>();
    // element geometries of the result are always in the order P,L,A
    geomList.addAll(resultPointList);
    geomList.addAll(resultLineList);
    geomList.addAll(resultPolyList);

    if ( geomList.isEmpty() )
      return createEmptyResult(opcode, geom[0], geom[1], geomFact);

    // build the most specific geometry possible
    return geomFact.buildGeometry(geomList);
  }

  
  /**
   * Creates an empty result geometry of the appropriate dimension,
   * based on the given overlay operation and the dimensions of the inputs.
   * The created geometry is always an atomic geometry, 
   * not a collection.
   * <p>
   * The empty result is constructed using the following rules:
   * <ul>
   * <li>{@link #INTERSECTION} - result has the dimension of the lowest input dimension
   * <li>{@link #UNION} - result has the dimension of the highest input dimension
   * <li>{@link #DIFFERENCE} - result has the dimension of the left-hand input
   * <li>{@link #SYMDIFFERENCE} - result has the dimension of the highest input dimension
   * (since the symmetric Difference is the union of the differences).
   * </ul>
   * 
   * @param overlayOpCode the code for the overlay operation being performed
   * @param a an input geometry
   * @param b an input geometry
   * @param geomFact the geometry factory being used for the operation
   * @return an empty atomic geometry of the appropriate dimension
   */
  public static Geometry createEmptyResult(int overlayOpCode, Geometry a, Geometry b, GeometryFactory geomFact)
  {
    Geometry result = null;
    switch (resultDimension(overlayOpCode, a, b)) {
    case -1:
      result = geomFact.createGeometryCollection();
      break;
    case 0:
      result =  geomFact.createPoint();
      break;
    case 1:
      result =  geomFact.createLineString();
      break;
    case 2:
      result =  geomFact.createPolygon();
      break;
    }
    return result;
  }
  
  private static int resultDimension(int opCode, Geometry g0, Geometry g1)
  {
    int dim0 = g0.getDimension();
    int dim1 = g1.getDimension();
    
    int resultDimension = -1;
    switch (opCode) {
    case OverlayOp.INTERSECTION: 
      resultDimension = Math.min(dim0, dim1);
      break;
    case OverlayOp.UNION: 
      resultDimension = Math.max(dim0, dim1);
      break;
    case OverlayOp.DIFFERENCE: 
      resultDimension = dim0;
      break;
    case OverlayOp.SYMDIFFERENCE: 
      /**
       * This result is chosen because
       * <pre>
       * SymDiff = Union(Diff(A, B), Diff(B, A)
       * </pre>
       * and Union has the dimension of the highest-dimension argument.
       */
      resultDimension = Math.max(dim0, dim1);
      break;
    }
    return resultDimension;
  }
  
  
  private Collection<SegmentString> node() {
    OverlaySRNoder noder = new OverlaySRNoder(pm);
    noder.add(geom[0], 0);
    noder.add(geom[1], 1);
    Collection<SegmentString> edges = noder.node();
    Collection<SegmentString> mergedEdges = merge(edges);
    return mergedEdges;
  }
  
  private Collection<SegmentString> merge(Collection<SegmentString> edges) {
    // TODO implement merging here
    
    //computeLabelsFromDepths();
    //replaceCollapsedEdges();
    
    return edges;
  }

  private OverlayGraph buildTopology(Collection<SegmentString> edges) {
    OverlayGraph graph = OverlayGraph.buildGraph( edges );
    graph.computeLabelling();
    labelIncompleteNodes(graph.getNodeEdges());
    return graph;
  }

  private void labelIncompleteNodes(Collection<OverlayEdge> collection) {
    for (OverlayEdge edge : collection) {
      if (edge.getLabel().isUnknown(0)) {
        labelIncompleteNode(edge, 0);
      }
      if (edge.getLabel().isUnknown(1)) {
        labelIncompleteNode(edge, 1);
      }
    }
  }

  private void labelIncompleteNode(OverlayEdge edge, int geomIndex) {
    Debug.println("\n------  labelIsolatedNode ");
    Debug.print("BEFORE: " + edge.toStringNode());
    int loc = locatePoint(edge.orig(), geomIndex);
    if (DIM_LINE == dimension(geomIndex)) {
      edge.getLabel().setLocationLine(geomIndex, loc);
    }
    else { // assume DIM_AREA
      edge.getLabel().setLocationArea(geomIndex, loc, loc);
    }
    edge.mergeSymLabels();
    Debug.print("AFTER: " + edge.toStringNode());
  }

  
  private Geometry toLines(OverlayGraph graph, GeometryFactory geomFact) {
    List lines = new ArrayList();
    for (OverlayEdge edge : graph.getEdges()) {
      boolean includeEdge = isOutputEdges || edge.isInResult();
      if (! includeEdge) continue;
      //Coordinate[] pts = getCoords(nss);
      Coordinate[] pts = edge.getCoordinatesOriented();
      LineString line = geomFact.createLineString(pts);
      line.setUserData(edge.getLabel().toString()
          + (edge.isInResult() ? " Res" : "") );
      lines.add(line);
    }
    return geomFact.buildGeometry(lines);
  }

}
