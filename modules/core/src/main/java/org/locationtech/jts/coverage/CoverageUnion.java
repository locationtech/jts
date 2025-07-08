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
import org.locationtech.jts.geom.TopologyException;

/**
 * Unions a valid polygonal coverage in an efficient way.
 * <p>
 * No checking is done to determine whether the input is a valid coverage.
 * If the input is not a valid coverage 
 * then in <i>some</i> cases this will be detected during processing 
 * and a {@link org.locationtech.jts.geom.TopologyException} is thrown.
 * Otherwise, the computation will produce output, but it will be invalid.
 * 
 * @author Martin Davis
 * 
 * @see CoverageValidator
 *
 */
public class CoverageUnion {
  /**
   * Unions a polygonal coverage.
   * 
   * @param coverage the polygons in the coverage
   * @return the union of the coverage polygons
   *
   * @throws TopologyException in some cases if the coverage is invalid
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
