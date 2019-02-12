package org.locationtech.jts.algorithm;

import java.util.PriorityQueue;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

public class MaximumInnerCircle {

  public static Point getCenter(Geometry polygon, double precision) {
    MaximumInnerCircle maxInnerCircle = new MaximumInnerCircle(polygon, precision);
    maxInnerCircle.compute();
    return maxInnerCircle.getCenter();
  }

  private Geometry polygon;
  private double precision;
  private Geometry boundary;
  private IndexedPointInAreaLocator locater;
  private IndexedFacetDistance indexedDistance;
  private Point center;

  MaximumInnerCircle(Geometry polygon, double precision) {
    this.polygon = polygon;
    this.precision = precision;
    locater = new IndexedPointInAreaLocator(polygon);
    boundary = polygon.getBoundary();
    indexedDistance = new IndexedFacetDistance(boundary);
  }

  private double distanceToPoly(Point p) {
    boolean inside = Location.EXTERIOR != locater.locate(p.getCoordinate());
    double dist = indexedDistance.distance(p);

    // Points outside has a negative distance and thus will be weighted down
    // later.
    return (inside ? 1 : -1) * dist;
  }

  private double nearestPoint(Point p) {
    boolean inside = Location.EXTERIOR != locater.locate(p.getCoordinate());
    double dist = indexedDistance.distance(p);

    // Points outside has a negative distance and thus will be weighted down
    // later.
    return (inside ? 1 : -1) * dist;
  }

  public Point getCenter() {
    return center;
  }
  
  public Point getExtremalPoint() {
    Coordinate[] nearestPts = indexedDistance.nearestPoints(center);
    Coordinate extremalPt = nearestPts[0];
    return polygon.getFactory().createPoint(extremalPt);
  }
  
  public void compute() {
    Geometry multiPolygon;
    if (polygon instanceof Polygon) {
      multiPolygon = polygon;
    } else if (polygon instanceof MultiPolygon) {
      multiPolygon = (MultiPolygon) polygon;
    } else {
      throw new IllegalStateException("Input polygon must be a Polygon or MultiPolygon");
    }

    if (polygon.isEmpty() || polygon.getArea() <= 0.0) {
      throw new IllegalStateException("Can not handle empty geometries");
    }

    // find the bounding box of the outer ring
    double minX, minY, maxX, maxY;
    Envelope env = multiPolygon.getEnvelopeInternal();
    minX = env.getMinX();
    maxX = env.getMaxX();
    minY = env.getMinY();
    maxY = env.getMaxY();
    double width = env.getWidth();
    double height = env.getHeight();
    double cellSize = Math.min(width, height);
    double h = cellSize / 2.0;

    // a priority queue of cells in order of their "potential" (max distance
    // to polygon)
    PriorityQueue<Quad> cellQueue = new PriorityQueue<>();

    // cover polygon with initial cells
    for (double x = minX; x < maxX; x += cellSize) {
      for (double y = minY; y < maxY; y += cellSize) {
        cellQueue.add(createCell(x + h, y + h, h));
      }
    }

    // take centroid as the first best guess
    Quad bestCell = getCentroidCell(multiPolygon);
    int numProbes = cellQueue.size();

    while (!cellQueue.isEmpty()) {
      // pick the most promising cell from the queue
      Quad cell = cellQueue.remove();

      // update the best cell if we found a better one
      if (cell.getD() > bestCell.getD()) {
        bestCell = cell;
      }

      // do not drill down if there's no chance of a better
      // solution
      if (cell.getMax() - bestCell.getD() <= precision)
        continue;

      // split the cell into four cells
      h = cell.getH() / 2;
      cellQueue.add(createCell(cell.getX() - h, cell.getY() - h, h));
      cellQueue.add(createCell(cell.getX() + h, cell.getY() - h, h));
      cellQueue.add(createCell(cell.getX() - h, cell.getY() + h, h));
      cellQueue.add(createCell(cell.getX() + h, cell.getY() + h, h));
      numProbes += 4;
    }
    center = createPoint(bestCell);
  }

  Point createPoint(Quad cell) {
    return createPoint(cell.getX(), cell.getY());
  }

  Point createPoint(double x, double y) {
    Coordinate coord = new Coordinate(x, y);
    return polygon.getFactory().createPoint(coord);
  }

  Quad createCell(double x, double y, double h) {
    return new Quad(x, y, h, distanceToPoly(createPoint(x, y)));
  }

  // get a cell centered on polygon centroid
  private Quad getCentroidCell(Geometry poly) {
    Point p = poly.getCentroid();
    return new Quad(p.getX(), p.getY(), 0, distanceToPoly(p));
  }

  public class Quad implements Comparable<Quad> {

    private static final double SQRT2 = 1.4142135623730951;

    private double x;
    private double y;
    private double h;
    private double d;
    private double max;

    Quad(double x, double y, double h, double distanceToPolygon) {
      this.x = x; // cell center x
      this.y = y; // cell center y
      this.h = h; // half the cell size

      // distance from cell center to polygon
      d = distanceToPolygon;

      // max distance to polygon within a cell
      this.setMax(this.getD() + this.getH() * SQRT2);
    }

    public int compareTo(Quad o) {
      return (int) (o.getMax() - getMax());
    }

    public double getMax() {
      return max;
    }

    public void setMax(double max) {
      this.max = max;
    }

    public double getD() {
      return d;
    }

    public double getH() {
      return h;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

  }

}