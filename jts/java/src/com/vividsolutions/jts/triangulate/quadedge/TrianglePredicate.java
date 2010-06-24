package com.vividsolutions.jts.triangulate.quadedge;

import com.vividsolutions.jts.algorithm.math.DoubleDouble;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Triangle;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Algorithms for computing values and predicates
 * associated with triangles.
 * For some algorithms extended-precision
 * versions are provided, which are more robust
 * (i.e. they produce correct answers in more cases).
 * 
 * @author Martin Davis
 *
 */
public class TrianglePredicate 
{
  /**
   * Tests if a point is inside the circle defined by the points a, b, c. 
   * This test uses simple
   * double-precision arithmetic, and thus may not be robust.
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param P the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleNonRobust(
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
   * Tests if a point is inside the circle defined by the points a, b, c. 
   * This test uses simple
   * double-precision arithmetic, and thus is not 10% robust.
   * However, by using normalization to the origin
   * it provides improved robustness and increased performance.
   * <p>
   * Based on code by J.R.Shewchuk.
   * 
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param P the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleNormalized(
      Coordinate a, Coordinate b, Coordinate c, 
      Coordinate p) {
    double adx = a.x - p.x;
    double ady = a.y - p.y;
    double bdx = b.x - p.x;
    double bdy = b.y - p.y;
    double cdx = c.x - p.x;
    double cdy = c.y - p.y;

    double abdet = adx * bdy - bdx * ady;
    double bcdet = bdx * cdy - cdx * bdy;
    double cadet = cdx * ady - adx * cdy;
    double alift = adx * adx + ady * ady;
    double blift = bdx * bdx + bdy * bdy;
    double clift = cdx * cdx + cdy * cdy;

    double disc = alift * bcdet + blift * cadet + clift * abdet;
    return disc > 0;
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
   * @param P the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleRobust(
      Coordinate a, Coordinate b, Coordinate c, 
      Coordinate p) 
  {
    //checkRobustInCircle(a, b, c, p);
    return isInCircleNormalized(a, b, c, p);       
  }

  /**
   * Tests if a point is inside the circle defined by the points a, b, c. 
   * The computation uses {@link DoubleDouble} arithmetic for robustness.
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param P the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleDDSlow(
      Coordinate a, Coordinate b, Coordinate c,
      Coordinate p) {
    DoubleDouble px = new DoubleDouble(p.x);
    DoubleDouble py = new DoubleDouble(p.y);
    DoubleDouble ax = new DoubleDouble(a.x);
    DoubleDouble ay = new DoubleDouble(a.y);
    DoubleDouble bx = new DoubleDouble(b.x);
    DoubleDouble by = new DoubleDouble(b.y);
    DoubleDouble cx = new DoubleDouble(c.x);
    DoubleDouble cy = new DoubleDouble(c.y);

    DoubleDouble aTerm = (ax.multiply(ax).add(ay.multiply(ay)))
        .multiply(triAreaDDSlow(bx, by, cx, cy, px, py));
    DoubleDouble bTerm = (bx.multiply(bx).add(by.multiply(by)))
        .multiply(triAreaDDSlow(ax, ay, cx, cy, px, py));
    DoubleDouble cTerm = (cx.multiply(cx).add(cy.multiply(cy)))
        .multiply(triAreaDDSlow(ax, ay, bx, by, px, py));
    DoubleDouble pTerm = (px.multiply(px).add(py.multiply(py)))
        .multiply(triAreaDDSlow(ax, ay, bx, by, cx, cy));

    DoubleDouble sum = aTerm.subtract(bTerm).add(cTerm).subtract(pTerm);
    boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }

  /**
   * Computes twice the area of the oriented triangle (a, b, c), i.e., the area
   * is positive if the triangle is oriented counterclockwise.
   * The computation uses {@link DoubleDouble} arithmetic for robustness.
   * 
   * @param ax the x ordinate of a vertex of the triangle
   * @param ay the y ordinate of a vertex of the triangle
   * @param bx the x ordinate of a vertex of the triangle
   * @param by the y ordinate of a vertex of the triangle
   * @param cx the x ordinate of a vertex of the triangle
   * @param cy the y ordinate of a vertex of the triangle
   */
  public static DoubleDouble triAreaDDSlow(DoubleDouble ax, DoubleDouble ay,
      DoubleDouble bx, DoubleDouble by, DoubleDouble cx, DoubleDouble cy) {
    return (bx.subtract(ax).multiply(cy.subtract(ay)).subtract(by.subtract(ay)
        .multiply(cx.subtract(ax))));
  }

