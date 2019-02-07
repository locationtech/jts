
/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.Assert;

/**
 * Computes a point in the interior of an areal geometry.
 *
 * <h2>Algorithm</h2>
 * For each input polygon:
 * <ul>
 * <li>Find a Y value which is near the centre of the polygon's 
 * vertical extent but distinct from all of its vertex Y ordinates.
 * This determines a horizontal scan line
 * on which an interior point will be found.
 * <li>Compute the sections of the scan line
 * which lie inside the polygon.
 * <li>Choose the widest interior section
 * and take its midpoint as the interior point.
 * </ul>
 * The overall interior point is chosen as
 * the one lying on the widest interior section.
 * <p>
 * This algorithm is a tradeoff between performance 
 * and point quality (where points further from the geometry
 * boundary are considered to be higher quality)
 * Priority given to performance. 
 *
 * <h3>KNOWN BUGS</h3>
 * <ul>
 * <li>If a fixed precision model is used, in some cases this method may return
 * a point which does not lie in the interior.
 * <li>If the input polygon is extremely the computed point
 * may not lie in the interior of the polygon.
 * </ul>
 *
 * @version 1.17
 */
public class InteriorPointArea {

  private static double avg(double a, double b) {
    return (a + b) / 2.0;
  }

  private Coordinate interiorPoint = null;
  private double maxWidth = -1;

  /**
   * Creates a new interior point finder for an areal geometry.
   * 
   * @param g an areal geometry
   */
  public InteriorPointArea(Geometry g) {
    add(g);
  }

  /**
   * Gets the computed interior point.
   * 
   * @return the coordinate of an interior point
   */
  public Coordinate getInteriorPoint() {
    return interiorPoint;
  }

  /**
   * Tests the interior vertices (if any) defined by an areal Geometry for the
   * best inside point. If a component Geometry is not of dimension 2 it is not
   * tested.
   * 
   * @param geom the geometry to add
   */
  private void add(Geometry geom) {
    if ( geom.isEmpty() )
      return;

    if ( geom instanceof Polygon ) {
      addPolygon((Polygon) geom);
    } else if ( geom instanceof GeometryCollection ) {
      GeometryCollection gc = (GeometryCollection) geom;
      for (int i = 0; i < gc.getNumGeometries(); i++) {
        add(gc.getGeometryN(i));
      }
    }
  }

  /**
   * Computes an interior point of a component Polygon
   * and updates current best interior point
   * if appropriate.
   * 
   * @param polygon the polygon to analyze
   */
  private void addPolygon(Polygon polygon) {
    InteriorPointPolygon intPtPoly = new InteriorPointPolygon(polygon);
    intPtPoly.process();
    double width = intPtPoly.getWidth();
    if ( width > maxWidth ) {
      maxWidth = width;
      interiorPoint = intPtPoly.getInteriorPoint();
    }
  }

  /**
   * Finds a safe bisector Y ordinate by projecting 
   * the polygon segments
   * to the Y axis and finding the
   * Y-axis interval which contains the centre of the Y extent. 
   * The centre of
   * this interval is returned as the bisector Y-ordinate.
   * 
   * @author mdavis
   *
   */
  private static class SafeBisectorFinder {
    public static double getBisectorY(Polygon poly) {
      SafeBisectorFinder finder = new SafeBisectorFinder(poly);
      return finder.getBisectorY();
    }

    private Polygon poly;

    private double centreY;
    private double hiY = Double.MAX_VALUE;
    private double loY = -Double.MAX_VALUE;

    public SafeBisectorFinder(Polygon poly) {
      this.poly = poly;

      // initialize using extremal values
      hiY = poly.getEnvelopeInternal().getMaxY();
      loY = poly.getEnvelopeInternal().getMinY();
      centreY = avg(loY, hiY);
    }

    public double getBisectorY() {
      process(poly.getExteriorRing());
      for (int i = 0; i < poly.getNumInteriorRing(); i++) {
        process(poly.getInteriorRingN(i));
      }
      double bisectY = avg(hiY, loY);
      return bisectY;
    }

    private void process(LineString line) {
      CoordinateSequence seq = line.getCoordinateSequence();
      for (int i = 0; i < seq.size(); i++) {
        double y = seq.getY(i);
        updateInterval(y);
      }
    }

    private void updateInterval(double y) {
      if ( y <= centreY ) {
        if ( y > loY )
          loY = y;
      } else if ( y > centreY ) {
        if ( y < hiY ) {
          hiY = y;
        }
      }
    }
  }

  /**
   * Computes an interior point in a single {@link Polygon},
   * as well as the width of the scan segment it occurs in
   * to allow choosing the widest segment occurence.
   * 
   * @author mdavis
   *
   */
  private static class InteriorPointPolygon {
    private Polygon polygon;
    private double interiorPointY;
    private double interiorSegWidth = 0.0;
    private List<Double> crossings = new ArrayList<Double>();
    private Coordinate interiorPoint = null;

    /**
     * Creates a new InteriorPointPolygon instance.
     * 
     * @param polygon the polygon to test
     */
    public InteriorPointPolygon(Polygon polygon) {
      this.polygon = polygon;
      interiorPointY = SafeBisectorFinder.getBisectorY(polygon);
    }

    /**
     * Gets the computed interior point.
     * 
     * @return the interior point coordinate,
     *  or <tt>null</tt> if the input is empty
     */
    public Coordinate getInteriorPoint() {
      return interiorPoint;
    }

    /**
     * Gets the width of the interior horizontal segment containing the interior point.
     * Used to determine the best point to use.
     * 
     * @return the width
     */
    public double getWidth() {
      return interiorSegWidth;
    }

