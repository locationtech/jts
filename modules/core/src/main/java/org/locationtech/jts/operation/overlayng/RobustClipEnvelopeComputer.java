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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

/**
 * Computes a robust clipping envelope for a pair of polygonal geometries.
 * The envelope is computed to be large enough to include the full
 * length of all geometry line segments which intersect 
 * a given target envelope.
 * This ensures that line segments which might intersect are
 * not perturbed when clipped using {@link RingClipper}.
 *  
 * @author Martin Davis
 *
 */
class RobustClipEnvelopeComputer {
  
  public static Envelope getEnvelope(Geometry a, Geometry b, Envelope targetEnv) {
    RobustClipEnvelopeComputer cec = new RobustClipEnvelopeComputer(targetEnv);
    cec.add(a);
    cec.add(b);
    return cec.getEnvelope();
  }
  
  private Envelope targetEnv;
  private Envelope clipEnv;

  public RobustClipEnvelopeComputer(Envelope targetEnv) {
    this.targetEnv = targetEnv;
    clipEnv = targetEnv.copy();
  }

  public Envelope getEnvelope() {
    return clipEnv;
  }
  
  public void add(Geometry g) {
    if ( g == null || g.isEmpty() )
      return;

    if ( g instanceof Polygon )
      addPolygon((Polygon) g);
    else if ( g instanceof GeometryCollection )
      addCollection((GeometryCollection) g);
  }

  private void addCollection(GeometryCollection gc) {
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Geometry g = gc.getGeometryN(i);
      add(g);
    }
  }

  private void addPolygon(Polygon poly) {
    LinearRing shell = poly.getExteriorRing();
    addPolygonRing(shell);

    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      LinearRing hole = poly.getInteriorRingN(i);
      addPolygonRing(hole);
    }
  }

  /**
   * Adds a polygon ring to the graph. Empty rings are ignored.
   */
  private void addPolygonRing(LinearRing ring) {
    // don't add empty lines
    if ( ring.isEmpty() )
      return;

    CoordinateSequence seq = ring.getCoordinateSequence();
    for (int i = 1; i < seq.size(); i++) {
      addSegment(seq.getCoordinate(i - 1), seq.getCoordinate(i));
    }
  }

  private void addSegment(Coordinate p1, Coordinate p2) {
    if (intersectsSegment(targetEnv, p1, p2)) {
      clipEnv.expandToInclude(p1);
      clipEnv.expandToInclude(p2);
    }
  }

  private static boolean intersectsSegment(Envelope env, Coordinate p1, Coordinate p2) {
    /**
     * This is a crude test of whether segment intersects envelope.
     * It could be refined by checking exact intersection.
     * This could be based on the algorithm in the HotPixel.intersectsScaled method.
     */
    return env.intersects(p1, p2);
  }
}
