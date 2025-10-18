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
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.util.IntArrayList;

/**
 * Cleans the linework of a set of valid polygonal geometries to form a valid polygonal coverage.
 * The input is an array of valid {@link Polygon} or {@link MultiPolygon} geometries 
 * which may contain topological errors such as overlaps and gaps.
 * Empty or non-polygonal inputs are removed.
 * Linework is snapped together to eliminate small discrepancies.
 * Overlaps are merged with a parent polygon, according to a given merge strategy.
 * Gaps narrower than a given width are filled and merged with an adjacent polygon.
 * The output is an array of polygonal geometries forming a valid polygonal coverage.
 * 
 * <h3>Snapping</h3>
 * 
 * Snapping to nearby vertices and line segment snapping is used to improve noding robustness 
 * and eliminate small errors in an efficient way,  
 * By default this uses a small snapping distance based on the extent of the input data.
 * The snapping distance may be specified explicitly.
 * This can reduce the number of overlaps and gaps that need to be merged,
 * and reduce the risk of spikes formed by merged gaps.
 * However, a large snapping distance may introduce undesirable data alteration.
 * Snapping is disabled if a zero snapping distance is specified.
 * (Note that disabling snapping may prevent collinear linework from being noded correctly.)
 * 
 * <h3>Overlap Merging</h3>
 * 
 * Overlaps are merged into a parent polygon chosen according to a specified merge strategy.
 * The supported strategies are:
 * <ul>
 * <li><b>Longest Border</b>: (default) merge with the polygon with longest shared border
 * ({@link #MERGE_LONGEST_BORDER}.)
 * <li><b>Maximum/Minimum Area</b>: merge with the polygon with largest or smallest area
 * ({@link #MERGE_MAX_AREA}, {@link #MERGE_MIN_AREA}.)
 * <li><b>Minimum Index</b>: merge with the polygon with the lowest index in the input array
 * ({@link #MERGE_MIN_INDEX}.)
 * This allows sorting the input according to some criteria to provide a priority 
 * for merging overlaps.
 * </ul>
 * 
 * <h3>Gap Merging</h3>
 * 
 * Gaps which are wider than a given distance are merged with an adjacent polygon.
 * Polygon width is determined as twice the radius of the {@link MaximumInscribedCircle}
 * of the gap polygon.
 * Gaps are merged with the adjacent polygon with longest shared border.
 * Empty holes in input polygons are treated as gaps, and may be filled in.
 * Gaps which are not fully enclosed ("inlets") are not removed.
 * <p>
 * Cleaning can be run on a valid coverage to remove gaps.
 * 
 * <p>
 * The clean result is an array of polygonal geometries 
 * which match one-to-one with the input array.
 * A result item may be <tt>null</tt> if:
 * <ul>
 * <li>the input item is non-polygonal or empty
 * <li>the input item is so small it is snapped to collapse
 * <li>the input item is covered by another input item 
 * (which may be a larger or a duplicate (nearly or exactly) geometry)
 * </ul>
 * The result is a valid coverage according to {@link CoverageValidator#isValid(Geometry[])}; 
 * 
 * <h3>Known Issues</h3>
 * <ul>
 * <li>Long narrow gaps adjacent to multiple polygons may form spikes when merged with a single polygon. 
 * </ul>
 * 
 * <h3>Future Enhancements</h3>
 * <ul>
 * <li>Provide an area-based tolerance for gap merging
 * <li>Prevent long narrow gaps from forming spikes by partitioning them before merging.
 * <li>Allow merging narrow parts of a gap while leaving wider portions.
 * <li>Support a priority value for each input polygon to control overlap and gap merging
 * (this could also allow blocking polygons from being merge targets)
 * </ul>
 * 
 * @see CoverageValidator
 * @author Martin Davis
 *
 */
public class CoverageCleaner { 
  
  /**
   * Merge strategy that chooses polygon with longest common border
   */
  public static final int MERGE_LONGEST_BORDER = 0;
  
  /**
   * Merge strategy that chooses polygon with maximum area
   */
  public static final int MERGE_MAX_AREA = 1;
  
  /**
   * Merge strategy that chooses polygon with minimum area
   */
  public static final int MERGE_MIN_AREA = 2;
  
  /**
   * Merge strategy that chooses polygon with smallest input index
   */
  public static final int MERGE_MIN_INDEX = 3;
  
  /**
   * Cleans a set of polygonal geometries to form a valid coverage,
   * allowing all cleaning parameters to be specified.
   * 
   * @param coverage an array of polygonal geometries to clean
   * @param snappingDistance the distance tolerance for snapping
   * @param overlapMergeStrategy the strategy to use for merging overlaps
   * @param maxGapWidth the maximum width of gaps to merge
   * @return the clean coverage
   */
  public static Geometry[] clean(Geometry[] coverage, double snappingDistance, 
      int overlapMergeStrategy, double maxGapWidth) {
    CoverageCleaner cc = new CoverageCleaner(coverage);
    cc.setSnappingDistance(snappingDistance);
    cc.setGapMaximumWidth(maxGapWidth);
    cc.setOverlapMergeStrategy(overlapMergeStrategy);
    cc.clean();
    return cc.getResult();
  }

