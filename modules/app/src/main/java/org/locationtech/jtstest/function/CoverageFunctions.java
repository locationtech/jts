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
package org.locationtech.jtstest.function;

import java.util.List;

import org.locationtech.jts.coverage.CoverageValidator;
import org.locationtech.jts.coverage.CoveragePolygonValidator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.operation.overlayng.CoverageUnion;
import org.locationtech.jtstest.geomfunction.Metadata;

public class CoverageFunctions {
  
  public static Geometry validatePolygon(Geometry geom, Geometry surround) {
    return CoveragePolygonValidator.validate(geom, surround);
  }
  
  public static Geometry validate(Geometry coverage) {
    return CoverageValidator.validate(coverage);
  }
  
  @Metadata(description="Fast Union of a coverage")
  public static Geometry unionCoverage(Geometry geom) {
    Geometry cov = extractPolygons(geom);
    return CoverageUnion.union(cov);
  }
  
  static Geometry extractPolygons(Geometry geom) {
    List components = PolygonExtracter.getPolygons(geom);
    Geometry result = geom.getFactory().buildGeometry(components);
    return result;
  }
}
