/*
 * Copyright (c) 2026 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.distance;

import java.util.Iterator;
import java.util.PriorityQueue;

import org.locationtech.jts.algorithm.construct.LargestEmptyCircle;
import org.locationtech.jts.algorithm.locate.IndexedPointInPolygonsLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Dimension;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.GeometryComponentFilter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.operation.distance.CoordinateSequenceLocation;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

/**
 * Computes the directed Hausdorff distance from one geometry to another. 
 * The directed Hausdorff distance is the maximum distance any point
 * on a query geometry A can be from a target geometry B.
 * Equivalently, every point in the query geometry is within that distance
 * of the target geometry.
 * The class can compute a pair of points at which the distance is attained:
 * <tt>[ farthest A point, nearest B point ]</tt>.
 * <p>
 *The directed Hausdorff distance (DHD) is defined as:
 * <pre>
 * DHD(A,B) = max<sub>a &isin; A</sub> (max<sub>b &isin; B</sub> (distance(a, b) )
 * </pre>
 * <p>
 * DHD is asymmetric: <tt>DHD(A,B)</tt> may not be equal to <tt>DHD(B,A)</tt>.
 * Hence it is not a distance metric.
 * The Hausdorff distance is is a symmetric distance metric defined as:
 * <pre>
 * HD(A,B) = max(DHD(A,B), DHD(B,A))
 * </pre>
 * This can be computed via the 
 * {@link #hausdorffDistancePoints(Geometry, Geometry)} function.
 * <p>
 * Points, lines and polygons are supported as input.
 * If the query geometry is polygonal, 
 * the point at maximum distance may occur in the interior of a query polygon.
 * For a polygonal target geometry the point always lies on the boundary. 
 * <p>
 * A common use case is to test whether a geometry A lies fully within a given 
 * distance of another one B.
 * {@link #isFullyWithinDistance(Geometry, double)} 
 * can be used to test this efficiently.  
 * It implements heuristic checks and short-circuiting to improve performance.
 * <p>
 * The class can be used in prepared mode.
 * Creating an instance on a target geometry caches indexes for that geometry.
 * Then {@link #farthestPoints(Geometry) 
 * or {@link #isFullyWithinDistance(Geometry, double)}
 * can be called efficiently for multiple query geometries. 
 * <p>
 * If the Hausdorff distance is attained at a non-vertex of the query geometry,
 * the location is approximated.  
 * The algorithm uses a distance tolerance to control the approximation accuracy.
 * The tolerance is automatically determined to balance between accuracy and performance.
 * if more accuracy is desired some function signatures are provided 
 * which allow specifying a distance tolerance .
 * <p>
 * This algorithm is easier to use, more accurate, 
 * and much faster than {@link DiscreteHausdorffDistance}.
 * 
 * @author Martin Davis
 *
 */
public class DirectedHausdorffDistance {
  
  private static final double EMPTY_DISTANCE = Double.NaN;

  /**
   * Computes the directed Hausdorff distance 
   * of a query geometry A from a target one B.
   * 
   * @param a the query geometry  
   * @param b the target geometry
   * @return the directed Hausdorff distance,
   *   or NaN if an input is empty
   */
  public static double distance(Geometry a, Geometry b)
  {
    DirectedHausdorffDistance hd = new DirectedHausdorffDistance(b);
    return distance(hd.farthestPoints(a));
  }
  
  /**
   * Computes the directed Hausdorff distance 
   * of a query geometry A from a target one B,
   * up to a given distance accuracy.
   * 
   * @param a the query geometry  
   * @param b the target geometry
   * @param tolerance the accuracy distance tolerance
   * @return the directed Hausdorff distance,
   * or NaN if an input is empty
   */
  public static double distance(Geometry a, Geometry b, double tolerance)
  {
    DirectedHausdorffDistance hd = new DirectedHausdorffDistance(b);
    return distance(hd.farthestPoints(a, tolerance));
  }

