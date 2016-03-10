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
 * An interface for classes which locate an edge in a {@link QuadEdgeSubdivision}
 * which either contains a given {@link Vertex} V 
 * or is an edge of a triangle which contains V. 
 * Implementors may utilized different strategies for
 * optimizing locating containing edges/triangles.
 * 
 * @author Martin Davis
 */
public interface QuadEdgeLocator {
    QuadEdge locate(Vertex v);
}