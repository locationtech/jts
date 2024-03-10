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

import org.locationtech.jts.coverage.CoverageGapFinder;
import org.locationtech.jts.coverage.CoveragePolygonValidator;
import org.locationtech.jts.coverage.CoverageSimplifier;
import org.locationtech.jts.coverage.CoverageUnion;
import org.locationtech.jts.coverage.CoverageValidator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jtstest.geomfunction.Metadata;

public class CoverageFunctions {
  
  public static Geometry validatePolygon(Geometry geom, Geometry adjacentPolys) {
    return CoveragePolygonValidator.validate(geom, toGeometryArray(adjacentPolys));
  }
  
  public static Geometry validatePolygonWithGaps(Geometry geom, Geometry adjacentPolys, 
      @Metadata(title="Gap width")
      double gapWidth) {
    return CoveragePolygonValidator.validate(geom, toGeometryArray(adjacentPolys), gapWidth);
  }
  
  public static Geometry validate(Geometry geom) {
    Geometry[] invalid = CoverageValidator.validate(toGeometryArray(geom));
    return FunctionsUtil.buildGeometryCollection(invalid, geom.getFactory().createLineString());
  }

  public static Geometry validateWithGaps(Geometry geom, 
      @Metadata(title="Gap width")
      double gapWidth) {
    Geometry[] invalid = CoverageValidator.validate(toGeometryArray(geom), gapWidth);
    return FunctionsUtil.buildGeometryCollection(invalid, geom.getFactory().createLineString());
  }

  public static Geometry findGaps(Geometry geom, 
      @Metadata(title="Gap width")
      double gapWidth) {
    return CoverageGapFinder.findGaps(toGeometryArray(geom),gapWidth);
  }

  @Metadata(description="Fast Union of a coverage")
  public static Geometry union(Geometry coverage) {
    Geometry[] cov = toGeometryArray(coverage);
    return CoverageUnion.union(cov);
  }
  
  @Metadata(description="Simplify a coverage")
  public static Geometry simplify(Geometry coverage, double tolerance) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageSimplifier.simplify(cov, tolerance);
    return FunctionsUtil.buildGeometry(result);
  }

  @Metadata(description="Simplify a coverage by providing one tolerance per geometry")
  public static Geometry simplifyDynamicTolerance(Geometry coverage, String tolerances) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageSimplifier.simplify(cov, tolerances);
    return FunctionsUtil.buildGeometry(result);
  }
  
  @Metadata(description="Simplify inner edges of a coverage")
  public static Geometry simplifyinner(Geometry coverage, double tolerance) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageSimplifier.simplifyInner(cov, tolerance);
    return FunctionsUtil.buildGeometry(result);
  }

  @Metadata(description="Simplify inner edges of a coverage by providing one tolerance per geometry")
  public static Geometry simplifyinnerDynamicTolerance(Geometry coverage, String tolerances) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageSimplifier.simplifyInner(cov, tolerances);
    return FunctionsUtil.buildGeometry(result);
  }
  
  static Geometry extractPolygons(Geometry geom) {
    List components = PolygonExtracter.getPolygons(geom);
    Geometry result = geom.getFactory().buildGeometry(components);
    return result;
  }
  
  private static Geometry[] toGeometryArray(Geometry geom) {
    Geometry[] geoms = new Geometry[geom.getNumGeometries()];
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      geoms[i]= geom.getGeometryN(i);
    }
    return geoms;
  }
  
}
