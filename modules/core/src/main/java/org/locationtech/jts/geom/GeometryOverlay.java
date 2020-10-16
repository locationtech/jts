/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom;

import org.locationtech.jts.geom.util.GeometryCollectionMapper;
import org.locationtech.jts.geom.util.GeometryMapper;
import org.locationtech.jts.operation.overlay.OverlayOp;
import org.locationtech.jts.operation.overlay.snap.SnapIfNeededOverlayOp;
import org.locationtech.jts.operation.union.UnaryUnionOp;

class GeometryOverlay {
  static Geometry difference(Geometry a, Geometry b)
  {
    // special case: if A.isEmpty ==> empty; if B.isEmpty ==> A
    if (a.isEmpty()) return OverlayOp.createEmptyResult(OverlayOp.DIFFERENCE, a, b, a.getFactory());
    if (b.isEmpty()) return a.copy();

    Geometry.checkNotGeometryCollection(a);
    Geometry.checkNotGeometryCollection(b);
    return SnapIfNeededOverlayOp.overlayOp(a, b, OverlayOp.DIFFERENCE);
  }
  
  static Geometry intersection(Geometry a, Geometry b)
  {
    /**
     * TODO: MD - add optimization for P-A case using Point-In-Polygon
     */
    // special case: if one input is empty ==> empty
    if (a.isEmpty() || b.isEmpty())
      return OverlayOp.createEmptyResult(OverlayOp.INTERSECTION, a, b, a.getFactory());

    // compute for GCs
    // (An inefficient algorithm, but will work)
    // TODO: improve efficiency of computation for GCs
    if (a.isGeometryCollection()) {
      final Geometry g2 = b;
      return GeometryCollectionMapper.map(
          (GeometryCollection) a,
          new GeometryMapper.MapOp() {
            public Geometry map(Geometry g) {
              return g.intersection(g2);
            }
      });
    }

    // No longer needed since GCs are handled by previous code
    //checkNotGeometryCollection(this);
    //checkNotGeometryCollection(other);
    return SnapIfNeededOverlayOp.overlayOp(a, b, OverlayOp.INTERSECTION);
  }

  static Geometry symDifference(Geometry a, Geometry b)
  {
    // handle empty geometry cases
    if (a.isEmpty() || b.isEmpty()) {
      // both empty - check dimensions
      if (a.isEmpty() && b.isEmpty())
        return OverlayOp.createEmptyResult(OverlayOp.SYMDIFFERENCE, a, b, a.getFactory());

    // special case: if either input is empty ==> result = other arg
      if (a.isEmpty()) return b.copy();
      if (b.isEmpty()) return a.copy();
    }

    Geometry.checkNotGeometryCollection(a);
    Geometry.checkNotGeometryCollection(b);
    return SnapIfNeededOverlayOp.overlayOp(a, b, OverlayOp.SYMDIFFERENCE);
  }
  
  static Geometry union(Geometry a, Geometry b)
  {
    // handle empty geometry cases
    if (a.isEmpty() || a.isEmpty()) {
      if (b.isEmpty() && b.isEmpty())
        return OverlayOp.createEmptyResult(OverlayOp.UNION, a, b, a.getFactory());

    // special case: if either input is empty ==> other input
      if (a.isEmpty()) return b.copy();
      if (b.isEmpty()) return a.copy();
    }

    // TODO: optimize if envelopes of geometries do not intersect

    Geometry.checkNotGeometryCollection(a);
    Geometry.checkNotGeometryCollection(b);
    return SnapIfNeededOverlayOp.overlayOp(a, b, OverlayOp.UNION);
  }
  
  static Geometry union(Geometry a) {
    return UnaryUnionOp.union(a);
  }
}
