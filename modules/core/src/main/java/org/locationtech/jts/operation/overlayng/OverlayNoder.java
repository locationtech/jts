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

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
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
import org.locationtech.jts.noding.snapround.FastSnapRounder;
import org.locationtech.jts.noding.snapround.MCIndexSnapRounder;
import org.locationtech.jts.noding.snapround.SimpleSnapRounder;

class OverlayNoder {

  private PrecisionModel pm;
  List<NodedSegmentString> segStrings = new ArrayList<NodedSegmentString>();
  private Noder customNoder;
  private boolean hasEdgesA;
  private boolean hasEdgesB;
  private LineClipper clipper;

  public OverlayNoder(PrecisionModel pm) {
    this.pm = pm;
  }
  
  public void setNoder(Noder noder) {
    this.customNoder = noder;
  }

  public void setLimiter(LineClipper clipper) {
    this.clipper = clipper;
  }
  
  public Collection<SegmentString> node() {
    Noder noder = getNoder();
    //Noder noder = getSRNoder();
    //Noder noder = getSimpleNoder(false);
    //Noder noder = getSimpleNoder(true);
    noder.computeNodes(segStrings);
    
    @SuppressWarnings("unchecked")
    Collection<SegmentString> nodedSS = noder.getNodedSubstrings();
    
    scanForHasEdges(nodedSS);
    
    return nodedSS;
  }

  /**
   * Records if each geometry has edges present after noding.
   * If a geometry has collapsed to a point due to low precision,
   * no edges will be present.
   * 
   * @param segStrings noded edges to scan
   */
  private void scanForHasEdges(Collection<SegmentString> segStrings) {
    for (SegmentString ss : segStrings) {
      EdgeInfo info = (EdgeInfo) ss.getData();
      int geomIndex = info.getIndex();
      if (geomIndex == 0)
        hasEdgesA = true;
      else if (geomIndex == 1) {
        hasEdgesB = true;
      }
      // short-circuit if both have been found
      if (hasEdgesA && hasEdgesB) return;
    }
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
    if (geomIndex == 0) return hasEdgesA;
    return hasEdgesB;
  }
  
  private Noder getNoder() {
    if (customNoder != null) return customNoder;
    return getSRNoder();
  }

  private Noder getSRNoder() {
    //Noder noder = new MCIndexSnapRounder(pm);
    //Noder noder = new SimpleSnapRounder(pm);
    Noder noder = new FastSnapRounder(pm);
    //Noder noder = new ValidatingNoder(new SimpleSnapRounder(pm));
    return noder;
  }
  
  private Noder getSimpleNoder(boolean doValidation) {
    MCIndexNoder mcNoder = new MCIndexNoder();
    LineIntersector li = new RobustLineIntersector();
    mcNoder.setSegmentIntersector(new IntersectionAdder(li));
    
    Noder noder = mcNoder;
    if (doValidation) {
      noder = new ValidatingNoder( mcNoder);
    }
    return noder;
  }
  
  public void add(Geometry g, int geomIndex)
  {
    if (g.isEmpty()) return;
    if (isClippedToEmpty(g.getEnvelopeInternal())) 
      return;


    if (g instanceof Polygon)                 addPolygon((Polygon) g, geomIndex);
    // LineString also handles LinearRings
    else if (g instanceof LineString)         addLineString((LineString) g, geomIndex);
    //else if (g instanceof Point)              addPoint((Point) g);
    //else if (g instanceof MultiPoint)         addCollection((MultiPoint) g);
    else if (g instanceof MultiLineString)    addCollection((MultiLineString) g, geomIndex);
    else if (g instanceof MultiPolygon)       addCollection((MultiPolygon) g, geomIndex);
    //else if (g instanceof GeometryCollection) addCollection((GeometryCollection) g);
    else throw new UnsupportedOperationException(g.getClass().getName());
  }
  
