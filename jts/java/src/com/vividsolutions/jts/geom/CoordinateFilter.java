

/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.geom;


/**
 * <code>Geometry</code> classes support the concept of applying a
 * coordinate filter to every coordinate in the <code>Geometry</code>. A
 * coordinate filter can be used to record information about each coordinate. 
 * Coordinate filters can be
 * used to implement centroid and
 * envelope computation, and many other functions.<p>
 * <code>CoordinateFilter</code> is
 * an example of the Gang-of-Four Visitor pattern. 
 * <p>
 * <b>Note</b>: it is not recommended to use filters to mutate the coordinates.
 * In particular, modified values may not be preserved if the target Geometry uses a non-default {@link CoordinateSequence}.
 * If in-place mutation is required, use {@link CoordinateSequenceFilter}.
 * In general, Geometrys should be treated as immutable, 
 * and mutation should be performed by creating a new Geometry object (see {@link GeometryEditor} 
 * and {@link GeometryTransformer} for convenient ways to do this).
 *  
 * @see Geometry#apply(CoordinateFilter)
 * @see CoordinateSequenceFilter
 * @see GeometryTransformer
 * @see GeometryEditor
 *
 *@version 1.7
 */
public interface CoordinateFilter {

  /**
   *  Performs an operation with or on <code>coord</code>.
   *
   *@param  coord  a <code>Coordinate</code> to which the filter is applied.
   */
  void filter(Coordinate coord);
}

