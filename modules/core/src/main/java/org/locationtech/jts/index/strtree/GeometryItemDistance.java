/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.index.strtree;

import org.locationtech.jts.geom.Geometry;

/**
 * An {@link ItemDistance} function for 
 * items which are {@link Geometry}s,
 * using the {@link Geometry#distance(Geometry)} method.
 * <p>
 * To make this distance function suitable for
 * using to query a single index tree,
 * the distance metric is <i>anti-reflexive</i>.
 * That is, if the two arguments are the same Geometry object,
 * the distance returned is {@link Double#MAX_VALUE}.
 * 
 * @author Martin Davis
 *
 */
public class GeometryItemDistance<B extends Bounds>
implements ItemDistance<Geometry,B>
{
  /**
   * Computes the distance between two {@link Geometry} items,
   * using the {@link Geometry#distance(Geometry)} method.
   * 
   * @param item1 an item which is a Geometry
   * @param item2 an item which is a Geometry
   * @return the distance between the geometries
   */
  public double distance(ItemBoundable<Geometry,B> item1, ItemBoundable<Geometry,B> item2) {
    if (item1 == item2) return Double.MAX_VALUE;
    Geometry g1 = item1.getItem();
    Geometry g2 = item2.getItem();
    return g1.distance(g2);    
  }
}

