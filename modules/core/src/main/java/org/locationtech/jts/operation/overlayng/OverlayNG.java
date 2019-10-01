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
import org.locationtech.jts.geom.Envelope;
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
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Debug;

public class OverlayNG 
{
  /**
   * The code for the Intersection overlay operation.
   */
  public static final int INTERSECTION  = OverlayOp.INTERSECTION;
  
  /**
   * The code for the Union overlay operation.
   */
  public static final int UNION         = OverlayOp.UNION;
  
  /**
   *  The code for the Difference overlay operation.
   */
  public static final int DIFFERENCE    = OverlayOp.DIFFERENCE;
  
  /**
   *  The code for the Symmetric Difference overlay operation.
   */
  public static final int SYMDIFFERENCE = OverlayOp.SYMDIFFERENCE;

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
  static boolean isResultOfOpPoint(OverlayLabel label, int opCode)
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
  static boolean isResultOfOp(int loc0, int loc1, int overlayOpCode)
  {
    if (loc0 == Location.BOUNDARY) loc0 = Location.INTERIOR;
    if (loc1 == Location.BOUNDARY) loc1 = Location.INTERIOR;
    switch (overlayOpCode) {
    case INTERSECTION:
      return loc0 == Location.INTERIOR
          && loc1 == Location.INTERIOR;
    case UNION:
      return loc0 == Location.INTERIOR
          || loc1 == Location.INTERIOR;
    case DIFFERENCE:
      return loc0 == Location.INTERIOR
          && loc1 != Location.INTERIOR;
    case SYMDIFFERENCE:
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

  /**
   * Reduces the precision of a geometry by rounding it to the
   * supplied precision model.
   * <p> 
   * The output is always a valid geometry (so that input polygons
   * may end up being merged).
   * <p>
   * The output is fully noded.  
   * This is an effective way to node and snap-rounding a collection of {@link LineString}s.
   * 
   * @param geom the geometry to reduce
   * @param pm the precision model to use
   * @return the reduced geometry
   */
  public static Geometry reducePrecision(Geometry geom, PrecisionModel pm) {
    Point emptyPoint = geom.getFactory().createPoint();
    Geometry reduced = OverlayNG.overlay(geom, emptyPoint, pm, UNION);
    return reduced;
  }
  
  private static final int SAFE_ENV_EXPAND_FACTOR = 3;

  private int opCode;
  private InputGeometry inputGeom;
  private GeometryFactory geomFact;
  private PrecisionModel pm;
  private boolean isOutputEdges;
  private boolean isOutputResultEdges;
  private boolean isOutputNodedEdges;
  private boolean isOptimized = true;
  
  // internal state
  private OverlayGraph graph;
  private Noder noder;
  private Geometry resultGeom;

  public OverlayNG(Geometry geom0, Geometry geom1, PrecisionModel pm, int opCode) {
    this.pm = pm;
    this.opCode = opCode;
    geomFact = geom0.getFactory();
    inputGeom = new InputGeometry( geom0, geom1 );
  }  
  
  /**
   * Sets whether overlay processing optimizations are enabled.
   * It may be useful to disable optimizations
   * for testing purposes.
   * Default is TRUE (optimization enabled).
   * 
   * @param isOptimized whether to optimize processing
   */
  public void setOptimized(boolean isOptimized) {
    this.isOptimized = isOptimized;
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
    computeOverlay();
    
    return resultGeom;
  }
  
  private void computeOverlay() {
    if (isEmptyResult()) {
      resultGeom = createEmptyResult();
      return;
    }
    
    LineClipper clipper = optimizeByClipper();

    //--- Noding phase
    List<Edge> edges = nodeAndMerge(clipper);
    
    //--- Topology building phase
    graph = new OverlayGraph( edges );
    
    if (isOutputNodedEdges) {
      resultGeom = toLines(graph, geomFact);
      return;
    }

    graph.computeLabelling(inputGeom);
    
    graph.markResultAreaEdges(opCode);
    graph.removeDuplicateResultAreaEdges();
    
    if (isOutputEdges || isOutputResultEdges) {
      resultGeom = toLines(graph, geomFact);
      return;
    }
    
    resultGeom = createResult(opCode);
    // only used for debugging
    //checkSanity(resultGeom);
  }

  private boolean isEmptyResult() {
    switch (opCode) {
    case INTERSECTION:
      if ( inputGeom.isEmpty(0) || inputGeom.isEmpty(1) )
        return true;
    case DIFFERENCE:
      if ( inputGeom.isEmpty(0) )     
        return true;
    }
    return false;
  }

  private Geometry createEmptyResult() {
    return createEmptyResult(opCode, inputGeom.getGeometry(0), inputGeom.getGeometry(1), geomFact);
  }

  private LineClipper optimizeByClipper() {
    if (! isOptimized) 
      return null;
    
    Envelope clipEnv = null;
    switch (opCode) {
    case INTERSECTION:
      Envelope envA = safeOverlapEnv( inputGeom.getGeometry(0).getEnvelopeInternal() );
      Envelope envB = safeOverlapEnv( inputGeom.getGeometry(1).getEnvelopeInternal() );
      clipEnv = envA.intersection(envB);   
      break;
    case DIFFERENCE:
      clipEnv = safeOverlapEnv( inputGeom.getGeometry(0).getEnvelopeInternal() );
      break;
    }
    // a conservative limit - seems to be ok to use more aggressive one tho
    //Envelope limitEnv = limitRectangle();
    
    if (clipEnv == null) return null;
    LineClipper clipper = new LineClipper(clipEnv);
    return clipper;
  }

  /**
   * Computes a limiter envelope based 
   * on the envelope of overlap of the two inputs.
   * This is the most aggressive limiter optimization strategy for intersection.
   * 
   * @return a limiter envelope of the input overlap envelope
   */
  private Envelope limitOverlap() {
    Envelope envA = safeOverlapEnv( inputGeom.getGeometry(0).getEnvelopeInternal() );
    Envelope envB = safeOverlapEnv( inputGeom.getGeometry(1).getEnvelopeInternal() );
    Envelope limitEnv = envA.intersection(envB);
    return limitEnv;
  }
  
  /**
   * Computes a limiter envelope for one of the inputs 
   * which is a rectangle.
   * This is a conservative limiter optimization strategy.
   * 
   * @return the limit envelope for a rectangle input
   */
  private Envelope limitRectangle() {
    int rectGeomIndex = -1;
    if (inputGeom.getGeometry(0).isRectangle()) rectGeomIndex = 0;
    if (inputGeom.getGeometry(1).isRectangle()) rectGeomIndex = 1;
    
    if (rectGeomIndex < 0) return null;

    Envelope limitEnv = safeOverlapEnv( inputGeom.getGeometry(rectGeomIndex).getEnvelopeInternal() );
    return limitEnv;
  }

  private Envelope safeOverlapEnv(Envelope originalEnv) {
    double envBufDist = 0;
    // if PM is FLOAT then there is no scale factor, so add 10%
    if (pm == null || pm.isFloating()) {
      double minSize = Math.min(originalEnv.getHeight(), originalEnv.getWidth());
      envBufDist = 0.1 * minSize;
    }
    else {
      envBufDist = SAFE_ENV_EXPAND_FACTOR * 1.0 / pm.getScale();
    }
    Envelope safeEnv = new Envelope(originalEnv);
    safeEnv.expandBy(envBufDist);
    return safeEnv;
  }

  private void checkSanity(Geometry result) {
    // for Union, area should be greater than largest of inputs
    double areaA = inputGeom.getGeometry(0).getArea();
    double areaB = inputGeom.getGeometry(1).getArea();
    double area = result.getArea();
    
    // if result is empty probably had a complete collapse, so can't use this check
    if (area == 0) return;
    
    if (opCode == UNION) {
      double minAreaLimit = 0.5 * Math.max(areaA, areaB);
      if (area < minAreaLimit ) {
        throw new TopologyException("Result area sanity issue");
      }
    }
    
  }

  private List<Edge> nodeAndMerge(LineClipper clipper) {
    /**
     * Node the edges, using whatever noder is being used
     */
    OverlayNoder ovNoder = new OverlayNoder(pm);
    
    if (noder != null) ovNoder.setNoder(noder);
    
    if (clipper != null) {
      ovNoder.setLimiter( clipper );
    }
    
    ovNoder.add(inputGeom.getGeometry(0), 0);
    ovNoder.add(inputGeom.getGeometry(1), 1);
    Collection<SegmentString> nodedSegStrings = ovNoder.node();
    
    /**
     * Record if an input geometry has collapsed.
     * This is used to avoid trying to locate disconnected edges
     * against a geometry which has collapsed completely.
     */
    inputGeom.setCollapsed(0, isCollapsed(0, ovNoder, clipper));
    inputGeom.setCollapsed(1, isCollapsed(1, ovNoder, clipper));
    
    /**
     * Merge the noded edges to eliminate duplicates.
     * Labels will be combined.
     */
    // nodedSegStrings are no longer needed, and will be GCed
    List<Edge> edges = createEdges(nodedSegStrings);
    List<Edge> mergedEdges = EdgeMerger.merge(edges);
    return mergedEdges;
  }

  private boolean isCollapsed(int geomIndex, OverlayNoder ovNoder, LineClipper clipper) {
    if (clipper != null) {
      /**
       * If the geometry is bigger than the limit env, it can't have collapsed.
       * Need this check because if geometry linework is wholly outside
       * limiter env, there will be no edges present for it.
       */
      /*
      Envelope geomEnv = inputGeom.getEnvelope( geomIndex );
      if (! limiter.isWithinLimit( geomEnv ))
        return false;
        */
    }
    /**
     * Otherwise, if no edges remain after noding, 
     * this geom must have collapsed.
     */
    return ! ovNoder.hasEdgesFor(geomIndex);
  }

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
      boolean includeEdge = isOutputEdges || edge.isInResultArea();
      if (! includeEdge) continue;
      //Coordinate[] pts = getCoords(nss);
      Coordinate[] pts = edge.getCoordinatesOriented();
      LineString line = geomFact.createLineString(pts);
      line.setUserData(labelForResult(edge) );
      lines.add(line);
    }
    return geomFact.buildGeometry(lines);
  }

