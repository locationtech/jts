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

package org.locationtech.jts.noding.snapround;

import org.locationtech.jts.algorithm.CGAlgorithmsDD;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.io.WKTWriter;

/**
 * Implements a "hot pixel" as used in the Snap Rounding algorithm.
 * A hot pixel is a square region centred 
 * on the rounded valud of the coordinate given,
 * and of width equal to the size of the scale factor.
 * It is a partially open region, which contains 
 * the interior of the tolerance square and
 * the boundary
 * <b>minus</b> the top and right segments.
 * This ensures that every point of the space lies in a unique hot pixel.
 * It also matches the rounding semantics for numbers.
 * <p>
 * The hot pixel operations are all computed in the integer domain
 * to avoid rounding problems.
 *
 * @version 1.7
 */
public class HotPixel
{
  // testing only
//  public static int nTests = 0;
  private static double TOLERANCE = 0.5;

  private Coordinate ptHot;
  private Coordinate originalPt;

  private double scaleFactor;

  private double minx;
  private double maxx;
  private double miny;
  private double maxy;
  
  private Envelope safeEnv = null;

  /**
   * Creates a new hot pixel, using a given scale factor.
   * The scale factor must be strictly positive (non-zero).
   * 
   * @param pt the coordinate at the centre of the pixel
   * @param scaleFactor the scaleFactor determining the pixel size.  Must be &gt; 0
   * @param li the intersector to use for testing intersection with line segments
   * 
   */
  public HotPixel(Coordinate pt, double scaleFactor) {
    originalPt = pt;
    this.ptHot = pt;
    this.scaleFactor = scaleFactor;
    
    if (scaleFactor <= 0) 
      throw new IllegalArgumentException("Scale factor must be non-zero");
    if (scaleFactor != 1.0) {
      this.ptHot = scaleRound(pt);
    }
    
    // extreme values for pixel
    minx = ptHot.x - TOLERANCE;
    maxx = ptHot.x + TOLERANCE;
    miny = ptHot.y - TOLERANCE;
    maxy = ptHot.y + TOLERANCE;
  }

  /**
   * Gets the coordinate this hot pixel is based at.
   * 
   * @return the coordinate of the pixel
   */
  public Coordinate getCoordinate() { return originalPt; }

  /**
   * Gets the scale factor for the precision grid for this pixel.
   * 
   * @return the pixel scale factor
   */
  public double getScaleFactor() {
    return scaleFactor;
  }

  /**
   * Gets the width of the hot pixel in the original coordinate system.
   * 
   * @return the width of the hot pixel tolerance square
   */
  public double getWidth() {
    return 1.0 / scaleFactor;
  }
  
  private double scaleRound(double val)
  {
    return (double) Math.round(val * scaleFactor);
  }

  private Coordinate scaleRound(Coordinate p)
  {
    return new Coordinate(scaleRound(p.x), scaleRound(p.y));
  }

  /**
   * Scale without rounding. 
   * This ensures intersections are checked against original
   * linework.
   * This is required to ensure that intersections are not missed
   * because the segment is moved by snapping.
   * 
   * @param val
   * @return
   */
  private double scale(double val)
  {
    return val * scaleFactor;
  }

  /**
   * Tests whether the line segment (p0-p1) 
   * intersects this hot pixel.
   * 
   * @param p0 the first coordinate of the line segment to test
   * @param p1 the second coordinate of the line segment to test
   * @return true if the line segment intersects this hot pixel
   */
  public boolean intersects(Coordinate p0, Coordinate p1)
  {
    if (scaleFactor == 1.0)
      return intersectsScaled(p0.x, p0.y, p1.x, p1.y);

    double sp0x = scale(p0.x);
    double sp0y = scale(p0.y);
    double sp1x = scale(p1.x);
    double sp1y = scale(p1.y);
    return intersectsScaled(sp0x, sp0y, sp1x, sp1y);
  }

