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
import org.locationtech.jts.util.Debug;

/**
 * Computes the geometric overlay of two {@link Geometry}s, 
 * using an explicit precision model to allow robust computation. 
 * The overlay can be used to determine any of the 
 * following set-theoretic operations (boolean combinations) of the geometries:
 * <ul>
 * <li>{@link INTERSECTION} - all points which lie in both geometries
 * <li>{@link UNION} - all points which lie in at least one geometry
 * <li>{@link DIFFERENCE} - all points which lie in the first geometry but not the second
 * <li>{@link SYMDIFFERENCE} - all points which lie in one geometry but not both
 * </ul>
 * Input geometries may have different dimension.  
 * Collections must be homogeneous
 * (all elements must have the same dimension).
 * <p>
 * The precision model used for the computation can be supplied 
 * independent of the precision model of the input geometry.
 * The main use for this is to allow using a fixed precision 
 * for geometry with a floating precision model.
 * This does two things: ensures robust computation;
 * and forces the output to be validly rounded to the precision model. 
 * <p>
 * For fixed precision models noding is performed using snap-rounding.
 * This provides robust computation (as long as precision is limited to
 * around 13 decimal digits).
 * <p>
 * For floating precision the conventional JTS noder is used. 
 * This is not fully robust, so can sometimes result in 
 * {@link TopologyException}s being thrown. 
 * <p>
 * A custom {@link Noder} can be supplied.
 * This allows using a more performant noding strategy in specific cases, 
 * for instance in {@link CoverageUnion}.
 * 
 * @author mdavis
 *
 */
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
    return isResultOfOp(opCode, loc0, loc1);
  }
  
  /**
   * Tests whether a point with given {@link Location}s
   * relative to two geometries is contained in 
   * the result of overlaying the geometries using
   * a given overlay operation.
   * <p>
   * The method handles arguments of {@link Location#NONE} correctly
   * @param overlayOpCode the code for the overlay operation to test
   * @param loc0 the code for the location in the first geometry 
   * @param loc1 the code for the location in the second geometry 
   *
   * @return true if the locations correspond to the overlayOpCode
   */
  static boolean isResultOfOp(int overlayOpCode, int loc0, int loc1)
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
   * the given geometry operands.
   * 
   * @param geom0 the first geometry argument
   * @param geom1 the second geometry argument
   * @param opCode the code for the desired overlay operation
   * @param pm the precision model to use
   * @return the result of the overlay operation
   */
  public static Geometry overlay(Geometry geom0, Geometry geom1, 
      int opCode, PrecisionModel pm)
  {
    OverlayNG ov = new OverlayNG(geom0, geom1, pm, opCode);
    Geometry geomOv = ov.getResult();
    return geomOv;
  }

  /**
   * Computes an overlay operation on the given geometry operands, 
   * using a supplied {@link Noder}.
   * 
   * @param geom0 the first geometry argument
   * @param geom1 the second geometry argument
   * @param opCode the code for the desired overlay operation
   * @param pm the precision model to use (which may be null if the noder does not use one)
   * @param noder the noder to use
   * @return the result of the overlay operation
   */
  public static Geometry overlay(Geometry geom0, Geometry geom1, 
      int opCode, PrecisionModel pm, Noder noder)
  {
    OverlayNG ov = new OverlayNG(geom0, geom1, pm, opCode);
    ov.setNoder(noder);
    Geometry geomOv = ov.getResult();
    return geomOv;
  }

  /**
   * Computes an overlay operation on 
   * the given geometry operands,
   * using an automatically-determined fixed precision model
   * which maximises precision while ensuring robust computation.
   * 
   * @param geom0 the first geometry argument
   * @param geom1 the second geometry argument
   * @param opCode the code for the desired overlay operation
   * @return the result of the overlay operation
   */
  public static Geometry overlayFixedPrecision(Geometry geom0, Geometry geom1, int opCode)
  {
    PrecisionModel pm = PrecisionUtil.robustPM(geom0, geom1);
    //System.out.println("Precision Model: " + pm);
    
    OverlayNG ov = new OverlayNG(geom0, geom1, pm, opCode);
    return ov.getResult();
  }

  /**
   * Computes an overlay operation on 
   * the given geometry operands,
   * using the precision model of the geometry.
   * and an appropriate noder.
   * <p>
   * The noder is chosen according to the precision model specified.
   * <ul>
   * <li>For {@link PrecisionModel#FIXED}
   * a snap-rounding noder is used, and the computation is robust.
   * <li>For {@link PrecisionModel#FLOATING}
   * a non-snapping noder is used,
   * and this computation may not be robust.
   * If errors occur a {@link TopologyException} is thrown.
   * </ul>
   * 
   * 
   * @param geom0 the first argument geometry
   * @param geom1 the second argument geometry
   * @param opCode the code for the desired overlay operation
   * @return the result of the overlay operation
   */
  public static Geometry overlay(Geometry geom0, Geometry geom1, int opCode)
  {
    OverlayNG ov = new OverlayNG(geom0, geom1, opCode);
    return ov.getResult();
  }

  /**
   * Computes a union operation on 
   * the given geometry, with the supplied precision model.
   * The primary use for this is to perform precision reduction
   * (round the geometry to the supplied precision).
   * <p>
   * The input must be a valid geometry.
   * Collections must be homogeneous.
   * <p>
   * To union an overlapping set of polygons in a more performant way use {@link UnaryUnionNG}.
   * To union a polyonal coverage or linear network in a more performant way, 
   * use {@link CoverageUnion}.
   * 
   * @param geom0 the geometry
   * @param pm the precision model to use
   * @return the result of the union operation
   * 
   * @see PrecisionReducer
   * @see CoverageUnion
   * @see UnaryUnionNG
   */
  static Geometry union(Geometry geom, PrecisionModel pm)
  {    
    // empty geometries are not included in the overlay
    Point emptyPoint = geom.getFactory().createPoint();
    OverlayNG ov = new OverlayNG(geom, emptyPoint, pm, UNION);
    Geometry geomOv = ov.getResult();
    return geomOv;
  }

  private int opCode;
  private InputGeometry inputGeom;
  private GeometryFactory geomFact;
  private PrecisionModel pm;
  private Noder noder;
  private boolean isOptimized = true;
  private boolean isOutputEdges = false;
  private boolean isOutputResultEdges = false;
  private boolean isOutputNodedEdges = false;

  private List<Polygon> resultPolyList;
  private List<LineString> resultLineList;
  private List<Point> resultPointList;

  private Geometry outputEdges;

  /**
   * Creates an overlay operation on the given geometries,
   * with a defined precision model.
   * 
   * @param geom0 the A operand geometry
   * @param geom1 the B operand geometry
   * @param pm the precision model to use
   * @param opCode the overlay opcode
   */
  public OverlayNG(Geometry geom0, Geometry geom1, PrecisionModel pm, int opCode) {
    this.pm = pm;
    this.opCode = opCode;
    geomFact = geom0.getFactory();
    inputGeom = new InputGeometry( geom0, geom1 );
  }  
  
  /**
   * Creates an overlay operation on the given geometries
   * using the precision model of the geometries.
   * <p>
   * The noder is chosen according to the precision model specified.
   * <ul>
   * <li>For {@link PrecisionModel#FIXED}
   * a snap-rounding noder is used, and the computation is robust.
   * <li>For {@link PrecisionModel#FLOATING}
   * a non-snapping noder is used,
   * and this computation may not be robust.
   * If errors occur a {@link TopologyException} is thrown.
   * </ul>
   *  
   * @param geom0 the A operand geometry
   * @param geom1 the B operand geometry
   * @param opCode the overlay opcode
   */
  public OverlayNG(Geometry geom0, Geometry geom1, int opCode) {
    this(geom0, geom1, geom0.getFactory().getPrecisionModel(), opCode);
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
  
  private void setNoder(Noder noder) {
    this.noder = noder;
  }
  
  public Geometry getResult() {
    if (OverlayUtil.isEmptyResult(opCode, inputGeom)) {
      return createEmptyResult();
    }

    if (inputGeom.isAllPoints()) {
      return OverlayPoints.overlay(opCode, inputGeom.getGeometry(0), inputGeom.getGeometry(1), pm);
    }
    
    computeEdgeOverlay();
    
    /**
     * If requested, output graph edges instead of final result 
     */
    if (outputEdges != null) return outputEdges;
    
    if (resultPolyList.size() == 0 && resultLineList.size() == 0 && resultPointList.size() == 0)
      return createEmptyResult();
    
    Geometry resultGeom = OverlayUtil.buildResultGeometry(opCode, resultPolyList, resultLineList, resultPointList, geomFact);
    return resultGeom;
  }
  
  private void computeEdgeOverlay() {
    
    OverlayGraph graph = buildGraph();
    
    if (isOutputNodedEdges) {
      outputEdges = OverlayUtil.toLines(graph, isOutputEdges, geomFact);
      return;
    }

    labelGraph(graph);
    
    if (isOutputEdges || isOutputResultEdges) {
      outputEdges =  OverlayUtil.toLines(graph, isOutputEdges, geomFact);
      return;
    }
    
    extractResult(opCode, graph);
    // only used for debugging
    //checkSanity(resultGeom);
  }

  private OverlayGraph buildGraph() {
    /**
     * Node the edges, using whatever noder is being used
     */
    OverlayNoder ovNoder = new OverlayNoder(pm);
    
    if (noder != null) ovNoder.setNoder(noder);
    
    if ( isOptimized ) {
      Envelope clipEnv = OverlayUtil.clippingEnvelope(opCode, inputGeom, pm);
      if (clipEnv != null)
        ovNoder.setClipEnvelope( clipEnv );
    }
    
    ovNoder.add(inputGeom.getGeometry(0), 0);
    ovNoder.add(inputGeom.getGeometry(1), 1);
    Collection<SegmentString> nodedLines = ovNoder.node();
    
    /**
     * Merge the noded edges to eliminate duplicates.
     * Labels will be combined.
     */
    // nodedSegStrings are no longer needed, and will be GCed
    List<Edge> edges = Edge.createEdges(nodedLines);
    List<Edge> mergedEdges = EdgeMerger.merge(edges);
    
    /**
     * Record if an input geometry has collapsed.
     * This is used to avoid trying to locate disconnected edges
     * against a geometry which has collapsed completely.
     */
    inputGeom.setCollapsed(0, ! ovNoder.hasEdgesFor(0) );
    inputGeom.setCollapsed(1, ! ovNoder.hasEdgesFor(0) );
    
    return new OverlayGraph( mergedEdges );
  }

  private void labelGraph(OverlayGraph graph) {
    OverlayLabeller labeller = new OverlayLabeller(graph, inputGeom);
    labeller.computeLabelling();
    labeller.markResultAreaEdges(opCode);
    labeller.unmarkDuplicateEdgesFromResultArea();
  }

  private void extractResult(int opCode, OverlayGraph graph) {
    
    //--- Build polygons
    List<OverlayEdge> resultAreaEdges = graph.getResultAreaEdges();
    PolygonBuilder polyBuilder = new PolygonBuilder(resultAreaEdges, geomFact);
    resultPolyList = polyBuilder.getPolygons();
    boolean hasResultArea = resultPolyList.size() > 0;
    
    //--- Build lines
    LineBuilder lineBuilder = new LineBuilder(inputGeom, graph, hasResultArea, opCode, geomFact);
    resultLineList = lineBuilder.getLines();

    //--- Build points for INTERSECTION op only
    if (opCode == INTERSECTION) {
      IntersectionPointBuilder pointBuilder = new IntersectionPointBuilder(graph, geomFact);
      resultPointList = pointBuilder.getPoints();
    }
    else {
      resultPointList = new ArrayList<Point>();
    }
  }

  private Geometry createEmptyResult() {
    return OverlayUtil.createEmptyResult(
        OverlayUtil.resultDimension(opCode, inputGeom.getDimension(0), inputGeom.getDimension(1))
        , geomFact);
  }
 
}


