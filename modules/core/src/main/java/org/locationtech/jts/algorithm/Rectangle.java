/*
 * Copyright (c) 2023 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.algorithm;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

class Rectangle {
  
  /**
   * Creates a rectangular {@link Polygon} from a base segment
   * defining the position and orientation of one side of the rectangle, and 
   * three points defining the locations of the line segments 
   * forming the opposite, left and right sides of the rectangle.
   * The base segment and side points must be presented so that the 
   * rectangle has CW orientation.
   * <p>
   * The rectangle corners are computed as intersections of
   * lines, which generally cannot produce exact values.
   * If a rectangle corner is determined to coincide with a side point
   * the side point value is used to avoid numerical inaccuracy.
   * <p>
   * The first side of the constructed rectangle contains the base segment.
   * 
   * @param baseRightPt the right point of the base segment
   * @param baseLeftPt the left point of the base segment
   * @param oppositePt the point defining the opposite side
   * @param leftSidePt the point defining the left side
   * @param rightSidePt the point defining the right side
   * @param factory the geometry factory to use
   * @return the rectangular polygon
   */
  public static Polygon createFromSidePts(Coordinate baseRightPt, Coordinate baseLeftPt, 
      Coordinate oppositePt, 
      Coordinate leftSidePt, Coordinate rightSidePt, 
      GeometryFactory factory)
  {
    //-- deltas for the base segment provide slope
    double dx = baseLeftPt.x - baseRightPt.x;
    double dy = baseLeftPt.y - baseRightPt.y;
    // Assert: dx and dy are not both zero
    
    double baseC = computeLineEquationC(dx, dy, baseRightPt);
    double oppC = computeLineEquationC(dx, dy, oppositePt);
    double leftC = computeLineEquationC(-dy, dx, leftSidePt);
    double rightC = computeLineEquationC(-dy, dx, rightSidePt);
    
    //-- compute lines along edges of rectangle
    LineSegment baseLine = createLineForStandardEquation(-dy, dx, baseC);
    LineSegment oppLine = createLineForStandardEquation(-dy, dx, oppC);
    LineSegment leftLine = createLineForStandardEquation(-dx, -dy, leftC);
    LineSegment rightLine = createLineForStandardEquation(-dx, -dy, rightC);
    
    /**
     * Corners of rectangle are the intersections of the 
     * base and opposite, and left and right lines.
     * The rectangle is constructed with CW orientation.
     * The first side of the constructed rectangle contains the base segment.
     * 
     * If a corner coincides with a input point
     * the exact value is used to avoid numerical inaccuracy.
     */
    Coordinate p0 = rightSidePt.equals2D(baseRightPt) ? baseRightPt.copy() 
        : baseLine.lineIntersection(rightLine);
    Coordinate p1 = leftSidePt.equals2D(baseLeftPt) ? baseLeftPt.copy() 
        : baseLine.lineIntersection(leftLine);
    Coordinate p2 = leftSidePt.equals2D(oppositePt) ? oppositePt.copy() 
        : oppLine.lineIntersection(leftLine);
    Coordinate p3 = rightSidePt.equals2D(oppositePt) ? oppositePt.copy() 
        : oppLine.lineIntersection(rightLine);
    
    LinearRing shell = factory.createLinearRing(
        new Coordinate[] { p0, p1, p2, p3, p0.copy() });
    return factory.createPolygon(shell);
  }

  /**
   * Computes the constant C in the standard line equation Ax + By = C
   * from A and B and a point on the line.
   * 
   * @param a the X coefficient
   * @param b the Y coefficient
   * @param p a point on the line
   * @return the constant C
   */
  private static double computeLineEquationC(double a, double b, Coordinate p)
  {
    return a * p.y - b * p.x;
  }
  
  private static LineSegment createLineForStandardEquation(double a, double b, double c)
  {
    Coordinate p0;
    Coordinate p1;
    /*
    * Line equation is ax + by = c
    * Slope m = -a/b.
    * Y-intercept = c/b
    * X-intercept = c/a
    * 
    * If slope is low, use constant X values; if high use Y values.
    * This handles lines that are vertical (b = 0, m = Inf ) 
    * and horizontal (a = 0, m = 0).
    */
    if (Math.abs(b) > Math.abs(a)) {
      //-- abs(m) < 1
      p0 = new Coordinate(0.0, c/b);
      p1 = new Coordinate(1.0, c/b - a/b);
    }
    else {
      //-- abs(m) >= 1
      p0 = new Coordinate(c/a, 0.0);
      p1 = new Coordinate(c/a - b/a, 1.0);
    }
    return new LineSegment(p0, p1);
  }
}
