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