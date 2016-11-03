/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package test.jts.perf.math;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.math.DD;

/**
 * Algorithms for computing values and predicates
 * associated with triangles.
 * For some algorithms extended-precision
 * versions are provided, which are more robust
 * (i.e. they produce correct answers in more cases).
 * These are used in triangulation algorithms.
 * 
 * @author Martin Davis
 *
 */
public class TriPredicate 
{
  /**
   * Tests if a point is inside the circle defined by the points a, b, c. 
   * This test uses simple
   * double-precision arithmetic, and thus may not be robust.
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircle(
      Coordinate a, Coordinate b, Coordinate c, 
      Coordinate p) {
    boolean isInCircle = 
              (a.x * a.x + a.y * a.y) * triArea(b, c, p)
            - (b.x * b.x + b.y * b.y) * triArea(a, c, p)
            + (c.x * c.x + c.y * c.y) * triArea(a, b, p)
            - (p.x * p.x + p.y * p.y) * triArea(a, b, c) 
            > 0;
    return isInCircle;
  }
  
  /**
   * Computes twice the area of the oriented triangle (a, b, c), i.e., the area is positive if the
   * triangle is oriented counterclockwise.
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   */
  private static double triArea(Coordinate a, Coordinate b, Coordinate c) {
      return (b.x - a.x) * (c.y - a.y) 
           - (b.y - a.y) * (c.x - a.x);
  }

  /**
   * Tests if a point is inside the circle defined by the points a, b, c. 
   * This test uses robust computation.
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleRobust(
      Coordinate a, Coordinate b, Coordinate c, 
      Coordinate p) 
  {
    //checkRobustInCircle(a, b, c, p);
    return isInCircleDD(a, b, c, p);       
  }

  /**
   * Tests if a point is inside the circle defined by the points a, b, c. 
   * The computation uses {@link DD} arithmetic for robustness.
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleDD(
      Coordinate a, Coordinate b, Coordinate c,
      Coordinate p) {
    DD px = new DD(p.x);
    DD py = new DD(p.y);
    DD ax = new DD(a.x);
    DD ay = new DD(a.y);
    DD bx = new DD(b.x);
    DD by = new DD(b.y);
    DD cx = new DD(c.x);
    DD cy = new DD(c.y);

    DD aTerm = (ax.multiply(ax).add(ay.multiply(ay)))
        .multiply(triAreaDD(bx, by, cx, cy, px, py));
    DD bTerm = (bx.multiply(bx).add(by.multiply(by)))
        .multiply(triAreaDD(ax, ay, cx, cy, px, py));
    DD cTerm = (cx.multiply(cx).add(cy.multiply(cy)))
        .multiply(triAreaDD(ax, ay, bx, by, px, py));
    DD pTerm = (px.multiply(px).add(py.multiply(py)))
        .multiply(triAreaDD(ax, ay, bx, by, cx, cy));

    DD sum = aTerm.subtract(bTerm).add(cTerm).subtract(pTerm);
    boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }
  public static boolean isInCircleDD2(
      Coordinate a, Coordinate b, Coordinate c,
      Coordinate p) {
    DD aTerm = (DD.sqr(a.x).selfAdd(DD.sqr(a.y)))
        .selfMultiply(triAreaDD2(b, c, p));
    DD bTerm = (DD.sqr(b.x).selfAdd(DD.sqr(b.y)))
        .selfMultiply(triAreaDD2(a, c, p));
    DD cTerm = (DD.sqr(c.x).selfAdd(DD.sqr(c.y)))
        .selfMultiply(triAreaDD2(a, b, p));
    DD pTerm = (DD.sqr(p.x).selfAdd(DD.sqr(p.y)))
        .selfMultiply(triAreaDD2(a, b, c));

    DD sum = aTerm.selfSubtract(bTerm).selfAdd(cTerm).selfSubtract(pTerm);
    boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }
  public static boolean isInCircleDD3(
      Coordinate a, Coordinate b, Coordinate c,
      Coordinate p) {
    DD adx = DD.valueOf(a.x).selfSubtract(p.x);
    DD ady = DD.valueOf(a.y).selfSubtract(p.y);
    DD bdx = DD.valueOf(b.x).selfSubtract(p.x);
    DD bdy = DD.valueOf(b.y).selfSubtract(p.y);
    DD cdx = DD.valueOf(c.x).selfSubtract(p.x);
    DD cdy = DD.valueOf(c.y).selfSubtract(p.y);

    DD abdet = adx.multiply(bdy).selfSubtract(bdx.multiply(ady));
    DD bcdet = bdx.multiply(cdy).selfSubtract(cdx.multiply(bdy));
    DD cadet = cdx.multiply(ady).selfSubtract(adx.multiply(cdy));
    DD alift = adx.multiply(adx).selfSubtract(ady.multiply(ady));
    DD blift = bdx.multiply(bdx).selfSubtract(bdy.multiply(bdy));
    DD clift = cdx.multiply(cdx).selfSubtract(cdy.multiply(cdy));

    DD sum = alift.selfMultiply(bcdet)
    .selfAdd(blift.selfMultiply(cadet))
    .selfAdd(clift.selfMultiply(abdet));
    
    boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }

  /**
   * Computes twice the area of the oriented triangle (a, b, c), i.e., the area
   * is positive if the triangle is oriented counterclockwise.
   * The computation uses {@link DD} arithmetic for robustness.
   * 
   * @param ax the x ordinate of a vertex of the triangle
   * @param ay the y ordinate of a vertex of the triangle
   * @param bx the x ordinate of a vertex of the triangle
   * @param by the y ordinate of a vertex of the triangle
   * @param cx the x ordinate of a vertex of the triangle
   * @param cy the y ordinate of a vertex of the triangle
   */
  public static DD triAreaDD(DD ax, DD ay,
      DD bx, DD by, DD cx, DD cy) {
    return (bx.subtract(ax).multiply(cy.subtract(ay)).subtract(by.subtract(ay)
        .multiply(cx.subtract(ax))));
  }

