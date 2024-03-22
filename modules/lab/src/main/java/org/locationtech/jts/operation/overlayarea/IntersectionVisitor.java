/*
 * Copyright (c) 2022 Martin Davis, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayarea;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentString;

/**
 * Computes the partial overlay area of two polygons by summing the contributions of
 * the edge vectors created by the intersections between the edges of the two polygons.
 */
class IntersectionVisitor implements SegmentIntersector {

    private static final LineIntersector li = new RobustLineIntersector();

    private double area = 0.0;

    double getArea() {
        return area;
    }

    @Override
    public void processIntersections(SegmentString a, int aIndex, SegmentString b, int bIndex) {
        boolean isCCWA = (boolean) a.getData();
        boolean isCCWB = (boolean) b.getData();

        Coordinate a0 = a.getCoordinate(aIndex);
        Coordinate a1 = a.getCoordinate(aIndex + 1);
        Coordinate b0 = b.getCoordinate(bIndex);
        Coordinate b1 = b.getCoordinate(bIndex + 1);

        if (isCCWA) {
            Coordinate tmp = a0;
            a0 = a1;
            a1 = tmp;
        }
        if (isCCWB) {
            Coordinate tmp = b0;
            b0 = b1;
            b1 = tmp;
        }

        li.computeIntersection(a0, a1, b0, b1);
        if (!li.hasIntersection()) return;

        if (li.isProper() || li.isInteriorIntersection()) {
            // Edge-edge intersection OR vertex-edge intersection

            /**
             * An intersection creates two edge vectors which contribute to the area.
             *
             * With both rings oriented CW (effectively)
             * There are two situations for segment intersection:
             *
             * 1) A entering B, B exiting A => rays are IP->A1:R, IP->B0:L
             * 2) A exiting B, B entering A => rays are IP->A0:L, IP->B1:R
             * (where IP is the intersection point,
             * and  :L/R indicates result polygon interior is to the Left or Right).
             *
             * For accuracy the full edge is used to provide the direction vector.
             */

            Coordinate intPt = li.getIntersection(0);

            if (Orientation.CLOCKWISE == Orientation.index(a0, a1, b0)) {
                if (intPt.equals2D(a1)) {
                    // Intersection at vertex and A0 -> A1 is outside the intersection area.
                    // Area will be computed by the segment A1 -> A2
                    return;
                }
                area += EdgeVector.area2Term(intPt, a0, a1, true);
                area += EdgeVector.area2Term(intPt, b1, b0, false);
            } else if (Orientation.CLOCKWISE == Orientation.index(a0, a1, b1)) {
                if (intPt.equals2D(a0)) {
                    // Intersection at vertex and A0 -> A1 is outside the intersection area.
                    // Area will be computed by the segment A(-1) -> A0
                    return;
                }
                area += EdgeVector.area2Term(intPt, a1, a0, false);
                area += EdgeVector.area2Term(intPt, b0, b1, true);
            }

        } else {
            // vertex-vertex intersection
            // This intersection is visited 4 times - include only once
            if (!a1.equals2D(b1)) {
                return;
            }

            // If A0->A1 is collinear with B0->B1, then the intersection point from LineIntersector might not be equal to A1 and B1
            Coordinate intPt = a1;

            Coordinate a2 = a.nextInRing(aIndex + 1);
            Coordinate b2 = b.nextInRing(bIndex + 1);
            if (isCCWA) {
                a2 = a.prevInRing(aIndex);
            }
            if (isCCWB) {
                b2 = b.prevInRing(bIndex);
            }

            double aAngle = Angle.interiorAngle(a0, intPt, a2);
            double bAngle = Angle.interiorAngle(b0, intPt, b2);

            // The LTE ja LT are chosen such that when A0->A1 is collinear with B0->B1,
            // or when A1->A2 is collinear with B1->B2, then A is chosen.
            // This avoids double counting in the case of collinear segments.
            if (Angle.interiorAngle(a0, intPt, b2) <= bAngle) {
                area += EdgeVector.area2Term(intPt, a1, a0, false);
            }
            if (Angle.interiorAngle(b0, intPt, a2) <= bAngle) {
                area += EdgeVector.area2Term(intPt, a1, a2, true);
            }
            if (Angle.interiorAngle(b0, intPt, a2) < aAngle) {
                area += EdgeVector.area2Term(intPt, b1, b0, false);
            }
            if (Angle.interiorAngle(a0, intPt, b2) < aAngle) {
                area += EdgeVector.area2Term(intPt, b1, b2, true);
            }
        }
    }

    @Override
    public boolean isDone() {
        // Process all intersections
        return false;
    }
}
