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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geomgraph.Label;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.util.Debug;

public class OverlayNG 
{
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
  public static boolean isResultOfOpPoint(OverlayLabel label, int opCode)
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
    OverlayNG ov = new OverlayNG(geom0, geom1, pm, opCode);
    Geometry geomOv = ov.getResultGeometry();
    return geomOv;
  }

  public static Geometry overlay(Geometry geom0, Geometry geom1, PrecisionModel pm, Noder noder, int opCode)
  {
    OverlayNG ov = new OverlayNG(geom0, geom1, pm, opCode);
    ov.setNoder(noder);
    Geometry geomOv = ov.getResultGeometry();
    return geomOv;
  }

  private InputGeometry inputGeom;
  private GeometryFactory geomFact;
  private PrecisionModel pm;
  private boolean isOutputEdges;
  private boolean isOutputResultEdges;
  private OverlayGraph graph;
  private int opCode;
  private boolean isOutputNodedEdges;
  private Noder noder;

  public OverlayNG(Geometry geom0, Geometry geom1, PrecisionModel pm, int opCode) {
    this.pm = pm;
    this.opCode = opCode;
    geomFact = geom0.getFactory();
    inputGeom = new InputGeometry( geom0, geom1 );
  }  
  
  public void setOutputEdges(boolean isOutputEdges ) {
    this.isOutputEdges = isOutputEdges;
  }
  
  public void setOutputNodedEdges(boolean isOutputNodedEdges ) {
    this.isOutputEdges = true;
    this.isOutputNodedEdges = isOutputNodedEdges;
  }
  
  public void setOutputResultEdges(boolean isOutputResultEdges ) {
    this.isOutputResultEdges = isOutputResultEdges;
  }
  
  public void setNoder(Noder noder) {
    this.noder = noder;
  }
  
  public Geometry getResultGeometry() {
    Geometry resultGeom = computeOverlay();
    return resultGeom;
  }

  /*
  private int dimension(int geomIndex) {
    // TODO: any edge cases that need to be handled?
    return geom[geomIndex].getDimension();
  }
*/
  
  private Geometry computeOverlay() {
    
    //--- Noding phase
    List<Edge> edges = nodeAndMerge();
    
    //--- Topology building phase
    graph = new OverlayGraph( edges );
    
    if (isOutputNodedEdges) {
      return toLines(graph, geomFact);
    }

    graph.computeLabelling(inputGeom);
    
    graph.markResultAreaEdges(opCode);
    graph.removeDuplicateResultAreaEdges();
    
    if (isOutputEdges || isOutputResultEdges) {
      return toLines(graph, geomFact);
    }
    
    Geometry result = createResult(opCode);
    // only enable for debugging
    //checkSanity(result);
    return result;
  }

  private void checkSanity(Geometry result) {
    // for Union, area should be greater than largest of inputs
    double areaA = inputGeom.getGeometry(0).getArea();
    double areaB = inputGeom.getGeometry(1).getArea();
    double area = result.getArea();
    
    // if result is empty probably had a complete collapse, so can't use this check
    if (area == 0) return;
    
    if (opCode == OverlayOp.UNION) {
      double minAreaLimit = 0.5 * Math.max(areaA, areaB);
      if (area < minAreaLimit ) {
        throw new TopologyException("Result area sanity issue");
      }
    }
    
  }

  private List<Edge> nodeAndMerge() {
    /**
     * Node the edges, using whatever noder is being used
     */
    OverlayNoder ovNoder = new OverlayNoder(
        inputGeom.getGeometry(0),
        inputGeom.getGeometry(1),
        pm);
    if (noder != null) ovNoder.setNoder(noder);
    Collection<SegmentString> nodedSegStrings = ovNoder.node();
    
    /**
     * Record if there are no edges for either input geometry.
     * This is used to avoid checking disconnected edges
     * against geometry which has collapsed completely.
     */
    inputGeom.setEdgesExist(0, ovNoder.hasEdgesFor(0));
    inputGeom.setEdgesExist(1, ovNoder.hasEdgesFor(1));
    
    /**
     * Merge the noded edges to eliminate duplicates.
     * Labels will be combined.
     */
    // nodedSegStrings are no longer needed, and will be GCed
    List<Edge> edges = createEdges(nodedSegStrings);
    List<Edge> mergedEdges = EdgeMerger.merge(edges);
    return mergedEdges;
  }

  /*
  private List<Edge> mergeEdges(Collection<SegmentString> nodedSegStrings) {
    List<Edge> edges = createEdges(nodedSegStrings);
    List<Edge> mergedEdges = EdgeMerger.merge(edges);
    return mergedEdges;
  }
*/
  private static List<Edge> createEdges(Collection<SegmentString> segStrings) {
    List<Edge> edges = new ArrayList<Edge>();
    for (SegmentString ss : segStrings) {
      Coordinate[] pts = ss.getCoordinates();
      
      // don't create edges from collapsed lines
      // TODO: perhaps convert these to points to be included in overlay?
      if (! Edge.isValidPoints(pts)) continue;
      
      EdgeInfo info = (EdgeInfo) ss.getData();
      edges.add(new Edge(ss.getCoordinates(), info));
    }
    return edges;
  }

  private Geometry toLines(OverlayGraph graph, GeometryFactory geomFact) {
    List<LineString> lines = new ArrayList<LineString>();
    for (OverlayEdge edge : graph.getEdges()) {
      boolean includeEdge = isOutputEdges || edge.isInResult();
      if (! includeEdge) continue;
      //Coordinate[] pts = getCoords(nss);
      Coordinate[] pts = edge.getCoordinatesOriented();
      LineString line = geomFact.createLineString(pts);
      line.setUserData(labelForResult(edge) );
      lines.add(line);
    }
    return geomFact.buildGeometry(lines);
  }

  private String labelForResult(OverlayEdge edge) {
    return edge.getLabel().toString(edge.isForward())
        + (edge.isInResult() ? " Res" : "");
  }

  private Geometry createResult(int opCode) {
    
    //--- Build polygons
    List<OverlayEdge> resultAreaEdges = graph.getResultAreaEdges();
    PolygonBuilder polyBuilder = new PolygonBuilder(resultAreaEdges, geomFact);
    List<Polygon> resultPolyList = polyBuilder.getPolygons();
    boolean hasResultArea = resultPolyList.size() > 0;
    
    //--- Build lines
    LineStringBuilder lineBuilder = new LineStringBuilder(inputGeom, graph, hasResultArea, opCode, geomFact);
    List<LineString> resultLineList = lineBuilder.getLines();

    //--- Build points
    List<Point> resultPointList = new ArrayList<Point>();
    
    Geometry resultGeom = buildResultGeometry(opCode, resultPolyList, resultLineList, resultPointList);
    return resultGeom;
  }

  private Geometry buildResultGeometry(int opcode, List<Polygon> resultPolyList, List<LineString> resultLineList, List<Point> resultPointList) {
    List<Geometry> geomList = new ArrayList<Geometry>();
    // element geometries of the result are always in the order P,L,A
    geomList.addAll(resultPolyList);
    geomList.addAll(resultLineList);
    geomList.addAll(resultPointList);

    if ( geomList.isEmpty() )
      return createEmptyResult(opcode, inputGeom.getGeometry(0), inputGeom.getGeometry(1), geomFact);

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
  
  public static int resultDimension(int opCode, Geometry g0, Geometry g1)
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
       * SymDiff = Union( Diff(A, B), Diff(B, A) )
       * </pre>
       * and Union has the dimension of the highest-dimension argument.
       */
      resultDimension = Math.max(dim0, dim1);
      break;
    }
    return resultDimension;
  }
 
}

