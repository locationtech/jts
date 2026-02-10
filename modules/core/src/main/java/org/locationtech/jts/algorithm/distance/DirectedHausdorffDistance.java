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
import org.locationtech.jts.operation.distance.FacetLocation;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

/**
 * Computes the directed Hausdorff distance from one geometry to another, 
 * up to an approximation distance tolerance. 
 * The directed Hausdorff distance is the maximum distance any point
 * on a query geometry A can be from a target geometry B.
 * Equivalently, every point in the query geometry is within that distance
 * of the target geometry.
 * The class can compute a pair of points at which the DHD is obtained:
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
 * {@link #hausdorffDistanceLine(Geometry, Geometry, double)} function.
 * <p>
 * Points, lines and polygons are supported as input.
 * If the query geometry is polygonal, 
 * the point at maximum distance may occur in the interior of a query polygon.
 * For a polygonal target geometry the point always lies on the boundary. 
 * <p>
 * A common use case is to test whether a geometry A lies fully within a given 
 * distance of another one B.
 * {@link #isFullyWithinDistance(Geometry, double, double)} 
 * can be used to test this efficiently.  
 * It implements heuristic checks and short-circuiting to improve performance.
 * This can much more efficient than computing whether A is covered by B.buffer(distance).
 * It is also more accurate, since constructed buffers 
 * are only linearized approximations to the true buffer.
 * <p>
 * The class can be used in prepared mode.
 * Creating an instance on a target geometry caches indexes for that geometry.
 * Then {@link #computeDistancePoints(Geometry, double) 
 * or {@link #isFullyWithinDistance(Geometry, double, double)}
 * can be called efficiently for multiple query geometries.
 * <p>
 * Due to the nature of the Hausdorff distance, 
 * performance is not very sensitive to the distance tolerance,
 * so using a small tolerance is recommended.
 * <p>
 * This algorithm is easier to use, more accurate, 
 * and much faster than {@link DiscreteHausdorffDistance}.
 * 
 * @author Martin Davis
 *
 */
public class DirectedHausdorffDistance {

  /**
   * Computes the directed Hausdorff distance of a query geometry A from a target one B.
   * 
   * @param a the query geometry  
   * @param b the target geometry
   * @param tolerance the approximation distance tolerance
   * @return the directed Hausdorff distance
   */
  public static double distance(Geometry a, Geometry b, double tolerance)
  {
    DirectedHausdorffDistance hd = new DirectedHausdorffDistance(b);
    return distance(hd.computeDistancePoints(a, tolerance));
  }
  
