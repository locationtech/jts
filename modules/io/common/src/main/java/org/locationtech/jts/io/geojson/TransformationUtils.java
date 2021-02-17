package org.locationtech.jts.io.geojson;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

public class TransformationUtils {

    /**
     * Transforms a geometry using the Right Hand Rule specifications defined
     * in the latest GeoJSON specification.
     * See <a href="https://tools.ietf.org/html/rfc7946#section-3.1.6">RFC-7946 Specification</a> for more context.
     *
     * @param geometry to be transformed
     * @return Geometry under the Right Hand Rule specifications
     */
    public static Geometry enforceRightHandRuleOrientation(final Geometry geometry) {

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;

            List<Polygon> polygons = new ArrayList<>();
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                final Geometry polygon = multiPolygon.getGeometryN(i);
                polygons.add((Polygon) enforceRightHandRuleOrientation(polygon));
            }

            return new GeometryFactory().createMultiPolygon(polygons.toArray(new Polygon[0]));

        } else if (geometry instanceof Polygon) {
            return enforceRightHandRuleOrientation((Polygon) geometry);

        } else {
            return geometry;
        }
    }

    /**
     * Transforms a polygon using the Right Hand Rule specifications defined
     * in the latest GeoJSON specification.
     * See <a href="https://tools.ietf.org/html/rfc7946#section-3.1.6">RFC-7946 Specification</a> for more context.
     *
     * @param polygon to be transformed
     * @return Polygon under the Right Hand Rule specifications
     */
    public static Polygon enforceRightHandRuleOrientation(Polygon polygon) {
        LinearRing exteriorRing = polygon.getExteriorRing();
        LinearRing exteriorRingEnforced = enforceRightHandRuleOrientation(exteriorRing, true);

        List<LinearRing> interiorRings = new ArrayList<>();
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            interiorRings.add(enforceRightHandRuleOrientation(polygon.getInteriorRingN(i), false));
        }

        return new GeometryFactory(polygon.getPrecisionModel(), polygon.getSRID())
                .createPolygon(exteriorRingEnforced, interiorRings.toArray(new LinearRing[0]));
    }

    /**
     * Transforms a polygon using the Right Hand Rule specifications defined
     * in the latest GeoJSON specification.
     * A linear ring MUST follow the right-hand rule with respect to the
     * area it bounds, i.e., exterior rings are counterclockwise, and
     * holes are clockwise.
     *
     * See <a href="https://tools.ietf.org/html/rfc7946#section-3.1.6">RFC 7946 Specification</a> for more context.
     *
     * @param ring the LinearRing, a constraint specific to Polygons
     * @param isExteriorRing true if the LinearRing is the exterior polygon ring, the one that defiens the boundary
     * @return LinearRing under the Right Hand Rule specifications
     */
    public static LinearRing enforceRightHandRuleOrientation(LinearRing ring, boolean isExteriorRing) {
        final boolean isRingClockWise = !Orientation.isCCW(ring.getCoordinateSequence());

        final LinearRing rightHandRuleRing;
        if (isExteriorRing) {
            rightHandRuleRing = isRingClockWise? ring.reverse() : ring;
        } else {
            rightHandRuleRing = isRingClockWise? ring : ring.reverse();
        }
        return rightHandRuleRing;
    }
}
