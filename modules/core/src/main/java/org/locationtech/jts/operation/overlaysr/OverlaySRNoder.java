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

import org.locationtech.jts.algorithm.Orientation;
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
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.noding.snapround.MCIndexSnapRounder;

public class OverlaySRNoder {

  private PrecisionModel pm;
  List<NodedSegmentString> segStrings = new ArrayList<NodedSegmentString>();

  public OverlaySRNoder(PrecisionModel pm) {
    this.pm = pm;
  }

  public Collection<SegmentString> node() {
    Noder sr = new MCIndexSnapRounder(pm);
    sr.computeNodes(segStrings);
    
    //TODO: merge duplicate edges
    @SuppressWarnings("unchecked")
    Collection<SegmentString> nodedSS = sr.getNodedSubstrings();
    return nodedSS;
  }

  public void add(Geometry g, int geomIndex)
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
            (LinearRing) p.getExteriorRing(),
            Location.EXTERIOR, Location.INTERIOR, geomIndex);

    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) p.getInteriorRingN(i);
      
      // Holes are topologically labelled opposite to the shell, since
      // the interior of the polygon lies on their opposite side
      // (on the left, if the hole is oriented CW)
      addPolygonRing(hole,
          Location.INTERIOR, Location.EXTERIOR, geomIndex);
    }
  }
  
  /**
   * Adds a polygon ring to the graph.
   * Empty rings are ignored.
   * 
   * The left and right topological location arguments assume that the ring is oriented CW.
   * If the ring is in the opposite orientation,
   * the left and right locations must be interchanged.
   */
  private void addPolygonRing(LinearRing lr, int cwLeft, int cwRight, int index)
  {
    // don't add empty holes
    if (lr.isEmpty()) return;
    
    Coordinate[] pts = round(lr.getCoordinates(), 4);

    int left  = cwLeft;
    int right = cwRight;
    if (Orientation.isCCW(pts)) {
      left = cwRight;
      right = cwLeft;
    }
    OverlayLabel lbl = OverlayLabel.createRingLabel(index, left, right);
    add(pts, lbl);
  }

  private void addLineString(LineString line, int geomIndex)
  {
    // don't add empty lines
    if (line.isEmpty()) return;
    
    Coordinate[] pts = round(line.getCoordinates(), 1);

    if (pts.length < 2) {
      // don't bother adding collapsed lines
      return;
    }
    OverlayLabel lbl = OverlayLabel.createLineLabel(geomIndex);
    add(pts, lbl);
  }
  
  private void add(Coordinate[] pts, OverlayLabel label) {
    NodedSegmentString ss = new NodedSegmentString(pts, label);
    segStrings.add(ss);
  }

  private Coordinate[] round(Coordinate[] pts, int minLength)  {
    CoordinateList noRepeatCoordList = new CoordinateList();

    for (int i = 0; i < pts.length; i++) {
      Coordinate coord = new Coordinate(pts[i]);
      pm.makePrecise(coord);
      noRepeatCoordList.add(coord, false);
    }
    Coordinate[] reducedPts = noRepeatCoordList.toCoordinateArray();
    if (reducedPts.length < minLength) {
      return pad(reducedPts, minLength);
    }
    return reducedPts;
  }

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
