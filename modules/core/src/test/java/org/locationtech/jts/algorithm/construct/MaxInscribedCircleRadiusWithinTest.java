package org.locationtech.jts.algorithm.construct;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class MaxInscribedCircleRadiusWithinTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(MaxInscribedCircleRadiusWithinTest.class);
  }

  public MaxInscribedCircleRadiusWithinTest(String name) { super(name); }
  
  public void testDiamond() {
    String wkt = "POLYGON ((150 250, 50 150, 150 50, 250 150, 150 250))";
    checkRadiusWithin(wkt, 0, false);
    checkRadiusWithin(wkt, 70, false);
    checkRadiusWithin(wkt, 71, true);
    checkRadiusWithin(wkt, 1000, true);
  }

  public void testIsland() {
    String wkt = "POLYGON ((0.041 0.3017, 0.1251 0.3137, 0.3414 0.3297, 0.7217 0.3351, 0.8979 0.3537, 1.0351 0.3838, 1.2432 0.4438, 1.3694 0.4492, 1.5145 0.4325, 1.6167 0.4151, 1.8529 0.4178, 1.94 0.4078, 2.0601 0.3798, 2.2283 0.3784, 2.3084 0.3677, 2.3254 0.347, 2.3414 0.2856, 2.3294 0.2643, 2.2934 0.2483, 2.2633 0.2376, 2.2613 0.2216, 2.2854 0.2069, 2.3414 0.1949, 2.3474 0.1802, 2.3394 0.1615, 2.3034 0.1402, 2.3133 0.1268, 2.3654 0.1228, 2.3855 0.1095, 2.3835 0.0708, 2.2984 0.0541, 2.2844 0.0407, 2.2704 0.0301, 2.2563 0.0327, 2.2203 0.0501, 2.1622 0.0514, 2.1422 0.0461, 2.1382 0.0314, 2.0882 0.0301, 2.0221 0.0407, 1.9761 0.0301, 1.932 0.022, 1.863 0, 1.8409 0.0053, 1.8309 0.0214, 1.8189 0.0147, 1.7869 0.0013, 1.7528 0.004, 1.7108 0.024, 1.6848 0.0547, 1.6888 0.0721, 1.6688 0.0841, 1.6207 0.0948, 1.5907 0.1201, 1.5606 0.1508, 1.4766 0.1628, 1.3054 0.1595, 1.2233 0.1782, 1.0872 0.1742, 0.907 0.1728, 0.7618 0.1935, 0.6317 0.2095, 0.4715 0.2015, 0.3974 0.2055, 0.2282 0.2409, 0.08 0.2382, 0.018 0.2329, 0 0.2422, 0.01 0.2729, 0.022 0.297, 0.041 0.3017))";
    checkRadiusWithin(wkt, 0.1, false);
    checkRadiusWithin(wkt, 0.2, true);
  }

  private void checkRadiusWithin(String wkt, double maxRadius, boolean expected) {
    Geometry geom = read(wkt);
    boolean actual = MaximumInscribedCircle.isRadiusWithin(geom, maxRadius);
    assertEquals(expected, actual);
  }
  
}
