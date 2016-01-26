/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 * 
 * Copyright (C) 2016 Martin Davis
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Martin Davis BSD
 * License v1.0 (found at the root of the repository).
 * 
 */

package com.vividsolutions.jts.index.strtree;


/**
 * A function method which computes the distance
 * between two {@link ItemBoundable}s in an {@link STRtree}.
 * Used for Nearest Neighbour searches.
 * 
 * @author Martin Davis
 *
 */
public interface ItemDistance 
{
  /**
   * Computes the distance between two items.
   * 
   * @param item1
   * @param item2
   * @return the distance between the items
   * 
   * @throws IllegalArgumentException if the metric is not applicable to the arguments
   */
  double distance(ItemBoundable item1, ItemBoundable item2);

}
