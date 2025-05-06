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
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

/**
 * Constructs the Maximum Inscribed Circle for a 
 * polygonal {@link Geometry}, up to a specified tolerance
 * (which can be specified or determined automatically).
 * The Maximum Inscribed Circle is determined by a point in the interior of the area 
 * which has the farthest distance from the area boundary,
 * along with a boundary point at that distance.
 * <p>
 * In the context of geography the center of the Maximum Inscribed Circle 
 * is known as the <b>Pole of Inaccessibility</b>.
 * A cartographic use case is to determine a suitable point 
 * to place a map label within a polygon.
 * <p>
 * The radius length of the Maximum Inscribed Circle is a 
 * measure of how "narrow" a polygon is. It is the 
 * distance at which the negative buffer becomes empty.
 * The class supports testing whether a polygon is "narrower"
 * than a specified distance via 
 * {@link #isRadiusWithin(Geometry, double)} or
 * {@link #isRadiusWithin(double)}.
 * Testing for the maximum radius is generally much faster
 * than computing the actual radius value, since short-circuiting
 * is used to limit the approximation iterations.
 * <p>
 * The class supports polygons with holes and multipolygons.
 * <p>
 * For small polygons (currently triangles and convex quadrilaterals)
 * the MIC is determined exactly.
 * For other polygons the implementation uses a successive-approximation technique
 * over a grid of square cells covering the area geometry.
 * The grid is refined using a branch-and-bound algorithm. 
 * Point containment and distance are computed in a performant
 * way by using spatial indexes.
 *
 * <h3>Future Enhancements</h3>
 * <ul>
 * <li>Support a polygonal constraint on placement of center point,
 *     for example to produce circle-packing constructions,
 *     or support multiple labels.
 * </ul>
 * 
 * @author Martin Davis
 * 
 * @see LargestEmptyCircle
 * @see InteriorPoint
 * @see Centroid
 *
 */
public class MaximumInscribedCircle {

  /**
   * Computes the center point of the Maximum Inscribed Circle
   * of a polygonal geometry.
   * 
   * @param polygonal a polygonal geometry
   * @return the center point of the maximum inscribed circle
   */
  public static Point getCenter(Geometry polygonal) {
    MaximumInscribedCircle mic = new MaximumInscribedCircle(polygonal);
    return mic.getCenter();
  }

  /**
   * Computes the center point of the Maximum Inscribed Circle
   * of a polygonal geometry, up to a given tolerance distance.
   * 
   * @param polygonal a polygonal geometry
   * @param tolerance the distance tolerance for computing the center point
   * @return the center point of the maximum inscribed circle
   */
  public static Point getCenter(Geometry polygonal, double tolerance) {
    MaximumInscribedCircle mic = new MaximumInscribedCircle(polygonal, tolerance);
    return mic.getCenter();
  }

  /**
   * Computes a radius line of the Maximum Inscribed Circle
   * of a polygonal geometry.
   * 
   * @param polygonal a polygonal geometry
   * @return a 2-point line from the center to a point on the circle
   */
  public static LineString getRadiusLine(Geometry polygonal) {
    MaximumInscribedCircle mic = new MaximumInscribedCircle(polygonal);
    return mic.getRadiusLine();
  }
  
  /**
   * Computes a radius line of the Maximum Inscribed Circle
   * of a polygonal geometry, up to a given tolerance distance.
   * 
   * @param polygonal a polygonal geometry
   * @param tolerance the distance tolerance for computing the center point
   * @return a 2-point line from the center to a point on the circle
   */
  public static LineString getRadiusLine(Geometry polygonal, double tolerance) {
    MaximumInscribedCircle mic = new MaximumInscribedCircle(polygonal, tolerance);
    return mic.getRadiusLine();
  }
  
