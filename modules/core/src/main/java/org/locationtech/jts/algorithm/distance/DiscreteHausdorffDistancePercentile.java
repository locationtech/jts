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

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * An algorithm for computing a variation of the standard Hausdorff distance
 * that is more robust to outliers. The algorithm calculates the n-th percentile
 * of the distances between corresponding geometries, rather than the maximum
 * distance (Hausdorff distance). The algorithm ignores a specified percentage
 * of the highest distances, treating the furthest points as outliers.
 * <p>For example:
 * <br>for percentile = 0.95: the 5% of furthest points are ignored
 * <br>for percentile = 1.0: The calculated distance (HD100) is equal to the
 * standard Hausdorff Distance.<br>
 * for percentile = 0.0: The calculated distance (HD0) is the shortest distance between the geometries.
 * <p> The algorithm is an approximation based on the discretization of the input
 * {@link Geometry}. The calculated distances are restricted to discrete points for one
 * of the geometries. These points can be: vertices of the geometries (default) only,
 * or the geometries densified by a given offset.
 * <p>The offset is a minimum distance in geometry units for which densification points
 * are added to the geometry's segments. The number of points added to each of segments is
 * given by formula:
 ** <blockquote>
 *    <i>segNbOfPoints = (int) Math.floor(segmentLength / offset)</i>
 * </blockquote>
 * Therefore, the distance between densification points is given by:
 * <blockquote>
 *    <i>d = segmentLength / segNbOfPoints</i>
 * </blockquote>
 * The smaller the offset is, the more equal distribution of the densification points
 * accross the whole geometry and therefore the better approximation of the real value
 * of percentile Hausdorff distance.
 *
 * @see DiscreteHausdorffDistance
 * 
 */
public class DiscreteHausdorffDistancePercentile
{

  /**
   * Computes the percentile Hausdorff distance between two geometries.
   *
   * @param g0 the first input
   * @param g1 the second input
   * @param percentile the percentile level (in [0, 1])
   * @return the percentile Hausdorff distance between g0 and g1
   */
  public static double distance(Geometry g0, Geometry g1, double percentile)
  {
    return distance(g0, g1, percentile, 0.0);
  }

  /**
   * Computes the percentile Hausdorff distance between two geometries,
   * with each segment densified by the given offset.
   *
   * @param g0 the first input
   * @param g1 the second input
   * @param percentile the percentile level (in [0, 1])
   * @param densifyOffset the distance between densification points
   * @return the percentile Hausdorff distance between g0 and g1
   */
  public static double distance(Geometry g0, Geometry g1, double percentile, double densifyOffset)
  {
    DiscreteHausdorffDistancePercentile dist = new DiscreteHausdorffDistancePercentile(g0, g1, percentile);
    dist.setDensifyOffset(densifyOffset);
    return dist.distance();
  }

  /**
   * Computes a line containing points indicating
   * the percentile Hausdorff distance between two geometries.
   *
   * @param g0 the first input
   * @param g1 the second input
   * @param percentile the percentile level (in [0, 1])
   * @return a 2-point line indicating the distance
   */
  public static LineString distanceLine(Geometry g0, Geometry g1, double percentile)
  {
    return distanceLine(g0, g1, percentile, 0.0);
  }

  /**
   * Computes a line containing points indicating
   * the percentile Hausdorff distance between two geometries,
   * with each segment densified by the given offset.
   *
   * @param g0 the first input
   * @param g1 the second input
   * @param percentile the percentile level (in [0, 1])
   * @param densifyOffset the distance between densification points
   * @return a 2-point line indicating the distance
   */
  public static LineString distanceLine(Geometry g0, Geometry g1, double percentile, double densifyOffset)
  {
    DiscreteHausdorffDistancePercentile dist = new DiscreteHausdorffDistancePercentile(g0, g1, percentile);
    dist.setDensifyOffset(densifyOffset);
    dist.distance();
    return g0.getFactory().createLineString(dist.getCoordinates());
  }

  /**
   * Computes the oriented Hausdorff distance from one geometry to another,
   * with each segment densified by the given fraction.
   *
   * @param g0 the first input
   * @param g1 the second input
   * @param percentile the percentile level (in [0, 1])
   * @return the oriented Hausdorff distance from g0 to g1
   */
  public static double orientedDistance(Geometry g0, Geometry g1, double percentile)
  {
    return orientedDistance(g0, g1, percentile, 0.0);
  }

  /**
   * Computes the oriented Hausdorff distance from one geometry to another,
   * with each segment densified by the given fraction.
   *
   * @param g0 the first input
   * @param g1 the second input
   * @param percentile the percentile level (in [0, 1])
   * @param densifyOffset the distance between densification points
   * @return the oriented Hausdorff distance from g0 to g1
   */
  public static double orientedDistance(Geometry g0, Geometry g1, double percentile, double densifyOffset)
  {
    DiscreteHausdorffDistancePercentile dist = new DiscreteHausdorffDistancePercentile(g0, g1, percentile);
    dist.setDensifyOffset(densifyOffset);
    return dist.orientedDistance(densifyOffset);
  }