  /**
   * Cleans a set of polygonal geometries to form a valid coverage,
   * using the default overlap merge strategy {@link #MERGE_LONGEST_BORDER}.
   * 
   * @param coverage an array of polygonal geometries to clean
   * @param snappingDistance the distance tolerance for snapping
   * @param maxGapWidth the maximum width of gaps to merge
   * @return the clean coverage
   */
  public static Geometry[] clean(Geometry[] coverage, double snappingDistance, 
      double maxGapWidth) {
    CoverageCleaner cc = new CoverageCleaner(coverage);
    cc.setSnappingDistance(snappingDistance);
    cc.setGapMaximumWidth(maxGapWidth);
    cc.clean();
    return cc.getResult();
  }

  /**
   * Cleans a set of polygonal geometries to form a valid coverage,
   * using the default snapping distance tolerance.
   * 
   * @param coverage an array of polygonal geometries to clean
   * @param overlapMergeStrategy the strategy to use for merging overlaps
   * @param maxGapWidth the maximum width of gaps to merge
   * @return the clean coverage
   */
  public static Geometry[] cleanOverlapGap(Geometry[] coverage, 
      int overlapMergeStrategy, double maxGapWidth) {
    return clean(coverage, -1, overlapMergeStrategy, maxGapWidth);
  }

  /**
   * Cleans a set of polygonal geometries to form a valid coverage,
   * with default snapping tolerance and overlap merging,
   * and merging gaps which are narrower than a specified width.
   *  
   * @param coverage an array of polygonal geometries to clean
   * @param maxGapWidth the maximum width of gaps to merge
   * @return the clean coverage
   */
  public static Geometry[] cleanGapWidth(Geometry[] coverage, double maxGapWidth) {
    return clean(coverage, -1, maxGapWidth);
  }

  private Geometry[] coverage;
  private double snappingDistance;  // set to compute default
  private double gapMaximumWidth = 0.0;
  private int overlapMergeStrategy = MERGE_LONGEST_BORDER;

  private GeometryFactory geomFactory;
  private STRtree covIndex;
  private Polygon[] resultants = null;  
  private CleanCoverage cleanCov;
  private HashMap<Integer, IntArrayList> overlapParentMap = new HashMap<Integer, IntArrayList>();
  private List<Polygon> overlaps = new ArrayList<Polygon>();
  private List<Polygon> gaps = new ArrayList<Polygon>();
  private List<Polygon> mergableGaps;
  
  private static final double DEFAULT_SNAPPING_FACTOR = 1.0e8;

  /**
   * Create a new cleaner instance for a set of polygonal geometries.
   * 
   * @param coverage an array of polygonal geometries to clean
   */
  public CoverageCleaner(Geometry[] coverage) {
    this.coverage = coverage;
    this.geomFactory = coverage[0].getFactory();
    snappingDistance = computeDefaultSnappingDistance(coverage);
  }

  /**
   * Sets the snapping distance tolerance.
   * The default is to use a small fraction of the input extent diameter.
   * A distance of zero prevents snapping from being used.
   * 
   * @param snappingDistance the snapping distance tolerance
   */
  public void setSnappingDistance(double snappingDistance) {
    //-- use default distance if invalid argument
    if (snappingDistance < 0)
      return;
    this.snappingDistance = snappingDistance;
  }
  
  /**
   * Sets the overlap merge strategy to use.
   * The default is {@link #MERGE_LONGEST_BORDER}.
   * 
   * @param mergeStrategy the merge strategy code
   */
  public void setOverlapMergeStrategy(int mergeStrategy) {
    if (mergeStrategy < MERGE_LONGEST_BORDER 
        || mergeStrategy > MERGE_MIN_INDEX)
      throw new IllegalArgumentException("Invalid merge strategy code: " + mergeStrategy);
    
    this.overlapMergeStrategy = mergeStrategy;
  } 
  
  /**
   * Sets the maximum width of the gaps that will be filled and merged.
   * The width of a gap is twice the radius of the Maximum Inscribed Circle in the gap polygon,
   * A width of zero prevents gaps from being merged. 
   * 
   * @param maxWidth the maximum gap width to merge
   */
  public void setGapMaximumWidth(double maxWidth) {
    if (maxWidth < 0)
      return;
    this.gapMaximumWidth = maxWidth;
  }
  
  //TODO: support snap-rounding noder for precision reduction
  //TODO: add merge gaps by: area?

