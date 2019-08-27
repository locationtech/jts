package org.locationtech.jts.operation.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;

public class IndexedFacetDistanceTest extends BaseDistanceTest {

  public static void main(String args[]) {
    TestRunner.run(IndexedFacetDistanceTest.class);
  }
  
  public IndexedFacetDistanceTest(String name) {
    super(name);
  }

  protected Coordinate[] nearestPoints(Geometry g1, Geometry g2) {
    return IndexedFacetDistance.nearestPoints(g1, g2);
  }

  public void testClosestPoints7() {
    // skip this test for now, since it relies on checking point-in-polygon
  }

  @Override
  protected double distance(Geometry g1, Geometry g2) {
    return IndexedFacetDistance.distance(g1,g2);
  }

  @Override
  protected boolean isWithinDistance(Geometry g1, Geometry g2, double distance) {
    return IndexedFacetDistance.isWithinDistance(g1,g2, distance);
  }
}
