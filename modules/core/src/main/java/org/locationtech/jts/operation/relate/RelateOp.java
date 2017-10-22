


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
package org.locationtech.jts.operation.relate;

import org.locationtech.jts.algorithm.BoundaryNodeRule;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.IntersectionMatrix;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.GeometryGraphOperation;
import org.locationtech.jts.operation.predicate.RectangleContains;
import org.locationtech.jts.operation.predicate.RectangleIntersects;

/**
 * Implements the SFS <tt>relate()</tt> generalized spatial predicate on two {@link Geometry}s.
 * <p>
 * The class supports specifying a custom {@link BoundaryNodeRule}
 * to be used during the relate computation.
 * <p>
 * If named spatial predicates are used on the result {@link IntersectionMatrix}
 * of the RelateOp, the result may or not be affected by the 
 * choice of <tt>BoundaryNodeRule</tt>, depending on the exact nature of the pattern.
 * For instance, {@link IntersectionMatrix#isIntersects()} is insensitive 
 * to the choice of <tt>BoundaryNodeRule</tt>, 
 * whereas {@link IntersectionMatrix#isTouches(int, int)} is affected by the rule chosen.
 * <p>
 * <b>Note:</b> custom Boundary Node Rules do not (currently)
 * affect the results of other {@link Geometry} methods (such
 * as {@link Geometry#getBoundary}.  The results of
 * these methods may not be consistent with the relationship computed by
 * a custom Boundary Node Rule.
 *
 * @version 1.7
 */