  private void addCollection(GeometryCollection gc, int geomIndex)
  {
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Geometry g = gc.getGeometryN(i);
      
      add(g, geomIndex);
    }
  }

  private void addPolygon(Polygon poly, int geomIndex)
  {
    addPolygonRing(
            (LinearRing) poly.getExteriorRing(), false, geomIndex);

    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) poly.getInteriorRingN(i);
      
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
    /**
     * Empty rings are not added.
     */
    if (ring.isEmpty()) return;
    
    if (isClippedToEmpty(ring.getEnvelopeInternal())) 
      return;
    
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
     * Round the input points, to ensure they match the requested precision.
     * This may cause collapsing to occur,
     * but it is handled by the overlay processing.
     */
    //Coordinate[] pts = round(ptsRaw);
    Coordinate[] pts = clip( ring );
    
    /**
     * Don't add edges that collapse to a point
     */
    if (pts.length < 2) {
      return;
    }
    
    int depthDelta = 1;
    
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
    int depthDeltaFinal = isOriented ? depthDelta : -1 * depthDelta;
    
    EdgeInfo info = new EdgeInfo(index, depthDeltaFinal, isHole);
    addEdge(pts, info);
  }

  private void addLineString(LineString line, int geomIndex)
  {
    // don't add empty lines
    if (line.isEmpty()) return;
    
    if (isClippedToEmpty(line.getEnvelopeInternal())) 
      return;
    
    //Coordinate[] pts = round(line.getCoordinates());
    Coordinate[] pts = clip( line );

    /**
     * Don't add edges that collapse to a point
     */
    if (pts.length < 2) {
      return;
    }
    EdgeInfo info = new EdgeInfo(geomIndex);
    addEdge(pts, info);
  }

  private void addEdge(Coordinate[] pts, EdgeInfo info) {
    NodedSegmentString ss = new NodedSegmentString(pts, info);
    segStrings.add(ss);
  }

  private boolean isClippedToEmpty(Envelope env) {
    if (clipper == null) return false;
    return clipper.isDisjoint(env);
  }
  
  private Coordinate[] clip(LineString line) {
    Coordinate[] pts = line.getCoordinates();
    if (clipper == null) {
      return pts;
    }
    Envelope env = line.getEnvelopeInternal();
    /**
     * If line is completely contained then no need to clip
     */
    if (clipper.covers(env)) {
      return pts;
    }
    return clipper.clip(pts);
  }


  private Coordinate[] round(Coordinate[] pts)  {
    
    CoordinateList noRepeatCoordList = new CoordinateList();

    for (int i = 0; i < pts.length; i++) {
      Coordinate coord = new Coordinate(pts[i]);
      
      // MD - disable for now to test improved snap-rounding
      //makePrecise(coord);
      noRepeatCoordList.add(coord, false);
    }
    Coordinate[] reducedPts = noRepeatCoordList.toCoordinateArray();
    return reducedPts;
  }  
  
  private void makePrecise(Coordinate coord) {
    // this allows clients to avoid rounding if needed by the noder
    if (pm != null)
      pm.makePrecise(coord);
  }

  /*
  private Coordinate[] round(Coordinate[] pts, int minLength)  {
    CoordinateList noRepeatCoordList = new CoordinateList();

    for (int i = 0; i < pts.length; i++) {
      Coordinate coord = new Coordinate(pts[i]);
      pm.makePrecise(coord);
      noRepeatCoordList.add(coord, false);
    }
    Coordinate[] reducedPts = noRepeatCoordList.toCoordinateArray();
    if (minLength > 0 && reducedPts.length < minLength) {
      return pad(reducedPts, minLength);
    }
    return reducedPts;
  }
*/
  
  private static Coordinate[] pad(Coordinate[] pts, int minLength) {
    Coordinate[] pts2 = new Coordinate[minLength];
    for (int i = 0; i < minLength; i++) {
      if (i < pts.length) {
        pts2[i] = pts[i];
      }
      else {
        pts2[i] = pts[pts.length - 1];
      }
    }
    return pts2;
  }
}