  /**
   * Computes a line containing a pair of points which attain the directed Hausdorff distance 
   * of a query geometry A from a target one B.
   * 
   * @param a the query geometry  
   * @param b the target geometry
   * @param tolerance the accuracy distance tolerance
   * @return a pair of points [ptA, ptB] demonstrating the distance,
   * or null if an input is empty
   */
  public static Coordinate[] distancePoints(Geometry a, Geometry b)
  {
    DirectedHausdorffDistance dhd = new DirectedHausdorffDistance(b);
    return dhd.farthestPoints(a);
  }
  
  /**
   * Computes a line containing a pair of points which attain the directed Hausdorff distance 
   * of a query geometry A from a target one B, up to a given distance accuracy.
   * 
   * @param a the query geometry  
   * @param b the target geometry
   * @param tolerance the accuracy distance tolerance
   * @return a pair of points [ptA, ptB] demonstrating the distance,
   * or null if an input is empty
   */
  public static Coordinate[] distancePoints(Geometry a, Geometry b, double tolerance)
  {
    DirectedHausdorffDistance dhd = new DirectedHausdorffDistance(b);
    return dhd.farthestPoints(a, tolerance);
  }
  
  /**
   * Computes a pair of points which attain the symmetric Hausdorff distance 
   * between two geometries.
   * This the maximum of the two directed Hausdorff distances.
   * 
   * @param a a geometry  
   * @param b a geometry
   * @return a pair of points [ptA, ptB] demonstrating the Hausdorff distance,
   * or null if an input is empty
   */  
  public static Coordinate[] hausdorffDistancePoints(Geometry a, Geometry b)
  {
    DirectedHausdorffDistance hdAB = new DirectedHausdorffDistance(b);
    Coordinate[] ptsAB = hdAB.farthestPoints(a);
    DirectedHausdorffDistance hdBA = new DirectedHausdorffDistance(a);
    Coordinate[] ptsBA = hdBA.farthestPoints(b);
    
    //-- return points in A-B order
    Coordinate[] pts = ptsAB;
    if (distance(ptsBA) > distance(ptsAB)) {
      //-- reverse the BA points
      pts = pair(ptsBA[1], ptsBA[0]);
    }
    return pts;
  }
  
  /**
   * Computes the symmetric Hausdorff distance between two geometries.
   * This the maximum of the two directed Hausdorff distances.
   * 
   * @param a a geometry  
   * @param b a geometry
   * @return the Hausdorff distance, or NaN if an input is empty
   */  
  public static double hausdorffDistance(Geometry a, Geometry b)
  {
    return distance(hausdorffDistancePoints(a, b));
  }
  
  /**
   * Computes whether a query geometry lies fully within a give distance of a target geometry.
   * Equivalently, detects whether any point of the query geometry is farther 
   * from the target than the specified distance.
   * This is the case if <tt>DHD(A, B) > maxDistance</tt>.
   *  
   * @param a the query geometry  
   * @param b the target geometry
   * @param maxDistance the distance limit
   * @return true if the query geometry lies fully within the distance of the target
   */
  public static boolean isFullyWithinDistance(Geometry a, Geometry b, double maxDistance)
  {
    DirectedHausdorffDistance hd = new DirectedHausdorffDistance(b);
    return hd.isFullyWithinDistance(a, maxDistance);
  }
  
  /**
   * Computes whether a query geometry lies fully within a give distance of a target geometry,
   * up to a given distance accuracy.
   * Equivalently, detects whether any point of the query geometry is farther 
   * from the target than the specified distance.
   * This is the case if <tt>DHD(A, B) > maxDistance</tt>.
   *  
   * @param a the query geometry  
   * @param b the target geometry
   * @param maxDistance the distance limit
   * @param tolerance the accuracy distance tolerance
   * @return true if the query geometry lies fully within the distance of the target
   */
  public static boolean isFullyWithinDistance(Geometry a, Geometry b, double maxDistance, double tolerance)
  {
    DirectedHausdorffDistance hd = new DirectedHausdorffDistance(b);
    return hd.isFullyWithinDistance(a, maxDistance, tolerance);
  }
  
