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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
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
import org.locationtech.jts.noding.snapround.MCIndexSnapRounder;

public class OverlayNoder {

  private PrecisionModel pm;
  List<NodedSegmentString> segStrings = new ArrayList<NodedSegmentString>();
  private Noder customNoder;

  public OverlayNoder(Geometry a, Geometry b, PrecisionModel pm) {
    this.pm = pm;
    add(a, 0);
    add(b, 1);
  }
  
  public void setNoder(Noder noder) {
    this.customNoder = noder;
  }

  public Collection<SegmentString> node() {
    Noder noder = getNoder();
    //Noder noder = getSRNoder();
    //Noder noder = getSimpleNoder(false);
    //Noder noder = getSimpleNoder(true);
    noder.computeNodes(segStrings);
    
    @SuppressWarnings("unchecked")
    Collection<SegmentString> nodedSS = noder.getNodedSubstrings();
    return nodedSS;
  }

  private Noder getNoder() {
    if (customNoder != null) return customNoder;
    return getSRNoder();
  }

  private Noder getSRNoder() {
    Noder noder = new MCIndexSnapRounder(pm);
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
  
  private void add(Geometry g, int geomIndex)
  {
    if (g.isEmpty()) return;

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

  private void addPolygon(Polygon p, int geomIndex)
  {
    addPolygonRing(
            (LinearRing) p.getExteriorRing(), false, geomIndex);

    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) p.getInteriorRingN(i);
      
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
  private void addPolygonRing(LinearRing lr, boolean isHole, int index)
  {
    /**
     * Empty rings are not added.
     */
    if (lr.isEmpty()) return;
    
    Coordinate[] ptsRaw = lr.getCoordinates();
    
    /**
     * Compute the orientation of the ring, to
     * allow assigning side interior/exterior labels correctly.
     * JTS canonical orientation is that shells are CW, holes are CCW.
     * 
     * Important to compute orientation BEFORE rounding,
     * since topology collapse can make the orientation computation give the wrong answer.
     */
    boolean isCCW = Orientation.isCCW(ptsRaw);
    
    /**
     * Round the input points, to ensure they match the requested precision.
     * This may cause collapsing to occur,
     * but it is handled by the overlay processing.
     */
    Coordinate[] pts = round(ptsRaw);
    
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
    add(pts, info);
  }

  private void addLineString(LineString line, int geomIndex)
  {
    // don't add empty lines
    if (line.isEmpty()) return;
    
    Coordinate[] pts = round(line.getCoordinates());

    /**
     * Don't add edges that collapse to a point
     */
    if (pts.length < 2) {
      return;
    }
    EdgeInfo info = new EdgeInfo(geomIndex);
    add(pts, info);
  }
  
  private void add(Coordinate[] pts, EdgeInfo info) {
    NodedSegmentString ss = new NodedSegmentString(pts, info);
    segStrings.add(ss);
  }

  private Coordinate[] round(Coordinate[] pts)  {
    
    CoordinateList noRepeatCoordList = new CoordinateList();

    for (int i = 0; i < pts.length; i++) {
      Coordinate coord = new Coordinate(pts[i]);
      makePrecise(coord);
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