  /**
   * Computes a line containing points indicating
   * the computed oriented Hausdorff distance from one geometry to another.
   *
   * @param g0 the first input
   * @param g1 the second input
   * @param percentile the percentile level (in [0, 1])
   * @return a 2-point line indicating the distance
   */
  public static LineString orientedDistanceLine(Geometry g0, Geometry g1, double percentile)
  {
    return orientedDistanceLine(g0, g1, percentile, 0.0);
  }

  /**
   * Computes a line containing points indicating
   * the computed oriented Hausdorff distance from one geometry to another,
   * with each segment densified by the given offset.
   *
   * @param g0 the first input
   * @param g1 the second input
   * @param percentile the percentile level (in [0, 1])
   * @param densifyOffset the distance between densification points
   * @return a 2-point line indicating the distance
   */
  public static LineString orientedDistanceLine(Geometry g0, Geometry g1, double percentile, double densifyOffset)
  {
    DiscreteHausdorffDistancePercentile dist = new DiscreteHausdorffDistancePercentile(g0, g1, percentile);
    dist.orientedDistance(densifyOffset);
    return g0.getFactory().createLineString(dist.getCoordinates());
  }

  private Geometry g0;
  private Geometry g1;
  private PointPairDistance ptDistPerc = new PointPairDistance();
  private double percentile;
  private int nbOfPoints = 0;

  /**
   * Value of 0.0 indicates that no densification should take place
   */
  private double densifyOffset = 0.0;

  public DiscreteHausdorffDistancePercentile(Geometry g0, Geometry g1, double percentile)
  {
    this.g0 = g0;
    this.g1 = g1;
    setPercentile(percentile);
  }

  /**
   * Sets the percentile level.
   * Each segment will be (virtually) split into a number of equal-length
   * subsegments, whose fraction of the total length is closest
   * to the given fraction.
   *
   * @param percentile a value in range (0, 1]
   */
  public void setPercentile(double percentile)
  {
    if (percentile > 1.0
            || percentile < 0.0)
      throw new IllegalArgumentException("Percentile is not in range [0.0 - 1.0]");

    this.percentile = percentile;
  }

  /**
   * Sets the minimum offset by which each segment is densified.
   * Each segment will be (virtually) split into a number of equal-length
   * subsegments. For each segment the number of subsegments is given by:
   * <blockquote>
   * <i>numSubSegs = (int) Math.floor(segmentLength / offset)</i>
   * </blockquote>
   *
   * The final distance between densification points for each of the segments is
   * calculated by a formula:
   * <blockquote>
   * <i>d = segmentLength / numSubSegs</i>
   * </blockquote>
   *
   * Note that:
   * <blockquote>
   * <i>d >= densifyOffset</i>
   * </blockquote>
   *
   * @param densifyOffset the minimum distance between the densification points
   */
  private void setDensifyOffset(double densifyOffset) {
    if (densifyOffset < 0.0)
      throw new IllegalArgumentException("Offset cannot be negative");
    this.densifyOffset = densifyOffset;
  }

  /**
   * Computes the percentile Hausdorff distance between A and B.
   * @param densifyOffset the distance between densification points
   * @return the percentile Hausdorff distance
   */
  public double distance(double densifyOffset)
  {
    setDensifyOffset(densifyOffset);
    return distance();
  }

  /**
   * Computes the percentile Hausdorff distance between A and B.
   *
   * @return the percentile Hausdorff distance
   */
  public double distance()
  {
    compute(g0, g1);
    return ptDistPerc.getDistance();
  }

  /**
   * Computes the oriented percentile Hausdorff distance from A to B.
   * @param densifyOffset the distance between densification points
   * @return the oriented Hausdorff distance
   */
  public double orientedDistance(double densifyOffset)
  {
    setDensifyOffset(densifyOffset);
    return orientedDistance();
  }

  /**
   * Computes the oriented percentile Hausdorff distance from A to B.
   * @return the oriented Hausdorff distance
   */
  public double orientedDistance()
  {
    PriorityQueue<PointPairDistance> percentilePointDistancesPQ =
            new PriorityQueue<>(Comparator.comparingDouble(PointPairDistance::getDistance));
    int maxSize = maxPriorityQueueSize(g0);
    nbOfPoints = 0;
    computeOrientedDistance(g0, g1, percentilePointDistancesPQ, maxSize);
    findPercentileDistance(percentilePointDistancesPQ);
    return ptDistPerc.getDistance();
  }

  public Coordinate[] getCoordinates() {
    return ptDistPerc.getCoordinates();
  }

  private void compute(Geometry g0, Geometry g1)
  {
    PriorityQueue<PointPairDistance> percentilePointDistancesPQ =
            new PriorityQueue<>(Comparator.comparingDouble(PointPairDistance::getDistance));
    int maxSize = maxPriorityQueueSize(g0, g1);
    nbOfPoints = 0;
    computeOrientedDistance(g0, g1, percentilePointDistancesPQ, maxSize);
    computeOrientedDistance(g1, g0, percentilePointDistancesPQ, maxSize);
    findPercentileDistance(percentilePointDistancesPQ);
  }