  private static double distance(Coordinate[] pts) {
    if (pts == null)
      return EMPTY_DISTANCE;
    return pts[0].distance(pts[1]);
  }

  private static Coordinate[] pair(Coordinate p0, Coordinate p1) {
    return new Coordinate[] { p0.copy(), p1.copy() };
  }
  
  /**
   * Heuristic factor to improve performance of area interior farthest point computation.
   */
  private static final double AREA_INTERIOR_PERFORMANCE_FACTOR = 20;
  
  /**
   * Heuristic automatic tolerance factor
   */
  private static final double AUTO_TOLERANCE_FACTOR = 1.0e4;
  
  private static double computeTolerance(Geometry geom) {
    return geom.getEnvelopeInternal().getDiameter() / AUTO_TOLERANCE_FACTOR;
  }

  private Geometry target;
  private TargetDistance targetDistance;

  /**
   * Create a new instance for a target geometry.
   * 
   * @param geom the geometry to compute the distance from
   */
  public DirectedHausdorffDistance(Geometry geom) {
    this.target = geom;
    targetDistance = new TargetDistance(this.target);
  }
  
  /**
   * Tests whether a query geometry lies fully within a give distance of the target geometry.
   * Equivalently, detects whether any point of the query geometry is farther 
   * from the target than the specified distance.
   * This is the case if <tt>DHD(A, B) > maxDistance</tt>.
   *  
   * @param geom the query geometry  
   * @param maxDistance the distance limit
   * @return true if the query geometry lies fully within the distance of the target
   */
  public boolean isFullyWithinDistance(Geometry geom, double maxDistance) {
    //TODO: should the tolerance be computed as a fraction of the maxDistance?
    //return isFullyWithinDistance(a, maxDistance, computeTolerance(a));
    double tolerance = maxDistance / AUTO_TOLERANCE_FACTOR;
    return isFullyWithinDistance(geom, maxDistance, tolerance);
  }
  
  /**
   * Tests whether a query geometry lies fully within a give distance of the target geometry,
   * up to a given distance accuracy.
   * Equivalently, detects whether any point of the query geometry is farther 
   * from the target than the specified distance.
   * This is the case if <tt>DHD(A, B) > maxDistance</tt>.
   *  
   * @param geom the query geometry  
   * @param maxDistance the distance limit
   * @param tolerance the accuracy distance tolerance
   * @return true if the query geometry lies fully within the distance of the target
   */
  public boolean isFullyWithinDistance(Geometry geom, double maxDistance, double tolerance) {
    //-- envelope checks
    if (isBeyond(geom.getEnvelopeInternal(), target.getEnvelopeInternal(), maxDistance))
      return false;

    Coordinate[] maxDistCoords = computeDistancePoints(geom, tolerance, maxDistance);
    //-- handle empty case
    if (maxDistCoords == null)
      return false;
    return distance(maxDistCoords) <= maxDistance;
  }

  /**
   * Tests if an envelope must have a side farther than the maximum distance
   * from another envelope.
   * 
   * @param envA
   * @param envB
   * @param maxDistance
   * @return true if envelope A must have a far point from envelope B
   */
  private static boolean isBeyond(Envelope envA, Envelope envB, double maxDistance) {
    /**
     * At least one point of the geometry lies on each edge of the envelope,
     * so if any edge is farther than maxDistance from the closest edge of the B envelope
     * there must be a point at that distance (or further).
     */
    return envA.getMinX() < envB.getMinX() - maxDistance
      || envA.getMinY() < envB.getMinY() - maxDistance
      || envA.getMaxX() > envB.getMaxX() + maxDistance
      || envA.getMaxY() > envB.getMaxY() + maxDistance;
  }
  
