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

package org.locationtech.jts.simplify;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;

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
