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

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.IntersectionAdder;
import org.locationtech.jts.noding.MCIndexNoder;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.noding.ValidatingNoder;
import org.locationtech.jts.noding.snapround.SnapRoundingNoder;

/**
 * Builds a set of noded, unique, labelled Edges from 
 * the edges of the two input geometries.
 * <p> 
 * It performs the following steps:
 * <ul>
 * <li>Extracts input edges, and attaches topological information
 * <li>if clipping is enabled, handles clipping or limiting input geometry
 * <li>chooses a {@link Noder} based on provided precision model, unless a custom one is supplied
 * <li>calls the chosen Noder, with precision model
 * <li>removes any fully collapsed noded edges
 * <li>builds {@link Edge}s and merges them
 * </ul>
 * 
 * @author mdavis
 *
 */
class EdgeNodingBuilder {

  /**
   * Limiting is skipped for Lines with few vertices,
   * to avoid additional copying.
   */
  private static final int MIN_LIMIT_PTS = 20;
  
  /**
   * Indicates whether floating precision noder output is validated.
   */
  private static final boolean IS_NODING_VALIDATED = true;
  
  private static Noder createFixedPrecisionNoder(PrecisionModel pm) {
    //Noder noder = new MCIndexSnapRounder(pm);
    //Noder noder = new SimpleSnapRounder(pm);
    Noder noder = new SnapRoundingNoder(pm);
    return noder;
  }
  
  private static Noder createFloatingPrecisionNoder(boolean doValidation) {
    MCIndexNoder mcNoder = new MCIndexNoder();
    LineIntersector li = new RobustLineIntersector();
    mcNoder.setSegmentIntersector(new IntersectionAdder(li));
    
    Noder noder = mcNoder;
    if (doValidation) {
      noder = new ValidatingNoder( mcNoder);
    }
    return noder;
  }
  
  private PrecisionModel pm;
  List<NodedSegmentString> inputEdges = new ArrayList<NodedSegmentString>();
  private Noder customNoder;
  
  private Envelope clipEnv = null;
  private RingClipper clipper;
  private LineLimiter limiter;

  private boolean[] hasEdges = new boolean[2];


  /**
   * Creates a new builder, with an optional custom noder.
   * If the noder is not provided, a suitable one will 
   * be used based on the supplied precision model.
   * 
   * @param pm the precision model to use
   * @param noder an optional custom noder to use (may be null)
   */
  public EdgeNodingBuilder(PrecisionModel pm, Noder noder) {
    this.pm = pm;
    this.customNoder = noder;
  }

  /**
   * Gets a noder appropriate for the precision model supplied.
   * This is one of:
   * <ul>
   * <li>Fixed precision: a snap-rounding noder (which should be fully robust)
   * <li>Floating precision: a conventional nodel (which may be non-robust).
   * In this case, a validation step is applied to the output from the noder.
   * </ul> 
   * 
   * @return
   */
  private Noder getNoder() {
    if (customNoder != null) return customNoder;
    if (OverlayUtil.isFloating(pm))
      return createFloatingPrecisionNoder(IS_NODING_VALIDATED);
    return createFixedPrecisionNoder(pm);
  }
  
  public void setClipEnvelope(Envelope clipEnv) {
    this.clipEnv = clipEnv;
    clipper = new RingClipper(clipEnv);
    limiter = new LineLimiter(clipEnv);
  }
  
  /**
   * Reports whether there are noded edges
   * for the given input geometry.
   * If there are none, this indicates that either
   * the geometry was empty, or has completely collapsed
   * (because it is smaller than the noding precision).
   * 
   * @param geomIndex index of input geometry
   * @return true if there are edges for the geometry
   */
  public boolean hasEdgesFor(int geomIndex ) {
    return hasEdges[geomIndex];
  }
  
  /**
   * Creates a set of labelled {Edge}s.
   * representing the fully noded edges of the input geometries.
   * Coincident edges (from the same or both geometries) 
   * are merged along with their labels
   * into a single unique, fully labelled edge.
   * 
   * @param geom0 the first geometry
   * @param geom1 the second geometry
   * @return the noded, merged, labelled edges
   */
  public List<Edge> build(Geometry geom0, Geometry geom1) {
    add(geom0, 0);
    add(geom1, 1);
    List<Edge> nodedEdges = node(inputEdges);
    
    /**
     * Merge the noded edges to eliminate duplicates.
     * Labels are combined.
     */
    List<Edge> mergedEdges = EdgeMerger.merge(nodedEdges);
    return mergedEdges;
  }
  