  /**
   * Computes a pair of points which attain the directed Hausdorff distance 
   * of a query geometry A from the target B.
   * If either geometry is empty the result is null.
   * 
   * @param geom the query geometry  
   * @return a pair of points [ptA, ptB] attaining the distance,
   * or null if an input is empty
   */
  public Coordinate[] farthestPoints(Geometry geom) {
    double tolerance = computeTolerance(geom);
    return farthestPoints(geom, tolerance);
  }
  
  /**
   * Computes a pair of points which attain the directed Hausdorff distance 
   * of a query geometry A from the target B,
   * up to a given distance accuracy.
   * If either geometry is empty the result is null.
   * 
   * @param geom the query geometry  
   * @param tolerance the approximation distance tolerance
   * @return a pair of points [ptA, ptB] attaining the distance,
   * or null if an input is empty
   */
  public Coordinate[] farthestPoints(Geometry geom, double tolerance) {
    return computeDistancePoints(geom, tolerance, -1.0);
  }

  private Coordinate[] computeDistancePoints(Geometry geom, double tolerance, double maxDistanceLimit) {
    if (geom.isEmpty() || target.isEmpty())
      return null;
    
    if (geom.getDimension() == Dimension.P) {
      return computeForPoints(geom, maxDistanceLimit);
    }
    //TODO: handle mixed geoms with points
    Coordinate[] maxDistPtsEdge = computeForEdges(geom, tolerance, maxDistanceLimit);
    
    if (isBeyondLimit(distance(maxDistPtsEdge), maxDistanceLimit)) {
      return maxDistPtsEdge;
    }
    
    /**
     * Polygonal query geometry may have an interior point as the farthest point.
     */
    if (geom.getDimension() == Dimension.A) {
      Coordinate[] maxDistPtsInterior = computeForAreaInterior(geom, tolerance);
      if (maxDistPtsInterior != null 
          && distance(maxDistPtsInterior) > distance(maxDistPtsEdge)) {
        return maxDistPtsInterior;
      }
    }
    return maxDistPtsEdge;
  }
  
  private Coordinate[] computeForPoints(Geometry geom, double maxDistanceLimit) {
    double maxDist = -1.0;;
    Coordinate[] maxDistPtsAB = null;
    Iterator geomi = new GeometryCollectionIterator(geom);
    while (geomi.hasNext()) {
      Geometry geomElem = (Geometry) geomi.next();
      if (! (geomElem instanceof Point))
        continue;
      
      Coordinate pA = geomElem.getCoordinate();
      Coordinate pB = targetDistance.nearestPoint(pA);
      double dist = pA.distance(pB);

      boolean isInterior = dist > 0 && targetDistance.isInterior(pA);
      //-- check for interior point
      if (isInterior) {
        dist = 0; 
        pB = pA;
      }
      if (dist > maxDist) {
        maxDist = dist;
        maxDistPtsAB = pair(pA, pB);
      }
      if (isBeyondLimit(maxDist, maxDistanceLimit)) {
        break;
      }
    }
    return maxDistPtsAB;
  }

