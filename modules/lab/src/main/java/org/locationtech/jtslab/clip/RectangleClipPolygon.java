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
package org.locationtech.jtslab.clip;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Clips polygonal geometry to a rectangle.
 * This implementation is faster, more robust, 
 * and less sensitive to invalid input than {@link Geometry#intersection(Geometry)}.
 * 
 * It can also enforce a supplied precision model on the computed result.
 * The inputs do not have to meet the precision model.
 * This allows clipping using integer coordinates
 * in the output, for example.
 * 
 * @author mdavis
 *
 */
public class RectangleClipPolygon {
  
  private static final int ENV_LEFT = 3;
  private static final int ENV_TOP = 2;
  private static final int ENV_RIGHT = 1;
  private static final int ENV_BOTTOM = 0;

  public static Geometry clip(Geometry geom, Geometry rectangle) {
    RectangleClipPolygon ctt = new RectangleClipPolygon(rectangle);
    Geometry result = ctt.clip(geom);
    return result;
  }

  public static Geometry clip(Geometry geom, Geometry rectangle, PrecisionModel pm) {
    RectangleClipPolygon ctt = new RectangleClipPolygon(rectangle, pm);
    Geometry result = ctt.clip(geom);
    return result;
  }

  private Envelope clipEnv;
  private double clipEnvMinY;
  private double clipEnvMaxY;
  private double clipEnvMinX;
  private double clipEnvMaxX;
  private PrecisionModel precModel;
  
  public RectangleClipPolygon(Envelope clipEnv) {
    this(clipEnv, new PrecisionModel(PrecisionModel.FLOATING));
  }
  
  public RectangleClipPolygon(Geometry clipRectangle) {
    this(clipRectangle, new PrecisionModel(PrecisionModel.FLOATING));
  }
  
  public RectangleClipPolygon(Geometry clipRectangle, PrecisionModel pm) {
    this(clipRectangle.getEnvelopeInternal(), pm);
  }

  public RectangleClipPolygon(Envelope clipEnv, PrecisionModel pm) {
    this.clipEnv = clipEnv;
    clipEnvMinY = clipEnv.getMinY();
    clipEnvMaxY = clipEnv.getMaxY();
    clipEnvMinX = clipEnv.getMinX();
    clipEnvMaxX = clipEnv.getMaxX();
    
    precModel = pm;
  }

  public Geometry clip(Geometry geom) {
    Geometry geomsClip = clipCollection(geom);
    
    if (geomsClip == null) {
      return geom.getFactory().createPolygon();
    }
    
    return fixTopology(geomsClip);
  }

  /**
   * The clipped geometry may be invalid
   * (due to coincident linework at clip edges, 
   * or due to precision reduction if performed).
   * This method fixed the geometry topology to be valid.
   * 
   * Currently uses the buffer(0) trick.
   * This should work in most cases (but need to verify this).
   * But it may produce unexpected results if the input polygon
   * was invalid inside the clip area.
   * 
   * @param geom
   * @return
   * 
   * @see GeometryPrecisionReducer
   */
  private Geometry fixTopology(Geometry geom) {
    // TODO: do this in a better way (could use GeometryPrecisionReducer?)
    // TODO: ensure this meets required precision model (see GeometryPrecisionReducer)
    return geom.buffer(0);
  }

  public Geometry clipCollection(Geometry geom) {
    if (isOutsideRectangle(geom)) return null;
    // TODO: need to precision reduce
    if (isInsideRectangle(geom)) return geom.copy();

    List<Geometry> geomsClip = new ArrayList<Geometry>(); 
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      Geometry poly = geom.getGeometryN(i);
      if (! (poly instanceof Polygon)) continue;
      Polygon polyClip = clipPolygon((Polygon) poly);
      if (polyClip == null) continue;
      geomsClip.add(polyClip);
    }
    
    if (geomsClip.size() == 0) {
      return null;
    }
    Geometry geomClip = geom.getFactory().buildGeometry(geomsClip);
    return geomClip;
  }

  private Polygon clipPolygon(Polygon poly) {
    if (isOutsideRectangle(poly)) return null;
    // TODO: need to precision reduce
    if (isInsideRectangle(poly)) return (Polygon) poly.copy();

    LinearRing shell = poly.getExteriorRing();
    LinearRing shellClip = clipRing(shell);
    if (shellClip == null) {
      return null;
    }
    LinearRing[] holesClip = clipHoles(poly);
    
    Polygon polyClip = poly.getFactory().createPolygon(shellClip, holesClip);
    return polyClip;
  }
  
  private LinearRing[] clipHoles(Polygon poly) {
    List<LinearRing> holesClip = new ArrayList<LinearRing>();
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      LinearRing holeClip = clipRing(poly.getInteriorRingN(i));
      if (holeClip != null) {
        holesClip.add(holeClip);
      }
    }
    return GeometryFactory.toLinearRingArray(holesClip);
  }

  private LinearRing clipRing(LinearRing ring) {
    if (isOutsideRectangle(ring)) return null;
    // TODO: need to precision reduce
    if (isInsideRectangle(ring)) return (LinearRing) ring.copy();
    
    Coordinate[] pts = clipRingToBox(ring.getCoordinates());
    // check for a collapsed ring
    if (pts == null || pts.length < 4) return null;
    return ring.getFactory().createLinearRing(pts);
  }

  private boolean isInsideRectangle(Geometry geom) {
    return clipEnv.covers(geom.getEnvelopeInternal());
  }

  private boolean isOutsideRectangle(Geometry geom) {
    return ! clipEnv.intersects(geom.getEnvelopeInternal());
  }

  /**
   * Clips ring to rectangle box.
   * This follows the Sutherland-Hodgson algorithm.
   * 
   * @param ring
   * @param env
   * @return the clipped points, or null if all were clipped
   */
  private Coordinate[] clipRingToBox(Coordinate[] ring) {
    Coordinate[] coords = ring;
    for (int edgeIndex = 0; edgeIndex < 4; edgeIndex++) {
      
      /*
       // this is a further optimization to clip entire line
       // but not clear it makes much difference
        if (edgeIndex >= 1 && isInsideEdge(currentCoordsEnv, edgeIndex))
          // all pts inside - skip clipping against this edge
          continue;
      */
      
      //currentCoordsEnv = new Envelope();
      coords = clipRingToBoxEdge(coords, edgeIndex);
      // check if all points clipped off
      if (coords == null) return null;

      //if (isOutsideEdge(currentCoordsEnv, edgeIndex)) return null;
    }
    return coords;
  }
  
  /*
  private boolean isInsideEdge(Envelope env, int edgeIndex) {
    switch (edgeIndex) {
    case ENV_BOTTOM: 
      return env.getMinY() > clipEnvMinY;
    case ENV_RIGHT: 
      return env.getMaxX() < clipEnvMaxX;
    case ENV_TOP: 
      return env.getMaxY() < clipEnvMaxY;
    case ENV_LEFT:
    default: 
      return env.getMinX() > clipEnvMinX;
    }
  }

  private boolean isOutsideEdge(Envelope env, int edgeIndex) {
    switch (edgeIndex) {
    case ENV_BOTTOM: 
      return env.getMaxY() < clipEnvMinY;
    case ENV_RIGHT:
      return env.getMinX() > clipEnvMaxX;
    case ENV_TOP: 
      return env.getMinY() > clipEnvMaxY;
    case ENV_LEFT:
    default: 
      return env.getMaxX() < clipEnvMinX;
    }
  }

  Envelope currentCoordsEnv;
  */
  
  /**
   * Clips ring to an axis-parallel line defined by the given box edge.
   * 
   * @param coords the coordinates for the ring.  Must be closed.
   * @param edgeIndex
   * @return the clipped points, or null if all were clipped
   */
  private Coordinate[] clipRingToBoxEdge(Coordinate[] coords, int edgeIndex) {
    CoordinateList clipCoords = new CoordinateList();

    Coordinate p0 = coords[coords.length - 1];
    for (int i = 0; i < coords.length; i++) {
      Coordinate p1 = coords[i];
      if ( isInsideEdge(p1, edgeIndex) ) {
        if ( !isInsideEdge(p0, edgeIndex) ) {
          Coordinate intPt = intersectionPrecise(p0, p1, edgeIndex);
          clipCoords.add( intPt, false);
          //currentCoordsEnv.expandToInclude(intPt);
        }
        // TODO: avoid copying so much?
        Coordinate p1Precise = makePrecise(p1.copy());
        clipCoords.add( p1Precise, false);
        //currentCoordsEnv.expandToInclude(p1Precise);
        
      } else if ( isInsideEdge(p0, edgeIndex) ) {
        Coordinate intPt = intersectionPrecise(p0, p1, edgeIndex);
        clipCoords.add( intPt, false);
        //currentCoordsEnv.expandToInclude(intPt);
      }
      // move to next segment
      p0 = p1;
    }
    // check if all points clipped off
    if (clipCoords.size() <= 0) return null;
    clipCoords.closeRing();
    return clipCoords.toCoordinateArray();
  }

  private Coordinate makePrecise(Coordinate coord) {
    if (precModel == null) return coord;
    precModel.makePrecise(coord);
    return coord;
  }

  private Coordinate intersectionPrecise(Coordinate a, Coordinate b, int edgeIndex) {
    return makePrecise(intersection(a, b, edgeIndex));
  }
  
  // TODO: test that intersection computatin is robust
  // e.g. how are nearly horizontal/vertical lines handled?
  
  /**
   * 
   * 
   * Due to the nature of the S-H algorithm,
   * it should never happen that the
   * computation of intersection line slope is infinite 
   * (i.e. encounters division-by-zero).
   * 
   * @param a
   * @param b
   * @param edgeIndex
   * @return
   */
  private Coordinate intersection(Coordinate a, Coordinate b, int edgeIndex) {
    switch (edgeIndex) {
    case ENV_BOTTOM:
      return new Coordinate(intersectionLineY(a, b, clipEnvMinY), clipEnvMinY);
    case ENV_RIGHT:
      return new Coordinate(clipEnvMaxX, intersectionLineX(a, b, clipEnvMaxX));
    case ENV_TOP:
      return new Coordinate(intersectionLineY(a, b, clipEnvMaxY), clipEnvMaxY);
    case ENV_LEFT:
    default:
      return new Coordinate(clipEnvMinX, intersectionLineX(a, b, clipEnvMinX));
    }
  }

  private double intersectionLineY(Coordinate a, Coordinate b, double y) {
    // short-circuit segment parallel to Y axis
    if (b.x == a.x) return a.x;
    double m = (b.x - a.x) / (b.y - a.y);
    double intercept = (y - a.y) * m;
    return a.x + intercept;
  }

  private double intersectionLineX(Coordinate a, Coordinate b, double x) {
    // short-circuit segment parallel to X axis
    if (b.y == a.y) return a.y;
    double m = (b.y - a.y) / (b.x - a.x);
    double intercept = (x - a.x) * m;
    return a.y + intercept;
  }

  private boolean isInsideEdge(Coordinate p, int edgeIndex) {
    switch (edgeIndex) {
    case ENV_BOTTOM: 
      return p.y > clipEnvMinY;
    case ENV_RIGHT: 
      return p.x < clipEnvMaxX;
    case ENV_TOP: 
      return p.y < clipEnvMaxY;
    case ENV_LEFT:
    default: 
      return p.x > clipEnvMinX;
    }
  }

}
;;;
