/*
 * Copyright (c) 2022 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * Unions a polygonal coverage in an efficient way.
 * <p>
 * Valid polygonal coverage topology allows merging polygons in a very efficient way.
 * 
 * @author Martin Davis
 *
 */
public class CoverageUnion {
  /**
   * Unions a polygonal coverage.
   * 
   * @param coverage the polygons in the coverage
   * @return the union of the coverage polygons
   */
  public static Geometry union(Geometry[] coverage) {
    // union of an empty coverage is null, since no factory is available
    if (coverage.length == 0)
      return null;
    
    GeometryFactory geomFact = coverage[0].getFactory();
    GeometryCollection geoms = geomFact.createGeometryCollection(coverage);
    return org.locationtech.jts.operation.overlayng.CoverageUnion.union(geoms);
  }
}
