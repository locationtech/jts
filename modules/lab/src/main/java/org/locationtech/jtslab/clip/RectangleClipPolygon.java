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
package org.locationtech.jtslab.clip;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
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
  
  public RectangleClipPolygon(Geometry clipRectangle) {
    this(clipRectangle, new PrecisionModel(PrecisionModel.FLOATING));
  }
  
  public RectangleClipPolygon(Geometry clipRectangle, PrecisionModel pm) {
    clipEnv = clipRectangle.getEnvelopeInternal();
    clipEnvMinY = clipEnv.getMinY();
    clipEnvMaxY = clipEnv.getMaxY();
    clipEnvMinX = clipEnv.getMinX();
    clipEnvMaxX = clipEnv.getMaxX();
    
    precModel = pm;
  }

  public Geometry clip(Geometry geom) {
    // TODO: handle MultiPolygons
    Polygon polyClip = clipPolygon((Polygon) geom);
    return fixTopology(polyClip);
  }

  /**
   * The clipped geometry may be invalid
   * (due to coincident linework from clipping to edges, 
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

  private Polygon clipPolygon(Polygon poly) {
    LinearRing shell = poly.getExteriorRing();
    LinearRing shellClip = clipRing(shell);
    
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
    return poly.getFactory().toLinearRingArray(holesClip);
  }

  private LinearRing clipRing(LinearRing ring) {
    Coordinate[] pts = clipRingToBox(ring.getCoordinates()); 
    return ring.getFactory().createLinearRing(pts);
  }

  /**
   * Clips ring to rectangle box.
   * This follows the Sutherland-Hodgson algorithm.
   * 
   * @param ring
   * @param env
   * @return
   */
  private Coordinate[] clipRingToBox(Coordinate[] ring) {
    Coordinate[] coords = ring;
    for (int edgeIndex = 0; edgeIndex < 4; edgeIndex++) {
      coords = clipRingToBoxEdge(coords, edgeIndex);
    }
    return coords;
  }

  /**
   * Clips ring to a axis-parallel line defined by a single box edge.
   * 
   * @param coords
   * @param edgeIndex
   * @return
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
        }
        // TODO: avoid copying so much?
        clipCoords.add( makePrecise(p1.copy()), false);
        
      } else if ( isInsideEdge(p0, edgeIndex) ) {
        Coordinate intPt = intersectionPrecise(p0, p1, edgeIndex);
        clipCoords.add( intPt, false);
      }
      p0 = p1;
    }
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
    double m = (b.x - a.x) / (b.y - a.y);
    double intercept = (y - a.y) * m;
    return a.x + intercept;
  }

  private double intersectionLineX(Coordinate a, Coordinate b, double x) {
    double m = (b.y - a.y) / (b.x - a.x);
    double intercept = (x - a.x) * m;
    return a.y + intercept;
  }

  private boolean isInsideEdge(Coordinate p, int edgeIndex) {
    switch (edgeIndex) {
    case ENV_BOTTOM: // bottom
      return p.y > clipEnvMinY;
    case ENV_RIGHT: // right
      return p.x < clipEnvMaxX;
    case ENV_TOP: // top
      return p.y < clipEnvMaxY;
    case ENV_LEFT:
    default: // left
      return p.x > clipEnvMinX;
    }
  }

}
;;;