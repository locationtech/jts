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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.locationtech.jts.operation.relateng.RelatePredicate;
import org.locationtech.jts.util.IntArrayList;
import org.locationtech.jts.util.Stopwatch;

/**
 * Cleans a polygonal coverage, removing overlaps and gaps smaller than a given tolerance.
 * 
 * Overlaps are merged with an adjacent polygon chosen according to a specified strategy.
 * Strategies:
 * - Id (min/max)
 * - Parent Area (min/max)
 * - Maximum Border Length
 * 
 * Gaps which exceed a specified tolerance are filled 
 * and merged with an adjacent polygon.
 * Merge with adjacent item with longest border
 * 
 * @see CoverageValidator
 * @author mdavis
 *
 */
public class CoverageCleaner {
  private static final double SLIVER_COMPACTNESS_RATIO = 0.05;

  public static Geometry[] clean(Geometry[] coverage, double tolerance) {
    CoverageCleaner c = new CoverageCleaner(coverage);
    c.clean(tolerance);
    return c.getResult();
  }

  public static List<Polygon> getOverlaps(Geometry[] coverage, double tolerance) {
    CoverageCleaner c = new CoverageCleaner(coverage);
    c.clean(tolerance);
    return c.getOverlaps();
  }

  public static List<Polygon> getMergedGaps(Geometry[] coverage, double tolerance) {
    CoverageCleaner c = new CoverageCleaner(coverage);
    c.clean(tolerance);
    return c.getMergedGaps();
  }

  private Geometry[] coverage;
  private GeometryFactory geomFactory;
  private Polygon[] resultants = null;  
  private CleanCoverage cleanCov;
  private HashMap<Integer, IntArrayList> overlapParentMap = new HashMap<Integer, IntArrayList>();
  private List<Polygon> overlaps = new ArrayList<Polygon>();
  private List<Polygon> gaps = new ArrayList<Polygon>();
  private List<Polygon> mergableGaps;
  private STRtree covIndex;
  
  public CoverageCleaner(Geometry[] coverage) {
    this.coverage = coverage;
    this.geomFactory = coverage[0].getFactory();
  }

  //TODO: allow snap distance = 0 -> non-snapping noder
  //TODO: allow snap-rounding noder for precision reduction
  //TODO: add overlap merge strategies
  //TODO: add merge gaps by: area / diameter / no merge
  
  public List<Polygon> getOverlaps() {
    return overlaps;
  }
  
  public List<Polygon> getMergedGaps() {
    return mergableGaps;
  }
  
  public Geometry[] getResult() {
    return cleanCov.toCoverage(geomFactory);
  }
  
  public void clean(double tolerance) {
    computeResultants(tolerance);
    System.out.format("Overlaps: %d  Gaps: %d\n", overlaps.size(), mergableGaps.size());
  
    Stopwatch sw = new Stopwatch();
    mergeOverlaps(overlapParentMap);
    System.out.println("Merging Overlaps: " + sw.getTimeString());
    sw.reset();
    cleanCov.mergeGaps(mergableGaps);
    System.out.println("Merging Gaps: " + sw.getTimeString());
  }

  private void mergeOverlaps(HashMap<Integer, IntArrayList> overlapParentMap) {
    for (int resIndex : overlapParentMap.keySet()) {
      cleanCov.mergeOverlap(resultants[resIndex], overlapParentMap.get(resIndex));
    }
  }
  
  private void computeResultants(double tolerance) {
    Stopwatch sw = new Stopwatch();
    sw.start();
    
    Geometry nodedEdges = node(coverage, tolerance);
    Geometry cleanEdges = LineDissolver.dissolve(nodedEdges);
    resultants = polygonize(cleanEdges);
    System.out.println("Noding/Polygonize: " + sw.getTimeString());
    
    cleanCov = new CleanCoverage(coverage.length);
    
    sw.reset();
    createCoverageIndex();
    classifyResult(resultants);
    System.out.println("Classify: " + sw.getTimeString());
    
    mergableGaps = findSlivers(gaps);
   }

  private void createCoverageIndex() {
    covIndex = new STRtree();
    for (int i = 0; i < coverage.length; i++) {
      covIndex.insert(coverage[i].getEnvelopeInternal(), i);
    }
  }

  private void classifyResult(Polygon[] resultants) {
    for (int i = 0; i < resultants.length; i++) {
      Polygon res = resultants[i];
      classifyResult(i, res);
    }
  }

  private void classifyResult(int resultIndex, Polygon resPoly) {
    int parentIndex = -1;
    IntArrayList overlapIndexes = null;
    Point intPt = resPoly.getInteriorPoint();
    
    @SuppressWarnings("unchecked")
    List<Integer> candidatesIndex = covIndex.query(resPoly.getEnvelopeInternal());
    for (int i : candidatesIndex) {
      Geometry parent = coverage[i];
      if (covers(parent, intPt)) {
        if (parentIndex < 0) {
          parentIndex = i;
        }
        else {
          if (overlapIndexes == null) {
            overlapIndexes = new IntArrayList();
          }
          overlapIndexes.add(parentIndex);
          overlapIndexes.add(i);
        }
      }
    }
    /**
     * Classify resultant based on # of parents:
     * 0 - gap
     * 1 - single polygon face
     * >1 - overlap
     */
    if (parentIndex < 0) {
      gaps.add(resPoly);
    }
    else if (overlapIndexes != null) {
      overlapParentMap.put(resultIndex, overlapIndexes);
      overlaps.add(resPoly);
    }
    else {
      cleanCov.add(parentIndex, resPoly);
    }
  }
  
  private static boolean covers(Geometry poly, Point intPt) {
    return RelateNG.relate(poly, intPt, RelatePredicate.covers());
  }

  private List<Polygon> findSlivers(List<Polygon> gaps) {
    return gaps.stream().filter(gap -> isSliver(gap))
        .collect(Collectors.toList());
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

  private static Polygon[] polygonize(Geometry cleanEdges) {
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(cleanEdges);
    Geometry polys = polygonizer.getGeometry();
    return toPolygonArray(polys);
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
  
  private static Polygon[] toPolygonArray(Geometry geom) {
    Polygon[] geoms = new Polygon[geom.getNumGeometries()];
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      geoms[i]= (Polygon) geom.getGeometryN(i);
    }
    return geoms;
  }
}
