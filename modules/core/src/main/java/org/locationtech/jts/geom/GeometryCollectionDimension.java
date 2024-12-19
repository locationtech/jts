/*
 * Copyright (c) 2024 Martin Davis.
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

import java.util.Iterator;

/**
 * Computes and caches dimension information for {@link GeometryCollection}s.
 * Optimizes performance of dimension reporting for heterogeneous collections.
 * 
 * @author mdavis
 */
class GeometryCollectionDimension {
  private int dimension = Dimension.FALSE;
  private boolean hasP = false;
  private boolean hasL = false;
  private boolean hasA = false;

  public GeometryCollectionDimension(GeometryCollection coll) {
    init(coll);
  }

  private void init(GeometryCollection coll) {
    Iterator geomi = new GeometryCollectionIterator(coll);
    while (geomi.hasNext()) {
      Geometry elem = (Geometry) geomi.next();
      //-- empty elements still determine dimension, to match previous semantics
      if (elem instanceof Point) {
        hasP = true;
        if (dimension < Dimension.P) dimension = Dimension.P;
      }
      if (elem instanceof LineString) {
        hasL = true;
        if (dimension < Dimension.L) dimension = Dimension.L;
      }
      if (elem instanceof Polygon) {
        hasA = true;
        if (dimension < Dimension.A) dimension = Dimension.A;
      }
    }
  }
  
  public boolean hasDimension(int dim) {
    if (dim == Dimension.A && hasA) return true;
    if (dim == Dimension.L && hasL) return true;
    if (dim == Dimension.P && hasP) return true;
    return false;
  }

  public int getDimension() {
    return dimension;
  }
  
}
