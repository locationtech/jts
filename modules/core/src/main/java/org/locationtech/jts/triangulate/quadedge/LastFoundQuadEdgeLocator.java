/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
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