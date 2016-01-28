

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
package org.locationtech.jts.geom;


/**
 *  An interface for classes which use the values of the coordinates in a {@link Geometry}. 
 * Coordinate filters can be used to implement centroid and
 * envelope computation, and many other functions.
 * <p>
 * <code>CoordinateFilter</code> is
 * an example of the Gang-of-Four Visitor pattern. 
 * <p>
 * <b>Note</b>: it is not recommended to use these filters to mutate the coordinates.
 * There is no guarantee that the coordinate is the actual object stored in the geometry.
 * In particular, modified values may not be preserved if the target Geometry uses a non-default {@link CoordinateSequence}.
 * If in-place mutation is required, use {@link CoordinateSequenceFilter}.
 *  
 * @see Geometry#apply(CoordinateFilter)
 * @see CoordinateSequenceFilter
 *
 *@version 1.7
 */
public interface CoordinateFilter {

  /**
   * Performs an operation with the <code>coord</code>.
   * There is no guarantee that the coordinate is the actual object stored in the target geometry.
   *
   *@param  coord  a <code>Coordinate</code> to which the filter is applied.
   */
  void filter(Coordinate coord);
}

