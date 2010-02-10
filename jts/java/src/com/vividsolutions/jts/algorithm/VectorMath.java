/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.vividsolutions.jts.algorithm;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Functions for performing vector mathematics.
 * 
 * @author Martin Davis
 * @version 1.0
 */

public class VectorMath 
{
    /**
     * Computes the normal vector to the triangle p0-p1-p2. In order to compute the normal each
     * triangle coordinate must have a Z value. If this is not the case, the returned Coordinate
     * will have NaN values. The returned vector has unit length.
     * 
     * @param p0
     * @param p1
     * @param p2
     * @return
     */
    public static Coordinate normalToTriangle(Coordinate p0, Coordinate p1, Coordinate p2) {
        Coordinate v1 = new Coordinate(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
        Coordinate v2 = new Coordinate(p2.x - p0.x, p2.y - p0.y, p2.z - p0.z);
        Coordinate cp = crossProduct(v1, v2);
        normalize(cp);
        return cp;
    }

    public static void normalize(Coordinate v) {
        double absVal = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        v.x /= absVal;
        v.y /= absVal;
        v.z /= absVal;
    }

    public static Coordinate crossProduct(Coordinate v1, Coordinate v2) {
        double x = det(v1.y, v1.z, v2.y, v2.z);
        double y = -det(v1.x, v1.z, v2.x, v2.z);
        double z = det(v1.x, v1.y, v2.x, v2.y);
        return new Coordinate(x, y, z);
    }

    public static double dotProduct(Coordinate v1, Coordinate v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public static double det(double a1, double a2, double b1, double b2) {
        return (a1 * b2) - (a2 * b1);
    }
}