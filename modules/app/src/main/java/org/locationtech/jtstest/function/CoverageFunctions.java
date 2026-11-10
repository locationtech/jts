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

import java.util.Arrays;

import org.locationtech.jts.algorithm.distance.DiscreteHausdorffDistance;
import org.locationtech.jts.coverage.CoverageCleaner;
import org.locationtech.jts.coverage.CoverageGapFinder;
import org.locationtech.jts.coverage.CoveragePolygonValidator;
import org.locationtech.jts.coverage.CoverageSimplifier;
import org.locationtech.jts.coverage.CoverageUnion;
import org.locationtech.jts.coverage.CoverageValidator;
import org.locationtech.jts.coverage.CoverageEdgeExtractor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
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
      @Metadata(title="Max Gap Width")
      double maxGapWidth) {
    return CoverageGapFinder.findGaps(toGeometryArray(geom), maxGapWidth);
  }

  @Metadata(description="Extract edges from a coverage")
  public static Geometry extractEdges(Geometry geom) {
    Geometry[] edges = CoverageEdgeExtractor.extract(toGeometryArray(geom));
    return FunctionsUtil.buildGeometryCollection(edges, geom.getFactory().createLineString());
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
    return coverage.getFactory().createGeometryCollection(result);
  }

  @Metadata(description="Simplify a coverage with a smoothness weight")
  public static Geometry simplifySharp(Geometry coverage, 
      @Metadata(title="Distance tol")
      double tolerance, 
      @Metadata(title="Weight")
      double weight) {
    Geometry[] cov = toGeometryArray(coverage);
    CoverageSimplifier simplifier = new CoverageSimplifier(cov);
    simplifier.setSmoothWeight(weight);
    Geometry[] result = simplifier.simplify(tolerance);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  @Metadata(description="Simplify a coverage with a ring removal size factor")
  public static Geometry simplifyRemoveRings(Geometry coverage, 
      @Metadata(title="Distance tol")
      double tolerance, 
      @Metadata(title="Removal Size Factor")
      double factor) {
    Geometry[] cov = toGeometryArray(coverage);
    CoverageSimplifier simplifier = new CoverageSimplifier(cov);
    simplifier.setRemovableRingSizeFactor(factor);
    Geometry[] result = simplifier.simplify(tolerance);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  @Metadata(description="Simplify inner edges of a coverage")
  public static Geometry simplifyInner(Geometry coverage, double tolerance) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageSimplifier.simplifyInner(cov, tolerance);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  @Metadata(description="Simplify outer edges of a coverage")
  public static Geometry simplifyOuter(Geometry coverage, double tolerance) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageSimplifier.simplifyOuter(cov, tolerance);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  @Metadata(description="Simplify inner and outer edges of a coverage differently")
  public static Geometry simplifyInOut(Geometry coverage, 
      @Metadata(title="Inner Distance tol")
      double toleranceInner, 
      @Metadata(title="Outer Distance tol")
      double toleranceOuter) {
    Geometry[] cov = toGeometryArray(coverage);
    CoverageSimplifier simplifier = new CoverageSimplifier(cov);
    Geometry[] result = simplifier.simplify(toleranceInner, toleranceOuter);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  @Metadata(description="Simplify a coverage with per-geometry tolerances")
  public static Geometry simplifyTolerances(Geometry coverage, 
      @Metadata(title="Tolerances (comma-sep)")
      String tolerancesCSV) {
    Geometry[] cov = toGeometryArray(coverage);
    double[] tolerances = tolerances(tolerancesCSV, cov.length);
    Geometry[] result =  CoverageSimplifier.simplify(cov, tolerances);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  private static double[] tolerances(String csvList, int len) {
    Double[] tolsDouble = toDoubleArray(csvList);
    double[] tols = new double[len];
    for (int i = 0; i < tolsDouble.length; i++) {
      tols[i] = tolsDouble[i];
    }
    return tols;
  }  
  
  //-------------------------------------------------------
  
  public static Geometry clean(Geometry coverage, 
      @Metadata(title="Snap Distance")
      double snapDistance, 
      @Metadata(title="Max Gap Width")
      double maxGapWidth) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageCleaner.clean(cov, snapDistance, maxGapWidth);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  public static Geometry cleanSnap(Geometry coverage, 
      @Metadata(title="Snap Distance")
      double snapDistance) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageCleaner.clean(cov, snapDistance, 0);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  public static Geometry cleanMergeMaxArea(Geometry coverage,
      @Metadata(title="Max Gap Width")
      double maxGapWidth) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageCleaner.cleanOverlapGap(cov, CoverageCleaner.MERGE_MAX_AREA,
        maxGapWidth);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  public static Geometry cleanMergeMinArea(Geometry coverage,
      @Metadata(title="Max Gap Width")
      double maxGapWidth) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageCleaner.cleanOverlapGap(cov, CoverageCleaner.MERGE_MIN_AREA,
        maxGapWidth);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  public static Geometry cleanGapWidth(Geometry coverage, 
      @Metadata(title="Max Gap Width")
      double maxGapWidth) {
    Geometry[] cov = toGeometryArray(coverage);
    Geometry[] result =  CoverageCleaner.cleanGapWidth(cov, maxGapWidth);
    return coverage.getFactory().createGeometryCollection(result);
  }
  
  public static Geometry cleanedOverlaps(Geometry coverage) {
    Geometry[] cov = toGeometryArray(coverage);
    CoverageCleaner cc = new CoverageCleaner(cov);
    cc.clean();
    Geometry[] overlaps = GeometryFactory.toGeometryArray(
        cc.getOverlaps());
    return coverage.getFactory().createGeometryCollection(overlaps);
  }
  
  public static Geometry cleanedOverlapsSnap(Geometry coverage, 
      @Metadata(title="Snap Distance")
      double snapDistance) {
    Geometry[] cov = toGeometryArray(coverage);
    CoverageCleaner cc = new CoverageCleaner(cov);
    cc.setSnappingDistance(snapDistance);
    cc.clean();
    Geometry[] overlaps = GeometryFactory.toGeometryArray(
        cc.getOverlaps());
    return coverage.getFactory().createGeometryCollection(overlaps);
  }
  
  public static Geometry cleanedGaps(Geometry coverage, 
      @Metadata(title="Max Gap Width")
      double maxGapWidth) {
    Geometry[] cov = toGeometryArray(coverage);
    CoverageCleaner cc = new CoverageCleaner(cov);
    cc.setGapMaximumWidth(maxGapWidth);
    cc.clean();
    Geometry[] gaps = GeometryFactory.toGeometryArray(
        cc.getMergedGaps());
    return coverage.getFactory().createGeometryCollection(gaps);
  }
  
  public static Geometry cleanedGapsSnap(Geometry coverage, 
      @Metadata(title="Snap Distance")
      double snapDistance,
      @Metadata(title="Max Gap Width")
      double maxGapWidth) {
    Geometry[] cov = toGeometryArray(coverage);
    CoverageCleaner cc = new CoverageCleaner(cov);
    cc.setSnappingDistance(snapDistance);
    cc.setGapMaximumWidth(maxGapWidth);
    cc.clean();
    Geometry[] gaps = GeometryFactory.toGeometryArray(
        cc.getMergedGaps());
    return coverage.getFactory().createGeometryCollection(gaps);
  }
  
  //--------------------------------------------------
  
  public static Geometry maxDistances(Geometry coverage1, Geometry coverage2) {
    if (coverage1.getNumGeometries() != coverage2.getNumGeometries()) {
      throw new IllegalArgumentException("Coverages must have same number of elements");
    }
    Geometry[] hd = new Geometry[coverage1.getNumGeometries()];
    for (int i = 0; i < coverage1.getNumGeometries(); i++) {
      Geometry e1 = coverage1.getGeometryN(i);
      Geometry e2 = coverage2.getGeometryN(i);
      hd[i] = DiscreteHausdorffDistance.distanceLine(e1, e2);
    }
    return coverage1.getFactory().createGeometryCollection(hd);
  }
  
  //====================================================================
  
  private static Double[] toDoubleArray(String csvList) {
    return Arrays.stream(csvList.split(",")).map(Double::parseDouble).toArray(Double[]::new);
  }
  
  private static Geometry[] toGeometryArray(Geometry geom) {
    Geometry[] geoms = new Geometry[geom.getNumGeometries()];
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      geoms[i]= geom.getGeometryN(i);
    }
    return geoms;
  }
  
}