  /**
   * Tests if the radius of the maximum inscribed circle 
   * is no longer than the specified distance.
   * The approximation tolerance is determined automatically
   * as a fraction of the maxRadius value.
   * 
   * @param polygonal a polygonal geometry
   * @param maxRadius the radius value to test
   * @return true if the max in-circle radius is no longer than the max radius
   */
  public static boolean isRadiusWithin(Geometry polygonal, double maxRadius) {
    MaximumInscribedCircle mic = new MaximumInscribedCircle(polygonal, -1);
    return mic.isRadiusWithin(maxRadius);
  }
  
  private Geometry inputGeom;
  private double tolerance;

  private GeometryFactory factory;
  private IndexedPointInAreaLocator ptLocater;
  private IndexedFacetDistance indexedDistance;
  private Cell centerCell = null;
  private Coordinate centerPt = null;
  private Coordinate radiusPt;
  private Point centerPoint;
  private Point radiusPoint;
  private double maximumRadius = -1;;

  /**
   * Creates a new instance of a Maximum Inscribed Circle computation.
   * 
   * @param polygonal an areal geometry
   * @throws IllegalArgumentException if the tolerance is negative, or the input geometry is non-polygonal or empty.
   */
  public MaximumInscribedCircle(Geometry polygonal) {
    this(polygonal, 0.0);
  }

  /**
   * Creates a new instance of a Maximum Inscribed Circle computation,
   * with an approximation tolerance distance.
   * A zero tolerance aut0matically determines an approximation tolerance.
   * 
   * @param polygonal an areal geometry
   * @param tolerance the distance tolerance for computing the centre point (must be non-negative)
   * @throws IllegalArgumentException if the tolerance is negative, or the input geometry is non-polygonal or empty.
   */
  public MaximumInscribedCircle(Geometry polygonal, double tolerance) {
    if (! (polygonal instanceof Polygon || polygonal instanceof MultiPolygon)) {
      throw new IllegalArgumentException("Input must be a Polygon or MultiPolygon");
    }
    if (polygonal.isEmpty()) {
      throw new IllegalArgumentException("Empty input is not supported");
    }
    
    this.inputGeom = polygonal;
    this.factory = polygonal.getFactory();
    this.tolerance = tolerance;
  }

  //-- used for isRadiusWithin
  private static final double MAX_RADIUS_FRACTION = 0.0001;
  
  /**
   * Tests if the radius of the maximum inscribed circle 
   * is no longer than the specified distance.
   * This method determines the distance tolerance automatically
   * as a fraction of the maxRadius value.
   * After this method is called the center and radius
   * points provide locations demonstrating where
   * the radius exceeds the specified maximum.
   * 
   * @param maxRadius the (non-negative) radius value to test
   * @return true if the max in-circle radius is no longer than the max radius
   */
  public boolean isRadiusWithin(double maxRadius) {
    if (maxRadius < 0) {
      throw new IllegalArgumentException("Radius length must be non-negative");
    }
    //-- handle 0 corner case, to provide maximum domain
    if (maxRadius == 0) {
      return false;
    }
    maximumRadius = maxRadius;
    
    /**
     * Check if envelope dimension is smaller than diameter
     */
    Envelope env = inputGeom.getEnvelopeInternal();
    double maxDiam = 2 * maximumRadius;
    if (env.getWidth() < maxDiam || env.getHeight() < maxDiam) {
      return true;
    }
    
    tolerance = maxRadius * MAX_RADIUS_FRACTION;
    compute();
    double radius = centerPt.distance(radiusPt);
    return radius <= maximumRadius;
  }
  
  /**
   * Gets the center point of the maximum inscribed circle
   * (up to the tolerance distance).
   * 
   * @return the center point of the maximum inscribed circle
   */
  public Point getCenter() {
    compute();
    return centerPoint;
  }
  