  /**
   * Cleans the coverage.
   * 
   */
  public void clean() {
    computeResultants(snappingDistance);
    //System.out.format("Overlaps: %d  Gaps: %d\n", overlaps.size(), mergableGaps.size());
  
    //Stopwatch sw = new Stopwatch();
    mergeOverlaps(overlapParentMap);
    //System.out.println("Merge Overlaps: " + sw.getTimeString());
    //sw.reset();
    cleanCov.mergeGaps(mergableGaps);
    //System.out.println("Merge Gaps: " + sw.getTimeString());
  }

  /**
   * Gets the cleaned coverage.
   * 
   * @return the clean coverage
   */
  public Geometry[] getResult() {
    return cleanCov.toCoverage(geomFactory);
  }  
  
  /**
   * Gets polygons representing the overlaps in the input,
   * which have been merged.
   * 
   * @return a list of overlap polygons
   */
  public List<Polygon> getOverlaps() {
    return overlaps;
  }
  
  /**
   * Gets polygons representing the gaps in the input
   * which have been merged.
   *  
   * @return a list of gap polygons
   */
  public List<Polygon> getMergedGaps() {
    return mergableGaps;
  }
  
  //-------------------------------------------------
  
  private static double computeDefaultSnappingDistance(Geometry[] geoms) {
    double diameter = extent(geoms).getDiameter();
    return diameter / DEFAULT_SNAPPING_FACTOR;
  }

  private static Envelope extent(Geometry[] geoms) {
    Envelope env = new Envelope();
    for (Geometry geom : geoms) {
      env.expandToInclude(geom.getEnvelopeInternal());
    }
    return env;
  }
  
  private void mergeOverlaps(HashMap<Integer, IntArrayList> overlapParentMap) {
    for (int resIndex : overlapParentMap.keySet()) {
      cleanCov.mergeOverlap(resultants[resIndex], mergeStrategy(overlapMergeStrategy), overlapParentMap.get(resIndex));
    }
  }
  
  private CleanCoverage.MergeStrategy mergeStrategy(int mergeStrategyId) {
    switch (mergeStrategyId) {
    case MERGE_LONGEST_BORDER: return new CleanCoverage.MergeStrategy.BorderMergeStrategy();
    case MERGE_MAX_AREA: return new CleanCoverage.MergeStrategy.AreaMergeStrategy(true);
    case MERGE_MIN_AREA: return new CleanCoverage.MergeStrategy.AreaMergeStrategy(false);
    case MERGE_MIN_INDEX: return new CleanCoverage.MergeStrategy.IndexMergeStrategy(false);
    }
    throw new IllegalArgumentException("Unknown merge strategy: " + mergeStrategyId);
  }

  private void computeResultants(double tolerance) {
    //System.out.println("Coverage Cleaner ===> polygons: " + coverage.length);
    //System.out.format("Snapping distance: %f\n", snappingDistance);
    //Stopwatch sw = new Stopwatch();
    //sw.start();
    
    Geometry nodedEdges = node(coverage, tolerance);
    //System.out.println("Noding: " + sw.getTimeString());
    
    //sw.reset();
    Geometry cleanEdges = LineDissolver.dissolve(nodedEdges);
    //System.out.println("Dissolve: " + sw.getTimeString());
    
    //sw.reset();
    resultants = polygonize(cleanEdges);
    //System.out.println("Polygonize: " + sw.getTimeString());
    
    cleanCov = new CleanCoverage(coverage.length);
    
    //sw.reset();
    createCoverageIndex();
    classifyResult(resultants);
    //System.out.println("Classify: " + sw.getTimeString());
    
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
      classifyResultant(i, res);
    }
  }

  private void classifyResultant(int resultIndex, Polygon resPoly) {
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
    return SimplePointInAreaLocator.isContained(intPt.getCoordinate(), poly);
    //return RelateNG.relate(poly, intPt, RelatePredicate.covers());
  }

  private List<Polygon> findMergableGaps(List<Polygon> gaps2) {
    return gaps.stream().filter(gap -> isMergableGap(gap))
        .collect(Collectors.toList());
  }
  
  private boolean isMergableGap(Polygon gap) {
    if (gapMaximumWidth <= 0) {
      return false;
    }
    return MaximumInscribedCircle.isRadiusWithin(gap, gapMaximumWidth / 2.0);
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
      //-- skip non-polygonal and empty elements
      if (! isPolygonal(geom))
        continue;
      if (geom.isEmpty())
        continue;
      extractNodedSegmentStrings(geom, segs);
    }
    Noder noder = new SnappingNoder(snapDistance);
    noder.computeNodes(segs);
    Collection nodedSegStrings = noder.getNodedSubstrings();
    return SegmentStringUtil.toGeometry(nodedSegStrings, coverage[0].getFactory());
  }
  
  private static boolean isPolygonal(Geometry geom) {
    return geom instanceof Polygon || geom instanceof MultiPolygon;
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
