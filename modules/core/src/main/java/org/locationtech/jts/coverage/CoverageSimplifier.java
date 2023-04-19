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

import java.util.BitSet;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
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
 * which is a non-negative quantity. It equates roughly to the maximum
 * distance by which a simplified line can change from the original.
 * (In fact, it is the square root of the area tolerance used 
 * in the Visvalingam-Whyatt algorithm.)
 * <p>
 * The simplified result coverage has the following characteristics:
 * <ul>
 * <li>It has the same number and types of polygonal geometries as the input
 * <li>Node points (inner vertices shared by three or more polygons, 
 *     or boundary vertices shared by two or more) are not changed
 * <li>If the input is a valid coverage, then so is the result
 * </ul>
 * This class also supports inner simplification, which simplifies
 * only edges of the coverage which are adjacent to two polygons.
 * This allows partial simplification of a coverage, since a simplified
 * subset of a coverage still matches the remainder of the coverage.
 * <p>
 * The input coverage should be valid according to {@link CoverageValidator}.
 * Invalid coverages may still be simplified, but the result will still be invalid.
 * 
 * @author Martin Davis
 */
public class CoverageSimplifier {
  
  /**
   * Simplifies the boundaries of a set of polygonal geometries forming a coverage,
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
   * Simplifies the inner boundaries of a set of polygonal geometries forming a coverage,
   * preserving the coverage topology.
   * Edges which form the exterior boundary of the coverage are left unchanged.
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
   * Create a new coverage simplifier instance.
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
   * preserving the coverage topology,
   * and leaving outer boundary edges unchanged.
   * 
   * @param tolerance the simplification tolerance
   * @return the simplified polygons
   */
  public Geometry[] simplifyInner(double tolerance) {
    CoverageRingEdges cov = CoverageRingEdges.create(input);
    List<CoverageEdge> innerEdges = cov.selectEdges(2);
    List<CoverageEdge> outerEdges = cov.selectEdges(1);
    MultiLineString constraintEdges = CoverageEdge.createLines(outerEdges, geomFactory);

    simplifyEdges(innerEdges, constraintEdges, tolerance);
    Geometry[] result = cov.buildCoverage();
    return result;
  }

  private void simplifyEdges(List<CoverageEdge> edges, MultiLineString constraints, double tolerance) {
    MultiLineString lines = CoverageEdge.createLines(edges, geomFactory);
    BitSet freeRings = getFreeRings(edges);
    MultiLineString linesSimp = TPVWSimplifier.simplify(lines, freeRings, constraints, tolerance);
    //Assert: mlsSimp.getNumGeometries = edges.length
    
    setCoordinates(edges, linesSimp);
  }

  private void setCoordinates(List<CoverageEdge> edges, MultiLineString lines) {
    for (int i = 0; i < edges.size(); i++) {
      edges.get(i).setCoordinates(lines.getGeometryN(i).getCoordinates());
    }
  }

  private BitSet getFreeRings(List<CoverageEdge> edges) {
    BitSet freeRings = new BitSet(edges.size());
    for (int i = 0 ; i < edges.size() ; i++) {
      freeRings.set(i, edges.get(i).isFreeRing());
    }
    return freeRings;
  }
  
}
