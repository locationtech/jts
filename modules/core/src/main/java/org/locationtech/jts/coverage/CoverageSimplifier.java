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
 * An area-based simplification algorithm 
 * (similar to Visvalingam-Whyatt simplification)
 * is used to provide high-quality results.
 * Also supports simplifying just the inner edges in a coverage,
 * which allows simplifying "patches" without affecting their boundary.
 * <p>
 * The amount of simplification is determined by a tolerance value, 
 * which is a non-zero quantity. It equates roughly to the maximum
 * distance by which a simplfied line can change from the original.
 * (In fact, it is the square root of the area tolerance used 
 * in the Visvalingam-Whyatt algorithm.)
 * 
 * @author Martin Davis
 */
public class CoverageSimplifier {
  
  /**
   * Simplify the boundaries of a set of polygonal geometries forming a coverage,
   * preserving the coverage topology.
   * 
   * @param coverage a set of polygonal geometries forming a coverage
   * @param tolerance the simplification tolerance
   * @return the simplified polygons
   */
  public static Geometry[] simplify(Geometry[] coverage, double tolerance) {
    CoverageSimplifier simplifier = new CoverageSimplifier(coverage);
    return simplifier.simplify(tolerance);
  }
  
  /**
   * Simplify the inner boundaries of a set of polygonal geometries forming a coverage,
   * preserving the coverage topology.
   * 
   * @param coverage a set of polygonal geometries forming a coverage
   * @param tolerance the simplification tolerance
   * @return the simplified polygons
   */
  public static Geometry[] simplifyInner(Geometry[] coverage, double tolerance) {
    CoverageSimplifier simplifier = new CoverageSimplifier(coverage);
    return simplifier.simplifyInner(tolerance);
  }
  
  private Geometry[] input;
  private GeometryFactory geomFactory;
  
  /**
   * Create a new simplifier instance.
   * 
   * @param coverage a set of polygonal geometries forming a coverage
   */
  public CoverageSimplifier(Geometry[] coverage) {
    input = coverage;
    geomFactory = coverage[0].getFactory();
  }
  
  /**
   * Computes the simplified coverage, preserving the coverage topology.
   * 
   * @param tolerance the simplification tolerance
   * @return the simplified polygons
   */
  public Geometry[] simplify(double tolerance) {
    CoverageRingEdges cov = CoverageRingEdges.create(input);
    simplifyEdges(cov.getEdges(), null, tolerance);
    Geometry[] result = cov.buildCoverage();
    return result;
  }
  
  /**
   * Computes the inner-boundary simplified coverage,
   * preserving the coverage topology.
   * 
   * @param tolerance the simplification tolerance
   * @return the simplified polygons
   */
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
    MultiLineString lines = createLines(edges);
    MultiLineString linesSimp = TPVWSimplifier.simplify(lines, constraints, tolerance);
    //Assert: mlsSimp.getNumGeometries = edges.length
    
    setCoordinates(edges, linesSimp);
  }

  private void setCoordinates(List<CoverageEdge> edges, MultiLineString lines) {
    for (int i = 0; i < edges.size(); i++) {
      edges.get(i).setCoordinates(lines.getGeometryN(i).getCoordinates());
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