  /**
   * Nodes a set of segment strings and creates {@link Edge}s from the result.
   * The input segment strings each carry a {@link EdgeSourceInfo} object,
   * which is used to provide source topology info to the constructed Edges
   * (and is then discarded).
   * 
   * @param segStrings
   * @return
   */
  private List<Edge> node(List<NodedSegmentString> segStrings) {
    Noder noder = getNoder();
    noder.computeNodes(segStrings);
    
    @SuppressWarnings("unchecked")
    Collection<SegmentString> nodedSS = noder.getNodedSubstrings();
    
    //scanForEdges(nodedSS);
    
    List<Edge> edges = createEdges(nodedSS);

    return edges;
  }

  private List<Edge> createEdges(Collection<SegmentString> segStrings) {
    List<Edge> edges = new ArrayList<Edge>();
    for (SegmentString ss : segStrings) {
      Coordinate[] pts = ss.getCoordinates();
      
      // don't create edges from collapsed lines
      if ( Edge.isCollapsed(pts) ) continue;
      
      EdgeSourceInfo info = (EdgeSourceInfo) ss.getData();
      /**
       * Record that a non-collapsed edge exists for the parent geometry
       */
      hasEdges[ info.getIndex() ] = true;
      
      edges.add(new Edge(ss.getCoordinates(), info));
    }
    return edges;
  }
  
  private void add(Geometry g, int geomIndex)
  {
    if (g == null || g.isEmpty()) return;
    
    if (isClippedCompletely(g.getEnvelopeInternal())) 
      return;

    if (g instanceof Polygon)                 addPolygon((Polygon) g, geomIndex);
    // LineString also handles LinearRings
    else if (g instanceof LineString)         addLine((LineString) g, geomIndex);
    else if (g instanceof MultiLineString)    addCollection((MultiLineString) g, geomIndex);
    else if (g instanceof MultiPolygon)       addCollection((MultiPolygon) g, geomIndex);
    else if (g instanceof GeometryCollection) addGeometryCollection((GeometryCollection) g, geomIndex, g.getDimension());
    // ignore Point geometries - they are handled elsewhere
  }
  
