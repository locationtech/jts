

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
package org.locationtech.jts.geom;


/**
 *  <code>GeometryCollection</code> classes support the concept of
 *  applying a <code>GeometryFilter</code> to the <code>Geometry</code>.
 *  The filter is applied to every element <code>Geometry</code>.
 *  A <code>GeometryFilter</code> can either record information about the <code>Geometry</code>
 *  or change the <code>Geometry</code> in some way.
 *  <code>GeometryFilter</code>
 *  is an example of the Gang-of-Four Visitor pattern.
 *
 *@version 1.7
 */
public interface GeometryFilter {

  /**
   *  Performs an operation with or on <code>geom</code>.
   *
   *@param  geom  a <code>Geometry</code> to which the filter is applied.
   */
  void filter(Geometry geom);
}

