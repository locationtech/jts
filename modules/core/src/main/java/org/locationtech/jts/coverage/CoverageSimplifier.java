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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;

/**
 * Simplifies the boundaries of the polygons in a polygonal coverage
 * while preserving the original coverage topology.
 * An area-based simplification algorithm is used to provide
 * high-quality results.
 * 
 * @author Martin Davis
 *
 */
public class CoverageSimplifier {
  
  public static Geometry[] simplify(Geometry[] coverage, double tolerance) {
    CoverageSimplifier simplifier = new CoverageSimplifier(coverage);
    return simplifier.simplify(tolerance);
  }
  
  Geometry[] input;
  
  public CoverageSimplifier(Geometry[] coverage) {
    input = coverage;
  }
  
  public Geometry[] simplify(double tolerance) {
    CoverageRingEdges covEdges = CoverageRingEdges.create(input);
    simplifyEdges(covEdges.getEdges(), tolerance);
    Geometry[] result = covEdges.buildCoverage();
    return result;
  }
  
  private void simplifyEdges(List<CoverageEdge> list, double tolerance) {
    //TODO: implement
  }

}
