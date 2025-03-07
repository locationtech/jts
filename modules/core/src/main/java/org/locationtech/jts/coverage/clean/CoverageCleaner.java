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
package org.locationtech.jts.coverage.clean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.Noder;
import org.locationtech.jts.noding.SegmentStringUtil;
import org.locationtech.jts.noding.snap.SnappingNoder;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.relateng.IntersectionMatrixPattern;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.locationtech.jts.operation.relateng.RelatePredicate;

public class CoverageCleaner {
  private static final double SLIVER_COMPACTNESS_RATIO = 0.05;

  public static Geometry[] clean(Geometry[] coverage, double tolerance) {
    CoverageCleaner c = new CoverageCleaner(coverage);
    return c.clean(tolerance);
  }

  private Geometry[] coverage;
  private GeometryFactory geomFactory;  
  
  public CoverageCleaner(Geometry[] coverage) {
    this.coverage = coverage;
    this.geomFactory = coverage[0].getFactory();
  }

  private Geometry[] clean(double tolerance) {
    Geometry nodedEdges = node(coverage, tolerance);
    Geometry cleanEdges = LineDissolver.dissolve(nodedEdges);
    //TODO: specify Polygon[] as return type?
    Geometry[] resultants = polygonize(cleanEdges);
    
    List<CoverageElement> coverageElements = CoverageElement.createElements(coverage);
    
    Coordinate[] parentIntPt = new Coordinate[resultants.length];
    List<Integer> cleanIndex = new ArrayList<Integer>();
    List<Integer> otherIndex = new ArrayList<Integer>();
    //List<Integer> sliverIndex = new ArrayList<Integer>();
    findClean(resultants, coverageElements,
        parentIntPt, cleanIndex, otherIndex);
   
    //Geometry[] holes = extract(resultants, holeIndex);
    //return holes;
    
    Map<Coordinate, CoverageElement> intPtCoverageMap = CoverageElement.createMap(coverageElements);
    Geometry[] cleanCov = buildCoverage(resultants, cleanIndex, parentIntPt, intPtCoverageMap);
    
    List<Geometry> mergable = findMergable(resultants, otherIndex, cleanCov);
    //return GeometryFactory.toGeometryArray(mergable);
    merge(mergable, cleanCov);
    
    //TODO: handle geoms with interior point in location which causes geom to disappear
    return cleanCov;
  }

  private void merge(List<Geometry> mergable, Geometry[] cleanCov) {
    for (Geometry poly : mergable) {
      merge(poly, cleanCov);
    }
  }

  private void merge(Geometry poly, Geometry[] cleanCov) {
    List<Integer> adjacentIndex = findAdjacent(poly, cleanCov);
    //TODO: be smart about which one to merge with
    // longest edge?  large/small area?
    //TODO: split mergable to avoid spikes?
    
    //TODO: if a mergable overlaps a polygon which is not adjacent, it should be added to that polygon
    // this can happen if snapping causes a coverage interior point to be outside the snapped polygon 
    
    System.out.println(poly);
    
    if (adjacentIndex.size() <= 0) {
      //TODO: find parent coverage polygon and merge with it
      System.out.println("No adjacent >>>>>> " + poly);
      return;
    }
    //TODO: for now just merge to first
    int firstAdjIndex = adjacentIndex.get(0);
    //TODO: do this faster.  Can do with Coverage Union
    Geometry merged = cleanCov[firstAdjIndex].union(poly);
    cleanCov[firstAdjIndex] = merged;
  }

  private List<Integer> findAdjacent(Geometry poly, Geometry[] cleanCov) {
    List<Integer> adjIndex = new ArrayList<Integer>();
    RelateNG rel = RelateNG.prepare(poly);
    for (int i = 0; i < cleanCov.length; i++) {
      boolean isAdjacent = rel.evaluate(cleanCov[i], IntersectionMatrixPattern.ADJACENT);
      if (isAdjacent)
        adjIndex.add(i);
    }
    return adjIndex;
  }

  /**
   * Find resultants which overlap an input polygon or are a narrow hole.
   * Does not include resultants which are "large" holes.
   * 
   * @param resultants
   * @param otherIndex
   * @param cleanCov
   * @return
   */
  private List<Geometry> findMergable(Geometry[] resultants, List<Integer> otherIndex, Geometry[] cleanCov) {
    List<Geometry> mergable = new ArrayList<Geometry>();
    for (int index : otherIndex) {
      //-- should be a Polygon - type it?
      Geometry resultantPoly = resultants[index];
      if (isSliver(resultantPoly) || isOverlap(resultantPoly, coverage)) {
        mergable.add(resultantPoly);
      }
    }
    return mergable;
  }

  private boolean isOverlap(Geometry poly, Geometry[] coverage) {
    //TODO: make this more efficient with index over coverage?
    Point intPt = poly.getInteriorPoint();
    for (Geometry covPoly : coverage) {
      if (RelateNG.relate(covPoly, intPt, RelatePredicate.covers()))
        return true;
    }
    return false;
  }

