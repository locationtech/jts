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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.GeometryComponentFilter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

/**
 * Computes the directed Hausdorff distance from one geometry to another, 
 * up to an approximation distance tolerance. 
 * The directed Hausdorff distance (DHD) is defined as:
 * <pre>
 * DHD(A,B) = max<sub>a &isin; A</sub> (max<sub>b &isin; B</sub> (distance(a, b) )
 * </pre>
 * The DHD is the maximum distance any point
 * on a query geometry A can be from a target geometry B.
 * Equivalently, every point in the query geometry is within the DHD distance
 * of the target geometry.
 * The class can compute a pair of points at which the DHD is obtained:
 * <tt>[ farthest A point, nearest B point ]</tt>.
 * <p>
 * The DHD is asymmetric: <tt>DHD(A,B)</tt> may not be equal to <tt>DHD(B,A)</tt>.
 * Hence it is not a distance metric.
 * The Hausdorff distance is is a symmetric distance metric:
 * <pre>
 * HD(A,B) = max(DHD(A,B), DHD(B,A))
 * </pre>
 * This can be computed via the 
 * {@link #hausdorffDistanceLine(Geometry, Geometry, double)} function.
 * <p>
 * Points, lines and polygons are supported as input.
 * If the query geometry is polygonal, 
 * the point of maximum distance may occur in the interior of a polygon.
 * <p>
 * The class can be used in prepared mode.
 * Creating an instance on a target geometry caches indexes for that geometry.
 * Then {@link #computeDistancePoints(Geometry, double) 
 * or {@link #isFullyWithinDistance(Geometry, double, double)}
 * can be called efficiently for multiple query geometries.
 * <p>
 * A use case is to test whether a geometry A lies fully within a given 
 * distance of another one B.
 * Using {@link #isFullyWithinDistance(Geometry, double, double)} 
 * is much more efficient than computing whether A is covered by a buffer of B.
 * <p>
 * Due to the nature of the Hausdorff distance, 
 * performance is not very sensitive to the distance tolerance,
 * so using a small tolerance is recommended.
 * <p>
 * This algorithm is easier to use, more accurate, 
 * and much faster than {@link DiscreteHausdorrffDistance}.
 * 
 * @author Martin Davis
 *
 */
public class DirectedHausdorffDistance {

  public static double distance(Geometry a, Geometry b, double tolerance)
  {
    DirectedHausdorffDistance hd = new DirectedHausdorffDistance(b);
    return distance(hd.computeDistancePoints(a, tolerance));
  }
  
  public static LineString distanceLine(Geometry a, Geometry b, double tolerance)
  {
    DirectedHausdorffDistance hd = new DirectedHausdorffDistance(b);
    return a.getFactory().createLineString(hd.computeDistancePoints(a, tolerance));
  }
  