    /**
     * Compute the interior point.
     * 
     */
    public void process() {
      if (polygon.isEmpty()) return;
      
      /**
       * set initial interior point in case polygon has zero area
       */
      interiorPoint = new Coordinate(polygon.getCoordinate());
      
      scanRing((LinearRing) polygon.getExteriorRing());
      for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
        scanRing((LinearRing) polygon.getInteriorRingN(i));
      }
      findBestMidpoint(crossings);
    }

    private void scanRing(LinearRing ring) {
      // skip rings which don't cross scan line
      if ( !intersectsHorizontalLine(ring.getEnvelopeInternal(), interiorPointY) )
        return;

      CoordinateSequence seq = ring.getCoordinateSequence();
      for (int i = 1; i < seq.size(); i++) {
        Coordinate ptPrev = seq.getCoordinate(i - 1);
        Coordinate pt = seq.getCoordinate(i);
        addSegmentCrossing(ptPrev, pt, interiorPointY, crossings);
      }
    }

    private void addSegmentCrossing(Coordinate p0, Coordinate p1, double scanY, List<Double> crossings) {
      // skip non-crossing segments
      if ( !intersectsHorizontalLine(p0, p1, scanY) )
        return;
      // skip horizontal lines
      if ( p0.getY() == p1.getY() )
        return;
      // handle vertices on scan-line
      // downward segment does not include start point
      if ( p0.y == scanY && p1.y < scanY )
        return;
      // upward segment does not include endpoint
      if ( p1.y == scanY && p0.y < scanY )
        return;

      // add a crossing
      double xInt = intersection(p0, p1, scanY);
      crossings.add(xInt);
      //checkIntersectionDD(p0, p1, scanY, xInt);
    }

    private void findBestMidpoint(List<Double> crossings) {
      // TODO: is there a better way to verify the crossings are correct?
      Assert.isTrue(0 == crossings.size() % 2, "Interior Point robustness failure: odd number of scanline crossings");
      
      crossings.sort(Double::compare);
      /*
       * Entries in crossings list are expected to occur in pairs representing a
       * section of the scan line interior to the polygon (which may be zero-length)
       */
      for (int i = 0; i < crossings.size(); i += 2) {
        double x1 = crossings.get(i);
        // crossings count must be even so this should be safe
        double x2 = crossings.get(i + 1);

        double width = x2 - x1;
        if ( width > interiorSegWidth ) {
          interiorSegWidth = width;
          double interiorPointX = avg(x1, x2);
          interiorPoint = new Coordinate(interiorPointX, interiorPointY);
        }
      }
    }

    /**
     * Computes the intersection of a segment with a horizontal line. 
     * The segment is expected to cross the horizontal line
     * - this condition is not checked.
     * Computation uses regular double-precision arithmetic.
     * Test seems to indicate this is as good as using DD arithmetic.
     * 
     * @param p0 an endpoint of the segment
     * @param p1 an endpoint of the segment
     * @param Y  the Y-ordinate of the horizontal line
     * @return
     */
    private static double intersection(Coordinate p0, Coordinate p1, double Y) {
      double x0 = p0.getX();
      double x1 = p1.getX();

      if ( x0 == x1 )
        return x0;
      
      // Assert: segDX is non-zero, due to previous equality test
      double segDX = x1 - x0;
      double segDY = p1.getY() - p0.getY();
      double m = segDY / segDX;
      double x = x0 + ((Y - p0.getY()) / m);
      return x;
    }

    /*
    // for testing only
    private static void checkIntersectionDD(Coordinate p0, Coordinate p1, double scanY, double xInt) {
      double xIntDD = intersectionDD(p0, p1, scanY);
      System.out.println(
          ((xInt != xIntDD) ? ">>" : "")
          + "IntPt x - DP: " + xInt + ", DD: " + xIntDD 
          + "   y: " + scanY + "   " + WKTWriter.toLineString(p0, p1) );
    }

    private static double intersectionDD(Coordinate p0, Coordinate p1, double Y) {
      double x0 = p0.getX();
      double x1 = p1.getX();

      if ( x0 == x1 )
        return x0;
      
      DD segDX = DD.valueOf(x1).selfSubtract(x0);
      // Assert: segDX is non-zero, due to previous equality test
      DD segDY = DD.valueOf(p1.getY()).selfSubtract(p0.getY());
      DD m = segDY.divide(segDX);
      DD dy = DD.valueOf(Y).selfSubtract(p0.getY());
      DD dx = dy.divide(m);
      DD xInt = DD.valueOf(x0).selfAdd(dx);
      return xInt.doubleValue();
    }
  */
    
    /**
     * Tests if an envelope intersects a horizontal line.
     * 
     * @param env the envelope to test
     * @param y the Y-ordinate of the horizontal line
     * @return true if the envelope and line intersect
     */
    private static boolean intersectsHorizontalLine(Envelope env, double y) {
      if ( y < env.getMinY() )
        return false;
      if ( y > env.getMaxY() )
        return false;
      return true;
    }
    
    /**
     * Tests if a line segment intersects a horizontal line.
     * 
     * @param p0 a segment endpoint
     * @param p1 a segment endpoint
     * @param y the Y-ordinate of the horizontal line
     * @return true if the segment and line intersect
     */
    private static boolean intersectsHorizontalLine(Coordinate p0, Coordinate p1, double y) {
      // both ends above?
      if ( p0.getY() > y && p1.getY() > y )
        return false;
      // both ends below?
      if ( p0.getY() < y && p1.getY() < y )
        return false;
      // segment must intersect line
      return true;
    }

  }
}
