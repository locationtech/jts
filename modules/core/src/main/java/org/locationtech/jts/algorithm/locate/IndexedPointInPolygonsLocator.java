/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.locate;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.util.PolygonalExtracter;
import org.locationtech.jts.index.strtree.STRtree;

/**
 * Determines the location of a point in the polygonal elements of a geometry.
 * The polygons may overlap.
 * Uses spatial indexing to provide efficient performance.
 * 
 * @author mdavis
 * 
 * @see IndexedPointInAreaLocator
 *
 */
public class IndexedPointInPolygonsLocator implements PointOnGeometryLocator {

  private Geometry geom;
  private STRtree index;

  public IndexedPointInPolygonsLocator(Geometry geom) {
    this.geom = geom;
  }
  
  private void init() {
    if (index != null)
      return;
    List<Geometry> polys = PolygonalExtracter.getPolygonals(geom);
    index = new STRtree();
    for (int i = 0; i < polys.size(); i++) {
      Geometry poly = polys.get(i);
      index.insert(poly.getEnvelopeInternal(), new IndexedPointInAreaLocator(poly));
    }
  }
  
  @Override
  public int locate(Coordinate p) {
    init();

    List<IndexedPointInAreaLocator> results = index.query(new Envelope(p));
    for (IndexedPointInAreaLocator ptLocater : results) {
      int loc = ptLocater.locate(p);
      if (loc != Location.EXTERIOR)
        return loc;
    }
    return Location.EXTERIOR;
  }

}
