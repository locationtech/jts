/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.geom;


/**
 * <code>Geometry</code> classes support the concept of applying
 * a <code>GeometryComponentFilter</code>
 * filter to a geometry.
 * The filter is applied to every component of a geometry,
 * as well as to the geometry itself.
 * (For instance, in a {@link Polygon}, 
 * all the {@link LinearRing} components for the shell and holes are visited,
 * as well as the polygon itself.
 * In order to process only atomic components, 
 * the {@link #filter} method code must
 * explicitly handle only {@link LineString}s, {@link LinearRing}s and {@link Point}s.
 * <p>
 * A <code>GeometryComponentFilter</code> filter can either
 * record information about the <code>Geometry</code>
 * or change the <code>Geometry</code> in some way.
 * <p>
 * <code>GeometryComponentFilter</code>
 * is an example of the Gang-of-Four Visitor pattern.
 *
 *@version 1.7
 */
public interface GeometryComponentFilter {

  /**
   * Performs an operation with or on a geometry component.
   *
   * @param geom a component of the geometry to which the filter is applied.
   */
  void filter(Geometry geom);
}

