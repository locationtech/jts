/*
 * Copyright (c) 2016 Vivid Solutions.
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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

/**
 * An algorithm for computing a distance metric
 * which is an approximation to the Hausdorff Distance
 * based on a discretization of the input {@link Geometry}s.
 * The algorithm computes the Hausdorff distance restricted to discrete points
 * for one of the geometries.
 * The algorithm works on point and linear geometries only; 
 * areal geometries are treated as their linear boundary.
 * The points can be either the vertices of the geometries (the default), 
 * or the geometries with line segments densified by a given fraction.
 * The class can also determine two points of the geometries 
 * which are separated by the computed distance.
* <p>
 * This algorithm is an approximation to the standard Hausdorff distance.
 * Specifically, 
 * <blockquote>
 *    <i>for all geometries A, B:    DHD(A, B) &lt;= HD(A, B)</i>
 * </blockquote>
 * The approximation can be made as close as needed by densifying the input geometries.  
 * In the limit, this value will approach the true Hausdorff distance:
 * <blockquote>
 *    <i>DHD(A, B, densifyFactor) &rarr; HD(A, B) as densifyFactor &rarr; 0.0</i>
 * </blockquote>
 * The default approximation is exact or close enough for a large subset of useful cases.
 * Examples of these are:
 * <ul>
 * <li>computing distance between Linestrings that are roughly parallel to each other,
 * and roughly equal in length.  This occurs in matching linear networks.
 * <li>Testing similarity of geometries.
 * </ul>
 * An example where the default approximation is not close is:
 * <pre>
 *   A = LINESTRING (0 0, 100 0, 10 100, 10 100)
 *   B = LINESTRING (0 100, 0 10, 80 10)
 *   
 *   DHD(A, B) = 22.360679774997898
 *   HD(A, B) ~= 47.8
 * </pre>
 * The class can compute the oriented Hausdorff distance from A to B.
 * This computes the distance to the farthest point on A from B.
 * <blockquote>
 *   <i>OHD(A, B) = max<sub>a &isin; A</sub>( Distance(a, B) )</i>
 *   <br>
 *   with
 *   <br>
 *   <i>HD(A, B) = max( OHD(A, B), OHD(B, A) )</i>
 * </blockquote>
 * A use case is to test whether a geometry A lies completely within a given 
 * distance of another one B.
 * This is more efficient than testing whether A is covered by a buffer of B.
 * 
 * @see DiscreteFrechetDistance
 * 
 */
public class DiscreteHausdorffDistance
{
  /**
   * Computes the Hausdorff distance between two geometries.
   * 
   * @param g0 the first input
   * @param g1 the second input
   * @return the Hausdorff distance between g0 and g1
   */
  public static double distance(Geometry g0, Geometry g1)
  {
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    return dist.distance();
  }

  /**
   * Computes the Hausdorff distance between two geometries,
   * with each segment densified by the given fraction.
   * 
   * @param g0 the first input
   * @param g1 the second input
   * @param densifyFrac the densification fraction (in [0, 1])
   * @return the Hausdorff distance between g0 and g1
   */
  public static double distance(Geometry g0, Geometry g1, double densifyFrac)
  {
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    dist.setDensifyFraction(densifyFrac);
    return dist.distance();
  }

  /**
   * Computes a line containing points indicating 
   * the Hausdorff distance between two geometries.
   * 
   * @param g0 the first input
   * @param g1 the second input
   * @return a 2-point line indicating the distance
   */
  public static LineString distanceLine(Geometry g0, Geometry g1)
  {
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    dist.distance();
    return g0.getFactory().createLineString(dist.getCoordinates());  
  }

  /**
   * Computes a line containing points indicating 
   * the Hausdorff distance between two geometries,
   * with each segment densified by the given fraction.
   * 
   * @param g0 the first input
   * @param g1 the second input
   * @param densifyFrac the densification fraction (in [0, 1])
   * @return a 2-point line indicating the distance
   */
  public static LineString distanceLine(Geometry g0, Geometry g1, double densifyFrac)
  {
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    dist.setDensifyFraction(densifyFrac);
    dist.distance();
    return g0.getFactory().createLineString(dist.getCoordinates());  
  }

  /**
   * Computes the oriented Hausdorff distance from one geometry to another.
   * 
   * @param g0 the first input
   * @param g1 the second input
   * @return the oriented Hausdorff distance from g0 to g1
   */
  public static double orientedDistance(Geometry g0, Geometry g1)
  {
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    return dist.orientedDistance();
  }

  /**
   * Computes the oriented Hausdorff distance from one geometry to another,
   * with each segment densified by the given fraction.
   * 
   * @param g0 the first input
   * @param g1 the second input
   * @param densifyFrac the densification fraction (in [0, 1])
   * @return the oriented Hausdorff distance from g0 to g1
   */
  public static double orientedDistance(Geometry g0, Geometry g1, double densifyFrac)
  {
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    dist.setDensifyFraction(densifyFrac);
    return dist.orientedDistance();
  }

  /**
   * Computes a line containing points indicating 
   * the computed oriented Hausdorff distance from one geometry to another.
   * 
   * @param g0 the first input
   * @param g1 the second input
   * @return a 2-point line indicating the distance
   */
  public static LineString orientedDistanceLine(Geometry g0, Geometry g1)
  {
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    dist.orientedDistance();
    return g0.getFactory().createLineString(dist.getCoordinates());  
  }