  private boolean intersectsScaled(double p0x, double p0y,
      double p1x, double p1y) {
    // determine oriented segment pointing in positive X direction
    double px = p0x;
    double py = p0y;
    double qx = p1x;
    double qy = p1y;
    if (px > qx) {
      px = p1x;
      py = p1y;
      qx = p0x;
      qy = p0y;
    }  
     /**
     * Report false if segment env does not intersect pixel env.
     * This check reflects the fact that the pixel Top and Right sides
     * are open (not part of the pixel).
     */   
    // check Right side
    double segMinx = Math.min(px, qx);
    if (segMinx >= maxx) return false;
    // check Left side
    double segMaxx = Math.max(px, qx);
    if (segMaxx < minx) return false;
    // check Top side
    double segMiny = Math.min(py, qy);
    if (segMiny >= maxy) return false;
    // check Bottom side
    double segMaxy = Math.max(py, qy);
    if (segMaxy < miny) return false;

    /**
     * Vertical or horizontal segments must now intersect
     * the segment interior or Left or Bottom sides.
     */
    //---- check vertical segment
    if (px == qx) {
      return true;
    }
    //---- check horizontal segment
    if (py == qy) {
      return true;
    }

    /**
     * Now know segment is not horizontal or vertical.
     * 
     * Compute orientation WRT each pixel corner.
     * If corner orientation == 0, 
     * segment intersects the corner.  
     * From the corner and whether segment is heading up or down,
     * can determine intersection or not.
     * 
     * Otherwise, check whether segment crosses interior of pixel side
     * This is the case if the orientations for each corner of the side are different.
     */
    
    int orientUL = CGAlgorithmsDD.orientationIndex(px, py, qx, qy, minx, maxy);
    if (orientUL == 0) {
      if (py < qy) return false;
      return true;
    }
    
    int orientUR = CGAlgorithmsDD.orientationIndex(px, py, qx, qy, maxx, maxy);
    if (orientUR == 0) {
      if (py > qy) return false;
      return true;
    }
    //--- check crossing Top side
    if (orientUL != orientUR) {
      return true;
    }
    
    int orientLL = CGAlgorithmsDD.orientationIndex(px, py, qx, qy, minx, miny);
    if (orientUL == 0) {
      // LL corner is the only one in pixel interior
      return true;
    }
    //--- check crossing Left side
    if (orientLL != orientUL) {
      return true;
    }
    
    int orientLR = CGAlgorithmsDD.orientationIndex(px, py, qx, qy, maxx, miny);
    if (orientLR == 0) {
      if (py < qy) return false;
      return true;
    }

    //--- check crossing Bottom side
    if (orientLL != orientLR) {
      return true;
    }
    //--- check crossing Right side
    if (orientLR != orientUR) {
      return true;
    }

    // segment does not intersect pixel
    return false;
  }
  
  private static final int UPPER_RIGHT = 0;
  private static final int UPPER_LEFT = 1;
  private static final int LOWER_LEFT = 2;
  private static final int LOWER_RIGHT = 3;

  /**
   * Test whether a segment intersects
   * the closure of this hot pixel.
   * This is NOT the test used in the standard snap-rounding
   * algorithm, which uses the partially-open tolerance square
   * instead.
   * This method is provided for testing purposes only.
   *
   * @param p0 the start point of a line segment
   * @param p1 the end point of a line segment
   * @return <code>true</code> if the segment intersects the closure of the pixel's tolerance square
   */
  private boolean intersectsPixelClosure(Coordinate p0, Coordinate p1)
  {
    Coordinate[] corner = new Coordinate[4];
    corner[UPPER_RIGHT] = new Coordinate(maxx, maxy);
    corner[UPPER_LEFT] = new Coordinate(minx, maxy);
    corner[LOWER_LEFT] = new Coordinate(minx, miny);
    corner[LOWER_RIGHT] = new Coordinate(maxx, miny);
    
    LineIntersector li = new RobustLineIntersector();
    li.computeIntersection(p0, p1, corner[0], corner[1]);
    if (li.hasIntersection()) return true;
    li.computeIntersection(p0, p1, corner[1], corner[2]);
    if (li.hasIntersection()) return true;
    li.computeIntersection(p0, p1, corner[2], corner[3]);
    if (li.hasIntersection()) return true;
    li.computeIntersection(p0, p1, corner[3], corner[0]);
    if (li.hasIntersection()) return true;

    return false;
  }
  
  public String toString() {
    return "HP(" + WKTWriter.format(ptHot) + ")";
  }

}
