
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

/**
 * A spatial object in an AbstractSTRtree.
 *
 * @version 1.7
 */
public interface Boundable {
  /**
   * Returns a representation of space that encloses this Boundable, preferably
   * not much bigger than this Boundable's boundary yet fast to test for intersection
   * with the bounds of other Boundables. The class of object returned depends
   * on the subclass of AbstractSTRtree.
   * @return an Envelope (for STRtrees), an Interval (for SIRtrees), or other object
   * (for other subclasses of AbstractSTRtree)
   */
  Object getBounds();
}