  /**
   * Computes a line containing points indicating 
   * the computed oriented Hausdorff distance from one geometry to another,
   * with each segment densified by the given fraction.
   *
   * @param g0 the first input
   * @param g1 the second input
   * @param densifyFrac the densification fraction (in [0, 1])
   * @return a 2-point line indicating the distance
   */
  public static LineString orientedDistanceLine(Geometry g0, Geometry g1, double densifyFrac)
  {
    DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    dist.setDensifyFraction(densifyFrac);
    dist.orientedDistance();
    return g0.getFactory().createLineString(dist.getCoordinates());  
  }

  private Geometry g0;
  private Geometry g1;
  private PointPairDistance ptDist = new PointPairDistance();
  
  /**
   * Value of 0.0 indicates that no densification should take place
   */
  private double densifyFrac = 0.0;

  public DiscreteHausdorffDistance(Geometry g0, Geometry g1)
  {
    this.g0 = g0;
    this.g1 = g1;
  }

  /**
   * Sets the fraction by which to densify each segment.
   * Each segment will be (virtually) split into a number of equal-length
   * subsegments, whose fraction of the total length is closest
   * to the given fraction.
   * 
   * @param densifyFrac a fraction in range (0, 1]
   */
  public void setDensifyFraction(double densifyFrac)
  {
    if (densifyFrac > 1.0 
        || densifyFrac <= 0.0)
      throw new IllegalArgumentException("Fraction is not in range (0.0 - 1.0]");
        
    this.densifyFrac = densifyFrac;
  }
  
  /** 
   * Computes the Hausdorff distance between A and B.
   * 
   * @return the Hausdorff distance
   */
  public double distance() 
  { 
    compute(g0, g1);
    return ptDist.getDistance(); 
  }

  /** 
   * Computes the oriented Hausdorff distance from A to B.
   * 
   * @return the oriented Hausdorff distance
   */
  public double orientedDistance() 
  { 
    computeOrientedDistance(g0, g1, ptDist);
    return ptDist.getDistance(); 
  }

  public Coordinate[] getCoordinates() { return ptDist.getCoordinates(); }

  private void compute(Geometry g0, Geometry g1)
  {
    computeOrientedDistance(g0, g1, ptDist);
    computeOrientedDistance(g1, g0, ptDist);
  }

  private void computeOrientedDistance(Geometry discreteGeom, Geometry geom, PointPairDistance ptDist)
  {
    MaxPointDistanceFilter distFilter = new MaxPointDistanceFilter(geom);
    discreteGeom.apply(distFilter);
    ptDist.setMaximum(distFilter.getMaxPointDistance());
    
    if (densifyFrac > 0) {
      MaxDensifiedByFractionDistanceFilter fracFilter = new MaxDensifiedByFractionDistanceFilter(geom, densifyFrac);
      discreteGeom.apply(fracFilter);
      ptDist.setMaximum(fracFilter.getMaxPointDistance());
      
    }
  }

  private static class MaxPointDistanceFilter
      implements CoordinateFilter
  {
    private PointPairDistance maxPtDist = new PointPairDistance();
    private PointPairDistance minPtDist = new PointPairDistance();
    private Geometry geom;

    public MaxPointDistanceFilter(Geometry geom)
    {
      this.geom = geom;
    }

    public void filter(Coordinate pt)
    {
      minPtDist.initialize();
      DistanceToPoint.computeDistance(geom, pt, minPtDist);
      maxPtDist.setMaximum(minPtDist);
    }

    public PointPairDistance getMaxPointDistance() { return maxPtDist; }
  }
  
  private static class MaxDensifiedByFractionDistanceFilter 
  implements CoordinateSequenceFilter 
  {
    private PointPairDistance maxPtDist = new PointPairDistance();
    private PointPairDistance minPtDist = new PointPairDistance();
    private Geometry geom;
    private int numSubSegs = 0;
  
    public MaxDensifiedByFractionDistanceFilter(Geometry geom, double fraction) {
      this.geom = geom;
      numSubSegs = (int) Math.rint(1.0/fraction);
    }
  
    public void filter(CoordinateSequence seq, int index) 
    {
      /**
       * This logic also handles skipping Point geometries
       */
      if (index == 0)
        return;
      
      Coordinate p0 = seq.getCoordinate(index - 1);
      Coordinate p1 = seq.getCoordinate(index);
      
      double delx = (p1.x - p0.x)/numSubSegs;
      double dely = (p1.y - p0.y)/numSubSegs;
  
      for (int i = 0; i < numSubSegs; i++) {
        double x = p0.x + i*delx;
        double y = p0.y + i*dely;
        Coordinate pt = new Coordinate(x, y);
        minPtDist.initialize();
        DistanceToPoint.computeDistance(geom, pt, minPtDist);
        maxPtDist.setMaximum(minPtDist);  
      }
      
      
    }
  
    public boolean isGeometryChanged() { return false; }
    
    public boolean isDone() { return false; }
    
    public PointPairDistance getMaxPointDistance() {
      return maxPtDist;
    }
  }

}
