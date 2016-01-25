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
package com.vividsolutions.jts.algorithm.locate;

import com.vividsolutions.jts.geom.*;

/**
 * An interface for classes which determine the {@link Location} of
 * points in a {@link Geometry}.
 * 
 * @author Martin Davis
 */
public interface PointOnGeometryLocator 
{
  /**
   * Determines the {@link Location} of a point in the {@link Geometry}.
   * 
   * @param p the point to test
   * @return the location of the point in the geometry  
   */
	int locate(Coordinate p);
}
