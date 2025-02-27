/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm.construct;

import java.util.PriorityQueue;

import org.locationtech.jts.algorithm.Centroid;
import org.locationtech.jts.algorithm.InteriorPoint;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

/**
 * Constructs the Largest Empty Circle for a set
 * of obstacle geometries, up to a given accuracy distance tolerance.
 * The obstacles may be any combination of point, linear and polygonal geometries.
 * <p>
 * The Largest Empty Circle (LEC) is the largest circle 
 * whose interior does not intersect with any obstacle
 * and whose center lies within a polygonal boundary.
 * The circle center is the point in the interior of the boundary 
 * which has the farthest distance from the obstacles 
 * (up to the accuracy of the distance tolerance).
 * The circle itself is determined by the center point
 * and a point lying on an obstacle determining the circle radius.
 * <p>
 * The polygonal boundary may be supplied explicitly.
 * If it is not specified the convex hull of the obstacles is used as the boundary.
 * <p>
 * To compute an LEC which lies <i>wholly</i> within
 * a polygonal boundary, include the boundary of the polygon(s) as a linear obstacle.
 * <p>
 * The implementation uses a successive-approximation technique
 * over a grid of square cells covering the obstacles and boundary.
 * The grid is refined using a branch-and-bound algorithm. 
 * Point containment and distance are computed in a performant
 * way by using spatial indexes.
 * 
 * @author Martin Davis
 * 
 * @see MaximumInscribedCircle
 * @see InteriorPoint
 * @see Centroid
 */
public class LargestEmptyCircle {

  /**
   * Computes the center point of the Largest Empty Circle 
   * interior-disjoint to a set of obstacles, 
   * with accuracy to a given tolerance distance.
   * The obstacles may be any collection of points, lines and polygons.
   * The center of the LEC lies within the convex hull of the obstacles.
   * 
   * @param obstacles a geometry representing the obstacles
   * @param tolerance the distance tolerance for computing the center point
   * @return the center point of the Largest Empty Circle
   */
  public static Point getCenter(Geometry obstacles, double tolerance) {
    return getCenter(obstacles, null, tolerance);
  }

  /**
   * Computes the center point of the Largest Empty Circle 
   * interior-disjoint to a set of obstacles and within a polygonal boundary, 
   * with accuracy to a given tolerance distance.
   * The obstacles may be any collection of points, lines and polygons.
   * The center of the LEC lies within the given boundary.
   * 
   * @param obstacles a geometry representing the obstacles
   * @param boundary a polygonal geometry to contain the LEC center
   * @param tolerance the distance tolerance for computing the center point
   * @return the center point of the Largest Empty Circle
   */
  public static Point getCenter(Geometry obstacles, Geometry boundary, double tolerance) {
    LargestEmptyCircle lec = new LargestEmptyCircle(obstacles, boundary, tolerance);
    return lec.getCenter();
  }
  
  /**
   * Computes a radius line of the Largest Empty Circle
   * interior-disjoint to a set of obstacles, 
   * with accuracy to a given tolerance distance.
   * The obstacles may be any collection of points, lines and polygons.
   * The center of the LEC lies within the convex hull of the obstacles.
   * 
   * @param obstacles a geometry representing the obstacles
   * @param tolerance the distance tolerance for computing the center point
   * @return a line from the center of the circle to a point on the edge
   */
  public static LineString getRadiusLine(Geometry obstacles, double tolerance) {
    return getRadiusLine(obstacles, null, tolerance);
  }
  
  /**
   * Computes a radius line of the Largest Empty Circle
   * interior-disjoint to a set of obstacles and within a polygonal boundary, 
   * with accuracy to a given tolerance distance.
   * The obstacles may be any collection of points, lines and polygons.
   * The center of the LEC lies within the given boundary.
   * 
   * @param obstacles a geometry representing the obstacles
   * @param boundary a polygonal geometry to contain the LEC center
   * @param tolerance the distance tolerance for computing the center point
   * @return a line from the center of the circle to a point on the edge
   */
  public static LineString getRadiusLine(Geometry obstacles, Geometry boundary, double tolerance) {
    LargestEmptyCircle lec = new LargestEmptyCircle(obstacles, boundary, tolerance);
    return lec.getRadiusLine();
  }
  
  private Geometry obstacles;
  private Geometry boundary;
  private double tolerance;

  private GeometryFactory factory;
  private IndexedDistanceToPoint obstacleDistance;
  private IndexedPointInAreaLocator boundaryPtLocater;
  private IndexedFacetDistance boundaryDistance;
  private Envelope gridEnv;
  private Cell farthestCell;
  
  private Cell centerCell = null;
  private Coordinate centerPt;
  private Point centerPoint = null;
  private Coordinate radiusPt;
  private Point radiusPoint = null;
  private Geometry bounds;

