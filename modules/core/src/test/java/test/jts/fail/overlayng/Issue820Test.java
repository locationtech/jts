package test.jts.fail.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Intersection produces point geometry instead of polygonal.
 * 
 * See https://github.com/locationtech/jts/issues/820
 *
 */
public class Issue820Test extends TestCase {
  
  public static void main(String args[]) {
    TestRunner.run(Issue820Test.class);
  }
  
  public void testIntersection() {
    Geometry smallerArea = new GeometryFactory().createPolygon(new Coordinate[]{
            new Coordinate(7.04972080711741E-9d, 0.0d),
            new Coordinate(2.2101801468358406E-9d, 1.4126466987753772d),
            new Coordinate(1.2696784445936622d, 1.41264669888319d),
            new Coordinate(1.2696784494332027d, 1.078129033360885E-10d),
            new Coordinate(7.04972080711741E-9d, 0.0d),
    });
    Polygon biggerArea = new GeometryFactory().createPolygon(new Coordinate[]{
            new Coordinate(7.04972080711741E-9, 0.0),
            new Coordinate(0.0, 2.0577913328003916),
            new Coordinate(4.498851115482424, 7.063229687109256),
            new Coordinate(13.596527845590671, 7.06322968918812),
            new Coordinate(19.94490668468672, 1.693592821538914E-9),
            new Coordinate(7.04972080711741E-9, 0.0)
    });

    Geometry intersection = OverlayNGRobust.overlay(smallerArea, biggerArea, OverlayNG.INTERSECTION);

    // Expected: POLYGON ((0.0000000022101801 1.4126466987753772, 1.2696784445936622 1.41264669888319, 1.2696784494332027 0.0000000001078129, 0.0000000070497208 0, 0.0000000022101801 1.4126466987753772))
    // Actual: MULTIPOINT ((0.0000000022101801 1.4126466987753772), (0.0000000070497208 0))

    assertTrue(intersection instanceof Polygonal);
  }
}
