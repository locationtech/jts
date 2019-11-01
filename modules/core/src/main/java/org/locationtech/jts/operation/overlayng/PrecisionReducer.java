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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Functions to reduce the precision of a geometry
 * by rounding it to a given precision model.
 * 
 * @author mdavis
 *
 */
public class PrecisionReducer {

  /**
   * Reduces the precision of a geometry by rounding it to the
   * supplied precision model.
   * <p> 
   * The output is always a valid geometry.  This implies that input components
   * may end up being merged together if they are closer than the grid precision.
   * if merging is not desired, then the individual geometry components
   * should be processed separately.
   * <p>
   * The output is fully noded.  
   * This is an effective way to node / snap-round a collection of {@link LineString}s.
   * 
   * @param geom the geometry to reduce
   * @param pm the precision model to use
   * @return the precision-reduced geometry
   */
  public static Geometry reducePrecision(Geometry geom, PrecisionModel pm) {
    Point emptyPoint = geom.getFactory().createPoint();
    Geometry reduced = OverlayNG.union(geom, pm);
    return reduced;
  }

}