  private Coordinate[] computeForEdges(Geometry geom, double tolerance, double maxDistanceLimit) {
    PriorityQueue<DHDSegment> segQueue = createSegQueue(geom);

    DHDSegment segMaxDist = null;
    long iter = 0;
    while (! segQueue.isEmpty()) {
      iter++;
      // get the segment with greatest distance bound
      DHDSegment segMaxBound = segQueue.remove();
      //System.out.println(segMaxBound);
      //System.out.println(WKTWriter.toLineString(segMaxBound.getMaxDistPts()));

/*
      double maxDistBound = segMaxBound.maxDistanceBound();
      double maxDist = segMaxBound.maxDistance();
      System.out.format("%s  len: %f bound: %f  maxDist: %f\n", 
          segMaxBound, segMaxBound.length(), maxDistBound, maxDist);
      if (maxDist > 1) {
        System.out.println("FOUND");
      }
*/
      /**
       * Save if segment point is farther than current farthest
       */
      if (segMaxDist == null 
          || segMaxBound.getMaxDistance() > segMaxDist.getMaxDistance()) {
        segMaxDist = segMaxBound;
      }
      /**
       * Stop searching if remaining items in queue must all be closer
       * than the current maximum distance.
       */
      if (segMaxBound.getMaxDistanceBound() <= segMaxDist.getMaxDistance()) {
        break;
      }
      /**
       * If maxDistanceLimit is specified, can stop searching if:
       * - if segment distance bound is less than distance limit, no other segment can be farther
       * - if a point of segment is farther than limit, isFulyWithin must be false
       */
      if (isWithinLimit(segMaxBound.getMaxDistanceBound(), maxDistanceLimit)
          || isBeyondLimit(segMaxBound.getMaxDistance(), maxDistanceLimit)
          ) {
        break;
      }

      /**
       * Check for equal or collinear segments.
       * If so, don't bisect the segment further.
       * This greatly improves performance when the inputs 
       * have identical or collinear segments
       * (in particular, the case when the inputs are identical).
       */
      if (segMaxBound.getMaxDistance() == 0.0) {
        if (isSameOrCollinear(segMaxBound))
          continue;
      }
      
      //System.out.println(segMaxDist);

      /**
       * If segment is longer than tolerance
       * it might provide a better max distance point,
       * so bisect and keep searching
       */
      if ((segMaxBound.getLength() > tolerance)) {
        DHDSegment[] bisects = segMaxBound.bisect(targetDistance);
        addNonInterior(bisects[0], segQueue);
        addNonInterior(bisects[1], segQueue);
      }
    }
    /**
     * A segment at maximum distance was found.
     * Return the farthest point pair
     */
    if (segMaxDist != null)
      return segMaxDist.getMaxDistPts();
    
    /**
     * No DHD segment was found. 
     * This must be because all were inside the target.
     * In this case distance is zero.
     * Return a single coordinate of the input as a representative point
     */
    Coordinate maxPt = geom.getCoordinate();
    return pair(maxPt, maxPt);
  }
  
  private boolean isSameOrCollinear(DHDSegment seg) {
    CoordinateSequenceLocation f0 = targetDistance.nearestLocation(seg.p0);
    CoordinateSequenceLocation f1 = targetDistance.nearestLocation(seg.p1);
    return f0.isSameSegment(f1);
  }

  private static boolean isBeyondLimit(double maxDist, double maxDistanceLimit) {
    return maxDistanceLimit >= 0 && maxDist > maxDistanceLimit;
  }

  private static boolean isWithinLimit(double maxDist, double maxDistanceLimit) {
    return maxDistanceLimit >= 0 && maxDist <= maxDistanceLimit;
  }

  private void addNonInterior(DHDSegment segment, PriorityQueue<DHDSegment> segQueue) {
    //-- discard segment if it is interior to a polygon
    if (isInterior(segment)) {
      return;
    }
    segQueue.add(segment);
  }

  /**
   * Tests if segment is fully in the interior of the target geometry polygons (if any).
   *  
   * @param segment
   * @return
   */
  private boolean isInterior(DHDSegment segment) {
    if (segment.getMaxDistance() > 0.0) {
      return false;
    }
    return targetDistance.isInterior(segment.getEndpoint(0), segment.getEndpoint(1));
  }

