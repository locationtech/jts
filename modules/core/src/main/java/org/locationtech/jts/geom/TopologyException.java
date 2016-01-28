


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
package org.locationtech.jts.geom;

/**
 * Indicates an invalid or inconsistent topological situation encountered during processing
 *
 * @version 1.7
 */
public class TopologyException
  extends RuntimeException
{
  private static String msgWithCoord(String msg, Coordinate pt)
  {
    if (pt != null)
      return msg + " [ " + pt + " ]";
    return msg;
  }

  private Coordinate pt = null;

  public TopologyException(String msg)
  {
    super(msg);
  }

  public TopologyException(String msg, Coordinate pt)
  {
    super(msgWithCoord(msg, pt));
    this.pt = new Coordinate(pt);
  }

  public Coordinate getCoordinate() { return pt; }

}