  public static DD triAreaDD2(
      Coordinate a, Coordinate b, Coordinate c) {
    
    DD t1 = DD.valueOf(b.x).selfSubtract(a.x)
          .selfMultiply(
              DD.valueOf(c.y).selfSubtract(a.y));
    
    DD t2 = DD.valueOf(b.y).selfSubtract(a.y)
          .selfMultiply(
              DD.valueOf(c.x).selfSubtract(a.x));
    
    return t1.selfSubtract(t2);
  }

  /**
   * Computes the inCircle test using distance from the circumcentre. 
   * Uses standard double-precision arithmetic.
   * <p>
   * In general this doesn't
   * appear to be any more robust than the standard calculation. However, there
   * is at least one case where the test point is far enough from the
   * circumcircle that this test gives the correct answer. 
   * <pre>
   * LINESTRING
   * (1507029.9878 518325.7547, 1507022.1120341457 518332.8225183258,
   * 1507029.9833 518325.7458, 1507029.9896965567 518325.744909031)
   * </pre>
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleCC(Coordinate a, Coordinate b, Coordinate c,
      Coordinate p) {
    Coordinate cc = Triangle.circumcentre(a, b, c);
    double ccRadius = a.distance(cc);
    double pRadiusDiff = p.distance(cc) - ccRadius;
    return pRadiusDiff <= 0;
  }
  
  /**
   * Checks if the computed value for isInCircle is correct, using
   * double-double precision arithmetic.
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   */
private static void checkRobustInCircle(Coordinate a, Coordinate b, Coordinate c,
    Coordinate p) 
{
  boolean nonRobustInCircle = isInCircle(a, b, c, p);
  boolean isInCircleDD = TriPredicate.isInCircleDD(a, b, c, p);
  boolean isInCircleCC = TriPredicate.isInCircleCC(a, b, c, p);

  Coordinate circumCentre = Triangle.circumcentre(a, b, c);
  System.out.println("p radius diff a = "
      + Math.abs(p.distance(circumCentre) - a.distance(circumCentre))
      / a.distance(circumCentre));

  if (nonRobustInCircle != isInCircleDD || nonRobustInCircle != isInCircleCC) {
    System.out.println("inCircle robustness failure (double result = "
        + nonRobustInCircle 
        + ", DD result = " + isInCircleDD
        + ", CC result = " + isInCircleCC + ")");
    System.out.println(WKTWriter.toLineString(new CoordinateArraySequence(
        new Coordinate[] { a, b, c, p })));
    System.out.println("Circumcentre = " + WKTWriter.toPoint(circumCentre)
        + " radius = " + a.distance(circumCentre));
    System.out.println("p radius diff a = "
        + Math.abs(p.distance(circumCentre)/a.distance(circumCentre) - 1));
    System.out.println("p radius diff b = "
        + Math.abs(p.distance(circumCentre)/b.distance(circumCentre) - 1));
    System.out.println("p radius diff c = "
        + Math.abs(p.distance(circumCentre)/c.distance(circumCentre) - 1));
    System.out.println();
  }
}


}
