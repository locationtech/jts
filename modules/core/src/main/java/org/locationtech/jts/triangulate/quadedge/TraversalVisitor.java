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
 * Interface for classes which process triangles visited during travesals of a
 * {@link QuadEdgeSubdivision}
 * 
 * @author Martin Davis
 */
public interface TraversalVisitor {
    /**
     * Visits a triangle during a traversal of a {@link QuadEdgeSubdivision}. An implementation of
     * this method may perform processing on the current triangle. It must also decide whether a
     * neighbouring triangle should be added to the queue so its neighbours are visited. Often it
     * will perform processing on the neighbour triangle as well, in order to mark it as processed
     * (visited) and/or to determine if it should be visited. Note that choosing <b>not</b> to
     * visit the neighbouring triangle is the terminating condition for many traversal algorithms.
     * In particular, if the neighbour triangle has already been visited, it should not be visited
     * again.
     * 
     * @param currTri the current triangle being processed
     * @param edgeIndex the index of the edge in the current triangle being traversed
     * @param neighbTri a neighbouring triangle next in line to visit
     * @return true if the neighbour triangle should be visited
     */
    boolean visit(QuadEdgeTriangle currTri, int edgeIndex, QuadEdgeTriangle neighbTri);
}
