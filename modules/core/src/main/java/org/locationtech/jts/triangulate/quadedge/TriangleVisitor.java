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

/**
 * An interface for algorithms which process the triangles in a {@link QuadEdgeSubdivision}.
 * 
 * @author Martin Davis
 * @version 1.0
 */
public interface TriangleVisitor {
    /**
     * Visits the {@link QuadEdge}s of a triangle.
     * 
     * @param triEdges an array of the 3 quad edges in a triangle (in CCW order)
     */
    void visit(QuadEdge[] triEdges);
}