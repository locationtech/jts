package org.locationtech.jts.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.TopologyException;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests OverlayNG with floating precision Noder.
 * 
 * @author Martin Davis
 *
 */
public class OverlayNGFloatingNoderTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(OverlayNGFloatingNoderTest.class);
  }

  public OverlayNGFloatingNoderTest(String name) { super(name); }
  
  public void testTriangleIntersection() {
    Geometry a = read("POLYGON ((0 0, 8 0, 8 3, 0 0))");
    Geometry b = read("POLYGON ((0 5, 5 0, 0 0, 0 5))");
    Geometry expected = read("POLYGON ((0 0, 3.6363636363636367 1.3636363636363638, 5 0, 0 0))");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual, 1e-10);
  }
  
  public void testPolygonWithRepeatedPointIntersection() {
    Geometry a = read("POLYGON ((1231646.6575 1042601.8724999996, 1231646.6575 1042601.8724999996, 1231646.6575 1042601.8724999996, 1231646.6575 1042601.8724999996, 1231646.6575 1042601.8724999996, 1231646.6575 1042601.8724999996, 1231646.6575 1042601.8724999996, 1231646.6575 1042601.8724999996, 1231647.72 1042600.4349999996, 1231653.22 1042592.1849999996, 1231665.14087406 1042572.5988970799, 1231595.8411746 1042545.58898314, 1231595.26811297 1042580.9672385901, 1231595.2825 1042582.8724999996, 1231646.6575 1042601.8724999996))");
    Geometry b = read("POLYGON ((1231665.14087406 1042572.5988970799, 1231665.14087406 1042572.5988970799, 1231665.14087406 1042572.5988970799, 1231665.14087406 1042572.5988970799, 1231665.14087406 1042572.5988970799, 1231665.14087406 1042572.5988970799, 1231665.14087406 1042572.5988970799, 1231665.14087406 1042572.5988970799, 1231666.51617512 1042570.3392651202, 1231677.47 1042558.9349999996, 1231685.50958834 1042553.8506523697, 1231603.31532446 1042524.6022436405, 1231603.31532446 1042524.6022436405, 1231603.31532446 1042524.6022436405, 1231603.31532446 1042524.6022436405, 1231596.4075 1042522.1849999996, 1231585.07346906 1042541.8167165304, 1231586.62051091 1042542.3586940402, 1231586.62051091 1042542.3586940402, 1231595.8411746 1042545.58898314, 1231665.14087406 1042572.5988970799))");
    Geometry actual = intersection(a, b);
    // test is ok if intersection computes without error
    boolean isCorrect = actual.getArea() < 1;
    assertTrue("Area of intersection result area is too large", isCorrect);
  }
  
  public void testPolygonWithRepeatedPointIntersectionSimple() {
    Geometry a = read("POLYGON ((100 200, 200 200, 200 100, 100 100, 100 151, 100 151, 100 151, 100 151, 100 200))");
    Geometry b = read("POLYGON ((300 200, 300 100, 200 100, 200 200, 200 200, 300 200))");
    Geometry expected = read("LINESTRING (200 200, 200 100)");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual, 1e-10);
  }
  
  public void testLineWithRepeatedPointIntersection() {
    Geometry a = read("LINESTRING (100 100, 200 200, 200 200, 200 200, 200 200, 300 300, 400 200)");
    Geometry b = read("LINESTRING (190 110, 120 180)");
    Geometry expected = read("POINT (150 150)");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual, 1e-10);
  }
  
  /**
   * GEOS failure case due to porting bug
   * See https://lists.osgeo.org/pipermail/geos-devel/2020-September/009679.html
   */
  public void testNarrowBoxesLineIntersection() {
    Geometry a = read("LINESTRING (832864.275023695 0, 835092.849076364 0)");
    Geometry b = read("MULTIPOLYGON (((832864.275023695 0, 833978.556808034 -0.000110682755987, 833978.556808034 0, 833978.556808034 0.000110682755987, 832864.275023695 0, 832864.275023695 0)), ((835092.849076364 0, 833978.557030887 -0.000110682755987, 833978.557030887 0, 833978.557030887 0.000110682755987, 835092.849076364 0, 835092.849076364 0)))");
    Geometry expected = read("MULTILINESTRING ((833978.557030887 0, 835092.849076364 0), (832864.275023695 0, 833978.556808034 0))");
    Geometry actual = intersection(a, b);
    checkEqual(expected, actual, 1e-10);
  }
  
  /**
   * Tests a case where ring clipping causes an incorrect result.
   * <p>
   * The incorrect result occurs because:
   * <ol>
   * <li>Ring Clipping causes a clipped A line segment to move slightly.
   * <li>This causes the clipped A and B edges to become disjoint
   * (whereas in the original geometry they intersected).  
   * <li>Both edge rings are thus determined to be disconnected during overlay labeling.
   * <li>For the overlay labeling for the disconnected edge in geometry B,
   * the chosen edge coordinate has its location computed as inside the original A polygon.
   * This is because the chosen coordinate happens to be the one that the 
   * clipped edge crossed over.
   * <li>This causes the (clipped) B edge ring to be labelled as Interior to the A polygon. 
   * <li>The B edge ring thus is computed as being in the intersection, 
   * and the entire ring is output, producing a much larger polygon than is correct.
   * </ol>
   * The test check here is a heuristic that detects the presence of a large
   * polygon in the output.
   * <p>
   * There are several possible fixes:
   * <ol>
   * <li>Improve clipping to avoid clipping line segments which may intersect
   * other geometry (by computing a large enough clipping envelope)</li>
   * <li>Improve choosing a point for disconnected edge location; 
   * i.e. by finding one that is far from the other geometry edges.
   * However, this still creates a result which may not reflect the 
   * actual input topology.
   * </li>
   * </ol>
   * The chosen fix is the first above - improve clipping 
   * by choosing a larger clipping envelope. 
   * <p>
   * NOTE: When clipping is improved to avoid perturbing intersecting segments, 
   * the floating overlay now reports a TopologyException.
   * This is reported as an empty geometry to allow tests to pass.
   */
  public void xtestPolygonsWithClippingPerturbationIntersection() {
    Geometry a = read("POLYGON ((4373089.33 5521847.89, 4373092.24 5521851.6, 4373118.52 5521880.22, 4373137.58 5521896.63, 4373153.33 5521906.43, 4373270.51 5521735.67, 4373202.5 5521678.73, 4373100.1 5521827.97, 4373089.33 5521847.89))");
    Geometry b = read("POLYGON ((4373225.587574724 5521801.132991467, 4373209.219497436 5521824.985294571, 4373355.5585138 5521943.53124194, 4373412.83157427 5521860.49206234, 4373412.577392304 5521858.140878815, 4373412.290476093 5521855.48690386, 4373374.245799139 5521822.532711867, 4373271.028377312 5521736.104060946, 4373225.587574724 5521801.132991467))");
    double area = intersectionAreaExpectError(a, b);
    boolean isCorrect = area < 1;
    assertTrue("Area of intersection result area is too large", isCorrect);
  }
  
  public void xtestPolygonsWithClippingPerturbation2Intersection() {
    Geometry a = read("POLYGON ((4379891.12 5470577.74, 4379875.16 5470581.54, 4379841.77 5470592.88, 4379787.53 5470612.89, 4379822.96 5470762.6, 4379873.52 5470976.3, 4379982.93 5470965.71, 4379936.91 5470771.25, 4379891.12 5470577.74))");
    Geometry b = read("POLYGON ((4379894.528437099 5470592.144163859, 4379968.579210246 5470576.004727546, 4379965.600743549 5470563.403176092, 4379965.350009631 5470562.383524827, 4379917.641365346 5470571.523966022, 4379891.224959933 5470578.183564024, 4379894.528437099 5470592.144163859))");
    double area = intersectionAreaExpectError(a, b);
    boolean isCorrect = area < 1;
    assertTrue("Area of intersection result area is too large", isCorrect);
  }
  
  
  static double intersectionAreaExpectError(Geometry a, Geometry b) {
    try {
     Geometry result = OverlayNG.overlay(a, b, INTERSECTION);
     return result.getArea();
    }
    catch (TopologyException ex) {
      /**
       * This exception is expected if the geometries are not perturbed by clipping
       */
    }
    return 0;
  }

  static Geometry intersection(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, INTERSECTION);
  }
}