  /**
   * If the query geometry is polygonal, it is possible
   * the farthest point lies in its interior.
   * In this case it occurs at the centre of the Largest Empty Circle
   * with B as obstacles and query geometry as constraint.
   * 
   * This is a potentially expensive computation, 
   * especially for small tolerances.  
   * It is avoided where possible if heuristic checks
   * can determine that the max distance is at 
   * the previously computed edge points.
   * 
   * @param geom
   * @param tolerance
   * @return the maximum distance point pair at an interior point of a polygon, 
   *   or null if it is known to not occur at an interior point 
   */
  private Coordinate[] computeForAreaInterior(Geometry geom, double tolerance) {
    //TODO: extract polygonal geoms from A
    Geometry polygonal = geom;
    
    /**
     * Optimization - skip if A interior cannot intersect B,
     * and thus farther point must lie on A boundary
     */
    if (polygonal.getEnvelopeInternal().disjoint(target.getEnvelopeInternal())) {
      return null;
    }
    
    /**
     * The LargestEmptyCircle computation is much slower than the boundary one, 
     * is quite unlikely to occur,
     * and accuracy is probably less critical (or obvious).
     * So improve performance by using a coarser distance tolerance.
     */
    double lecTol = AREA_INTERIOR_PERFORMANCE_FACTOR * tolerance;

    //TODO: add short-circuiting based on maxDistanceLimit?
    
    Point centerPt = LargestEmptyCircle.getCenter(target, polygonal, lecTol);
    Coordinate ptA = centerPt.getCoordinate();
    /**
     * If LEC centre is in B, the max distance is zero, so return null.
     * This will cause the computed segment distance to be returned,
     * which is preferred since it occurs on a vertex or edge.
     */
    if (targetDistance.isInterior(ptA)) {
      return null;
    }
    Coordinate ptB = targetDistance.nearestFacetPoint(ptA);
    return pair(ptA, ptB);
  }

  /**
   * Creates the priority queue for segments.
   * Segments which are interior to a polygonal target geometry
   * are not added to the queue.
   * So if a query geometry is fully covered by the target
   * the returned queue is empty.
   * 
   * @param geom
   * @return the segment priority queue
   */
  private PriorityQueue<DHDSegment> createSegQueue(Geometry geom) {
    PriorityQueue<DHDSegment> priq = new PriorityQueue<DHDSegment>();
    geom.apply(new GeometryComponentFilter() {

      @Override
      public void filter(Geometry geom) {
        if (geom instanceof LineString) {
          addSegments(geom.getCoordinates(), priq);
        }
      }
    });
    return priq;
  }

  /**
   * Add segments to queue 
   *  
   * @param pts
   * @param priq
   */
  private void addSegments(Coordinate[] pts, PriorityQueue<DHDSegment> priq) {
    DHDSegment segMaxDist = null;
    DHDSegment prevSeg = null;
    for (int i = 0; i < pts.length - 1; i++) {
      DHDSegment seg;
      if (i == 0) {
        seg = DHDSegment.create(pts[i], pts[i + 1], targetDistance);
      } 
      else {
        //-- avoiding recomputing prev pt distance
        seg = DHDSegment.create(prevSeg, pts[i + 1], targetDistance);
      }
      prevSeg = seg;
      
      //-- don't add segment if it can't be further away then current max
      if (segMaxDist == null 
          || seg.getMaxDistanceBound() > segMaxDist.getMaxDistance()) {
        /**
         * Don't add interior segments, since their distance must be zero.
         */
        addNonInterior(seg, priq);
      }
      
      if (segMaxDist == null 
          || seg.getMaxDistance() > segMaxDist.getMaxDistance()) {
        segMaxDist = seg;;
      }
      //System.out.println(seg.distance());
    }
  }
  
  private static class TargetDistance {
    private IndexedFacetDistance distanceToFacets;
    private boolean isArea;
    private IndexedPointInPolygonsLocator ptInArea;
    
    public TargetDistance(Geometry geom) {
      distanceToFacets = new IndexedFacetDistance(geom);
      isArea = geom.getDimension() >= Dimension.A;
      if (isArea) {
        ptInArea = new IndexedPointInPolygonsLocator(geom);
      }
    }

    public CoordinateSequenceLocation nearestLocation(Coordinate p) {
      return distanceToFacets.nearestLocation(p);
    }

    public Coordinate nearestFacetPoint(Coordinate p) {
      return distanceToFacets.nearestPoint(p);
    }
    
    public Coordinate nearestPoint(Coordinate p) {
      if (ptInArea != null) {
        if (ptInArea.locate(p) != Location.EXTERIOR) {
          return p;
        }
      }
      Coordinate nearestPt = distanceToFacets.nearestPoint(p);
      return nearestPt;
    }
    
