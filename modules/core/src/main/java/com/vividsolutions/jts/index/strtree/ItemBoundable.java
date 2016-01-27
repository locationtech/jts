
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
package com.vividsolutions.jts.index.strtree;

import java.io.Serializable;

/**
 * Boundable wrapper for a non-Boundable spatial object. Used internally by
 * AbstractSTRtree.
 *
 * @version 1.7
 */
public class ItemBoundable implements Boundable, Serializable {
  private Object bounds;
  private Object item;

  public ItemBoundable(Object bounds, Object item) {
    this.bounds = bounds;
    this.item = item;
  }

  public Object getBounds() {
    return bounds;
  }

  public Object getItem() { return item; }
}