  /**
   * Creates a new instance of a Largest Empty Circle construction,
   * interior-disjoint to a set of obstacle geometries 
   * and having its center within a polygonal boundary.
   * The obstacles may be any collection of points, lines and polygons.
   * If the boundary is null or empty the convex hull
   * of the obstacles is used as the boundary.
   * 
   * @param obstacles a non-empty geometry representing the obstacles
   * @param boundary a polygonal geometry (may be null or empty)
   * @param tolerance a distance tolerance for computing the circle center point (a positive value)
   */
  public LargestEmptyCircle(Geometry obstacles, Geometry boundary, double tolerance) {
    if (obstacles == null || obstacles.isEmpty()) {
      throw new IllegalArgumentException("Obstacles geometry is empty or null");
    }
    if (boundary != null && ! (boundary instanceof Polygonal)) {
      throw new IllegalArgumentException("Boundary must be polygonal");
    }
    if (tolerance <= 0) {
      throw new IllegalArgumentException("Accuracy tolerance is non-positive: " + tolerance);
    }
    this.obstacles = obstacles;
    this.boundary = boundary;
    this.factory = obstacles.getFactory();
    this.tolerance = tolerance;
    obstacleDistance = new IndexedDistanceToPoint( obstacles );
  }

  /**
   * Gets the center point of the Largest Empty Circle
   * (up to the tolerance distance).
   * 
   * @return the center point of the Largest Empty Circle
   */
  public Point getCenter() {
    compute();
    return centerPoint;
  }
  
  /**
   * Gets a point defining the radius of the Largest Empty Circle.
   * This is a point on the obstacles which is 
   * nearest to the computed center of the Largest Empty Circle.
   * The line segment from the center to this point
   * is a radius of the constructed circle, and this point
   * lies on the boundary of the circle.
   * 
   * @return a point defining the radius of the Largest Empty Circle
   */
  public Point getRadiusPoint() {
    compute();
    return radiusPoint;
  }
  
  /**
   * Gets a line representing a radius of the Largest Empty Circle.
   * 
   * @return a line from the center of the circle to a point on the edge
   */
  public LineString getRadiusLine() {
    compute();
    LineString radiusLine = factory.createLineString(
        new Coordinate[] { centerPt.copy(), radiusPt.copy() });
    return radiusLine;
  }
  
  /**
   * Computes the signed distance from a point to the constraints
   * (obstacles and boundary).
   * Points outside the boundary polygon are assigned a negative distance. 
   * Their containing cells will be last in the priority queue
   * (but will still end up being tested since they may be refined).
   * 
   * @param p the point to compute the distance for
   * @return the signed distance to the constraints (negative indicates outside the boundary)
   */
  private double distanceToConstraints(Point p) {
    boolean isOutide = Location.EXTERIOR == boundaryPtLocater.locate(p.getCoordinate());
    if (isOutide) {
      double boundaryDist = boundaryDistance.distance(p);
      return -boundaryDist;
    }
    double dist = obstacleDistance.distance(p);
    return dist;
  }

  private double distanceToConstraints(double x, double y) {
    Coordinate coord = new Coordinate(x, y);
    Point pt = factory.createPoint(coord);
    return distanceToConstraints(pt);
  }
  
  private void initBoundary() {
    bounds = this.boundary;
    if (bounds == null || bounds.isEmpty()) {
      bounds = obstacles.convexHull();
    }
    //-- the centre point must be in the extent of the boundary
    gridEnv = bounds.getEnvelopeInternal();
    // if bounds does not enclose an area cannot create a ptLocater
    if (bounds.getDimension() >= 2) {
      boundaryPtLocater = new IndexedPointInAreaLocator( bounds );
      boundaryDistance = new IndexedFacetDistance( bounds );
    }
  }
  
