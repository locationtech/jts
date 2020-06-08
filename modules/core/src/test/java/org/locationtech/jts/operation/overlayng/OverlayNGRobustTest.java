package org.locationtech.jts.operation.overlayng;

import static org.locationtech.jts.operation.overlayng.OverlayNG.INTERSECTION;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

/**
 * Tests robustness cases for OverlayNG.
 * 
 * @author Martin Davis
 *
 */
public class OverlayNGRobustTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(OverlayNGRobustTest.class);
  }

  public OverlayNGRobustTest(String name) { super(name); }
  
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
   * <ul>
   * <li>Improve clipping to avoid clipping line segments which may intersect
   * other geometry (by computing a large enough clipping envelope)</li>
   * <li>Improve choosing a point for disconnected edge location; 
   * i.e. by finding one that is far from the other geometry edges.
   * However, this still creates a result which may not reflect the 
   * actual input topology.
   * </li>
   * </ul>
   * 
   */
  public void testPolygonsWithClippingPerturbationIntersection() {
    Geometry a = read("POLYGON ((4373089.33 5521847.89, 4373092.24 5521851.6, 4373118.52 5521880.22, 4373137.58 5521896.63, 4373153.33 5521906.43, 4373270.51 5521735.67, 4373202.5 5521678.73, 4373100.1 5521827.97, 4373089.33 5521847.89))");
    Geometry b = read("POLYGON ((4373225.587574724 5521801.132991467, 4373209.219497436 5521824.985294571, 4373355.5585138 5521943.53124194, 4373412.83157427 5521860.49206234, 4373412.577392304 5521858.140878815, 4373412.290476093 5521855.48690386, 4373374.245799139 5521822.532711867, 4373271.028377312 5521736.104060946, 4373225.587574724 5521801.132991467))");
    Geometry actual = intersection(a, b);
    boolean isCorrect = actual.getArea() < 1;
    assertTrue("Area of intersection result area is too large", isCorrect);
  }
  
  static Geometry intersection(Geometry a, Geometry b) {
    return OverlayNG.overlay(a, b, INTERSECTION);
  }
}
