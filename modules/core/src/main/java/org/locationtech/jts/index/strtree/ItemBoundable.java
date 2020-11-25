
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
package org.locationtech.jts.index.strtree;

import java.io.Serializable;

/**
 * Boundable wrapper for a non-Boundable spatial object. Used internally by
 * AbstractSTRtree.
 *
 * @version 1.7
 */
public class ItemBoundable<T,B extends Bounds> implements Boundable<B>, Serializable {
  private B bounds;
  private T item;

  public ItemBoundable(B bounds, T item) {
    this.bounds = bounds;
    this.item = item;
  }

  public B getBounds() {
    return bounds;
  }

  public T getItem() { return item; }
}