  private int maxPriorityQueueSize(Geometry g0) {
    return maxPriorityQueueSize(g0, g0.getFactory().createPoint(g0.getCoordinate()));
  }

  private int maxPriorityQueueSize(Geometry g0, Geometry g1) {
    int maxNbOfPoints = g0.getNumPoints() + g1.getNumPoints();
    if (this.densifyOffset > 0.0){
      int numPointsLength = (int) Math.ceil(g0.getLength() / this.densifyOffset) + 1
              + (int) Math.ceil(g1.getLength() / this.densifyOffset) + 1;
      maxNbOfPoints = Math.max(maxNbOfPoints, numPointsLength);
    }
    return (int) Math.ceil(maxNbOfPoints * (1 - percentile)) + 1;
  }

  private void computeOrientedDistance(Geometry discreteGeom, Geometry geom,
                                       PriorityQueue<PointPairDistance> percentilePointDistancesPQ,
                                       int maxSize)
  {
    PointDistanceFilter distFilter = new PointDistanceFilter(geom, maxSize);
    distFilter.setPtDistsPQ(percentilePointDistancesPQ);
    discreteGeom.apply(distFilter);
    nbOfPoints += distFilter.getNbOfPoints();

    if (densifyOffset > 0) {
      DensificationPointsFilter fracFilter = new DensificationPointsFilter(geom, densifyOffset, maxSize);
      fracFilter.setPtDistsPQ(percentilePointDistancesPQ);
      discreteGeom.apply(fracFilter);
      nbOfPoints += fracFilter.getNbOfPoints();
    }
  }

  private void findPercentileDistance(PriorityQueue<PointPairDistance> percentilePointDistancesPQ) {
    int index = (int) Math.ceil(nbOfPoints * percentile) - 1;
    index = Math.max(index, 0);
    int newSize = nbOfPoints - index;
    while (percentilePointDistancesPQ.size() > newSize) {
      percentilePointDistancesPQ.poll();
    }
    ptDistPerc = percentilePointDistancesPQ.poll();
  }

  private static class PointDistanceFilter
          implements CoordinateFilter
  {
    private PointPairDistance minPtDist = new PointPairDistance();
    private PriorityQueue<PointPairDistance> ptDistsPQ =
            new PriorityQueue<>(Comparator.comparingDouble(PointPairDistance::getDistance));
    private int maxSize;
    private int nbOfPoints = 0;
    private Geometry geom;

    public PointDistanceFilter(Geometry geom, int maxSize)
    {
      this.geom = geom;
      this.maxSize = maxSize;
    }

    public void filter(Coordinate pt)
    {
      nbOfPoints++;
      minPtDist.initialize();
      DistanceToPoint.computeDistance(geom, pt, minPtDist);
      PointPairDistance pointPairDistance = new PointPairDistance();
      pointPairDistance.setMaximum(minPtDist);
      ptDistsPQ.add(pointPairDistance);
      if (ptDistsPQ.size() > maxSize){
        ptDistsPQ.poll();
      }
    }

    public int getNbOfPoints() {
      return nbOfPoints;
    }

    public void setPtDistsPQ(PriorityQueue<PointPairDistance> ptDistsPQ) {
      this.ptDistsPQ = ptDistsPQ;
    }
  }

  private static class DensificationPointsFilter
          implements CoordinateSequenceFilter
  {
    private PointPairDistance minPtDist = new PointPairDistance();
    private PriorityQueue<PointPairDistance> ptDistsPQ =
            new PriorityQueue<>(Comparator.comparingDouble(PointPairDistance::getDistance));
    private int maxSize;
    private int nbOfPoints = 0;
    private Geometry geom;
    private double offset;

    public DensificationPointsFilter(Geometry geom, double offset, int maxSize) {
      this.geom = geom;
      this.offset = offset;
      this.maxSize = maxSize;
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

      int numSubSegs = (int) Math.floor(p0.distance(p1) / offset);

      double delx = (p1.x - p0.x)/numSubSegs;
      double dely = (p1.y - p0.y)/numSubSegs;

      for (int i = 1; i < numSubSegs; i++) {
        nbOfPoints++;
        double x = p0.x + i*delx;
        double y = p0.y + i*dely;
        Coordinate pt = new Coordinate(x, y);
        minPtDist.initialize();
        DistanceToPoint.computeDistance(geom, pt, minPtDist);
        PointPairDistance pointPairDistance = new PointPairDistance();
        pointPairDistance.setMaximum(minPtDist);
        ptDistsPQ.add(pointPairDistance);
        if (ptDistsPQ.size() > maxSize){
          ptDistsPQ.poll();
        }
      }
    }

    public int getNbOfPoints() {
      return nbOfPoints;
    }

    public void setPtDistsPQ(PriorityQueue<PointPairDistance> ptDistsPQ) {
      this.ptDistsPQ = ptDistsPQ;
    }

    public boolean isGeometryChanged() { return false; }

    public boolean isDone() { return false; }
  }
}
