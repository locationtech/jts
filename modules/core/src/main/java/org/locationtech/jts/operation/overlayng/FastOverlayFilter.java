package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Geometry;

public class FastOverlayFilter {
  // superceded by rectangle clipping
  
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
   * @return
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
