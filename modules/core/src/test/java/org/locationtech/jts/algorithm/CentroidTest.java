package org.locationtech.jts.algorithm;

import junit.framework.TestCase;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

public class CentroidTest extends TestCase {


    public CentroidTest(String name) {
        super(name);
    }

    public void testCentroidMultiPolygon() throws Exception {
        // Verify that the computed centroid of a MultiPolygon is equivalent to the
        // area-weighted average of its components.
        Geometry g = new WKTReader().read(
                "MULTIPOLYGON ((( -92.661322 36.58994900000003, -92.66132199999993 36.58994900000005, -92.66132199999993 36.589949000000004, -92.661322 36.589949, -92.661322 36.58994900000003)), (( -92.65560500000008 36.58708800000005, -92.65560499999992 36.58708800000005, -92.65560499998745 36.587087999992576, -92.655605 36.587088, -92.65560500000008 36.58708800000005 )), (( -92.65512450000065 36.586800000000466, -92.65512449999994 36.58680000000004, -92.65512449998666 36.5867999999905, -92.65512450000065 36.586800000000466 )))"
        );

        double totalArea = g.getArea();
        double cx = 0;
        double cy = 0;

        for(int i = 0; i < g.getNumGeometries(); i++) {
            Geometry component = g.getGeometryN(i);
            double areaFraction = component.getArea() / totalArea;

            Coordinate componentCentroid = component.getCentroid().getCoordinate();

            cx += areaFraction * componentCentroid.x;
            cy += areaFraction * componentCentroid.y;
        }

        assertEquals(new Coordinate(cx, cy), g.getCentroid().getCoordinate());
    }
}