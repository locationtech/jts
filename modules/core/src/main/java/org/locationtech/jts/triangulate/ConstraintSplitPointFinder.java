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

package org.locationtech.jts.triangulate;


import org.locationtech.jts.geom.Coordinate;

/**
 * An interface for strategies for determining the location of split points on constraint segments.
 * The location of split points has a large effect on the performance and robustness of enforcing a
 * constrained Delaunay triangulation. Poorly chosen split points can cause repeated splitting,
 * especially at narrow constraint angles, since the split point will end up encroaching on the
 * segment containing the original encroaching point. With detailed knowledge of the geometry of the
 * constraints, it is sometimes possible to choose better locations for splitting.
 * 
 * @author mbdavis
 */
public interface ConstraintSplitPointFinder {
    /**
     * Finds a point at which to split an encroached segment to allow the original segment to appear
     * as edges in a constrained Delaunay triangulation.
     * 
     * @param seg the encroached segment
     * @param encroachPt the encroaching point
     * @return the point at which to split the encroached segment
     */
    Coordinate findSplitPoint(Segment seg, Coordinate encroachPt);
}