  private static Geometry[] extract(Geometry[] resultants, List<Integer> indices) {
    Geometry[] result = new Geometry[indices.size()];
    int i = 0;
    for (int index : indices) {
      result[i++] = resultants[index];
    }
    return result;
  }

  private void findClean(Geometry[] resultants, 
      List<CoverageElement> coverageElements,
      Coordinate[] parentIntPt,
      List<Integer> keepIndex,
      List<Integer> otherIndex
      ) {
    for (int i = 0; i < resultants.length; i++) {
      Geometry poly = resultants[i];
      Coordinate intPt = findIntPt(poly, coverageElements);
      if (intPt != null) {
        keepIndex.add(i);
        parentIntPt[i] = intPt;
      }
      else {
        otherIndex.add(i);
      }
    }
  }

  /**
   * Finds an interior point of a polygon.
   * If the polygon contains multiple interior points
   * (which can happen if an input polygon collapses completely 
   * due to a large tolerance)
   * finds the interior point of the largest coverage element.
   * 
   * @param poly
   * @param coverageElements
   * @return
   */
  private Coordinate findIntPt(Geometry poly, List<CoverageElement> coverageElements) {
    //TODO: use spatial index on int pts
    IndexedPointInAreaLocator loc = new IndexedPointInAreaLocator(poly);
    for (CoverageElement elem : coverageElements) {
      Coordinate intPt = elem.getInteriorPoint();
      if (loc.locate(intPt) != Location.EXTERIOR)
        return intPt;
    }
    return null;
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
  
  private Geometry[] buildCoverage(Geometry[] resultants, 
      List<Integer> resultIndex, 
      Coordinate[] parentIntPt,
      Map<Coordinate, CoverageElement> intPtCoverageIndexMap) {
    Geometry[] cleanCov = new Geometry[coverage.length];
    PolygonMultiMap multiPolyMap = new PolygonMultiMap();
    for (int index : resultIndex) {
      Polygon resultant = (Polygon) resultants[index];
      
      CoverageElement covElem = intPtCoverageIndexMap.get(parentIntPt[index]);
      int covIndex = covElem.getIndex();
      if (cleanCov[covIndex] == null) {
        cleanCov[covIndex] = resultant;
      }
      else {
        //-- either: parent is a MultiPoly, or cov polygon was split by noding (can this ever happen?)
        if (! multiPolyMap.containsKey(covIndex)) {
          multiPolyMap.add(covIndex, (Polygon) cleanCov[covIndex]);
        }
        multiPolyMap.add(covIndex, resultant);
      }
    }
    for (int index : multiPolyMap.keys()) {
      cleanCov[index] = multiPolyMap.getGeometry(index, geomFactory);
    }
    /**
     * An output element may be null if the 
     * input polygon collapsed entirely
     * (due to a large snapping tolerance).
     * null entries are filled with empty geoms.
     */
    fillEmpty(cleanCov, geomFactory);
    return cleanCov;
  }

  private static void fillEmpty(Geometry[] geom, GeometryFactory geomFactory) {
    for (int i = 0; i < geom.length; i++) {
      if (geom[i] == null) {
        geom[i] = geomFactory.createEmpty(2);
      }
    }
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
  
  private static class CoverageElement {
    private Geometry coverageItem;
    private Geometry polygon;
    private double area;
    private Coordinate interiorPoint;
    private int index;
    private int indexElement;

    public CoverageElement(Geometry coverageItem, int index, 
        Geometry polygon, int iElement)
    {
      this.coverageItem = coverageItem;
      this.index = index;
      this.polygon = polygon;
      this.indexElement = iElement;
      this.area = polygon.getArea();
      this.interiorPoint = polygon.getInteriorPoint().getCoordinate();
    }

    public int getIndex() {
      return index;
    }

    public Coordinate getInteriorPoint() {
      return interiorPoint;
    }

    public static Map<Coordinate, CoverageElement> createMap(List<CoverageElement> coverageIntPts) {
      Map<Coordinate, CoverageElement> map = new HashMap<Coordinate, CoverageElement>();
      for (CoverageElement elem : coverageIntPts) {
        map.put(elem.interiorPoint, elem);
      }
      return map;
    }

    public static List<CoverageElement> createElements(Geometry[] geoms) {
      List<CoverageElement> covElems = new ArrayList<CoverageElement>();
      for (int index = 0; index < geoms.length; index++) {
        Geometry geom = geoms[index];
        for (int iElement = 0; iElement < geom.getNumGeometries(); iElement++) {
          Polygon poly = (Polygon) geom.getGeometryN(iElement);
          covElems.add(new CoverageElement(geom, index, poly, iElement));
        }
      }
      Collections.sort(covElems, new Comparator<CoverageElement> () {

        @Override
        public int compare(CoverageElement e1, CoverageElement e2) {
          //-- sort in order of decreasing area
          return Double.compare(e2.area, e1.area);
        }
        
      });
      return covElems;
    }
  }
}
