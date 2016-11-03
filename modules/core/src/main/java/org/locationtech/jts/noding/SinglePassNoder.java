
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
package org.locationtech.jts.noding;

import java.util.Collection;

/**
 * Base class for {@link Noder}s which make a single
 * pass to find intersections.
 * This allows using a custom {@link SegmentIntersector}
 * (which for instance may simply identify intersections, rather than
 * insert them).
 *
 * @version 1.7
 */
public abstract class SinglePassNoder
    implements Noder
{

  protected SegmentIntersector segInt;

  public SinglePassNoder() {
  }

  public SinglePassNoder(SegmentIntersector segInt) {
    setSegmentIntersector(segInt);
  }

  /**
   * Sets the SegmentIntersector to use with this noder.
   * A SegmentIntersector will normally add intersection nodes
   * to the input segment strings, but it may not - it may
   * simply record the presence of intersections.
   * However, some Noders may require that intersections be added.
   *
   * @param segInt
   */
  public void setSegmentIntersector(SegmentIntersector segInt)
  {
    this.segInt = segInt;
  }

  /**
   * Computes the noding for a collection of {@link SegmentString}s.
   * Some Noders may add all these nodes to the input SegmentStrings;
   * others may only add some or none at all.
   *
   * @param segStrings a collection of {@link SegmentString}s to node
   */
  public abstract void computeNodes(Collection segStrings);

  /**
   * Returns a {@link Collection} of fully noded {@link SegmentString}s.
   * The SegmentStrings have the same context as their parent.
   *
   * @return a Collection of SegmentStrings
   */
  public abstract Collection getNodedSubstrings();

}
