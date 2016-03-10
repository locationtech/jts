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
package org.locationtech.jts.algorithm.locate;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;

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
