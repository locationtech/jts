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
import java.util.LinkedList;

/**
 * A framework to visit sets of edge-connected {@link QuadEdgeTriangle}s in breadth-first order
 * 
 * @author Martin Davis
 * @version 1.0
 */
public class EdgeConnectedTriangleTraversal {
    private LinkedList triQueue = new LinkedList();

    public EdgeConnectedTriangleTraversal() {}

    public void init(QuadEdgeTriangle tri) {
        triQueue.addLast(tri);
    }

    /**
     * Called to initialize the traversal queue with a given set of {@link QuadEdgeTriangle}s
     * 
     * @param tris a collection of QuadEdgeTriangle
     */
    public void init(Collection tris) {
        triQueue.addAll(tris);
    }

    /**
     * Subclasses can call this method to add a triangle to the end of the queue. This is useful for
     * initializing the queue to a chosen set of triangles.
     * 
     * @param tri a triangle
     */
    /*
     * protected void addLast(QuadEdgeTriangle tri) { triQueue.addLast(tri); }
     */

    /**
     * Subclasses call this method to perform the visiting process.
     */
    public void visitAll(TraversalVisitor visitor) {
        while (!triQueue.isEmpty()) {
            QuadEdgeTriangle tri = (QuadEdgeTriangle) triQueue.removeFirst();
            process(tri, visitor);
        }
    }

    private void process(QuadEdgeTriangle currTri, TraversalVisitor visitor) {
        currTri.getNeighbours();
        for (int i = 0; i < 3; i++) {
            QuadEdgeTriangle neighTri = (QuadEdgeTriangle) currTri.getEdge(i).sym().getData();
            if (neighTri == null)
                continue;
            if (visitor.visit(currTri, i, neighTri))
                triQueue.addLast(neighTri);
        }
    }

}