  /**
   * Computes a pair of points which attain the directed Hausdorff distance 
   * of a query geometry A from a target one B.
   * 
   * @param a the query geometry  
   * @param b the target geometry
   * @param tolerance the approximation distance tolerance
   * @return a pair of points [ptA, ptB] demonstrating the distance
   */
  public static LineString distanceLine(Geometry a, Geometry b, double tolerance)
  {
    DirectedHausdorffDistance hd = new DirectedHausdorffDistance(b);
    return a.getFactory().createLineString(hd.computeDistancePoints(a, tolerance));
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
   * @param tolerance the approximation distance tolerance
   * @return true if the query geometry lies fully within the distance of the target
   */
  public static boolean isFullyWithinDistance(Geometry a, Geometry b, double maxDistance, double tolerance)
  {
    DirectedHausdorffDistance hd = new DirectedHausdorffDistance(b);
    return hd.isFullyWithinDistance(a, maxDistance, tolerance);
  }

  /**
   * Computes a pair of points which attain the Hausdorff distance 
   * between two geometries.
   * 
   * @param a a geometry  
   * @param b a geometry
   * @param tolerance the approximation distance tolerance
   * @return a pair of points [ptA, ptB] demonstrating the Hausdorff distance
   */
  public static LineString hausdorffDistanceLine(Geometry a, Geometry b, double tolerance)
  {
    DirectedHausdorffDistance hdAB = new DirectedHausdorffDistance(b);
    Coordinate[] ptsAB = hdAB.computeDistancePoints(a, tolerance);
    DirectedHausdorffDistance hdBA = new DirectedHausdorffDistance(a);
    Coordinate[] ptsBA = hdBA.computeDistancePoints(b, tolerance);
    
    //-- return points in A-B order
    Coordinate[] pts;
    if (distance(ptsAB) > distance(ptsBA)) {
      pts = ptsAB;
    } 
    else {
      pts = pairReverse(ptsBA);
    }
    return a.getFactory().createLineString(pts);
  }
  
  private static double distance(Coordinate[] pts) {
    return pts[0].distance(pts[1]);
  }

  private static Coordinate[] pair(Coordinate p0, Coordinate p1) {
    return new Coordinate[] { p0.copy(), p1.copy() };
  }

  private static Coordinate[] pairReverse(Coordinate[] pts) {
    return new Coordinate[] { pts[1].copy(), pts[0].copy() };
  }
  
  private Geometry geomB;
  private TargetDistance distanceToB;

  /**
   * Create a new instance for a target geometry B
   * 
   * @param b the geometry to compute the distance from
   */
  public DirectedHausdorffDistance(Geometry b) {
    geomB = b;
    distanceToB = new TargetDistance(geomB);
  }
  
  /**
   * Computes whether a query geometry lies fully within a give distance of the target geometry.
   * Equivalently, detects whether any point of the query geometry is farther 
   * from the target than the specified distance.
   * This is the case if <tt>DHD(A, B) > maxDistance</tt>.
   *  
   * @param a the query geometry  
   * @param maxDistance the distance limit
   * @param tolerance the approximation distance tolerance
   * @return true if the query geometry lies fully within the distance of the target
   */
  public boolean isFullyWithinDistance(Geometry a, double maxDistance, double tolerance) {
    //-- envelope checks
    if (isBeyond(a.getEnvelopeInternal(), geomB.getEnvelopeInternal(), maxDistance))
      return false;

    Coordinate[] maxDistCoords = computeDistancePoints(a, tolerance, maxDistance);
    return distance(maxDistCoords) <= maxDistance;
  }

  /**
   * Tests if a geometry must have a point farther than the maximum distance
   * using the geometry envelopes.
   * 
   * @param envA
   * @param envB
   * @param maxDistance
   * @return true if geometry A must have a far point from B
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
   * of a query geometry A from the target.
   * 
   * @param geomA the query geometry  
   * @param tolerance the approximation distance tolerance
   * @return a pair of points [ptA, ptB] demonstrating the distance
   */
  public Coordinate[] computeDistancePoints(Geometry geomA, double tolerance) {
    return computeDistancePoints(geomA, tolerance, -1.0);
  }
  
  private Coordinate[] computeDistancePoints(Geometry geomA, double tolerance, double maxDistanceLimit) {
    if (geomA.getDimension() == Dimension.P) {
      return computeAtPoints(geomA, maxDistanceLimit);
    }
    //TODO: handle mixed geoms with points
    Coordinate[] maxDistPtsEdge = computeAtEdges(geomA, tolerance, maxDistanceLimit);
    
    if (isBeyondLimit(distance(maxDistPtsEdge), maxDistanceLimit)) {
      return maxDistPtsEdge;
    }
    
    /**
     * Polygonal query geometry may have an interior point as the farthest point.
     */
    if (geomA.getDimension() == Dimension.A) {
      Coordinate[] maxDistPtsInterior = computeAtAreaInterior(geomA, tolerance);
      if (maxDistPtsInterior != null 
          && distance(maxDistPtsInterior) > distance(maxDistPtsEdge)) {
        return maxDistPtsInterior;
      }
    }
    return maxDistPtsEdge;
  }
  
  private Coordinate[] computeAtPoints(Geometry geomA, double maxDistanceLimit) {
    double maxDist = -1.0;;
    Coordinate[] maxDistPtsAB = null;
    Iterator geomi = new GeometryCollectionIterator(geomA);
    while (geomi.hasNext()) {
      Geometry geomElemA = (Geometry) geomi.next();
      if (! (geomElemA instanceof Point))
        continue;
      
      Coordinate pA = geomElemA.getCoordinate();
      Coordinate pB = distanceToB.nearestPoint(pA);
      double dist = pA.distance(pB);

      boolean isInterior = dist > 0 && distanceToB.isInterior(pA);
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

  private Coordinate[] computeAtEdges(Geometry geomA, double tolerance, double maxDistanceLimit) {
    PriorityQueue<DHDSegment> segQueue = createSegQueue(geomA);

    DHDSegment segMaxDist = null;
    long iter = 0;
    while (! segQueue.isEmpty()) {
      iter++;
      // get the segment with greatest distance bound
      DHDSegment segMaxBound = segQueue.remove();
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
       * Save if segment is farther than most distant so far
       */
      if (segMaxDist == null 
          || segMaxBound.maxDistance() > segMaxDist.maxDistance()) {
        segMaxDist = segMaxBound;
      }
      /**
       * If maxDistanceLimit is specified, short-circuit if:
       * - if segment distance bound is less than distance limit, no other segment can be farther
       * - if a point of segment is farther than limit, isFulyWithin must be false
       */
      if (isWithinLimit(segMaxBound.maxDistanceBound(), maxDistanceLimit)
          || isBeyondLimit(segMaxBound.maxDistance(), maxDistanceLimit)
          ) {
        break;
      }
      
      /**
       * Check for equal or coincident segments.
       * If so, don't bisect the segment further.
       * This improves performance when the inputs have identical segments.
       */
      if (segMaxBound.maxDistance() == 0.0) {
        if (isSameSegment(segMaxBound))
          continue;
        //System.out.println(segMaxDist);
        //isSameSegment(segMaxBound);
      }
      
      //System.out.println(segMaxDist);

      /**
       * If segment is longer than tolerance 
       * and it might provide a better max distance point,
       * bisect and keep searching
       */
      if ((segMaxBound.length() > tolerance)
          && segMaxBound.maxDistanceBound() > segMaxDist.maxDistance()) {
        DHDSegment[] bisects = segMaxBound.bisect(distanceToB);
        addNonInterior(bisects[0], segQueue);
        addNonInterior(bisects[1], segQueue);
      }
    }
    if (segMaxDist != null)
      return segMaxDist.getMaxDistPts();
    
    /**
     * No DHD segment was found. 
     * This must be because all were inside the target.
     * In this case distance is zero.
     * Return a single coordinate of the input as a representative point
     */
    Coordinate maxPt = geomA.getCoordinate();
    return pair(maxPt, maxPt);
  }
  
  private boolean isSameSegment(DHDSegment seg) {
    FacetLocation f0 = distanceToB.nearestLocation(seg.p0);
    FacetLocation f1 = distanceToB.nearestLocation(seg.p1);
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
    if (segment.maxDistance() > 0.0) {
      return false;
    }
    return distanceToB.isInterior(segment.getEndpoint(0), segment.getEndpoint(1));
  }

  /**
   * If the query geometry A is polygonal, it is possible
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
   * @param geomA
   * @param tolerance
   * @return the maximum distance point pair at an interior point of A, 
   *   or null if it is known to not occur at an interior point 
   */
  private Coordinate[] computeAtAreaInterior(Geometry geomA, double tolerance) {
    //TODO: extract polygonal geoms from A
    Geometry polygonalA = geomA;
    
    /**
     * Optimization - skip if A interior cannot intersect B,
     * and thus farther point must lie on A segment
     */
    if (polygonalA.getEnvelopeInternal().disjoint(geomB.getEnvelopeInternal())) {
      return null;
    }
    
    //TODO: add short-circuiting based on maxDistanceLimit?
    
    Point centerPt = LargestEmptyCircle.getCenter(geomB, polygonalA, tolerance);
    Coordinate ptA = centerPt.getCoordinate();
    /**
     * If LEC centre is in B, the max distance is zero, so return null.
     * This will cause the computed segment distance to be returned,
     * which is preferred since it occurs on a vertex or edge.
     */
    if (distanceToB.isInterior(ptA)) {
      return null;
    }
    Coordinate ptB = distanceToB.nearestFacetPoint(ptA);
    return pair(ptA, ptB);
  }

  /**
   * Creates the priority queue for segments.
   * Segments which are interior to a polygonal target geometry
   * are not added to the queue.
   * So if a query geometry is fully covered by the target
   * the returned queue is empty.
   * 
   * @param geomA
   * @return the segment priority queue
   */
  private PriorityQueue<DHDSegment> createSegQueue(Geometry geomA) {
    PriorityQueue<DHDSegment> priq = new PriorityQueue<DHDSegment>();
    geomA.apply(new GeometryComponentFilter() {

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
        seg = DHDSegment.create(pts[i], pts[i + 1], distanceToB);
      } 
      else {
        //-- avoiding recomputing prev pt distance
        seg = DHDSegment.create(prevSeg, pts[i + 1], distanceToB);
      }
      prevSeg = seg;
      
      //-- don't bother adding segment if it can't be further away then current max
      if (segMaxDist == null 
          || seg.maxDistanceBound() > segMaxDist.maxDistance()) {
        /**
         * Don't add interior segments, since their distance must be zero.
         */
        addNonInterior(seg, priq);
      }
      
      if (segMaxDist == null 
          || seg.maxDistance() > segMaxDist.maxDistance()) {
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

    public FacetLocation nearestLocation(Coordinate p) {
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
    double maxDistanceBound = Double.NEGATIVE_INFINITY;
    
    private DHDSegment(Coordinate p0, Coordinate p1) {
      this.p0 = p0;
      this.p1 = p1;
    }

    private DHDSegment(Coordinate p0, Coordinate nearPt0, Coordinate p1, Coordinate nearPt1) {
      this.p0 = p0;
      this.nearPt0 = nearPt0;
      this.p1 = p1;
      this.nearPt1 = nearPt1;
      computeMaxDistanceBound();
    }

    private void init(TargetDistance dist) {
      nearPt0 = dist.nearestPoint(p0);
      nearPt1 = dist.nearestPoint(p1);
      computeMaxDistanceBound();
    }

    private void init(Coordinate nearest0, TargetDistance dist) {
      nearPt0 = nearest0;
      nearPt1 = dist.nearestPoint(p1);
      computeMaxDistanceBound();
    }

    public Coordinate getEndpoint(int index) {
      return index == 0 ? p0 : p1;
    }

    public double length() {
      return p0.distance(p1);
    }
    
    public double maxDistance() {
      double dist0 = p0.distance(nearPt0);
      double dist1 = p1.distance(nearPt1);
      return Math.max(dist0,dist1);
    }
    
    public double maxDistanceBound() {
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
    private void computeMaxDistanceBound() {
      //System.out.println(distance());
      
      /**
       * Least upper bound is the max distance to the endpoints,
       * plus half segment length.
       */
      double dist0 = p0.distance(nearPt0);
      double dist1 = p1.distance(nearPt1);
      double maxDist = Math.max(dist0, dist1);
      maxDistanceBound = maxDist + length() / 2;
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
