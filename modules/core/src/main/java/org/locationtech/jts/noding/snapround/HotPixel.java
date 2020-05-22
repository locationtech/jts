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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Debug;

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


  private static final int UPPER_RIGHT = 0;
  private static final int UPPER_LEFT = 1;
  private static final int LOWER_LEFT = 2;
  private static final int LOWER_RIGHT = 3;

  private LineIntersector li;

  private Coordinate ptHot;
  private Coordinate originalPt;

  private Coordinate p0Scaled;
  private Coordinate p1Scaled;

  private double scaleFactor;

  private double minx;
  private double maxx;
  private double miny;
  private double maxy;
  
  private int snapCount = 0;
  
  /**
   * The corners of the hot pixel, in the order:
   *  10
   *  23
   */
  private Coordinate[] corner = new Coordinate[4];

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
  public HotPixel(Coordinate pt, double scaleFactor, LineIntersector li) {
    originalPt = pt;
    this.ptHot = pt;
    this.scaleFactor = scaleFactor;
    this.li = li;
    //tolerance = 0.5;
    if (scaleFactor <= 0) 
      throw new IllegalArgumentException("Scale factor must be non-zero");
    if (scaleFactor != 1.0) {
      this.ptHot = scaleRound(pt);
      p0Scaled = new Coordinate();
      p1Scaled = new Coordinate();
    }
    initCorners(this.ptHot);
  }

  public void incrementSnapCount() {
    snapCount++;
  }
  public int getSnapCount() {
    return snapCount;
  }
  
  /**
   * Gets the coordinate this hot pixel is based at.
   * 
   * @return the coordinate of the pixel
   */
  public Coordinate getCoordinate() { return originalPt; }

  private static final double SAFE_ENV_EXPANSION_FACTOR = 0.75;
  
  /**
   * Returns a "safe" envelope that is guaranteed to contain the hot pixel.
   * The envelope returned will be larger than the exact envelope of the 
   * pixel.
   * 
   * @return an envelope which contains the hot pixel
   */
  public Envelope getSafeEnvelope()
  {
    if (safeEnv == null) {
      double safeTolerance = SAFE_ENV_EXPANSION_FACTOR / scaleFactor;
      safeEnv = new Envelope(originalPt.x - safeTolerance,
                             originalPt.x + safeTolerance,
                             originalPt.y - safeTolerance,
                             originalPt.y + safeTolerance
                             );
    }
    return safeEnv;
  }

  private void initCorners(Coordinate pt)
  {
    double tolerance = 0.5;
    minx = pt.x - tolerance;
    maxx = pt.x + tolerance;
    miny = pt.y - tolerance;
    maxy = pt.y + tolerance;

    corner[UPPER_RIGHT] = new Coordinate(maxx, maxy);
    corner[UPPER_LEFT] = new Coordinate(minx, maxy);
    corner[LOWER_LEFT] = new Coordinate(minx, miny);
    corner[LOWER_RIGHT] = new Coordinate(maxx, miny);
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
      return intersectsScaled(p0, p1);

    copyScaled(p0, p0Scaled);
    copyScaled(p1, p1Scaled);
    return intersectsScaled(p0Scaled, p1Scaled);
  }

  private void copyScaled(Coordinate p, Coordinate pScaled)
  {
    pScaled.x = scale(p.x);
    pScaled.y = scale(p.y);
  }

  private boolean intersectsScaled(Coordinate p0, Coordinate p1)
  {
    double segMinx = Math.min(p0.x, p1.x);
    double segMaxx = Math.max(p0.x, p1.x);
    double segMiny = Math.min(p0.y, p1.y);
    double segMaxy = Math.max(p0.y, p1.y);

    /**
     * Report false if segment env does not intersect pixel env.
     * This check reflects the fact that the pixel top and right sides
     * are open (not part of the pixel).
     */
    boolean isOutsidePixelEnv =  maxx <= segMinx
                         || minx > segMaxx
                         || maxy <= segMiny
                         || miny > segMaxy;
    if (isOutsidePixelEnv)
      return false;
    
    boolean intersects = intersectsToleranceSquareScaled(p0, p1);
/*
    // Debugging code
    boolean intersectsPixelClosure = intersectsPixelClosure(p0, p1);

    if (intersectsPixelClosure != intersects) {
      System.out.println("Found hot pixel intersection mismatch at " + originalPt);
      System.out.println("Test segment: " + p0 + " " + p1);
    }
//*/
/*
    if (scaleFactor != 1.0) {
      boolean intersectsScaled = intersectsScaledTest(p0, p1);
      if (intersectsScaled != intersects) {
        intersectsScaledTest(p0, p1);
//        Debug.println("Found hot pixel scaled intersection mismatch at " + pt);
//        Debug.println("Test segment: " + p0 + " " + p1);
      }
      return intersectsScaled;
    }
*/

    Assert.isTrue(! (isOutsidePixelEnv && intersects), "Found bad envelope test");
//    if (isOutsideEnv && intersects) {
//      Debug.println("Found bad envelope test");
//    }

    return intersects;
    //return intersectsPixelClosure;
  }

  private boolean intersectsToleranceSquareScaled(Coordinate p0, Coordinate p1) {
    // determine oriented segment
    double px = p0.x;
    double py = p0.y;
    double qx = p1.x;
    double qy = p1.y;
    if (px > qx) {
      px = p1.x;
      py = p1.y;
      qx = p0.x;
      qy = p0.y;
    }  
    
    // vertical segment
    if (px == qx) {
      if (px < minx) return false;
      if (px >= maxx) return false;
      
      double segminy = Math.min(py, qy);
      double segmaxy = Math.max(py, qy);
      if (segmaxy < miny) return false;
      if (segminy >= maxy) return false;
      return true;
    }
    // horizontal segment
    if (py == qy) {
      if (py < miny) return false;
      if (py >= maxy) return false;
      
      double segminx = Math.min(px, qx);
      double segmaxx = Math.max(px, qx);
      if (segmaxx < minx) return false;
      if (segminx >= maxx) return false;
      return true;
    }

    /**
     * Now know segment is not horizontal or vertical.
     * 
     * Compute orientation WRT each pixel corner.
     * If corner orientation == 0, 
     * segment intersects that corner.  
     * Depending on corner and whether segment is up or down,
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
    // crosses Top side
    if (orientUL != orientUR) {
      return true;
    }
    
    int orientLL = CGAlgorithmsDD.orientationIndex(px, py, qx, qy, minx, miny);
    if (orientUL == 0) {
      // LL corner is in pixel
      return true;
    }
    // crosses Left side
    if (orientLL != orientUL) {
      return true;
    }
    
    int orientLR = CGAlgorithmsDD.orientationIndex(px, py, qx, qy, maxx, miny);
    if (orientLR == 0) {
      if (py < qy) return false;
      return true;
    }

    // crosses Bottom side
    if (orientLL != orientLR) {
      return true;
    }
    // crosses Right side
    if (orientLR != orientUR) {
      return true;
    }
    
    
    /**
     * Check for an edge crossing pixel on a diagonal.
     * The check handles both diagonals.
     * It relies on segments which lie along Top or Right sides
     * already being reported as not intersecting.
     */
    //if (intersectsTop && intersectsBottom) {
    //  return true;
    //}
    
    /**
     * Tests if either endpoint snaps to this pixel.
     * This is needed because a (un-rounded) segment may
     * terminate inside the pixel without crossing a pixel edge interior
     * (i.e. it may enter through a corner)
     */
    //if (equalsPointScaled(p0)) return true;
    //if (equalsPointScaled(p1)) return true;

    // segment does not intersect pixel
    return false;
  }
  /**
   * Tests whether the segment p0-p1 intersects the hot pixel tolerance square.
   * <p>
   * The pixel tolerance square is open on the Top and Right sides.
   * It is sufficient to check if any of the following occur:
   * <ul>
   * <li>a proper intersection between the segment and any pixel side.
   * <li>an intersection between a segment coincident with some or all of the Left or Bottom sides.
   * <li>an intersection between the segment and <b>both</b> the Top and Bottom pixel sides
   * (which detects the case where the segment lies exactly on the diagonal of the pixel)
   * <li>an intersection between a segment endpoint and the pixel interior
   * </ul>
   * 
   * @param p0 an endpoint of the line segment, scaled
   * @param p1 an endpoint of the line segment, scale
   * @return true if the line segment intersects this hot pixel
   */
  private boolean OLDintersectsToleranceSquareScaled(Coordinate p0, Coordinate p1)
  {
    //System.out.println("Hot Pixel: " + this + " - [ " + WKTWriter.toLineString(corner));
    //System.out.println("Segment: " + WKTWriter.toLineString(p0, p1));
    
    boolean intersectsTop = false;
    boolean intersectsBottom = false;
    
    // check intersection with pixel Left side
    li.computeIntersection(p0, p1, corner[UPPER_LEFT], corner[LOWER_LEFT]);
    if (li.isProper()) return true;
    
    // check intersection with pixel Right side
    li.computeIntersection(p0, p1, corner[LOWER_RIGHT], corner[UPPER_RIGHT]);
    if (li.isProper()) return true;

    // check intersection with pixel Top side
    li.computeIntersection(p0, p1, corner[UPPER_RIGHT], corner[UPPER_LEFT]);
    if (li.isProper()) return true;
    if (li.hasIntersection()) {
      intersectsTop = true;
    }

    // check intersection with pixel Bottom side
    li.computeIntersection(p0, p1, corner[LOWER_LEFT], corner[LOWER_RIGHT]);
    if (li.isProper()) return true;
    if (li.hasIntersection()) {
      intersectsBottom = true;
    }
    
    /**
     * Check for an edge crossing pixel on a diagonal.
     * The check handles both diagonals.
     * It relies on segments which lie along Top or Right sides
     * already being reported as not intersecting.
     */
    if (intersectsTop && intersectsBottom) {
      return true;
    }
    
    // check intersection of vertical segment overlapping pixel Left side
    if (p0.getX() == minx && p1.getX() == minx) {
      if (p0.getY() < maxy || p1.getY() < maxy) {
        return true;
      }
    }
    // check intersection of horizontal segment overlapping pixel Bottom side
    if (p0.getY() == miny && p1.getY() == miny) {
      if (p0.getX() < maxx || p1.getX() < maxx) {
        return true;
      }
    }

    /**
     * Tests if either endpoint snaps to this pixel.
     * This is needed because a (un-rounded) segment may
     * terminate inside the pixel without crossing a pixel edge interior
     * (i.e. it may enter through a corner)
     */
    if (equalsPointScaled(p0)) return true;
    if (equalsPointScaled(p1)) return true;

    // segment does not intersect pixel
    return false;
  }
  
  /**
   * Tests if a scaled coordinate snaps (rounds) to this pixel.
   * 
   * @param p the point to test
   * @return true if the coordinate snaps to this pixel
   */
  private boolean equalsPointScaled(Coordinate p) {
    double x = Math.round(p.x);
    if (x != ptHot.x) return false;
    double y = Math.round(p.y);
    return y == ptHot.y;
  }
  
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
  
  /**
   * Adds a new node (equal to the snap pt) to the specified segment
   * if the segment passes through the hot pixel
   *
   * @param segStr
   * @param segIndex
   * @return true if a node was added to the segment
   */
  public boolean addSnappedNode(
      NodedSegmentString segStr,
      int segIndex
      )
  {
    Coordinate p0 = segStr.getCoordinate(segIndex);
    Coordinate p1 = segStr.getCoordinate(segIndex + 1);

    if (intersects(p0, p1)) {
      //System.out.println("snapped: " + snapPt);
      //System.out.println("POINT (" + snapPt.x + " " + snapPt.y + ")");
      segStr.addIntersection(getCoordinate(), segIndex);

      return true;
    }
    return false;
  }
  
  public String toString() {
    return "HP(" + WKTWriter.format(ptHot) + ")";
  }

}