  private void compute() {
    initBoundary();
    
    // check if already computed
    if (centerCell != null) return;
    
    // if boundaryPtLocater is not present then result is degenerate (represented as zero-radius circle)
    if (boundaryPtLocater == null) {
      Coordinate pt = obstacles.getCoordinate();
      centerPt = pt.copy();
      centerPoint = factory.createPoint(pt);
      radiusPt = pt.copy();
      radiusPoint = factory.createPoint(pt);
      return;
    }
    
    // Priority queue of cells, ordered by decreasing distance from constraints
    PriorityQueue<Cell> cellQueue = new PriorityQueue<>();
    
    //-- grid covers extent of obstacles and boundary (if any)
    createInitialGrid(gridEnv, cellQueue);

    // use the area centroid as the initial candidate center point
    farthestCell = createCentroidCell(obstacles);
    //int totalCells = cellQueue.size();

    /**
     * Carry out the branch-and-bound search
     * of the cell space
     */
    long maxIter = MaximumInscribedCircle.computeMaximumIterations(bounds, tolerance);
    long iter = 0;
    while (! cellQueue.isEmpty() && iter < maxIter) {
      iter++;
      // pick the cell with greatest distance from the queue
      Cell cell = cellQueue.remove();
      //System.out.println(iter + "] Dist: " + cell.getDistance() + " Max D: " + cell.getMaxDistance() + " size: " + cell.getHSide());

      // update the center cell if the candidate is further from the constraints
      if (cell.getDistance() > farthestCell.getDistance()) {
        farthestCell = cell;
      }
      
      /**
       * If this cell may contain a better approximation to the center 
       * of the empty circle, then refine it (partition into subcells 
       * which are added into the queue for further processing).
       * Otherwise the cell is pruned (not investigated further),
       * since no point in it can be further than the current farthest distance.
       */
      if (mayContainCircleCenter(cell)) {
        // split the cell into four sub-cells
        double h2 = cell.getHSide() / 2;
        cellQueue.add( createCell( cell.getX() - h2, cell.getY() - h2, h2));
        cellQueue.add( createCell( cell.getX() + h2, cell.getY() - h2, h2));
        cellQueue.add( createCell( cell.getX() - h2, cell.getY() + h2, h2));
        cellQueue.add( createCell( cell.getX() + h2, cell.getY() + h2, h2));
        //totalCells += 4;
      }
    }
    // the farthest cell is the best approximation to the LEC center
    centerCell = farthestCell;
    // compute center point
    centerPt = new Coordinate(centerCell.getX(), centerCell.getY());
    centerPoint = factory.createPoint(centerPt);
    // compute radius point
    Coordinate[] nearestPts = obstacleDistance.nearestPoints(centerPoint);
    radiusPt = nearestPts[0].copy();
    radiusPoint = factory.createPoint(radiusPt);
  }

  /**
   * Tests whether a cell may contain the circle center,
   * and thus should be refined (split into subcells 
   * to be investigated further.)
   * 
   * @param cell the cell to test
   * @return true if the cell might contain the circle center
   */
  private boolean mayContainCircleCenter(Cell cell) {
    /**
     * Every point in the cell lies outside the boundary,
     * so they cannot be the center point
     */
    if (cell.isFullyOutside())
      return false;
    
    /**
     * The cell is outside, but overlaps the boundary
     * so it may contain a point which should be checked.
     * This is only the case if the potential overlap distance 
     * is larger than the tolerance.
     */
    if (cell.isOutside()) {
      boolean isOverlapSignificant = cell.getMaxDistance() > tolerance;
      return isOverlapSignificant;
    }
    
    /**
     * Cell is inside the boundary. It may contain the center
     * if the maximum possible distance is greater than the current distance
     * (up to tolerance).
     */
    double potentialIncrease = cell.getMaxDistance() - farthestCell.getDistance();
    return potentialIncrease > tolerance;
  }

  /**
   * Initializes the queue with a cell covering 
   * the extent of the area.
   * 
   * @param env the area extent to cover
   * @param cellQueue the queue to initialize
   */
  private void createInitialGrid(Envelope env, PriorityQueue<Cell> cellQueue) {
    double cellSize = Math.max(env.getWidth(), env.getHeight());
    double hSide = cellSize / 2.0;

    // Check for flat collapsed input and if so short-circuit
    // Result will just be centroid
    if (cellSize == 0) return;
    
    Coordinate centre = env.centre();
    cellQueue.add(createCell(centre.x, centre.y, hSide));   
  }

  private Cell createCell(double x, double y, double h) {
    return new Cell(x, y, h, distanceToConstraints(x, y));
  }

  // create a cell centered on area centroid
  private Cell createCentroidCell(Geometry geom) {
    Point p = geom.getCentroid();
    return new Cell(p.getX(), p.getY(), 0, distanceToConstraints(p));
  }

  /**
   * A square grid cell centered on a given point 
   * with a given side half-length, 
   * and having a given distance from the center point to the constraints.
   * The maximum possible distance from any point in the cell to the
   * constraints can be computed.
   * This is used as the ordering and upper-bound function in
   * the branch-and-bound algorithm. 
   */
  private static class Cell implements Comparable<Cell> {

    private static final double SQRT2 = 1.4142135623730951;

    private double x;
    private double y;
    private double hSide;
    private double distance;
    private double maxDist;

    Cell(double x, double y, double hSide, double distanceToConstraints) {
      this.x = x; // cell center x
      this.y = y; // cell center y
      this.hSide = hSide; // half the cell size

      // the distance from cell center to constraints
      distance = distanceToConstraints;

      /**
       * The maximum possible distance to the constraints for points in this cell
       * is the center distance plus the radius (half the diagonal length).
       */
      this.maxDist = distance + hSide * SQRT2;
    }

    public boolean isFullyOutside() {
      return getMaxDistance() < 0;
    }

    public boolean isOutside() {
      return distance < 0;
    }

    public double getMaxDistance() {
      return maxDist;
    }

    public double getDistance() {
      return distance;
    }

    public double getHSide() {
      return hSide;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }
    
    /**
     * For maximum efficieny sort the PriorityQueue with largest maxDistance at front.
     * Since Java PQ sorts least-first, need to invert the comparison
     */
    @Override
    public int compareTo(Cell o) {
      return -Double.compare(maxDist, o.maxDist);
    }
  }

}