  public static boolean isFullyWithinDistance(Geometry a, Geometry b, double maxDistance, double tolerance)
  {
    DirectedHausdorffDistance hd = new DirectedHausdorffDistance(b);
    return hd.isFullyWithinDistance(a, maxDistance, tolerance);
  }

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
      pts = copyReverse(ptsBA);
    }
    return a.getFactory().createLineString(pts);
  }
  
  private Geometry geomB;
  private IndexedFacetDistance distToB;
  private boolean isAreaB;
  private IndexedPointInPolygonsLocator ptInAreaB;

  public DirectedHausdorffDistance(Geometry b) {
    geomB = b;
    distToB = new IndexedFacetDistance(b);
    isAreaB = b.getDimension() >= Dimension.A;
    if (isAreaB) {
      ptInAreaB = new IndexedPointInPolygonsLocator(b);
    }
  }
  
  public boolean isFullyWithinDistance(Geometry a, double maxDistance, double tolerance) {
    //TODO: optimize with short-circuiting
    Coordinate[] maxDistCoords = computeDistancePoints(a, tolerance);
    return distance(maxDistCoords) <= maxDistance;
  }
  
  private static double distance(Coordinate[] pts) {
    return pts[0].distance(pts[1]);
  }

  private Coordinate[] computeDistancePoints(Geometry geomA, double tolerance) {
    if (geomA.getDimension() == Dimension.P) {
      return computeAtPoints(geomA);
    }
    //TODO: handle mixed geoms with points
    Coordinate[] maxDistPtsEdge = computeAtEdges(geomA, tolerance);
    
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
    
    Point centerPt = LargestEmptyCircle.getCenter(geomB, polygonalA, tolerance);
    Coordinate ptA = centerPt.getCoordinate();
    /**
     * If LEC centre is in B, the max distance is zero, so return null.
     * This will cause the computed segment distance to be returned,
     * which is better since it occurs on a vertex (or edge?)
     */
    if (isInteriorB(ptA)) {
      return null;
    }
    Coordinate[] nearPtsBA = distToB.nearestPoints(ptA);
    return copyReverse(nearPtsBA);
  }

  private Coordinate[] computeAtPoints(Geometry geomA) {
    double maxDist = -1.0;;
    Coordinate[] maxDistPtsAB = null;
    Iterator geomi = new GeometryCollectionIterator(geomA);
    while (geomi.hasNext()) {
      Geometry geomElemA = (Geometry) geomi.next();
      if (! (geomElemA instanceof Point))
        continue;
      
      Coordinate pA = geomElemA.getCoordinate();
      Coordinate[] nearPtsBA = distToB.nearestPoints(pA);
      double dist = distance(nearPtsBA);

      boolean isInterior = dist > 0 && isInteriorB(pA);
      //-- check for interior point
      if (isInterior) {
        dist = 0; 
        nearPtsBA = copyPair( pA, pA );
      }
      if (dist > maxDist) {
        maxDist = dist;
        maxDistPtsAB = copyReverse(nearPtsBA);
      }
    }
    return maxDistPtsAB;
  }
  
  private static Coordinate[] copyPair(Coordinate p0, Coordinate p1) {
    return new Coordinate[] { p0.copy(), p1.copy() };
  }

  private static Coordinate[] copyReverse(Coordinate[] pts) {
    return new Coordinate[] { pts[1].copy(), pts[0].copy() };
  }

  public Coordinate[] computeAtEdges(Geometry geomA, double tolerance) {
    PriorityQueue<DHDSegment> segQueue = createSegQueue(geomA);
    
    DHDSegment ohdSeg = null;
    DHDSegment maxDistSeg = null;
    long iter = 0;
    while (! segQueue.isEmpty()) {
      iter++;
      // get the segment with greatest distance
      maxDistSeg = segQueue.remove();
      
      if (maxDistSeg.length() <= tolerance) {
        ohdSeg = maxDistSeg;
        break;
      }
      
      //-- not within tolerance, so bisect segment and keep searching
      DHDSegment[] bisects = maxDistSeg.bisect(distToB);
      addNonInterior(bisects[0], segQueue);
      addNonInterior(bisects[1], segQueue);
    }
    if (ohdSeg != null)
      return ohdSeg.getMaxDistPts();
    
    /**
     * If no OHD segment was found, all were inside the target.
     * In this case distance is zero.
     * Return a single coordinate of the input as a representative point
     */
    Coordinate maxPt = maxDistSeg.getEndpoint(0);
    return copyPair(maxPt, maxPt);
  }

  private void addNonInterior(DHDSegment ohdSegment, PriorityQueue<DHDSegment> segQueue) {
    //-- discard segment if it is interior to a polygon
    if (isInterior(ohdSegment)) {
      return;
    }
    segQueue.add(ohdSegment);
  }

  /**
   * Tests if segment is fully in the interior of the target geometry polygons (if any).
   *  
   * @param ohdSegment
   * @return
   */
  private boolean isInterior(DHDSegment ohdSegment) {
    if (! isAreaB)
      return false;
    double segDist = distToB.distance(ohdSegment.getEndpoint(0), ohdSegment.getEndpoint(1));
    //-- if segment touches B it is not in interior
    if (segDist == 0)
      return false;
    //-- only need to test one point to check interior
    Coordinate pt = ohdSegment.getEndpoint(0);
    boolean isInterior = isInteriorB(pt);
    return isInterior;
  }

  private boolean isInteriorB(Coordinate p) {
    if (! isAreaB) return false;
    return ptInAreaB.locate(p) == Location.INTERIOR;
  }
  
  private PriorityQueue<DHDSegment> createSegQueue(Geometry geomA) {
    PriorityQueue<DHDSegment> priq = new PriorityQueue<DHDSegment>();
    geomA.apply(new GeometryComponentFilter() {

      @Override
      public void filter(Geometry geom) {
        if (geom instanceof LineString) {
          initSegments(geom.getCoordinates(), priq);
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
  private void initSegments(Coordinate[] pts, PriorityQueue<DHDSegment> priq) {
    DHDSegment prevSeg = null;
    for (int i = 0; i < pts.length - 1; i++) {
      DHDSegment seg;
      if (i == 0) {
        seg = DHDSegment.create(pts[i], pts[i + 1], distToB);
      } 
      else {
        //-- optimize by avoiding recomputing pt distance
        seg = DHDSegment.create(prevSeg, pts[i + 1], distToB);
      }
      prevSeg = seg;
      /**
       * Delay interior check until splitting, 
       * to avoid computing more than needed.
       */
      priq.add(seg);
      //System.out.println(seg.distance());
    }
  }
  
  private static class DHDSegment implements Comparable<DHDSegment> {

    public static DHDSegment create(Coordinate p0, Coordinate p1, IndexedFacetDistance indexDist) {
      DHDSegment seg = new DHDSegment(p0, p1);
      seg.init(indexDist);
      return seg;
    }

    public static DHDSegment create(DHDSegment prevSeg, Coordinate p1, IndexedFacetDistance indexDist) {
      DHDSegment seg = new DHDSegment(prevSeg.p1, p1);
      seg.init(prevSeg.nearPt1, indexDist);
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

    private void init(IndexedFacetDistance indexDist) {
      nearPt0 = nearestPt(p0, indexDist);
      nearPt1 = nearestPt(p1, indexDist);
      computeMaxDistanceBound();
    }

    private void init(Coordinate nearest0, IndexedFacetDistance indexDist) {
      nearPt0 = nearest0;
      nearPt1 = nearestPt(p1, indexDist);
      computeMaxDistanceBound();
    }

    private static Coordinate nearestPt(Coordinate p, IndexedFacetDistance indexDist) {
      Coordinate[] nearestPts = indexDist.nearestPoints(p);
      return nearestPts[0];
    }

    public Coordinate getEndpoint(int index) {
      return index == 0 ? p0 : p1;
    }
    
    public Coordinate getNearestPt(int index) {
      return index == 0 ? nearPt0 : nearPt1;
    }

    public double length() {
      return p0.distance(p1);
    }
    
    public Coordinate[] getMaxDistPts() {
      double dist0 = p0.distance(nearPt0);
      double dist1 = p1.distance(nearPt1);
      if (dist0 > dist1) {
        return copyPair(p0, nearPt0);        
      }
      return copyPair(p1, nearPt1);        
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
    
    public DHDSegment[] bisect(IndexedFacetDistance distToB) {
      Coordinate mid = new Coordinate(
          (p0.x + p1.x) / 2, 
          (p0.y + p1.y) / 2 
          );
      Coordinate nearPtMid = nearestPt(mid, distToB);
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
  }
}