public class RelateOp
  extends GeometryGraphOperation
{
  /**
   * Computes the {@link IntersectionMatrix} for the spatial relationship
   * between two {@link Geometry}s, using the default (OGC SFS) Boundary Node Rule
   *
   * @param a a Geometry to test
   * @param b a Geometry to test
   * @return the IntersectionMatrix for the spatial relationship between the geometries
   */
  public static IntersectionMatrix relate(Geometry a, Geometry b)
  {
    RelateOp relOp = new RelateOp(a, b);
    IntersectionMatrix im = relOp.getIntersectionMatrix();
    return im;
  }

  /**
   * Computes the {@link IntersectionMatrix} for the spatial relationship
   * between two {@link Geometry}s using a specified Boundary Node Rule.
   *
   * @param a a Geometry to test
   * @param b a Geometry to test
   * @param boundaryNodeRule the Boundary Node Rule to use
   * @return the IntersectionMatrix for the spatial relationship between the input geometries
   */
  public static IntersectionMatrix relate(Geometry a, Geometry b, BoundaryNodeRule boundaryNodeRule)
  {
    RelateOp relOp = new RelateOp(a, b, boundaryNodeRule);
    IntersectionMatrix im = relOp.getIntersectionMatrix();
    return im;
  }
  
  /**
   * @see Geometry#intersects(Geometry)
   */
  public static boolean intersects(Geometry g1, Geometry g2) {
    // short-circuit envelope test
    if (! g1.getEnvelopeInternal().intersects(g2.getEnvelopeInternal()))
      return false;

    /**
     * TODO: (MD) Add optimizations:
     *
     * - for P-A case:
     * If P is in env(A), test for point-in-poly
     *
     * - for A-A case:
     * If env(A1).overlaps(env(A2))
     * test for overlaps via point-in-poly first (both ways)
     * Possibly optimize selection of point to test by finding point of A1
     * closest to centre of env(A2).
     * (Is there a test where we shouldn't bother - e.g. if env A
     * is much smaller than env B, maybe there's no point in testing
     * pt(B) in env(A)?
     */

    // optimization for rectangle arguments
    if (g1.isRectangle()) {
      return RectangleIntersects.intersects((Polygon) g1, g2);
    }
    if (g2.isRectangle()) {
      return RectangleIntersects.intersects((Polygon) g2, g1);
    }
    if (g1.isGeometryCollection() || g2.isGeometryCollection()) {
      boolean r = false;
      for (int i = 0 ; i < g1.getNumGeometries() ; i++) {
        for (int j = 0 ; j < g2.getNumGeometries() ; j++) {
          if (g1.getGeometryN(i).intersects(g2.getGeometryN(j))) {
            return true;
          }
        }
      }
      return false;
    }
    // general case
    return new RelateOp(g1, g2).getIntersectionMatrix().isIntersects();
  }
  
  /**
   * @see Geometry#contains(Geometry)
   */
  public static boolean contains(Geometry g1, Geometry g2) {
    // optimization - lower dimension cannot contain areas
    if (g2.getDimension() == 2 && g1.getDimension() < 2) {
      return false;
    }
    // optimization - P cannot contain a non-zero-length L
    // Note that a point can contain a zero-length lineal geometry, 
    // since the line has no boundary due to Mod-2 Boundary Rule
    if (g2.getDimension() == 1 && g1.getDimension() < 1 && g2.getLength() > 0.0) {
      return false;
    }
    // optimization - envelope test
    if (! g1.getEnvelopeInternal().contains(g2.getEnvelopeInternal()))
      return false;
    // optimization for rectangle arguments
    if (g1.isRectangle()) {
      return RectangleContains.contains((Polygon) g1, g2);
    }
    // general case
    return new RelateOp(g1, g2).getIntersectionMatrix().isContains();
  }
  
  /**
   * @see Geometry#overlaps(Geometry)
   */
  public static boolean overlaps(Geometry g1, Geometry g2) {
    // short-circuit test
    if (! g1.getEnvelopeInternal().intersects(g2.getEnvelopeInternal()))
      return false;
    return new RelateOp(g1, g2).getIntersectionMatrix().isOverlaps(g1.getDimension(), g2.getDimension());
  }
  
  /**
   * @see Geometry#covers(Geometry)
   */
  public static boolean covers(Geometry g1, Geometry g2) {
    // optimization - lower dimension cannot cover areas
    if (g2.getDimension() == 2 && g1.getDimension() < 2) {
      return false;
    }
    // optimization - P cannot cover a non-zero-length L
    // Note that a point can cover a zero-length lineal geometry
    if (g2.getDimension() == 1 && g1.getDimension() < 1 && g2.getLength() > 0.0) {
      return false;
    }
    // optimization - envelope test
    if (! g1.getEnvelopeInternal().covers(g2.getEnvelopeInternal()))
      return false;
    // optimization for rectangle arguments
    if (g1.isRectangle()) {
    	// since we have already tested that the test envelope is covered
      return true;
    }
    return new RelateOp(g1, g2).getIntersectionMatrix().isCovers();
  }
  
  /**
   * @see Geometry#touches(Geometry)
   */
  public static boolean touches(Geometry g1, Geometry g2) {
    // short-circuit test
    if (! g1.getEnvelopeInternal().intersects(g2.getEnvelopeInternal()))
      return false;
    return new RelateOp(g1, g2).getIntersectionMatrix().isTouches(g1.getDimension(), g2.getDimension());
  }
  
  /**
   * @see Geometry#crosses(Geometry)
   */
  public static boolean crosses(Geometry g1, Geometry g2) {
    // short-circuit test
    if (! g1.getEnvelopeInternal().intersects(g2.getEnvelopeInternal()))
      return false;
    return new RelateOp(g1, g2).getIntersectionMatrix().isCrosses(g1.getDimension(), g2.getDimension());
  }

  private RelateComputer relate;

  /**
   * Creates a new Relate operation, using the default (OGC SFS) Boundary Node Rule.
   *
   * @param g0 a Geometry to relate
   * @param g1 another Geometry to relate
   */
  public RelateOp(Geometry g0, Geometry g1)
  {
    super(g0, g1);
    relate = new RelateComputer(arg);
  }

  /**
   * Creates a new Relate operation with a specified Boundary Node Rule.
   *
   * @param g0 a Geometry to relate
   * @param g1 another Geometry to relate
   * @param boundaryNodeRule the Boundary Node Rule to use
   */
  public RelateOp(Geometry g0, Geometry g1, BoundaryNodeRule boundaryNodeRule)
  {
    super(g0, g1, boundaryNodeRule);
    relate = new RelateComputer(arg);
  }

  /**
   * Gets the IntersectionMatrix for the spatial relationship
   * between the input geometries.
   *
   * @return the IntersectionMatrix for the spatial relationship between the input geometries
   */
  public IntersectionMatrix getIntersectionMatrix()
  {
    return relate.computeIM();
  }

}
