/*
 * Copyright (c) 2024 Nick Bowsher.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.coverage;

import junit.textui.TestRunner;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import test.jts.GeometryTestCase;

public class CoverageEdgeExtractorTest extends GeometryTestCase {
    public static void main(String args[]) {
        TestRunner.run(CoverageEdgeExtractorTest.class);
    }

    public CoverageEdgeExtractorTest(String name) {
        super(name);
    }

    public void testTwoAdjacent() {
        checkEdges("GEOMETRYCOLLECTION (POLYGON ((1 1, 1 6, 6 5, 9 6, 9 1, 1 1)), POLYGON ((1 9, 6 9, 6 5, 1 6, 1 9)))",
                "MULTILINESTRING ((1 6, 1 1, 9 1, 9 6, 6 5), (1 6, 1 9, 6 9, 6 5), (1 6, 6 5))");
    }

    public void testTwoAdjacentWithFilledHole() {
        checkEdges("GEOMETRYCOLLECTION (POLYGON ((1 1, 1 6, 6 5, 9 6, 9 1, 1 1), (2 4, 4 4, 4 2, 2 2, 2 4)), POLYGON ((1 9, 6 9, 6 5, 1 6, 1 9)), POLYGON ((4 2, 2 2, 2 4, 4 4, 4 2)))",
                "MULTILINESTRING ((1 6, 1 1, 9 1, 9 6, 6 5), (1 6, 1 9, 6 9, 6 5), (1 6, 6 5), (2 4, 2 2, 4 2, 4 4, 2 4))");
    }

    public void testHolesAndFillWithDifferentEndpoints() {
        checkEdges("GEOMETRYCOLLECTION (POLYGON ((0 10, 10 10, 10 0, 0 0, 0 10), (1 9, 4 8, 9 9, 9 1, 1 1, 1 9)), POLYGON ((9 9, 1 1, 1 9, 4 8, 9 9)), POLYGON ((1 1, 9 9, 9 1, 1 1)))",
                "MULTILINESTRING ((0 10, 0 0, 10 0, 10 10, 0 10), (1 1, 1 9, 4 8, 9 9), (1 1, 9 1, 9 9), (1 1, 9 9))");
    }

    public void testMultiPolygons() {
        checkEdges("GEOMETRYCOLLECTION (MULTIPOLYGON (((5 9, 2.5 7.5, 1 5, 5 5, 5 9)), ((5 5, 9 5, 7.5 2.5, 5 1, 5 5))), MULTIPOLYGON (((5 9, 6.5 6.5, 9 5, 5 5, 5 9)), ((1 5, 5 5, 5 1, 3.5 3.5, 1 5))))",
                "MULTILINESTRING ((1 5, 2.5 7.5, 5 9), (1 5, 3.5 3.5, 5 1), (1 5, 5 5), (5 1, 5 5), (5 1, 7.5 2.5, 9 5), (5 5, 5 9), (5 5, 9 5), (5 9, 6.5 6.5, 9 5))"
        );
    }

    private void checkEdges(String wkt, String wktExpected) {
        Geometry geom = read(wkt);
        Geometry[] coverage = toArray(geom);
        Geometry[] edges = CoverageEdgeExtractor.extract(coverage);
        MultiLineString edgeLines = toArray(edges, geom.getFactory());
        Geometry expected = read(wktExpected);
        checkEqual(expected, edgeLines);
    }

    private MultiLineString toArray(Geometry[] edges, GeometryFactory geomFactory) {
        LineString[] lines = new LineString[edges.length];
        for (int i = 0; i < edges.length; i++) {
            lines[i] = geomFactory.createLineString(edges[i].getCoordinates());
        }
        return geomFactory.createMultiLineString(lines);
    }

    private static Geometry[] toArray(Geometry geom) {
        Geometry[] geoms = new Geometry[geom.getNumGeometries()];
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            geoms[i] = geom.getGeometryN(i);
        }
        return geoms;
    }
}