    public boolean isInterior(Coordinate p) {
      if (! isArea) return false;
      return ptInArea.locate(p) == Location.INTERIOR;
    }

    public boolean isInterior(Coordinate p0, Coordinate p1) {
      if (! isArea)
        return false;
      //-- compute distance to B linework
      double segDist = distanceToFacets.distance(p0, p1);
      //-- if segment touches B linework it is not in interior
      if (segDist == 0)
        return false;
      //-- only need to test one point to check interior
      boolean isInterior = isInterior(p0);
      return isInterior;
    }

  }
  
  private static class DHDSegment implements Comparable<DHDSegment> {

    public static DHDSegment create(Coordinate p0, Coordinate p1, TargetDistance dist) {
      DHDSegment seg = new DHDSegment(p0, p1);
      seg.init(dist);
      return seg;
    }

    public static DHDSegment create(DHDSegment prevSeg, Coordinate p1, TargetDistance dist) {
      DHDSegment seg = new DHDSegment(prevSeg.p1, p1);
      seg.init(prevSeg.nearPt1, dist);
      return seg;
    }

    private Coordinate p0;
    private Coordinate nearPt0;
    private Coordinate p1;
    private Coordinate nearPt1;
    private double maxDistanceBound = Double.NEGATIVE_INFINITY;
    private double maxDistance;
    
    private DHDSegment(Coordinate p0, Coordinate p1) {
      this.p0 = p0;
      this.p1 = p1;
    }

    private DHDSegment(Coordinate p0, Coordinate nearPt0, Coordinate p1, Coordinate nearPt1) {
      this.p0 = p0;
      this.nearPt0 = nearPt0;
      this.p1 = p1;
      this.nearPt1 = nearPt1;
      computeMaxDistances();
    }

    private void init(TargetDistance dist) {
      nearPt0 = dist.nearestPoint(p0);
      nearPt1 = dist.nearestPoint(p1);
      computeMaxDistances();
    }

    private void init(Coordinate nearest0, TargetDistance dist) {
      nearPt0 = nearest0;
      nearPt1 = dist.nearestPoint(p1);
      computeMaxDistances();
    }

    public Coordinate getEndpoint(int index) {
      return index == 0 ? p0 : p1;
    }

    public double getLength() {
      return p0.distance(p1);
    }
    
    public double getMaxDistance() {
      return maxDistance;
    }
    
    public double getMaxDistanceBound() {
      return maxDistanceBound;
    }
    
    public Coordinate[] getMaxDistPts() {
      double dist0 = p0.distance(nearPt0);
      double dist1 = p1.distance(nearPt1);
      if (dist0 > dist1) {
        return pair(p0, nearPt0);        
      }
      return pair(p1, nearPt1);        
    }

    /**
     * Computes a least upper bound for the maximum distance to a segment.
     */
    private void computeMaxDistances() {
      /**
       * Least upper bound is the max distance to the endpoints,
       * plus half segment length.
       */
      double dist0 = p0.distance(nearPt0);
      double dist1 = p1.distance(nearPt1);
      maxDistance = Math.max(dist0, dist1);
      maxDistanceBound = maxDistance + getLength() / 2;
    } 
    
    public DHDSegment[] bisect(TargetDistance dist) {
      Coordinate mid = new Coordinate(
          (p0.x + p1.x) / 2, 
          (p0.y + p1.y) / 2 
          );
      Coordinate nearPtMid = dist.nearestPoint(mid);
      return new DHDSegment[] {
          new DHDSegment(p0, nearPt0, mid, nearPtMid ),
          new DHDSegment(mid, nearPtMid, p1, nearPt1)
      };
    }

    /**
     * For maximum efficiency sort the PriorityQueue with largest maxDistance at front.
     * Since Java PQ sorts least-first, need to invert the comparison
     */
    public int compareTo(DHDSegment o) {
      return -Double.compare(maxDistanceBound, o.maxDistanceBound);
    }
    
    public String toString() {
      return WKTWriter.toLineString(p0, p1);
    }
  }
}
