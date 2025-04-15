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

import org.locationtech.jts.algorithm.construct.MaximumInscribedCircle;
import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.Envelope;
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
 * - Maximum Boundary Length (default)
 * - Parent Area (min/max)
 * - Id (min/max)
 * 
 * Gaps which exceed a specified tolerance are filled 
 * and merged with an adjacent polygon.
 * The tolerance can be:
 * - Max Width (default)
 * - Max Area
 * Merge with adjacent polygon with longest boundary.
 * 
 * @see CoverageValidator
 * @author mdavis
 *
 */
public class CoverageCleaner {  
  private static final double DEFAULT_SNAPPING_FACTOR = 1.0e8;

  public static Geometry[] clean(Geometry[] coverage, double snappingDistance) {
    CoverageCleaner cc = new CoverageCleaner(coverage);
    cc.setSnappingDistance(snappingDistance);
    cc.clean();
    return cc.getResult();
  }

  public static Geometry[] cleanWithGapWidth(Geometry[] coverage, double maxGapWidth) {
    CoverageCleaner cc = new CoverageCleaner(coverage);
    //cc.setSnappingDistance(snappingDistance);
    cc.setGapMaximumWidth(maxGapWidth);
    cc.clean();
    return cc.getResult();
  }

  public static List<Polygon> getOverlaps(Geometry[] coverage, double tolerance) {
    CoverageCleaner cc = new CoverageCleaner(coverage);
    cc.clean();
    return cc.getOverlaps();
  }

  public static List<Polygon> getMergedGaps(Geometry[] coverage, double tolerance) {
    CoverageCleaner cc = new CoverageCleaner(coverage);
    cc.clean();
    return cc.getMergedGaps();
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
  private double snappingDistance;

  private double gapMaximumWidth;

  private double gapMaxiumumRadius = 0;
  
  public CoverageCleaner(Geometry[] coverage) {
    this.coverage = coverage;
    this.geomFactory = coverage[0].getFactory();
    snappingDistance = computeDefaultSnappingDistance(coverage);
  }

  private static double computeDefaultSnappingDistance(Geometry[] geoms) {
    Envelope covEnv = envelope(geoms);
    double diameter = covEnv.getDiameter();
    return diameter / DEFAULT_SNAPPING_FACTOR;
  }

  private static Envelope envelope(Geometry[] geoms) {
    Envelope env = new Envelope();
    for (Geometry geom : geoms) {
      env.expandToInclude(geom.getEnvelopeInternal());
    }
    return env;
  }

  public void setSnappingDistance(double snappingDistance) {
    this.snappingDistance = snappingDistance;
  }
  
  public void setGapMaximumWidth(double maxWidth) {
    this.gapMaximumWidth = maxWidth;
    gapMaxiumumRadius  = maxWidth;
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
  
  public void clean() {
    computeResultants(snappingDistance);
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
    System.out.println("Noding: " + sw.getTimeString());
    
    sw.reset();
    Geometry cleanEdges = LineDissolver.dissolve(nodedEdges);
    System.out.println("Dissolve: " + sw.getTimeString());
    
    sw.reset();
    resultants = polygonize(cleanEdges);
    System.out.println("Polygonize: " + sw.getTimeString());
    
    cleanCov = new CleanCoverage(coverage.length);
    
    sw.reset();
    createCoverageIndex();
    classifyResult(resultants);
    System.out.println("Classify: " + sw.getTimeString());
    
    mergableGaps = findMergableGaps(gaps);
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
    Point intPt = resPoly.getInteriorPoint();
    int parentIndex = -1;
    IntArrayList overlapIndexes = null;
    
    @SuppressWarnings("unchecked")
    List<Integer> candidateParentIndex = covIndex.query(intPt.getEnvelopeInternal());
    for (int i : candidateParentIndex) {
      Geometry parent = coverage[i];
      if (covers(parent, intPt)) {
        //-- found first parent
        if (parentIndex < 0) {
          parentIndex = i;
        }
        else {
          //-- more than one parent - record them all
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

  private List<Polygon> findMergableGaps(List<Polygon> gaps2) {
    return gaps.stream().filter(gap -> isMergableGap(gap))
        .collect(Collectors.toList());
  }
  
  private boolean isMergableGap(Polygon gap) {
    if (gapMaxiumumRadius > 0) {
      return MaximumInscribedCircle.isRadiusWithin(gap, gapMaxiumumRadius);
    }
    return false;
  }

  /*
  private List<Polygon> findSlivers(List<Polygon> gaps) {
    return gaps.stream().filter(gap -> isSliver(gap))
        .collect(Collectors.toList());
  }

  private static final double SLIVER_COMPACTNESS_RATIO = 0.05;

  private boolean isSliver(Geometry poly) {
    //TODO: add min area cutoff?
    //TODO: for low vertex count, check polygon width?
    if (poly.getNumPoints() <= 5)
      return true;
    return compactness(poly) < SLIVER_COMPACTNESS_RATIO;
  }
  */

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
