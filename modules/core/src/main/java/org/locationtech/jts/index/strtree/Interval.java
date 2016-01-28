
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
package org.locationtech.jts.index.strtree;

import org.locationtech.jts.util.*;

/**
 * A contiguous portion of 1D-space. Used internally by SIRtree.
 * @see SIRtree
 *
 * @version 1.7
 */
public class Interval {

  public Interval(Interval other) {
    this(other.min, other.max);
  }

  public Interval(double min, double max) {
    Assert.isTrue(min <= max);
    this.min = min;
    this.max = max;
  }

  private double min;
  private double max;

  public double getCentre() { return (min+max)/2; }

  /**
   * @return this
   */
  public Interval expandToInclude(Interval other) {
    max = Math.max(max, other.max);
    min = Math.min(min, other.min);
    return this;
  }

  public boolean intersects(Interval other) {
    return !(other.min > max || other.max < min);
  }
  public boolean equals(Object o) {
    if (! (o instanceof Interval)) { return false; }
    Interval other = (Interval) o;
    return min == other.min && max == other.max;
  }
}
