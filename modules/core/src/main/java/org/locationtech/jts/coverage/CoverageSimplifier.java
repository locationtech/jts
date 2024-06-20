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

import org.locationtech.jts.coverage.TPVWSimplifier.Edge;
import org.locationtech.jts.geom.Geometry;

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
 * <li>It has the same number of polygonal geometries as the input
 * <li>If the input is a valid coverage, then so is the result
 * <li>Node points (inner vertices shared by three or more polygons, 
 *     or boundary vertices shared by two or more) are not changed
 * <li>Polygons maintain their line-adjacency (edges are never removed)
 * <li>Rings are simplified to a minimum of 4 vertices, to better preserve their shape
 * <lI>Rings smaller than the area tolerance are removed where possible.
 *  This applies to both holes and "islands" (multipolygon elements
 *  which are disjoint or touch another polygon at a single vertex).
 *  At least one polygon is retained for each input geometry
 *  (the one with largest area).
 * </ul>
 * This class supports simplification using different distance tolerances 
 * for inner and outer edges of the coverage (including no simplfication
 * using a tolerance of 0.0).  
 * This allows, for example, inner simplification, which simplifies
 * only edges of the coverage which are adjacent to two polygons.
 * This allows partial simplification of a coverage, since a simplified
 * subset of a coverage still matches the remainder of the coverage.
 * <p>
 * The class allows specifying a separate tolerance for each element of the input coverage.
 * <p>
 * The input coverage should be valid according to {@link CoverageValidator}.
 * Invalid coverages may be simplified, but the result will likely still be invalid.
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
   * @return the simplified coverage polygons
   */
  public static Geometry[] simplify(Geometry[] coverage, double tolerance) {
    CoverageSimplifier simplifier = new CoverageSimplifier(coverage);
    return simplifier.simplify(tolerance);
  }
  
  /**
   * Simplifies the boundaries of a set of polygonal geometries forming a coverage,
   * preserving the coverage topology, using a separate tolerance
   * for each element of the coverage.
   * Coverage edges are simplified using the lowest tolerance of each adjacent
   * element.
   * 
   * @param coverage a set of polygonal geometries forming a coverage
   * @param tolerance the simplification tolerances (one per input element)
   * @return the simplified coverage polygons
   */
  public static Geometry[] simplify(Geometry[] coverage, double[] tolerances) {
    CoverageSimplifier simplifier = new CoverageSimplifier(coverage);
    return simplifier.simplify(tolerances);
  }
  
  /**
   * Simplifies the inner boundaries of a set of polygonal geometries forming a coverage,
   * preserving the coverage topology.
   * Edges which form the exterior boundary of the coverage are left unchanged.
   * 
   * @param coverage a set of polygonal geometries forming a coverage
   * @param tolerance the simplification tolerance
   * @return the simplified coverage polygons
   */
  public static Geometry[] simplifyInner(Geometry[] coverage, double tolerance) {
    CoverageSimplifier simplifier = new CoverageSimplifier(coverage);
    return simplifier.simplify(tolerance, 0);
  }
  
  /**
   * Simplifies the outer boundaries of a set of polygonal geometries forming a coverage,
   * preserving the coverage topology.
   * Edges in the interior of the coverage are left unchanged.
   * 
   * @param coverage a set of polygonal geometries forming a coverage
   * @param tolerance the simplification tolerance
   * @return the simplified polygons
   */
  public static Geometry[] simplifyOuter(Geometry[] coverage, double tolerance) {
    CoverageSimplifier simplifier = new CoverageSimplifier(coverage);
    return simplifier.simplify(0, tolerance);
  }
  
  private Geometry[] coverage;
  private double smoothWeight = CornerArea.DEFAULT_SMOOTH_WEIGHT;
  private double removableSizeFactor = 1.0;
  
  /**
   * Create a new coverage simplifier instance.
   * 
   * @param coverage a set of polygonal geometries forming a coverage
   */
  public CoverageSimplifier(Geometry[] coverage) {
    this.coverage = coverage;
  }
  
  /**
   * Sets the factor applied to the area tolerance to determine
   * if small rings should be removed.
   * Larger values cause more rings to be removed.
   * A value of 0 prevents rings from being removed.
   * 
   * @param removableSizeFactor the factor to determine ring size to remove
   */
  public void setRemovableRingSizeFactor(double removableSizeFactor) {
    double factor = removableSizeFactor;
    if (factor < 0.0)
      factor = 0.0;
    this.removableSizeFactor = factor;
  }
  
  /**
   * Sets the weight influencing how smooth the simplification should be.
   * The weight must be between 0 and 1.  
   * Larger values increase the smoothness of the simplified edges.
   * 
   * @param smoothWeight a value between 0 and 1
   */
  public void setSmoothWeight(double smoothWeight) {
    if (smoothWeight < 0.0 || smoothWeight > 1.0)
      throw new IllegalArgumentException("smoothWeight must be in range [0 - 1]");
    this.smoothWeight  = smoothWeight;
  }
  
  /**
   * Computes the simplified coverage using a single distance tolerance, 
   * preserving the coverage topology.
   * 
   * @param tolerance the simplification distance tolerance
   * @return the simplified coverage polygons
   */
  public Geometry[] simplify(double tolerance) {
    return simplifyEdges(tolerance, tolerance);
  }

  /**
   * Computes the simplified coverage using separate distance tolerances
   * for inner and outer edges, 
   * preserving the coverage topology.
   * 
   * @param toleranceInner the distance tolerance for inner edges
   * @param toleranceOuter the distance tolerance for outer edges
   * @return the simplified coverage polygons
   */
  public Geometry[] simplify(double toleranceInner, double toleranceOuter) {
    return simplifyEdges(toleranceInner, toleranceOuter);
  }

  /**
   * Computes the simplified coverage using separate distance tolerances
   * for each coverage element, 
   * preserving the coverage topology.
   * 
   * @param tolerances the distance tolerances for the coverage elements
   * @return the simplified coverage polygons
   */
  public Geometry[] simplify(double[] tolerances) {
    if (tolerances.length != coverage.length)
      throw new IllegalArgumentException("number of tolerances does not match number of coverage elements");
    return simplifyEdges(tolerances);
  }

  private Geometry[] simplifyEdges(double[] tolerances) {
    CoverageRingEdges covRings = CoverageRingEdges.create(coverage);
    List<CoverageEdge> covEdges = covRings.getEdges();
    TPVWSimplifier.Edge[] edges = createEdges(covEdges, tolerances);
    return simplify(covRings, covEdges, edges);
  }

  private Edge[] createEdges(List<CoverageEdge> covEdges, double[] tolerances) {
    TPVWSimplifier.Edge[] edges = new TPVWSimplifier.Edge[covEdges.size()];
    for (int i = 0; i < covEdges.size(); i++) {
      CoverageEdge covEdge = covEdges.get(i);
      double tol = computeTolerance(covEdge, tolerances);
      edges[i] = createEdge(covEdge, tol);
    }
    return edges;
  }

  private double computeTolerance(CoverageEdge covEdge, double[] tolerances) {
    int index0 = covEdge.getAdjacentIndex(0);
    // assert: index0 >= 0
    double tolerance = tolerances[index0];
    
    int index1 = covEdge.getAdjacentIndex(0);
    if (index1 >= 0) {
      double tol1 = tolerances[index1];
      //-- minimum tolerance is used
      if (tol1 < tolerance)
        tolerance = tol1;
    }
    return tolerance;
  }

  private Geometry[] simplifyEdges(double toleranceInner, double toleranceOuter) {
    CoverageRingEdges covRings = CoverageRingEdges.create(coverage);
    List<CoverageEdge> covEdges = covRings.getEdges();
    TPVWSimplifier.Edge[] edges = createEdges(covEdges, toleranceInner, toleranceOuter);
    return simplify(covRings, covEdges, edges);
  }

  private Geometry[] simplify(CoverageRingEdges covRings, List<CoverageEdge> covEdges, TPVWSimplifier.Edge[] edges) {
    CornerArea cornerArea = new CornerArea(smoothWeight);
    TPVWSimplifier.simplify(edges, cornerArea, removableSizeFactor);
    setCoordinates(covEdges, edges);
    Geometry[] result = covRings.buildCoverage();
    return result;
  }

  private static TPVWSimplifier.Edge[] createEdges(List<CoverageEdge> covEdges, double toleranceInner, double toleranceOuter) {
    TPVWSimplifier.Edge[] edges = new TPVWSimplifier.Edge[covEdges.size()];
    for (int i = 0; i < covEdges.size(); i++) {
      CoverageEdge covEdge = covEdges.get(i);
      double tol = computeTolerance(covEdge, toleranceInner, toleranceOuter);
      edges[i] = createEdge(covEdge, tol);
    }
    return edges;
  }

  private static Edge createEdge(CoverageEdge covEdge, double tol) {
    return new TPVWSimplifier.Edge(covEdge.getCoordinates(), tol, 
        covEdge.isFreeRing(), covEdge.isRemovableRing());
  }
  
  private static double computeTolerance(CoverageEdge covEdge, double toleranceInner, double toleranceOuter) {
    return covEdge.isInner() ? toleranceInner : toleranceOuter;
  }
  
  private void setCoordinates(List<CoverageEdge> covEdges, Edge[] edges) {
    for (int i = 0; i < covEdges.size(); i++) {
      Edge edge = edges[i];
      if (edge.getTolerance() > 0) {
        covEdges.get(i).setCoordinates(edges[i].getCoordinates());
      }
    }
  }
  
}
