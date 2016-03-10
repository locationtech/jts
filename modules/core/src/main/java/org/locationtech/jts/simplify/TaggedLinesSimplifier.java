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

import java.util.Collection;
import java.util.Iterator;

/**
 * Simplifies a collection of TaggedLineStrings, preserving topology
 * (in the sense that no new intersections are introduced).
 * This class is essentially just a container for the common
 * indexes used by {@link TaggedLineStringSimplifier}.
 */
class TaggedLinesSimplifier
{
  private LineSegmentIndex inputIndex = new LineSegmentIndex();
  private LineSegmentIndex outputIndex = new LineSegmentIndex();
  private double distanceTolerance = 0.0;

  public TaggedLinesSimplifier()
  {

  }

  /**
   * Sets the distance tolerance for the simplification.
   * All vertices in the simplified geometry will be within this
   * distance of the original geometry.
   *
   * @param distanceTolerance the approximation tolerance to use
   */
  public void setDistanceTolerance(double distanceTolerance) {
    this.distanceTolerance = distanceTolerance;
  }

  /**
   * Simplify a collection of TaggedLineStrings
   *
   * @param taggedLines the collection of lines to simplify
   */
  public void simplify(Collection taggedLines) {
    for (Iterator i = taggedLines.iterator(); i.hasNext(); ) {
      inputIndex.add((TaggedLineString) i.next());
    }
    for (Iterator i = taggedLines.iterator(); i.hasNext(); ) {
      TaggedLineStringSimplifier tlss
                    = new TaggedLineStringSimplifier(inputIndex, outputIndex);
      tlss.setDistanceTolerance(distanceTolerance);
      tlss.simplify((TaggedLineString) i.next());
    }
  }

}
