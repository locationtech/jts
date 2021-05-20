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
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

/**
 * Constructs the Largest Empty Circle for a set
 * of obstacle geometries, up to a specified tolerance.
 * The obstacles are point and line geometries.
 * <p>
 * The Largest Empty Circle is the largest circle which
 * has its center in the convex hull of the obstacles (the <i>boundary</i>),
 * and whose interior does not intersect with any obstacle.
 * The circle center is the point in the interior of the boundary 
 * which has the farthest distance from the obstacles (up to tolerance).
 * The circle is determined by the center point
 * and a point lying on an obstacle indicating the circle radius.
 * <p>
 * The implementation uses a successive-approximation technique
 * over a grid of square cells covering the obstacles and boundary.
 * The grid is refined using a branch-and-bound algorithm. 
 * Point containment and distance are computed in a performant
 * way by using spatial indexes.
 * <p>
 * <h3>Future Enhancements</h3>
 * <ul>
 * <li>Support polygons as obstacles
 * <li>Support a client-defined boundary polygon
 * </ul>
 * 
 * @author Martin Davis
 * @see MaximumInscribedCircle
 * @see InteriorPoint
 * @see Centroid
 */
public class LargestEmptyCircle {

  /**
   * Computes the center point of the Largest Empty Circle 
   * within a set of obstacles, up to a given tolerance distance.
   * 
   * @param obstacles a geometry representing the obstacles (points and lines)
   * @param tolerance the distance tolerance for computing the center point
   * @return the center point of the Largest Empty Circle
   */
  public static Point getCenter(Geometry obstacles, double tolerance) {
    LargestEmptyCircle lec = new LargestEmptyCircle(obstacles, tolerance);
    return lec.getCenter();
  }

  /**
   * Computes a radius line of the Largest Empty Circle
   * within a set of obstacles, up to a given distance tolerance.
   * 
   * @param obstacles a geometry representing the obstacles (points and lines)
   * @param tolerance the distance tolerance for computing the center point
   * @return a line from the center of the circle to a point on the edge
   */
  public static LineString getRadiusLine(Geometry obstacles, double tolerance) {
    LargestEmptyCircle lec = new LargestEmptyCircle(obstacles, tolerance);
    return lec.getRadiusLine();
  }
  
  private Geometry obstacles;
  private double tolerance;

  private GeometryFactory factory;
  private Geometry boundary;
  private IndexedPointInAreaLocator ptLocater;
  private IndexedFacetDistance obstacleDistance;
  private IndexedFacetDistance boundaryDistance;
  private Cell farthestCell;
  
  private Cell centerCell = null;
  private Coordinate centerPt;
  private Point centerPoint = null;
  private Coordinate radiusPt;
  private Point radiusPoint = null;

  /**
   * Creates a new instance of a Largest Empty Circle construction.
   * 
   * @param obstacles a geometry representing the obstacles (points and lines)
   * @param tolerance the distance tolerance for computing the circle center point
   */
  public LargestEmptyCircle(Geometry obstacles, double tolerance) {
    if (obstacles.isEmpty()) {
      throw new IllegalArgumentException("Empty obstacles geometry is not supported");
    }
    
    this.obstacles = obstacles;
    this.factory = obstacles.getFactory();
    this.tolerance = tolerance;
    obstacleDistance = new IndexedFacetDistance( obstacles );
    setBoundary(obstacles);
  }

  /**
   * Sets the area boundary as the convex hull
   * of the obstacles.
   *
   * @param obstacles
   */
  private void setBoundary(Geometry obstacles) {
    // TODO: allow this to be set by client as arbitrary polygon
    this.boundary = obstacles.convexHull();
    // if boundary does not enclose an area cannot create a ptLocater
    if (boundary.getDimension() >= 2) {
      ptLocater = new IndexedPointInAreaLocator(boundary);
      boundaryDistance = new IndexedFacetDistance( boundary );
    }
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
    boolean isOutide = Location.EXTERIOR == ptLocater.locate(p.getCoordinate());
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
  
  private void compute() {
    // check if already computed
    if (centerCell != null) return;
    
    // if ptLocater is not present then result is degenerate (represented as zero-radius circle)
    if (ptLocater == null) {
      Coordinate pt = obstacles.getCoordinate();
      centerPt = pt.copy();
      centerPoint = factory.createPoint(pt);
      radiusPt = pt.copy();
      radiusPoint = factory.createPoint(pt);
      return;
    }
    
    // Priority queue of cells, ordered by decreasing distance from constraints
    PriorityQueue<Cell> cellQueue = new PriorityQueue<>();
    
    createInitialGrid(obstacles.getEnvelopeInternal(), cellQueue);

    // use the area centroid as the initial candidate center point
    farthestCell = createCentroidCell(obstacles);
    //int totalCells = cellQueue.size();

    /**
     * Carry out the branch-and-bound search
     * of the cell space
     */
    while (! cellQueue.isEmpty()) {
      // pick the cell with greatest distance from the queue
      Cell cell = cellQueue.remove();

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
   * Initializes the queue with a grid of cells covering 
   * the extent of the area.
   * 
   * @param env the area extent to cover
   * @param cellQueue the queue to initialize
   */
  private void createInitialGrid(Envelope env, PriorityQueue<Cell> cellQueue) {
    double minX = env.getMinX();
    double maxX = env.getMaxX();
    double minY = env.getMinY();
    double maxY = env.getMaxY();
    double width = env.getWidth();
    double height = env.getHeight();
    double cellSize = Math.min(width, height);
    double hSize = cellSize / 2.0;

    // compute initial grid of cells to cover area
    for (double x = minX; x < maxX; x += cellSize) {
      for (double y = minY; y < maxY; y += cellSize) {
        cellQueue.add(createCell(x + hSize, y + hSize, hSize));
      }
    }
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
     * A cell is greater if its maximum distance is larger.
     */
    public int compareTo(Cell o) {
      return (int) (o.maxDist - this.maxDist);
    }
  }

}
