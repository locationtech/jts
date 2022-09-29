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

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

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
  
  public static Geometry[] simplifyInner(Geometry[] coverage, double tolerance) {
    CoverageSimplifier simplifier = new CoverageSimplifier(coverage);
    return simplifier.simplifyInner(tolerance);
  }
  
  private Geometry[] input;
  private GeometryFactory geomFactory;
  //private LineSimplifier simplifier;
  
  public CoverageSimplifier(Geometry[] coverage) {
    input = coverage;
    geomFactory = coverage[0].getFactory();
  }
  
  public Geometry[] simplify(double tolerance) {
    CoverageRingEdges cov = CoverageRingEdges.create(input);
    simplifyEdges(cov.getEdges(), null, tolerance);
    Geometry[] result = cov.buildCoverage();
    return result;
  }
  
  public Geometry[] simplifyInner(double tolerance) {
    CoverageRingEdges cov = CoverageRingEdges.create(input);
    List<CoverageEdge> innerEdges = cov.selectEdges(2);
    
    List<CoverageEdge> outerEdges = cov.selectEdges(1);
    MultiLineString constraint = createLines(outerEdges);
    
    simplifyEdges(innerEdges, constraint, tolerance);
    Geometry[] result = cov.buildCoverage();
    return result;
  }

  private void simplifyEdges(List<CoverageEdge> edges, MultiLineString constraints, double tolerance) {
    MultiLineString mls = createLines(edges);
    MultiLineString mlsSimp = TPVWSimplifier.simplify(mls, constraints, tolerance);
    //Assert: mlsSimp.getNumGeometries = edges.length
    
    for (int i = 0; i < edges.size(); i++) {
      edges.get(i).setCoordinates(mlsSimp.getGeometryN(i).getCoordinates());
    }
  }

  private MultiLineString createLines(List<CoverageEdge> edges) {
    LineString lines[] = new LineString[edges.size()];
    for (int i = 0; i < edges.size(); i++) {
      lines[i] = geomFactory.createLineString(edges.get(i).getCoordinates());
    }
    MultiLineString mls = geomFactory.createMultiLineString(lines);
    return mls;
  }
  
}
