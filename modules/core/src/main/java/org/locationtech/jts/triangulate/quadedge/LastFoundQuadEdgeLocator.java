/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Vivid Solutions
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Vivid Solutions BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package org.locationtech.jts.triangulate.quadedge;

import java.util.Collection;

/**
 * Locates {@link QuadEdge}s in a {@link QuadEdgeSubdivision},
 * optimizing the search by starting in the
 * locality of the last edge found.
 * 
 * @author Martin Davis
 */
public class LastFoundQuadEdgeLocator implements QuadEdgeLocator {
    private QuadEdgeSubdivision subdiv;
    private QuadEdge            lastEdge = null;

    public LastFoundQuadEdgeLocator(QuadEdgeSubdivision subdiv) {
        this.subdiv = subdiv;
        init();
    }

    private void init() {
        lastEdge = findEdge();
    }

    private QuadEdge findEdge() {
        Collection edges = subdiv.getEdges();
        // assume there is an edge - otherwise will get an exception
        return (QuadEdge) edges.iterator().next();
    }

    /**
     * Locates an edge e, such that either v is on e, or e is an edge of a triangle containing v.
     * The search starts from the last located edge amd proceeds on the general direction of v.
     */
    public QuadEdge locate(Vertex v) {
        if (! lastEdge.isLive()) {
            init();
        }

        QuadEdge e = subdiv.locateFromEdge(v, lastEdge);
        lastEdge = e;
        return e;
    }
}