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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.coverage.CoverageRingEdges;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts the set of unique coverage edges from a polygonal coverage.
 * The coverage edges are returned as an array of linear geometries.
 * The input coverage should be valid according to {@link CoverageValidator}.
 *
 * @author Nick Bowsher
 */

public class CoverageEdgeExtractor {
    /*
     * Extracts the set of unique coverage edges from a polygonal coverage.
     *
     * @param coverage an array of polygons forming a coverage
     * @return an array of linear geometries representing coverage edges
     */
    public static Geometry[] extract(Geometry[] coverage) {
        CoverageEdgeExtractor e = new CoverageEdgeExtractor(coverage);
        return e.extract();
    }

    /*
     * Creates a new coverage edge extractor
     *
     * @param coverage an array of polygons forming a coverage
     */
    public CoverageEdgeExtractor(Geometry[] coverage) {
        this.coverage = coverage;
    }

    /*
     * Extracts the set of unique coverage edges from a polygonal coverage.
     * The result is an array of the same size as the input coverage.
     *
     * @return an array of linear geometries representing coverage edges
     */
    public Geometry[] extract() {
        CoverageRingEdges covRings = new CoverageRingEdges(coverage);
        GeometryFactory f = new GeometryFactory();
        List<LineString> lines = new ArrayList<LineString>();
        for (CoverageEdge edge : covRings.getEdges()) {
            lines.add(edge.toLineString(f));
        }
        return GeometryFactory.toLineStringArray(lines);
    }

    private Geometry[] coverage;
}