  private void addCollection(GeometryCollection gc, int geomIndex)
  {
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Geometry g = gc.getGeometryN(i);
      add(g, geomIndex);
    }
  }

  private void addGeometryCollection(GeometryCollection gc, int geomIndex, int expectedDim)
  {
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Geometry g = gc.getGeometryN(i);
      // check for mixed-dimension input, which is not supported
      if (g.getDimension() != expectedDim) {
        throw new IllegalArgumentException("Overlay input is mixed-dimension");
      }
      add(g, geomIndex);
    }
  }

  private void addPolygon(Polygon poly, int geomIndex)
  {
    LinearRing shell = poly.getExteriorRing();
    addPolygonRing(shell, false, geomIndex);

    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      LinearRing hole = poly.getInteriorRingN(i);
      
      // Holes are topologically labelled opposite to the shell, since
      // the interior of the polygon lies on their opposite side
      // (on the left, if the hole is oriented CW)
      addPolygonRing(hole, true, geomIndex);
    }
  }

  /**
   * Adds a polygon ring to the graph.
   * Empty rings are ignored.
   */
  private void addPolygonRing(LinearRing ring, boolean isHole, int index)
  {
    // don't add empty rings
    if (ring.isEmpty()) return;
    
    if (isClippedCompletely(ring.getEnvelopeInternal())) 
      return;
    
    Coordinate[] pts = clip( ring );

    /**
     * Don't add edges that collapse to a point
     */
    if (pts.length < 2) {
      return;
    }
    
    //if (pts.length < ring.getNumPoints()) System.out.println("Ring clipped: " + ring.getNumPoints() + " => " + pts.length);
    
    int depthDelta = computeDepthDelta(ring, isHole);
    EdgeSourceInfo info = new EdgeSourceInfo(index, depthDelta, isHole);
    addEdge(pts, info);
  }

  /**
   * Tests whether a geometry (represented by its envelope)
   * lies completely outside the clip extent(if any).
   * 
   * @param env the geometry envelope
   * @return true if the geometry envelope is outside the clip extent.
   */
  private boolean isClippedCompletely(Envelope env) {
    if (clipEnv == null) return false;
    return clipEnv.disjoint(env);
  }
  
  /**
   * If a clipper is present, 
   * clip the line to the clip extent.
   * Otherwise, remove duplicate points from the ring.
   * <p>
   * If clipping is enabled, then every ring MUST 
   * be clipped, to ensure that holes are clipped to
   * be inside the shell.  
   * This means it is not possible to skip 
   * clipping for rings with few vertices.
   * 
   * @param ring the line to clip
   * @return the points in the clipped line
   */
  private Coordinate[] clip(LinearRing ring) {
    Coordinate[] pts = ring.getCoordinates();
    Envelope env = ring.getEnvelopeInternal();
    
    /**
     * If no clipper or ring is completely contained then no need to clip.
     * But repeated points must be removed to ensure correct noding.
     */
    if (clipper == null || clipEnv.covers(env)) {
      return removeRepeatedPoints(ring);
    }

    return clipper.clip(pts);
  }
  
  /**
   * Removes any repeated points from a linear component.
   * This is required so that noding can be computed correctly.
   * 
   * @param line the line to process
   * @return the points of the line with repeated points removed
   */
  private static Coordinate[] removeRepeatedPoints(LineString line) {
    Coordinate[] pts = line.getCoordinates();
    return CoordinateArrays.removeRepeatedPoints(pts);
  }
  
  private static int computeDepthDelta(LinearRing ring, boolean isHole) {
    /**
     * Compute the orientation of the ring, to
     * allow assigning side interior/exterior labels correctly.
     * JTS canonical orientation is that shells are CW, holes are CCW.
     * 
     * It is important to compute orientation on the original ring,
     * since topology collapse can make the orientation computation give the wrong answer.
     */
    boolean isCCW = Orientation.isCCW( ring.getCoordinateSequence() );
    /**
     * Compute whether ring is in canonical orientation or not.
     * Canonical orientation for the overlay process is
     * Shells : CW, Holes: CCW
     */
    boolean isOriented = true;
    if (! isHole)
      isOriented = ! isCCW;
    else {
      isOriented = isCCW;
    }
    /**
     * Depth delta can now be computed. 
     * Canonical depth delta is 1 (Exterior on L, Interior on R).
     * It is flipped to -1 if the ring is oppositely oriented.
     */
    int depthDelta = isOriented ? 1 : -1;
    return depthDelta;
  }

  /**
   * Adds a line geometry, limiting it if enabled,
   * and otherwise removing repeated points.
   * 
   * @param line the line to add
   * @param geomIndex the index of the parent geometry
   */
  private void addLine(LineString line, int geomIndex)
  {
    // don't add empty lines
    if (line.isEmpty()) return;
    
    if (isClippedCompletely(line.getEnvelopeInternal())) 
      return;
    
    if (isToBeLimited(line)) {
      List<Coordinate[]> sections = limit( line );
      for (Coordinate[] pts : sections) {
        addLine( pts, geomIndex );
      }
    }
    else {
      Coordinate[] ptsNoRepeat = removeRepeatedPoints(line);
      addLine( ptsNoRepeat, geomIndex );
    }
  }


  private void addLine(Coordinate[] pts, int geomIndex) {
    /**
     * Don't add edges that collapse to a point
     */
    if (pts.length < 2) {
      return;
    }
    
    EdgeSourceInfo info = new EdgeSourceInfo(geomIndex);
    addEdge(pts, info);
  }
  
  private void addEdge(Coordinate[] pts, EdgeSourceInfo info) {
    NodedSegmentString ss = new NodedSegmentString(pts, info);
    inputEdges.add(ss);
  }

  /**
   * Tests whether it is worth limiting a line.
   * Lines that have few vertices or are covered
   * by the clip extent do not need to be limited.
   * 
   * @param line line to test
   * @return true if the line should be limited
   */
  private boolean isToBeLimited(LineString line) {
    Coordinate[] pts = line.getCoordinates();
    if (limiter == null || pts.length <= MIN_LIMIT_PTS) {
      return false;
    }
    Envelope env = line.getEnvelopeInternal();
    /**
     * If line is completely contained then no need to limit
     */
    if (clipEnv.covers(env)) {
      return false;
    }
    return true;
  }

  /**
   * If limiter is provided, 
   * limit the line to the clip envelope.
   * 
   * @param line the line to clip
   * @return the point sections in the clipped line
   */
  private List<Coordinate[]> limit(LineString line) {
    Coordinate[] pts = line.getCoordinates();
    return limiter.limit(pts);
  }

}