  public static boolean isInCircleDDFast(
      Coordinate a, Coordinate b, Coordinate c,
      Coordinate p) {
    DoubleDouble aTerm = (DoubleDouble.sqr(a.x).selfAdd(DoubleDouble.sqr(a.y)))
        .selfMultiply(triAreaDDFast(b, c, p));
    DoubleDouble bTerm = (DoubleDouble.sqr(b.x).selfAdd(DoubleDouble.sqr(b.y)))
        .selfMultiply(triAreaDDFast(a, c, p));
    DoubleDouble cTerm = (DoubleDouble.sqr(c.x).selfAdd(DoubleDouble.sqr(c.y)))
        .selfMultiply(triAreaDDFast(a, b, p));
    DoubleDouble pTerm = (DoubleDouble.sqr(p.x).selfAdd(DoubleDouble.sqr(p.y)))
        .selfMultiply(triAreaDDFast(a, b, c));

    DoubleDouble sum = aTerm.selfSubtract(bTerm).selfAdd(cTerm).selfSubtract(pTerm);
    boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }

  public static DoubleDouble triAreaDDFast(
      Coordinate a, Coordinate b, Coordinate c) {
    
    DoubleDouble t1 = DoubleDouble.valueOf(b.x).selfSubtract(a.x)
          .selfMultiply(
              DoubleDouble.valueOf(c.y).selfSubtract(a.y));
    
    DoubleDouble t2 = DoubleDouble.valueOf(b.y).selfSubtract(a.y)
          .selfMultiply(
              DoubleDouble.valueOf(c.x).selfSubtract(a.x));
    
    return t1.selfSubtract(t2);
  }

  public static boolean isInCircleDDNormalized(
      Coordinate a, Coordinate b, Coordinate c,
      Coordinate p) {
    DoubleDouble adx = DoubleDouble.valueOf(a.x).selfSubtract(p.x);
    DoubleDouble ady = DoubleDouble.valueOf(a.y).selfSubtract(p.y);
    DoubleDouble bdx = DoubleDouble.valueOf(b.x).selfSubtract(p.x);
    DoubleDouble bdy = DoubleDouble.valueOf(b.y).selfSubtract(p.y);
    DoubleDouble cdx = DoubleDouble.valueOf(c.x).selfSubtract(p.x);
    DoubleDouble cdy = DoubleDouble.valueOf(c.y).selfSubtract(p.y);

    DoubleDouble abdet = adx.multiply(bdy).selfSubtract(bdx.multiply(ady));
    DoubleDouble bcdet = bdx.multiply(cdy).selfSubtract(cdx.multiply(bdy));
    DoubleDouble cadet = cdx.multiply(ady).selfSubtract(adx.multiply(cdy));
    DoubleDouble alift = adx.multiply(adx).selfAdd(ady.multiply(ady));
    DoubleDouble blift = bdx.multiply(bdx).selfAdd(bdy.multiply(bdy));
    DoubleDouble clift = cdx.multiply(cdx).selfAdd(cdy.multiply(cdy));

    DoubleDouble sum = alift.selfMultiply(bcdet)
    .selfAdd(blift.selfMultiply(cadet))
    .selfAdd(clift.selfMultiply(abdet));
    
    boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
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
  boolean nonRobustInCircle = isInCircleNonRobust(a, b, c, p);
  boolean isInCircleDD = TrianglePredicate.isInCircleDDSlow(a, b, c, p);
  boolean isInCircleCC = TrianglePredicate.isInCircleCC(a, b, c, p);

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
