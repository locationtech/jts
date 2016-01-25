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

package com.vividsolutions.jts.simplify;

import com.vividsolutions.jts.geom.*;

/**
 * A {@link LineSegment} which is tagged with its location in a parent {@link Geometry}.
 * Used to index the segments in a geometry and recover the segment locations
 * from the index.
 */
class TaggedLineSegment
    extends LineSegment
{
  private Geometry parent;
  private int index;

  public TaggedLineSegment(Coordinate p0, Coordinate p1, Geometry parent, int index) {
    super(p0, p1);
    this.parent = parent;
    this.index = index;
  }

  public TaggedLineSegment(Coordinate p0, Coordinate p1) {
    this(p0, p1, null, -1);
  }

  public Geometry getParent() { return parent; }
  public int getIndex() { return index; }
}
