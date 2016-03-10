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