/*
 * Copyright (c) 2020 Martin Davis, and others.
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

import org.locationtech.jts.geom.Geometry;

public class FastOverlayFilter {
  // superceded by overlap clipping?
  // TODO: perhaps change this to RectangleClipping, with fast/looser semantics?
  
  private Geometry targetGeom;
  private boolean isTargetRectangle;

  public FastOverlayFilter(Geometry geom) {
    this.targetGeom = geom;
    isTargetRectangle = targetGeom.isRectangle();
  }
  
  /**
   * Computes the overlay operation on the input geometries,
   * if it can be determined that the result is either
   * empty or equal to one of the input values.
   * Otherwise <code>null</code> is returned, indicating
   * that a full overlay operation must be performed.
   * 
   * @param geom
   * @param overlayOpCode
   * @return overlay of the input geometries
   */
  public Geometry overlay(Geometry geom, int overlayOpCode) {
    // for now only INTERSECTION is handled
    if (overlayOpCode != OverlayNG.INTERSECTION)
      return null;
    return intersection(geom);
  }

  private Geometry intersection(Geometry geom) {
    // handle rectangle case
    Geometry resultForRect = intersectionRectangle(geom);
    if (resultForRect != null)
      return resultForRect;
    
    // handle general case
    if ( ! isEnvelopeIntersects(targetGeom, geom) ) {
      return createEmpty(geom);
    }
    
    return null;
  }

  private Geometry createEmpty(Geometry geom) {
    // empty result has dimension of non-rectangle input
    return OverlayUtil.createEmptyResult(geom.getDimension(), geom.getFactory());
  }

  private Geometry intersectionRectangle(Geometry geom) {
    if (! isTargetRectangle)
      return null;
    
    if ( isEnvelopeCovers(targetGeom, geom) ) {
      return geom.copy();
    }
    if ( ! isEnvelopeIntersects(targetGeom, geom) ) {
      return createEmpty(geom);
    }
    return null;
  }

  private boolean isEnvelopeIntersects(Geometry a, Geometry b) {
    return a.getEnvelopeInternal().intersects( b.getEnvelopeInternal() );
  }

  private boolean isEnvelopeCovers(Geometry a, Geometry b) {
    return a.getEnvelopeInternal().covers( b.getEnvelopeInternal() );
  }
}