  /**
   * Gets a point defining the radius of the Maximum Inscribed Circle.
   * This is a point on the boundary which is 
   * nearest to the computed center of the Maximum Inscribed Circle.
   * The line segment from the center to this point
   * is a radius of the constructed circle, and this point
   * lies on the boundary of the circle.
   * 
   * @return a point defining the radius of the Maximum Inscribed Circle
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
   * Computes the signed distance from a point to the area boundary.
   * Points outside the polygon are assigned a negative distance. 
   * Their containing cells will be last in the priority queue
   * (but may still end up being tested since they may need to be refined).
   * 
   * @param p the point to compute the distance for
   * @return the signed distance to the area boundary (negative indicates outside the area)
   */
  private double distanceToBoundary(Point p) {
    double dist = indexedDistance.distance(p);
    boolean isOutide = Location.EXTERIOR == ptLocater.locate(p.getCoordinate());
    if (isOutide) return -dist;
    return dist;
  }

  private double distanceToBoundary(double x, double y) {
    Coordinate coord = new Coordinate(x, y);
    Point pt = factory.createPoint(coord);
    return distanceToBoundary(pt);
  }
  
  private void compute() {
    // check if already computed
    if (centerPt != null) return;
    
    /**
     * Handle flat geometries.
     */
    if (inputGeom.getArea() == 0.0) {
      Coordinate c = inputGeom.getCoordinate().copy();
      createResult(c, c.copy());
      return;
    }
    
    /**
     * Optimization for small simple convex polygons 
     */
    if (ExactMaxInscribedCircle.isSupported(inputGeom)) {
      Coordinate[] centreRadius = ExactMaxInscribedCircle.computeRadius((Polygon) inputGeom);
      createResult(centreRadius[0], centreRadius[1]);
      return;
    }
    
    computeApproximation();
  }

  private void createResult(Coordinate c, Coordinate r) {
    centerPt = c;
    radiusPt = r;
    centerPoint = factory.createPoint(centerPt);
    radiusPoint = factory.createPoint(radiusPt);
  }

  //-- empirically determined to balance accuracy and speed
  private static final double AUTO_TOLERANCE_FRACTION = 0.001;
  
  private void computeApproximation() {  
    if (tolerance < 0) {
      throw new IllegalArgumentException("Tolerance must be non-negative");
    }
    
    ptLocater = new IndexedPointInAreaLocator(inputGeom);
    indexedDistance = new IndexedFacetDistance( inputGeom.getBoundary() );
    
    // Priority queue of cells, ordered by maximum distance from boundary
    PriorityQueue<Cell> cellQueue = new PriorityQueue<>();
    
    createInitialGrid(inputGeom.getEnvelopeInternal(), cellQueue);

    // initial candidate center point
    Cell farthestCell = createInterorPointCell(inputGeom);
    //int totalCells = cellQueue.size();

    /**
     * Carry out the branch-and-bound search
     * of the cell space
     */
    long maxIter = computeMaximumIterations(inputGeom, tolerance);
    long iter = 0;
    while (! cellQueue.isEmpty() && iter < maxIter) {
      iter++;
      // pick the most promising cell from the queue
      Cell cell = cellQueue.remove();
      
      //System.out.println(factory.toGeometry(cell.getEnvelope()));
      //System.out.println(iter + "] Dist: " + cell.getDistance() + " Max D: " + cell.getMaxDistance() + " size: " + cell.getHSide());
      //TestBuilderProxy.showIndicator(inputGeom.getFactory().toGeometry(cell.getEnvelope()));
      
      // update the circle center cell if the candidate is further from the boundary
      if (cell.getDistance() > farthestCell.getDistance()) {
        farthestCell = cell;
      }
      
      //-- search termination when checking isRadiusWithin predicate
      if (maximumRadius >= 0) {
        //-- found a inside point further than max radius
        if (farthestCell.getDistance() > maximumRadius)
          break;
        //-- no cells can have larger radius
        if (cell.getMaxDistance() < maximumRadius)
          break;
      }
      
      /**
       * Refine this cell if the potential distance improvement
       * is greater than the required tolerance.
       * Otherwise the cell is pruned (not investigated further),
       * since no point in it is further than 
       * the current farthest distance (up to tolerance).
       * 
       * The tolerance can be automatically determined 
       * as a fraction of the current farthest distance.
       * For a very small actual MIC distance this may cause many iterations, 
       * but the iter limit prevents an infinite loop
       */
      double requiredTol = tolerance > 0 
          ? tolerance
          : farthestCell.getDistance() * AUTO_TOLERANCE_FRACTION;

      double potentialIncrease = cell.getMaxDistance() - farthestCell.getDistance();
      if (potentialIncrease < requiredTol)
        break;
      
      // refine the cell into four sub-cells
      double h2 = cell.getHSide() / 2;
      cellQueue.add( createCell( cell.getX() - h2, cell.getY() - h2, h2));
      cellQueue.add( createCell( cell.getX() + h2, cell.getY() - h2, h2));
      cellQueue.add( createCell( cell.getX() - h2, cell.getY() + h2, h2));
      cellQueue.add( createCell( cell.getX() + h2, cell.getY() + h2, h2));
      //totalCells += 4;
    }
    //System.out.println("Iter: " + iter);
    
    //-- the farthest cell is the best approximation to the MIC center
    centerCell = farthestCell;
    centerPt = new Coordinate(centerCell.getX(), centerCell.getY());
    centerPoint = factory.createPoint(centerPt);
    // compute radius point
    Coordinate[] nearestPts = indexedDistance.nearestPoints(centerPoint);
    radiusPt = nearestPts[0].copy();
    radiusPoint = factory.createPoint(radiusPt);
  }

