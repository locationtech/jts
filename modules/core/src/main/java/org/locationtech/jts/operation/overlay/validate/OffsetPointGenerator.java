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

package org.locationtech.jts.operation.overlay.validate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.LinearComponentExtracter;

/**
 * Generates points offset by a given distance 
 * from both sides of the midpoint of
 * all segments in a {@link Geometry}.
 * Can be used to generate probe points for
 * determining whether a polygonal overlay result
 * is incorrect.
 * The input geometry may have any orientation for its rings,
 * but {@link #setSidesToGenerate(boolean, boolean)} is
 * only meaningful if the orientation is known.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class OffsetPointGenerator
{
  private Geometry g;
  private boolean doLeft = true; 
  private boolean doRight = true;
  
  public OffsetPointGenerator(Geometry g)
  {
    this.g = g;
  }

  /**
   * Set the sides on which to generate offset points.
   * 
   * @param doLeft
   * @param doRight
   */
  public void setSidesToGenerate(boolean doLeft, boolean doRight)
  {
    this.doLeft = doLeft;
    this.doRight = doRight;
  }
  
  /**
   * Gets the computed offset points.
   *
   * @return List&lt;Coordinate&gt;
   */
  public List getPoints(double offsetDistance)
  {
    List offsetPts = new ArrayList();
    List lines = LinearComponentExtracter.getLines(g);
    for (Iterator i = lines.iterator(); i.hasNext(); ) {
      LineString line = (LineString) i.next();
      extractPoints(line, offsetDistance, offsetPts);
    }
    //System.out.println(toMultiPoint(offsetPts));
    return offsetPts;
  }

  private void extractPoints(LineString line, double offsetDistance, List offsetPts)
  {
    Coordinate[] pts = line.getCoordinates();
    for (int i = 0; i < pts.length - 1; i++) {
    	computeOffsetPoints(pts[i], pts[i + 1], offsetDistance, offsetPts);
    }
  }

  /**
   * Generates the two points which are offset from the 
   * midpoint of the segment <tt>(p0, p1)</tt> by the
   * <tt>offsetDistance</tt>.
   * 
   * @param p0 the first point of the segment to offset from
   * @param p1 the second point of the segment to offset from
   */
  private void computeOffsetPoints(Coordinate p0, Coordinate p1, double offsetDistance, List offsetPts)
  {
    double dx = p1.x - p0.x;
    double dy = p1.y - p0.y;
    double len = Math.sqrt(dx * dx + dy * dy);
    // u is the vector that is the length of the offset, in the direction of the segment
    double ux = offsetDistance * dx / len;
    double uy = offsetDistance * dy / len;

    double midX = (p1.x + p0.x) / 2;
    double midY = (p1.y + p0.y) / 2;

    if (doLeft) {
      Coordinate offsetLeft = new Coordinate(midX - uy, midY + ux);
      offsetPts.add(offsetLeft);
    }
    
    if (doRight) {
      Coordinate offsetRight = new Coordinate(midX + uy, midY - ux);
      offsetPts.add(offsetRight);
    }
  }

}
