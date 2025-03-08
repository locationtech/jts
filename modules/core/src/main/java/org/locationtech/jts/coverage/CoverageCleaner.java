/*
 * Copyright (c) 2025 Martin Davis.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.locationtech.jts.operation.relateng.RelatePredicate;

/**
 * Cleans a polygonal coverage, removing overlaps and specified gaps.
 * 
 * Overlaps are merged with an adjacent polygon chosen according to a specified strategy.
 * 
 * Gaps which exceed a specified tolerance are filled and merged with an adjacent polygon.
 * 
 * @see CoverageValidator
 * @author mdavis
 *
 */
public class CoverageCleaner {
  private static final double SLIVER_COMPACTNESS_RATIO = 0.05;
  private static final int RESULT_OVERLAP = -2;
  private static final int RESULT_GAP = -1;

  public static Geometry[] clean(Geometry[] coverage, double tolerance) {
    CoverageCleaner c = new CoverageCleaner(coverage);
    return c.clean(tolerance);
  }

  private Geometry[] coverage;
  private GeometryFactory geomFactory;
  private ArrayList<Polygon> overlaps;
  private ArrayList<Polygon> gaps;
  private CleanCoverage cleanCov;
  private List<Polygon> mergableGaps;  
  
  public CoverageCleaner(Geometry[] coverage) {
    this.coverage = coverage;
    this.geomFactory = coverage[0].getFactory();
  }

  public List<Polygon> getOverlaps() {
    return overlaps;
  }
  
  public List<Polygon> getMergedGaps() {
    return mergableGaps;
  }
  
  public Geometry[] clean(double tolerance) {
    computeResultants(tolerance);
   
    //Geometry[] holes = extract(resultants, holeIndex);
    //return holes;
    
    cleanCov.merge(overlaps, true);
    cleanCov.merge(mergableGaps, false);
    
    return cleanCov.toCoverage(geomFactory);
  }

  private void computeResultants(double tolerance) {
    Geometry nodedEdges = node(coverage, tolerance);
    Geometry cleanEdges = LineDissolver.dissolve(nodedEdges);
    //TODO: specify Polygon[] as return type?
    Geometry[] resultants = polygonize(cleanEdges);
    
    overlaps = new ArrayList<Polygon>();
    gaps = new ArrayList<Polygon>();
    cleanCov = new CleanCoverage(coverage.length);
    
    classify(resultants, cleanCov, overlaps, gaps);
    
    mergableGaps = findSlivers(gaps);
  }

  private List<Polygon> findSlivers(List<Polygon> gaps) {
    return gaps.stream().filter(gap -> isSliver(gap))
        .collect(Collectors.toList());
  }

  private void classify(Geometry[] resultants, CleanCoverage cleanCov, 
      List<Polygon> overlaps, List<Polygon> gaps) {
    for (int i = 0; i < resultants.length; i++) {
      Geometry res = resultants[i];
      Point intPt = res.getInteriorPoint();
      int cleanParentIndex = findUniqueParent(coverage, intPt);
      if (cleanParentIndex >= 0) {
        cleanCov.add(cleanParentIndex, (Polygon) res);
      }
      else if (cleanParentIndex == RESULT_GAP) {
        gaps.add((Polygon) res);
      }
      else {
        overlaps.add((Polygon) res);
      }
    }
  }

  /**
   * Classifies a resultant to be either in a unique parent, an overlap (-2), or a gap (-1)
   * 
   * @param coverage
   * @param intPt
   * @return
   */
  public int findUniqueParent(Geometry[] coverage, Point intPt) {
    //TODO: use spatial index on coverage?
    int index = RESULT_GAP;
    for (int i = 0; i < coverage.length; i++) {
      Geometry parent = coverage[i];
      if (covers(parent, intPt)) {
        if (index >= 0)
          return RESULT_OVERLAP;
        index = i;
      }
    }
    //-- RESULT_GAP or a unique parent index
    return index;
  }
  
  private static boolean covers(Geometry poly, Point intPt) {
    return RelateNG.relate(poly, intPt, RelatePredicate.covers());
  }

  private boolean isSliver(Geometry poly) {
    //TODO: add min area cutoff?
    //TODO: for low vertex count, check polygon width?
    if (poly.getNumPoints() <= 5)
      return true;
    return compactness(poly) < SLIVER_COMPACTNESS_RATIO;
  }

  private static double compactness(Geometry poly) {
    double perimeter = poly.getLength();
    double area = poly.getArea();
    if (perimeter <= 0) return 0;
    return Math.abs(area) * Math.PI * 4 / (perimeter * perimeter);
  }

  private Geometry[] polygonize(Geometry cleanEdges) {
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(cleanEdges);
    Geometry polys = polygonizer.getGeometry();
    return toGeometryArray(polys);
  }
  
  public static Geometry node(Geometry[] coverage,  
      double snapDistance)
  {
    List<NodedSegmentString> segs = new ArrayList<NodedSegmentString>();
    for (Geometry geom : coverage) {
      extractNodedSegmentStrings(geom, segs);
    }
    Noder noder = new SnappingNoder(snapDistance);
    noder.computeNodes(segs);
    Collection nodedSegStrings = noder.getNodedSubstrings();
    return SegmentStringUtil.toGeometry(nodedSegStrings, coverage[0].getFactory());
  }
  
  private static void extractNodedSegmentStrings(Geometry geom, List<NodedSegmentString> segs) {
    //TODO: do more efficiently by adding directly to list
    @SuppressWarnings("unchecked")
    List<NodedSegmentString> segsGeom = SegmentStringUtil.extractNodedSegmentStrings(geom);
    segs.addAll(segsGeom);
  }
  
  private static Geometry[] toGeometryArray(Geometry geom) {
    Geometry[] geoms = new Geometry[geom.getNumGeometries()];
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      geoms[i]= geom.getGeometryN(i);
    }
    return geoms;
  }
  
}