  /**
   * Computes the maximum number of iterations allowed.
   * Uses a heuristic based on the size of the input geometry
   * and the tolerance distance.
   * A smaller tolerance distance allows more iterations.
   * This is a rough heuristic, intended
   * to prevent huge iterations for very thin geometries.
   * 
   * @param geom the input geometry
   * @param toleranceDist the tolerance distance
   * @return the maximum number of iterations allowed
   */
  static long computeMaximumIterations(Geometry geom, double toleranceDist) {
    double diam = geom.getEnvelopeInternal().getDiameter();
    double tolDist = toleranceDist <= 0 ? 0.5 * diam * AUTO_TOLERANCE_FRACTION : toleranceDist;
    double ncells = diam / tolDist;
    //-- Using log of ncells allows control over number of iterations
    int factor = (int) Math.log(ncells);
    if (factor < 1) factor = 1;
    return 2000 + 2000 * factor;
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

  private Cell createCell(double x, double y, double hSide) {
    return new Cell(x, y, hSide, distanceToBoundary(x, y));
  }

  // create a cell at an interior point
  private Cell createInterorPointCell(Geometry geom) {
    Point p = geom.getInteriorPoint();
    double hSide = geom.getEnvelopeInternal().getDiameter();
    return new Cell(p.getX(), p.getY(), hSide, distanceToBoundary(p));
  }

  /**
   * A square grid cell centered on a given point, 
   * with a given half-side size, and having a given distance
   * to the area boundary.
   * The maximum possible distance from any point in the cell to the
   * boundary can be computed, and is used
   * as the ordering and upper-bound function in
   * the branch-and-bound algorithm. 
   *
   */
  private static class Cell implements Comparable<Cell> {

    private static final double SQRT2 = 1.4142135623730951;

    private double x;
    private double y;
    private double hSide;
    private double distance;
    private double maxDist;

    Cell(double x, double y, double hSide, double distanceToBoundary) {
      this.x = x; // cell center x
      this.y = y; // cell center y
      this.hSide = hSide; // half the cell size

      // the distance from cell center to area boundary
      distance = distanceToBoundary;

      // the maximum possible distance to area boundary for points in this cell
      this.maxDist = distance + hSide * SQRT2;
    }

    public Envelope getEnvelope() {
      return new Envelope(x - hSide, x + hSide, y - hSide, y + hSide);
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
    public int compareTo(Cell o) {
      return -Double.compare(maxDist, o.maxDist);
    }
    
  }

}
