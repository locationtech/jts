
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
package org.locationtech.jts.operation.distance;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * Represents the location of a point on a Geometry.
 * Maintains both the actual point location 
 * (which may not be exact, if the point is not a vertex) 
 * as well as information about the component
 * and segment index where the point occurs.
 * Locations inside area Geometrys will not have an associated segment index,
 * so in this case the segment index will have the sentinel value of 
 * {@link #INSIDE_AREA}.
 *
 * @version 1.7
 */
public class GeometryLocation
{
  /**
   * A special value of segmentIndex used for locations inside area geometries. 
   * These locations are not located on a segment, 
   * and thus do not have an associated segment index.
   */
  public static final int INSIDE_AREA = -1;

  private Geometry component = null;
  private int segIndex;
  private Coordinate pt = null;

  /**
   * Constructs a GeometryLocation specifying a point on a geometry, as well as the 
   * segment that the point is on 
   * (or {@link #INSIDE_AREA} if the point is not on a segment).
   * 
   * @param component the component of the geometry containing the point
   * @param segIndex the segment index of the location, or INSIDE_AREA
   * @param pt the coordinate of the location
   */
  public GeometryLocation(Geometry component, int segIndex, Coordinate pt)
  {
    this.component = component;
    this.segIndex = segIndex;
    this.pt = pt;
  }

  /**
   * Constructs a GeometryLocation specifying a point inside an area geometry.
   * 
   * @param component the component of the geometry containing the point
   * @param pt the coordinate of the location
   */  
  public GeometryLocation(Geometry component,Coordinate pt)
  {
    this(component, INSIDE_AREA, pt);
  }

  /**
   * Returns the geometry component on (or in) which this location occurs.
   */
  public Geometry getGeometryComponent() { return component; }
  
  /**
   * Returns the segment index for this location. If the location is inside an
   * area, the index will have the value {@link #INSIDE_AREA};
   *
   * @return the segment index for the location, or INSIDE_AREA
   */
  public int getSegmentIndex() { return segIndex; }
  
  /**
   * Returns the {@link Coordinate} of this location.
   */
  public Coordinate getCoordinate() { return pt; }
  
  /**
   * Tests whether this location represents a point inside an area geometry.
   */
  public boolean isInsideArea() { return segIndex == INSIDE_AREA; }
}