  private static String labelForResult(OverlayEdge edge) {
    return edge.getLabel().toString(edge.isForward())
        + (edge.isInResultArea() ? " Res" : "");
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
    
    // TODO: return Multi geoms for all output dimensions
    
    // element geometries of the result are always in the order P,L,A
    geomList.addAll(resultPolyList);
    geomList.addAll(resultLineList);
    geomList.addAll(resultPointList);

    if ( geomList.isEmpty() )
      return createEmptyResult();

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
   * (since the Symmetric Difference is the Union of the Differences).
   * </ul>
   * 
   * @param overlayOpCode the code for the overlay operation being performed
   * @param a an input geometry
   * @param b an input geometry
   * @param geomFact the geometry factory being used for the operation
   * @return an empty atomic geometry of the appropriate dimension
   */
  private static Geometry createEmptyResult(int overlayOpCode, Geometry a, Geometry b, GeometryFactory geomFact)
  {
    Geometry result = null;
    switch (resultDimension(overlayOpCode, a, b)) {
    case 0:
      result =  geomFact.createPoint();
      break;
    case 1:
      result =  geomFact.createLineString();
      break;
    case 2:
      result =  geomFact.createPolygon();
      break;
    default:
      Assert.shouldNeverReachHere("Unable to determine overlay result geometry dimension");
    }
    return result;
  }
  
  public static int resultDimension(int opCode, Geometry g0, Geometry g1)
  {
    int dim0 = g0.getDimension();
    int dim1 = g1.getDimension();
    
    int resultDimension = -1;
    switch (opCode) {
    case INTERSECTION: 
      resultDimension = Math.min(dim0, dim1);
      break;
    case UNION: 
      resultDimension = Math.max(dim0, dim1);
      break;
    case DIFFERENCE: 
      resultDimension = dim0;
      break;
    case SYMDIFFERENCE: 
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


