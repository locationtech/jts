
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
package org.locationtech.jts.index.bintree;



import org.locationtech.jts.index.quadtree.DoubleBits;

/**
 * A Key is a unique identifier for a node in a tree.
 * It contains a lower-left point and a level number. The level number
 * is the power of two for the size of the node envelope
 *
 * @version 1.7
 */
public class Key {

  public static int computeLevel(Interval interval)
  {
    double dx = interval.getWidth();
    //int level = BinaryPower.exponent(dx) + 1;
    int level = DoubleBits.exponent(dx) + 1;
    return level;
  }


  // the fields which make up the key
  private double pt = 0.0;
  private int level = 0;
  // auxiliary data which is derived from the key for use in computation
  private Interval interval;

  public Key(Interval interval)
  {
    computeKey(interval);
  }

  public double getPoint() { return pt; }
  public int getLevel() { return level; }
  public Interval getInterval() { return interval; }

  /**
   * return a square envelope containing the argument envelope,
   * whose extent is a power of two and which is based at a power of 2
   */
  public void computeKey(Interval itemInterval)
  {
    level = computeLevel(itemInterval);
    interval = new Interval();
    computeInterval(level, itemInterval);
    // MD - would be nice to have a non-iterative form of this algorithm
    while (! interval.contains(itemInterval)) {
      level += 1;
      computeInterval(level, itemInterval);
    }
  }

  private void computeInterval(int level, Interval itemInterval)
  {
    double size = DoubleBits.powerOf2(level);
    //double size = pow2.power(level);
    pt = Math.floor(itemInterval.getMin() / size) * size;
    interval.init(pt, pt + size);
  }
}
